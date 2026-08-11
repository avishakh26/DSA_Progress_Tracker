package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.Difficulty;
import com.dsatracker.model.enums.Platform;
import com.dsatracker.model.enums.ProblemStatus;
import com.dsatracker.service.ProblemFilter;
import com.dsatracker.service.ProblemService;
import com.dsatracker.service.ProblemSortBy;
import com.dsatracker.service.TopicService;
import com.dsatracker.util.AlertHelper;
import com.dsatracker.view.Badge;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ProblemsController implements Refreshable {

    private final ProblemService problemService;
    private final TopicService topicService;
    private final ThemeManager themeManager;

    private final ObservableList<Problem> problemsData = FXCollections.observableArrayList();
    private Map<Integer, String> topicNamesById = Map.of();

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<Topic> topicFilterCombo;

    @FXML
    private ComboBox<Difficulty> difficultyFilterCombo;

    @FXML
    private ComboBox<Platform> platformFilterCombo;

    @FXML
    private ComboBox<ProblemStatus> statusFilterCombo;

    @FXML
    private ComboBox<ProblemSortBy> sortCombo;

    @FXML
    private TableView<Problem> problemsTable;

    @FXML
    private TableColumn<Problem, String> titleColumn;

    @FXML
    private TableColumn<Problem, String> platformColumn;

    @FXML
    private TableColumn<Problem, String> topicColumn;

    @FXML
    private TableColumn<Problem, Difficulty> difficultyColumn;

    @FXML
    private TableColumn<Problem, ProblemStatus> statusColumn;

    @FXML
    private TableColumn<Problem, String> dateAddedColumn;

    @FXML
    private TableColumn<Problem, String> dateSolvedColumn;

    @FXML
    private TableColumn<Problem, Void> actionsColumn;

    public ProblemsController(final ProblemService problemService, final TopicService topicService,
                               final ThemeManager themeManager) {
        this.problemService = problemService;
        this.topicService = topicService;
        this.themeManager = themeManager;
    }

    @FXML
    private void initialize() {
        setupFilterControls();
        setupTableColumns();
        // Forces columns to always share the table's actual available width instead of
        // keeping their raw prefWidth and triggering a horizontal scrollbar when the sum
        // of those prefWidths doesn't exactly match the rendered width.
        problemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        problemsTable.setItems(problemsData);
        problemsTable.setPlaceholder(new Label("No problems match your filters."));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        topicFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        difficultyFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        platformFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        refresh();
    }

    private void setupFilterControls() {
        topicFilterCombo.setConverter(nullableConverter("All Topics"));
        difficultyFilterCombo.setConverter(nullableConverter("All Difficulties"));
        platformFilterCombo.setConverter(nullableConverter("All Platforms"));
        statusFilterCombo.setConverter(nullableConverter("All Statuses"));
        sortCombo.setConverter(sortConverter());

        difficultyFilterCombo.getItems().add(null);
        difficultyFilterCombo.getItems().addAll(Difficulty.values());
        difficultyFilterCombo.getSelectionModel().selectFirst();

        platformFilterCombo.getItems().add(null);
        platformFilterCombo.getItems().addAll(Platform.values());
        platformFilterCombo.getSelectionModel().selectFirst();

        statusFilterCombo.getItems().add(null);
        statusFilterCombo.getItems().addAll(ProblemStatus.values());
        statusFilterCombo.getSelectionModel().selectFirst();

        sortCombo.getItems().addAll(ProblemSortBy.values());
        sortCombo.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitle()));
        platformColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getPlatform().getDisplayName()));
        topicColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(topicNamesById.getOrDefault(data.getValue().getTopicId(), "Unknown")));
        dateAddedColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getDateAdded().toString()));
        dateSolvedColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getDateSolved() == null ? "-" : data.getValue().getDateSolved().toString()));

        difficultyColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDifficulty()));
        difficultyColumn.setCellFactory(col -> badgeCell(d -> new Badge(d.getDisplayName(), d.getStyleClass())));

        statusColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStatus()));
        statusColumn.setCellFactory(col -> badgeCell(s -> new Badge(s.getDisplayName(), problemStatusStyleClass(s))));

        actionsColumn.setCellFactory(col -> new ActionsCell());
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

        applyFilters();
    }

    private void applyFilters() {
        final ProblemFilter filter = new ProblemFilter(
                blankToNull(searchField.getText()),
                topicFilterCombo.getValue() == null ? null : topicFilterCombo.getValue().getId(),
                difficultyFilterCombo.getValue(),
                platformFilterCombo.getValue(),
                statusFilterCombo.getValue());
        final ProblemSortBy sortBy = sortCombo.getValue() == null ? ProblemSortBy.DATE_ADDED_DESC : sortCombo.getValue();
        problemsData.setAll(problemService.search(filter, sortBy));
    }

    @FXML
    private void onClearFilters() {
        searchField.clear();
        topicFilterCombo.setValue(null);
        difficultyFilterCombo.setValue(null);
        platformFilterCombo.setValue(null);
        statusFilterCombo.setValue(null);
        sortCombo.setValue(ProblemSortBy.DATE_ADDED_DESC);
    }

    @FXML
    private void onAddProblem() {
        final List<Topic> topics = topicService.getAllTopics();
        if (topics.isEmpty()) {
            AlertHelper.showWarning("No Topics", "Add a topic to the roadmap before adding problems.");
            return;
        }
        new ProblemFormDialog(themeManager, topics, null).showAndWait().ifPresent(result -> {
            try {
                problemService.addProblem(result.title(), result.platform(), result.url(), result.topicId(),
                        result.difficulty(), result.notes());
                refresh();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Add Problem", e.getMessage());
            }
        });
    }

    private void onEditProblem(final Problem problem) {
        if (problem == null) {
            return;
        }
        new ProblemFormDialog(themeManager, topicService.getAllTopics(), problem).showAndWait().ifPresent(result -> {
            try {
                problem.setTitle(result.title());
                problem.setPlatform(result.platform());
                problem.setUrl(result.url());
                problem.setTopicId(result.topicId());
                problem.setDifficulty(result.difficulty());
                problem.setNotes(result.notes());
                problemService.updateProblem(problem);
                refresh();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Update Problem", e.getMessage());
            }
        });
    }

    private void onMarkSolved(final Problem problem) {
        if (problem == null) {
            return;
        }
        try {
            problemService.markSolved(problem.getId());
            refresh();
        } catch (final RuntimeException e) {
            AlertHelper.showError("Could Not Mark Solved", e.getMessage());
        }
    }

    private void onDeleteProblem(final Problem problem) {
        if (problem == null) {
            return;
        }
        if (AlertHelper.confirm("Delete Problem", "Delete \"" + problem.getTitle() + "\"? This cannot be undone.")) {
            try {
                problemService.deleteProblem(problem.getId());
                refresh();
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Delete Problem", e.getMessage());
            }
        }
    }

    private void onOpenUrl(final Problem problem) {
        if (problem == null || problem.getUrl() == null || problem.getUrl().isBlank()) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(problem.getUrl()));
        } catch (final Exception e) {
            AlertHelper.showError("Could Not Open URL", "Failed to open the link in your browser.\n\n" + e.getMessage());
        }
    }

    private String problemStatusStyleClass(final ProblemStatus status) {
        return switch (status) {
            case NOT_STARTED -> "status-not-started";
            case ATTEMPTED -> "status-attempted";
            case SOLVED -> "status-completed";
        };
    }

    private static Button iconButton(final String iconLiteral, final String tooltipText,
                                      final String... extraStyleClasses) {
        final FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("table-action-icon");
        final Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("table-action-button");
        button.getStyleClass().addAll(extraStyleClasses);
        Tooltip.install(button, new Tooltip(tooltipText));
        return button;
    }

    private <T> TableCell<Problem, T> badgeCell(final Function<T, Badge> badgeFactory) {
        return new TableCell<>() {
            @Override
            protected void updateItem(final T item, final boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : badgeFactory.apply(item));
            }
        };
    }

    private static String blankToNull(final String text) {
        return text == null || text.isBlank() ? null : text;
    }

    private static <T> StringConverter<T> nullableConverter(final String allLabel) {
        return new StringConverter<>() {
            @Override
            public String toString(final T item) {
                return item == null ? allLabel : item.toString();
            }

            @Override
            public T fromString(final String string) {
                return null;
            }
        };
    }

    private static StringConverter<ProblemSortBy> sortConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(final ProblemSortBy sortBy) {
                if (sortBy == null) {
                    return "";
                }
                return switch (sortBy) {
                    case DATE_ADDED_DESC -> "Date Added (Newest)";
                    case DIFFICULTY -> "Difficulty";
                    case TITLE_ALPHABETICAL -> "Title (A-Z)";
                };
            }

            @Override
            public ProblemSortBy fromString(final String string) {
                return null;
            }
        };
    }

    /** Solve/Edit/Open/Delete action buttons for a single row - icon-only so all four fit in a narrow column. */
    private final class ActionsCell extends TableCell<Problem, Void> {
        private final Button solveButton = iconButton("fas-check", "Mark as solved", "table-action-solve");
        private final Button editButton = iconButton("fas-edit", "Edit");
        private final Button openButton = iconButton("fas-external-link-alt", "Open URL");
        private final Button deleteButton = iconButton("fas-trash", "Delete", "table-action-delete");
        private final HBox box = new HBox(4, solveButton, editButton, openButton, deleteButton);

        ActionsCell() {
            getStyleClass().add("actions-cell");
            solveButton.setOnAction(event -> onMarkSolved(rowItem()));
            editButton.setOnAction(event -> onEditProblem(rowItem()));
            openButton.setOnAction(event -> onOpenUrl(rowItem()));
            deleteButton.setOnAction(event -> onDeleteProblem(rowItem()));
        }

        private Problem rowItem() {
            return getTableRow() == null ? null : getTableRow().getItem();
        }

        @Override
        protected void updateItem(final Void item, final boolean empty) {
            super.updateItem(item, empty);
            final Problem problem = rowItem();
            if (empty || problem == null) {
                setGraphic(null);
            } else {
                solveButton.setDisable(problem.getStatus() == ProblemStatus.SOLVED);
                openButton.setDisable(problem.getUrl() == null || problem.getUrl().isBlank());
                setGraphic(box);
            }
        }
    }
}
