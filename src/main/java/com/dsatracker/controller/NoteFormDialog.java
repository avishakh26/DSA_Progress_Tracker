package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.exception.ValidationException;
import com.dsatracker.model.Note;
import com.dsatracker.model.Topic;
import com.dsatracker.util.Validator;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Modal add/edit form for a single note. Pass an existing {@link Note} to
 * edit it, or {@code null} to create a new one. Topic is optional (a note
 * can be general, not tied to any topic) - the first combo item is an
 * explicit "No Topic (General)" sentinel backed by a {@code null} value.
 */
final class NoteFormDialog extends Dialog<NoteFormDialog.Result> {

    record Result(String title, Integer topicId, String content) {
    }

    private final TextField titleField = new TextField();
    private final ComboBox<Topic> topicCombo = new ComboBox<>();
    private final TextArea contentArea = new TextArea();
    private final Label errorLabel = new Label();

    NoteFormDialog(final ThemeManager themeManager, final List<Topic> topics, final Note existing) {
        setTitle(existing == null ? "Add Note" : "Edit Note");
        getDialogPane().getStylesheets().add(
                getClass().getResource(themeManager.getStylesheetPath()).toExternalForm());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final List<Topic> topicItems = new ArrayList<>();
        topicItems.add(null);
        topicItems.addAll(topics);
        topicCombo.getItems().addAll(topicItems);
        topicCombo.setConverter(generalConverter());

        titleField.setPromptText("e.g. Two-pointer cheat sheet");
        contentArea.setPromptText("Write your notes here...");
        contentArea.setPrefRowCount(8);

        if (existing == null) {
            topicCombo.getSelectionModel().selectFirst();
        } else {
            titleField.setText(existing.getTitle());
            topics.stream()
                    .filter(topic -> topic.getId().equals(existing.getTopicId()))
                    .findFirst()
                    .ifPresentOrElse(topicCombo::setValue, () -> topicCombo.getSelectionModel().selectFirst());
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
        grid.addRow(row++, new Label("Topic"), topicCombo);
        grid.addRow(row++, new Label("Content"), contentArea);
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
                ? new Result(titleField.getText(), topicCombo.getValue() == null ? null : topicCombo.getValue().getId(),
                        contentArea.getText())
                : null);
    }

    private static StringConverter<Topic> generalConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(final Topic topic) {
                return topic == null ? "No Topic (General)" : topic.getName();
            }

            @Override
            public Topic fromString(final String string) {
                return null;
            }
        };
    }
}
