package com.dsatracker.view;

import javafx.scene.control.Label;

/** A small rounded pill label used for difficulty/status tags - extends Label rather than composing over it. */
public final class Badge extends Label {

    public Badge(final String text, final String styleClass) {
        super(text);
        getStyleClass().addAll("badge", styleClass);
    }
}
