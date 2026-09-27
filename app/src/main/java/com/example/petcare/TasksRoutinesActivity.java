package com.example.petcare;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TasksRoutinesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;

    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private final List<Task> allTasks = new ArrayList<>();
    private final List<Task> filteredTasks = new ArrayList<>();

    private TextView pendingCountText;
    private TextView completedCountText;
    private TextView totalCountText;

    private TextView chipAll;
    private TextView chipPending;
    private TextView chipCompleted;

    private String filterMode = "all";

    private String pendingSmsPhone;
    private String pendingSmsMessage;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> { }
            );

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {

                        if (granted) {

                            sendSms(pendingSmsPhone, pendingSmsMessage);

                        } else {

                            Toast.makeText(
                                    this,
                                    "SMS permission denied",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        setContentView(R.layout.activity_tasks_routines);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setHasFixedSize(true);

        pendingCountText = findViewById(R.id.pendingCountText);
        completedCountText = findViewById(R.id.completedCountText);
        totalCountText = findViewById(R.id.totalCountText);

        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipCompleted = findViewById(R.id.chipCompleted);

        AnimUtils.bounceClick(chipAll);
        AnimUtils.bounceClick(chipPending);
        AnimUtils.bounceClick(chipCompleted);

        chipAll.setOnClickListener(v -> {
            filterMode = "all";
            updateChipStyles();
            applyFilter();
        });

        chipPending.setOnClickListener(v -> {
            filterMode = "pending";
            updateChipStyles();
            applyFilter();
        });

        chipCompleted.setOnClickListener(v -> {
            filterMode = "completed";
            updateChipStyles();
            applyFilter();
        });

        findViewById(R.id.emptyAddButton)
                .setOnClickListener(v -> showTask(null));

        AnimUtils.bounceClick(findViewById(R.id.emptyAddButton));

        requestNotificationPermissionIfNeeded();

        View bottomNavigation = findViewById(R.id.bottomNavigation);

        View navHome = bottomNavigation.findViewById(R.id.navHome);
        View navPets = bottomNavigation.findViewById(R.id.navPets);
        View navRoutines = bottomNavigation.findViewById(R.id.navRoutines);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TasksRoutinesActivity.this,
                    HomeActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
        });

        navPets.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            TasksRoutinesActivity.this,
                            MyPetsActivity.class
                    )
            );
        });

        navRoutines.setOnClickListener(v -> {
            // Already on routines screen
        });

        adapter = new TaskAdapter(filteredTasks, this::onTaskClick, this::onTaskLongClick);
        rvTasks.setAdapter(adapter);

        load();
    }

    private void onTaskClick(Task task) {
        showTask(task);
    }

    private void onTaskLongClick(Task task) {
        taskMenu(task);
    }

    private void updateChipStyles() {

        chipAll.setBackgroundResource(
                filterMode.equals("all")
                        ? R.drawable.chip_filter_selected
                        : R.drawable.chip_filter_unselected
        );

        chipAll.setTextColor(
                filterMode.equals("all")
                        ? getColor(R.color.white)
                        : getColor(R.color.text_secondary)
        );

        chipPending.setBackgroundResource(
                filterMode.equals("pending")
                        ? R.drawable.chip_filter_selected
                        : R.drawable.chip_filter_unselected
        );

        chipPending.setTextColor(
                filterMode.equals("pending")
                        ? getColor(R.color.white)
                        : getColor(R.color.text_secondary)
        );

        chipCompleted.setBackgroundResource(
                filterMode.equals("completed")
                        ? R.drawable.chip_filter_selected
                        : R.drawable.chip_filter_unselected
        );

        chipCompleted.setTextColor(
                filterMode.equals("completed")
                        ? getColor(R.color.white)
                        : getColor(R.color.text_secondary)
        );
    }

    private void requestNotificationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT < 33) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        load();
    }

    private void load() {

        allTasks.clear();
        allTasks.addAll(db.getTasks(session.getUserId()));

        int pendingCount = 0;
        int completedCount = 0;

        for (Task t : allTasks) {

            if (t.isCompleted()) {
                completedCount++;
            } else {
                pendingCount++;
            }
        }

        pendingCountText.setText(String.valueOf(pendingCount));
        completedCountText.setText(String.valueOf(completedCount));
        totalCountText.setText(String.valueOf(allTasks.size()));

        applyFilter();
    }

    private void applyFilter() {

        filteredTasks.clear();

        for (Task t : allTasks) {

            if (filterMode.equals("pending") && t.isCompleted()) {
                continue;
            }

            if (filterMode.equals("completed") && !t.isCompleted()) {
                continue;
            }

            filteredTasks.add(t);
        }

        adapter.notifyDataSetChanged();

        boolean isEmpty = filteredTasks.isEmpty();

        findViewById(R.id.emptyContainer).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void taskMenu(Task t) {

        String markAction = t.isCompleted() ? getString(R.string.mark_pending) : getString(R.string.mark_done);

        new AlertDialog.Builder(this)

                .setTitle(t.getTitle())

                .setItems(
                        new String[]{

                                markAction,

                                getString(R.string.edit_task),

                                getString(R.string.delegate_sms),

                                getString(R.string.delete_task)

                        },

                        (dialog, which) -> {

                            if (which == 0) {

                                boolean newStatus = !t.isCompleted();

                                db.setTaskDone(
                                        t.getId(),
                                        session.getUserId(),
                                        newStatus
                                );

                                t.setCompleted(newStatus);

                                if (newStatus) {
                                    ReminderScheduler.cancel(this, t);
                                } else {
                                    ReminderScheduler.schedule(this, t);
                                }

                                load();

                            }

                            else if (which == 1) {

                                showTask(t);

                            }

                            else if (which == 2) {

                                delegate(t);

                            }

                            else {

                                ReminderScheduler.cancel(this, t);

                                db.deleteTask(
                                        t.getId(),
                                        session.getUserId()
                                );

                                load();
                            }
                        }
                )

                .show();
    }

    private void showTask(Task old) {

        View v = getLayoutInflater()
                .inflate(
                        R.layout.dialog_add_task,
                        null
                );

        EditText title =
                v.findViewById(
                        R.id.etTaskTitle
                );

        EditText desc =
                v.findViewById(
                        R.id.etTaskDesc
                );

        EditText date =
                v.findViewById(
                        R.id.etTaskDate
                );

        EditText time =
                v.findViewById(
                        R.id.etTaskTime
                );

        Spinner repeatSpinner =
                v.findViewById(
                        R.id.etTaskRepeat
                );

        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.repeat_types_array,
                android.R.layout.simple_spinner_dropdown_item
        );

        repeatSpinner.setAdapter(repeatAdapter);

        if (old != null) {

            title.setText(
                    old.getTitle()
            );

            desc.setText(
                    old.getDescription()
            );

            date.setText(
                    old.getDate()
            );

            time.setText(
                    old.getTime()
            );

            String rt = old.getRepeatType();

            if ("daily".equals(rt)) {
                repeatSpinner.setSelection(1);
            } else if ("weekly".equals(rt)) {
                repeatSpinner.setSelection(2);
            } else {
                repeatSpinner.setSelection(0);
            }
        }

        date.setOnClickListener(v2 -> {

            Calendar initial = Calendar.getInstance();

            try {

                String existing = date.getText().toString().trim();

                if (!existing.isEmpty()) {
                    initial.setTime(ReminderScheduler.DATE_FORMAT.parse(existing));
                }

            } catch (Exception ignored) { }

            new DatePickerDialog(
                    this,
                    (picker, year, month, dayOfMonth) -> {

                        Calendar picked = Calendar.getInstance();
                        picked.set(year, month, dayOfMonth);

                        date.setText(
                                ReminderScheduler.DATE_FORMAT.format(picked.getTime())
                        );
                    },
                    initial.get(Calendar.YEAR),
                    initial.get(Calendar.MONTH),
                    initial.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        time.setOnClickListener(v2 -> {

            Calendar initial = Calendar.getInstance();

            try {

                String existing = time.getText().toString().trim();

                if (!existing.isEmpty()) {
                    initial.setTime(ReminderScheduler.TIME_FORMAT.parse(existing));
                }

            } catch (Exception ignored) { }

            new TimePickerDialog(
                    this,
                    (picker, hourOfDay, minute) -> {

                        Calendar picked = Calendar.getInstance();
                        picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        picked.set(Calendar.MINUTE, minute);

                        time.setText(
                                ReminderScheduler.TIME_FORMAT.format(picked.getTime())
                        );
                    },
                    initial.get(Calendar.HOUR_OF_DAY),
                    initial.get(Calendar.MINUTE),
                    false
            ).show();
        });

        AlertDialog dlg =
                new AlertDialog.Builder(this)

                        .setTitle(
                                old == null
                                        ? R.string.dialog_title_new
                                        : R.string.dialog_title_edit
                        )

                        .setView(v)

                        .setNegativeButton(
                                R.string.btn_cancel,
                                null
                        )

                        .setPositiveButton(
                                old == null
                                        ? R.string.btn_add
                                        : R.string.btn_save,
                                null
                        )

                        .create();

        dlg.setOnShowListener(x -> {

            dlg.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(y -> {

                String titleText =
                        title.getText()
                                .toString()
                                .trim();

                if (titleText.isEmpty()) {

                    title.setError(
                            getString(R.string.error_title_required)
                    );

                    return;
                }

                String repeatSelection =
                        repeatSpinner.getSelectedItem().toString();

                String repeatType;
                if (repeatSelection.equals(getString(R.string.repeat_daily))) {
                    repeatType = "daily";
                } else if (repeatSelection.equals(getString(R.string.repeat_weekly))) {
                    repeatType = "weekly";
                } else {
                    repeatType = "none";
                }

                Task t;

                if (old == null) {
                    t = new Task(
                            0,
                            session.getUserId(),
                            titleText,
                            desc.getText().toString().trim(),
                            date.getText().toString().trim(),
                            time.getText().toString().trim(),
                            false
                    );
                } else {

                    t = old;

                    t.setTitle(titleText);

                    t.setDescription(
                            desc.getText()
                                    .toString()
                                    .trim()
                    );

                    t.setDate(
                            date.getText()
                                    .toString()
                                    .trim()
                    );

                    t.setTime(
                            time.getText()
                                    .toString()
                                    .trim()
                    );
                }

                t.setRepeatType(repeatType);

                if (old == null) {

                    long newId = db.addTask(t);

                    t.setId((int) newId);

                } else {

                    db.updateTask(t);
                }

                if (t.isCompleted()) {
                    ReminderScheduler.cancel(this, t);
                } else {
                    ReminderScheduler.schedule(this, t);
                }

                dlg.dismiss();

                load();
            });
        });

        dlg.show();
    }

    private void delegate(Task t) {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {

            Toast.makeText(
                    this,
                    getString(R.string.device_no_sms),
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        final EditText input =
                new EditText(this);

        input.setHint(
                getString(R.string.phone_hint_delegate)
        );

        input.setInputType(3);

        new AlertDialog.Builder(this)

                .setTitle(
                        getString(R.string.delegate_title)
                )

                .setMessage(
                        t.getTitle()
                                + " • "
                                + t.getDate()
                                + " "
                                + t.getTime()
                )

                .setView(input)

                .setNegativeButton(
                        R.string.btn_cancel,
                        null
                )

                .setPositiveButton(
                        getString(R.string.send_sms_button),
                        (dialog, which) -> {

                            String phone =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (phone.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        getString(R.string.enter_phone),
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            String msg =
                                    getString(R.string.sms_message, t.getTitle(), t.getDate(), t.getTime());

                            pendingSmsPhone = phone;
                            pendingSmsMessage = msg;

                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.SEND_SMS
                            ) == PackageManager.PERMISSION_GRANTED) {

                                sendSms(phone, msg);

                            } else {

                                smsPermissionLauncher.launch(
                                        Manifest.permission.SEND_SMS
                                );
                            }
                        }
                )

                .show();
    }

    private void sendSms(String phone, String message) {

        try {

            SmsManager smsManager = SmsManager.getDefault();

            ArrayList<String> parts = smsManager.divideMessage(message);

            smsManager.sendMultipartTextMessage(
                    phone,
                    null,
                    parts,
                    null,
                    null
            );

            Toast.makeText(
                    this,
                    getString(R.string.sms_sent, phone),
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    getString(R.string.sms_failed, e.getMessage()),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}