package com.dsatracker.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Central place for showing user-facing dialogs. Keeps controllers and the
 * app bootstrap from constructing {@link Alert} instances (title/header/
 * content boilerplate) by hand, and ensures errors are always surfaced as a
 * readable message rather than a raw stack trace.
 */
public final class AlertHelper {

    private AlertHelper() {
        throw new AssertionError("AlertHelper is a static utility class and cannot be instantiated.");
    }

    public static void showError(final String title, final String message) {
        show(AlertType.ERROR, title, message);
    }

    public static void showInfo(final String title, final String message) {
        show(AlertType.INFORMATION, title, message);
    }

    public static void showWarning(final String title, final String message) {
        show(AlertType.WARNING, title, message);
    }

    /** @return true if the user confirmed (clicked OK), false otherwise (Cancel or dismissed) */
    public static boolean confirm(final String title, final String message) {
        final Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        final Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void show(final AlertType type, final String title, final String message) {
        final Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
