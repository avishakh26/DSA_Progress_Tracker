package com.dsatracker.service;

import java.util.List;

/** One-call snapshot of everything the Dashboard view needs to render. */
public record DashboardStats(
        int totalTopics,
        int completedTopics,
        int totalProblems,
        int solvedProblems,
        int attemptedProblems,
        int currentStreak,
        int longestStreak,
        double overallProgressPercent,
        int easySolved,
        int mediumSolved,
        int hardSolved,
        List<String> recentActivity,
        GoalProgress todayGoal
) {
}
