package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class welcome extends AppCompatActivity {
    private Button signInButton;
    private Button signUpButton;
    private View languageSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        signInButton = findViewById(R.id.sign_in_button);
        signUpButton = findViewById(R.id.sign_up_button);
        languageSet = findViewById(R.id.language_set);

        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(welcome.this, Sign_In_Activity.class);
            startActivity(intent);
        });

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(welcome.this, Sign_Up_Activity.class);
            startActivity(intent);
        });

        languageSet.setOnClickListener(v -> {
            // Implement language selection functionality
        });

    }
}