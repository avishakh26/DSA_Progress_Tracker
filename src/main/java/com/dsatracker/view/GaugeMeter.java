package com.dsatracker.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;

/**
 * A speedometer-style gauge: a semicircle split into five red-to-green bands with a needle that
 * swings from the red end (just started) to the green end (goal complete) - read at a glance,
 * rather than needing to compare two numbers on a linear bar.
 */
final class GaugeMeter extends Pane {

    private static final double WIDTH = 220;
    private static final double HEIGHT = 128;
    private static final double CENTER_X = WIDTH / 2;
    private static final double CENTER_Y = HEIGHT - 12;
    private static final double OUTER_RADIUS = 96;
    private static final double BAND_THICKNESS = 22;
    private static final double NEEDLE_LENGTH = 66;
    private static final double BAND_SPAN = 36;
    private static final double BAND_GAP = 2;

    /** Worst (just started) to best (goal complete) - a fixed traffic-light ramp independent of
     *  the app's theme/accent, since red-to-green "how close to done" is its own universal signal. */
    private static final String[] BAND_COLORS = {"#E5533D", "#F2994A", "#F2C94C", "#92C93E", "#4CAF50"};

    private final Rotate needleRotate = new Rotate(-90, CENTER_X, CENTER_Y);

    GaugeMeter() {
        setPrefSize(WIDTH, HEIGHT);
        setMinSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);

        for (int i = 0; i < BAND_COLORS.length; i++) {
            final Arc band = new Arc(CENTER_X, CENTER_Y, OUTER_RADIUS, OUTER_RADIUS,
                    180 - i * BAND_SPAN, -(BAND_SPAN - BAND_GAP));
            band.setType(ArcType.OPEN);
            band.setFill(null);
            band.setStroke(Color.web(BAND_COLORS[i]));
            band.setStrokeWidth(BAND_THICKNESS);
            band.setStrokeLineCap(StrokeLineCap.BUTT);
            getChildren().add(band);
        }

        final Polygon needle = new Polygon(
                CENTER_X - 4, CENTER_Y,
                CENTER_X + 4, CENTER_Y,
                CENTER_X, CENTER_Y - NEEDLE_LENGTH);
        needle.getStyleClass().add("gauge-needle");
        needle.getTransforms().add(needleRotate);

        final Circle hub = new Circle(CENTER_X, CENTER_Y, 8);
        hub.getStyleClass().add("gauge-hub");

        getChildren().addAll(needle, hub);
    }

    /** @param progress 0.0-1.0, clamped */
    void setProgress(final double progress) {
        needleRotate.setAngle(-90 + Math.max(0, Math.min(1, progress)) * 180);
    }
}
