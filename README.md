# DSA Progress Tracker

An offline JavaFX desktop application for systematically learning Data Structures &
Algorithms: track solved problems, follow a phased roadmap, keep daily streaks, and
visualise progress with charts and a contribution heatmap.

**Stack:** Java 17 · JavaFX 21 · SQLite (JDBC) · Maven · Layered MVC

---

## Download & use (Windows — no Java needed)

1. Open the **[latest release](https://github.com/avishakh26/DSA_Progress_Tracker/releases/latest)**
   and download `DSA-Progress-Tracker-vX.Y.Z-windows.zip` (under *Assets*).
2. **Right-click the zip → Extract All.** Don't run it from inside the zip.
3. Open the extracted `DSA Progress Tracker` folder and double-click **`DSA Progress Tracker.exe`**.
4. On the first launch the app **creates a "DSA Progress Tracker" shortcut on your Desktop** for you.
   After that you can launch it from there (it's only created once — deleting it later is fine).

**First launch**
- Windows SmartScreen may say *"Windows protected your PC"* because the app isn't code-signed.
  Click **More info → Run anyway**.
- The app starts with the standard **DSA roadmap** (17 topics across 6 phases, all "not started") and is
  otherwise **empty** — add your own problems, notes, goals and diary entries.
  (*Settings → Restore Sample Data* loads demo problems and notes if you want to explore.)

**Your data & updates**
- Everything is stored offline on your PC in `%USERPROFILE%\.dsa-tracker`, outside the app folder.
- When a newer release exists, a banner appears at the top of the app with a **Download** button.
  To update: download the new zip, extract it, and use the new folder. Your data carries over
  automatically. You can then delete the old folder.
- To uninstall, delete the app folder (and `%USERPROFILE%\.dsa-tracker` if you also want your data gone).

Requires 64-bit Windows 10/11. Build the exe yourself (needs a JDK 17+):
`powershell -ExecutionPolicy Bypass -File package-exe.ps1`.

### Publishing a new version (maintainer)

Raise `APP_VERSION` in `AppConstants.java`, commit, then push a tag — GitHub Actions builds and publishes the release:

```bash
git tag v1.0.1
git push origin v1.0.1
```

---

## Running (developers)

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
- [x] **Step 10** — Notes, Goals & Settings
- [x] **Step 11** — Final polish
