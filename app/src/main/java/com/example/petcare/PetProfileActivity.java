package com.example.petcare;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;

public class PetProfileActivity extends AppCompatActivity {

    public static final String EXTRA_PET_ID = "extra_pet_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pet_profile);

        int petId = getIntent().getIntExtra(EXTRA_PET_ID, -1);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        Pet pet = petId != -1
                ? databaseHelper.getPetById(petId)
                : null;

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (pet == null) {
            finish();
            return;
        }

        TextView profileName = findViewById(R.id.profileName);
        TextView profileTypeBreed = findViewById(R.id.profileTypeBreed);
        TextView profileAge = findViewById(R.id.profileAge);
        TextView profileGender = findViewById(R.id.profileGender);
        TextView profileWeight = findViewById(R.id.profileWeight);
        TextView profileNotes = findViewById(R.id.profileNotes);

        ShapeableImageView profilePhoto = findViewById(R.id.profilePhoto);
        TextView profilePhotoPlaceholder = findViewById(R.id.profilePhotoPlaceholder);

        profileName.setText(pet.getName());

        profileTypeBreed.setText(
                pet.getType() + " • " + pet.getBreed()
        );

        profileAge.setText(pet.getAge());
        profileGender.setText(pet.getGender());
        profileWeight.setText(pet.getWeight());

        profileNotes.setText(
                TextUtils.isEmpty(pet.getNotes())
                        ? "No notes added"
                        : pet.getNotes()
        );

        String photoPath = pet.getPhotoPath();

        if (photoPath != null && new File(photoPath).exists()) {

            profilePhoto.setImageBitmap(
                    BitmapFactory.decodeFile(photoPath)
            );

            profilePhoto.setVisibility(View.VISIBLE);
            profilePhotoPlaceholder.setVisibility(View.GONE);

        } else {

            profilePhoto.setVisibility(View.GONE);
            profilePhotoPlaceholder.setVisibility(View.VISIBLE);
        }
    }
}