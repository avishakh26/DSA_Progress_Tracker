package com.dsatracker.view;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/** A title + progress bar + fraction-text card, e.g. "Today's Goal" / bar / "2 / 3 problems solved today". */
public final class ProgressCard extends Card {

    private final ProgressBar progressBar;
    private final Label fractionLabel;

    public ProgressCard(final String title) {
        getStyleClass().add("progress-card");

        final Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("progress-card-title");

        progressBar = new ProgressBar(0);
        progressBar.getStyleClass().add("progress-card-bar");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        fractionLabel = new Label();
        fractionLabel.getStyleClass().add("progress-card-fraction");

        getChildren().addAll(titleLabel, progressBar, fractionLabel);
    }

    /** @param progress 0.0-1.0 */
    public void update(final double progress, final String fractionText) {
        progressBar.setProgress(progress);
        fractionLabel.setText(fractionText);
    }
}
