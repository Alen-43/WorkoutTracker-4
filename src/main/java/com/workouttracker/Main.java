package com.workouttracker;

import java.awt.GraphicsEnvironment;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.workouttracker.database.DatabaseConnection;
import com.workouttracker.ui.WorkoutTrackerFrame;


public class Main {

    public static void main(String[] args) {

        DatabaseConnection.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            try {
        
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                

                
                if (!GraphicsEnvironment.isHeadless()) {
                    WorkoutTrackerFrame frame = new WorkoutTrackerFrame();
                    frame.setVisible(true);
                } else {
                    System.out.println("[Main] Running in a headless environment. GUI skipped.");
                }

            } catch (Exception e) {
                System.err.println("[Main] Error starting application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
