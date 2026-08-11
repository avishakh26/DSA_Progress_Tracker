package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.Activity;
import com.dsatracker.model.Goal;
import com.dsatracker.model.enums.GoalType;
import com.dsatracker.repository.ActivityRepository;
import com.dsatracker.repository.GoalRepository;
import com.dsatracker.repository.SqliteActivityRepository;
import com.dsatracker.repository.SqliteGoalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalServiceImplTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final GoalRepository goalRepository = new SqliteGoalRepository();
    private final ActivityRepository activityRepository = new SqliteActivityRepository();
    private final GoalService service = new GoalServiceImpl(goalRepository, activityRepository);

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void setGoalUpdatesTheExistingActiveGoalInPlaceInsteadOfDuplicating() {
        // Seed data already creates a DAILY goal (target 3) covering today.
        final List<Goal> before = service.getGoalsByType(GoalType.DAILY);

        service.setGoal(GoalType.DAILY, 5);

        final List<Goal> after = service.getGoalsByType(GoalType.DAILY);
        assertEquals(before.size(), after.size(), "Updating today's goal must not insert a second row");
        assertEquals(5, after.get(0).getTarget());
    }

    @Test
    void setGoalStartsANewGoalWhenNoneCoversToday() {
        service.setGoal(GoalType.MONTHLY, 40);

        final GoalProgress progress = service.getTodayProgress(GoalType.MONTHLY).orElseThrow();
        assertEquals(40, progress.goal().getTarget());
    }

    @Test
    void dailyProgressReflectsOnlyTodaysSolvedCount() {
        // Seed data already inserts a "today" row (0 solved) - update it in place
        // rather than inserting a second row, which would violate the UNIQUE(activity_date) constraint.
        final Activity today = activityRepository.findByDate(LocalDate.now()).orElseThrow();
        today.setProblemsSolved(2);
        activityRepository.save(today);

        final GoalProgress progress = service.getTodayProgress(GoalType.DAILY).orElseThrow();

        assertEquals(2, progress.actualCount());
        assertEquals(3, progress.goal().getTarget()); // from seed data
        assertEquals(1, progress.remaining());
        assertEquals(67, progress.percentComplete());
    }

    @Test
    void weeklyProgressSumsTheLastSevenDaysIncludingToday() {
        final LocalDate today = LocalDate.now();
        // Replace both seeded rows (today=0, yesterday=2) with a known-clean slate.
        activityRepository.deleteById(activityRepository.findByDate(today).orElseThrow().getId());
        activityRepository.deleteById(activityRepository.findByDate(today.minusDays(1)).orElseThrow().getId());
        activityRepository.save(new Activity(today, 2));
        activityRepository.save(new Activity(today.minusDays(3), 3));
        activityRepository.save(new Activity(today.minusDays(10), 100)); // outside the 7-day window

        service.setGoal(GoalType.WEEKLY, 10);
        final GoalProgress progress = service.getTodayProgress(GoalType.WEEKLY).orElseThrow();

        assertEquals(5, progress.actualCount());
    }

    @Test
    void percentCompleteIsCappedAtOneHundred() {
        service.setGoal(GoalType.DAILY, 1);
        final Goal goal = service.getGoalsByType(GoalType.DAILY).get(0);
        final GoalProgress progress = new GoalProgress(goal, 99);

        assertTrue(progress.percentComplete() <= 100);
        assertEquals(0, progress.remaining());
    }
}
