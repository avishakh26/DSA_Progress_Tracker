package com.dsatracker.model;

import java.time.LocalDate;

/** One day's worth of solved-problem count, the raw data behind the streak system and heatmap. */
public class Activity extends BaseEntity {

    private LocalDate activityDate;
    private int problemsSolved;

    public Activity() {
    }

    public Activity(final LocalDate activityDate, final int problemsSolved) {
        this.activityDate = activityDate;
        this.problemsSolved = problemsSolved;
    }

    /** Increments today's solved count by one - called when a problem is marked solved. */
    public void incrementSolved() {
        this.problemsSolved++;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(final LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public int getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(final int problemsSolved) {
        this.problemsSolved = problemsSolved;
    }
}
