package com.dsatracker.view;

import com.dsatracker.model.Topic;
import com.dsatracker.model.enums.TopicStatus;
import com.dsatracker.service.TopicProgress;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

/** A roadmap tile: topic name, difficulty/status badges, description, and a solved/total progress bar. */
public final class TopicCard extends Card {

    public TopicCard(final TopicProgress topicProgress) {
        getStyleClass().add("topic-card");
        setPrefWidth(280);

        final Topic topic = topicProgress.topic();

        final Label nameLabel = new Label(topic.getName());
        nameLabel.getStyleClass().add("topic-card-name");
        nameLabel.setWrapText(true);

        final Badge difficultyBadge = new Badge(topic.getDifficulty().getDisplayName(), topic.getDifficulty().getStyleClass());
        final Badge statusBadge = new Badge(topic.getStatus().getDisplayName(), statusStyleClass(topic.getStatus()));
        final HBox badgeRow = new HBox(8, difficultyBadge, statusBadge);

        final Label descriptionLabel = new Label(topic.getDescription());
        descriptionLabel.getStyleClass().add("topic-card-description");
        descriptionLabel.setWrapText(true);

        final ProgressBar bar = new ProgressBar(topicProgress.percentComplete() / 100.0);
        bar.getStyleClass().add("topic-card-bar");
        bar.setMaxWidth(Double.MAX_VALUE);

        final Label countLabel = new Label(topicProgress.solvedProblems() + " / " + topicProgress.totalProblems() + " solved");
        countLabel.getStyleClass().add("topic-card-count");

        getChildren().addAll(nameLabel, badgeRow, descriptionLabel, bar, countLabel);
    }

    private String statusStyleClass(final TopicStatus status) {
        return switch (status) {
            case NOT_STARTED -> "status-not-started";
            case IN_PROGRESS -> "status-in-progress";
            case COMPLETED -> "status-completed";
        };
    }
}
