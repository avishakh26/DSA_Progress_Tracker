-- =====================================================================
-- DSA Progress Tracker - SQLite schema
-- Run on every startup; every statement is idempotent (IF NOT EXISTS)
-- so re-running it against an already-initialized database is a no-op.
-- =====================================================================

CREATE TABLE IF NOT EXISTS topics (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    description TEXT,
    phase       INTEGER NOT NULL CHECK (phase BETWEEN 1 AND 6),
    difficulty  TEXT    NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    status      TEXT    NOT NULL DEFAULT 'NOT_STARTED'
                        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
);

CREATE TABLE IF NOT EXISTS problems (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    title        TEXT    NOT NULL,
    platform     TEXT    NOT NULL CHECK (platform IN
                         ('LEETCODE', 'CODEFORCES', 'HACKERRANK', 'GEEKSFORGEEKS', 'CODECHEF', 'ATCODER', 'OTHER')),
    url          TEXT,
    topic_id     INTEGER NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    difficulty   TEXT    NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    status       TEXT    NOT NULL DEFAULT 'NOT_STARTED'
                         CHECK (status IN ('NOT_STARTED', 'ATTEMPTED', 'SOLVED')),
    notes        TEXT,
    date_added   TEXT    NOT NULL DEFAULT (date('now')),
    date_solved  TEXT
);

CREATE TABLE IF NOT EXISTS notes (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    title      TEXT    NOT NULL,
    topic_id   INTEGER REFERENCES topics(id) ON DELETE SET NULL,
    content    TEXT,
    created_at TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS goals (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    goal_type  TEXT    NOT NULL CHECK (goal_type IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    target     INTEGER NOT NULL CHECK (target > 0),
    start_date TEXT    NOT NULL,
    end_date   TEXT
);

CREATE TABLE IF NOT EXISTS activity (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    activity_date   TEXT    NOT NULL UNIQUE,
    problems_solved INTEGER NOT NULL DEFAULT 0 CHECK (problems_solved >= 0)
);

CREATE INDEX IF NOT EXISTS idx_problems_topic_id  ON problems(topic_id);
CREATE INDEX IF NOT EXISTS idx_problems_status     ON problems(status);
CREATE INDEX IF NOT EXISTS idx_problems_difficulty ON problems(difficulty);
CREATE INDEX IF NOT EXISTS idx_notes_topic_id      ON notes(topic_id);
CREATE INDEX IF NOT EXISTS idx_activity_date        ON activity(activity_date);
