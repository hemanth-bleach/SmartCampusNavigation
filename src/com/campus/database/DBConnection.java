package com.campus.database;

import com.campus.utils.Constants;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Manages database connection and handles fallback to text file storage.
 */
public class DBConnection {
    private static Connection connection = null;
    private static boolean useSQLite = false;

    static {
        try {
            // Ensure database directory exists
            File dbFolder = new File(Constants.DB_FOLDER);
            if (!dbFolder.exists()) {
                dbFolder.mkdirs();
            }

            // Check if SQLite JDBC Driver is on the classpath
            Class.forName("org.sqlite.JDBC");
            
            // Try establishing connection
            String url = "jdbc:sqlite:" + Constants.DB_FILE_PATH;
            connection = DriverManager.getConnection(url);
            useSQLite = true;
            System.out.println("SQLite database connected successfully.");

            // Initialize database schema
            initializeSchema();
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found on classpath. Falling back to local text file database.");
        } catch (Exception e) {
            System.out.println("Could not connect to SQLite database: " + e.getMessage() + ". Falling back to local text file database.");
        }
    }

    private static void initializeSchema() {
        if (!useSQLite || connection == null) return;
        try (Statement stmt = connection.createStatement()) {
            // Read schema from campus.sql in the project root or construct locally
            String createBuildingsTable = "CREATE TABLE IF NOT EXISTS Buildings (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "x INTEGER NOT NULL DEFAULT 0, " +
                    "y INTEGER NOT NULL DEFAULT 0);";

            String createRoadsTable = "CREATE TABLE IF NOT EXISTS Roads (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "source TEXT NOT NULL, " +
                    "destination TEXT NOT NULL, " +
                    "distance REAL NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'Open', " +
                    "FOREIGN KEY(source) REFERENCES Buildings(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(destination) REFERENCES Buildings(id) ON DELETE CASCADE);";

            String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL);";

            stmt.execute(createBuildingsTable);
            stmt.execute(createRoadsTable);
            stmt.execute(createUsersTable);

            // Pre-seed users
            String seedAdmin = "INSERT OR IGNORE INTO Users (username, password, role) VALUES ('admin', 'admin123', 'admin');";
            String seedStudent = "INSERT OR IGNORE INTO Users (username, password, role) VALUES ('student', 'student123', 'student');";
            stmt.execute(seedAdmin);
            stmt.execute(seedStudent);
        } catch (Exception e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        if (useSQLite) {
            try {
                if (connection == null || connection.isClosed()) {
                    String url = "jdbc:sqlite:" + Constants.DB_FILE_PATH;
                    connection = DriverManager.getConnection(url);
                }
            } catch (Exception e) {
                useSQLite = false;
                System.out.println("Connection lost. Falling back to local files: " + e.getMessage());
            }
        }
        return connection;
    }

    public static boolean isSqliteActive() {
        return useSQLite && connection != null;
    }
}
