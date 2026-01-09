package com.example.expensetrackerapp.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.expensetrackerapp.ExpenseTrackerApp;
import com.example.expensetrackerapp.MainActivity;
import com.example.expensetrackerapp.R;

/**
 * Worker for sending daily expense reminder notifications.
 */
public class ReminderWorker extends Worker {

    private static final int NOTIFICATION_ID = 1001;

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        sendReminderNotification();
        return Result.success();
    }

    private void sendReminderNotification() {
        Context context = getApplicationContext();

        // Create intent for notification click
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification
        Notification notification = new NotificationCompat.Builder(context, ExpenseTrackerApp.CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Don't forget to log your expenses!")
                .setContentText("Track your daily spending to stay on budget.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // Show notification
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, notification);
        } catch (SecurityException e) {
            // Permission not granted
            e.printStackTrace();
        }
    }
}
