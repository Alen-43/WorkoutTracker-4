package com.workouttracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseConnection {

    private static final String DEFAULT_DB_FILE = "workout_tracker.db";
    private static String dbUrl = "jdbc:sqlite:" + DEFAULT_DB_FILE;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found! Ensure sqlite-jdbc dependency is present.");
            e.printStackTrace();
        }
    }

    private DatabaseConnection() {
    }

    public static void setDbUrl(String customDbUrl) {
        dbUrl = customDbUrl;
    }

    public static void resetToDefaultDbUrl() {
        dbUrl = "jdbc:sqlite:" + DEFAULT_DB_FILE;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS workouts ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "body_section TEXT NOT NULL DEFAULT 'Chest', "
                + "exercise_name TEXT NOT NULL, "
                + "workout_date TEXT NOT NULL, "
                + "duration_minutes INTEGER DEFAULT 0, "
                + "weight_kg REAL DEFAULT 0, "
                + "reps INTEGER DEFAULT 10, "
                + "notes TEXT DEFAULT ''"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);

   
            try {
                stmt.execute("ALTER TABLE workouts ADD COLUMN body_section TEXT DEFAULT 'Chest';");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE workouts ADD COLUMN reps INTEGER DEFAULT 10;");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE workouts ADD COLUMN weight_kg REAL DEFAULT 0;");
            } catch (SQLException ignored) {}

            System.out.println("[DatabaseConnection] SQLite database and 'workouts' table initialized successfully.");

        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
