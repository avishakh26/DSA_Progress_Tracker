package com.dsatracker.service;

import com.dsatracker.model.Goal;
import com.dsatracker.model.enums.GoalType;
import com.dsatracker.repository.ActivityRepository;
import com.dsatracker.repository.GoalRepository;
import com.dsatracker.util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final ActivityRepository activityRepository;

    public GoalServiceImpl(final GoalRepository goalRepository, final ActivityRepository activityRepository) {
        this.goalRepository = goalRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    public List<Goal> getGoalsByType(final GoalType type) {
        return goalRepository.findByType(type);
    }

    @Override
    public Goal setGoal(final GoalType type, final int target) {
        Validator.requirePositive(target, "Goal target");
        final LocalDate today = LocalDate.now();
        final Optional<Goal> current = goalRepository.findCurrent(type, today);
        if (current.isPresent()) {
            final Goal goal = current.get();
            goal.setTarget(target);
            return goalRepository.save(goal);
        }
        return goalRepository.save(new Goal(type, target, today));
    }

    @Override
    public Optional<GoalProgress> getTodayProgress(final GoalType type) {
        final LocalDate today = LocalDate.now();
        return goalRepository.findCurrent(type, today)
                .map(goal -> new GoalProgress(goal, actualCountFor(type, today)));
    }

    private int actualCountFor(final GoalType type, final LocalDate today) {
        return switch (type) {
            case DAILY -> activityRepository.findByDate(today)
                    .map(activity -> activity.getProblemsSolved())
                    .orElse(0);
            case WEEKLY -> sumBetween(today.minusDays(6), today);
            case MONTHLY -> sumBetween(today.withDayOfMonth(1), today);
        };
    }

    private int sumBetween(final LocalDate start, final LocalDate end) {
        return activityRepository.findBetween(start, end).stream()
                .mapToInt(activity -> activity.getProblemsSolved())
                .sum();
    }

    @Override
    public void deleteGoal(final int id) {
        goalRepository.deleteById(id);
    }
}
