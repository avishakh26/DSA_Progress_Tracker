package com.dsatracker.view;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

/** A single "icon + big number + label" dashboard tile, e.g. an icon, "42", and "Solved Problems". */
public final class StatCard extends Card {

    private final Label valueLabel;

    public StatCard(final String iconLiteral, final String title, final String initialValue) {
        this(iconLiteral, title, initialValue, null);
    }

    /** @param variantStyleClass extra style class for color variants (e.g. "stat-card-easy"), or null for the default look */
    public StatCard(final String iconLiteral, final String title, final String initialValue, final String variantStyleClass) {
        getStyleClass().add("stat-card");
        if (variantStyleClass != null) {
            getStyleClass().add(variantStyleClass);
        }

        final FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("stat-icon");
        final HBox iconWrap = new HBox(icon);
        iconWrap.getStyleClass().add("stat-icon-wrap");

        valueLabel = new Label(initialValue);
        valueLabel.getStyleClass().add("stat-value");

        final Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        getChildren().addAll(iconWrap, valueLabel, titleLabel);
    }

    public void setValue(final String value) {
        valueLabel.setText(value);
    }
}
