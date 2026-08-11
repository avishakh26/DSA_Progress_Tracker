package com.dsatracker.repository;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.Note;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteNoteRepositoryTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final TopicRepository topicRepository = new SqliteTopicRepository();
    private final NoteRepository repository = new SqliteNoteRepository();

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
    void saveAndFindByIdRoundTripsContentAndTimestampsToSecondPrecision() {
        final Note note = new Note("Sliding window", arraysTopicId, "shrink from the left when the window is invalid");

        repository.save(note);

        final Note reloaded = repository.findById(note.getId()).orElseThrow();
        assertEquals("Sliding window", reloaded.getTitle());
        assertEquals("shrink from the left when the window is invalid", reloaded.getContent());
        // SQLite storage truncates to whole seconds; compare with the same precision.
        assertEquals(note.getCreatedAt().withNano(0), reloaded.getCreatedAt());
        assertEquals(note.getUpdatedAt().withNano(0), reloaded.getUpdatedAt());
    }

    @Test
    void findByTopicIdReturnsOnlyThatTopicsNotes() {
        repository.save(new Note("Extra note", arraysTopicId, "content"));

        final List<Note> notes = repository.findByTopicId(arraysTopicId);

        assertFalse(notes.isEmpty(), "Seed data already includes an Arrays note");
        assertTrue(notes.stream().allMatch(n -> n.getTopicId().equals(arraysTopicId)));
    }

    @Test
    void updateContentThenSavePersistsTheNewContent() {
        final Note note = repository.save(new Note("Draft", arraysTopicId, "v1"));

        note.updateContent("v2");
        repository.save(note);

        final Note reloaded = repository.findById(note.getId()).orElseThrow();
        assertEquals("v2", reloaded.getContent());
        assertEquals(note.getUpdatedAt().withNano(0), reloaded.getUpdatedAt());
    }
}
