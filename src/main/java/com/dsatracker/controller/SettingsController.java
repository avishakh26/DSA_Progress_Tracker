package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.ThemeManager.Theme;
import com.dsatracker.model.DiaryEntry;
import com.dsatracker.model.Profile;
import com.dsatracker.service.DashboardService;
import com.dsatracker.service.DashboardStats;
import com.dsatracker.service.DiaryService;
import com.dsatracker.service.ProfileService;
import com.dsatracker.service.SettingsService;
import com.dsatracker.util.AlertHelper;
import com.dsatracker.view.DiaryEntryCard;
import com.dsatracker.view.StatCard;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SettingsController implements Refreshable {

    /** Small literal preview swatches per theme - not sourced from the stylesheets themselves
     *  (there's no clean way to introspect an unloaded CSS file's token values from Java), so
     *  these are kept in sync with each theme's .root block by hand: background, surface,
     *  accent, primary text, in that order. */
    private static final Map<Theme, List<String>> THEME_PREVIEW_COLORS = new EnumMap<>(Map.of(
            Theme.DARK, List.of("#0F1117", "#171A23", "#6C8CFF", "#E6E9F0"),
            Theme.LIGHT, List.of("#F5F6FA", "#FFFFFF", "#3B5BDB", "#1B1F2A"),
            Theme.OCEAN, List.of("#0A1931", "#1A3D63", "#4A7FA7", "#F6FAFD"),
            Theme.FOREST, List.of("#051F20", "#0B2B26", "#8EB69B", "#DAF1DE"),
            Theme.ROSE, List.of("#800021", "#881144", "#FF69B4", "#FDEAF1")
    ));

    private static final Map<Theme, String> THEME_LABELS = new EnumMap<>(Map.of(
            Theme.DARK, "Dark",
            Theme.LIGHT, "Light",
            Theme.OCEAN, "Ocean",
            Theme.FOREST, "Forest",
            Theme.ROSE, "Rose"
    ));

    private static final double AVATAR_RADIUS = 48;

    private final SettingsService settingsService;
    private final ThemeManager themeManager;
    private final ProfileService profileService;
    private final DiaryService diaryService;
    private final DashboardService dashboardService;
    private final ToggleGroup themeGroup = new ToggleGroup();

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private HBox themeRow;

    @FXML
    private StackPane avatarContainer;

    @FXML
    private TextField displayNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea bioArea;

    @FXML
    private FlowPane progressStatGrid;

    @FXML
    private FlowPane diaryGrid;

    @FXML
    private Label diaryEmptyLabel;

    public SettingsController(final SettingsService settingsService, final ThemeManager themeManager,
                               final ProfileService profileService, final DiaryService diaryService,
                               final DashboardService dashboardService) {
        this.settingsService = settingsService;
        this.themeManager = themeManager;
        this.profileService = profileService;
        this.diaryService = diaryService;
        this.dashboardService = dashboardService;
    }

    @FXML
    private void initialize() {
        // See RoadmapController for why this specific binding (to the ScrollPane's actual
        // viewport bounds, not an ancestor's widthProperty) is necessary.
        progressStatGrid.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));
        diaryGrid.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));
        refresh();
    }

    @Override
    public void refresh() {
        themeGroup.getToggles().clear();
        themeRow.getChildren().setAll(Arrays.stream(Theme.values()).map(this::buildThemeOption).toList());

        loadProfile();
        renderProgressStats();
        renderDiaryEntries();
    }

    private ToggleButton buildThemeOption(final Theme theme) {
        final HBox swatches = new HBox(4);
        for (final String hex : THEME_PREVIEW_COLORS.get(theme)) {
            final Region swatch = new Region();
            swatch.getStyleClass().add("theme-option-swatch");
            swatch.setStyle("-fx-background-color: " + hex + ";");
            swatch.setPrefSize(16, 16);
            swatch.setMinSize(16, 16);
            swatches.getChildren().add(swatch);
        }

        final Label nameLabel = new Label(THEME_LABELS.get(theme));
        nameLabel.getStyleClass().add("theme-option-label");

        final ToggleButton option = new ToggleButton();
        option.setGraphic(new VBox(10, swatches, nameLabel));
        option.getStyleClass().add("theme-option");
        option.setToggleGroup(themeGroup);
        option.setSelected(theme == themeManager.getCurrentTheme());
        option.setOnAction(event -> {
            if (option.isSelected()) {
                themeManager.setTheme(theme);
            } else {
                // Keep exactly one theme selected at all times - clicking the already-active
                // swatch would otherwise deselect it and leave the picker showing nothing chosen.
                option.setSelected(true);
            }
        });
        return option;
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

    private void renderProgressStats() {
        final DashboardStats stats = dashboardService.getStats();
        progressStatGrid.getChildren().setAll(
                new StatCard("fas-layer-group", "Total Topics", String.valueOf(stats.totalTopics())),
                new StatCard("fas-check-circle", "Completed Topics", String.valueOf(stats.completedTopics())),
                new StatCard("fas-check-double", "Solved Problems", String.valueOf(stats.solvedProblems())),
                new StatCard("fas-percentage", "Overall Progress", formatPercent(stats.overallProgressPercent())),
                new StatCard("fas-fire", "Current Streak", stats.currentStreak() + " days"),
                new StatCard("fas-trophy", "Longest Streak", stats.longestStreak() + " days")
        );
    }

    private String formatPercent(final double percent) {
        return String.format(Locale.US, "%.0f%%", percent);
    }

    // ----- Diary ---------------------------------------------------------------------------------

    private void renderDiaryEntries() {
        final List<DiaryEntry> entries = diaryService.getAllEntries();

        diaryEmptyLabel.setVisible(entries.isEmpty());
        diaryEmptyLabel.setManaged(entries.isEmpty());

        diaryGrid.getChildren().setAll(entries.stream()
                .map(entry -> new DiaryEntryCard(entry, () -> onEditDiaryEntry(entry), () -> onDeleteDiaryEntry(entry)))
                .toList());
    }

    @FXML
    private void onAddDiaryEntry() {
        new DiaryEntryFormDialog(themeManager, null).showAndWait().ifPresent(result -> {
            try {
                diaryService.createEntry(result.title(), result.content());
                renderDiaryEntries();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Add Entry", e.getMessage());
            }
        });
    }

    private void onEditDiaryEntry(final DiaryEntry entry) {
        new DiaryEntryFormDialog(themeManager, entry).showAndWait().ifPresent(result -> {
            try {
                diaryService.updateEntry(entry.getId(), result.title(), result.content());
                renderDiaryEntries();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Update Entry", e.getMessage());
            }
        });
    }

    private void onDeleteDiaryEntry(final DiaryEntry entry) {
        if (AlertHelper.confirm("Delete Entry", "Delete \"" + entry.getTitle() + "\"? This cannot be undone.")) {
            try {
                diaryService.deleteEntry(entry.getId());
                renderDiaryEntries();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Delete Entry", e.getMessage());
            }
        }
    }

    // ----- Data management -----------------------------------------------------------------------

    @FXML
    private void onResetProgress() {
        final boolean confirmed = AlertHelper.confirm("Reset Progress",
                "This sets every topic and problem back to Not Started and clears your activity "
                        + "history. Your topics, problems and notes themselves are kept. Continue?");
        if (!confirmed) {
            return;
        }
        settingsService.resetProgress();
        AlertHelper.showInfo("Progress Reset", "All progress has been reset.");
    }

    @FXML
    private void onRestoreSampleData() {
        final boolean confirmed = AlertHelper.confirm("Restore Sample Data",
                "This permanently deletes everything in the database and replaces it with the "
                        + "original sample roadmap and problems. Continue?");
        if (!confirmed) {
            return;
        }
        settingsService.restoreSampleData();
        AlertHelper.showInfo("Sample Data Restored", "The database has been reset to the sample roadmap.");
    }

    @FXML
    private void onClearAllData() {
        final boolean confirmed = AlertHelper.confirm("Clear All Data",
                "This permanently deletes every topic, problem, note, goal and activity record. "
                        + "This cannot be undone. Continue?");
        if (!confirmed) {
            return;
        }
        settingsService.clearAllData();
        AlertHelper.showInfo("Data Cleared", "All data has been removed.");
    }
}
