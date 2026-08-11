package com.dsatracker.model.enums;

/** Difficulty rating shared by topics and problems; constant names mirror the DB CHECK constraints. */
public enum Difficulty {
    EASY("Easy", "difficulty-easy"),
    MEDIUM("Medium", "difficulty-medium"),
    HARD("Hard", "difficulty-hard");

    private final String displayName;
    private final String styleClass;

    Difficulty(final String displayName, final String styleClass) {
        this.displayName = displayName;
        this.styleClass = styleClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** CSS style class (see dark-theme.css) for badge coloring in later UI steps. */
    public String getStyleClass() {
        return styleClass;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
