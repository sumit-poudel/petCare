package com.example.petcare;

public class Task {

    private int id;
    private String userUid;

    private String title;
    private String description;
    private String date;
    private String time;

    private boolean completed;
    private String repeatType;

    public Task(
            int id,
            String userUid,
            String title,
            String description,
            String date,
            String time,
            boolean completed
    ) {

        this.id = id;
        this.userUid = userUid;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.completed = completed;
        this.repeatType = "none";
    }

    public int getId() {
        return id;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getRepeatType() {
        return repeatType == null ? "none" : repeatType;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }
}