package edu.upc.dsa.event.config;

public final class PersistenceProvider {

    private PersistenceProvider() {
    }

    public static boolean isMongo() {
        String provider = read("DB_PROVIDER", "sql");
        String normalized = provider.toLowerCase();
        return "mongo".equals(normalized) || "mongodb".equals(normalized);
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

