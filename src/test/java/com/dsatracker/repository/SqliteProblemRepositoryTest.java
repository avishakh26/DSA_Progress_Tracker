package com.dsatracker.repository;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.Problem;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
import com.dsatracker.model.enums.ProblemStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteProblemRepositoryTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final TopicRepository topicRepository = new SqliteTopicRepository();
    private final ProblemRepository repository = new SqliteProblemRepository();

    private Integer arraysTopicId;

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
        arraysTopicId = topicRepository.findByPhase(1).stream()
                .filter(t -> t.getName().equals("Arrays"))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void saveAndFindByIdRoundTripsNullableFields() {
        final Problem problem = new Problem("Container With Most Water", Platform.LEETCODE, null,
                arraysTopicId, Difficulty.MEDIUM);

        repository.save(problem);

        final Problem reloaded = repository.findById(problem.getId()).orElseThrow();
        assertEquals("Container With Most Water", reloaded.getTitle());
        assertNotNull(reloaded.getDateAdded());
        assertNull(reloaded.getUrl());
        assertNull(reloaded.getDateSolved());
    }

    @Test
    void markSolvedThenSavePersistsStatusAndSolvedDate() {
        final Problem problem = repository.save(
                new Problem("Rotate Array", Platform.LEETCODE, null, arraysTopicId, Difficulty.MEDIUM));

        problem.markSolved();
        repository.save(problem);

        final Problem reloaded = repository.findById(problem.getId()).orElseThrow();
        assertEquals(ProblemStatus.SOLVED, reloaded.getStatus());
        assertEquals(problem.getDateSolved(), reloaded.getDateSolved());
    }

    @Test
    void findByTopicIdReturnsOnlyThatTopicsProblems() {
        final List<Problem> problems = repository.findByTopicId(arraysTopicId);

        assertFalse(problems.isEmpty(), "Seed data should include Arrays problems");
        assertTrue(problems.stream().allMatch(p -> p.getTopicId().equals(arraysTopicId)));
    }

    @Test
    void findByStatusReturnsOnlyMatchingProblems() {
        final List<Problem> solved = repository.findByStatus(ProblemStatus.SOLVED);

        assertFalse(solved.isEmpty(), "Seed data should include solved problems");
        assertTrue(solved.stream().allMatch(p -> p.getStatus() == ProblemStatus.SOLVED));
    }

    @Test
    void searchByTitleFindsCaseInsensitiveSubstring() {
        final List<Problem> results = repository.searchByTitle("two sum");

        assertTrue(results.stream().anyMatch(p -> p.getTitle().equals("Two Sum")));
    }

    @Test
    void deletingATopicCascadesToDeleteItsProblems() {
        assertFalse(repository.findByTopicId(arraysTopicId).isEmpty());

        topicRepository.deleteById(arraysTopicId);

        assertTrue(repository.findByTopicId(arraysTopicId).isEmpty(),
                "ON DELETE CASCADE should remove problems belonging to a deleted topic");
    }
}
