package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.exception.ValidationException;
import com.dsatracker.model.enums.GoalType;
import com.dsatracker.service.GoalProgress;
import com.dsatracker.service.GoalService;
import com.dsatracker.util.AlertHelper;
import com.dsatracker.view.ProgressCard;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

import java.util.Optional;

public final class GoalsController implements Refreshable {

    private final GoalService goalService;
    private final ThemeManager themeManager;

    @FXML
    private VBox dailyGoalContainer;

    @FXML
    private VBox weeklyGoalContainer;

    @FXML
    private VBox monthlyGoalContainer;

    public GoalsController(final GoalService goalService, final ThemeManager themeManager) {
        this.goalService = goalService;
        this.themeManager = themeManager;
    }

    @FXML
    private void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        renderGoalSection(dailyGoalContainer, GoalType.DAILY, "Daily Goal");
        renderGoalSection(weeklyGoalContainer, GoalType.WEEKLY, "Weekly Goal");
        renderGoalSection(monthlyGoalContainer, GoalType.MONTHLY, "Monthly Goal");
    }

    private void renderGoalSection(final VBox container, final GoalType type, final String title) {
        final Optional<GoalProgress> progress = goalService.getTodayProgress(type);

        final ProgressCard card = new ProgressCard(title);
        final Integer currentTarget;
        if (progress.isPresent()) {
            final GoalProgress goalProgress = progress.get();
            final int target = goalProgress.goal().getTarget();
            final double fraction = target == 0 ? 0 : Math.min(1.0, goalProgress.actualCount() / (double) target);
            card.update(fraction, goalProgress.actualCount() + " / " + target + " solved");
            currentTarget = target;
        } else {
            card.update(0, "No " + title.toLowerCase() + " set yet");
            currentTarget = null;
        }

        final Button actionButton = new Button(currentTarget == null ? "Set Goal" : "Edit Target");
        actionButton.getStyleClass().add("primary-button");
        actionButton.setOnAction(event -> onSetGoal(type, title, currentTarget));

        container.getChildren().setAll(card, actionButton);
    }

    private void onSetGoal(final GoalType type, final String title, final Integer currentTarget) {
        final TextInputDialog dialog = new TextInputDialog(currentTarget == null ? "3" : String.valueOf(currentTarget));
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText("Target problems to solve:");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource(themeManager.getStylesheetPath()).toExternalForm());

        dialog.showAndWait().ifPresent(text -> {
            try {
                final int target = Integer.parseInt(text.trim());
                goalService.setGoal(type, target);
                refresh();
            } catch (final NumberFormatException e) {
                AlertHelper.showError("Invalid Target", "Please enter a whole number greater than zero.");
            } catch (final ValidationException e) {
                AlertHelper.showError("Invalid Target", e.getMessage());
            }
        });
    }
}
