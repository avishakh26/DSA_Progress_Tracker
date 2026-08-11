# DSA Progress Tracker

An offline JavaFX desktop application for systematically learning Data Structures &
Algorithms: track solved problems, follow a phased roadmap, keep daily streaks, and
visualise progress with charts and a contribution heatmap.

**Stack:** Java 17 · JavaFX 21 · SQLite (JDBC) · Maven · Layered MVC

---

## Running

```bash
mvn clean javafx:run          # run in development
mvn clean package             # build target/dsa-progress-tracker.jar
java -jar target/dsa-progress-tracker.jar
```

Requires JDK 17+ on the `PATH` (and `JAVA_HOME` set). JavaFX is pulled in by Maven — no separate
SDK install needed. If Maven itself isn't installed, use the bundled wrapper instead — it
downloads the right Maven version automatically and needs nothing but the JDK:

```bash
./mvnw clean javafx:run       # Linux/macOS
mvnw.cmd clean javafx:run     # Windows
```

---

## Folder structure

```
dsa-progress-tracker/
├── pom.xml                              Maven build: deps, Java 17, javafx:run, fat jar
├── data/                                SQLite database file lives here at runtime (git-ignored)
└── src/
    ├── main/
    │   ├── java/com/dsatracker/
    │   │   ├── Main.java                Plain launcher (required for the shaded jar)
    │   │   ├── DsaTrackerApp.java       JavaFX Application: loads root FXML + stylesheet
    │   │   ├── controller/              One controller per FXML view. UI binding ONLY —
    │   │   │                            reads input, calls a service, updates nodes.
    │   │   ├── model/                   Plain domain objects (Topic, Problem, Note, Goal…)
    │   │   │   └── enums/               Difficulty, ProblemStatus, TopicStatus, Platform…
    │   │   ├── repository/              DAO layer. Interfaces + SQLite implementations.
    │   │   │                            The only package allowed to write SQL.
    │   │   ├── service/                 Business logic: streaks, progress %, validation,
    │   │   │                            goal evaluation. Knows nothing about JavaFX.
    │   │   ├── database/                DatabaseManager singleton, schema init, seed data
    │   │   ├── exception/               Custom checked/unchecked exceptions
    │   │   ├── util/                    AppConstants, formatters, AlertHelper, validators
    │   │   └── view/                    Reusable custom JavaFX components (stat card,
    │   │                                heatmap cell, badge) built by inheritance
    │   └── resources/com/dsatracker/
    │       ├── fxml/                    View definitions (MainView, Dashboard, Roadmap…)
    │       ├── css/                     dark-theme.css / light-theme.css
    │       ├── sql/                     schema.sql, seed data scripts
    │       └── images/                  Icons and artwork
    └── test/java/com/dsatracker/        JUnit 5 tests (services, repositories)
```

### Why this layout

The dependency arrow points **one way only**:

```
FXML  →  controller  →  service  →  repository  →  database  →  SQLite
                            ↘        ↙
                             model / enums
```

* A **controller** never touches JDBC; it calls a service interface.
* A **service** never touches JavaFX; it can be unit-tested headlessly.
* A **repository** is the sole owner of SQL, always through `PreparedStatement`.
* `model` is shared by every layer and depends on nothing.

Swapping SQLite for another database, or the JavaFX UI for a CLI, would touch exactly
one layer each.

---

## Database

`DatabaseManager` (a Singleton) owns the single SQLite connection and is the only class that
opens one or runs a raw script — every other read/write goes through a repository using
`PreparedStatement`. On startup it creates `data/dsa_tracker.db` if missing, enables
`PRAGMA foreign_keys` (off by default in SQLite) and WAL mode, then re-runs `sql/schema.sql`
(idempotent — every statement is `IF NOT EXISTS`) and, only the first time `topics` is empty,
`sql/seed.sql` with a 6-phase / 17-topic sample roadmap and a few solved/attempted problems.

```
topics(id, name, description, phase, difficulty, status)
problems(id, title, platform, url, topic_id → topics, difficulty, status, notes, date_added, date_solved)
notes(id, title, topic_id → topics, content, created_at, updated_at)
goals(id, goal_type, target, start_date, end_date)
activity(id, activity_date UNIQUE, problems_solved)
```

Covered by `DatabaseManagerTest` (JUnit 5) against an isolated in-memory database
(`jdbc:sqlite::memory:`), so the test suite never touches the real data file.

---

## Build progress

- [x] **Step 1** — Maven + JavaFX scaffold, package structure, theme foundation
- [x] **Step 2** — SQLite setup & `DatabaseManager`
- [x] **Step 3** — Models & enums
- [x] **Step 4** — Repositories (DAO)
- [x] **Step 5** — Services (business logic)
- [x] **Step 6** — Main window & sidebar routing
- [x] **Step 7** — Dashboard & Roadmap UI
- [x] **Step 8** — Problem Tracker UI
- [x] **Step 9** — Analytics, charts & heatmap
- [ ] Step 10 — Notes, Goals & Settings
- [ ] Step 11 — Final polish
