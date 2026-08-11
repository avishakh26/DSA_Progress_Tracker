package com.dsatracker.service;

import com.dsatracker.model.Topic;

/** A topic paired with its live problem counts, for the Roadmap view. */
public record TopicProgress(Topic topic, int totalProblems, int solvedProblems) {

    public double percentComplete() {
        return totalProblems == 0 ? 0.0 : solvedProblems * 100.0 / totalProblems;
    }
}
