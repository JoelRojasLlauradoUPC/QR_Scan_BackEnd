package edu.upc.dsa;

import edu.upc.dsa.event.config.DatabaseInitializer;
import edu.upc.dsa.event.config.PersistenceProvider;
import io.swagger.jaxrs.config.BeanConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.sql.SQLException;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = buildBaseUri();
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        if (!PersistenceProvider.isMongo()) {
            try {
                DatabaseInitializer.initialize();
            } catch (SQLException ex) {
                throw new RuntimeException("No se pudo inicializar la base de datos", ex);
            }
        }

        // Only expose the new event-access API modules.
        final ResourceConfig rc = new ResourceConfig().packages("edu.upc.dsa.event");

        rc.register(io.swagger.jaxrs.listing.ApiListingResource.class);
        rc.register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        BeanConfig beanConfig = new BeanConfig();

        beanConfig.setHost(resolveSwaggerHost());
        beanConfig.setBasePath("/api");
        beanConfig.setContact("support@example.com");
        beanConfig.setDescription("REST API para acceso y verificacion de entradas mediante QR");
        beanConfig.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html");
        beanConfig.setResourcePackage("edu.upc.dsa.event.services");
        beanConfig.setTermsOfServiceUrl("http://www.example.com/resources/eula");
        beanConfig.setTitle("QR Event Access API");
        beanConfig.setVersion("1.0.0");
        beanConfig.setScan(true);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }


    /**
     * Main method.
     * @param args argumentos de inicio
     * @throws InterruptedException si el hilo principal se interrumpe
     */
    public static void main(String[] args) throws InterruptedException {
        final HttpServer server = startServer();

        StaticHttpHandler staticHttpHandler = new StaticHttpHandler("./public/");
        server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

        System.out.println("Jersey app started with WADL available at "
                + BASE_URI + "application.wadl");

        Thread.currentThread().join();
    }

    private static String buildBaseUri() {
        String host = readHost();
        int port = readPort();
        return "http://" + host + ":" + port + "/api/";
    }

    private static String resolveSwaggerHost() {
        String externalHostname = System.getenv("RENDER_EXTERNAL_HOSTNAME");
        if (externalHostname != null && !externalHostname.trim().isEmpty()) {
            return externalHostname.trim();
        }
        return "localhost:" + readPort();
    }

    private static int readPort() {
        String rawPort = System.getenv("PORT");
        if (rawPort == null || rawPort.trim().isEmpty()) {
            return 8080;
        }
        try {
            return Integer.parseInt(rawPort.trim());
        } catch (NumberFormatException ex) {
            return 8080;
        }
    }

    private static String readHost() {
        String value = System.getenv("HOST");
        if (value == null || value.trim().isEmpty()) {
            return "0.0.0.0";
        }
        return value.trim();
    }
}

