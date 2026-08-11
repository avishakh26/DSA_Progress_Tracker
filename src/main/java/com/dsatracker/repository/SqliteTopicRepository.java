package com.dsatracker.repository;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.TopicStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class SqliteTopicRepository extends AbstractSqliteRepository<Topic> implements TopicRepository {

    @Override
    public Topic save(final Topic topic) {
        return topic.getId() == null ? insert(topic) : update(topic);
    }

    private Topic insert(final Topic topic) {
        final int id = executeInsert(
                "INSERT INTO topics (name, description, phase, difficulty, status) VALUES (?, ?, ?, ?, ?)",
                statement -> bind(statement, topic));
        topic.setId(id);
        return topic;
    }

    private Topic update(final Topic topic) {
        final int rows = executeUpdate(
                "UPDATE topics SET name = ?, description = ?, phase = ?, difficulty = ?, status = ? WHERE id = ?",
                statement -> {
                    bind(statement, topic);
                    statement.setInt(6, topic.getId());
                });
        if (rows == 0) {
            throw new EntityNotFoundException("No topic found with id " + topic.getId());
        }
        return topic;
    }

    private void bind(final PreparedStatement statement, final Topic topic) throws SQLException {
        statement.setString(1, topic.getName());
        statement.setString(2, topic.getDescription());
        statement.setInt(3, topic.getPhase());
        statement.setString(4, topic.getDifficulty().name());
        statement.setString(5, topic.getStatus().name());
    }

    @Override
    public Optional<Topic> findById(final Integer id) {
        return queryOne("SELECT * FROM topics WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    public List<Topic> findAll() {
        return query("SELECT * FROM topics ORDER BY phase, id", statement -> { });
    }

    @Override
    public List<Topic> findByPhase(final int phase) {
        return query("SELECT * FROM topics WHERE phase = ? ORDER BY id", statement -> statement.setInt(1, phase));
    }

    @Override
    public List<Topic> findByStatus(final TopicStatus status) {
        return query("SELECT * FROM topics WHERE status = ? ORDER BY phase, id",
                statement -> statement.setString(1, status.name()));
    }

    @Override
    public boolean deleteById(final Integer id) {
        return delete("DELETE FROM topics WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    protected Topic mapRow(final ResultSet resultSet) throws SQLException {
        final Topic topic = new Topic();
        topic.setId(resultSet.getInt("id"));
        topic.setName(resultSet.getString("name"));
        topic.setDescription(resultSet.getString("description"));
        topic.setPhase(resultSet.getInt("phase"));
        topic.setDifficulty(Difficulty.valueOf(resultSet.getString("difficulty")));
        topic.setStatus(TopicStatus.valueOf(resultSet.getString("status")));
        return topic;
    }
}
