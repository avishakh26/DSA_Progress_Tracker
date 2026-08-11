package com.dsatracker.view;

import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** A single day's square in the GitHub-style activity heatmap; background intensity reflects problems solved. */
public final class HeatmapCell extends Region {

    private static final DateTimeFormatter TOOLTIP_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final double SIZE = 14;

    public HeatmapCell(final LocalDate date, final int problemsSolved) {
        getStyleClass().addAll("heatmap-cell", intensityStyleClass(problemsSolved));
        setPrefSize(SIZE, SIZE);
        setMinSize(SIZE, SIZE);
        setMaxSize(SIZE, SIZE);

        final String tooltipText = problemsSolved <= 0
                ? "No problems solved on " + date.format(TOOLTIP_DATE)
                : problemsSolved + (problemsSolved == 1 ? " problem" : " problems") + " solved on "
                        + date.format(TOOLTIP_DATE);
        Tooltip.install(this, new Tooltip(tooltipText));
    }

    private static String intensityStyleClass(final int problemsSolved) {
        if (problemsSolved <= 0) {
            return "heatmap-level-0";
        }
        if (problemsSolved == 1) {
            return "heatmap-level-1";
        }
        if (problemsSolved <= 3) {
            return "heatmap-level-2";
        }
        if (problemsSolved <= 5) {
            return "heatmap-level-3";
        }
        return "heatmap-level-4";
    }
}
