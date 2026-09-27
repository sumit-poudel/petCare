package com.example.petcare;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MyPetsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private PetAdapter adapter;
    private RecyclerView recyclerView;

    private ShapeableImageView dialogPhotoPreview;
    private TextView dialogPhotoPlaceholder;
    private String pendingPhotoPath;

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri == null) {
                            return;
                        }

                        String path = copyImageToInternalStorage(uri);

                        if (path == null) {
                            return;
                        }

                        pendingPhotoPath = path;

                        if (dialogPhotoPreview != null) {

                            dialogPhotoPreview.setImageBitmap(
                                    BitmapFactory.decodeFile(path)
                            );

                            dialogPhotoPreview.setVisibility(View.VISIBLE);
                            dialogPhotoPlaceholder.setVisibility(View.GONE);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_pets);

        databaseHelper =
                new DatabaseHelper(this);

        sessionManager =
                new SessionManager(this);

        recyclerView =
                findViewById(R.id.petsRecyclerView);

        Button addPetButton =
                findViewById(R.id.addPetButton);
        Button emptyAddButton =
                findViewById(R.id.emptyAddButton);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        addPetButton.setOnClickListener(
                v -> showPetDialog(null)
        );
        emptyAddButton.setOnClickListener(
                v -> showPetDialog(null)
        );

        View bottomNavigation = findViewById(R.id.bottomNavigation);

        View navHome = bottomNavigation.findViewById(R.id.navHome);
        View navPets = bottomNavigation.findViewById(R.id.navPets);
        View navRoutines = bottomNavigation.findViewById(R.id.navRoutines);

        navHome.setOnClickListener(v -> {
            Intent i = new Intent(this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        navPets.setOnClickListener(v -> {
            // Already on pets screen
        });

        navRoutines.setOnClickListener(v ->
                startActivity(new Intent(this, TasksRoutinesActivity.class))
        );

        loadPets();
    }


    private void loadPets() {

        List<Pet> pets =
                databaseHelper.getPets(
                        sessionManager.getUserId()
                );

        // Update stats card
        TextView petCountText = findViewById(R.id.petCountText);
        TextView taskCountText = findViewById(R.id.taskCountText);
        petCountText.setText(String.valueOf(pets.size()));
        taskCountText.setText(String.valueOf(databaseHelper.getTaskCount(sessionManager.getUserId())));

        adapter =
                new PetAdapter(
                        pets,
                        this::showPetDialog,
                        this::deletePet,
                        this::openPetProfile,
                        this
                );

        recyclerView.setAdapter(adapter);
        recyclerView.scheduleLayoutAnimation();

        // Handle empty state
        boolean isEmpty = pets.isEmpty();
        findViewById(R.id.emptyContainer).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openPetProfile(Pet pet) {

        Intent intent = new Intent(this, PetProfileActivity.class);

        intent.putExtra(PetProfileActivity.EXTRA_PET_ID, pet.getId());

        startActivity(intent);
    }


    private void showPetDialog(Pet pet) {

        View view =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_pet,
                                null
                        );

        EditText name =
                view.findViewById(R.id.petNameInput);

        EditText breed =
                view.findViewById(R.id.petBreedInput);

        Spinner type =
                view.findViewById(R.id.petTypeSpinner);

        EditText age =
                view.findViewById(R.id.petAgeInput);

        Spinner gender =
                view.findViewById(R.id.petGenderSpinner);

        EditText weight =
                view.findViewById(R.id.petWeightInput);

        EditText notes =
                view.findViewById(R.id.petNotesInput);

        dialogPhotoPreview = view.findViewById(R.id.petPhotoPreview);
        dialogPhotoPlaceholder = view.findViewById(R.id.petPhotoPlaceholder);
        TextView changePhotoText = view.findViewById(R.id.changePhotoText);

        pendingPhotoPath = pet != null ? pet.getPhotoPath() : null;

        if (pendingPhotoPath != null && new File(pendingPhotoPath).exists()) {

            dialogPhotoPreview.setImageBitmap(
                    BitmapFactory.decodeFile(pendingPhotoPath)
            );

            dialogPhotoPreview.setVisibility(View.VISIBLE);
            dialogPhotoPlaceholder.setVisibility(View.GONE);

        } else {

            dialogPhotoPreview.setVisibility(View.GONE);
            dialogPhotoPlaceholder.setVisibility(View.VISIBLE);
        }

        changePhotoText.setOnClickListener(v ->
                photoPickerLauncher.launch("image/*")
        );

        setupSpinner(
                type,
                getResources().getStringArray(R.array.pet_types_array)
        );

        setupSpinner(
                gender,
                getResources().getStringArray(R.array.pet_genders_array)
        );

        if (pet != null) {

            name.setText(pet.getName());
            breed.setText(pet.getBreed());
            age.setText(pet.getAge());
            weight.setText(pet.getWeight());
            notes.setText(pet.getNotes());

            setSpinnerValue(type, pet.getType());
            setSpinnerValue(gender, pet.getGender());
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                pet == null
                                        ? R.string.dialog_title_add_pet
                                        : R.string.dialog_title_edit_pet
                        )
                        .setView(view)
                        .setNegativeButton(
                                R.string.btn_cancel,
                                null
                        )
                        .setPositiveButton(
                                pet == null
                                        ? R.string.btn_add
                                        : R.string.btn_save,
                                null
                        )
                        .create();

        dialog.setOnDismissListener(d -> {
            dialogPhotoPreview = null;
            dialogPhotoPlaceholder = null;
        });

        dialog.setOnShowListener(d -> {

            Button positive =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            positive.setOnClickListener(v -> {

                String petName =
                        name.getText()
                                .toString()
                                .trim();

                if (TextUtils.isEmpty(petName)) {

                    name.setError(
                            getString(R.string.pet_name_required)
                    );

                    return;
                }

                String petBreed =
                        breed.getText()
                                .toString()
                                .trim();

                String petType =
                        type.getSelectedItem()
                                .toString();

                String petAge =
                        age.getText()
                                .toString()
                                .trim();

                String petGender =
                        gender.getSelectedItem()
                                .toString();

                String petWeight =
                        weight.getText()
                                .toString()
                                .trim();

                String petNotes =
                        notes.getText()
                                .toString()
                                .trim();

                if (pet == null) {

                    databaseHelper.addPet(
                            sessionManager.getUserId(),
                            petName,
                            petBreed,
                            petType,
                            petAge,
                            petGender,
                            petWeight,
                            petNotes,
                            pendingPhotoPath
                    );

                    Toast.makeText(
                            this,
                            getString(R.string.pet_added),
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    databaseHelper.updatePet(
                            pet.getId(),
                            petName,
                            petBreed,
                            petType,
                            petAge,
                            petGender,
                            petWeight,
                            petNotes,
                            pendingPhotoPath
                    );

                    Toast.makeText(
                            this,
                            getString(R.string.pet_updated),
                            Toast.LENGTH_SHORT
                    ).show();
                }

                dialog.dismiss();

                loadPets();
            });
        });

        dialog.show();
    }

    private void deletePet(Pet pet) {

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_pet_title)
                .setMessage(
                        getString(R.string.delete_pet_confirm, pet.getName())
                )
                .setNegativeButton(
                        R.string.btn_cancel,
                        null
                )
                .setPositiveButton(
                        R.string.delete_task,
                        (dialog, which) -> {

                            databaseHelper.deletePet(
                                    pet.getId()
                            );

                            loadPets();

                            Toast.makeText(
                                    this,
                                    getString(R.string.pet_deleted),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    private void setupSpinner(
            Spinner spinner,
            String[] items
    ) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        items
                );

        spinner.setAdapter(adapter);
    }

    private void setSpinnerValue(
            Spinner spinner,
            String value
    ) {

        ArrayAdapter adapter =
                (ArrayAdapter) spinner.getAdapter();

        int position =
                adapter.getPosition(value);

        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private String copyImageToInternalStorage(Uri sourceUri) {

        try {

            InputStream input = getContentResolver().openInputStream(sourceUri);

            if (input == null) {
                return null;
            }

            File dir = new File(getFilesDir(), "pet_photos");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File outFile = new File(
                    dir,
                    "pet_" + System.currentTimeMillis() + ".jpg"
            );

            OutputStream output = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            output.flush();
            output.close();
            input.close();

            return outFile.getAbsolutePath();

        } catch (IOException e) {

            Toast.makeText(
                    this,
                    "Could not load the selected photo",
                    Toast.LENGTH_SHORT
            ).show();

            return null;
        }
    }
}