package com.dsatracker.repository;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.Activity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteActivityRepositoryTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final ActivityRepository repository = new SqliteActivityRepository();

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void findByDateReturnsTheSeededYesterdayEntry() {
        final Activity yesterday = repository.findByDate(LocalDate.now().minusDays(1)).orElseThrow();

        assertEquals(2, yesterday.getProblemsSolved());
    }

    @Test
    void findBetweenReturnsBothSeededDaysInAscendingOrder() {
        final List<Activity> range = repository.findBetween(LocalDate.now().minusDays(1), LocalDate.now());

        assertEquals(2, range.size());
        assertTrue(range.get(0).getActivityDate().isBefore(range.get(1).getActivityDate()));
    }

    @Test
    void incrementSolvedThenSavePersistsTheNewCount() {
        final Activity today = repository.findByDate(LocalDate.now()).orElseThrow();
        assertEquals(0, today.getProblemsSolved());

        today.incrementSolved();
        repository.save(today);

        final Activity reloaded = repository.findByDate(LocalDate.now()).orElseThrow();
        assertEquals(1, reloaded.getProblemsSolved());
    }

    @Test
    void findAllOrderedByDateIsAscending() {
        final List<Activity> all = repository.findAllOrderedByDate();

        for (int i = 1; i < all.size(); i++) {
            assertTrue(!all.get(i).getActivityDate().isBefore(all.get(i - 1).getActivityDate()));
        }
    }
}
