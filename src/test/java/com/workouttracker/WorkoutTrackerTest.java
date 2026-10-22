package com.workouttracker;

import com.workouttracker.database.DatabaseConnection;
import com.workouttracker.database.WorkoutDAO;
import com.workouttracker.model.Workout;
import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkoutTrackerTest: Automated Unit & Integration Tests.
 * Tests Model validation (body section, exercise name, date, weight, reps),
 * Database connectivity, safe migration, and DAO CRUD operations including
 * repeated sets and calendar distinct date queries.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkoutTrackerTest {

    private static final String TEST_DB_FILE = "test_workouts_v3.db";
    private static WorkoutDAO dao;
    private static int createdWorkoutId;

    @BeforeAll
    public static void setUpClass() {
        File f = new File(TEST_DB_FILE);
        if (f.exists()) {
            f.delete();
        }
        DatabaseConnection.setDbUrl("jdbc:sqlite:" + TEST_DB_FILE);
        DatabaseConnection.initializeDatabase();
        dao = new WorkoutDAO();
    }

    @AfterAll
    public static void tearDownClass() {
        DatabaseConnection.resetToDefaultDbUrl();
        File f = new File(TEST_DB_FILE);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Model Validation: Rejects Empty Exercise Name")
    public void testModelRejectsEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Workout("Chest", "", "2026-10-22", 60.0, 10);
        });
    }

    @Test
    @Order(2)
    @DisplayName("Model Validation: Rejects Invalid Date Format")
    public void testModelRejectsInvalidDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Workout("Chest", "Bench Press", "22/10/2026", 60.0, 10);
        });
    }

    @Test
    @Order(3)
    @DisplayName("Model Validation: Rejects Negative Weight")
    public void testModelRejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Workout("Chest", "Bench Press", "2026-10-22", -5.0, 10);
        });
    }

    @Test
    @Order(4)
    @DisplayName("Model Validation: Rejects Non-Positive Reps")
    public void testModelRejectsNonPositiveReps() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Workout("Chest", "Bench Press", "2026-10-22", 60.0, 0);
        });
    }

    @Test
    @Order(5)
    @DisplayName("CRUD - Create: Insert Set with Body Section and Reps")
    public void testAddWorkoutSet() throws SQLException {
        Workout set1 = new Workout("Chest", "Bench Press", "2026-10-20", 60.0, 10);
        boolean success = dao.addWorkout(set1);

        assertTrue(success, "Workout set insertion should succeed.");
        assertTrue(set1.getId() > 0, "Generated ID should be greater than 0.");
        createdWorkoutId = set1.getId();
    }

    @Test
    @Order(6)
    @DisplayName("CRUD - Multi-Set: Repeated Sets for Same Exercise on Same Day")
    public void testRepeatedSetsAllowed() throws SQLException {
        // Log set 2 and set 3 of Bench Press on the same date
        Workout set2 = new Workout("Chest", "Bench Press", "2026-10-20", 62.5, 8);
        Workout set3 = new Workout("Chest", "Bench Press", "2026-10-20", 65.0, 6);

        assertTrue(dao.addWorkout(set2));
        assertTrue(dao.addWorkout(set3));

        List<Workout> setsOnDate = dao.getWorkoutsByDate("2026-10-20");
        assertEquals(3, setsOnDate.size(), "Should contain all 3 sets for 2026-10-20.");
        assertEquals("Chest", setsOnDate.get(0).getBodySection());
        assertEquals("Bench Press", setsOnDate.get(0).getExerciseName());
    }

    @Test
    @Order(7)
    @DisplayName("Calendar Queries: Distinct Workout Dates in Month for Green Marking")
    public void testDistinctWorkoutDatesForCalendar() throws SQLException {
        // Add a workout on another day
        Workout legsSet = new Workout("Legs", "Squat", "2026-10-22", 80.0, 5);
        dao.addWorkout(legsSet);

        Set<String> activeDates = dao.getDistinctWorkoutDatesInMonth(2026, 10);
        // Should contain 2 distinct dates: "2026-10-20" and "2026-10-22"
        assertTrue(activeDates.contains("2026-10-20"));
        assertTrue(activeDates.contains("2026-10-22"));
        assertFalse(activeDates.contains("2026-10-21"), "Date without workouts should not be active.");
    }

    @Test
    @Order(8)
    @DisplayName("CRUD - Delete: Remove Specific Workout Set")
    public void testDeleteWorkout() throws SQLException {
        boolean deleted = dao.deleteWorkout(createdWorkoutId);
        assertTrue(deleted, "Deletion should return true.");

        Workout fetched = dao.getWorkoutById(createdWorkoutId);
        assertNull(fetched, "Set should no longer exist after deletion.");
    }

    @Test
    @Order(9)
    @DisplayName("UI Button Styling: High Contrast & BasicButtonUI configured")
    public void testUIButtonStyling() {
        javax.swing.JButton primaryBtn = com.workouttracker.ui.UIUtils.createPrimaryButton("Add Workout");
        assertTrue(primaryBtn.isOpaque(), "Primary button must be opaque for custom background");
        assertTrue(primaryBtn.isContentAreaFilled(), "Primary button content area must be filled");
        assertEquals(java.awt.Color.WHITE, primaryBtn.getForeground(), "Primary button text must be white");
        assertEquals(new java.awt.Color(37, 99, 235), primaryBtn.getBackground(), "Primary button background must be blue");
        assertTrue(primaryBtn.getUI() instanceof javax.swing.plaf.basic.BasicButtonUI, "Must use BasicButtonUI to avoid Windows LookAndFeel overriding background");

        javax.swing.JButton secondaryBtn = com.workouttracker.ui.UIUtils.createSecondaryButton("Cancel");
        assertTrue(secondaryBtn.isOpaque());
        assertEquals(new java.awt.Color(30, 41, 59), secondaryBtn.getForeground(), "Secondary button text must be dark slate");

        javax.swing.JButton dangerBtn = com.workouttracker.ui.UIUtils.createDangerButton("Delete");
        assertTrue(dangerBtn.isOpaque());
        assertEquals(new java.awt.Color(185, 28, 28), dangerBtn.getForeground(), "Danger button text must be crimson");

        javax.swing.JButton navBtn = com.workouttracker.ui.UIUtils.createNavButton(" < ");
        assertTrue(navBtn.isOpaque());
        assertEquals(new java.awt.Color(30, 41, 59), navBtn.getForeground(), "Nav button text must be dark");
    }
}
