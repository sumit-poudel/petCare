package com.example.petcare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.List;

public class PetAdapter
        extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    public interface OnPetClick {
        void onClick(Pet pet);
    }

    public interface OnPetDelete {
        void onDelete(Pet pet);
    }

    public interface OnPetView {
        void onView(Pet pet);
    }

    private final List<Pet> pets;
    private final OnPetClick editListener;
    private final OnPetDelete deleteListener;
    private final OnPetView viewListener;
    private final Context context;

    public PetAdapter(
            List<Pet> pets,
            OnPetClick editListener,
            OnPetDelete deleteListener,
            OnPetView viewListener,
            Context context
    ) {

        this.pets = pets;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.viewListener = viewListener;
        this.context = context;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_pet,
                                parent,
                                false
                        );

        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PetViewHolder holder,
            int position
    ) {

        Pet pet = pets.get(position);

        holder.name.setText(pet.getName());

        holder.details.setText(
                pet.getType()
                        + " • "
                        + pet.getBreed()
        );

        holder.info.setText(
                context.getString(R.string.pet_info_format, pet.getAge(), pet.getWeight())
        );

        String photoPath = pet.getPhotoPath();

        if (photoPath != null && new File(photoPath).exists()) {

            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

            holder.photo.setImageBitmap(bitmap);
            holder.photo.setVisibility(View.VISIBLE);
            holder.photoPlaceholder.setVisibility(View.GONE);

        } else {

            holder.photo.setVisibility(View.GONE);
            holder.photoPlaceholder.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(
                v -> editListener.onClick(pet)
        );

        holder.itemView.setOnLongClickListener(v -> {

            deleteListener.onDelete(pet);

            return true;
        });

        holder.photoContainer.setOnClickListener(
                v -> viewListener.onView(pet)
        );
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    static class PetViewHolder
            extends RecyclerView.ViewHolder {

        TextView name;
        TextView details;
        TextView info;
        ShapeableImageView photo;
        TextView photoPlaceholder;
        View photoContainer;

        public PetViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            name = itemView.findViewById(R.id.petName);
            details = itemView.findViewById(R.id.petDetails);
            info = itemView.findViewById(R.id.petInfo);
            photo = itemView.findViewById(R.id.petPhoto);
            photoPlaceholder = itemView.findViewById(R.id.petPhotoPlaceholder);
            photoContainer = itemView.findViewById(R.id.petPhotoContainer);
        }
    }
}