package com.example.petcare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class CreateRoutineActivity
        extends AppCompatActivity {

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText dateInput;
    private EditText timeInput;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_create_routine
        );

        databaseHelper =
                new DatabaseHelper(this);

        sessionManager =
                new SessionManager(this);

        titleInput =
                findViewById(
                        R.id.titleInput
                );

        descriptionInput =
                findViewById(
                        R.id.descriptionInput
                );

        dateInput =
                findViewById(
                        R.id.dateInput
                );

        timeInput =
                findViewById(
                        R.id.timeInput
                );

        Button saveButton =
                findViewById(
                        R.id.saveRoutineButton
                );

        dateInput.setOnClickListener(
                v -> showDatePicker()
        );

        timeInput.setOnClickListener(
                v -> showTimePicker()
        );

        saveButton.setOnClickListener(
                v -> saveRoutine()
        );
    }

    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, day) -> {

                            String date =
                                    String.format(
                                            Locale.getDefault(),
                                            "%04d-%02d-%02d",
                                            year,
                                            month + 1,
                                            day
                                    );

                            dateInput.setText(date);
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }

    private void showTimePicker() {

        Calendar calendar =
                Calendar.getInstance();

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view, hour, minute) -> {

                            String time =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d",
                                            hour,
                                            minute
                                    );

                            timeInput.setText(time);
                        },
                        calendar.get(
                                Calendar.HOUR_OF_DAY
                        ),
                        calendar.get(
                                Calendar.MINUTE
                        ),
                        true
                );

        dialog.show();
    }

    private void saveRoutine() {

        String title =
                titleInput.getText()
                        .toString()
                        .trim();

        String description =
                descriptionInput.getText()
                        .toString()
                        .trim();

        String date =
                dateInput.getText()
                        .toString()
                        .trim();

        String time =
                timeInput.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(title)) {

            titleInput.setError(
                    getString(R.string.error_title_required)
            );

            return;
        }

        if (TextUtils.isEmpty(date)) {

            dateInput.setError(
                    getString(R.string.hint_choose_date)
            );

            return;
        }

        if (TextUtils.isEmpty(time)) {

            timeInput.setError(
                    getString(R.string.hint_choose_time)
            );

            return;
        }

        long result =
                databaseHelper.addTask(
                        sessionManager.getUserId(),
                        title,
                        description,
                        date,
                        time
                );

        if (result != -1) {

            Toast.makeText(
                    this,
                    getString(R.string.routine_created),
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    getString(R.string.pet_update_failed),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}