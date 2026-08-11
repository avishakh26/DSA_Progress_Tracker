package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.Activity;
import com.dsatracker.repository.ActivityRepository;
import com.dsatracker.repository.SqliteActivityRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActivityServiceImplTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final ActivityRepository activityRepository = new SqliteActivityRepository();
    private final ActivityService service = new ActivityServiceImpl(activityRepository);

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
        // Wipe the two seeded activity rows so streak math starts from a known-empty slate.
        activityRepository.findAll().forEach(a -> activityRepository.deleteById(a.getId()));
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void recordProblemSolvedCreatesThenIncrementsTodaysRow() {
        service.recordProblemSolved();
        service.recordProblemSolved();

        assertEquals(2, activityRepository.findByDate(LocalDate.now()).orElseThrow().getProblemsSolved());
    }

    @Test
    void currentStreakCountsConsecutiveDaysEndingYesterdayWhenTodayIsUntouched() {
        final LocalDate today = LocalDate.now();
        activityRepository.save(new Activity(today.minusDays(1), 1));
        activityRepository.save(new Activity(today.minusDays(2), 1));
        activityRepository.save(new Activity(today.minusDays(3), 1));
        // today has no row at all - must not reset the streak to 0.

        assertEquals(3, service.getCurrentStreak());
    }

    @Test
    void currentStreakBreaksOnAGapDay() {
        final LocalDate today = LocalDate.now();
        activityRepository.save(new Activity(today, 1));
        activityRepository.save(new Activity(today.minusDays(1), 1));
        // gap: today - 2 has no row / zero solved
        activityRepository.save(new Activity(today.minusDays(3), 1));

        assertEquals(2, service.getCurrentStreak());
    }

    @Test
    void currentStreakIsZeroAfterAMissedDay() {
        activityRepository.save(new Activity(LocalDate.now().minusDays(2), 1));
        // yesterday and today both untouched -> streak is broken

        assertEquals(0, service.getCurrentStreak());
    }

    @Test
    void longestStreakFindsTheBestRunEvenIfItIsNotTheCurrentOne() {
        final LocalDate today = LocalDate.now();
        // A 4-day run far in the past...
        activityRepository.save(new Activity(today.minusDays(10), 1));
        activityRepository.save(new Activity(today.minusDays(9), 1));
        activityRepository.save(new Activity(today.minusDays(8), 1));
        activityRepository.save(new Activity(today.minusDays(7), 1));
        // ...then a gap, then a shorter 2-day run ending today.
        activityRepository.save(new Activity(today.minusDays(1), 1));
        activityRepository.save(new Activity(today, 1));

        assertEquals(4, service.getLongestStreak());
        assertEquals(2, service.getCurrentStreak());
    }

    @Test
    void heatmapDataZeroFillsDaysWithNoActivityRow() {
        final LocalDate today = LocalDate.now();
        activityRepository.save(new Activity(today, 5));

        final Map<LocalDate, Integer> heatmap = service.getHeatmapData(today.minusDays(2), today);

        assertEquals(3, heatmap.size());
        assertEquals(0, heatmap.get(today.minusDays(2)));
        assertEquals(0, heatmap.get(today.minusDays(1)));
        assertEquals(5, heatmap.get(today));
    }
}
