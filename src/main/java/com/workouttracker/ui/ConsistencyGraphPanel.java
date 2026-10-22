package com.workouttracker.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

public class ConsistencyGraphPanel extends JPanel {

    private YearMonth displayedMonth = YearMonth.now();
    private Set<Integer> activeDaysOfMonth = new HashSet<>();

    public ConsistencyGraphPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 220));
        setMinimumSize(new Dimension(380, 180));
    }

    public void updateConsistencyData(YearMonth month, Set<String> distinctDatesSet) {
        this.displayedMonth = month != null ? month : YearMonth.now();
        this.activeDaysOfMonth.clear();

        if (distinctDatesSet != null) {
            String prefix = String.format("%04d-%02d-", displayedMonth.getYear(), displayedMonth.getMonthValue());
            for (String dateStr : distinctDatesSet) {
                if (dateStr.startsWith(prefix) && dateStr.length() >= 10) {
                    try {
                        int day = Integer.parseInt(dateStr.substring(8, 10));
                        activeDaysOfMonth.add(day);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
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

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(30, 41, 59));
        String monthTitle = "Training Consistency: " + displayedMonth.getMonth().name() + " " + displayedMonth.getYear();
        g2.drawString(monthTitle, 16, 22);

        int daysInMonth = displayedMonth.lengthOfMonth();
        int gymVisits = activeDaysOfMonth.size();
        double consistencyPercent = (daysInMonth > 0) ? ((double) gymVisits / daysInMonth) * 100 : 0.0;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(new Color(100, 116, 139));
        String summary = String.format("Gym Visits: %d days   |   Rest: %d days   |   Monthly Consistency: %.1f%%",
                gymVisits, (daysInMonth - gymVisits), consistencyPercent);
        g2.drawString(summary, 16, 38);

        if (gymVisits == 0) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(new Color(148, 163, 184));
            String msg = "No workout records logged in " + displayedMonth.getMonth().name() + " " + displayedMonth.getYear();
            int msgW = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (width - msgW) / 2, height / 2 + 10);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String sub = "Log workouts in Tab 1 to track your daily gym attendance consistency.";
            int subW = g2.getFontMetrics().stringWidth(sub);
            g2.drawString(sub, (width - subW) / 2, height / 2 + 30);
            g2.dispose();
            return;
        }
        int[] weekVisits = new int[5];
        int[] weekPossible = new int[5];

        for (int day = 1; day <= daysInMonth; day++) {
            int weekIdx = (day - 1) / 7;
            if (weekIdx > 4) weekIdx = 4;
            weekPossible[weekIdx]++;
            if (activeDaysOfMonth.contains(day)) {
                weekVisits[weekIdx]++;
            }
        }

        int chartLeft = 50;
        int chartRight = width - 20;
        int chartTop = 60;
        int chartBottom = height - 35;
        int chartW = chartRight - chartLeft;
        int chartH = chartBottom - chartTop;

        if (chartW <= 0 || chartH <= 0) {
            g2.dispose();
            return;
        }

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(241, 245, 249));
        for (int v = 0; v <= 7; v += 2) {
            int y = chartBottom - (int) ((v / 7.0) * chartH);
            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(chartLeft, y, chartRight, y);

            g2.setColor(new Color(148, 163, 184));
            String yLabel = v + "d";
            g2.drawString(yLabel, chartLeft - 24, y + 4);
        }

        g2.setColor(new Color(203, 213, 225));
        g2.drawLine(chartLeft, chartTop, chartLeft, chartBottom);
        g2.drawLine(chartLeft, chartBottom, chartRight, chartBottom);

        int numBars = 5;
        int slotWidth = chartW / numBars;
        int barWidth = Math.max(18, slotWidth - 28);

        String[] weekLabels = {"W1 (1-7)", "W2 (8-14)", "W3 (15-21)", "W4 (22-28)", "W5 (29+)"};

        for (int i = 0; i < numBars; i++) {
            int xCenter = chartLeft + (i * slotWidth) + (slotWidth / 2);
            int xBar = xCenter - (barWidth / 2);

            int visits = weekVisits[i];
            int possible = weekPossible[i];
            if (possible == 0) continue;

            double ratio = (double) visits / 7.0;
            int barH = (int) (ratio * chartH);
            int yBar = chartBottom - barH;

            if (visits > 0) {
                g2.setColor(new Color(34, 197, 94));
                g2.fillRoundRect(xBar, yBar, barWidth, barH, 4, 4);

                g2.setColor(new Color(22, 101, 52));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String vText = visits + " visits";
                g2.drawString(vText, xCenter - (g2.getFontMetrics().stringWidth(vText) / 2), yBar - 4);
            } else {
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(xBar, chartBottom - 4, barWidth, 4, 2, 2);
            }

            // X-Axis Week Label
            g2.setColor(new Color(71, 85, 105));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String wLabel = weekLabels[i];
            g2.drawString(wLabel, xCenter - (g2.getFontMetrics().stringWidth(wLabel) / 2), chartBottom + 16);
        }

        g2.dispose();
    }
}
