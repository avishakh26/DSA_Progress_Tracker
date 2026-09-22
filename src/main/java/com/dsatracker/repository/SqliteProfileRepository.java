package com.dsatracker.repository;

import com.dsatracker.model.Profile;
import com.dsatracker.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public final class SqliteProfileRepository extends AbstractSqliteRepository<Profile> implements ProfileRepository {

    @Override
    public Optional<Profile> find() {
        return queryOne("SELECT * FROM profile WHERE id = 1", statement -> { });
    }

    @Override
    public Profile save(final Profile profile) {
        executeUpdate("""
                INSERT INTO profile (id, display_name, email, bio, photo_path, updated_at)
                VALUES (1, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    display_name = excluded.display_name,
                    email = excluded.email,
                    bio = excluded.bio,
                    photo_path = excluded.photo_path,
                    updated_at = excluded.updated_at
                """,
                statement -> {
                    statement.setString(1, profile.getDisplayName());
                    statement.setString(2, profile.getEmail());
                    statement.setString(3, profile.getBio());
                    statement.setString(4, profile.getPhotoPath());
                    statement.setString(5, profile.getUpdatedAt().format(DateTimeUtil.SQLITE_DATETIME));
                });
        return profile;
    }

    @Override
    protected Profile mapRow(final ResultSet resultSet) throws SQLException {
        final Profile profile = new Profile();
        profile.setDisplayName(resultSet.getString("display_name"));
        profile.setEmail(resultSet.getString("email"));
        profile.setBio(resultSet.getString("bio"));
        profile.setPhotoPath(resultSet.getString("photo_path"));
        profile.setUpdatedAt(LocalDateTime.parse(resultSet.getString("updated_at"), DateTimeUtil.SQLITE_DATETIME));
        return profile;
    }
}
