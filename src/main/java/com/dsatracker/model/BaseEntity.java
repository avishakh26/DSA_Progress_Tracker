package com.dsatracker.model;

import java.util.Objects;

/**
 * Common identity for every persisted domain object. {@code id} is
 * {@code null} until a repository assigns the autoincrement primary key
 * returned by SQLite, so equality only holds between two persisted
 * instances of the same concrete type with the same id - two brand-new,
 * not-yet-saved entities are never equal to each other. {@code hashCode}
 * is deliberately based on the class alone (not the mutable id), so an
 * entity's hash stays stable if it's placed in a {@code HashSet} before
 * being saved and its id changes afterwards.
 */
public abstract class BaseEntity {

    private Integer id;

    protected BaseEntity() {
    }

    protected BaseEntity(final Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final BaseEntity that = (BaseEntity) other;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
