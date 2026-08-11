package com.dsatracker.repository;

import com.dsatracker.model.Problem;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.ProblemStatus;

import java.util.List;

public interface ProblemRepository extends Repository<Problem, Integer> {
    List<Problem> findByTopicId(Integer topicId);
    List<Problem> findByStatus(ProblemStatus status);
    List<Problem> findByDifficulty(Difficulty difficulty);
    List<Problem> searchByTitle(String keyword);
}
