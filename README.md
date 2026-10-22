# Workout Tracker Desktop Application
**College S3 Object Oriented Programming (PBCST304) Lab Project**

A practical desktop application built strictly with **Java (JDK 21)**, **Java Swing (GUI)**, **JDBC**, and **SQLite** for local permanent data storage, built and managed using **Apache Maven**.

---

## 1. Application Layout & Features

The application features **exactly two primary tabs**:

### TAB 1: WORKOUT LOG
- **Body Section Selector (Left Side):**
  - Instant navigation between 8 body sections: **Chest, Back, Biceps, Triceps, Shoulders, Legs, Abs, Forearms**.
  - Dynamically updates the exercise list in the adjacent panel.
- **Exercise Library (Default + Custom):**
  - **Chest:** Bench Press, Incline Dumbbell Press, Chest Fly
  - **Back:** Lat Pulldown, Seated Cable Row, One-Arm Dumbbell Row
  - **Biceps:** Barbell Curl, Dumbbell Curl, Hammer Curl
  - **Triceps:** Triceps Pushdown, Overhead Triceps Extension, Close-Grip Bench Press
  - **Shoulders:** Shoulder Press, Lateral Raise, Rear Delt Fly
  - **Legs:** Squat, Leg Press, Romanian Deadlift
  - **Abs:** Crunches, Leg Raises, Plank
  - **Forearms:** Wrist Curl, Reverse Wrist Curl, Farmer's Carry
  - **+ Add Custom Exercise:** Add new custom exercises directly to the selected body section without replacing default exercises.
- **Log Workout Set Dialog:**
  - Select an exercise and click **"➕ Add Workout (Log Set)"**.
  - Prompts for **Workout Date** (defaults to today), **Weight (kg)**, and **Repetitions (reps)**.
  - Multi-set support: repeated sets for the same exercise on the same day are saved as separate entries.
- **Monthly Gym Calendar (Right Side):**
  - Displays the active month with previous/next month navigation controls and a "Today" button.
  - **Marked GREEN:** Dates with at least one saved workout in SQLite turn green automatically.
  - **Rest Days:** Days without workouts remain unmarked (white).
  - **Date Drill-down:** Clicking any date displays all workout sets logged on that day in the details panel below the calendar.

---

### TAB 2: MY PROGRESS
- **Chronological Workout History Table:**
  - Table displaying: **ID, Date, Body Section, Exercise Name, Weight (kg), Reps**.
  - Review records for the selected month or toggle **"Show All History"**.
  - **Delete Set:** Select any row and click **"🗑 Delete Selected Set"** with confirmation.
- **Training Consistency Graph (Java 2D):**
  - Visualizes monthly gym visit consistency (Week 1 to Week 5).
  - Counts **distinct workout dates** (e.g., 5 sets on one day count as 1 gym visit).
  - Calculates and displays: **Total Gym Visits, Rest Days, and Monthly Consistency Percentage**.
  - Useful empty state when no history exists for a month (no fake data).

---

## 2. Project Folder Structure

```text
WorkoutTracker/
├── pom.xml                                  # Maven project configuration & dependencies
├── README.md                                # Setup, execution instructions & test cases
├── PROJECT_REPORT.md                        # College project report, Viva Q&A & syllabus notes
├── workout_tracker.db                       # Local SQLite database file (auto-initialized)
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── workouttracker/
│   │               ├── Main.java            # Entry point; launches GUI on Swing EDT
│   │               ├── model/
│   │               │   └── Workout.java     # Encapsulated model POJO (body section, reps, weight)
│   │               ├── database/
│   │               │   ├── DatabaseConnection.java # SQLite connection & safe schema migration
│   │               │   └── WorkoutDAO.java         # JDBC CRUD + calendar & history queries
│   │               └── ui/
│   │                   ├── CalendarPanel.java       # Monthly gym calendar (green active dates)
│   │                   ├── ConsistencyGraphPanel.java # Java 2D consistency graph
│   │                   └── WorkoutTrackerFrame.java # Two-tab desktop interface
│   └── test/
│       └── java/
│           └── com/
│               └── workouttracker/
│                   └── WorkoutTrackerTest.java     # Automated JUnit 5 unit & integration tests
```

---

## 3. How to Compile, Test, and Run

Open PowerShell or Command Prompt in the `WorkoutTracker` directory:

### Step 1: Compile the Project
```powershell
mvn clean compile
```
*Expected Result:* `BUILD SUCCESS`.

### Step 2: Run the Automated Tests
```powershell
mvn test
```
*Expected Result:* 8 automated unit/integration tests executed with 0 failures and 0 errors (`BUILD SUCCESS`).

### Step 3: Launch the Desktop GUI
```powershell
mvn exec:java
```

---

## 4. Manual Test Cases Matrix

| Test ID | Action / Input | Expected Result | Status |
| :--- | :--- | :--- | :--- |
| **TC-01** | Select "Chest", pick "Bench Press", click "Add Workout (Log Set)" | Modal dialog opens showing Chest and Bench Press pre-filled with today's date. | **PASS** |
| **TC-02** | Enter invalid reps (`0` or negative) | Validation error: *"Reps must be a positive number."* | **PASS** |
| **TC-03** | Enter negative weight (`-10`) | Validation error: *"Weight cannot be negative."* | **PASS** |
| **TC-04** | Enter `60` kg, `10` reps, click Save Set | Record saved into SQLite; Calendar turns today's date GREEN immediately. | **PASS** |
| **TC-05** | Log a second set (`62.5` kg, `8` reps) for same exercise on same day | Second set saved as a distinct record; repeated sets allowed. | **PASS** |
| **TC-06** | Click on today's date in the calendar | Details panel below calendar displays Set #1 and Set #2 with weights and reps. | **PASS** |
| **TC-07** | Click "+ Add Custom Exercise" under Biceps, enter "Incline Curl" | "Incline Curl" added to Biceps exercise list without removing defaults. | **PASS** |
| **TC-08** | Switch to **"2. My Progress"** tab | History table lists all sets with dates, sections, exercises, weights, and reps. | **PASS** |
| **TC-09** | Inspect consistency graph in Tab 2 | Displays weekly gym visits and monthly consistency percentage based on distinct dates. | **PASS** |
| **TC-10** | Select a row in the history table and click "Delete Selected Set" | Confirmation popup appears; on "Yes", set is deleted and calendar/graph refresh. | **PASS** |
| **TC-11** | Close and reopen application (`mvn exec:java`) | All saved records reload from SQLite database permanently. | **PASS** |
