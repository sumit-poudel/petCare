package com.example.petcare;

public class TaskTimeUtils {

    public static boolean isOverdue(Task task) {

        if (task.isCompleted()) {
            return false;
        }

        Long triggerMillis = ReminderScheduler.toTriggerMillis(
                task.getDate(),
                task.getTime()
        );

        if (triggerMillis == null) {
            return false;
        }

        return triggerMillis < System.currentTimeMillis();
    }
}