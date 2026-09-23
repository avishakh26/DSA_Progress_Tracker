package com.dsatracker.service;

import com.dsatracker.model.Profile;
import javafx.scene.image.WritableImage;

import java.nio.file.Path;

public interface ProfileService {

    /** @return the saved profile, or a blank one if the user has never saved one yet */
    Profile getProfile();

    Profile updateProfile(String displayName, String email, String bio);

    /** Copies {@code sourceImageFile} into the app's data directory and stores it as the profile picture. */
    Profile updatePhoto(Path sourceImageFile);

    /** Saves an already-cropped/resized avatar image (e.g. from the photo resize dialog) as the profile picture. */
    Profile updatePhoto(WritableImage image);

    Profile removePhoto();
}
