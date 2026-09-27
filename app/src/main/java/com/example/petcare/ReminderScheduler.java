package com.example.petcare;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderScheduler {

    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.US);

    public static Long toTriggerMillis(String date, String time) {

        if (date == null || time == null || date.isEmpty() || time.isEmpty()) {
            return null;
        }

        try {

            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(DATE_FORMAT.parse(date));

            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(TIME_FORMAT.parse(time));

            Calendar result = Calendar.getInstance();

            result.set(
                    dateCal.get(Calendar.YEAR),
                    dateCal.get(Calendar.MONTH),
                    dateCal.get(Calendar.DAY_OF_MONTH),
                    timeCal.get(Calendar.HOUR_OF_DAY),
                    timeCal.get(Calendar.MINUTE),
                    0
            );

            result.set(Calendar.MILLISECOND, 0);

            return result.getTimeInMillis();

        } catch (ParseException e) {
            return null;
        }
    }

    public static void schedule(Context context, Task task) {

        Long triggerAt = toTriggerMillis(task.getDate(), task.getTime());

        if (triggerAt == null || task.isCompleted()) {
            cancel(context, task);
            return;
        }

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);

        intent.putExtra(ReminderReceiver.EXTRA_TASK_ID, task.getId());
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, task.getTitle());
        intent.putExtra(ReminderReceiver.EXTRA_DESCRIPTION, task.getDescription());
        intent.putExtra(ReminderReceiver.EXTRA_REPEAT_TYPE, task.getRepeatType());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
    }

    public static void cancel(Context context, Task task) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}