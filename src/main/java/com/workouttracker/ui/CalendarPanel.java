package com.workouttracker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.workouttracker.database.WorkoutDAO;
import com.workouttracker.model.Workout;

public class CalendarPanel extends JPanel {

    private final WorkoutDAO workoutDAO;
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private Set<String> activeWorkoutDates = new HashSet<>();

    private JLabel lblMonthYear;
    private JPanel daysGridPanel;
    private JPanel dateDetailsPanel;
    private JLabel lblDetailsHeader;
    private DefaultListModel<String> dateEntriesListModel;
    private JList<String> dateEntriesList;

    private Consumer<LocalDate> onDateSelectedListener;

    public CalendarPanel(WorkoutDAO workoutDAO) {
        this.workoutDAO = workoutDAO;
        this.currentYearMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();

        setLayout(new BorderLayout(0, 8));
        setBackground(Color.WHITE);
        setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(203, 213, 225), 1),
                        "  Gym Calendar  ", TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13), new Color(30, 41, 59)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        initUI();
        refreshCalendar();
    }

    public void setOnDateSelectedListener(Consumer<LocalDate> listener) {
        this.onDateSelectedListener = listener;
    }

    private void initUI() {
        JPanel navPanel = new JPanel(new BorderLayout(4, 0));
        navPanel.setOpaque(false);
        navPanel.setBorder(new EmptyBorder(0, 0, 6, 0));

        JButton btnPrev = UIUtils.createNavButton(" < ");
        btnPrev.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            refreshCalendar();
        });

        JButton btnNext = UIUtils.createNavButton(" > ");
        btnNext.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            refreshCalendar();
        });

        JButton btnToday = UIUtils.createSecondaryButton("Today");
        btnToday.addActionListener(e -> {
            currentYearMonth = YearMonth.now();
            selectedDate = LocalDate.now();
            refreshCalendar();
        });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMonthYear.setForeground(new Color(30, 41, 59));

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        leftNav.setOpaque(false);
        leftNav.add(btnPrev);

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        rightNav.setOpaque(false);
        rightNav.add(btnToday);
        rightNav.add(btnNext);

        navPanel.add(leftNav, BorderLayout.WEST);
        navPanel.add(lblMonthYear, BorderLayout.CENTER);
        navPanel.add(rightNav, BorderLayout.EAST);

        JPanel calendarContainer = new JPanel(new BorderLayout(0, 4));
        calendarContainer.setOpaque(false);

        JPanel dayHeaderPanel = new JPanel(new GridLayout(1, 7, 3, 3));
        dayHeaderPanel.setOpaque(false);
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : daysOfWeek) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(new Color(100, 116, 139));
            dayHeaderPanel.add(lbl);
        }
        calendarContainer.add(dayHeaderPanel, BorderLayout.NORTH);

        daysGridPanel = new JPanel(new GridLayout(6, 7, 3, 3));
        daysGridPanel.setOpaque(false);
        calendarContainer.add(daysGridPanel, BorderLayout.CENTER);

        JLabel lblLegend = new JLabel("Green = Workout logged   White = No workout", SwingConstants.CENTER);
        lblLegend.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLegend.setForeground(new Color(22, 101, 52));
        lblLegend.setBorder(new EmptyBorder(4, 0, 4, 0));
        calendarContainer.add(lblLegend, BorderLayout.SOUTH);

        dateDetailsPanel = new JPanel(new BorderLayout(0, 4));
        dateDetailsPanel.setOpaque(false);
        dateDetailsPanel.setPreferredSize(new Dimension(0, 150));
        dateDetailsPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        lblDetailsHeader = new JLabel("Workouts for this day");
        lblDetailsHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDetailsHeader.setForeground(new Color(30, 41, 59));
        dateDetailsPanel.add(lblDetailsHeader, BorderLayout.NORTH);

        dateEntriesListModel = new DefaultListModel<>();
        dateEntriesList = new JList<>(dateEntriesListModel);
        dateEntriesList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateEntriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane detailsScroll = new JScrollPane(dateEntriesList);
        detailsScroll.setBorder(null);
        dateDetailsPanel.add(detailsScroll, BorderLayout.CENTER);

        add(navPanel, BorderLayout.NORTH);
        add(calendarContainer, BorderLayout.CENTER);
        add(dateDetailsPanel, BorderLayout.SOUTH);
    }

    public void refreshCalendar() {
        try {
            activeWorkoutDates = workoutDAO.getDistinctWorkoutDatesInMonth(
                    currentYearMonth.getYear(), currentYearMonth.getMonthValue());
        } catch (SQLException e) {
            System.err.println("[CalendarPanel] Error querying workout dates: " + e.getMessage());
            activeWorkoutDates = new HashSet<>();
        }

        String monthName = currentYearMonth.getMonth().name();
        String formattedMonth = monthName.charAt(0) + monthName.substring(1).toLowerCase();
        lblMonthYear.setText(formattedMonth + " " + currentYearMonth.getYear());

        daysGridPanel.removeAll();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        LocalDate today = LocalDate.now();

        for (int i = 0; i < startDayOfWeek; i++) {
            JLabel emptyLabel = new JLabel("");
            daysGridPanel.add(emptyLabel);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            boolean hasWorkout = activeWorkoutDates.contains(dateStr);
            boolean isSelected = date.equals(selectedDate);
            boolean isToday = date.equals(today);

            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            dayBtn.setOpaque(true);
            dayBtn.setContentAreaFilled(true);
            dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            dayBtn.setFont(new Font("Segoe UI", (hasWorkout || isSelected || isToday) ? Font.BOLD : Font.PLAIN, 11));
            dayBtn.setMargin(new Insets(1, 1, 1, 1));
            dayBtn.setFocusPainted(false);

            if (hasWorkout) {
                dayBtn.setBackground(new Color(220, 252, 231));
                dayBtn.setForeground(new Color(22, 101, 52));
                dayBtn.setToolTipText("Workout recorded on " + dateStr);
            } else {
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setForeground(new Color(51, 65, 85));
            }

            if (isSelected) {
                dayBtn.setBorder(new LineBorder(new Color(37, 99, 235), 2));
            } else if (isToday) {
                dayBtn.setBorder(new LineBorder(new Color(245, 158, 11), 1));
            } else if (hasWorkout) {
                dayBtn.setBorder(new LineBorder(new Color(134, 239, 172), 1));
            } else {
                dayBtn.setBorder(new LineBorder(new Color(226, 232, 240), 1));
            }

            dayBtn.addActionListener(e -> {
                selectedDate = date;
                refreshCalendar();
                if (onDateSelectedListener != null) {
                    onDateSelectedListener.accept(date);
                }
            });

            daysGridPanel.add(dayBtn);
        }

        int totalCellsUsed = startDayOfWeek + daysInMonth;
        int remainingCells = 42 - totalCellsUsed;
        for (int i = 0; i < remainingCells; i++) {
            JLabel emptyLabel = new JLabel("");
            daysGridPanel.add(emptyLabel);
        }

        daysGridPanel.revalidate();
        daysGridPanel.repaint();

        updateDateDetails();
    }

    private void updateDateDetails() {
        if (selectedDate == null) return;

        String dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        lblDetailsHeader.setText("Workouts on " + dateStr + ":");
        dateEntriesListModel.clear();

        try {
            List<Workout> workoutsOnDate = workoutDAO.getWorkoutsByDate(dateStr);
            if (workoutsOnDate.isEmpty()) {
                dateEntriesListModel.addElement("No workouts logged on this day.");
            } else {
                int setNumber = 1;
                for (Workout workout : workoutsOnDate) {
                    dateEntriesListModel.addElement(String.format("Set #%d: [%s] %s — %.1f kg × %d reps",
                            setNumber++, workout.getBodySection(), workout.getExerciseName(),
                            workout.getWeightKg(), workout.getReps()));
                }
            }
        } catch (SQLException e) {
            dateEntriesListModel.addElement("Error loading sets: " + e.getMessage());
        }
    }

    public LocalDate getSelectedDate() {
        return selectedDate != null ? selectedDate : LocalDate.now();
    }

    public YearMonth getCurrentYearMonth() {
        return currentYearMonth;
    }
}
