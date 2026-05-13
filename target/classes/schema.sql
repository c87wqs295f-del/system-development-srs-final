-- Enterprise Service Request Management System -- SQLite schema
-- Executed once on first run by com.fs.srs.repository.Database

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    username  TEXT    NOT NULL UNIQUE,
    password  TEXT    NOT NULL,
    full_name TEXT    NOT NULL,
    email     TEXT    NOT NULL,
    role      TEXT    NOT NULL CHECK (role IN ('EMP','AGENT','MGR'))
);

CREATE TABLE IF NOT EXISTS requests (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    title         TEXT    NOT NULL,
    description   TEXT    NOT NULL,
    category      TEXT    NOT NULL,
    priority      TEXT    NOT NULL,
    status        TEXT    NOT NULL,
    submitter_id  INTEGER NOT NULL,
    assignee_id   INTEGER,
    created_at    TEXT    NOT NULL,
    updated_at    TEXT    NOT NULL,
    resolved_at   TEXT,
    closed_at     TEXT,
    FOREIGN KEY (submitter_id) REFERENCES users(id),
    FOREIGN KEY (assignee_id)  REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    request_id INTEGER NOT NULL,
    author_id  INTEGER NOT NULL,
    text       TEXT    NOT NULL,
    created_at TEXT    NOT NULL,
    FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id)  REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_requests_status    ON requests(status);
CREATE INDEX IF NOT EXISTS idx_requests_assignee  ON requests(assignee_id);
CREATE INDEX IF NOT EXISTS idx_requests_submitter ON requests(submitter_id);
CREATE INDEX IF NOT EXISTS idx_comments_request   ON comments(request_id);
