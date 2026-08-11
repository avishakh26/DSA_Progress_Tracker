package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.repository.ActivityRepository;
import com.dsatracker.repository.GoalRepository;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.SqliteActivityRepository;
import com.dsatracker.repository.SqliteGoalRepository;
import com.dsatracker.repository.SqliteProblemRepository;
import com.dsatracker.repository.SqliteTopicRepository;
import com.dsatracker.repository.TopicRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises DashboardServiceImpl against the real seed data loaded by DatabaseManager. */
class DashboardServiceImplTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");

        final TopicRepository topicRepository = new SqliteTopicRepository();
        final ProblemRepository problemRepository = new SqliteProblemRepository();
        final ActivityRepository activityRepository = new SqliteActivityRepository();
        final GoalRepository goalRepository = new SqliteGoalRepository();

        final ActivityService activityService = new ActivityServiceImpl(activityRepository);
        final GoalService goalService = new GoalServiceImpl(goalRepository, activityRepository);
        dashboardService = new DashboardServiceImpl(topicRepository, problemRepository, activityService, goalService);
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void statsMatchTheKnownSeedData() {
        final DashboardStats stats = dashboardService.getStats();

        assertEquals(17, stats.totalTopics());
        assertEquals(5, stats.totalProblems());
        assertEquals(2, stats.solvedProblems());   // Two Sum, Valid Anagram
        assertEquals(1, stats.attemptedProblems()); // Best Time to Buy and Sell Stock
        assertEquals(40.0, stats.overallProgressPercent());
        assertEquals(2, stats.easySolved());
        assertEquals(0, stats.mediumSolved());
        assertEquals(0, stats.hardSolved());
        assertEquals(1, stats.currentStreak());    // yesterday solved, today untouched yet
        assertEquals(1, stats.longestStreak());
    }

    @Test
    void recentActivityListsRecentlySolvedProblemsByTitle() {
        final DashboardStats stats = dashboardService.getStats();

        assertEquals(2, stats.recentActivity().size());
        assertTrue(stats.recentActivity().contains("Solved Two Sum"));
        assertTrue(stats.recentActivity().contains("Solved Valid Anagram"));
    }

    @Test
    void todayGoalReflectsTheSeededDailyGoal() {
        final DashboardStats stats = dashboardService.getStats();

        assertNotNull(stats.todayGoal());
        assertEquals(3, stats.todayGoal().goal().getTarget());
        assertEquals(0, stats.todayGoal().actualCount());
        assertEquals(3, stats.todayGoal().remaining());
    }
}
