package edu.upc.dsa.event.services;

import edu.upc.dsa.event.exception.ApiException;
import edu.upc.dsa.event.model.Ticket;
import edu.upc.dsa.event.repository.JdbcTicketRepository;
import edu.upc.dsa.event.service.EventManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Api(value = "/event", description = "Acceso y verificacion de entradas de evento")
@Path("/event")
@Produces(MediaType.APPLICATION_JSON)
public class EventService {

    private final EventManager eventManager;

    public EventService() {
        this(new EventManager(new JdbcTicketRepository()));
    }

    EventService(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @GET
    @Path("/verify/{hash}")
    @ApiOperation(value = "Consulta una entrada por hash sin consumirla")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Entrada encontrada", response = Ticket.class),
            @ApiResponse(code = 404, message = "Entrada no encontrada")
    })
    public Response verify(@PathParam("hash") String hash) {
        try {
            Ticket ticket = eventManager.verify(hash);
            return Response.ok(ticket).build();
        } catch (SQLException ex) {
            throw dbError(ex, "Error al consultar la base de datos");
        }
    }

    @GET
    @Path("/enter/{hash}")
    @ApiOperation(value = "Valida el acceso y consume la entrada")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Entrada validada", response = Ticket.class),
            @ApiResponse(code = 404, message = "Entrada no encontrada"),
            @ApiResponse(code = 409, message = "Entrada ya usada")
    })
    public Response enter(@PathParam("hash") String hash) {
        try {
            Ticket ticket = eventManager.enter(hash);
            return Response.ok(ticket).build();
        } catch (SQLException ex) {
            throw dbError(ex, "Error al actualizar la base de datos");
        }
    }

    @GET
    @Path("/tickets")
    @ApiOperation(value = "Lista todas las entradas")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Listado de entradas", response = Ticket.class, responseContainer = "List")
    })
    public Response listAllTickets() {
        try {
            List<Ticket> tickets = eventManager.listAllTickets();
            GenericEntity<List<Ticket>> entity = new GenericEntity<List<Ticket>>(tickets) {
            };
            return Response.ok(entity).build();
        } catch (SQLException ex) {
            throw dbError(ex, "Error al consultar la base de datos");
        }
    }

    @GET
    @Path("/tickets/email/{email}")
    @ApiOperation(value = "Lista entradas por correo electronico")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Listado por correo", response = Ticket.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Correo invalido")
    })
    public Response listTicketsByEmail(@PathParam("email") String email) {
        try {
            List<Ticket> tickets = eventManager.listTicketsByEmail(email);
            GenericEntity<List<Ticket>> entity = new GenericEntity<List<Ticket>>(tickets) {
            };
            return Response.ok(entity).build();
        } catch (SQLException ex) {
            throw dbError(ex, "Error al consultar la base de datos");
        }
    }

    @GET
    @Path("/tickets/used")
    @ApiOperation(value = "Lista todas las entradas usadas")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Listado de entradas usadas", response = Ticket.class, responseContainer = "List")
    })
    public Response listUsedTickets() {
        try {
            List<Ticket> tickets = eventManager.listUsedTickets();
            GenericEntity<List<Ticket>> entity = new GenericEntity<List<Ticket>>(tickets) {
            };
            return Response.ok(entity).build();
        } catch (SQLException ex) {
            throw dbError(ex, "Error al consultar la base de datos");
        }
    }

    @GET
    @Path("/tickets/unused")
    @ApiOperation(value = "Lista todas las entradas no usadas")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Listado de entradas no usadas", response = Ticket.class, responseContainer = "List")
    })
    public Response listUnusedTickets() {
        try {
            List<Ticket> tickets = eventManager.listUnusedTickets();
            GenericEntity<List<Ticket>> entity = new GenericEntity<List<Ticket>>(tickets) {
            };
            return Response.ok(entity).build();
        } catch (SQLException ex) {
            throw dbError(ex, "Error al consultar la base de datos");
        }
    }

    private ApiException dbError(SQLException ex, String message) {
        String detail = ex.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            detail = ex.getClass().getSimpleName();
        }
        return new ApiException(500, "DB_ERROR", message + ": " + detail);
    }
}




