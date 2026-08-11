package com.dsatracker.repository;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.TopicStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteTopicRepositoryTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final TopicRepository repository = new SqliteTopicRepository();

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void saveNewTopicAssignsGeneratedIdAndRoundTrips() {
        final Topic topic = new Topic("Tries (extra)", "Prefix tree drills", 6, Difficulty.HARD);

        repository.save(topic);

        assertNotNull(topic.getId());
        final Optional<Topic> reloaded = repository.findById(topic.getId());
        assertTrue(reloaded.isPresent());
        assertEquals("Tries (extra)", reloaded.get().getName());
        assertEquals(Difficulty.HARD, reloaded.get().getDifficulty());
        assertEquals(TopicStatus.NOT_STARTED, reloaded.get().getStatus());
    }

    @Test
    void savingExistingTopicUpdatesItsFields() {
        final Topic topic = repository.save(new Topic("Custom Topic", "desc", 1, Difficulty.EASY));

        topic.setStatus(TopicStatus.COMPLETED);
        topic.setName("Custom Topic (renamed)");
        repository.save(topic);

        final Topic reloaded = repository.findById(topic.getId()).orElseThrow();
        assertEquals(TopicStatus.COMPLETED, reloaded.getStatus());
        assertEquals("Custom Topic (renamed)", reloaded.getName());
    }

    @Test
    void updatingAMissingIdThrowsEntityNotFoundException() {
        final Topic ghost = new Topic("Ghost", "desc", 1, Difficulty.EASY);
        ghost.setId(999_999);

        assertThrows(EntityNotFoundException.class, () -> repository.save(ghost));
    }

    @Test
    void findByPhaseReturnsOnlyTopicsInThatPhase() {
        final List<Topic> phaseOne = repository.findByPhase(1);

        assertFalse(phaseOne.isEmpty(), "Seed data should include phase-1 topics");
        assertTrue(phaseOne.stream().allMatch(t -> t.getPhase() == 1));
    }

    @Test
    void deleteByIdRemovesTheRowAndIsIdempotent() {
        final Topic topic = repository.save(new Topic("Disposable", "desc", 1, Difficulty.EASY));

        assertTrue(repository.deleteById(topic.getId()));
        assertTrue(repository.findById(topic.getId()).isEmpty());
        assertFalse(repository.deleteById(topic.getId()), "Deleting an already-deleted id must return false, not throw");
    }
}
