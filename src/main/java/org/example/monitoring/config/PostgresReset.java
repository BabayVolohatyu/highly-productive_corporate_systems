package org.example.monitoring.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

public final class PostgresReset {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private PostgresReset() {
    }

    public static void dropAndRecreate() {
        String host = property("POSTGRES_HOST", "localhost");
        String port = property("POSTGRES_PORT", "5432");
        String database = property("POSTGRES_DB", "monitoring");
        String user = property("POSTGRES_USER", "monitoring");
        String password = property("POSTGRES_PASSWORD", "monitoring");
        if (!IDENTIFIER.matcher(database).matches()) {
            throw new IllegalStateException("POSTGRES_DB must be a simple identifier");
        }
        String url = "jdbc:postgresql://" + host + ":" + port + "/postgres";
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(true);
            terminateBackends(connection, database);
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP DATABASE IF EXISTS " + database);
                statement.execute("CREATE DATABASE " + database);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not reset PostgreSQL database '" + database + "'. Is the database container running?",
                    exception);
        }
        System.out.println("Dropped and recreated database " + database);
    }

    private static void terminateBackends(Connection connection, String database) throws SQLException {
        String sql = """
                SELECT pg_terminate_backend(pid)
                FROM pg_stat_activity
                WHERE datname = ? AND pid <> pg_backend_pid()
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, database);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    results.getBoolean(1);
                }
            }
        }
    }

    private static String property(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
