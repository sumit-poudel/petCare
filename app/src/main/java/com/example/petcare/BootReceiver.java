package com.example.petcare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        DatabaseHelper db = new DatabaseHelper(context);

        List<Task> allPendingTasks = db.getAllIncompleteTasks();

        for (Task task : allPendingTasks) {
            ReminderScheduler.schedule(context, task);
        }
    }
}