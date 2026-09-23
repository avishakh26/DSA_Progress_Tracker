package com.dsatracker;

import com.dsatracker.util.AppConstants;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

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
 * pure UI preference. Also owns an optional custom accent color that
 * overrides whichever theme is active - applied as an inline style on the
 * scene root, which JavaFX's CSS lookup resolves ahead of anything a theme
 * stylesheet declares for the same {@code -fx-accent} token, and layered
 * back on top whenever the theme itself is switched.
 */
public final class ThemeManager {

    public enum Theme {
        DARK(AppConstants.CSS_DARK_THEME, "#6C8CFF"),
        LIGHT(AppConstants.CSS_LIGHT_THEME, "#3B5BDB"),
        OCEAN(AppConstants.CSS_OCEAN_THEME, "#4A7FA7"),
        FOREST(AppConstants.CSS_FOREST_THEME, "#8EB69B"),
        ROSE(AppConstants.CSS_ROSE_THEME, "#FF69B4");

        private final String cssPath;
        private final String defaultAccentHex;

        Theme(final String cssPath, final String defaultAccentHex) {
            this.cssPath = cssPath;
            this.defaultAccentHex = defaultAccentHex;
        }

        /** This theme's own accent color, hand-kept in sync with its stylesheet's {@code .root}
         *  block - used to seed the color picker and as the fallback once a custom accent is cleared. */
        public String getDefaultAccentHex() {
            return defaultAccentHex;
        }
    }

    private static final Path SETTINGS_FILE = Path.of(AppConstants.DATA_DIRECTORY, "settings.properties");
    private static final String THEME_KEY = "theme";
    private static final String ACCENT_KEY = "accentColor";

    private final Scene scene;
    private Theme currentTheme;
    private String customAccentHex;

    public ThemeManager(final Scene scene) {
        this.scene = scene;
        this.currentTheme = loadSavedTheme();
        this.customAccentHex = loadSavedAccent();
        apply(currentTheme);
        applyAccentOverride(customAccentHex);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Classpath location of the stylesheet for the current theme. Dialogs (Alert, Dialog) get
     * their own Scene outside the main window's, so anything shown in one - a confirmation, an
     * add/edit form - has to attach this explicitly instead of inheriting it from {@link #scene}.
     */
    public String getStylesheetPath() {
        return currentTheme.cssPath;
    }

    public void setTheme(final Theme theme) {
        if (theme == currentTheme) {
            return;
        }
        this.currentTheme = theme;
        apply(theme);
        persist();
    }

    /** @return the custom accent if one is set, otherwise the current theme's own default. */
    public String getEffectiveAccentHex() {
        return customAccentHex != null ? customAccentHex : currentTheme.defaultAccentHex;
    }

    public boolean hasCustomAccent() {
        return customAccentHex != null;
    }

    public void setAccentColor(final Color color) {
        customAccentHex = toHex(color);
        applyAccentOverride(customAccentHex);
        persist();
    }

    /** Drops the override so the active theme's own accent shows again. */
    public void resetAccentColor() {
        customAccentHex = null;
        applyAccentOverride(null);
        persist();
    }

    private void apply(final Theme theme) {
        scene.getStylesheets().setAll(Objects.requireNonNull(
                getClass().getResource(theme.cssPath), "Missing theme stylesheet: " + theme.cssPath).toExternalForm());
    }

    private void applyAccentOverride(final String hex) {
        if (hex == null) {
            scene.getRoot().setStyle(null);
            return;
        }
        final String hoverHex = toHex(Color.web(hex).deriveColor(0, 1.0, 1.2, 1.0));
        scene.getRoot().setStyle("-fx-accent: " + hex + "; -fx-accent-hover: " + hoverHex + ";");
    }

    private static String toHex(final Color color) {
        return String.format("#%02X%02X%02X",
                Math.round(color.getRed() * 255),
                Math.round(color.getGreen() * 255),
                Math.round(color.getBlue() * 255));
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

    private String loadSavedAccent() {
        if (!Files.exists(SETTINGS_FILE)) {
            return null;
        }
        try (InputStream in = Files.newInputStream(SETTINGS_FILE)) {
            final Properties props = new Properties();
            props.load(in);
            return props.getProperty(ACCENT_KEY);
        } catch (final IOException e) {
            return null;
        }
    }

    private void persist() {
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            final Properties props = new Properties();
            props.setProperty(THEME_KEY, currentTheme.name());
            if (customAccentHex != null) {
                props.setProperty(ACCENT_KEY, customAccentHex);
            }
            try (OutputStream out = Files.newOutputStream(SETTINGS_FILE)) {
                props.store(out, "DSA Progress Tracker user settings");
            }
        } catch (final IOException e) {
            // Non-critical: the preference still applies for this session even if persisting it
            // fails (e.g. read-only disk) - not worth interrupting the user over a cosmetic setting.
        }
    }
}
