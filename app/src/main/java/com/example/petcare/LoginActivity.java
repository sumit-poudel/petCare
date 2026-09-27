package com.example.petcare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;

    private FirebaseAuthHelper firebaseAuthHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        firebaseAuthHelper = new FirebaseAuthHelper();

        if (sessionManager.isLoggedIn() && firebaseAuthHelper.isLoggedIn()) {

            startActivity(
                    new Intent(this, HomeActivity.class)
            );

            finish();

            return;
        }

        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        Button loginButton = findViewById(R.id.loginButton);
        Button registerText = findViewById(R.id.registerText);
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);

        AnimUtils.bounceClick(loginButton);
        AnimUtils.bounceClick(registerText);

        loginButton.setOnClickListener(v -> login());

        registerText.setOnClickListener(v -> {

            startActivity(
                    new Intent(this, RegisterActivity.class)
            );

            AnimUtils.slideForward(this);
        });

        forgotPasswordText.setOnClickListener(v -> {

            startActivity(
                    new Intent(this, ForgotPasswordActivity.class)
            );

            AnimUtils.slideForward(this);
        });
    }

    private void login() {

        String email =
                emailInput.getText().toString().trim();

        String password =
                passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {

            emailInput.setError(getString(R.string.enter_email));
            return;
        }

        if (TextUtils.isEmpty(password)) {

            passwordInput.setError(getString(R.string.enter_password));
            return;
        }

        firebaseAuthHelper.loginWithEmailAndPassword(email, password, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                if (user != null) {
                    String name = user.getDisplayName();
                    if (name == null || name.isEmpty()) {
                        name = email.split("@")[0];
                    }
                    sessionManager.createSession(
                            user.getUid(),
                            name,
                            user.getEmail()
                    );

                    Toast.makeText(
                            LoginActivity.this,
                            getString(R.string.welcome_back),
                            Toast.LENGTH_SHORT
                    ).show();

                    startActivity(
                            new Intent(LoginActivity.this, HomeActivity.class)
                    );

                    AnimUtils.slideForward(LoginActivity.this);

                    finish();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(
                        LoginActivity.this,
                        getString(R.string.invalid_credentials) + ": " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}