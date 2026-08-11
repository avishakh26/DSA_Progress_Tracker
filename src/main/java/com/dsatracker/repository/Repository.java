package com.dsatracker.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract every entity repository implements. Keeps
 * controllers/services talking to a type-safe abstraction instead of raw
 * SQL or a specific JDBC implementation.
 *
 * @param <T>  the entity type
 * @param <ID> the entity's primary-key type
 */
public interface Repository<T, ID> {

    /** Inserts a brand-new entity ({@code id == null}) or updates an existing one otherwise. */
    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    /** @return {@code true} if a row was deleted, {@code false} if no row had this id (idempotent). */
    boolean deleteById(ID id);

    default boolean existsById(final ID id) {
        return findById(id).isPresent();
    }
}
