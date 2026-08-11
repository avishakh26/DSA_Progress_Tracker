package com.dsatracker.model;

import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.TopicStatus;

/** A single node in the phased DSA roadmap (e.g. "Arrays", phase 1). */
public class Topic extends BaseEntity {

    private String name;
    private String description;
    private int phase;
    private Difficulty difficulty;
    private TopicStatus status;

    public Topic() {
        this.status = TopicStatus.NOT_STARTED;
    }

    public Topic(final String name, final String description, final int phase, final Difficulty difficulty) {
        this();
        this.name = name;
        this.description = description;
        this.phase = phase;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(final int phase) {
        this.phase = phase;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(final Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public TopicStatus getStatus() {
        return status;
    }

    public void setStatus(final TopicStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name;
    }
}
