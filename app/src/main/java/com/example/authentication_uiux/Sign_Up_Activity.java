package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Sign_Up_Activity extends AppCompatActivity {
    private EditText fullNameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private CheckBox termsCheckbox;
    private Button signUpButton;
    private CardView googleSignUp;
    private CardView facebookSignUp;
    private View backArrow;
    private TextView signInLink;
    private TextView policyLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        setupClickListeners();
    }
    private void initializeViews() {
        fullNameInput = findViewById(R.id.Username);
        emailInput = findViewById(R.id.Mail);
        passwordInput = findViewById(R.id.Pass);
        confirmPasswordInput = findViewById(R.id.Confirm);
        termsCheckbox = findViewById(R.id.checkBox);
        signUpButton = findViewById(R.id.buttonSignUp);
        googleSignUp = findViewById(R.id.cardViewGG);
        facebookSignUp = findViewById(R.id.cardViewFB);
        backArrow = findViewById(R.id.arrow);
        signInLink = findViewById(R.id.Have2);
        policyLink = findViewById(R.id.Policy); // Giả sử ID của TextView là termPolicy
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> attemptSignUp());

        backArrow.setOnClickListener(v -> finish());

        signInLink.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_Up_Activity.this, Sign_In_Activity.class);
            startActivity(intent);
            finish();
        });

        policyLink.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_Up_Activity.this, Term_Policy_Main.class);
            startActivity(intent);
        });

        googleSignUp.setOnClickListener(v -> handleGoogleSignUp());
        facebookSignUp.setOnClickListener(v -> handleFacebookSignUp());
    }

    private void attemptSignUp() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            fullNameInput.setError("Full name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Confirm password is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement actual sign up logic here
        Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
    }

    private void handleGoogleSignUp() {
        // TODO: Implement Google Sign Up
        Toast.makeText(this, "Google Sign Up clicked", Toast.LENGTH_SHORT).show();
    }

    private void handleFacebookSignUp() {
        // TODO: Implement Facebook Sign Up
        Toast.makeText(this, "Facebook Sign Up clicked", Toast.LENGTH_SHORT).show();
    }
}