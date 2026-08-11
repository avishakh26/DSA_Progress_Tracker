package com.dsatracker.controller;

import com.dsatracker.service.DashboardService;
import com.dsatracker.service.DashboardStats;
import com.dsatracker.service.GoalProgress;
import com.dsatracker.view.ProgressCard;
import com.dsatracker.view.StatCard;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;

public final class DashboardController implements Refreshable {

    private final DashboardService dashboardService;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private FlowPane statGrid;

    @FXML
    private HBox difficultyRow;

    @FXML
    private VBox goalContainer;

    @FXML
    private VBox recentActivityList;

    @FXML
    private Label recentActivityEmptyLabel;

    public DashboardController(final DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @FXML
    private void initialize() {
        // See RoadmapController for why this specific binding (to the ScrollPane's
        // actual viewport bounds, not an ancestor's widthProperty) is necessary.
        statGrid.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));
        refresh();
    }

    @Override
    public void refresh() {
        final DashboardStats stats = dashboardService.getStats();
        renderStatCards(stats);
        renderDifficultyBreakdown(stats);
        renderGoalCard(stats.todayGoal());
        renderRecentActivity(stats.recentActivity());
    }

    private void renderStatCards(final DashboardStats stats) {
        statGrid.getChildren().setAll(
                new StatCard("fas-layer-group", "Total Topics", String.valueOf(stats.totalTopics())),
                new StatCard("fas-check-circle", "Completed Topics", String.valueOf(stats.completedTopics())),
                new StatCard("fas-list-ol", "Total Problems", String.valueOf(stats.totalProblems())),
                new StatCard("fas-check-double", "Solved", String.valueOf(stats.solvedProblems())),
                new StatCard("fas-hourglass-half", "Attempted", String.valueOf(stats.attemptedProblems())),
                new StatCard("fas-percentage", "Overall Progress", formatPercent(stats.overallProgressPercent())),
                new StatCard("fas-fire", "Current Streak", stats.currentStreak() + " days"),
                new StatCard("fas-trophy", "Longest Streak", stats.longestStreak() + " days")
        );
    }

    private void renderDifficultyBreakdown(final DashboardStats stats) {
        difficultyRow.getChildren().setAll(
                new StatCard("fas-circle", "Easy Solved", String.valueOf(stats.easySolved()), "stat-card-easy"),
                new StatCard("fas-circle", "Medium Solved", String.valueOf(stats.mediumSolved()), "stat-card-medium"),
                new StatCard("fas-circle", "Hard Solved", String.valueOf(stats.hardSolved()), "stat-card-hard")
        );
    }

    private void renderGoalCard(final GoalProgress goal) {
        final ProgressCard card = new ProgressCard("Today's Goal");
        if (goal == null) {
            card.update(0, "No daily goal set yet - set one in Goals.");
        } else {
            final int target = goal.goal().getTarget();
            final double fraction = target == 0 ? 0 : Math.min(1.0, goal.actualCount() / (double) target);
            card.update(fraction, goal.actualCount() + " / " + target + " problems solved today");
        }
        goalContainer.getChildren().setAll(card);
    }

    private void renderRecentActivity(final List<String> recentActivity) {
        recentActivityEmptyLabel.setManaged(recentActivity.isEmpty());
        recentActivityEmptyLabel.setVisible(recentActivity.isEmpty());

        recentActivityList.getChildren().clear();
        for (final String entry : recentActivity) {
            final Label label = new Label(entry);
            label.getStyleClass().add("activity-item");
            recentActivityList.getChildren().add(label);
        }
    }

    private String formatPercent(final double percent) {
        return String.format(Locale.US, "%.0f%%", percent);
    }
}
