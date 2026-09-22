package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.exception.ValidationException;
import com.dsatracker.model.DiaryEntry;
import com.dsatracker.repository.DiaryEntryRepository;
import com.dsatracker.repository.SqliteDiaryEntryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiaryServiceImplTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final DiaryEntryRepository diaryEntryRepository = new SqliteDiaryEntryRepository();
    private final DiaryService service = new DiaryServiceImpl(diaryEntryRepository);

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void createEntryRejectsBlankTitle() {
        assertThrows(ValidationException.class, () -> service.createEntry(" ", "content"));
    }

    @Test
    void updateEntryOnMissingIdThrows() {
        assertThrows(EntityNotFoundException.class, () -> service.updateEntry(999_999, "Title", "content"));
    }

    @Test
    void updateEntryPersistsTheNewTitleAndContent() {
        final DiaryEntry entry = service.createEntry("Original", "v1");

        service.updateEntry(entry.getId(), "Revised", "v2");

        final DiaryEntry reloaded = diaryEntryRepository.findById(entry.getId()).orElseThrow();
        assertEquals("Revised", reloaded.getTitle());
        assertEquals("v2", reloaded.getContent());
    }

    @Test
    void updateEntryRejectsBlankTitle() {
        final DiaryEntry entry = service.createEntry("Original", "content");

        assertThrows(ValidationException.class, () -> service.updateEntry(entry.getId(), " ", "content"));
    }

    @Test
    void deleteEntryRemovesIt() {
        final DiaryEntry entry = service.createEntry("Temp", "content");

        service.deleteEntry(entry.getId());

        assertEquals(0, service.getAllEntries().size());
    }
}
