package edu.upc.dsa.event.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConfig {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "qr_app";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        String host = read("DB_HOST", DEFAULT_HOST);
        String port = read("DB_PORT", DEFAULT_PORT);
        String db = DEFAULT_DB;
        String user = read("DB_USER", DEFAULT_USER);
        String pass = read("DB_PASS", DEFAULT_PASS);

        String jdbcUrl = "jdbc:mariadb://" + host + ":" + port + "/" + db +
                "?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8";
        Connection connection = DriverManager.getConnection(jdbcUrl, user, pass);
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET NAMES utf8mb4");
        }
        return connection;
    }

    private static String read(String key, String fallback) {
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
}





