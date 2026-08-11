package com.dsatracker.model.enums;

/** Progress state of a roadmap topic; constant names mirror the DB CHECK constraint on topics.status. */
public enum TopicStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String displayName;

    TopicStatus(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
