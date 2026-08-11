package com.dsatracker.service;

import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.Difficulty;

import java.util.List;
import java.util.Optional;

public interface TopicService {

    List<Topic> getAllTopics();

    List<Topic> getTopicsByPhase(int phase);

    Optional<Topic> findById(int id);

    Topic createTopic(String name, String description, int phase, Difficulty difficulty);

    Topic updateTopic(Topic topic);

    void deleteTopic(int id);

    /** Every topic paired with its live problem counts, for the Roadmap view. */
    List<TopicProgress> getRoadmap();

    /** Re-derives NOT_STARTED/IN_PROGRESS/COMPLETED from the topic's current problems and persists it if changed. */
    void recalculateStatus(int topicId);
}
