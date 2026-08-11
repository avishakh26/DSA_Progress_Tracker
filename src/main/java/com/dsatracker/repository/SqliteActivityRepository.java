package com.dsatracker.repository;

import com.dsatracker.exception.EntityNotFoundException;
import com.dsatracker.model.Activity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class SqliteActivityRepository extends AbstractSqliteRepository<Activity> implements ActivityRepository {

    @Override
    public Activity save(final Activity activity) {
        return activity.getId() == null ? insert(activity) : update(activity);
    }

    private Activity insert(final Activity activity) {
        final int id = executeInsert(
                "INSERT INTO activity (activity_date, problems_solved) VALUES (?, ?)",
                statement -> bind(statement, activity));
        activity.setId(id);
        return activity;
    }

    private Activity update(final Activity activity) {
        final int rows = executeUpdate(
                "UPDATE activity SET activity_date = ?, problems_solved = ? WHERE id = ?",
                statement -> {
                    bind(statement, activity);
                    statement.setInt(3, activity.getId());
                });
        if (rows == 0) {
            throw new EntityNotFoundException("No activity row found with id " + activity.getId());
        }
        return activity;
    }

    private void bind(final PreparedStatement statement, final Activity activity) throws SQLException {
        statement.setString(1, activity.getActivityDate().toString());
        statement.setInt(2, activity.getProblemsSolved());
    }

    @Override
    public Optional<Activity> findById(final Integer id) {
        return queryOne("SELECT * FROM activity WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    public List<Activity> findAll() {
        return query("SELECT * FROM activity ORDER BY activity_date DESC", statement -> { });
    }

    @Override
    public Optional<Activity> findByDate(final LocalDate date) {
        return queryOne("SELECT * FROM activity WHERE activity_date = ?",
                statement -> statement.setString(1, date.toString()));
    }

    @Override
    public List<Activity> findBetween(final LocalDate start, final LocalDate end) {
        return query("SELECT * FROM activity WHERE activity_date BETWEEN ? AND ? ORDER BY activity_date ASC",
                statement -> {
                    statement.setString(1, start.toString());
                    statement.setString(2, end.toString());
                });
    }

    @Override
    public List<Activity> findAllOrderedByDate() {
        return query("SELECT * FROM activity ORDER BY activity_date ASC", statement -> { });
    }

    @Override
    public boolean deleteById(final Integer id) {
        return delete("DELETE FROM activity WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    protected Activity mapRow(final ResultSet resultSet) throws SQLException {
        final Activity activity = new Activity();
        activity.setId(resultSet.getInt("id"));
        activity.setActivityDate(LocalDate.parse(resultSet.getString("activity_date")));
        activity.setProblemsSolved(resultSet.getInt("problems_solved"));
        return activity;
    }
}
