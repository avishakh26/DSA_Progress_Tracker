package com.dsatracker.repository;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.Profile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteProfileRepositoryTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final ProfileRepository repository = new SqliteProfileRepository();

    @BeforeEach
    void setUp() {
        manager.initializeForTesting("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Test
    void findReturnsEmptyBeforeAnyProfileIsSaved() {
        assertTrue(repository.find().isEmpty());
    }

    @Test
    void saveThenFindRoundTripsAllFields() {
        final Profile profile = new Profile();
        profile.setDisplayName("Ada");
        profile.setEmail("ada@example.com");
        profile.setBio("Learning DSA.");
        profile.setPhotoPath("data/profile/avatar.png");

        repository.save(profile);

        final Profile reloaded = repository.find().orElseThrow();
        assertEquals("Ada", reloaded.getDisplayName());
        assertEquals("ada@example.com", reloaded.getEmail());
        assertEquals("Learning DSA.", reloaded.getBio());
        assertEquals("data/profile/avatar.png", reloaded.getPhotoPath());
    }

    @Test
    void savingTwiceUpdatesTheSingleRowRatherThanInsertingAnother() {
        final Profile first = new Profile();
        first.setDisplayName("Ada");
        repository.save(first);

        final Profile second = new Profile();
        second.setDisplayName("Grace");
        repository.save(second);

        assertEquals("Grace", repository.find().orElseThrow().getDisplayName());
    }
}
