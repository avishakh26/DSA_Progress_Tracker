package com.dsatracker.repository;

import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.TopicStatus;

import java.util.List;

public interface TopicRepository extends Repository<Topic, Integer> {
    List<Topic> findByPhase(int phase);
    List<Topic> findByStatus(TopicStatus status);
}
