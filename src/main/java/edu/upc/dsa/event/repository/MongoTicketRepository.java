package edu.upc.dsa.event.repository;

import com.mongodb.MongoException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import edu.upc.dsa.event.model.Ticket;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class MongoTicketRepository implements TicketRepository {

    private static final String DEFAULT_DB = "qr_app";
    private static final String DEFAULT_COLLECTION = "event_tickets";

    private static MongoClient sharedClient;

    private final MongoCollection<Document> collection;

    public MongoTicketRepository() {
        this.collection = resolveCollection();
    }

    @Override
    public Optional<Ticket> findByHash(String hash) throws SQLException {
        try {
            Document doc = collection.find(Filters.eq("hash", hash)).first();
            if (doc == null) {
                return Optional.empty();
            }
            return Optional.of(mapTicket(doc));
        } catch (MongoException ex) {
            throw new SQLException("Error en MongoDB al buscar por hash", ex);
        }
    }

    @Override
    public List<Ticket> findAll() throws SQLException {
        try {
            FindIterable<Document> docs = collection.find().sort(Sorts.ascending("hash"));
            return mapList(docs);
        } catch (MongoException ex) {
            throw new SQLException("Error en MongoDB al listar entradas", ex);
        }
    }

    @Override
    public List<Ticket> findByEmail(String email) throws SQLException {
        try {
            FindIterable<Document> docs = collection.find(Filters.eq("correo_electronico", email)).sort(Sorts.ascending("hash"));
            return mapList(docs);
        } catch (MongoException ex) {
            throw new SQLException("Error en MongoDB al listar por correo", ex);
        }
    }

    @Override
    public List<Ticket> findByConsumed(boolean consumed) throws SQLException {
        try {
            FindIterable<Document> docs = collection.find(consumedFilter(consumed)).sort(Sorts.ascending("hash"));
            return mapList(docs);
        } catch (MongoException ex) {
            throw new SQLException("Error en MongoDB al listar por estado", ex);
        }
    }

    @Override
    public boolean markConsumed(String hash) throws SQLException {
        try {
            Document updated = collection.findOneAndUpdate(
                    Filters.and(Filters.eq("hash", hash), consumedFilter(false)),
                    Updates.combine(
                            Updates.set("consumed", true),
                            Updates.set("consumed_at", new Date())
                    ),
                    new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
            );
            return updated != null;
        } catch (MongoException ex) {
            throw new SQLException("Error en MongoDB al consumir la entrada", ex);
        }
    }

    private Bson consumedFilter(boolean consumed) {
        if (consumed) {
            return Filters.eq("consumed", true);
        }

        // Documentos importados sin `consumed` se consideran no usados.
        return Filters.or(
                Filters.eq("consumed", false),
                Filters.exists("consumed", false)
        );
    }

    private List<Ticket> mapList(FindIterable<Document> docs) {
        List<Ticket> tickets = new ArrayList<Ticket>();
        for (Document doc : docs) {
            tickets.add(mapTicket(doc));
        }
        return tickets;
    }

    private Ticket mapTicket(Document doc) {
        String nombre = readString(doc, "nombre");
        String apellido = readString(doc, "apellido");
        String correo = readString(doc, "correo_electronico");
        int tipo = readInt(doc, "tipo");
        boolean pmr = readBoolean(doc, "pmr");
        String hash = readString(doc, "hash");
        int numeroLocal = readInt(doc, "numero_local");
        boolean consumed = readBoolean(doc, "consumed");

        return new Ticket(nombre, apellido, correo, tipo, pmr, hash, numeroLocal, consumed);
    }

    private String readString(Document doc, String key) {
        Object value = doc.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private int readInt(Document doc, String key) {
        Object value = doc.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private boolean readBoolean(Document doc, String key) {
        Object value = doc.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private MongoCollection<Document> resolveCollection() {
        String uri = readRequired("MONGO_URI");
        String dbName = read("MONGO_DB", DEFAULT_DB);
        String collectionName = read("MONGO_COLLECTION", DEFAULT_COLLECTION);

        synchronized (MongoTicketRepository.class) {
            if (sharedClient == null) {
                sharedClient = new MongoClient(new MongoClientURI(uri));
            }
        }

        MongoDatabase db = sharedClient.getDatabase(dbName);
        return db.getCollection(collectionName);
    }

    private String read(String key, String fallback) {
        String fromProperty = System.getProperty(key);
        if (fromProperty != null && !fromProperty.trim().isEmpty()) {
            return fromProperty.trim();
        }
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        return fallback;
    }

    private String readRequired(String key) {
        String value = read(key, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Falta la variable obligatoria: " + key);
        }
        return value.trim();
    }
}






