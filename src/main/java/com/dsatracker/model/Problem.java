package com.dsatracker.model;

import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
import com.dsatracker.model.enums.ProblemStatus;

import java.time.LocalDate;

/**
 * A single tracked practice problem. Holds only {@code topicId} (not a
 * {@link Topic} reference) so the model layer stays free of inter-entity
 * coupling; a repository join resolves the topic name for display.
 */
public class Problem extends BaseEntity {

    private String title;
    private Platform platform;
    private String url;
    private Integer topicId;
    private Difficulty difficulty;
    private ProblemStatus status;
    private String notes;
    private LocalDate dateAdded;
    private LocalDate dateSolved;

    public Problem() {
        this.status = ProblemStatus.NOT_STARTED;
        this.dateAdded = LocalDate.now();
    }

    public Problem(final String title, final Platform platform, final String url,
                    final Integer topicId, final Difficulty difficulty) {
        this();
        this.title = title;
        this.platform = platform;
        this.url = url;
        this.topicId = topicId;
        this.difficulty = difficulty;
    }

    /** Marks this problem solved as of today. */
    public void markSolved() {
        this.status = ProblemStatus.SOLVED;
        this.dateSolved = LocalDate.now();
    }

    /** Marks this problem attempted but not yet solved. */
    public void markAttempted() {
        if (this.status != ProblemStatus.SOLVED) {
            this.status = ProblemStatus.ATTEMPTED;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(final Platform platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(final Integer topicId) {
        this.topicId = topicId;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(final Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public ProblemStatus getStatus() {
        return status;
    }

    public void setStatus(final ProblemStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(final LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }

    public LocalDate getDateSolved() {
        return dateSolved;
    }

    public void setDateSolved(final LocalDate dateSolved) {
        this.dateSolved = dateSolved;
    }

    @Override
    public String toString() {
        return title;
    }
}
