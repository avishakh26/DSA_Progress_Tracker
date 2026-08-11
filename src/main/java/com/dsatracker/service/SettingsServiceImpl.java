package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.Activity;
import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.ProblemStatus;
import com.dsatracker.model.enums.TopicStatus;
import com.dsatracker.repository.ActivityRepository;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.TopicRepository;

/**
 * Orchestrates the Settings page's data-management actions. {@code resetProgress}
 * goes through the normal repositories like any other mutation; {@code restoreSampleData}
 * and {@code clearAllData} delegate to {@link DatabaseManager} because re-running the
 * seed script is a capability only it has - not something expressible through the
 * per-entity repositories.
 */
public final class SettingsServiceImpl implements SettingsService {

    private final TopicRepository topicRepository;
    private final ProblemRepository problemRepository;
    private final ActivityRepository activityRepository;
    private final DatabaseManager databaseManager;

    public SettingsServiceImpl(final TopicRepository topicRepository, final ProblemRepository problemRepository,
                                final ActivityRepository activityRepository, final DatabaseManager databaseManager) {
        this.topicRepository = topicRepository;
        this.problemRepository = problemRepository;
        this.activityRepository = activityRepository;
        this.databaseManager = databaseManager;
    }

    @Override
    public void resetProgress() {
        for (final Problem problem : problemRepository.findAll()) {
            problem.setStatus(ProblemStatus.NOT_STARTED);
            problem.setDateSolved(null);
            problemRepository.save(problem);
        }
        for (final Topic topic : topicRepository.findAll()) {
            topic.setStatus(TopicStatus.NOT_STARTED);
            topicRepository.save(topic);
        }
        for (final Activity activity : activityRepository.findAll()) {
            activityRepository.deleteById(activity.getId());
        }
    }

    @Override
    public void restoreSampleData() {
        databaseManager.restoreSampleData();
    }

    @Override
    public void clearAllData() {
        databaseManager.clearAllData();
    }
}
