package com.dsatracker.controller;

import com.dsatracker.model.Problem;
import com.dsatracker.model.enums.ProblemStatus;
import com.dsatracker.service.ActivityService;
import com.dsatracker.service.DifficultyBreakdown;
import com.dsatracker.service.ProblemService;
import com.dsatracker.service.TopicProgress;
import com.dsatracker.service.TopicService;
import com.dsatracker.view.HeatmapCell;
import com.dsatracker.view.StatCard;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AnalyticsController implements Refreshable {

    private static final int HEATMAP_WEEKS = 53;

    private final ProblemService problemService;
    private final TopicService topicService;
    private final ActivityService activityService;

    @FXML
    private HBox streakRow;

    @FXML
    private VBox pieChartContainer;

    @FXML
    private VBox barChartContainer;

    @FXML
    private VBox heatmapContainer;

    @FXML
    private VBox topicProgressList;

    public AnalyticsController(final ProblemService problemService, final TopicService topicService,
                                final ActivityService activityService) {
        this.problemService = problemService;
        this.topicService = topicService;
        this.activityService = activityService;
    }

    @FXML
    private void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        renderStreaks();
        renderPieChart();
        renderBarChart();
        renderHeatmap();
        renderTopicProgress();
    }

    private void renderStreaks() {
        streakRow.getChildren().setAll(
                new StatCard("fas-fire", "Current Streak", activityService.getCurrentStreak() + " days"),
                new StatCard("fas-trophy", "Longest Streak", activityService.getLongestStreak() + " days"));
    }

    private void renderPieChart() {
        final List<Problem> problems = problemService.getAllProblems();
        final long solved = problems.stream().filter(p -> p.getStatus() == ProblemStatus.SOLVED).count();
        final long unsolved = problems.size() - solved;

        final PieChart chart = new PieChart();
        chart.getData().add(new PieChart.Data("Solved (" + solved + ")", solved));
        chart.getData().add(new PieChart.Data("Unsolved (" + unsolved + ")", unsolved));
        chart.getStyleClass().add("analytics-chart");
        VBox.setVgrow(chart, Priority.ALWAYS);

        pieChartContainer.getChildren().setAll(pieChartContainer.getChildren().get(0), chart);
    }

    private void renderBarChart() {
        final List<DifficultyBreakdown> breakdown = problemService.getDifficultyBreakdown();

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(true);
        final BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().add("analytics-chart");

        final XYChart.Series<String, Number> solvedSeries = new XYChart.Series<>();
        solvedSeries.setName("Solved");
        final XYChart.Series<String, Number> unsolvedSeries = new XYChart.Series<>();
        unsolvedSeries.setName("Unsolved");

        for (final DifficultyBreakdown entry : breakdown) {
            solvedSeries.getData().add(new XYChart.Data<>(entry.difficulty().getDisplayName(), entry.solved()));
            unsolvedSeries.getData().add(new XYChart.Data<>(entry.difficulty().getDisplayName(), entry.unsolved()));
        }
        chart.getData().addAll(solvedSeries, unsolvedSeries);
        VBox.setVgrow(chart, Priority.ALWAYS);

        barChartContainer.getChildren().setAll(barChartContainer.getChildren().get(0), chart);
    }

    private void renderHeatmap() {
        final LocalDate end = LocalDate.now();
        final LocalDate start = end.minusDays(7L * HEATMAP_WEEKS - 1);
        final Map<LocalDate, Integer> heatmapData = activityService.getHeatmapData(start, end);

        final ScrollPane heatmapScroll = new ScrollPane(buildHeatmapGrid(heatmapData, start, end));
        heatmapScroll.setFitToHeight(true);
        heatmapScroll.getStyleClass().add("heatmap-scroll");

        heatmapContainer.getChildren().setAll(
                heatmapContainer.getChildren().get(0), heatmapScroll, buildHeatmapLegend());
    }

    private GridPane buildHeatmapGrid(final Map<LocalDate, Integer> heatmapData, final LocalDate start, final LocalDate end) {
        final GridPane grid = new GridPane();
        grid.setHgap(3);
        grid.setVgap(3);

        final LocalDate gridStart = start.minusDays(start.getDayOfWeek().getValue() % 7);
        int column = 0;
        String lastMonthLabelled = "";
        LocalDate cursor = gridStart;
        while (!cursor.isAfter(end)) {
            final int row = cursor.getDayOfWeek().getValue() % 7; // Sunday=0 ... Saturday=6
            if (!cursor.isBefore(start)) {
                grid.add(new HeatmapCell(cursor, heatmapData.getOrDefault(cursor, 0)), column, row + 1);

                if (cursor.getDayOfMonth() <= 7) {
                    final String monthLabel = cursor.getMonth().getDisplayName(TextStyle.SHORT, Locale.US);
                    if (!monthLabel.equals(lastMonthLabelled)) {
                        final Label label = new Label(monthLabel);
                        label.getStyleClass().add("heatmap-month-label");
                        grid.add(label, column, 0);
                        lastMonthLabelled = monthLabel;
                    }
                }
            }
            cursor = cursor.plusDays(1);
            if (row == DayOfWeek.SATURDAY.getValue() % 7) {
                column++;
            }
        }
        return grid;
    }

    private Node buildHeatmapLegend() {
        final HBox legend = new HBox(4);
        legend.setAlignment(Pos.CENTER_LEFT);

        final Label less = new Label("Less");
        less.getStyleClass().add("heatmap-legend-label");
        legend.getChildren().add(less);

        for (int level = 0; level <= 4; level++) {
            final Region swatch = new Region();
            swatch.getStyleClass().addAll("heatmap-cell", "heatmap-level-" + level);
            swatch.setPrefSize(12, 12);
            swatch.setMinSize(12, 12);
            legend.getChildren().add(swatch);
        }

        final Label more = new Label("More");
        more.getStyleClass().add("heatmap-legend-label");
        legend.getChildren().add(more);

        return legend;
    }

    private void renderTopicProgress() {
        topicProgressList.getChildren().clear();
        for (final TopicProgress topicProgress : topicService.getRoadmap()) {
            topicProgressList.getChildren().add(buildTopicProgressRow(topicProgress));
        }
    }

    private Node buildTopicProgressRow(final TopicProgress topicProgress) {
        final Label nameLabel = new Label(topicProgress.topic().getName());
        nameLabel.getStyleClass().add("topic-progress-name");
        nameLabel.setPrefWidth(190);
        nameLabel.setMinWidth(190);

        final ProgressBar bar = new ProgressBar(topicProgress.percentComplete() / 100.0);
        bar.getStyleClass().add("topic-progress-bar");
        bar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);

        final Label percentLabel = new Label(String.format(Locale.US, "%.0f%% (%d/%d)",
                topicProgress.percentComplete(), topicProgress.solvedProblems(), topicProgress.totalProblems()));
        percentLabel.getStyleClass().add("topic-progress-percent");
        percentLabel.setPrefWidth(100);

        final HBox row = new HBox(12, nameLabel, bar, percentLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
