package com.dsatracker.exception;

/**
 * Thrown when the local SQLite database cannot be opened or initialized
 * (missing driver, locked/corrupt file, unreadable schema resource, or the
 * database was accessed before {@code DatabaseManager.initialize()} ran).
 *
 * <p>Unchecked on purpose: a failure here means the application has no
 * usable persistence layer, so every caller up the stack would otherwise be
 * forced to declare or swallow {@code SQLException}. Callers that can show
 * UI (e.g. {@code DsaTrackerApp}) catch this once, at the top, and present a
 * JavaFX alert instead of leaking a stack trace to the user.</p>
 */
public class DatabaseInitializationException extends RuntimeException {

    public DatabaseInitializationException(final String message) {
        super(message);
    }

    public DatabaseInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
