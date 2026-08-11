package com.dsatracker.controller;

/** One sidebar destination: its label, FontAwesome5 icon literal, and the FXML view it routes to. */
public enum NavItem {
    DASHBOARD("Dashboard", "fas-th-large", "/com/dsatracker/fxml/DashboardView.fxml"),
    ROADMAP("Roadmap", "fas-road", "/com/dsatracker/fxml/RoadmapView.fxml"),
    PROBLEMS("Problems", "fas-tasks", "/com/dsatracker/fxml/ProblemsView.fxml"),
    ANALYTICS("Analytics", "fas-chart-bar", "/com/dsatracker/fxml/AnalyticsView.fxml"),
    NOTES("Notes", "fas-sticky-note", "/com/dsatracker/fxml/NotesView.fxml"),
    GOALS("Goals", "fas-bullseye", "/com/dsatracker/fxml/GoalsView.fxml"),
    SETTINGS("Settings", "fas-cog", "/com/dsatracker/fxml/SettingsView.fxml");

    private final String label;
    private final String iconLiteral;
    private final String fxmlPath;

    NavItem(final String label, final String iconLiteral, final String fxmlPath) {
        this.label = label;
        this.iconLiteral = iconLiteral;
        this.fxmlPath = fxmlPath;
    }

    public String getLabel() {
        return label;
    }

    public String getIconLiteral() {
        return iconLiteral;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
