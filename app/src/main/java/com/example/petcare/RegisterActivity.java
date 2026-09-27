package com.example.petcare;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;

    private FirebaseAuthHelper firebaseAuthHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        firebaseAuthHelper = new FirebaseAuthHelper();
        sessionManager = new SessionManager(this);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput =
                findViewById(R.id.confirmPasswordInput);

        Button registerButton =
                findViewById(R.id.registerButton);

        TextView loginText =
                findViewById(R.id.loginText);

        AnimUtils.bounceClick(registerButton);

        registerButton.setOnClickListener(
                v -> register()
        );

        loginText.setOnClickListener(v -> {

            finish();

            AnimUtils.slideBack(this);
        });
    }

    private void register() {

        String name =
                nameInput.getText().toString().trim();

        String email =
                emailInput.getText().toString().trim();

        String phone =
                phoneInput.getText().toString().trim();

        String password =
                passwordInput.getText().toString();

        String confirm =
                confirmPasswordInput.getText().toString();

        if (TextUtils.isEmpty(name)) {

            nameInput.setError(getString(R.string.enter_name));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            emailInput.setError(getString(R.string.enter_valid_email));
            return;
        }

        if (password.length() < 6) {

            passwordInput.setError(
                    getString(R.string.password_min_length)
            );

            return;
        }

        if (!password.equals(confirm)) {

            confirmPasswordInput.setError(
                    getString(R.string.passwords_mismatch)
            );

            return;
        }

        firebaseAuthHelper.registerWithEmailAndPassword(email, password, name, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                if (user != null) {
                    sessionManager.createSession(
                            user.getUid(),
                            name,
                            user.getEmail()
                    );

                    Toast.makeText(
                            RegisterActivity.this,
                            getString(R.string.account_created),
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                    AnimUtils.slideBack(RegisterActivity.this);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(
                        RegisterActivity.this,
                        getString(R.string.email_exists) + ": " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}