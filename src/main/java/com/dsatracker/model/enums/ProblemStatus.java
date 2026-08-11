package com.dsatracker.model.enums;

/** Solve state of a tracked problem; constant names mirror the DB CHECK constraint on problems.status. */
public enum ProblemStatus {
    NOT_STARTED("Not Started"),
    ATTEMPTED("Attempted"),
    SOLVED("Solved");

    private final String displayName;

    ProblemStatus(final String displayName) {
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
