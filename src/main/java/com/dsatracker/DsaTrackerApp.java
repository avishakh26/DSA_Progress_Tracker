package com.dsatracker;

import com.dsatracker.database.DatabaseManager;
import com.dsatracker.exception.DatabaseInitializationException;
import com.dsatracker.util.AlertHelper;
import com.dsatracker.util.AppConstants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * JavaFX entry point of the DSA Progress Tracker.
 *
 * <p>Responsibilities are deliberately narrow: bootstrap the JavaFX runtime,
 * load the root FXML view, attach the stylesheet and show the primary stage.
 * All domain logic lives in the service layer, all persistence in the
 * repository layer.</p>
 */
public class DsaTrackerApp extends Application {

    @Override
    public void start(final Stage primaryStage) throws IOException {
        try {
            DatabaseManager.getInstance().initialize();
        } catch (final DatabaseInitializationException e) {
            AlertHelper.showError("Database Error",
                    "The local database could not be opened, so the application cannot start.\n\n" + e.getMessage());
            Platform.exit();
            return;
        }

        // The Scene is created with a placeholder root so ThemeManager has a live Scene to
        // apply the saved theme to before AppContext (and therefore SettingsController) exists;
        // the real root swaps in once FXML is loaded, a few lines down.
        final Scene scene = new Scene(new StackPane(), AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        final ThemeManager themeManager = new ThemeManager(scene);

        final AppContext appContext = new AppContext(themeManager);

        final FXMLLoader loader = new FXMLLoader(resource(AppConstants.FXML_MAIN));
        loader.setControllerFactory(appContext::createController);
        final Parent root = loader.load();
        scene.setRoot(root);

        primaryStage.setTitle(AppConstants.APP_TITLE);
        primaryStage.setMinWidth(AppConstants.MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(AppConstants.MIN_WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Resolves a classpath resource, failing fast with a readable message
     * instead of a {@code NullPointerException} deep inside the FXML loader.
     *
     * @param path absolute classpath location of the resource
     * @return the resolved URL, never {@code null}
     */
    private URL resource(final String path) {
        return Objects.requireNonNull(
                DsaTrackerApp.class.getResource(path),
                "Missing application resource on the classpath: " + path);
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().close();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
