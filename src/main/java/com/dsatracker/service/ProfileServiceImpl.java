package com.dsatracker.service;

import com.dsatracker.model.Profile;
import com.dsatracker.repository.ProfileRepository;
import com.dsatracker.util.AppConstants;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public final class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(final ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile getProfile() {
        return profileRepository.find().orElseGet(Profile::new);
    }

    @Override
    public Profile updateProfile(final String displayName, final String email, final String bio) {
        final Profile profile = getProfile();
        profile.setDisplayName(displayName);
        profile.setEmail(email);
        profile.setBio(bio);
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }

    @Override
    public Profile updatePhoto(final Path sourceImageFile) {
        try {
            final Path photoDir = Path.of(AppConstants.DATA_DIRECTORY, AppConstants.PROFILE_PHOTO_DIRECTORY);
            Files.createDirectories(photoDir);
            // Only one avatar file is ever kept - clear whatever's there first so switching from
            // a .png to a .jpg (different filename) doesn't leave the old file orphaned on disk.
            clearExistingPhotos(photoDir);

            final Path target = photoDir.resolve("avatar" + extensionOf(sourceImageFile));
            Files.copy(sourceImageFile, target, StandardCopyOption.REPLACE_EXISTING);

            final Profile profile = getProfile();
            profile.setPhotoPath(target.toString());
            profile.setUpdatedAt(LocalDateTime.now());
            return profileRepository.save(profile);
        } catch (final IOException e) {
            throw new UncheckedIOException("Could not save the profile picture.", e);
        }
    }

    @Override
    public Profile removePhoto() {
        final Profile profile = getProfile();
        if (profile.getPhotoPath() != null) {
            try {
                Files.deleteIfExists(Path.of(profile.getPhotoPath()));
            } catch (final IOException e) {
                throw new UncheckedIOException("Could not remove the profile picture file.", e);
            }
        }
        profile.setPhotoPath(null);
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }

    private void clearExistingPhotos(final Path photoDir) throws IOException {
        try (Stream<Path> files = Files.list(photoDir)) {
            for (final Path file : files.toList()) {
                Files.deleteIfExists(file);
            }
        }
    }

    private String extensionOf(final Path file) {
        final String name = file.getFileName().toString();
        final int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot);
    }
}
