package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.exception.ValidationException;
import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
import com.dsatracker.model.enums.ProblemStatus;
import com.dsatracker.model.enums.TopicStatus;
import com.dsatracker.repository.ActivityRepository;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.SqliteActivityRepository;
import com.dsatracker.repository.SqliteProblemRepository;
import com.dsatracker.repository.SqliteTopicRepository;
import com.dsatracker.repository.TopicRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemServiceImplTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final TopicRepository topicRepository = new SqliteTopicRepository();
    private final ProblemRepository problemRepository = new SqliteProblemRepository();
    private final ActivityRepository activityRepository = new SqliteActivityRepository();

    private final TopicService topicService = new TopicServiceImpl(topicRepository, problemRepository);
    private final ActivityService activityService = new ActivityServiceImpl(activityRepository);
    private final ProblemService service = new ProblemServiceImpl(problemRepository, topicService, activityService);

    private Integer topicId;

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
        topicId = topicRepository.save(new Topic("Test Topic", "desc", 1, Difficulty.EASY)).getId();
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void addProblemRejectsBlankTitleAndMalformedUrl() {
        assertThrows(ValidationException.class,
                () -> service.addProblem(" ", Platform.LEETCODE, null, topicId, Difficulty.EASY, null));
        assertThrows(ValidationException.class,
                () -> service.addProblem("Title", Platform.LEETCODE, "not a url", topicId, Difficulty.EASY, null));
    }

    @Test
    void addProblemAcceptsAValidHttpsUrl() {
        final Problem problem = service.addProblem("Two Sum", Platform.LEETCODE,
                "https://leetcode.com/problems/two-sum/", topicId, Difficulty.EASY, null);

        assertTrue(problem.getId() != null);
    }

    @Test
    void markSolvedUpdatesStatusRecordsActivityAndRecalculatesTopic() {
        final Problem problem = service.addProblem("Two Sum", Platform.LEETCODE, null, topicId, Difficulty.EASY, null);

        service.markSolved(problem.getId());

        final Problem reloaded = problemRepository.findById(problem.getId()).orElseThrow();
        assertEquals(ProblemStatus.SOLVED, reloaded.getStatus());

        assertEquals(1, activityRepository.findByDate(LocalDate.now()).orElseThrow().getProblemsSolved());
        assertEquals(TopicStatus.COMPLETED, topicRepository.findById(topicId).orElseThrow().getStatus());
    }

    @Test
    void markSolvingTwoProblemsOnTheSameDayAccumulatesActivity() {
        final Problem p1 = service.addProblem("P1", Platform.LEETCODE, null, topicId, Difficulty.EASY, null);
        final Problem p2 = service.addProblem("P2", Platform.LEETCODE, null, topicId, Difficulty.EASY, null);

        service.markSolved(p1.getId());
        service.markSolved(p2.getId());

        assertEquals(2, activityRepository.findByDate(LocalDate.now()).orElseThrow().getProblemsSolved());
    }

    @Test
    void searchFiltersByStatusAndSortsAlphabetically() {
        final Problem zeta = service.addProblem("Zeta", Platform.LEETCODE, null, topicId, Difficulty.EASY, null);
        final Problem alpha = service.addProblem("Alpha", Platform.LEETCODE, null, topicId, Difficulty.EASY, null);
        service.addProblem("Unsolved", Platform.LEETCODE, null, topicId, Difficulty.EASY, null);
        service.markSolved(zeta.getId());
        service.markSolved(alpha.getId());

        // Scope to this test's own topic - seed data already has other SOLVED problems elsewhere.
        final ProblemFilter filter = new ProblemFilter(null, topicId, null, null, ProblemStatus.SOLVED);
        final List<Problem> results = service.search(filter, ProblemSortBy.TITLE_ALPHABETICAL);

        assertEquals(List.of("Alpha", "Zeta"), results.stream().map(Problem::getTitle).toList());
    }

    @Test
    void searchFiltersByDifficulty() {
        service.addProblem("Easy One", Platform.LEETCODE, null, topicId, Difficulty.EASY, null);
        service.addProblem("Hard One", Platform.LEETCODE, null, topicId, Difficulty.HARD, null);

        final ProblemFilter filter = new ProblemFilter(null, null, Difficulty.HARD, null, null);
        final List<Problem> results = service.search(filter, ProblemSortBy.TITLE_ALPHABETICAL);

        assertEquals(1, results.size());
        assertEquals("Hard One", results.get(0).getTitle());
    }

    @Test
    void getDifficultyBreakdownReflectsTotalsAndSolvedCountsPerDifficulty() {
        // Seed data already has 3 EASY problems (2 solved) and 2 MEDIUM (0 solved), 0 HARD.
        service.addProblem("Hard One", Platform.LEETCODE, null, topicId, Difficulty.HARD, null);

        final Map<Difficulty, DifficultyBreakdown> byDifficulty = service.getDifficultyBreakdown().stream()
                .collect(Collectors.toMap(DifficultyBreakdown::difficulty, b -> b));

        assertEquals(3, byDifficulty.get(Difficulty.EASY).total());
        assertEquals(2, byDifficulty.get(Difficulty.EASY).solved());
        assertEquals(1, byDifficulty.get(Difficulty.EASY).unsolved());

        assertEquals(2, byDifficulty.get(Difficulty.MEDIUM).total());
        assertEquals(0, byDifficulty.get(Difficulty.MEDIUM).solved());

        assertEquals(1, byDifficulty.get(Difficulty.HARD).total());
        assertEquals(0, byDifficulty.get(Difficulty.HARD).solved());
    }
}
