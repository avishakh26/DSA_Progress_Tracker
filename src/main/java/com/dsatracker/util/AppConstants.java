package com.dsatracker.util;

import java.io.File;

/**
 * Central, immutable configuration values for the application.
 *
 * <p>Keeping every magic string and number here means no controller, service or
 * repository has to hard-code a file path or window size. The class is
 * {@code final} with a private constructor - it is a pure constant holder and
 * must never be instantiated or subclassed.</p>
 */
public final class AppConstants {

    // ----- Application identity -------------------------------------------
    public static final String APP_TITLE = "DSA Progress Tracker";
    public static final String APP_VERSION = "1.0.0";

    // ----- Database --------------------------------------------------------
    /** Folder holding the SQLite file, the profile photo and settings.properties - anchored to
     *  the user's home directory rather than the process's working directory, so it resolves to
     *  the same place no matter how the app is launched (IDE run config, a double-clicked jar,
     *  a desktop shortcut with its own "Start in" folder, ...). A working-directory-relative
     *  folder only ever finds the same data when every launch happens to start from the exact
     *  same directory - otherwise each differently-launched run silently gets its own empty copy,
     *  which is most visible with the profile photo since (unlike topics/problems) there's no
     *  seed data to mask a "new" database looking like the old one. {@link
     *  com.dsatracker.database.DatabaseManager} migrates an old working-directory-relative
     *  {@code ./data} folder here once, the first time it finds this location empty. */
    public static final String DATA_DIRECTORY =
            System.getProperty("user.home") + File.separator + ".dsa-tracker";
    public static final String DATABASE_FILE = "dsa_tracker.db";
    public static final String JDBC_URL_PREFIX = "jdbc:sqlite:";

    /** Subfolder of {@link #DATA_DIRECTORY} holding the copied profile picture file, if any. */
    public static final String PROFILE_PHOTO_DIRECTORY = "profile";

    /** Classpath location of the idempotent DDL script, run on every startup. */
    public static final String SQL_SCHEMA = "/com/dsatracker/sql/schema.sql";
    /** Classpath location of the first-launch sample data script. */
    public static final String SQL_SEED = "/com/dsatracker/sql/seed.sql";

    // ----- Views (classpath locations under src/main/resources) ------------
    public static final String FXML_MAIN = "/com/dsatracker/fxml/MainView.fxml";
    public static final String CSS_DARK_THEME = "/com/dsatracker/css/dark-theme.css";
    public static final String CSS_LIGHT_THEME = "/com/dsatracker/css/light-theme.css";
    public static final String CSS_OCEAN_THEME = "/com/dsatracker/css/ocean-theme.css";
    public static final String CSS_FOREST_THEME = "/com/dsatracker/css/forest-theme.css";
    /** Window/taskbar icon, provided at several sizes so the OS can pick the sharpest fit. */
    public static final int[] APP_ICON_SIZES = {16, 24, 32, 48, 64, 128, 256};
    public static final String APP_ICON_PATH_TEMPLATE = "/com/dsatracker/images/app-icon-%d.png";

    // ----- Window geometry -------------------------------------------------
    public static final double WINDOW_WIDTH = 1280;
    public static final double WINDOW_HEIGHT = 800;
    public static final double MIN_WINDOW_WIDTH = 1024;
    public static final double MIN_WINDOW_HEIGHT = 640;

    private AppConstants() {
        throw new AssertionError("AppConstants is a constant holder and cannot be instantiated.");
    }
}
