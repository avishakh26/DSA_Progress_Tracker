package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.enums.ProblemStatus;
import com.dsatracker.model.enums.TopicStatus;
import com.dsatracker.repository.ActivityRepository;
import com.dsatracker.repository.NoteRepository;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.SqliteActivityRepository;
import com.dsatracker.repository.SqliteNoteRepository;
import com.dsatracker.repository.SqliteProblemRepository;
import com.dsatracker.repository.SqliteTopicRepository;
import com.dsatracker.repository.TopicRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsServiceImplTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final TopicRepository topicRepository = new SqliteTopicRepository();
    private final ProblemRepository problemRepository = new SqliteProblemRepository();
    private final NoteRepository noteRepository = new SqliteNoteRepository();
    private final ActivityRepository activityRepository = new SqliteActivityRepository();
    private final SettingsService service =
            new SettingsServiceImpl(topicRepository, problemRepository, activityRepository, manager);

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void resetProgressClearsStatusesAndActivityButKeepsRows() {
        final int problemCountBefore = problemRepository.findAll().size();
        final int topicCountBefore = topicRepository.findAll().size();
        assertTrue(problemCountBefore > 0, "Seed data should include problems");

        service.resetProgress();

        assertEquals(problemCountBefore, problemRepository.findAll().size(), "Reset must not delete problems");
        assertEquals(topicCountBefore, topicRepository.findAll().size(), "Reset must not delete topics");
        assertTrue(problemRepository.findAll().stream().allMatch(p -> p.getStatus() == ProblemStatus.NOT_STARTED));
        assertTrue(problemRepository.findAll().stream().allMatch(p -> p.getDateSolved() == null));
        assertTrue(topicRepository.findAll().stream().allMatch(t -> t.getStatus() == TopicStatus.NOT_STARTED));
        assertTrue(activityRepository.findAll().isEmpty());
    }

    @Test
    void restoreSampleDataResetsEverythingToTheOriginalSeed() {
        // Mutate state first, so restoring is a real change, not a no-op.
        problemRepository.deleteById(problemRepository.findAll().get(0).getId());
        assertTrue(problemRepository.findAll().size() < 5);

        service.restoreSampleData();

        assertEquals(17, topicRepository.findAll().size());
        assertEquals(5, problemRepository.findAll().size());
        assertEquals(1, noteRepository.findAll().size());
    }

    @Test
    void clearAllDataEmptiesEveryTable() {
        service.clearAllData();

        assertTrue(topicRepository.findAll().isEmpty());
        assertTrue(problemRepository.findAll().isEmpty());
        assertTrue(noteRepository.findAll().isEmpty());
        assertTrue(activityRepository.findAll().isEmpty());
    }
}
