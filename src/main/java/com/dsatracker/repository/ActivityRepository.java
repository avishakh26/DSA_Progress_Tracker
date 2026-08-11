package com.dsatracker.repository;

import com.dsatracker.model.Activity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends Repository<Activity, Integer> {
    Optional<Activity> findByDate(LocalDate date);

    /** Inclusive date range, ascending - used by the heatmap and streak calculations. */
    List<Activity> findBetween(LocalDate start, LocalDate end);

    List<Activity> findAllOrderedByDate();
}
