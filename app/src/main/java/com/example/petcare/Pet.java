package com.example.petcare;

public class Pet {

    private int id;
    private String userUid;

    private String name;
    private String breed;
    private String type;
    private String age;
    private String gender;
    private String weight;
    private String notes;
    private String photoPath;

    public Pet(
            int id,
            String userUid,
            String name,
            String breed,
            String type,
            String age,
            String gender,
            String weight,
            String notes,
            String photoPath
    ) {

        this.id = id;
        this.userUid = userUid;
        this.name = name;
        this.breed = breed;
        this.type = type;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.notes = notes;
        this.photoPath = photoPath;
    }

    public int getId() {
        return id;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getType() {
        return type;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getWeight() {
        return weight;
    }

    public String getNotes() {
        return notes;
    }

    public String getPhotoPath() {
        return photoPath;
    }
}