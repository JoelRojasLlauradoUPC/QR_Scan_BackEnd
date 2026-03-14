package edu.upc.dsa.event.repository;

import edu.upc.dsa.event.model.Ticket;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTicketRepository implements TicketRepository {

    private final Map<String, Ticket> tickets = new ConcurrentHashMap<String, Ticket>();

    public void upsert(Ticket ticket) {
        tickets.put(ticket.getHash(), ticket);
    }

    @Override
    public Optional<Ticket> findByHash(String hash) throws SQLException {
        Ticket ticket = tickets.get(hash);
        if (ticket == null) {
            return Optional.empty();
        }
        return Optional.of(copy(ticket));
    }

    @Override
    public List<Ticket> findAll() throws SQLException {
        return sortAndCopy(tickets.values());
    }

    @Override
    public List<Ticket> findByEmail(String email) throws SQLException {
        List<Ticket> result = new ArrayList<Ticket>();
        for (Ticket ticket : tickets.values()) {
            if (ticket.getCorreoElectronico().equals(email)) {
                result.add(copy(ticket));
            }
        }
        Collections.sort(result, byHash());
        return result;
    }

    @Override
    public List<Ticket> findByConsumed(boolean consumed) throws SQLException {
        List<Ticket> result = new ArrayList<Ticket>();
        for (Ticket ticket : tickets.values()) {
            if (ticket.isConsumed() == consumed) {
                result.add(copy(ticket));
            }
        }
        Collections.sort(result, byHash());
        return result;
    }

    @Override
    public boolean markConsumed(String hash) throws SQLException {
        Ticket ticket = tickets.get(hash);
        if (ticket == null || ticket.isConsumed()) {
            return false;
        }
        ticket.setConsumed(true);
        return true;
    }

    private Ticket copy(Ticket source) {
        return new Ticket(
                source.getNombre(),
                source.getApellido(),
                source.getCorreoElectronico(),
                source.getTipo(),
                source.isPmr(),
                source.getHash(),
                source.getNumeroLocal(),
                source.isConsumed()
        );
    }

    private List<Ticket> sortAndCopy(Iterable<Ticket> source) {
        List<Ticket> result = new ArrayList<Ticket>();
        for (Ticket ticket : source) {
            result.add(copy(ticket));
        }
        Collections.sort(result, byHash());
        return result;
    }

    private Comparator<Ticket> byHash() {
        return new Comparator<Ticket>() {
            @Override
            public int compare(Ticket a, Ticket b) {
                return a.getHash().compareTo(b.getHash());
            }
        };
    }
}


