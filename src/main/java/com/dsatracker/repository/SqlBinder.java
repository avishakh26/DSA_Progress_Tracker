package com.dsatracker.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Binds parameters onto a {@link PreparedStatement}; lets query methods pass binding logic as a lambda. */
@FunctionalInterface
interface SqlBinder {
    void bind(PreparedStatement statement) throws SQLException;
}
