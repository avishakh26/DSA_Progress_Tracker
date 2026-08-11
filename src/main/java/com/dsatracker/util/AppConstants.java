package com.dsatracker.util;

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
    /** Folder (relative to the working directory) holding the SQLite file. */
    public static final String DATA_DIRECTORY = "data";
    public static final String DATABASE_FILE = "dsa_tracker.db";
    public static final String JDBC_URL_PREFIX = "jdbc:sqlite:";

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
    public static final String CSS_ROSE_THEME = "/com/dsatracker/css/rose-theme.css";
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
