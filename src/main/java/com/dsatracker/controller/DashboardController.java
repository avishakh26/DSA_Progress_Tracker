package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.model.DiaryEntry;
import com.dsatracker.model.Profile;
import com.dsatracker.service.DashboardService;
import com.dsatracker.service.DashboardStats;
import com.dsatracker.service.DiaryService;
import com.dsatracker.service.GoalProgress;
import com.dsatracker.service.ProfileService;
import com.dsatracker.util.AlertHelper;
import com.dsatracker.view.DiaryEntryCard;
import com.dsatracker.view.ProgressCard;
import com.dsatracker.view.StatCard;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * The app's home screen: the user's own profile front and center, their progress at a glance,
 * and a peek at their most recent diary entries - the things that make this feel like *their*
 * tracker rather than a generic one, instead of being buried a click away in Settings.
 */
public final class DashboardController implements Refreshable {

    private static final double AVATAR_RADIUS = 48;
    private static final int DIARY_PREVIEW_COUNT = 3;

    private final DashboardService dashboardService;
    private final ProfileService profileService;
    private final DiaryService diaryService;
    private final ThemeManager themeManager;
    private final Runnable openDiary;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private StackPane avatarContainer;

    @FXML
    private TextField displayNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea bioArea;

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

    @FXML
    private FlowPane diaryPreviewGrid;

    @FXML
    private Label diaryPreviewEmptyLabel;

    public DashboardController(final DashboardService dashboardService, final ProfileService profileService,
                                final DiaryService diaryService, final ThemeManager themeManager,
                                final Runnable openDiary) {
        this.dashboardService = dashboardService;
        this.profileService = profileService;
        this.diaryService = diaryService;
        this.themeManager = themeManager;
        this.openDiary = openDiary;
    }

    @FXML
    private void initialize() {
        // See RoadmapController for why this specific binding (to the ScrollPane's
        // actual viewport bounds, not an ancestor's widthProperty) is necessary.
        statGrid.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));
        diaryPreviewGrid.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));
        refresh();
    }

    @Override
    public void refresh() {
        loadProfile();
        final DashboardStats stats = dashboardService.getStats();
        renderStatCards(stats);
        renderDifficultyBreakdown(stats);
        renderGoalCard(stats.todayGoal());
        renderRecentActivity(stats.recentActivity());
        renderDiaryPreview();
    }

    // ----- Profile ----------------------------------------------------------------------------

    private void loadProfile() {
        final Profile profile = profileService.getProfile();
        displayNameField.setText(nullToEmpty(profile.getDisplayName()));
        emailField.setText(nullToEmpty(profile.getEmail()));
        bioArea.setText(nullToEmpty(profile.getBio()));
        renderAvatar(profile.getPhotoPath());
    }

    private void renderAvatar(final String photoPath) {
        final Circle circle = new Circle(AVATAR_RADIUS);
        avatarContainer.getChildren().clear();

        if (photoPath != null && Files.exists(Path.of(photoPath))) {
            circle.getStyleClass().add("profile-avatar");
            circle.setFill(new ImagePattern(new Image(Path.of(photoPath).toUri().toString())));
            avatarContainer.getChildren().add(circle);
        } else {
            circle.getStyleClass().add("profile-avatar-placeholder");
            final FontIcon placeholderIcon = new FontIcon("fas-user");
            placeholderIcon.getStyleClass().add("profile-avatar-icon");
            avatarContainer.getChildren().addAll(circle, placeholderIcon);
        }
    }

    @FXML
    private void onChangePhoto() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        final File chosen = fileChooser.showOpenDialog(avatarContainer.getScene().getWindow());
        if (chosen == null) {
            return;
        }
        new PhotoResizeDialog(themeManager, chosen).showAndWait().ifPresent(cropped -> {
            try {
                profileService.updatePhoto(cropped);
                renderAvatar(profileService.getProfile().getPhotoPath());
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Set Profile Picture", e.getMessage());
            }
        });
    }

    @FXML
    private void onRemovePhoto() {
        if (profileService.getProfile().getPhotoPath() == null) {
            return;
        }
        if (AlertHelper.confirm("Remove Profile Picture", "Remove your current profile picture?")) {
            profileService.removePhoto();
            renderAvatar(null);
        }
    }

    @FXML
    private void onSaveProfile() {
        try {
            profileService.updateProfile(displayNameField.getText(), emailField.getText(), bioArea.getText());
            AlertHelper.showInfo("Profile Saved", "Your profile has been updated.");
        } catch (final RuntimeException e) {
            AlertHelper.showError("Could Not Save Profile", e.getMessage());
        }
    }

    private static String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }

    // ----- Progress dashboard -------------------------------------------------------------------

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

    // ----- Diary preview ---------------------------------------------------------------------------

    private void renderDiaryPreview() {
        final List<DiaryEntry> entries = diaryService.getAllEntries().stream()
                .limit(DIARY_PREVIEW_COUNT)
                .toList();

        diaryPreviewEmptyLabel.setVisible(entries.isEmpty());
        diaryPreviewEmptyLabel.setManaged(entries.isEmpty());

        diaryPreviewGrid.getChildren().setAll(entries.stream()
                .map(entry -> new DiaryEntryCard(entry, () -> onEditDiaryEntry(entry), () -> onDeleteDiaryEntry(entry)))
                .toList());
    }

    @FXML
    private void onAddDiaryEntry() {
        new DiaryEntryFormDialog(themeManager, null).showAndWait().ifPresent(result -> {
            try {
                diaryService.createEntry(result.title(), result.content());
                renderDiaryPreview();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Add Entry", e.getMessage());
            }
        });
    }

    private void onEditDiaryEntry(final DiaryEntry entry) {
        new DiaryEntryFormDialog(themeManager, entry).showAndWait().ifPresent(result -> {
            try {
                diaryService.updateEntry(entry.getId(), result.title(), result.content());
                renderDiaryPreview();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Update Entry", e.getMessage());
            }
        });
    }

    private void onDeleteDiaryEntry(final DiaryEntry entry) {
        if (AlertHelper.confirm("Delete Entry", "Delete \"" + entry.getTitle() + "\"? This cannot be undone.")) {
            try {
                diaryService.deleteEntry(entry.getId());
                renderDiaryPreview();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Delete Entry", e.getMessage());
            }
        }
    }

    @FXML
    private void onViewAllDiary() {
        openDiary.run();
    }
}
