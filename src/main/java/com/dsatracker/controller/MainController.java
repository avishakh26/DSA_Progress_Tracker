package com.dsatracker.controller;

import com.dsatracker.AppContext;
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

    public MainController(final AppContext appContext) {
        this.appContext = appContext;
    }

    @FXML
    private void initialize() {
        for (final NavItem item : NavItem.values()) {
            final Button button = createNavButton(item);
            navButtons.put(item, button);
            navContainer.getChildren().add(button);
        }
        navigateTo(NavItem.DASHBOARD);
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
