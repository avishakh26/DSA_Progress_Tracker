package com.dsatracker;

import com.dsatracker.util.AppConstants;
import javafx.scene.Scene;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * Owns the live {@link Scene}'s active stylesheet and persists the chosen
 * theme in a small properties file under {@code data/}, so a Settings-page
 * toggle survives across launches without needing a database column for a
 * pure UI preference.
 */
public final class ThemeManager {

    public enum Theme {
        DARK, LIGHT
    }

    private static final Path SETTINGS_FILE = Path.of(AppConstants.DATA_DIRECTORY, "settings.properties");
    private static final String THEME_KEY = "theme";

    private final Scene scene;
    private Theme currentTheme;

    public ThemeManager(final Scene scene) {
        this.scene = scene;
        this.currentTheme = loadSavedTheme();
        apply(currentTheme);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setTheme(final Theme theme) {
        if (theme == currentTheme) {
            return;
        }
        this.currentTheme = theme;
        apply(theme);
        save(theme);
    }

    private void apply(final Theme theme) {
        final String cssPath = theme == Theme.DARK ? AppConstants.CSS_DARK_THEME : AppConstants.CSS_LIGHT_THEME;
        scene.getStylesheets().setAll(Objects.requireNonNull(
                getClass().getResource(cssPath), "Missing theme stylesheet: " + cssPath).toExternalForm());
    }

    private Theme loadSavedTheme() {
        if (!Files.exists(SETTINGS_FILE)) {
            return Theme.DARK;
        }
        try (InputStream in = Files.newInputStream(SETTINGS_FILE)) {
            final Properties props = new Properties();
            props.load(in);
            return Theme.valueOf(props.getProperty(THEME_KEY, Theme.DARK.name()));
        } catch (final IOException | IllegalArgumentException e) {
            return Theme.DARK;
        }
    }

    private void save(final Theme theme) {
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            final Properties props = new Properties();
            props.setProperty(THEME_KEY, theme.name());
            try (OutputStream out = Files.newOutputStream(SETTINGS_FILE)) {
                props.store(out, "DSA Progress Tracker user settings");
            }
        } catch (final IOException e) {
            // Non-critical: the theme still applies for this session even if persisting it fails
            // (e.g. read-only disk) - not worth interrupting the user over a cosmetic preference.
        }
    }
}
