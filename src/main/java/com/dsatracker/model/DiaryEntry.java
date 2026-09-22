package com.dsatracker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** A personal journal entry, dated to the day it was first written. */
public class DiaryEntry extends BaseEntity {

    private String title;
    private String content;
    private LocalDate entryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DiaryEntry() {
        final LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.entryDate = now.toLocalDate();
    }

    public DiaryEntry(final String title, final String content) {
        this();
        this.title = title;
        this.content = content;
    }

    /** Replaces title/content and refreshes the modification timestamp; entryDate never changes after creation. */
    public void updateEntry(final String title, final String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(final LocalDate entryDate) {
        this.entryDate = entryDate;
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
