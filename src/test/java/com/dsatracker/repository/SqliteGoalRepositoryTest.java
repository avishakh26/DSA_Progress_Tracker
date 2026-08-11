package com.dsatracker.repository;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.Goal;
import com.dsatracker.model.enums.GoalType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteGoalRepositoryTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final GoalRepository repository = new SqliteGoalRepository();

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void findCurrentReturnsTheSeededDailyGoalForToday() {
        final Optional<Goal> current = repository.findCurrent(GoalType.DAILY, LocalDate.now());

        assertTrue(current.isPresent());
        assertEquals(3, current.get().getTarget());
    }

    @Test
    void findCurrentIsEmptyBeforeTheGoalsStartDate() {
        final Optional<Goal> current = repository.findCurrent(GoalType.DAILY, LocalDate.now().minusYears(1));

        assertTrue(current.isEmpty());
    }

    @Test
    void saveAndDeleteRoundTrip() {
        final Goal goal = repository.save(new Goal(GoalType.WEEKLY, 10, LocalDate.now()));

        assertTrue(repository.findById(goal.getId()).isPresent());
        assertTrue(repository.deleteById(goal.getId()));
        assertTrue(repository.findById(goal.getId()).isEmpty());
    }
}
