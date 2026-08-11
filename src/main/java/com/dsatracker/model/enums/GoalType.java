package com.dsatracker.model.enums;

/** Recurrence period of a solving goal; constant names mirror the DB CHECK constraint on goals.goal_type. */
public enum GoalType {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    private final String displayName;

    GoalType(final String displayName) {
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
