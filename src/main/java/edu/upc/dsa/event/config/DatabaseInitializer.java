package edu.upc.dsa.event.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        executeSqlScript("db/schema.sql");
        ensureTicketTableCompatibility();
        executeSqlScript("db/seed.sql");
    }

    private static void ensureTicketTableCompatibility() throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection()) {
            ensureColumn(connection, "event_tickets", "consumed", "BOOLEAN NOT NULL DEFAULT FALSE");
            ensureColumn(connection, "event_tickets", "consumed_at", "DATETIME NULL");
            ensureColumn(connection, "event_tickets", "created_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        }
    }

    private static void ensureColumn(Connection connection, String table, String column, String definition) throws SQLException {
        if (columnExists(connection, table, column)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, table, column)) {
            return columns.next();
        }
    }

    private static void executeSqlScript(String classpathResource) throws SQLException {
        String script = readClasspathResource(classpathResource);
        String[] statements = script.split(";");

        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            for (String rawStatement : statements) {
                String sql = rawStatement.trim();
                if (sql.isEmpty()) {
                    continue;
                }
                if (sql.startsWith("--") || sql.startsWith("#")) {
                    continue;
                }
                statement.execute(sql);
            }
        }
    }

    private static String readClasspathResource(String path) {
        InputStream stream = DatabaseInitializer.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("No se encontro el recurso SQL: " + path);
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el recurso SQL: " + path, ex);
        }

        return content.toString();
    }
}


