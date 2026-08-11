package com.dsatracker.repository;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.exception.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Template-method base for every {@code Sqlite*Repository}: owns connection
 * access and the query/insert/update execution skeleton so concrete
 * repositories only ever supply SQL text, parameter binding and
 * {@link #mapRow}. No subclass opens a connection or runs JDBC calls by
 * hand - this is the only place that does.
 *
 * @param <T> the entity type this repository maps rows to
 */
abstract class AbstractSqliteRepository<T> {

    protected final Connection connection() {
        return DatabaseManager.getInstance().getConnection();
    }

    protected final List<T> query(final String sql, final SqlBinder binder) {
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }
                return results;
            }
        } catch (final SQLException e) {
            throw new RepositoryException("Query failed: " + sql, e);
        }
    }

    protected final Optional<T> queryOne(final String sql, final SqlBinder binder) {
        final List<T> results = query(sql, binder);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /** @return the number of rows affected */
    protected final int executeUpdate(final String sql, final SqlBinder binder) {
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate();
        } catch (final SQLException e) {
            throw new RepositoryException("Update failed: " + sql, e);
        }
    }

    protected final boolean delete(final String sql, final SqlBinder binder) {
        return executeUpdate(sql, binder) > 0;
    }

    /** Runs an INSERT and returns the autoincrement id SQLite generated for it. */
    protected final int executeInsert(final String sql, final SqlBinder binder) {
        try (PreparedStatement statement = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(statement);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new RepositoryException("Insert did not return a generated id: " + sql);
        } catch (final SQLException e) {
            throw new RepositoryException("Insert failed: " + sql, e);
        }
    }

    protected abstract T mapRow(ResultSet resultSet) throws SQLException;
}
