package com.sameboat.backend.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Flyway migrations (V1..Vn) apply cleanly to an empty DB and yield expected schema.
 * Skips gracefully if Docker is unavailable (to not block contributors without Docker Desktop).
 */
class MigrationIntegrationTest {

    @Test
    @DisplayName("Flyway migrations apply and expected tables/columns/indexes exist")
    void migrationsProduceExpectedSchema() throws Exception {
        if (Boolean.getBoolean("skip.migration.test") || System.getenv("SKIP_MIGRATION_TEST") != null) {
            Assumptions.abort("Migration test skipped via flag.");
        }

        // try-with-resources so container always stops & warning is resolved
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")) {
            try {
                postgres.start();
            } catch (Throwable t) {
                Assumptions.abort("Docker not available or failed to start container: " + t.getMessage());
            }

            Flyway flyway = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations("classpath:db/migration")
                    .load();

            flyway.migrate();

            try (Connection conn = flyway.getConfiguration().getDataSource().getConnection()) {
                assertTablesPresent(conn, "users", "stories", "trust_events", "sessions");
                assertUserTableColumns(conn);
                assertSessionsTableColumns(conn);
                assertUsersEmailLowerIndex(conn);
                assertFlywayHistoryVersions(conn, "1", "2", "3");
            }
        }
    }

    private void assertTablesPresent(Connection conn, String... tables) throws SQLException {
        Set<String> expected = Arrays.stream(tables).map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> actual = new HashSet<>();
        try (ResultSet rs = conn.getMetaData().getTables(null, null, null, new String[]{"TABLE"})) {
            while (rs.next()) {
                actual.add(rs.getString("TABLE_NAME").toLowerCase());
            }
        }
        for (String t : expected) {
            assertTrue(actual.contains(t), "Expected table missing: " + t + "; actual=" + actual);
        }
    }

    private void assertUserTableColumns(Connection conn) throws SQLException {
        Set<String> cols = getColumns(conn, "users");
        String[] expected = {"id", "email", "display_name", "role", "created_at", "password_hash", "avatar_url", "bio", "updated_at"};
        for (String c : expected) {
            assertTrue(cols.contains(c), "Missing users column: " + c + "; present=" + cols);
        }
    }

    private void assertSessionsTableColumns(Connection conn) throws SQLException {
        Set<String> cols = getColumns(conn, "sessions");
        String[] expected = {"id", "user_id", "created_at", "last_seen_at", "expires_at"};
        for (String c : expected) {
            assertTrue(cols.contains(c), "Missing sessions column: " + c + "; present=" + cols);
        }
    }

    private Set<String> getColumns(Connection conn, String table) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        Set<String> cols = new HashSet<>();
        try (ResultSet rs = meta.getColumns(null, null, table, null)) {
            while (rs.next()) {
                cols.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }
        return cols;
    }

    private void assertUsersEmailLowerIndex(Connection conn) throws SQLException {
        String sql = "SELECT indexname FROM pg_indexes WHERE tablename='users'";
        List<String> indexes = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                indexes.add(rs.getString(1));
            }
        }
        assertTrue(indexes.stream().anyMatch(i -> i.equalsIgnoreCase("users_email_lower_uidx")),
                "Expected users_email_lower_uidx index missing; found=" + indexes);
    }

    private void assertFlywayHistoryVersions(Connection conn, String... versions) throws SQLException {
        Set<String> expected = new HashSet<>(Arrays.asList(versions));
        Set<String> actual = new HashSet<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT version FROM flyway_schema_history WHERE success")) {
            while (rs.next()) {
                actual.add(rs.getString(1));
            }
        }
        for (String v : expected) {
            assertTrue(actual.contains(v), "Flyway history missing version " + v + "; present=" + actual);
        }
    }
}
