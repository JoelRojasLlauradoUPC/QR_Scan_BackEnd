package edu.upc.dsa.event.repository;

import edu.upc.dsa.event.config.DatabaseConfig;
import edu.upc.dsa.event.model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTicketRepository implements TicketRepository {

    private static final String BASE_SELECT = "SELECT nombre, apellido, correo_electronico, tipo, pmr, hash, numero_local, consumed FROM event_tickets";

    @Override
    public Optional<Ticket> findByHash(String hash) throws SQLException {
        String sql = BASE_SELECT + " WHERE hash = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, hash);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapTicket(rs));
            }
        }
    }

    @Override
    public List<Ticket> findAll() throws SQLException {
        return queryList(BASE_SELECT + " ORDER BY hash ASC");
    }

    @Override
    public List<Ticket> findByEmail(String email) throws SQLException {
        return queryList(BASE_SELECT + " WHERE correo_electronico = ? ORDER BY hash ASC", email);
    }

    @Override
    public List<Ticket> findByConsumed(boolean consumed) throws SQLException {
        return queryList(BASE_SELECT + " WHERE consumed = ? ORDER BY hash ASC", consumed);
    }

    @Override
    public boolean markConsumed(String hash) throws SQLException {
        String updateSql = "UPDATE event_tickets SET consumed = 1, consumed_at = CURRENT_TIMESTAMP " +
                "WHERE hash = ? AND consumed = 0";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setString(1, hash);
            return statement.executeUpdate() == 1;
        }
    }

    private List<Ticket> queryList(String sql, Object... params) throws SQLException {
        List<Ticket> tickets = new ArrayList<Ticket>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        }
        return tickets;
    }

    private void bindParams(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int index = i + 1;
            if (param instanceof Boolean) {
                statement.setBoolean(index, (Boolean) param);
            } else {
                statement.setObject(index, param);
            }
        }
    }

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("correo_electronico"),
                rs.getInt("tipo"),
                rs.getBoolean("pmr"),
                rs.getString("hash"),
                rs.getInt("numero_local"),
                rs.getBoolean("consumed")
        );
    }
}



