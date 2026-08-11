package com.dsatracker.exception;

/**
 * Thrown when a repository fails to execute a query or statement against
 * the database (constraint violation, I/O error, malformed result set).
 * Unchecked so DAO method signatures stay clean; controllers catch this at
 * the boundary and show a JavaFX alert instead of a raw stack trace.
 */
public class RepositoryException extends RuntimeException {

    public RepositoryException(final String message) {
        super(message);
    }

    public RepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
