package com.example.expensetrackerapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.expensetrackerapp.data.local.AppDatabase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Main Application class for Expense Tracker App.
 * Initializes Firebase, Room Database, and Notification Channels.
 */
public class ExpenseTrackerApp extends Application {

    // Notification channel IDs
    public static final String CHANNEL_REMINDERS = "reminders_channel";
    public static final String CHANNEL_ALERTS = "alerts_channel";
    public static final String CHANNEL_GENERAL = "general_channel";

    private static ExpenseTrackerApp instance;
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Configure Firestore with offline persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        // Initialize Room Database
        database = AppDatabase.getInstance(this);

        // Create notification channels
        createNotificationChannels();
    }

    public static ExpenseTrackerApp getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    /**
     * Create notification channels for Android O and above.
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // Reminders channel
            NotificationChannel remindersChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Expense Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            remindersChannel.setDescription("Daily and weekly reminders to log expenses");

            // Alerts channel
            NotificationChannel alertsChannel = new NotificationChannel(
                    CHANNEL_ALERTS,
                    "Budget Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            alertsChannel.setDescription("Alerts when budget is exceeded");

            // General channel
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_LOW);
            generalChannel.setDescription("General app notifications");

            // Register channels
            manager.createNotificationChannel(remindersChannel);
            manager.createNotificationChannel(alertsChannel);
            manager.createNotificationChannel(generalChannel);
        }
    }
}
