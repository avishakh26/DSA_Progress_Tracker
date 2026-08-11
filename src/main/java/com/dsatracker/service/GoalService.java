package com.dsatracker.service;

import com.dsatracker.model.Goal;
import com.dsatracker.model.enums.GoalType;

import java.util.List;
import java.util.Optional;

public interface GoalService {

    List<Goal> getGoalsByType(GoalType type);

    /** Updates the goal of this type that currently covers today, or starts a new one from today if none does. */
    Goal setGoal(GoalType type, int target);

    /** Empty if no goal of this type currently covers today. */
    Optional<GoalProgress> getTodayProgress(GoalType type);

    void deleteGoal(int id);
}
