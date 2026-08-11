package com.dsatracker.repository;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.Note;
import com.dsatracker.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class SqliteNoteRepository extends AbstractSqliteRepository<Note> implements NoteRepository {

    @Override
    public Note save(final Note note) {
        return note.getId() == null ? insert(note) : update(note);
    }

    private Note insert(final Note note) {
        final int id = executeInsert(
                "INSERT INTO notes (title, topic_id, content, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                statement -> {
                    statement.setString(1, note.getTitle());
                    statement.setObject(2, note.getTopicId());
                    statement.setString(3, note.getContent());
                    statement.setString(4, note.getCreatedAt().format(DateTimeUtil.SQLITE_DATETIME));
                    statement.setString(5, note.getUpdatedAt().format(DateTimeUtil.SQLITE_DATETIME));
                });
        note.setId(id);
        return note;
    }

    private Note update(final Note note) {
        final int rows = executeUpdate(
                "UPDATE notes SET title = ?, topic_id = ?, content = ?, updated_at = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, note.getTitle());
                    statement.setObject(2, note.getTopicId());
                    statement.setString(3, note.getContent());
                    statement.setString(4, note.getUpdatedAt().format(DateTimeUtil.SQLITE_DATETIME));
                    statement.setInt(5, note.getId());
                });
        if (rows == 0) {
            throw new EntityNotFoundException("No note found with id " + note.getId());
        }
        return note;
    }

    @Override
    public Optional<Note> findById(final Integer id) {
        return queryOne("SELECT * FROM notes WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    public List<Note> findAll() {
        return query("SELECT * FROM notes ORDER BY updated_at DESC", statement -> { });
    }

    @Override
    public List<Note> findByTopicId(final Integer topicId) {
        return query("SELECT * FROM notes WHERE topic_id = ? ORDER BY updated_at DESC",
                statement -> statement.setInt(1, topicId));
    }

    @Override
    public boolean deleteById(final Integer id) {
        return delete("DELETE FROM notes WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    protected Note mapRow(final ResultSet resultSet) throws SQLException {
        final Note note = new Note();
        note.setId(resultSet.getInt("id"));
        note.setTitle(resultSet.getString("title"));
        final int topicId = resultSet.getInt("topic_id");
        note.setTopicId(resultSet.wasNull() ? null : topicId);
        note.setContent(resultSet.getString("content"));
        note.setCreatedAt(LocalDateTime.parse(resultSet.getString("created_at"), DateTimeUtil.SQLITE_DATETIME));
        note.setUpdatedAt(LocalDateTime.parse(resultSet.getString("updated_at"), DateTimeUtil.SQLITE_DATETIME));
        return note;
    }
}
