package com.workouttracker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.workouttracker.database.WorkoutDAO;
import com.workouttracker.model.Workout;

public class WorkoutTrackerFrame extends JFrame {

    private final WorkoutDAO workoutDAO;

    private static final String[] BODY_SECTIONS = {
            "Chest", "Back", "Biceps", "Triceps", "Shoulders", "Legs", "Abs", "Forearms"
    };

    private final Map<String, List<String>> exerciseMap = new LinkedHashMap<>();

    private JList<String> listBodySections;
    private DefaultListModel<String> exerciseListModel;
    private JList<String> listExercises;
    private JLabel lblCurrentSectionTitle;
    private CalendarPanel calendarPanel;

    private YearMonth progressMonth = YearMonth.now();
    private boolean showAllMonths = false;
    private JLabel lblProgressMonth;
    private ConsistencyGraphPanel consistencyGraphPanel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JButton btnDeleteSet;
    private JLabel lblProgressStats;

    public WorkoutTrackerFrame() {
        this.workoutDAO = new WorkoutDAO();

        initDefaultExercises();

        setTitle("Workout Tracker");
        setSize(1150, 750);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        buildTwoTabInterface();

        refreshAllViews();
    }

    private void initDefaultExercises() {
        exerciseMap.put("Chest", new ArrayList<>(List.of(
                "Bench Press", "Incline Dumbbell Press", "Chest Fly"
        )));
        exerciseMap.put("Back", new ArrayList<>(List.of(
                "Lat Pulldown", "Seated Cable Row", "One-Arm Dumbbell Row"
        )));
        exerciseMap.put("Biceps", new ArrayList<>(List.of(
                "Barbell Curl", "Dumbbell Curl", "Hammer Curl"
        )));
        exerciseMap.put("Triceps", new ArrayList<>(List.of(
                "Triceps Pushdown", "Overhead Triceps Extension", "Close-Grip Bench Press"
        )));
        exerciseMap.put("Shoulders", new ArrayList<>(List.of(
                "Shoulder Press", "Lateral Raise", "Rear Delt Fly"
        )));
        exerciseMap.put("Legs", new ArrayList<>(List.of(
                "Squat", "Leg Press", "Romanian Deadlift"
        )));
        exerciseMap.put("Abs", new ArrayList<>(List.of(
                "Crunches", "Leg Raises", "Plank"
        )));
        exerciseMap.put("Forearms", new ArrayList<>(List.of(
                "Wrist Curl", "Reverse Wrist Curl", "Farmer's Carry"
        )));
    }

