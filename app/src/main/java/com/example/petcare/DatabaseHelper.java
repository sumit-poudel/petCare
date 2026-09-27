package com.example.petcare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "petcare.db";
    private static final int DATABASE_VERSION = 6;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE pets (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_uid TEXT NOT NULL," +
                        "name TEXT NOT NULL," +
                        "breed TEXT," +
                        "type TEXT," +
                        "age TEXT," +
                        "gender TEXT," +
                        "weight TEXT," +
                        "notes TEXT," +
                        "photo_path TEXT)"
        );

        db.execSQL(
                "CREATE TABLE tasks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_uid TEXT NOT NULL," +
                        "title TEXT NOT NULL," +
                        "description TEXT," +
                        "date TEXT," +
                        "time TEXT," +
                        "is_completed INTEGER DEFAULT 0," +
                        "repeat_type TEXT DEFAULT 'none')"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        if (oldVersion < 6) {
            db.execSQL("DROP TABLE IF EXISTS pets");
            db.execSQL("DROP TABLE IF EXISTS tasks");
            onCreate(db);
        }

        if (oldVersion < 4) {

            try {
                db.execSQL("ALTER TABLE pets ADD COLUMN photo_path TEXT");
            } catch (SQLException e) {
            }
        }

        if (oldVersion < 5) {

            try {
                db.execSQL("ALTER TABLE tasks ADD COLUMN repeat_type TEXT DEFAULT 'none'");
            } catch (SQLException e) {
            }
        }
    }

    public long addPet(
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

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("user_uid", userUid);
        values.put("name", name);
        values.put("breed", breed);
        values.put("type", type);
        values.put("age", age);
        values.put("gender", gender);
        values.put("weight", weight);
        values.put("notes", notes);
        values.put("photo_path", photoPath);

        return db.insert("pets", null, values);
    }

    public int updatePet(
            int id,
            String name,
            String breed,
            String type,
            String age,
            String gender,
            String weight,
            String notes,
            String photoPath
    ) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("breed", breed);
        values.put("type", type);
        values.put("age", age);
        values.put("gender", gender);
        values.put("weight", weight);
        values.put("notes", notes);
        values.put("photo_path", photoPath);

        return db.update(
                "pets",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deletePet(int id) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                "pets",
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public List<Pet> getPets(String userUid) {

        List<Pet> pets = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "pets",
                null,
                "user_uid=?",
                new String[]{userUid},
                null,
                null,
                "id DESC"
        );

        while (cursor.moveToNext()) {

            pets.add(
                    new Pet(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("user_uid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("breed")),
                            cursor.getString(cursor.getColumnIndexOrThrow("type")),
                            cursor.getString(cursor.getColumnIndexOrThrow("age")),
                            cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                            cursor.getString(cursor.getColumnIndexOrThrow("weight")),
                            cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                            cursor.getString(cursor.getColumnIndexOrThrow("photo_path"))
                    )
            );
        }

        cursor.close();

        return pets;
    }

    public Pet getPetById(int id) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "pets",
                null,
                "id=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Pet pet = null;

        if (cursor.moveToFirst()) {

            pet = new Pet(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("user_uid")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("breed")),
                    cursor.getString(cursor.getColumnIndexOrThrow("type")),
                    cursor.getString(cursor.getColumnIndexOrThrow("age")),
                    cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                    cursor.getString(cursor.getColumnIndexOrThrow("weight")),
                    cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                    cursor.getString(cursor.getColumnIndexOrThrow("photo_path"))
            );
        }

        cursor.close();

        return pet;
    }

    public int getPetCount(String userUid) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM pets WHERE user_uid=?",
                new String[]{userUid}
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    public long addTask(
            String userUid,
            String title,
            String description,
            String date,
            String time,
            String repeatType
    ) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("user_uid", userUid);
        values.put("title", title);
        values.put("description", description);
        values.put("date", date);
        values.put("time", time);
        values.put("is_completed", 0);
        values.put("repeat_type", repeatType == null ? "none" : repeatType);

        return db.insert("tasks", null, values);
    }

    public long addTask(
            String userUid,
            String title,
            String description,
            String date,
            String time
    ) {
        return addTask(userUid, title, description, date, time, "none");
    }

    public long addTask(Task task) {

        return addTask(
                task.getUserUid(),
                task.getTitle(),
                task.getDescription(),
                task.getDate(),
                task.getTime(),
                task.getRepeatType()
        );
    }

    public int updateTask(
            int id,
            String title,
            String description,
            String date,
            String time,
            String repeatType
    ) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("title", title);
        values.put("description", description);
        values.put("date", date);
        values.put("time", time);
        values.put("repeat_type", repeatType == null ? "none" : repeatType);

        return db.update(
                "tasks",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int updateTask(
            int id,
            String title,
            String description,
            String date,
            String time
    ) {
        return updateTask(id, title, description, date, time, "none");
    }

    public int updateTask(Task task) {

        return updateTask(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDate(),
                task.getTime(),
                task.getRepeatType()
        );
    }

    public int updateTaskDate(int id, String date) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("date", date);

        return db.update(
                "tasks",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deleteTask(int id) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                "tasks",
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int setTaskCompleted(
            int id,
            boolean completed
    ) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                "is_completed",
                completed ? 1 : 0
        );

        return db.update(
                "tasks",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public List<Task> getTasks(String userUid) {

        List<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "tasks",
                null,
                "user_uid=?",
                new String[]{userUid},
                null,
                null,
                "date ASC, time ASC"
        );

        while (cursor.moveToNext()) {

            Task task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("user_uid")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("time")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1
            );

            int repeatIdx = cursor.getColumnIndex("repeat_type");

            if (repeatIdx != -1) {
                task.setRepeatType(cursor.getString(repeatIdx));
            }

            tasks.add(task);
        }

        cursor.close();

        return tasks;
    }

    public Task getTaskById(int id) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "tasks",
                null,
                "id=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Task task = null;

        if (cursor.moveToFirst()) {

            task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("user_uid")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("time")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1
            );

            task.setRepeatType(cursor.getString(cursor.getColumnIndexOrThrow("repeat_type")));
        }

        cursor.close();

        return task;
    }

    public List<Task> getAllIncompleteTasks() {

        List<Task> result = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "tasks",
                null,
                "is_completed=0",
                null, null, null, null
        );

        while (cursor.moveToNext()) {

            Task task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("user_uid")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("time")),
                    false
            );

            task.setRepeatType(cursor.getString(cursor.getColumnIndexOrThrow("repeat_type")));

            result.add(task);
        }

        cursor.close();

        return result;
    }

    public int getTaskCount(String userUid) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM tasks WHERE user_uid=?",
                new String[]{userUid}
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    public int getCompletedTaskCount(String userUid) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM tasks WHERE user_uid=? AND is_completed=1",
                new String[]{userUid}
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    public boolean setTaskDone(int taskId, String userUid, boolean completed) {
        return updateTaskStatus(taskId, completed);
    }

    private boolean updateTaskStatus(int taskId, boolean completed) {
        return setTaskCompleted(taskId, completed) > 0;
    }

    public boolean deleteTask(int taskId, String userUid) {
        return deleteTask(taskId) > 0;
    }
}