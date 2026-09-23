package com.dsatracker.view;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

/** Common visual base for every card-style component: dark surface, rounded corners, consistent padding/spacing. */
abstract class Card extends VBox {

    protected Card() {
        getStyleClass().add("card");
        setSpacing(6);
        setPadding(new Insets(18));
        // Without this, a parent VBox/HBox trying to stretch a card to fill an assigned column
        // (e.g. two cards sharing a row 50/50) silently clamps it back to its own small natural
        // width instead - a FlowPane-hosted card (StatCard, DiaryEntryCard, ...) is unaffected,
        // since FlowPane never asks a child to grow past its own preferred size anyway.
        setMaxWidth(Double.MAX_VALUE);
    }
}
