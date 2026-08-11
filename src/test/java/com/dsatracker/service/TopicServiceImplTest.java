package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.exception.ValidationException;
import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
import com.dsatracker.model.enums.TopicStatus;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.SqliteProblemRepository;
import com.dsatracker.repository.SqliteTopicRepository;
import com.dsatracker.repository.TopicRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopicServiceImplTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final TopicRepository topicRepository = new SqliteTopicRepository();
    private final ProblemRepository problemRepository = new SqliteProblemRepository();
    private final TopicService service = new TopicServiceImpl(topicRepository, problemRepository);

    private Integer topicId;

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
        final Topic topic = topicRepository.save(new Topic("Recalc Target", "desc", 1, Difficulty.EASY));
        topicId = topic.getId();
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void topicWithNoProblemsStaysNotStarted() {
        service.recalculateStatus(topicId);

        assertEquals(TopicStatus.NOT_STARTED, topicRepository.findById(topicId).orElseThrow().getStatus());
    }

    @Test
    void topicWithAnAttemptedProblemBecomesInProgress() {
        final Problem problem = problemRepository.save(
                new Problem("P1", Platform.LEETCODE, null, topicId, Difficulty.EASY));
        problem.markAttempted();
        problemRepository.save(problem);

        service.recalculateStatus(topicId);

        assertEquals(TopicStatus.IN_PROGRESS, topicRepository.findById(topicId).orElseThrow().getStatus());
    }

    @Test
    void topicWithAllProblemsSolvedBecomesCompleted() {
        final Problem p1 = problemRepository.save(new Problem("P1", Platform.LEETCODE, null, topicId, Difficulty.EASY));
        final Problem p2 = problemRepository.save(new Problem("P2", Platform.LEETCODE, null, topicId, Difficulty.EASY));
        p1.markSolved();
        p2.markSolved();
        problemRepository.save(p1);
        problemRepository.save(p2);

        service.recalculateStatus(topicId);

        assertEquals(TopicStatus.COMPLETED, topicRepository.findById(topicId).orElseThrow().getStatus());
    }

    @Test
    void topicRegressesFromCompletedIfANewUnsolvedProblemIsAdded() {
        final Problem p1 = problemRepository.save(new Problem("P1", Platform.LEETCODE, null, topicId, Difficulty.EASY));
        p1.markSolved();
        problemRepository.save(p1);
        service.recalculateStatus(topicId);
        assertEquals(TopicStatus.COMPLETED, topicRepository.findById(topicId).orElseThrow().getStatus());

        problemRepository.save(new Problem("P2", Platform.LEETCODE, null, topicId, Difficulty.EASY));
        service.recalculateStatus(topicId);

        assertEquals(TopicStatus.IN_PROGRESS, topicRepository.findById(topicId).orElseThrow().getStatus());
    }

    @Test
    void getRoadmapReportsCorrectCountsAndPercent() {
        final Problem p1 = problemRepository.save(new Problem("P1", Platform.LEETCODE, null, topicId, Difficulty.EASY));
        problemRepository.save(new Problem("P2", Platform.LEETCODE, null, topicId, Difficulty.EASY));
        p1.markSolved();
        problemRepository.save(p1);

        final TopicProgress progress = service.getRoadmap().stream()
                .filter(tp -> tp.topic().getId().equals(topicId))
                .findFirst()
                .orElseThrow();

        assertEquals(2, progress.totalProblems());
        assertEquals(1, progress.solvedProblems());
        assertEquals(50.0, progress.percentComplete());
    }

    @Test
    void createTopicRejectsBlankNameAndOutOfRangePhase() {
        assertThrows(ValidationException.class, () -> service.createTopic("  ", "desc", 1, Difficulty.EASY));
        assertThrows(ValidationException.class, () -> service.createTopic("Name", "desc", 7, Difficulty.EASY));
    }

    @Test
    void createTopicAcceptsValidInput() {
        final Topic topic = service.createTopic("New Topic", "desc", 2, Difficulty.MEDIUM);

        assertFalse(topic.getId() == null);
        assertTrue(topicRepository.findById(topic.getId()).isPresent());
    }
}
