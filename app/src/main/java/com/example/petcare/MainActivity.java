package com.example.petcare;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager =
                new SessionManager(this);

        if (sessionManager.isLoggedIn()) {

            startActivity(
                    new Intent(
                            this,
                            HomeActivity.class
                    )
            );

        } else {

            startActivity(
                    new Intent(
                            this,
                            LoginActivity.class
                    )
            );
        }

        finish();
    }
}