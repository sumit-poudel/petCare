package com.example.petcare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "petcare_routine_reminders";

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_REPEAT_TYPE = "extra_repeat_type";

    @Override
    public void onReceive(Context context, Intent intent) {

        int taskId = intent.getIntExtra(EXTRA_TASK_ID, -1);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String description = intent.getStringExtra(EXTRA_DESCRIPTION);
        String repeatType = intent.getStringExtra(EXTRA_REPEAT_TYPE);

        showNotification(context, taskId, title, description);

        rescheduleIfRepeating(context, taskId, repeatType);
    }

    private void showNotification(
            Context context,
            int taskId,
            String title,
            String description
    ) {

        createChannel(context);

        Intent openIntent = new Intent(context, TasksRoutinesActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                taskId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(
                                title == null ? "Pet care reminder" : title
                        )
                        .setContentText(
                                description == null || description.isEmpty()
                                        ? "It's time for this routine"
                                        : description
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify(taskId, builder.build());
        }
    }

    private void rescheduleIfRepeating(
            Context context,
            int taskId,
            String repeatType
    ) {

        if (repeatType == null || repeatType.equals("none")) {
            return;
        }

        DatabaseHelper db = new DatabaseHelper(context);

        Task task = db.getTaskById(taskId);

        if (task == null) {
            return;
        }

        Long currentTrigger = ReminderScheduler.toTriggerMillis(
                task.getDate(),
                task.getTime()
        );

        if (currentTrigger == null) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTrigger);

        if (repeatType.equals("daily")) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        } else if (repeatType.equals("weekly")) {
            cal.add(Calendar.DAY_OF_YEAR, 7);
        } else {
            return;
        }

        String newDate = ReminderScheduler.DATE_FORMAT.format(cal.getTime());

        db.updateTaskDate(taskId, newDate);

        task.setDate(newDate);

        ReminderScheduler.schedule(context, task);
    }

    private void createChannel(Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Routine reminders",
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription("Reminders for pet care routines and tasks");

        manager.createNotificationChannel(channel);
    }
}