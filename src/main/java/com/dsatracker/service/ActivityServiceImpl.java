package com.dsatracker.service;

import com.dsatracker.model.Activity;
import com.dsatracker.repository.ActivityRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityServiceImpl(final ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void recordProblemSolved() {
        final LocalDate today = LocalDate.now();
        final Optional<Activity> existing = activityRepository.findByDate(today);
        if (existing.isPresent()) {
            final Activity activity = existing.get();
            activity.incrementSolved();
            activityRepository.save(activity);
        } else {
            activityRepository.save(new Activity(today, 1));
        }
    }

    @Override
    public int getCurrentStreak() {
        final Map<LocalDate, Integer> solved = solvedByDate();

        // Today not yet solved doesn't break an ongoing streak - only count it if it's done.
        LocalDate cursor = LocalDate.now();
        if (solved.getOrDefault(cursor, 0) == 0) {
            cursor = cursor.minusDays(1);
        }

        int streak = 0;
        while (solved.getOrDefault(cursor, 0) > 0) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    @Override
    public int getLongestStreak() {
        final Map<LocalDate, Integer> solved = solvedByDate();
        if (solved.isEmpty()) {
            return 0;
        }

        final LocalDate start = solved.keySet().stream().min(LocalDate::compareTo).orElseThrow();
        final LocalDate end = LocalDate.now();

        int longest = 0;
        int running = 0;
        // Walk every calendar day (not just rows that exist) so a day with no row at all
        // - not merely a zero-count row - correctly breaks the streak.
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            if (solved.getOrDefault(day, 0) > 0) {
                running++;
                longest = Math.max(longest, running);
            } else {
                running = 0;
            }
        }
        return longest;
    }

    @Override
    public Map<LocalDate, Integer> getHeatmapData(final LocalDate start, final LocalDate end) {
        final Map<LocalDate, Integer> heatmap = new LinkedHashMap<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            heatmap.put(day, 0);
        }
        for (final Activity activity : activityRepository.findBetween(start, end)) {
            heatmap.put(activity.getActivityDate(), activity.getProblemsSolved());
        }
        return heatmap;
    }

    private Map<LocalDate, Integer> solvedByDate() {
        final Map<LocalDate, Integer> map = new LinkedHashMap<>();
        for (final Activity activity : activityRepository.findAllOrderedByDate()) {
            map.put(activity.getActivityDate(), activity.getProblemsSolved());
        }
        return map;
    }
}
