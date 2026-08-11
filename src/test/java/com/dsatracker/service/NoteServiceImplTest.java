package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.exception.ValidationException;
import com.dsatracker.model.Note;
import com.dsatracker.repository.NoteRepository;
import com.dsatracker.repository.SqliteNoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoteServiceImplTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final NoteRepository noteRepository = new SqliteNoteRepository();
    private final NoteService service = new NoteServiceImpl(noteRepository);

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void createNoteRejectsBlankTitle() {
        assertThrows(ValidationException.class, () -> service.createNote(" ", null, "content"));
    }

    @Test
    void updateNoteContentOnMissingIdThrows() {
        assertThrows(EntityNotFoundException.class, () -> service.updateNoteContent(999_999, "x"));
    }

    @Test
    void updateNoteContentPersistsTheNewContent() {
        final Note note = service.createNote("Title", null, "v1");

        service.updateNoteContent(note.getId(), "v2");

        assertEquals("v2", noteRepository.findById(note.getId()).orElseThrow().getContent());
    }

    @Test
    void updateNotePersistsTitleTopicAndContentTogether() {
        final Note note = service.createNote("Original Title", null, "original content");

        note.setTitle("New Title");
        note.setTopicId(null);
        note.setContent("new content");
        service.updateNote(note);

        final Note reloaded = noteRepository.findById(note.getId()).orElseThrow();
        assertEquals("New Title", reloaded.getTitle());
        assertEquals("new content", reloaded.getContent());
    }

    @Test
    void updateNoteRejectsBlankTitle() {
        final Note note = service.createNote("Title", null, "content");
        note.setTitle(" ");

        assertThrows(ValidationException.class, () -> service.updateNote(note));
    }
}
