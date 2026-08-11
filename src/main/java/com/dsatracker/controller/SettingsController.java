package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.ThemeManager.Theme;
import com.dsatracker.service.SettingsService;
import com.dsatracker.util.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
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

    private final SettingsService settingsService;
    private final ThemeManager themeManager;
    private final ToggleGroup themeGroup = new ToggleGroup();

    @FXML
    private HBox themeRow;

    public SettingsController(final SettingsService settingsService, final ThemeManager themeManager) {
        this.settingsService = settingsService;
        this.themeManager = themeManager;
    }

    @FXML
    private void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        themeGroup.getToggles().clear();
        themeRow.getChildren().setAll(Arrays.stream(Theme.values()).map(this::buildThemeOption).toList());
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
