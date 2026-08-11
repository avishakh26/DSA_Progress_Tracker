package com.dsatracker.repository;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.Goal;
import com.dsatracker.model.enums.GoalType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class SqliteGoalRepository extends AbstractSqliteRepository<Goal> implements GoalRepository {

    @Override
    public Goal save(final Goal goal) {
        return goal.getId() == null ? insert(goal) : update(goal);
    }

    private Goal insert(final Goal goal) {
        final int id = executeInsert(
                "INSERT INTO goals (goal_type, target, start_date, end_date) VALUES (?, ?, ?, ?)",
                statement -> bind(statement, goal));
        goal.setId(id);
        return goal;
    }

    private Goal update(final Goal goal) {
        final int rows = executeUpdate(
                "UPDATE goals SET goal_type = ?, target = ?, start_date = ?, end_date = ? WHERE id = ?",
                statement -> {
                    bind(statement, goal);
                    statement.setInt(5, goal.getId());
                });
        if (rows == 0) {
            throw new EntityNotFoundException("No goal found with id " + goal.getId());
        }
        return goal;
    }

    private void bind(final PreparedStatement statement, final Goal goal) throws SQLException {
        statement.setString(1, goal.getGoalType().name());
        statement.setInt(2, goal.getTarget());
        statement.setString(3, goal.getStartDate().toString());
        statement.setString(4, goal.getEndDate() == null ? null : goal.getEndDate().toString());
    }

    @Override
    public Optional<Goal> findById(final Integer id) {
        return queryOne("SELECT * FROM goals WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    public List<Goal> findAll() {
        return query("SELECT * FROM goals ORDER BY start_date DESC", statement -> { });
    }

    @Override
    public List<Goal> findByType(final GoalType type) {
        return query("SELECT * FROM goals WHERE goal_type = ? ORDER BY start_date DESC",
                statement -> statement.setString(1, type.name()));
    }

    @Override
    public Optional<Goal> findCurrent(final GoalType type, final LocalDate onDate) {
        return queryOne(
                "SELECT * FROM goals WHERE goal_type = ? AND start_date <= ? AND (end_date IS NULL OR end_date >= ?) "
                        + "ORDER BY start_date DESC",
                statement -> {
                    statement.setString(1, type.name());
                    statement.setString(2, onDate.toString());
                    statement.setString(3, onDate.toString());
                });
    }

    @Override
    public boolean deleteById(final Integer id) {
        return delete("DELETE FROM goals WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    protected Goal mapRow(final ResultSet resultSet) throws SQLException {
        final Goal goal = new Goal();
        goal.setId(resultSet.getInt("id"));
        goal.setGoalType(GoalType.valueOf(resultSet.getString("goal_type")));
        goal.setTarget(resultSet.getInt("target"));
        goal.setStartDate(LocalDate.parse(resultSet.getString("start_date")));
        final String endDate = resultSet.getString("end_date");
        goal.setEndDate(endDate == null ? null : LocalDate.parse(endDate));
        return goal;
    }
}
