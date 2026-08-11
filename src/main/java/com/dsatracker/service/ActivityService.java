package com.dsatracker.service;

import java.time.LocalDate;
import java.util.Map;

public interface ActivityService {

    /** Increments (or creates) today's solved-problem count. */
    void recordProblemSolved();

    int getCurrentStreak();

    int getLongestStreak();

    /** Every date in the inclusive range mapped to its solved count (0 for days with no activity). */
    Map<LocalDate, Integer> getHeatmapData(LocalDate start, LocalDate end);
}
