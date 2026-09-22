package com.dsatracker.view;

import com.dsatracker.model.DiaryEntry;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;

/** A diary-entry tile: title, entry date, a truncated content preview, and edit/delete actions. */
public final class DiaryEntryCard extends Card {

    private static final DateTimeFormatter ENTRY_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final int PREVIEW_LENGTH = 160;

    public DiaryEntryCard(final DiaryEntry entry, final Runnable onEdit, final Runnable onDelete) {
        getStyleClass().add("note-card");
        setPrefWidth(300);

        final Label titleLabel = new Label(entry.getTitle());
        titleLabel.getStyleClass().add("note-card-title");
        titleLabel.setWrapText(true);

        final Badge dateBadge = new Badge(entry.getEntryDate().format(ENTRY_DATE_FORMAT), "status-in-progress");

        final Label contentPreview = new Label(truncate(entry.getContent()));
        contentPreview.getStyleClass().add("note-card-content");
        contentPreview.setWrapText(true);

        final Label updatedLabel = new Label("Updated " + entry.getUpdatedAt().format(
                DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")));
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

        getChildren().addAll(titleLabel, dateBadge, contentPreview, updatedLabel, actionsRow);
    }

    private static String truncate(final String content) {
        if (content == null || content.isBlank()) {
            return "(no content)";
        }
        final String trimmed = content.trim();
        return trimmed.length() <= PREVIEW_LENGTH ? trimmed : trimmed.substring(0, PREVIEW_LENGTH) + "...";
    }
}
