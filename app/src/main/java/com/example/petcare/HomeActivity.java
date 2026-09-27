package com.example.petcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;

    private TextView greetingText;
    private TextView petCountText;
    private TextView taskCountText;
    private TextView completedCountText;

    private LinearLayout petsStripContainer;
    private LinearLayout todaysRoutinesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {

            startActivity(
                    new Intent(this, LoginActivity.class)
            );

            finish();

            return;
        }

        setContentView(R.layout.activity_home);

        databaseHelper = new DatabaseHelper(this);

        greetingText = findViewById(R.id.greetingText);
        petCountText = findViewById(R.id.petCountText);
        taskCountText = findViewById(R.id.taskCountText);
        completedCountText = findViewById(R.id.completedCountText);

        petsStripContainer = findViewById(R.id.petsStripContainer);
        todaysRoutinesContainer = findViewById(R.id.todaysRoutinesContainer);

        TextView addPetQuickLink = findViewById(R.id.addPetQuickLink);
        TextView seeAllRoutinesLink = findViewById(R.id.seeAllRoutinesLink);
        View quickAddRoutineButton = findViewById(R.id.quickAddRoutineButton);
        View logoutButton = findViewById(R.id.logoutButton);

        AnimUtils.bounceClick(quickAddRoutineButton);
        AnimUtils.bounceClick(logoutButton);


        View navHome = findViewById(R.id.navHome);
        View navPets = findViewById(R.id.navPets);
        View navRoutines = findViewById(R.id.navRoutines);

        greetingText.setText(
                "Hello, " + sessionManager.getName() + "!"
        );

        addPetQuickLink.setOnClickListener(v -> openPets());
        navPets.setOnClickListener(v -> openPets());

        seeAllRoutinesLink.setOnClickListener(v -> openRoutines());
        navRoutines.setOnClickListener(v -> openRoutines());

        quickAddRoutineButton.setOnClickListener(v ->
                startActivity(new Intent(this, CreateRoutineActivity.class))
        );

        navHome.setOnClickListener(v -> { /* already on Home */ });

        logoutButton.setOnClickListener(v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!sessionManager.isLoggedIn()) {
            return;
        }

        String userUid = sessionManager.getUserId();

        petCountText.setText(
                String.valueOf(databaseHelper.getPetCount(userUid))
        );

        taskCountText.setText(
                String.valueOf(databaseHelper.getTaskCount(userUid))
        );

        completedCountText.setText(
                String.valueOf(databaseHelper.getCompletedTaskCount(userUid))
        );

        loadPetsStrip(userUid);
        loadTodaysRoutines(userUid);
    }

    private void loadPetsStrip(String userUid) {

        petsStripContainer.removeAllViews();

        List<Pet> pets = databaseHelper.getPets(userUid);

        if (pets.isEmpty()) {

            TextView empty = new TextView(this);
            empty.setText("No pets added yet");
            empty.setTextColor(getColor(R.color.text_secondary));
            empty.setTextSize(13);
            petsStripContainer.addView(empty);

            return;
        }

        for (Pet pet : pets) {

            TextView chip = new TextView(this);

            chip.setText(petEmoji(pet.getType()) + "  " + pet.getName());
            chip.setTextColor(getColor(R.color.primary));
            chip.setTextSize(14);
            chip.setBackgroundResource(R.drawable.chip_pet);
            chip.setPadding(28, 18, 28, 18);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(12);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> openPets());

            petsStripContainer.addView(chip);
        }
    }

    private void loadTodaysRoutines(String userUid) {

        todaysRoutinesContainer.removeAllViews();

        List<Task> tasks = databaseHelper.getTasks(userUid);

        List<Task> pending = new ArrayList<>();

        for (Task t : tasks) {
            if (!t.isCompleted()) {
                pending.add(t);
            }
        }

        if (pending.isEmpty()) {

            TextView empty = new TextView(this);
            empty.setText("No pending routines. You're all caught up!");
            empty.setTextColor(getColor(R.color.text_secondary));
            empty.setTextSize(13);
            empty.setPadding(4, 8, 4, 8);
            todaysRoutinesContainer.addView(empty);

            return;
        }

        int limit = Math.min(3, pending.size());

        for (int i = 0; i < limit; i++) {

            Task task = pending.get(i);

            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_task_row, todaysRoutinesContainer, false);

            CheckBox check = row.findViewById(R.id.rowCheck);
            TextView title = row.findViewById(R.id.rowTitle);
            TextView subtitle = row.findViewById(R.id.rowSubtitle);
            TextView time = row.findViewById(R.id.rowTime);

            check.setChecked(false);
            title.setText(task.getTitle());
            subtitle.setText(task.getDate());
            time.setText(task.getTime());

            View missedBadge = row.findViewById(R.id.rowMissedBadge);

            missedBadge.setVisibility(
                    TaskTimeUtils.isOverdue(task) ? View.VISIBLE : View.GONE
            );

            row.setOnClickListener(v -> openRoutines());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = 10;
            row.setLayoutParams(params);

            todaysRoutinesContainer.addView(row);
        }
    }

    private String petEmoji(String type) {

        if (type == null) {
            return "🐾";
        }

        switch (type) {
            case "Dog":
                return "🐶";
            case "Cat":
                return "🐱";
            case "Bird":
                return "🐦";
            case "Rabbit":
                return "🐰";
            default:
                return "🐾";
        }
    }

    private void openPets() {
        startActivity(new Intent(this, MyPetsActivity.class));
    }

    private void openRoutines() {
        startActivity(new Intent(this, TasksRoutinesActivity.class));
    }

    private void logout() {

        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}