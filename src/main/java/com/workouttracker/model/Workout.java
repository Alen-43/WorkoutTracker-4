package com.workouttracker.model;
public class Workout {

    private int id;
    private String bodySection;
    private String exerciseName;
    private String date;
    private int durationMinutes;
    private double weightKg;
    private int reps;
    private String notes;

    public Workout() {
        this.bodySection = "General";
        this.reps = 10;
        this.weightKg = 0.0;
        this.notes = "";
    }

    public Workout(String bodySection, String exerciseName, String date, double weightKg, int reps) {
        this(0, bodySection, exerciseName, date, 0, weightKg, reps, "");
    }

    public Workout(String exerciseName, String date, int durationMinutes, String notes) {
        this(0, "General", exerciseName, date, durationMinutes, 0.0, 10, notes);
    }

    public Workout(String exerciseName, String date, int durationMinutes, double weightKg, String notes) {
        this(0, "General", exerciseName, date, durationMinutes, weightKg, 10, notes);
    }

    public Workout(int id, String bodySection, String exerciseName, String date, int durationMinutes, double weightKg, int reps, String notes) {
        this.id = id;
        setBodySection(bodySection);
        setExerciseName(exerciseName);
        setDate(date);
        setDurationMinutes(durationMinutes);
        setWeightKg(weightKg);
        setReps(reps);
        setNotes(notes);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBodySection() {
        return bodySection != null ? bodySection : "General";
    }

    public void setBodySection(String bodySection) {
        this.bodySection = (bodySection != null && !bodySection.trim().isEmpty()) ? bodySection.trim() : "General";
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Exercise name cannot be empty.");
        }
        this.exerciseName = exerciseName.trim();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be empty.");
        }
        String trimmed = date.trim();
        if (!trimmed.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new IllegalArgumentException("Date must be in YYYY-MM-DD format (e.g., 2026-10-22).");
        }
        this.date = trimmed;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative.");
        }
        this.durationMinutes = durationMinutes;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        if (weightKg < 0) {
            throw new IllegalArgumentException("Weight cannot be a negative number.");
        }
        this.weightKg = weightKg;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        if (reps <= 0) {
            throw new IllegalArgumentException("Reps must be a positive whole number (at least 1).");
        }
        this.reps = reps;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes != null ? notes.trim() : "";
    }

    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", bodySection='" + bodySection + '\'' +
                ", exerciseName='" + exerciseName + '\'' +
                ", date='" + date + '\'' +
                ", weightKg=" + weightKg +
                ", reps=" + reps +
                '}';
    }
}