    private void buildTwoTabInterface() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 8));
        rootPanel.setBorder(new EmptyBorder(10, 12, 10, 12));
        rootPanel.setBackground(new Color(245, 247, 250));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Workout Tracker");
        JLabel lblCourse = new JLabel("Track your workouts and progress");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        
        lblCourse.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCourse.setForeground(new Color(100, 116, 139));

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblCourse, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("  1. Workout Log  ", createWorkoutLogTab());

        tabbedPane.addTab("  2. My Progress  ", createMyProgressTab());

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                refreshProgressTab();
            } else {
                calendarPanel.refreshCalendar();
            }
        });

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    private JPanel createWorkoutLogTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel workoutSelectorPanel = createWorkoutSelectorPanel();

        calendarPanel = new CalendarPanel(workoutDAO);
        calendarPanel.setPreferredSize(new Dimension(380, 0));
        calendarPanel.setMinimumSize(new Dimension(330, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, workoutSelectorPanel, calendarPanel);
        splitPane.setResizeWeight(0.62);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createWorkoutSelectorPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        JPanel bodySectionsCard = new JPanel(new BorderLayout(0, 6));
        bodySectionsCard.setBackground(Color.WHITE);
        bodySectionsCard.setPreferredSize(new Dimension(170, 0));
        bodySectionsCard.setMinimumSize(new Dimension(140, 0));
        bodySectionsCard.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(203, 213, 225), 1),
                        "  Body Sections  ", TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12), new Color(30, 41, 59)),
                new EmptyBorder(6, 6, 6, 6)
        ));

        listBodySections = new JList<>(BODY_SECTIONS);
        listBodySections.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listBodySections.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listBodySections.setFixedCellHeight(32);
        listBodySections.setSelectedIndex(0);

        JScrollPane scrollSections = new JScrollPane(listBodySections);
        scrollSections.setBorder(null);
        bodySectionsCard.add(scrollSections, BorderLayout.CENTER);

        JPanel exercisesCard = new JPanel(new BorderLayout(0, 8));
        exercisesCard.setBackground(Color.WHITE);
        exercisesCard.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(203, 213, 225), 1),
                        "  Exercises  ", TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12), new Color(30, 41, 59)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        lblCurrentSectionTitle = new JLabel("Chest Exercises:");
        lblCurrentSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCurrentSectionTitle.setForeground(new Color(30, 41, 59));
        exercisesCard.add(lblCurrentSectionTitle, BorderLayout.NORTH);

        exerciseListModel = new DefaultListModel<>();
        listExercises = new JList<>(exerciseListModel);
        listExercises.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listExercises.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listExercises.setFixedCellHeight(30);

        JScrollPane scrollExercises = new JScrollPane(listExercises);
        scrollExercises.setBorder(new LineBorder(new Color(241, 245, 249), 1));
        exercisesCard.add(scrollExercises, BorderLayout.CENTER);

        JPanel actionBtnBox = new JPanel(new GridLayout(2, 1, 0, 6));
        actionBtnBox.setOpaque(false);
        actionBtnBox.setBorder(new EmptyBorder(6, 0, 0, 0));

        JButton btnAddWorkout = UIUtils.createPrimaryButton("➕  Add Workout (Log Set)");
        btnAddWorkout.setPreferredSize(new Dimension(0, 36));
        btnAddWorkout.addActionListener(e -> openAddWorkoutDialog());

        JButton btnAddCustom = UIUtils.createSecondaryButton("+ Add Custom Exercise");
        btnAddCustom.setPreferredSize(new Dimension(0, 30));
        btnAddCustom.addActionListener(e -> promptAddCustomExercise());

        actionBtnBox.add(btnAddWorkout);
        actionBtnBox.add(btnAddCustom);

        listBodySections.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateExerciseListForSelectedSection();
            }
        });

        updateExerciseListForSelectedSection();

        panel.add(bodySectionsCard, BorderLayout.WEST);
        panel.add(exercisesCard, BorderLayout.CENTER);
        return panel;
    }

    private void updateExerciseListForSelectedSection() {
        String selectedSection = listBodySections.getSelectedValue();
        if (selectedSection == null) selectedSection = "Chest";

        lblCurrentSectionTitle.setText(selectedSection + " Exercises:");
        exerciseListModel.clear();

        List<String> exercises = exerciseMap.getOrDefault(selectedSection, new ArrayList<>());
        for (String ex : exercises) {
            exerciseListModel.addElement(ex);
        }
        if (!exerciseListModel.isEmpty()) {
            listExercises.setSelectedIndex(0);
        }
    }

    private void promptAddCustomExercise() {
        String selectedSection = listBodySections.getSelectedValue();
        if (selectedSection == null) selectedSection = "Chest";

        String customName = JOptionPane.showInputDialog(
                this,
                "Enter new custom exercise name for " + selectedSection + ":",
                "Add Custom Exercise",
                JOptionPane.PLAIN_MESSAGE
        );

        if (customName != null && !customName.trim().isEmpty()) {
            String trimmed = customName.trim();
            List<String> list = exerciseMap.computeIfAbsent(selectedSection, k -> new ArrayList<>());
            if (!list.contains(trimmed)) {
                list.add(trimmed);
                exerciseListModel.addElement(trimmed);
                listExercises.setSelectedValue(trimmed, true);
                JOptionPane.showMessageDialog(this,
                        "'" + trimmed + "' added to " + selectedSection + " exercises!",
                        "Exercise Added", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Exercise already exists in " + selectedSection + ".",
                        "Duplicate Exercise", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void openAddWorkoutDialog() {
        String selectedSection = listBodySections.getSelectedValue();
        String selectedExercise = listExercises.getSelectedValue();

        if (selectedSection == null || selectedExercise == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an exercise from the list first.",
                    "No Exercise Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Log Workout Set - " + selectedExercise, true);
        dialog.setSize(400, 320);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel("<html><b>Body Section:</b> " + selectedSection +
                "<br><b>Exercise:</b> " + selectedExercise + "</html>");
        lblHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblHeader, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 8, 10));
        fieldsPanel.setOpaque(false);

        JLabel lblDate = new JLabel("Workout Date:*");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JTextField txtDate = new JTextField(calendarPanel.getSelectedDate().toString());
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblWeight = new JLabel("Weight (kg):*");
        lblWeight.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JTextField txtWeight = new JTextField("0");
        txtWeight.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblReps = new JLabel("Repetitions (reps):*");
        lblReps.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JTextField txtReps = new JTextField("10");
        txtReps.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        fieldsPanel.add(lblDate);
        fieldsPanel.add(txtDate);
        fieldsPanel.add(lblWeight);
        fieldsPanel.add(txtWeight);
        fieldsPanel.add(lblReps);
        fieldsPanel.add(txtReps);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSaveSet = UIUtils.createPrimaryButton("Save Set");
        btnSaveSet.addActionListener(e -> {
            String dateStr = txtDate.getText().trim();
            String weightStr = txtWeight.getText().trim();
            String repsStr = txtReps.getText().trim();

            if (dateStr.isEmpty() || !dateStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid date in YYYY-MM-DD format (e.g. 2026-10-22).",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightStr);
                if (weight < 0) {
                    JOptionPane.showMessageDialog(dialog,
                            "Weight cannot be negative.",
                            "Invalid Weight", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Weight must be a valid number (e.g., 60, 75.5, or 0 for bodyweight).",
                        "Invalid Weight", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int reps;
            try {
                reps = Integer.parseInt(repsStr);
                if (reps <= 0) {
                    JOptionPane.showMessageDialog(dialog,
                            "Reps must be a positive number (at least 1).",
                            "Invalid Reps", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Reps must be a whole integer number.",
                        "Invalid Reps", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Workout set = new Workout(selectedSection, selectedExercise, dateStr, weight, reps);
            try {
                boolean success = workoutDAO.addWorkout(set);
                if (success) {
                    dialog.dispose();
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this,
                            "Set logged successfully for " + selectedExercise + " (" + weight + " kg × " + reps + " reps)!",
                            "Workout Saved", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to save workout to database.",
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Database Error: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSaveSet);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private JPanel createMyProgressTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel monthNavPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        monthNavPanel.setOpaque(false);

        JButton btnPrevMonth = UIUtils.createNavButton(" < ");
        btnPrevMonth.addActionListener(e -> {
            showAllMonths = false;
            progressMonth = progressMonth.minusMonths(1);
            refreshProgressTab();
        });

        JButton btnNextMonth = UIUtils.createNavButton(" > ");
        btnNextMonth.addActionListener(e -> {
            showAllMonths = false;
            progressMonth = progressMonth.plusMonths(1);
            refreshProgressTab();
        });

        lblProgressMonth = new JLabel("", SwingConstants.LEFT);
        lblProgressMonth.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblProgressMonth.setForeground(new Color(30, 41, 59));

        JButton btnAllTime = UIUtils.createSecondaryButton("Show All History");
        btnAllTime.addActionListener(e -> {
            showAllMonths = true;
            refreshProgressTab();
        });

        JButton btnThisMonth = UIUtils.createSecondaryButton("This Month");
        btnThisMonth.addActionListener(e -> {
            showAllMonths = false;
            progressMonth = YearMonth.now();
            refreshProgressTab();
        });

        monthNavPanel.add(btnPrevMonth);
        monthNavPanel.add(lblProgressMonth);
        monthNavPanel.add(btnNextMonth);
        monthNavPanel.add(btnThisMonth);
        monthNavPanel.add(btnAllTime);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionPanel.setOpaque(false);

        lblProgressStats = new JLabel("Total Sets: 0");
        lblProgressStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblProgressStats.setForeground(new Color(100, 116, 139));

        btnDeleteSet = UIUtils.createDangerButton("🗑  Delete Selected Set");
        btnDeleteSet.setEnabled(false);
        btnDeleteSet.addActionListener(e -> handleDeleteSelectedSet());

        actionPanel.add(lblProgressStats);
        actionPanel.add(btnDeleteSet);

        topBar.add(monthNavPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));

        consistencyGraphPanel = new ConsistencyGraphPanel();
        consistencyGraphPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(6, 6, 6, 6)
        ));
        centerPanel.add(consistencyGraphPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 4));
        tablePanel.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Workout Sets History (Chronological):");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTableTitle.setForeground(new Color(30, 41, 59));
        tablePanel.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Date", "Body Section", "Exercise Name", "Weight (kg)", "Reps"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(28);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(241, 245, 249));

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(110);  // Date
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(140);  // Section
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(220);  // Exercise
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Weight
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // Reps

        DefaultTableCellRenderer centerRend = new DefaultTableCellRenderer();
        centerRend.setHorizontalAlignment(SwingConstants.CENTER);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(centerRend);
        historyTable.getColumnModel().getColumn(1).setCellRenderer(centerRend);
        historyTable.getColumnModel().getColumn(4).setCellRenderer(centerRend);
        historyTable.getColumnModel().getColumn(5).setCellRenderer(centerRend);

        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnDeleteSet.setEnabled(historyTable.getSelectedRow() >= 0);
            }
        });

        JScrollPane scrollTable = new JScrollPane(historyTable);
        scrollTable.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        tablePanel.add(scrollTable, BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void refreshProgressTab() {
        if (showAllMonths) {
            lblProgressMonth.setText("Viewing All Records");
        } else {
            String mName = progressMonth.getMonth().name();
            String fName = mName.charAt(0) + mName.substring(1).toLowerCase();
            lblProgressMonth.setText(fName + " " + progressMonth.getYear());
        }

        try {
            List<Workout> list;
            if (showAllMonths) {
                list = workoutDAO.getAllWorkouts();
            } else {
                list = workoutDAO.getWorkoutsByMonth(progressMonth.getYear(), progressMonth.getMonthValue());
            }

            historyTableModel.setRowCount(0);
            for (Workout w : list) {
                Object[] row = {
                        w.getId(),
                        w.getDate(),
                        w.getBodySection(),
                        w.getExerciseName(),
                        String.format("%.1f", w.getWeightKg()),
                        w.getReps()
                };
                historyTableModel.addRow(row);
            }

            lblProgressStats.setText(String.format("Total Sets: %d", list.size()));
            btnDeleteSet.setEnabled(false);

            // Update Consistency Graph with distinct workout dates for the displayed month
            Set<String> distinctDates = workoutDAO.getDistinctWorkoutDatesInMonth(
                    progressMonth.getYear(), progressMonth.getMonthValue());
            consistencyGraphPanel.updateConsistencyData(progressMonth, distinctDates);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database Error loading history: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteSelectedSet() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a workout set from the table to delete.",
                    "No Set Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) historyTableModel.getValueAt(selectedRow, 0);
        String date = (String) historyTableModel.getValueAt(selectedRow, 1);
        String exercise = (String) historyTableModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this set?\n" +
                "[" + date + "] " + exercise + " (ID #" + id + ")",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = workoutDAO.deleteWorkout(id);
                if (success) {
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this,
                            "Set deleted successfully.",
                            "Deleted", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Record could not be deleted.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Database Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshAllViews() {
        if (calendarPanel != null) {
            calendarPanel.refreshCalendar();
        }
        refreshProgressTab();
    }
}
