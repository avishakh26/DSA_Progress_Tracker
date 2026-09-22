package com.dsatracker.repository;

import com.dsatracker.model.Profile;

import java.util.Optional;

/** Single-row DAO for the local user's profile - there is exactly one profile, never a list of them. */
public interface ProfileRepository {

    Optional<Profile> find();

    Profile save(Profile profile);
}
