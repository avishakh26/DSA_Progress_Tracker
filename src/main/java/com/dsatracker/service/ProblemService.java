package com.dsatracker.service;

import com.dsatracker.model.Problem;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;

import java.util.List;
import java.util.Optional;

public interface ProblemService {

    List<Problem> getAllProblems();

    Optional<Problem> findById(int id);

    Problem addProblem(String title, Platform platform, String url, int topicId, Difficulty difficulty, String notes);

    Problem updateProblem(Problem problem);

    void deleteProblem(int id);

    Problem markSolved(int id);

    Problem markAttempted(int id);

    List<Problem> search(ProblemFilter filter, ProblemSortBy sortBy);

    /** Total and solved counts per difficulty tier, for the Analytics difficulty-distribution chart. */
    List<DifficultyBreakdown> getDifficultyBreakdown();
}
