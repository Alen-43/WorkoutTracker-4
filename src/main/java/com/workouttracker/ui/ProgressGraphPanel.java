package com.workouttracker.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.workouttracker.model.Workout;


public class ProgressGraphPanel extends JPanel {

    private List<Workout> workoutHistory = new ArrayList<>();
    private String currentExercise = "All Exercises";

    public ProgressGraphPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(650, 420));
    }

    public void setWorkoutData(String exerciseName, List<Workout> data) {
        this.currentExercise = exerciseName != null ? exerciseName : "Exercise";
        this.workoutHistory = data != null ? new ArrayList<>(data) : new ArrayList<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (workoutHistory.isEmpty()) {
            drawEmptyState(g2, width, height);
            g2.dispose();
            return;
        }

        double maxWeight = 0;
        double minWeight = Double.MAX_VALUE;
        double sumWeight = 0;
        int validWeightPoints = 0;

        for (Workout w : workoutHistory) {
            double wt = w.getWeightKg();
            if (wt > maxWeight) maxWeight = wt;
            if (wt < minWeight) minWeight = wt;
            sumWeight += wt;
            validWeightPoints++;
        }

        double avgWeight = validWeightPoints > 0 ? (sumWeight / validWeightPoints) : 0;
        if (minWeight == Double.MAX_VALUE) minWeight = 0;


        double yCeiling = maxWeight > 0 ? (maxWeight * 1.2) : 50;


        drawHeaderAndStats(g2, width, maxWeight, minWeight, avgWeight, validWeightPoints);


        int padLeft = 65;
        int padRight = 40;
        int padTop = 85;
        int padBottom = 60;

        int plotWidth = width - padLeft - padRight;
        int plotHeight = height - padTop - padBottom;

        if (plotWidth <= 0 || plotHeight <= 0) {
            g2.dispose();
            return;
        }


        g2.setColor(new Color(230, 235, 240));
        g2.setStroke(new BasicStroke(1f));

        int yDivisions = 5;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i <= yDivisions; i++) {
            int y = padTop + plotHeight - (i * plotHeight / yDivisions);

            g2.setColor(new Color(235, 238, 242));
            g2.drawLine(padLeft, y, padLeft + plotWidth, y);

            g2.setColor(new Color(120, 130, 140));
            double yVal = (yCeiling * i) / yDivisions;
            String label = String.format("%.0f kg", yVal);
            g2.drawString(label, padLeft - fm.stringWidth(label) - 8, y + 4);
        }

        g2.setColor(new Color(180, 185, 195));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(padLeft, padTop, padLeft, padTop + plotHeight); // Y axis
        g2.drawLine(padLeft, padTop + plotHeight, padLeft + plotWidth, padTop + plotHeight); // X axis


        int n = workoutHistory.size();
        Point[] points = new Point[n];

        for (int i = 0; i < n; i++) {
            Workout w = workoutHistory.get(i);
            int x = (n == 1) ? (padLeft + plotWidth / 2) : (padLeft + (i * plotWidth / (n - 1)));
            double ratio = yCeiling > 0 ? (w.getWeightKg() / yCeiling) : 0;
            int y = padTop + plotHeight - (int) (ratio * plotHeight);
            points[i] = new Point(x, y);
        }

        g2.setColor(new Color(13, 110, 253));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n - 1; i++) {
            g2.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y);
        }

    
        for (int i = 0; i < n; i++) {
            Workout w = workoutHistory.get(i);
            Point p = points[i];

            g2.setColor(new Color(13, 110, 253));
            g2.fillOval(p.x - 5, p.y - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillOval(p.x - 2, p.y - 2, 4, 4);
            g2.setColor(new Color(33, 37, 41));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String valStr = String.format("%.1f kg", w.getWeightKg());
            g2.drawString(valStr, p.x - (g2.getFontMetrics().stringWidth(valStr) / 2), p.y - 8);

            g2.setColor(new Color(108, 117, 125));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String dateLabel = w.getDate().length() >= 5 ? w.getDate().substring(5) : w.getDate(); // MM-DD
            g2.drawString(dateLabel, p.x - (g2.getFontMetrics().stringWidth(dateLabel) / 2), padTop + plotHeight + 18);
        }

        g2.dispose();
    }

    private void drawHeaderAndStats(Graphics2D g2, int width, double max, double min, double avg, int count) {

        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.setColor(new Color(33, 37, 41));
        g2.drawString("Progress Trend: " + currentExercise, 20, 26);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        String stats = String.format("Sessions: %d   |   Max: %.1f kg (PR)   |   Min: %.1f kg   |   Avg: %.1f kg",
                count, max, min, avg);
        g2.setColor(new Color(73, 80, 87));
        g2.drawString(stats, 20, 48);

        g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g2.setColor(new Color(130, 138, 145));
        g2.drawString("Dates shown on horizontal axis (MM-DD)  •  Weights plotted chronologically", 20, 66);
    }

    private void drawEmptyState(Graphics2D g2, int width, int height) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(new Color(108, 117, 125));
        String msg = "No workout logs found for: " + currentExercise;
        int msgWidth = g2.getFontMetrics().stringWidth(msg);
        g2.drawString(msg, (width - msgWidth) / 2, height / 2 - 10);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        String sub = "Log workouts with this exercise and weight in the 'Log & History' tab to view your progress graph!";
        int subWidth = g2.getFontMetrics().stringWidth(sub);
        g2.drawString(sub, (width - subWidth) / 2, height / 2 + 16);
    }
}