package com.example.petcare;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseAuthHelper {

    private static final String TAG = "FirebaseAuthHelper";
    private final FirebaseAuth auth;

    public FirebaseAuthHelper() {
        auth = FirebaseAuth.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public void registerWithEmailAndPassword(String email, String password, String name, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && name != null && !name.isEmpty()) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d(TAG, "User profile updated");
                                        }
                                        callback.onSuccess(user);
                                    });
                        } else {
                            callback.onSuccess(user);
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Log.e(TAG, "Registration failed: " + error);
                        callback.onFailure(error);
                    }
                });
    }

    public void loginWithEmailAndPassword(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Log.e(TAG, "Login failed: " + error);
                        callback.onFailure(error);
                    }
                });
    }

    public void resetPassword(String email, AuthCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Password reset failed";
                        Log.e(TAG, "Password reset failed: " + error);
                        callback.onFailure(error);
                    }
                });
    }

    public void logout() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    public String getCurrentUserName() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getDisplayName() : null;
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}