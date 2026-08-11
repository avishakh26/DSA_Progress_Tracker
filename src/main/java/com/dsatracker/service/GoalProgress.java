package com.dsatracker.service;

import com.dsatracker.model.Goal;

/** A goal paired with how many problems have actually been solved in its current window. */
public record GoalProgress(Goal goal, int actualCount) {

    public int percentComplete() {
        if (goal.getTarget() <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round(actualCount * 100.0 / goal.getTarget()));
    }

    public int remaining() {
        return Math.max(0, goal.getTarget() - actualCount);
    }
}
