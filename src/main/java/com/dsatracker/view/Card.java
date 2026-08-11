package com.dsatracker.view;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

/** Common visual base for every card-style component: dark surface, rounded corners, consistent padding/spacing. */
abstract class Card extends VBox {

    protected Card() {
        getStyleClass().add("card");
        setSpacing(6);
        setPadding(new Insets(18));
    }
}
