package com.dsatracker.model;

import com.dsatracker.model.enums.GoalType;

import java.time.LocalDate;

/** A daily/weekly/monthly problem-solving target over an optional date range. */
public class Goal extends BaseEntity {

    private GoalType goalType;
    private int target;
    private LocalDate startDate;
    private LocalDate endDate;

    public Goal() {
    }

    public Goal(final GoalType goalType, final int target, final LocalDate startDate) {
        this.goalType = goalType;
        this.target = target;
        this.startDate = startDate;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(final GoalType goalType) {
        this.goalType = goalType;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(final int target) {
        this.target = target;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }
}
