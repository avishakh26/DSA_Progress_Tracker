package com.dsatracker.service;

import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.GoalType;
import com.dsatracker.model.enums.ProblemStatus;
import com.dsatracker.model.enums.TopicStatus;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.TopicRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates dashboard numbers straight from the repositories rather than
 * through {@link TopicService}/{@link ProblemService}: this class only
 * reads and combines data, it never mutates anything, so going through the
 * mutation-oriented services would add a layer without a reason - and
 * would risk a dependency cycle since those services already depend on
 * each other.
 */
public final class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_ACTIVITY_LIMIT = 5;

    private final TopicRepository topicRepository;
    private final ProblemRepository problemRepository;
    private final ActivityService activityService;
    private final GoalService goalService;

    public DashboardServiceImpl(final TopicRepository topicRepository, final ProblemRepository problemRepository,
                                 final ActivityService activityService, final GoalService goalService) {
        this.topicRepository = topicRepository;
        this.problemRepository = problemRepository;
        this.activityService = activityService;
        this.goalService = goalService;
    }

    @Override
    public DashboardStats getStats() {
        final List<Topic> topics = topicRepository.findAll();
        final List<Problem> problems = problemRepository.findAll();

        final int completedTopics = (int) topics.stream().filter(t -> t.getStatus() == TopicStatus.COMPLETED).count();
        final int solvedProblems = (int) problems.stream().filter(p -> p.getStatus() == ProblemStatus.SOLVED).count();
        final int attemptedProblems = (int) problems.stream().filter(p -> p.getStatus() == ProblemStatus.ATTEMPTED).count();
        final double overallProgress = problems.isEmpty() ? 0.0 : solvedProblems * 100.0 / problems.size();

        final List<String> recentActivity = problems.stream()
                .filter(p -> p.getStatus() == ProblemStatus.SOLVED && p.getDateSolved() != null)
                .sorted(Comparator.comparing(Problem::getDateSolved).reversed())
                .limit(RECENT_ACTIVITY_LIMIT)
                .map(p -> "Solved " + p.getTitle())
                .collect(Collectors.toList());

        return new DashboardStats(
                topics.size(),
                completedTopics,
                problems.size(),
                solvedProblems,
                attemptedProblems,
                activityService.getCurrentStreak(),
                activityService.getLongestStreak(),
                overallProgress,
                countSolvedByDifficulty(problems, Difficulty.EASY),
                countSolvedByDifficulty(problems, Difficulty.MEDIUM),
                countSolvedByDifficulty(problems, Difficulty.HARD),
                recentActivity,
                goalService.getTodayProgress(GoalType.DAILY).orElse(null));
    }

    private int countSolvedByDifficulty(final List<Problem> problems, final Difficulty difficulty) {
        return (int) problems.stream()
                .filter(p -> p.getStatus() == ProblemStatus.SOLVED && p.getDifficulty() == difficulty)
                .count();
    }
}
