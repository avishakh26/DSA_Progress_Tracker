package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Guards a destructive action behind two exact-text confirmations typed one after the other,
 * on top of the destructive-button styling alone - for actions with no undo, where a single
 * misclick on a plain Yes/No dialog isn't enough friction.
 */
final class TypedConfirmDialog extends Dialog<Boolean> {

    TypedConfirmDialog(final ThemeManager themeManager, final String title, final String warningText,
                        final String firstPhrase, final String secondPhrase, final String confirmButtonText) {
        setTitle(title);
        getDialogPane().getStylesheets().add(
                getClass().getResource(themeManager.getStylesheetPath()).toExternalForm());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final double contentWidth = 420;
        final double labelWidth = contentWidth - 32; // minus the VBox's own left+right padding below

        final Label warning = new Label(warningText);
        warning.getStyleClass().add("form-error-label");
        warning.setWrapText(true);
        warning.setMaxWidth(labelWidth);

        final Label firstLabel = new Label("Type the following exactly to continue:\n\"" + firstPhrase + "\"");
        firstLabel.setWrapText(true);
        firstLabel.setMaxWidth(labelWidth);
        final TextField firstField = new TextField();

        final Label secondLabel = new Label("Now type this to confirm you're absolutely sure:\n\"" + secondPhrase + "\"");
        secondLabel.setWrapText(true);
        secondLabel.setMaxWidth(labelWidth);
        secondLabel.setVisible(false);
        secondLabel.setManaged(false);
        final TextField secondField = new TextField();
        secondField.setVisible(false);
        secondField.setManaged(false);

        // The second phrase only appears once the first is typed correctly - each step must be
        // cleared in order rather than both boxes being available to fill in at once.
        firstField.textProperty().addListener((obs, oldValue, newValue) -> {
            final boolean unlocked = firstPhrase.equals(newValue);
            secondLabel.setVisible(unlocked);
            secondLabel.setManaged(unlocked);
            secondField.setVisible(unlocked);
            secondField.setManaged(unlocked);
            if (unlocked) {
                secondField.requestFocus();
            }
            resizeToFitContent();
        });

        final VBox content = new VBox(10, warning, firstLabel, firstField, secondLabel, secondField);
        content.setPadding(new Insets(16));
        content.setPrefWidth(contentWidth);

        getDialogPane().setContent(content);
        getDialogPane().getStyleClass().add("form-dialog");

        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText(confirmButtonText);
        okButton.getStyleClass().add("danger-button");
        okButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !firstPhrase.equals(firstField.getText()) || !secondPhrase.equals(secondField.getText()),
                firstField.textProperty(), secondField.textProperty()));

        setResultConverter(buttonType -> buttonType == ButtonType.OK);

        // A Dialog's window is sized from its content's preferred size before the stylesheet's
        // fonts are applied, so wrapped-label heights (and later, the second field appearing)
        // are measured wrong and the button bar ends up clipped. Re-sizing to the scene once
        // it's actually shown - and again whenever the revealed content changes - fixes both.
        setOnShown(event -> resizeToFitContent());
    }

    private void resizeToFitContent() {
        if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
            getDialogPane().getScene().getWindow().sizeToScene();
        }
    }
}
