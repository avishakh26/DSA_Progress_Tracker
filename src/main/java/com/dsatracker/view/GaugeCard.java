package com.dsatracker.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

/** A title + speedometer gauge + fraction-text card - the Goals-page counterpart to
 *  {@link ProgressCard}, for a more at-a-glance read on how close a goal is to done. */
public final class GaugeCard extends Card {

    private final GaugeMeter gauge;
    private final Label fractionLabel;

    public GaugeCard(final String title) {
        getStyleClass().add("gauge-card");
        setAlignment(Pos.CENTER);

        final Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("progress-card-title");

        gauge = new GaugeMeter();

        fractionLabel = new Label();
        fractionLabel.getStyleClass().add("progress-card-fraction");

        getChildren().addAll(titleLabel, gauge, fractionLabel);
    }

    /** @param progress 0.0-1.0 */
    public void update(final double progress, final String fractionText) {
        gauge.setProgress(progress);
        fractionLabel.setText(fractionText);
    }
}
