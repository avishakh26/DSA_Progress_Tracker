package com.dsatracker.service;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.ProblemStatus;
import com.dsatracker.model.enums.TopicStatus;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.TopicRepository;
import com.dsatracker.util.Validator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final ProblemRepository problemRepository;

    public TopicServiceImpl(final TopicRepository topicRepository, final ProblemRepository problemRepository) {
        this.topicRepository = topicRepository;
        this.problemRepository = problemRepository;
    }

    @Override
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    @Override
    public List<Topic> getTopicsByPhase(final int phase) {
        return topicRepository.findByPhase(phase);
    }

    @Override
    public Optional<Topic> findById(final int id) {
        return topicRepository.findById(id);
    }

    @Override
    public Topic createTopic(final String name, final String description, final int phase, final Difficulty difficulty) {
        Validator.requireNonBlank(name, "Topic name");
        Validator.requireInRange(phase, 1, 6, "Phase");
        return topicRepository.save(new Topic(name, description, phase, difficulty));
    }

    @Override
    public Topic updateTopic(final Topic topic) {
        Validator.requireNonBlank(topic.getName(), "Topic name");
        Validator.requireInRange(topic.getPhase(), 1, 6, "Phase");
        return topicRepository.save(topic);
    }

    @Override
    public void deleteTopic(final int id) {
        topicRepository.deleteById(id);
    }

    @Override
    public List<TopicProgress> getRoadmap() {
        return topicRepository.findAll().stream()
                .map(this::withProgress)
                .collect(Collectors.toList());
    }

    private TopicProgress withProgress(final Topic topic) {
        final List<Problem> problems = problemRepository.findByTopicId(topic.getId());
        final long solved = problems.stream().filter(p -> p.getStatus() == ProblemStatus.SOLVED).count();
        return new TopicProgress(topic, problems.size(), (int) solved);
    }

    @Override
    public void recalculateStatus(final int topicId) {
        final Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("No topic found with id " + topicId));
        final List<Problem> problems = problemRepository.findByTopicId(topicId);
        final long solved = problems.stream().filter(p -> p.getStatus() == ProblemStatus.SOLVED).count();
        final long attempted = problems.stream().filter(p -> p.getStatus() == ProblemStatus.ATTEMPTED).count();

        final TopicStatus newStatus;
        if (problems.isEmpty()) {
            newStatus = TopicStatus.NOT_STARTED;
        } else if (solved == problems.size()) {
            newStatus = TopicStatus.COMPLETED;
        } else if (solved > 0 || attempted > 0) {
            newStatus = TopicStatus.IN_PROGRESS;
        } else {
            newStatus = TopicStatus.NOT_STARTED;
        }

        if (newStatus != topic.getStatus()) {
            topic.setStatus(newStatus);
            topicRepository.save(topic);
        }
    }
}
