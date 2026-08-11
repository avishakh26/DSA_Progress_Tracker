package com.dsatracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoteTest {

    @Test
    void updateContentChangesContentAndBumpsUpdatedAtButNotCreatedAt() throws InterruptedException {
        final Note note = new Note("Cheat sheet", 1, "original content");
        final LocalDateTime createdAt = note.getCreatedAt();
        final LocalDateTime firstUpdatedAt = note.getUpdatedAt();

        Thread.sleep(2); // ensure the next timestamp is strictly later
        note.updateContent("revised content");

        assertEquals("revised content", note.getContent());
        assertEquals(createdAt, note.getCreatedAt(), "createdAt must never change after construction");
        assertTrue(note.getUpdatedAt().isAfter(firstUpdatedAt), "updatedAt must advance on every content update");
    }

    @Test
    void setContentDoesNotTouchUpdatedAt() {
        final Note note = new Note("Cheat sheet", 1, "original content");
        final LocalDateTime firstUpdatedAt = note.getUpdatedAt();

        note.setContent("loaded from DB row");

        assertEquals(firstUpdatedAt, note.getUpdatedAt(), "Row-mapping setter must not simulate a user edit");
    }
}
