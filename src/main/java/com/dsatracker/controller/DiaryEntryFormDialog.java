package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.exception.ValidationException;
import com.dsatracker.model.DiaryEntry;
import com.dsatracker.util.Validator;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Modal add/edit form for a single diary entry. Pass an existing
 * {@link DiaryEntry} to edit it, or {@code null} to write a new one - the
 * entry date itself is never editable here, it is stamped once on creation.
 */
final class DiaryEntryFormDialog extends Dialog<DiaryEntryFormDialog.Result> {

    record Result(String title, String content) {
    }

    private final TextField titleField = new TextField();
    private final TextArea contentArea = new TextArea();
    private final Label errorLabel = new Label();

    DiaryEntryFormDialog(final ThemeManager themeManager, final DiaryEntry existing) {
        setTitle(existing == null ? "Write Diary Entry" : "Edit Diary Entry");
        getDialogPane().getStylesheets().add(
                getClass().getResource(themeManager.getStylesheetPath()).toExternalForm());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        titleField.setPromptText("e.g. Today I finally cracked binary search");
        contentArea.setPromptText("Dear diary...");
        contentArea.setPrefRowCount(10);

        if (existing != null) {
            titleField.setText(existing.getTitle());
            contentArea.setText(existing.getContent());
        }

        errorLabel.getStyleClass().add("form-error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setWrapText(true);

        final GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        int row = 0;
        grid.addRow(row++, new Label("Title"), titleField);
        grid.addRow(row++, new Label("Entry"), contentArea);
        grid.add(errorLabel, 1, row);

        getDialogPane().setContent(grid);
        getDialogPane().getStyleClass().add("form-dialog");

        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Validator.requireNonBlank(titleField.getText(), "Title");
            } catch (final ValidationException e) {
                errorLabel.setText(e.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
            }
        });

        setResultConverter(buttonType -> buttonType == ButtonType.OK
                ? new Result(titleField.getText(), contentArea.getText())
                : null);
    }
}
