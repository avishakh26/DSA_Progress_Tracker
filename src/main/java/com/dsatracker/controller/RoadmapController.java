package com.dsatracker.controller;

import com.dsatracker.service.TopicProgress;
import com.dsatracker.service.TopicService;
import com.dsatracker.view.TopicCard;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox phasesContainer;

    public RoadmapController(final TopicService topicService) {
        this.topicService = topicService;
    }

    @FXML
    private void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        final Map<Integer, List<TopicProgress>> byPhase = topicService.getRoadmap().stream()
                .collect(Collectors.groupingBy(tp -> tp.topic().getPhase(), TreeMap::new, Collectors.toList()));

        phasesContainer.getChildren().clear();
        byPhase.forEach((phase, topics) -> phasesContainer.getChildren().add(buildPhaseSection(phase, topics)));
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
        topics.stream().map(TopicCard::new).forEach(grid.getChildren()::add);

        return new VBox(12, header, grid);
    }
}
