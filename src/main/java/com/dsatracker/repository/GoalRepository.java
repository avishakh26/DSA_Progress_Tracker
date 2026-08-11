package com.dsatracker.repository;

import com.dsatracker.model.Goal;
import com.dsatracker.model.enums.GoalType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoalRepository extends Repository<Goal, Integer> {
    List<Goal> findByType(GoalType type);

    /** The goal of this type whose date range covers {@code onDate}, if any. */
    Optional<Goal> findCurrent(GoalType type, LocalDate onDate);
}
