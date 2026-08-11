package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.ThemeManager.Theme;
import com.dsatracker.service.SettingsService;
import com.dsatracker.util.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public final class SettingsController implements Refreshable {

    private final SettingsService settingsService;
    private final ThemeManager themeManager;

    @FXML
    private Button darkThemeButton;

    @FXML
    private Button lightThemeButton;

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
        final boolean isDark = themeManager.getCurrentTheme() == Theme.DARK;
        darkThemeButton.getStyleClass().setAll(isDark ? "primary-button" : "button");
        lightThemeButton.getStyleClass().setAll(isDark ? "button" : "primary-button");
    }

    @FXML
    private void onSelectDarkTheme() {
        themeManager.setTheme(Theme.DARK);
        refresh();
    }

    @FXML
    private void onSelectLightTheme() {
        themeManager.setTheme(Theme.LIGHT);
        refresh();
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
