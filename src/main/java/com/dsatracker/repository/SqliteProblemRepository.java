package com.dsatracker.repository;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.Problem;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
import com.dsatracker.model.enums.ProblemStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class SqliteProblemRepository extends AbstractSqliteRepository<Problem> implements ProblemRepository {

    @Override
    public Problem save(final Problem problem) {
        return problem.getId() == null ? insert(problem) : update(problem);
    }

    private Problem insert(final Problem problem) {
        final int id = executeInsert(
                "INSERT INTO problems (title, platform, url, topic_id, difficulty, status, notes, date_added, date_solved) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                statement -> bind(statement, problem));
        problem.setId(id);
        return problem;
    }

    private Problem update(final Problem problem) {
        final int rows = executeUpdate(
                "UPDATE problems SET title = ?, platform = ?, url = ?, topic_id = ?, difficulty = ?, status = ?, "
                        + "notes = ?, date_added = ?, date_solved = ? WHERE id = ?",
                statement -> {
                    bind(statement, problem);
                    statement.setInt(10, problem.getId());
                });
        if (rows == 0) {
            throw new EntityNotFoundException("No problem found with id " + problem.getId());
        }
        return problem;
    }

    private void bind(final PreparedStatement statement, final Problem problem) throws SQLException {
        statement.setString(1, problem.getTitle());
        statement.setString(2, problem.getPlatform().name());
        statement.setString(3, problem.getUrl());
        statement.setInt(4, problem.getTopicId());
        statement.setString(5, problem.getDifficulty().name());
        statement.setString(6, problem.getStatus().name());
        statement.setString(7, problem.getNotes());
        statement.setString(8, problem.getDateAdded().toString());
        statement.setString(9, problem.getDateSolved() == null ? null : problem.getDateSolved().toString());
    }

    @Override
    public Optional<Problem> findById(final Integer id) {
        return queryOne("SELECT * FROM problems WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    public List<Problem> findAll() {
        return query("SELECT * FROM problems ORDER BY date_added DESC", statement -> { });
    }

    @Override
    public List<Problem> findByTopicId(final Integer topicId) {
        return query("SELECT * FROM problems WHERE topic_id = ? ORDER BY date_added DESC",
                statement -> statement.setInt(1, topicId));
    }

    @Override
    public List<Problem> findByStatus(final ProblemStatus status) {
        return query("SELECT * FROM problems WHERE status = ? ORDER BY date_added DESC",
                statement -> statement.setString(1, status.name()));
    }

    @Override
    public List<Problem> findByDifficulty(final Difficulty difficulty) {
        return query("SELECT * FROM problems WHERE difficulty = ? ORDER BY date_added DESC",
                statement -> statement.setString(1, difficulty.name()));
    }

    @Override
    public List<Problem> searchByTitle(final String keyword) {
        return query("SELECT * FROM problems WHERE title LIKE ? ORDER BY date_added DESC",
                statement -> statement.setString(1, "%" + keyword + "%"));
    }

    @Override
    public boolean deleteById(final Integer id) {
        return delete("DELETE FROM problems WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    protected Problem mapRow(final ResultSet resultSet) throws SQLException {
        final Problem problem = new Problem();
        problem.setId(resultSet.getInt("id"));
        problem.setTitle(resultSet.getString("title"));
        problem.setPlatform(Platform.valueOf(resultSet.getString("platform")));
        problem.setUrl(resultSet.getString("url"));
        problem.setTopicId(resultSet.getInt("topic_id"));
        problem.setDifficulty(Difficulty.valueOf(resultSet.getString("difficulty")));
        problem.setStatus(ProblemStatus.valueOf(resultSet.getString("status")));
        problem.setNotes(resultSet.getString("notes"));
        problem.setDateAdded(LocalDate.parse(resultSet.getString("date_added")));
        final String dateSolved = resultSet.getString("date_solved");
        problem.setDateSolved(dateSolved == null ? null : LocalDate.parse(dateSolved));
        return problem;
    }
}
