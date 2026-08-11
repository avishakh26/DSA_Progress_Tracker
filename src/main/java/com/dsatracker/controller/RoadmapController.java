package com.dsatracker.controller;

import com.dsatracker.ThemeManager;
import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.ProblemStatus;
import com.dsatracker.model.enums.TopicStatus;
import com.dsatracker.service.ProblemFilter;
import com.dsatracker.service.ProblemService;
import com.dsatracker.service.ProblemSortBy;
import com.dsatracker.service.TopicProgress;
import com.dsatracker.service.TopicService;
import com.dsatracker.util.AlertHelper;
import com.dsatracker.view.Badge;
import com.dsatracker.view.ProgressCard;
import com.dsatracker.view.TopicCard;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.Desktop;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Roadmap page: a phase-by-phase grid of {@link TopicCard}s by default: clicking one drills into
 * that topic's detail view (progress, solved/remaining problem lists) in the same page, with a
 * "Back to Roadmap" button returning to the grid - no separate routed view or nav entry, since
 * the detail page only ever makes sense as a child of Roadmap.
 */
public final class RoadmapController implements Refreshable {

    private static final Map<Integer, String> PHASE_NAMES = Map.of(
            1, "Fundamentals",
            2, "Linear Data Structures",
            3, "Recursion, Searching & Sorting",
            4, "Trees & Graphs",
            5, "Dynamic Programming & Greedy",
            6, "Advanced Topics"
    );

    private final TopicService topicService;
    private final ProblemService problemService;
    private final ThemeManager themeManager;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox contentContainer;

    public RoadmapController(final TopicService topicService, final ProblemService problemService,
                              final ThemeManager themeManager) {
        this.topicService = topicService;
        this.problemService = problemService;
        this.themeManager = themeManager;
    }

