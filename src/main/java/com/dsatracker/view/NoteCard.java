package com.dsatracker.view;

import com.dsatracker.model.Note;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;

/** A study-note tile: title, topic tag, a truncated content preview, last-updated time, and edit/delete actions. */
public final class NoteCard extends Card {

    private static final DateTimeFormatter UPDATED_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");
    private static final int PREVIEW_LENGTH = 160;

    public NoteCard(final Note note, final String topicLabel, final Runnable onEdit, final Runnable onDelete) {
        getStyleClass().add("note-card");
        setPrefWidth(300);

        final Label titleLabel = new Label(note.getTitle());
        titleLabel.getStyleClass().add("note-card-title");
        titleLabel.setWrapText(true);

        final Badge topicBadge = new Badge(topicLabel, "status-in-progress");

        final Label contentPreview = new Label(truncate(note.getContent()));
        contentPreview.getStyleClass().add("note-card-content");
        contentPreview.setWrapText(true);

        final Label updatedLabel = new Label("Updated " + note.getUpdatedAt().format(UPDATED_FORMAT));
        updatedLabel.getStyleClass().add("note-card-updated");

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final Button editButton = new Button("", new FontIcon("fas-edit"));
        editButton.getStyleClass().add("table-action-button");
        editButton.setOnAction(event -> onEdit.run());

        final Button deleteButton = new Button("", new FontIcon("fas-trash"));
        deleteButton.getStyleClass().addAll("table-action-button", "table-action-delete");
        deleteButton.setOnAction(event -> onDelete.run());

        final HBox actionsRow = new HBox(4, spacer, editButton, deleteButton);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(titleLabel, topicBadge, contentPreview, updatedLabel, actionsRow);
    }

    private static String truncate(final String content) {
        if (content == null || content.isBlank()) {
            return "(no content)";
        }
        final String trimmed = content.trim();
        return trimmed.length() <= PREVIEW_LENGTH ? trimmed : trimmed.substring(0, PREVIEW_LENGTH) + "...";
    }
}
