-- Database schema for Smart Campus Navigation System

CREATE TABLE IF NOT EXISTS Buildings (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    x INTEGER NOT NULL DEFAULT 0,
    y INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS Roads (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    source TEXT NOT NULL,
    destination TEXT NOT NULL,
    distance REAL NOT NULL,
    status TEXT NOT NULL DEFAULT 'Open',
    FOREIGN KEY(source) REFERENCES Buildings(id) ON DELETE CASCADE,
    FOREIGN KEY(destination) REFERENCES Buildings(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL
);

-- Prepopulate Users
INSERT OR IGNORE INTO Users (username, password, role) VALUES ('admin', 'admin123', 'admin');
INSERT OR IGNORE INTO Users (username, password, role) VALUES ('student', 'student123', 'student');
