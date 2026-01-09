package com.example.expensetrackerapp.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.expensetrackerapp.ExpenseTrackerApp;
import com.example.expensetrackerapp.MainActivity;
import com.example.expensetrackerapp.R;

import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

/**
 * Helper class for managing notifications.
 */
public class NotificationHelper {

    private static final String WORK_NAME_DAILY_REMINDER = "daily_expense_reminder";
    private static final int BUDGET_ALERT_ID = 2001;

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Schedule daily reminder notification.
     */
    public void scheduleDailyReminder() {
        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                ReminderWorker.class,
                1,
                TimeUnit.DAYS)
                .addTag("expense_reminder")
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_DAILY_REMINDER,
                ExistingPeriodicWorkPolicy.KEEP,
                reminderWork);
    }

    /**
     * Cancel daily reminder.
     */
    public void cancelDailyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_DAILY_REMINDER);
    }

    /**
     * Show budget exceeded alert.
     */
    public void showBudgetExceededAlert(double budget, double spent) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String message = String.format(
                "You've exceeded your monthly budget of ৳%.2f. Total spent: ৳%.2f",
                budget,
                spent);

        Notification notification = new NotificationCompat.Builder(context, ExpenseTrackerApp.CHANNEL_ALERTS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Budget Exceeded!")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(BUDGET_ALERT_ID, notification);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show budget warning (approaching limit).
     */
    public void showBudgetWarning(double budget, double spent, int percentUsed) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String message = String.format(
                "You've used %d%% of your monthly budget. ৳%.2f remaining.",
                percentUsed,
                budget - spent);

        Notification notification = new NotificationCompat.Builder(context, ExpenseTrackerApp.CHANNEL_ALERTS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Budget Warning")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(BUDGET_ALERT_ID + 1, notification);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
