package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.exception.ValidationException;
import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
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

import java.util.List;

/**
 * Modal add/edit form for a single problem. Pass an existing {@link Problem}
 * to pre-fill and edit it, or {@code null} to create a new one - the same
 * form and validation serve both flows. Validation reuses {@link Validator}
 * (the same rules {@code ProblemService} itself enforces) so the dialog can
 * reject bad input inline, before it ever closes, instead of the user
 * losing their entered data to a post-close service exception.
 */
final class ProblemFormDialog extends Dialog<ProblemFormDialog.Result> {

    record Result(String title, Platform platform, String url, Integer topicId, Difficulty difficulty, String notes) {
    }

    private final TextField titleField = new TextField();
    private final ComboBox<Platform> platformCombo = new ComboBox<>();
    private final TextField urlField = new TextField();
    private final ComboBox<Topic> topicCombo = new ComboBox<>();
    private final ComboBox<Difficulty> difficultyCombo = new ComboBox<>();
    private final TextArea notesArea = new TextArea();
    private final Label errorLabel = new Label();

    /**
     * @param defaultTopic pre-selected topic for a new problem (e.g. opened from that topic's
     *                      Roadmap detail page); ignored when {@code existing} is not {@code null},
     *                      and falls back to the first topic when {@code null} or not in {@code topics}
     */
    ProblemFormDialog(final ThemeManager themeManager, final List<Topic> topics, final Problem existing,
                       final Topic defaultTopic) {
        setTitle(existing == null ? "Add Problem" : "Edit Problem");
        getDialogPane().getStylesheets().add(
                getClass().getResource(themeManager.getStylesheetPath()).toExternalForm());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        platformCombo.getItems().addAll(Platform.values());
        topicCombo.getItems().addAll(topics);
        difficultyCombo.getItems().addAll(Difficulty.values());
        topicCombo.setConverter(nameConverter());

        titleField.setPromptText("e.g. Two Sum");
        urlField.setPromptText("https:// (optional)");
        notesArea.setPromptText("Optional notes");
        notesArea.setPrefRowCount(3);

        if (existing == null) {
            if (!platformCombo.getItems().isEmpty()) {
                platformCombo.getSelectionModel().selectFirst();
            }
            if (defaultTopic != null && topics.contains(defaultTopic)) {
                topicCombo.setValue(defaultTopic);
            } else if (!topics.isEmpty()) {
                topicCombo.getSelectionModel().selectFirst();
            }
            if (!difficultyCombo.getItems().isEmpty()) {
                difficultyCombo.getSelectionModel().selectFirst();
            }
        } else {
            titleField.setText(existing.getTitle());
            platformCombo.setValue(existing.getPlatform());
            urlField.setText(existing.getUrl());
            topics.stream()
                    .filter(topic -> topic.getId().equals(existing.getTopicId()))
                    .findFirst()
                    .ifPresent(topicCombo::setValue);
            difficultyCombo.setValue(existing.getDifficulty());
            notesArea.setText(existing.getNotes());
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
        grid.addRow(row++, new Label("Platform"), platformCombo);
        grid.addRow(row++, new Label("URL"), urlField);
        grid.addRow(row++, new Label("Topic"), topicCombo);
        grid.addRow(row++, new Label("Difficulty"), difficultyCombo);
        grid.addRow(row++, new Label("Notes"), notesArea);
        grid.add(errorLabel, 1, row);

        getDialogPane().setContent(grid);
        getDialogPane().getStyleClass().add("form-dialog");

        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                validateForm();
            } catch (final ValidationException e) {
                errorLabel.setText(e.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
            }
        });

        setResultConverter(buttonType -> buttonType == ButtonType.OK
                ? new Result(titleField.getText(), platformCombo.getValue(), blankToNull(urlField.getText()),
                        topicCombo.getValue() == null ? null : topicCombo.getValue().getId(),
                        difficultyCombo.getValue(), blankToNull(notesArea.getText()))
                : null);
    }

    private void validateForm() {
        Validator.requireNonBlank(titleField.getText(), "Title");
        if (topicCombo.getValue() == null) {
            throw new ValidationException("Please select a topic.");
        }
        Validator.requireValidUrlIfPresent(urlField.getText(), "URL");
    }

    private static String blankToNull(final String text) {
        return text == null || text.isBlank() ? null : text;
    }

    private static <T> StringConverter<T> nameConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(final T item) {
                return item == null ? "" : item.toString();
            }

            @Override
            public T fromString(final String string) {
                return null;
            }
        };
    }
}
