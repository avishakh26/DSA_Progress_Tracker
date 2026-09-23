package com.dsatracker.controller;

import com.dsatracker.AppContext;
import com.dsatracker.DsaTrackerApp;
import com.dsatracker.util.UpdateChecker;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import com.dsatracker.util.AlertHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Shell controller for {@code MainView.fxml}: builds the sidebar from
 * {@link NavItem} and swaps the routed content pane. Each destination view
 * is lazily loaded (via the same {@link AppContext} controller factory
 * that built this controller) and cached on first visit, so switching
 * sections doesn't reload FXML - or lose in-progress form state - every time.
 */
public final class MainController {

    private final AppContext appContext;
    private final Map<NavItem, Parent> viewCache = new EnumMap<>(NavItem.class);
    private final Map<NavItem, Object> controllerCache = new EnumMap<>(NavItem.class);
    private final Map<NavItem, Button> navButtons = new EnumMap<>(NavItem.class);

    @FXML
    private VBox navContainer;

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox updateBanner;

    public MainController(final AppContext appContext) {
        this.appContext = appContext;
        // Registered before any routed view is loaded (below, in initialize()), so a controller
        // that needs to jump to another section - e.g. Dashboard's "View All" diary link - can
        // call back into the same navigation this sidebar uses instead of duplicating it.
        appContext.setNavigator(this::navigateTo);
    }

    @FXML
    private void initialize() {
        for (final NavItem item : NavItem.values()) {
            final Button button = createNavButton(item);
            navButtons.put(item, button);
            navContainer.getChildren().add(button);
        }
        navigateTo(NavItem.DASHBOARD);
        checkForUpdateInBackground();
    }

    /** Asks GitHub for a newer release without blocking startup; shows a dismissible banner if one exists. */
    private void checkForUpdateInBackground() {
        final Thread thread = new Thread(() -> UpdateChecker.checkForUpdate()
                .ifPresent(update -> Platform.runLater(() -> showUpdateBanner(update))), "update-check");
        thread.setDaemon(true);
        thread.start();
    }

    private void showUpdateBanner(final UpdateChecker.Update update) {
        final Label message = new Label("A new version (" + update.version() + ") is available.");
        message.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        final Button download = new Button("Download");
        download.setOnAction(event -> DsaTrackerApp.openInBrowser(update.pageUrl()));
        final Button dismiss = new Button("Later");
        dismiss.setOnAction(event -> {
            updateBanner.setVisible(false);
            updateBanner.setManaged(false);
        });
        final HBox bar = new HBox(10, message, spacer, download, dismiss);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setStyle("-fx-background-color: -fx-accent;");
        updateBanner.getChildren().setAll(bar);
        updateBanner.setVisible(true);
        updateBanner.setManaged(true);
    }

    private Button createNavButton(final NavItem item) {
        final FontIcon icon = new FontIcon(item.getIconLiteral());
        icon.getStyleClass().add("nav-icon");

        final Button button = new Button(item.getLabel(), icon);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> navigateTo(item));
        return button;
    }

    private void navigateTo(final NavItem item) {
        navButtons.values().forEach(button -> button.getStyleClass().remove("nav-button-active"));
        navButtons.get(item).getStyleClass().add("nav-button-active");

        final Parent view = viewCache.computeIfAbsent(item, this::loadView);
        if (controllerCache.get(item) instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
        contentArea.getChildren().setAll(view);
    }

    private Parent loadView(final NavItem item) {
        try {
            final FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource(item.getFxmlPath()), "Missing view resource: " + item.getFxmlPath()));
            loader.setControllerFactory(appContext::createController);
            final Parent root = loader.load();
            controllerCache.put(item, loader.getController());
            return root;
        } catch (final IOException e) {
            AlertHelper.showError("Navigation Error", "Could not open " + item.getLabel() + ".\n\n" + e.getMessage());
            return new StackPane();
        }
    }
}
