package com.dsatracker.exception;

/**
 * Thrown by a service when caller-supplied data fails a business rule
 * (blank title, phase out of range, malformed URL...) before it would ever
 * reach the database. Lets controllers show a precise, friendly message
 * instead of a SQLite CHECK-constraint failure.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(final String message) {
        super(message);
    }
}
