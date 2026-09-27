package com.example.petcare;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;

    private FirebaseAuthHelper firebaseAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        firebaseAuthHelper = new FirebaseAuthHelper();

        emailInput = findViewById(R.id.fpEmailInput);

        Button resetButton = findViewById(R.id.fpResetButton);
        TextView backToLogin = findViewById(R.id.fpBackToLogin);

        resetButton.setOnClickListener(v -> resetPassword());

        backToLogin.setOnClickListener(v -> finish());
    }

    private void resetPassword() {

        String email = emailInput.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.enter_valid_email));
            return;
        }

        firebaseAuthHelper.resetPassword(email, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                Toast.makeText(ForgotPasswordActivity.this, getString(R.string.password_reset_sent), Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ForgotPasswordActivity.this, getString(R.string.password_reset_failed) + ": " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}