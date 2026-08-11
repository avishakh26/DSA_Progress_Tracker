package com.dsatracker.model;

import java.time.LocalDateTime;

/** A free-form study note, optionally scoped to a topic ({@code topicId == null} means a general note). */
public class Note extends BaseEntity {

    private String title;
    private Integer topicId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Note() {
        final LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Note(final String title, final Integer topicId, final String content) {
        this();
        this.title = title;
        this.topicId = topicId;
        this.content = content;
    }

    /** Replaces the content and refreshes the modification timestamp. */
    public void updateContent(final String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(final Integer topicId) {
        this.topicId = topicId;
    }

    public String getContent() {
        return content;
    }

    /**
     * Plain setter for repository row-mapping (loading an existing note
     * must not disturb {@code updatedAt}). User-driven edits should go
     * through {@link #updateContent(String)} instead.
     */
    public void setContent(final String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return title;
    }
}