    @FXML
    private void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        contentContainer.getChildren().setAll(buildRoadmapListView());
    }

    // ----- Roadmap list (default view) --------------------------------------------------------

    private VBox buildRoadmapListView() {
        final Label title = new Label("Roadmap");
        title.getStyleClass().add("page-title");
        final Label subtitle = new Label("Six phases, from fundamentals to advanced topics.");
        subtitle.getStyleClass().add("page-subtitle");

        final VBox phasesContainer = new VBox(28);
        final Map<Integer, List<TopicProgress>> byPhase = topicService.getRoadmap().stream()
                .collect(Collectors.groupingBy(tp -> tp.topic().getPhase(), TreeMap::new, Collectors.toList()));
        byPhase.forEach((phase, topics) -> phasesContainer.getChildren().add(buildPhaseSection(phase, topics)));

        return new VBox(24, new VBox(4, title, subtitle), phasesContainer);
    }

    private VBox buildPhaseSection(final int phase, final List<TopicProgress> topics) {
        final Label header = new Label("Phase " + phase + ": " + PHASE_NAMES.getOrDefault(phase, "Topics"));
        header.getStyleClass().add("section-title");

        final FlowPane grid = new FlowPane(16, 16);
        // FlowPane.prefWrapLength defaults to a fixed value, not the actual available
        // width. Binding it to an ancestor's widthProperty() doesn't reliably work here
        // either - ScrollPane.fitToWidth can't shrink content below the unwrapped
        // FlowPane's own reported min width, so that width just keeps growing. The
        // viewport's actual rendered bounds are computed independently of content
        // sizing, so they're the one value immune to that feedback loop.
        grid.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));
        topics.stream()
                .map(tp -> new TopicCard(tp, () -> showTopicDetail(tp.topic().getId())))
                .forEach(grid.getChildren()::add);

        return new VBox(12, header, grid);
    }

    // ----- Topic detail view --------------------------------------------------------------------

    private void showTopicDetail(final int topicId) {
        final Topic topic = topicService.findById(topicId)
                .orElseThrow(() -> new IllegalStateException("Topic " + topicId + " no longer exists"));
        contentContainer.getChildren().setAll(buildTopicDetailView(topic));
    }

    private VBox buildTopicDetailView(final Topic topic) {
        final Button backButton = new Button("Back to Roadmap", new FontIcon("fas-arrow-left"));
        backButton.setOnAction(event -> refresh());

        final Label nameLabel = new Label(topic.getName());
        nameLabel.getStyleClass().add("page-title");

        final Label descriptionLabel = new Label(topic.getDescription());
        descriptionLabel.getStyleClass().add("page-subtitle");
        descriptionLabel.setWrapText(true);

        final Badge difficultyBadge = new Badge(topic.getDifficulty().getDisplayName(), topic.getDifficulty().getStyleClass());
        final Badge statusBadge = new Badge(topic.getStatus().getDisplayName(), topicStatusStyleClass(topic.getStatus()));
        final HBox badgeRow = new HBox(8, difficultyBadge, statusBadge);

        final List<Problem> problems = problemService.search(
                new ProblemFilter(null, topic.getId(), null, null, null), ProblemSortBy.DATE_ADDED_DESC);
        final List<Problem> solved = problems.stream()
                .filter(p -> p.getStatus() == ProblemStatus.SOLVED)
                .sorted(Comparator.comparing(Problem::getDateSolved, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        final List<Problem> remaining = problems.stream()
                .filter(p -> p.getStatus() != ProblemStatus.SOLVED)
                .toList();

        final ProgressCard progressCard = new ProgressCard(topic.getName() + " Progress");
        final double fraction = problems.isEmpty() ? 0 : solved.size() / (double) problems.size();
        progressCard.update(fraction, solved.size() + " / " + problems.size() + " problems solved");

        final Button addButton = new Button("Add Problem");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(event -> onAddProblem(topic));

        final VBox solvedSection = buildProblemSection("Solved Problems (" + solved.size() + ")", solved,
                "No problems solved in this topic yet.");
        final VBox remainingSection = buildProblemSection("Remaining Problems (" + remaining.size() + ")", remaining,
                "Nothing left to solve here - nice work!");

        return new VBox(20, backButton, new VBox(4, nameLabel, descriptionLabel), badgeRow, progressCard, addButton,
                solvedSection, remainingSection);
    }

    private VBox buildProblemSection(final String title, final List<Problem> problems, final String emptyText) {
        final Label header = new Label(title);
        header.getStyleClass().add("section-title");

        final VBox list = new VBox(10);
        if (problems.isEmpty()) {
            final Label empty = new Label(emptyText);
            empty.getStyleClass().add("page-subtitle");
            list.getChildren().add(empty);
        } else {
            problems.stream().map(this::buildProblemRow).forEach(list.getChildren()::add);
        }
        return new VBox(12, header, list);
    }

    private HBox buildProblemRow(final Problem problem) {
        final Label titleLabel = new Label(problem.getTitle());
        titleLabel.getStyleClass().add("topic-card-name");
        titleLabel.setWrapText(true);

        final Label platformLabel = new Label(
                problem.getPlatform() == null ? "" : problem.getPlatform().getDisplayName());
        platformLabel.getStyleClass().add("topic-card-count");

        final VBox titleBox = new VBox(2, titleLabel, platformLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        final Badge difficultyBadge = new Badge(problem.getDifficulty().getDisplayName(), problem.getDifficulty().getStyleClass());
        final Badge statusBadge = new Badge(problem.getStatus().getDisplayName(), problemStatusStyleClass(problem.getStatus()));
        final HBox badges = new HBox(6, difficultyBadge, statusBadge);
        badges.setAlignment(Pos.CENTER_LEFT);

        final HBox actions = new HBox(4);
        if (problem.getStatus() != ProblemStatus.SOLVED) {
            actions.getChildren().add(
                    iconButton("fas-check", "Mark as solved", "table-action-solve", () -> onMarkSolved(problem)));
        }
        if (problem.getUrl() != null && !problem.getUrl().isBlank()) {
            actions.getChildren().add(iconButton("fas-external-link-alt", "Open URL", null, () -> onOpenUrl(problem)));
        }
        actions.getChildren().add(iconButton("fas-edit", "Edit", null, () -> onEditProblem(problem)));
        actions.getChildren().add(iconButton("fas-trash", "Delete", "table-action-delete", () -> onDeleteProblem(problem)));

        final HBox row = new HBox(16, titleBox, badges, actions);
        row.getStyleClass().add("card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        return row;
    }

    private void onAddProblem(final Topic topic) {
        new ProblemFormDialog(themeManager, topicService.getAllTopics(), null, topic).showAndWait().ifPresent(result -> {
            try {
                problemService.addProblem(result.title(), result.platform(), result.url(), result.topicId(),
                        result.difficulty(), result.notes());
                showTopicDetail(topic.getId());
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Add Problem", e.getMessage());
            }
        });
    }

    private void onEditProblem(final Problem problem) {
        new ProblemFormDialog(themeManager, topicService.getAllTopics(), problem, null).showAndWait().ifPresent(result -> {
            try {
                final int originalTopicId = problem.getTopicId();
                problem.setTitle(result.title());
                problem.setPlatform(result.platform());
                problem.setUrl(result.url());
                problem.setTopicId(result.topicId());
                problem.setDifficulty(result.difficulty());
                problem.setNotes(result.notes());
                problemService.updateProblem(problem);
                // The edit may have moved the problem to a different topic - show whichever
                // topic the user is left looking at now that it's no longer here.
                showTopicDetail(result.topicId() != null ? result.topicId() : originalTopicId);
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Update Problem", e.getMessage());
            }
        });
    }

    private void onMarkSolved(final Problem problem) {
        try {
            problemService.markSolved(problem.getId());
            showTopicDetail(problem.getTopicId());
        } catch (final RuntimeException e) {
            AlertHelper.showError("Could Not Mark Solved", e.getMessage());
        }
    }

    private void onDeleteProblem(final Problem problem) {
        if (AlertHelper.confirm("Delete Problem", "Delete \"" + problem.getTitle() + "\"? This cannot be undone.")) {
            try {
                final int topicId = problem.getTopicId();
                problemService.deleteProblem(problem.getId());
                showTopicDetail(topicId);
            } catch (final RuntimeException e) {
                AlertHelper.showError("Could Not Delete Problem", e.getMessage());
            }
        }
    }

    private void onOpenUrl(final Problem problem) {
        try {
            Desktop.getDesktop().browse(new URI(problem.getUrl()));
        } catch (final Exception e) {
            AlertHelper.showError("Could Not Open URL", "Failed to open the link in your browser.\n\n" + e.getMessage());
        }
    }

    private static Button iconButton(final String iconLiteral, final String tooltipText,
                                      final String extraStyleClass, final Runnable action) {
        final FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("table-action-icon");
        final Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("table-action-button");
        if (extraStyleClass != null) {
            button.getStyleClass().add(extraStyleClass);
        }
        Tooltip.install(button, new Tooltip(tooltipText));
        button.setOnAction(event -> action.run());
        return button;
    }

    private String topicStatusStyleClass(final TopicStatus status) {
        return switch (status) {
            case NOT_STARTED -> "status-not-started";
            case IN_PROGRESS -> "status-in-progress";
            case COMPLETED -> "status-completed";
        };
    }

    private String problemStatusStyleClass(final ProblemStatus status) {
        return switch (status) {
            case NOT_STARTED -> "status-not-started";
            case ATTEMPTED -> "status-attempted";
            case SOLVED -> "status-completed";
        };
    }
}
