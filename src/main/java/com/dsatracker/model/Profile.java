package com.dsatracker.model;

import java.time.LocalDateTime;

/** The local user's profile: display name, email, bio and picture. Always a single row (id 1). */
public class Profile {

    private String displayName;
    private String email;
    private String bio;
    private String photoPath;
    private LocalDateTime updatedAt;

    public Profile() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(final String bio) {
        this.bio = bio;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(final String photoPath) {
        this.photoPath = photoPath;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
