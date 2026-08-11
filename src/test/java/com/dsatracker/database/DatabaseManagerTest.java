package com.dsatracker.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void initializeCreatesAllRequiredTables() throws SQLException {
        manager.initializeForTesting("jdbc:sqlite::memory:");
        final Connection connection = manager.getConnection();

        final Set<String> expected = Set.of("topics", "problems", "notes", "goals", "activity");
        final Set<String> actual = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'")) {
            while (rs.next()) {
                actual.add(rs.getString("name"));
            }
        }

        assertTrue(actual.containsAll(expected), "Expected tables " + expected + " but found " + actual);
    }

    @Test
    void foreignKeyEnforcementIsEnabled() throws SQLException {
        manager.initializeForTesting("jdbc:sqlite::memory:");
        try (Statement statement = manager.getConnection().createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA foreign_keys")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void insertingProblemWithUnknownTopicIsRejected() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
        final Connection connection = manager.getConnection();

        assertThrows(SQLException.class, () -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO problems (title, platform, topic_id, difficulty) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, "Orphan Problem");
                ps.setString(2, "LeetCode");
                ps.setInt(3, 9999); // no topic with this id exists
                ps.setString(4, "EASY");
                ps.executeUpdate();
            }
        });
    }

    @Test
    void seedDataIsLoadedOnFirstInitializeOnly() throws SQLException {
        manager.initializeForTesting("jdbc:sqlite::memory:");
        final Connection connection = manager.getConnection();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM topics")) {
            assertTrue(rs.next());
            assertEquals(17, rs.getInt(1), "Seed script should populate all 17 roadmap topics");
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM problems")) {
            assertTrue(rs.next());
            assertFalse(rs.getInt(1) == 0, "Seed script should populate sample problems");
        }
    }

    @Test
    void getConnectionBeforeInitializeThrows() {
        assertThrows(com.dsatracker.exception.DatabaseInitializationException.class, manager::getConnection);
    }
}
