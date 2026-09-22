package com.dsatracker.repository;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.DiaryEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteDiaryEntryRepositoryTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final DiaryEntryRepository repository = new SqliteDiaryEntryRepository();

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void saveAndFindByIdRoundTripsContentAndTimestampsToSecondPrecision() {
        final DiaryEntry entry = new DiaryEntry("Cracked two-pointer", "Finally understood the shrink condition.");

        repository.save(entry);

        final DiaryEntry reloaded = repository.findById(entry.getId()).orElseThrow();
        assertEquals("Cracked two-pointer", reloaded.getTitle());
        assertEquals("Finally understood the shrink condition.", reloaded.getContent());
        assertEquals(LocalDate.now(), reloaded.getEntryDate());
        assertEquals(entry.getCreatedAt().withNano(0), reloaded.getCreatedAt());
        assertEquals(entry.getUpdatedAt().withNano(0), reloaded.getUpdatedAt());
    }

    @Test
    void findAllOrdersMostRecentEntryFirst() {
        repository.save(new DiaryEntry("First entry", "content"));
        repository.save(new DiaryEntry("Second entry", "content"));

        final List<DiaryEntry> entries = repository.findAll();

        assertFalse(entries.isEmpty());
        assertEquals("Second entry", entries.get(0).getTitle());
    }

    @Test
    void updateEntryThenSavePersistsTheNewTitleAndContent() {
        final DiaryEntry entry = repository.save(new DiaryEntry("Draft", "v1"));

        entry.updateEntry("Final", "v2");
        repository.save(entry);

        final DiaryEntry reloaded = repository.findById(entry.getId()).orElseThrow();
        assertEquals("Final", reloaded.getTitle());
        assertEquals("v2", reloaded.getContent());
    }

    @Test
    void deleteByIdRemovesTheEntry() {
        final DiaryEntry entry = repository.save(new DiaryEntry("Temp", "content"));

        assertTrue(repository.deleteById(entry.getId()));

        assertTrue(repository.findById(entry.getId()).isEmpty());
    }
}
