package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class forgotPassword extends AppCompatActivity {
    private TextInputEditText emailInput;
    private MaterialButton sendEmailButton;
    private TextView signInLink;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        sendEmailButton = findViewById(R.id.sendEmailButton);
        backButton = findViewById(R.id.backButton);
        signInLink = findViewById(R.id.signInLink);
    }

    private void setupClickListeners() {
        sendEmailButton.setOnClickListener(v -> attemptSendRecoveryEmail());

        backButton.setOnClickListener(v -> finish());

        signInLink.setOnClickListener(v -> {
            Intent intent = new Intent(forgotPassword.this, Sign_In_Activity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptSendRecoveryEmail() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        // TODO: Implement actual email recovery logic here
        Toast.makeText(this, "Recovery email sent", Toast.LENGTH_SHORT).show();

        // Navigate to recovery password screen
        Intent intent = new Intent(forgotPassword.this, recoveryPassword.class);
        startActivity(intent);
    }
}