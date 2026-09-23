package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.model.DiaryEntry;
import com.dsatracker.service.DiaryService;
import com.dsatracker.util.AlertHelper;
import com.dsatracker.view.DiaryEntryCard;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;

import java.util.List;

public final class DiaryController implements Refreshable {

    private final ThemeManager themeManager;
    private final DiaryService diaryService;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private FlowPane diaryGrid;

    @FXML
    private Label diaryEmptyLabel;

    public DiaryController(final ThemeManager themeManager, final DiaryService diaryService) {
        this.themeManager = themeManager;
        this.diaryService = diaryService;
    }

    @FXML
    private void initialize() {
        // See RoadmapController for why this specific binding (to the ScrollPane's actual
        // viewport bounds, not an ancestor's widthProperty) is necessary.
        diaryGrid.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));
        refresh();
    }

    @Override
    public void refresh() {
        renderDiaryEntries();
    }

    private void renderDiaryEntries() {
        final List<DiaryEntry> entries = diaryService.getAllEntries();

        diaryEmptyLabel.setVisible(entries.isEmpty());
        diaryEmptyLabel.setManaged(entries.isEmpty());

        diaryGrid.getChildren().setAll(entries.stream()
                .map(entry -> new DiaryEntryCard(entry, () -> onEditDiaryEntry(entry), () -> onDeleteDiaryEntry(entry)))
                .toList());
    }

    @FXML
    private void onAddDiaryEntry() {
        new DiaryEntryFormDialog(themeManager, null).showAndWait().ifPresent(result -> {
            try {
                diaryService.createEntry(result.title(), result.content());
                renderDiaryEntries();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Add Entry", e.getMessage());
            }
        });
    }

    private void onEditDiaryEntry(final DiaryEntry entry) {
        new DiaryEntryFormDialog(themeManager, entry).showAndWait().ifPresent(result -> {
            try {
                diaryService.updateEntry(entry.getId(), result.title(), result.content());
                renderDiaryEntries();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Update Entry", e.getMessage());
            }
        });
    }

    private void onDeleteDiaryEntry(final DiaryEntry entry) {
        if (AlertHelper.confirm("Delete Entry", "Delete \"" + entry.getTitle() + "\"? This cannot be undone.")) {
            try {
                diaryService.deleteEntry(entry.getId());
                renderDiaryEntries();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Delete Entry", e.getMessage());
            }
        }
    }
}
