package com.dsatracker.repository;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.DiaryEntry;
import com.dsatracker.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class SqliteDiaryEntryRepository extends AbstractSqliteRepository<DiaryEntry> implements DiaryEntryRepository {

    @Override
    public DiaryEntry save(final DiaryEntry entry) {
        return entry.getId() == null ? insert(entry) : update(entry);
    }

    private DiaryEntry insert(final DiaryEntry entry) {
        final int id = executeInsert(
                "INSERT INTO diary_entries (title, content, entry_date, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                statement -> {
                    statement.setString(1, entry.getTitle());
                    statement.setString(2, entry.getContent());
                    statement.setString(3, entry.getEntryDate().toString());
                    statement.setString(4, entry.getCreatedAt().format(DateTimeUtil.SQLITE_DATETIME));
                    statement.setString(5, entry.getUpdatedAt().format(DateTimeUtil.SQLITE_DATETIME));
                });
        entry.setId(id);
        return entry;
    }

    private DiaryEntry update(final DiaryEntry entry) {
        final int rows = executeUpdate(
                "UPDATE diary_entries SET title = ?, content = ?, updated_at = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, entry.getTitle());
                    statement.setString(2, entry.getContent());
                    statement.setString(3, entry.getUpdatedAt().format(DateTimeUtil.SQLITE_DATETIME));
                    statement.setInt(4, entry.getId());
                });
        if (rows == 0) {
            throw new EntityNotFoundException("No diary entry found with id " + entry.getId());
        }
        return entry;
    }

    @Override
    public Optional<DiaryEntry> findById(final Integer id) {
        return queryOne("SELECT * FROM diary_entries WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    public List<DiaryEntry> findAll() {
        return query("SELECT * FROM diary_entries ORDER BY entry_date DESC, created_at DESC", statement -> { });
    }

    @Override
    public boolean deleteById(final Integer id) {
        return delete("DELETE FROM diary_entries WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    protected DiaryEntry mapRow(final ResultSet resultSet) throws SQLException {
        final DiaryEntry entry = new DiaryEntry();
        entry.setId(resultSet.getInt("id"));
        entry.setTitle(resultSet.getString("title"));
        entry.setContent(resultSet.getString("content"));
        entry.setEntryDate(LocalDate.parse(resultSet.getString("entry_date")));
        entry.setCreatedAt(LocalDateTime.parse(resultSet.getString("created_at"), DateTimeUtil.SQLITE_DATETIME));
        entry.setUpdatedAt(LocalDateTime.parse(resultSet.getString("updated_at"), DateTimeUtil.SQLITE_DATETIME));
        return entry;
    }
}
