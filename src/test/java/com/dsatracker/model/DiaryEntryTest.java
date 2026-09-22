package com.dsatracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiaryEntryTest {

    @Test
    void constructorStampsEntryDateToTheCreationDay() {
        final DiaryEntry entry = new DiaryEntry("Title", "content");

        assertEquals(LocalDate.now(), entry.getEntryDate());
    }

    @Test
    void updateEntryChangesTitleAndContentAndBumpsUpdatedAtButNotEntryDate() throws InterruptedException {
        final DiaryEntry entry = new DiaryEntry("Title", "original content");
        final LocalDate entryDate = entry.getEntryDate();
        final LocalDateTime firstUpdatedAt = entry.getUpdatedAt();

        Thread.sleep(2); // ensure the next timestamp is strictly later
        entry.updateEntry("New Title", "revised content");

        assertEquals("New Title", entry.getTitle());
        assertEquals("revised content", entry.getContent());
        assertEquals(entryDate, entry.getEntryDate(), "entryDate must never change after creation");
        assertTrue(entry.getUpdatedAt().isAfter(firstUpdatedAt), "updatedAt must advance on every edit");
    }
}
