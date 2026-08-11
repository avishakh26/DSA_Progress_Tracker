package com.dsatracker.model;

import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
import com.dsatracker.model.enums.ProblemStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProblemTest {

    @Test
    void newProblemDefaultsToNotStartedWithNoSolvedDate() {
        final Problem problem = new Problem("Two Sum", Platform.LEETCODE, "https://leetcode.com/problems/two-sum/",
                1, Difficulty.EASY);

        assertEquals(ProblemStatus.NOT_STARTED, problem.getStatus());
        assertNull(problem.getDateSolved());
        assertEquals(LocalDate.now(), problem.getDateAdded());
    }

    @Test
    void markSolvedSetsStatusAndStampsToday() {
        final Problem problem = new Problem("Two Sum", Platform.LEETCODE, null, 1, Difficulty.EASY);

        problem.markSolved();

        assertEquals(ProblemStatus.SOLVED, problem.getStatus());
        assertNotNull(problem.getDateSolved());
        assertEquals(LocalDate.now(), problem.getDateSolved());
    }

    @Test
    void markAttemptedDoesNotDowngradeAnAlreadySolvedProblem() {
        final Problem problem = new Problem("Two Sum", Platform.LEETCODE, null, 1, Difficulty.EASY);
        problem.markSolved();

        problem.markAttempted();

        assertEquals(ProblemStatus.SOLVED, problem.getStatus(), "Solved problems must not regress to Attempted");
    }
}
