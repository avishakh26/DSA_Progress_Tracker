package com.dsatracker.view;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.List;

/**
 * A GitHub-style sparkline: a thin trend line, with a soft area fill beneath it, plotting daily
 * activity counts over a short recent window - no axes, gridlines or labels, just the shape of
 * recent momentum at a glance.
 */
public final class Sparkline extends StackPane {

    private static final double WIDTH = 220;
    private static final double HEIGHT = 40;
    private static final double VERTICAL_MARGIN = 4;

    public Sparkline(final List<Integer> dailyCounts) {
        setPrefSize(WIDTH, HEIGHT);
        setMinSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        getStyleClass().add("sparkline");

        // Nothing to plot yet - leave the area blank rather than draw a flat line across zero,
        // which would visually claim "no activity" as data rather than just "no data".
        if (dailyCounts.size() < 2 || dailyCounts.stream().allMatch(count -> count == 0)) {
            return;
        }

        final int max = dailyCounts.stream().mapToInt(Integer::intValue).max().orElse(1);
        final double stepX = WIDTH / (dailyCounts.size() - 1);
        final double plotHeight = HEIGHT - VERTICAL_MARGIN * 2;

        final Path line = new Path();
        line.getStyleClass().add("sparkline-line");
        final Path area = new Path();
        area.getStyleClass().add("sparkline-area");

        for (int i = 0; i < dailyCounts.size(); i++) {
            final double x = i * stepX;
            final double y = HEIGHT - VERTICAL_MARGIN - (max == 0 ? 0 : dailyCounts.get(i) / (double) max * plotHeight);
            if (i == 0) {
                line.getElements().add(new MoveTo(x, y));
                area.getElements().add(new MoveTo(x, HEIGHT));
                area.getElements().add(new LineTo(x, y));
            } else {
                line.getElements().add(new LineTo(x, y));
                area.getElements().add(new LineTo(x, y));
            }
        }
        area.getElements().add(new LineTo(WIDTH, HEIGHT));
        area.getElements().add(new ClosePath());

        getChildren().addAll(area, line);
    }
}
