# College Project Report: Workout Tracker Desktop Application
**Course:** S3 Object Oriented Programming (PBCST304)  
**Technology Stack:** Java (JDK 21), Java Swing, JDBC, SQLite, Apache Maven  

---

## 1. Project Objective & Scope

The objective of this project is to develop a desktop application named **Workout Tracker** designed for structured gym logging and progress visualization.

The application strictly aligns with the S3 OOP syllabus (PBCST304) and includes:
- **Two Focused Tabs:** Workout Log (body sections, exercises, set logging, gym calendar) and My Progress (chronological history and training consistency graph).
- **Body Section Categorization:** 8 muscle groups with at least 3 distinct exercises each, plus support for custom exercises.
- **Set-by-Set Logging:** Tracking exercise name, body section, date, weight in kg, and repetitions (reps), supporting repeated sets on the same day.
- **Interactive Monthly Gym Calendar:** Dynamically highlights active workout days in GREEN from actual SQLite records.
- **Visual Consistency Tracking:** Custom Java 2D consistency graph displaying gym visit attendance per week and monthly consistency percentage based on distinct visit dates.
- **Robust JDBC Persistence:** Prepared statements for secure SQL CRUD operations without external web or cloud services.

---

## 2. System Architecture & Design

The application follows the **Model-View-Controller (MVC)** architectural design pattern:

```text
+-----------------------------------------------------------------------------------------+
|                                    VIEW & CONTROLLER                                    |
|   ui/WorkoutTrackerFrame.java   |   ui/CalendarPanel.java   |   ui/ConsistencyGraphPanel|
|   - TAB 1: Body Sections JList -> Exercises JList -> Log Set Dialog -> Gym Calendar    |
|   - TAB 2: Month Navigation -> Java 2D Consistency Graph -> Workout History JTable      |
+--------------------------------------------+--------------------------------------------+
                                             |
                            Passes validated | Returns List<Workout> & Set<String> dates
                            Workout objects  |
                                             v
+-----------------------------------------------------------------------------------------+
|                                      DATA ACCESS LAYER                                  |
|                                  database/WorkoutDAO.java                               |
|  - Executes SQL queries via JDBC PreparedStatement (INSERT, SELECT, DELETE)             |
|  - Queries distinct active dates in month for green calendar highlighting               |
|  - Queries sets on specific dates and months                                            |
+--------------------------------------------+--------------------------------------------+
                                             |
                            Connects via     |
                            DriverManager    |
                                             v
+-----------------------------------------------------------------------------------------+
|                                      DATABASE ENGINE                                    |
|                             database/DatabaseConnection.java                            |
|  - Connects to SQLite (workout_tracker.db)                                              |
|  - Auto-initializes table & performs safe schema migrations (body_section, reps)        |
+-----------------------------------------------------------------------------------------+
```

---

## 3. Database Design & SQL Operations

The application creates and connects to an SQLite database named `workout_tracker.db`.

### Table Schema: `workouts`
```sql
CREATE TABLE IF NOT EXISTS workouts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    body_section TEXT NOT NULL DEFAULT 'Chest',
    exercise_name TEXT NOT NULL,
    workout_date TEXT NOT NULL,
    duration_minutes INTEGER DEFAULT 0,
    weight_kg REAL DEFAULT 0,
    reps INTEGER DEFAULT 10,
    notes TEXT DEFAULT ''
);
```

### SQL CRUD Statements in `WorkoutDAO`:
| Operation | SQL Query | JDBC Method |
| :--- | :--- | :--- |
| **Create (Insert Set)** | `INSERT INTO workouts (body_section, exercise_name, workout_date, duration_minutes, weight_kg, reps, notes) VALUES (?, ?, ?, ?, ?, ?, ?)` | `PreparedStatement.executeUpdate()` |
| **Read (All History)** | `SELECT id, body_section, exercise_name, workout_date, duration_minutes, weight_kg, reps, notes FROM workouts ORDER BY workout_date DESC, id DESC` | `PreparedStatement.executeQuery()` & `ResultSet` |
| **Read (By Date)** | `SELECT id, body_section, exercise_name, workout_date, duration_minutes, weight_kg, reps, notes FROM workouts WHERE workout_date = ? ORDER BY id ASC` | `PreparedStatement.executeQuery()` |
| **Read (Distinct Dates in Month)** | `SELECT DISTINCT workout_date FROM workouts WHERE workout_date LIKE ?` | `PreparedStatement.executeQuery()` |
| **Delete (Delete Set)** | `DELETE FROM workouts WHERE id = ?` | `PreparedStatement.executeUpdate()` |

---

## 4. Key OOP Concepts Applied (Syllabus PBCST304)

### 4.1 Classes and Objects
- `Workout` serves as a blueprint defining the state and behavior of an individual workout set.
- Every set logged is an instantiated object with encapsulated properties.

### 4.2 Encapsulation (Data Hiding)
- Private attributes (`id`, `bodySection`, `exerciseName`, `date`, `weightKg`, `reps`, `notes`).
- Access is governed strictly through public getters and setters with validation (e.g. non-empty exercise names, positive reps, non-negative weight).

### 4.3 Inheritance
- `WorkoutTrackerFrame extends JFrame`: Main window management.
- `CalendarPanel extends JPanel`: Reusable monthly calendar.
- `ConsistencyGraphPanel extends JPanel`: Custom graphics component.

### 4.4 Polymorphism & Event Handling
- **Compile-time Polymorphism (Overloading):** Multiple constructors in `Workout`.
- **Runtime Polymorphism (Overriding & Interfaces):** 
  - Overriding `paintComponent(Graphics g)` in `ConsistencyGraphPanel`.
  - Overriding `toString()` in `Workout`.
  - Implementing `ActionListener`, `ListSelectionListener`, and `ChangeListener` callbacks.

### 4.5 Exception Handling
- `try-with-resources` ensuring all `Connection`, `PreparedStatement`, and `ResultSet` objects are closed automatically.
- `NumberFormatException` and `IllegalArgumentException` caught with user-friendly `JOptionPane` error dialogs.

---

## 5. Likely Viva Questions & Simple Answers

### Q1: How does the calendar mark dates green dynamically?
**Answer:**  
In `CalendarPanel.java`, we query the database using `WorkoutDAO.getDistinctWorkoutDatesInMonth(year, month)`. For every day of the month, if that date is present in the returned set of active dates, we set the button background to green (`new Color(220, 252, 231)`) and font color to dark green (`new Color(22, 101, 52)`). Days without workouts remain white.

### Q2: Why did you separate Body Sections from Exercises?
**Answer:**  
To provide a clean, organized workout logging workflow. A `JList` of 8 body sections (Chest, Back, etc.) filters the adjacent exercise list using a `Map<String, List<String>>`. Selecting an exercise enables logging specific sets without cluttered forms.

### Q3: How does the consistency graph distinguish sets from gym visits?
**Answer:**  
The consistency graph uses the set of **distinct workout dates** in the month. If a user logs 5 sets of Bench Press and Squats on the same date, all 5 sets share the same date and count as **1 gym visit** for consistency tracking on that day.

### Q4: Why are PreparedStatement objects used instead of Statement?
**Answer:**  
`PreparedStatement` pre-compiles SQL queries and uses parameter placeholders (`?`). This prevents **SQL Injection** attacks and handles escaping of quotes and special characters automatically.

### Q5: How do you support multiple sets of the same exercise on the same day?
**Answer:**  
Each set is inserted into the `workouts` table as a distinct row with an auto-incremented primary key (`id`). The database does not restrict multiple entries for the same exercise and date.
