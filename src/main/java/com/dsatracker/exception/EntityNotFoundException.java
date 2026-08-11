package com.dsatracker.exception;

/**
 * Thrown when an operation targets an entity id that does not exist -
 * e.g. updating a {@code Problem} that was deleted from under the caller.
 * A missing row on delete is treated as a harmless no-op instead (delete
 * is idempotent), so only save/update paths raise this.
 */
public class EntityNotFoundException extends RepositoryException {

    public EntityNotFoundException(final String message) {
        super(message);
    }
}
