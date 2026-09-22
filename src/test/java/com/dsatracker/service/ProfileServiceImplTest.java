package com.dsatracker.service;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.model.Profile;
import com.dsatracker.repository.ProfileRepository;
import com.dsatracker.repository.SqliteProfileRepository;
import com.dsatracker.util.AppConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileServiceImplTest {

    private final DatabaseManager manager = DatabaseManager.getInstance();
    private final ProfileRepository profileRepository = new SqliteProfileRepository();
    private final ProfileService service = new ProfileServiceImpl(profileRepository);

    private final Path photoDir = Path.of(AppConstants.DATA_DIRECTORY, AppConstants.PROFILE_PHOTO_DIRECTORY);
    private Path sourceImage;

    @BeforeEach
    void setUp() throws IOException {
        manager.initializeForTesting("jdbc:sqlite::memory:");
        sourceImage = Files.createTempFile("avatar-source", ".png");
        Files.write(sourceImage, new byte[]{1, 2, 3, 4});
    }

    @AfterEach
    void tearDown() throws IOException {
        manager.close();
        Files.deleteIfExists(sourceImage);
        deleteRecursively(photoDir);
    }

    @Test
    void getProfileReturnsABlankProfileBeforeAnythingIsSaved() {
        final Profile profile = service.getProfile();

        assertNull(profile.getDisplayName());
        assertNull(profile.getPhotoPath());
    }

    @Test
    void updateProfilePersistsDisplayNameEmailAndBio() {
        service.updateProfile("Ada", "ada@example.com", "Learning DSA.");

        final Profile profile = service.getProfile();
        assertEquals("Ada", profile.getDisplayName());
        assertEquals("ada@example.com", profile.getEmail());
        assertEquals("Learning DSA.", profile.getBio());
    }

    @Test
    void updatePhotoCopiesTheFileIntoTheDataDirectoryAndStoresItsPath() {
        final Profile profile = service.updatePhoto(sourceImage);

        assertTrue(profile.getPhotoPath().endsWith("avatar.png"));
        assertTrue(Files.exists(Path.of(profile.getPhotoPath())));
    }

    @Test
    void removePhotoDeletesTheFileAndClearsThePath() {
        service.updatePhoto(sourceImage);

        final Profile profile = service.removePhoto();

        assertNull(profile.getPhotoPath());
        assertFalse(Files.exists(photoDir.resolve("avatar.png")));
    }

    private static void deleteRecursively(final Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (final IOException ignored) {
                    // best-effort test cleanup
                }
            });
        }
    }
}
