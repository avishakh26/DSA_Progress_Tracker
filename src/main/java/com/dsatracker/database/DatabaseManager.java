package com.dsatracker.database;

import com.dsatracker.exception.DatabaseInitializationException;
import com.dsatracker.util.AppConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Singleton owner of the single JDBC {@link Connection} to the local SQLite
 * database. Responsible for creating the {@code data/} directory, opening
 * the connection, enabling foreign-key enforcement (off by default in
 * SQLite), and running the schema/seed scripts. This is the only class in
 * the application that opens a database connection or executes a raw SQL
 * script; every other read/write goes through a repository using
 * {@link java.sql.PreparedStatement}s against {@link #getConnection()}.
 */
public final class DatabaseManager {

    private static volatile DatabaseManager instance;

    private Connection connection;
    private boolean initialized;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Opens the on-disk database under {@code data/}, creating and seeding
     * it on first run. Safe to call more than once - subsequent calls are a
     * no-op while a connection is already open.
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }
        try {
            final Path dataDir = Path.of(AppConstants.DATA_DIRECTORY);
            Files.createDirectories(dataDir);
            final Path dbFile = dataDir.resolve(AppConstants.DATABASE_FILE);
            open(AppConstants.JDBC_URL_PREFIX + dbFile);
        } catch (final IOException e) {
            throw new DatabaseInitializationException("Could not create the data directory for the database.", e);
        }
    }

    /**
     * Opens a database at an arbitrary JDBC URL (e.g. {@code jdbc:sqlite::memory:}).
     * A test-only seam - exists so tests across every package (database,
     * repository, service...) can exercise real schema/seed logic against
     * an isolated in-memory database instead of the app's data file.
     */
    public synchronized void initializeForTesting(final String jdbcUrl) {
        if (initialized) {
            return;
        }
        open(jdbcUrl);
    }

    private void open(final String jdbcUrl) {
        try {
            connection = DriverManager.getConnection(jdbcUrl);
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
                pragma.execute("PRAGMA journal_mode = WAL");
            }
            runScript(AppConstants.SQL_SCHEMA);
            if (isTopicsTableEmpty()) {
                runScript(AppConstants.SQL_SEED);
            }
            initialized = true;
        } catch (final IOException | SQLException e) {
            throw new DatabaseInitializationException("Failed to initialize the local database.", e);
        }
    }

    /**
     * @return the shared connection for repositories to build
     *         {@link java.sql.PreparedStatement}s from
     */
    public Connection getConnection() {
        if (!initialized || connection == null) {
            throw new DatabaseInitializationException("Database was accessed before initialize() completed.");
        }
        return connection;
    }

    public synchronized void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (final SQLException e) {
            throw new DatabaseInitializationException("Failed to close the database connection cleanly.", e);
        } finally {
            connection = null;
            initialized = false;
        }
    }

    /** Deletes every row from every table, then re-runs the seed script - Settings' "Restore Sample Data". */
    public synchronized void restoreSampleData() {
        clearAllTables();
        try {
            runScript(AppConstants.SQL_SEED);
        } catch (final IOException | SQLException e) {
            throw new DatabaseInitializationException("Failed to restore sample data.", e);
        }
    }

    /** Deletes every row from every table, leaving an empty (but still valid) database - Settings' "Clear All Data". */
    public synchronized void clearAllData() {
        clearAllTables();
    }

    private void clearAllTables() {
        // Children before parents: not strictly required (FKs already cascade/null-out
        // correctly either way) but keeps the intent obvious.
        final String[] tables = {"activity", "notes", "goals", "problems", "topics"};
        try (Statement statement = connection.createStatement()) {
            for (final String table : tables) {
                statement.execute("DELETE FROM " + table);
            }
        } catch (final SQLException e) {
            throw new DatabaseInitializationException("Failed to clear existing data.", e);
        }
    }

    private boolean isTopicsTableEmpty() throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM topics")) {
            return resultSet.next() && resultSet.getInt(1) == 0;
        }
    }

    private void runScript(final String classpathResource) throws IOException, SQLException {
        final String sql = readResource(classpathResource);
        try (Statement statement = connection.createStatement()) {
            for (final String stmt : splitStatements(sql)) {
                final String trimmed = stmt.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        }
    }

    /**
     * Splits a script on {@code ;} statement terminators while ignoring any
     * semicolon that appears inside a single-quoted string literal (e.g.
     * free-text note content containing "step 1; step 2").
     */
    private List<String> splitStatements(final String sql) {
        final List<String> statements = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        for (final char c : sql.toCharArray()) {
            if (c == '\'') {
                inSingleQuote = !inSingleQuote;
            }
            if (c == ';' && !inSingleQuote) {
                statements.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (!current.toString().isBlank()) {
            statements.add(current.toString());
        }
        return statements;
    }

    private String readResource(final String classpathResource) throws IOException {
        final InputStream stream = Objects.requireNonNull(
                DatabaseManager.class.getResourceAsStream(classpathResource),
                "Missing SQL resource on classpath: " + classpathResource);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            final StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                final String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                builder.append(line).append('\n');
            }
            return builder.toString();
        }
    }
}
