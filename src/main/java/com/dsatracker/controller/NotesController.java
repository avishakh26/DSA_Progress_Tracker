package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.model.Note;
import com.dsatracker.model.Topic;
import com.dsatracker.service.NoteService;
import com.dsatracker.service.TopicService;
import com.dsatracker.util.AlertHelper;
import com.dsatracker.view.NoteCard;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class NotesController implements Refreshable {

    private final NoteService noteService;
    private final TopicService topicService;
    private final ThemeManager themeManager;

    private Map<Integer, String> topicNamesById = Map.of();

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private ComboBox<Topic> topicFilterCombo;

    @FXML
    private FlowPane notesGrid;

    @FXML
    private Label emptyStateLabel;

    public NotesController(final NoteService noteService, final TopicService topicService,
                            final ThemeManager themeManager) {
        this.noteService = noteService;
        this.topicService = topicService;
        this.themeManager = themeManager;
    }

    @FXML
    private void initialize() {
        notesGrid.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));

        topicFilterCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(final Topic topic) {
                return topic == null ? "All Topics" : topic.getName();
            }

            @Override
            public Topic fromString(final String string) {
                return null;
            }
        });
        topicFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> renderNotes());

        refresh();
    }

    @Override
    public void refresh() {
        topicNamesById = topicService.getAllTopics().stream()
                .collect(Collectors.toMap(Topic::getId, Topic::getName));

        final Topic previouslySelected = topicFilterCombo.getValue();
        final List<Topic> topicItems = new ArrayList<>();
        topicItems.add(null);
        topicItems.addAll(topicService.getAllTopics());
        topicFilterCombo.getItems().setAll(topicItems);
        topicFilterCombo.setValue(topicItems.contains(previouslySelected) ? previouslySelected : null);

        renderNotes();
    }

    private void renderNotes() {
        final Topic filterTopic = topicFilterCombo.getValue();
        final List<Note> notes = filterTopic == null
                ? noteService.getAllNotes()
                : noteService.getNotesByTopic(filterTopic.getId());

        emptyStateLabel.setVisible(notes.isEmpty());
        emptyStateLabel.setManaged(notes.isEmpty());

        notesGrid.getChildren().setAll(notes.stream()
                .map(note -> new NoteCard(note, topicLabelFor(note), () -> onEditNote(note), () -> onDeleteNote(note)))
                .toList());
    }

    private String topicLabelFor(final Note note) {
        return note.getTopicId() == null ? "General" : topicNamesById.getOrDefault(note.getTopicId(), "Unknown");
    }

    @FXML
    private void onAddNote() {
        new NoteFormDialog(themeManager, topicService.getAllTopics(), null).showAndWait().ifPresent(result -> {
            try {
                noteService.createNote(result.title(), result.topicId(), result.content());
                refresh();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Add Note", e.getMessage());
            }
        });
    }

    private void onEditNote(final Note note) {
        new NoteFormDialog(themeManager, topicService.getAllTopics(), note).showAndWait().ifPresent(result -> {
            try {
                note.setTitle(result.title());
                note.setTopicId(result.topicId());
                note.setContent(result.content());
                noteService.updateNote(note);
                refresh();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Update Note", e.getMessage());
            }
        });
    }

    private void onDeleteNote(final Note note) {
        if (AlertHelper.confirm("Delete Note", "Delete \"" + note.getTitle() + "\"? This cannot be undone.")) {
            try {
                noteService.deleteNote(note.getId());
                refresh();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Delete Note", e.getMessage());
            }
        }
    }
}
