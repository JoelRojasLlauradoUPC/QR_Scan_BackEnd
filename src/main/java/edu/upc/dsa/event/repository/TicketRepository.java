package edu.upc.dsa.event.repository;

import edu.upc.dsa.event.model.Ticket;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TicketRepository {

    Optional<Ticket> findByHash(String hash) throws SQLException;

    List<Ticket> findAll() throws SQLException;

    List<Ticket> findByEmail(String email) throws SQLException;

    List<Ticket> findByConsumed(boolean consumed) throws SQLException;

    boolean markConsumed(String hash) throws SQLException;
}


