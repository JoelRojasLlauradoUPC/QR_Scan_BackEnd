package edu.upc.dsa.event.service;

import edu.upc.dsa.event.exception.ApiException;
import edu.upc.dsa.event.model.Ticket;
import edu.upc.dsa.event.repository.TicketRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EventManager {

    private final TicketRepository ticketRepository;

    public EventManager(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket verify(String hash) throws SQLException {
        validateHash(hash);
        Optional<Ticket> ticket = ticketRepository.findByHash(hash);
        if (!ticket.isPresent()) {
            throw new ApiException(404, "TICKET_NOT_FOUND", "No existe ninguna entrada para este QR");
        }
        return ticket.get();
    }

    public Ticket enter(String hash) throws SQLException {
        Ticket ticket = verify(hash);
        if (ticket.isConsumed()) {
            throw new ApiException(409, "ALREADY_USED", "La entrada ya ha sido validada anteriormente");
        }

        boolean updated = ticketRepository.markConsumed(hash);
        if (!updated) {
            throw new ApiException(409, "ALREADY_USED", "La entrada ya ha sido validada anteriormente");
        }

        ticket.setConsumed(true);
        return ticket;
    }

    public List<Ticket> listAllTickets() throws SQLException {
        return ticketRepository.findAll();
    }

    public List<Ticket> listTicketsByEmail(String email) throws SQLException {
        validateEmail(email);
        return ticketRepository.findByEmail(email.trim());
    }

    public List<Ticket> listUsedTickets() throws SQLException {
        return ticketRepository.findByConsumed(true);
    }

    public List<Ticket> listUnusedTickets() throws SQLException {
        return ticketRepository.findByConsumed(false);
    }

    private void validateHash(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            throw new ApiException(400, "INVALID_HASH", "El hash del QR es obligatorio");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ApiException(400, "INVALID_EMAIL", "El correo electronico es obligatorio");
        }
    }
}


