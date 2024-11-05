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

public class Sign_In_Activity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private CheckBox rememberMeCheckbox;
    private Button signInButton;
    private TextView forgotPasswordText;
    private CardView googleSignIn;
    private CardView facebookSignIn;
    private View backArrow;
    private TextView signUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initializeViews();
        setupClickListeners();

    }
    private void initializeViews() {
        emailInput = findViewById(R.id.Mail);
        passwordInput = findViewById(R.id.Pass);
        rememberMeCheckbox = findViewById(R.id.checkBox);
        signInButton = findViewById(R.id.buttonSignIn);
        forgotPasswordText = findViewById(R.id.Policy);
        googleSignIn = findViewById(R.id.cardViewGG);
        facebookSignIn = findViewById(R.id.cardViewFB);
        backArrow = findViewById(R.id.arrow);
        signUpLink = findViewById(R.id.Have2);
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> attemptLogin());

        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_In_Activity.this, forgotPassword.class);
            startActivity(intent);
        });

        backArrow.setOnClickListener(v -> finish());

        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_In_Activity.this, Sign_Up_Activity.class);
            startActivity(intent);
        });

        googleSignIn.setOnClickListener(v -> handleGoogleSignIn());
        facebookSignIn.setOnClickListener(v -> handleFacebookSignIn());
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        // TODO: Implement actual login logic here
        // For now, just show a success message
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
    }

    private void handleGoogleSignIn() {
        // TODO: Implement Google Sign In
        Toast.makeText(this, "Google Sign In clicked", Toast.LENGTH_SHORT).show();
    }

    private void handleFacebookSignIn() {
        // TODO: Implement Facebook Sign In
        Toast.makeText(this, "Facebook Sign In clicked", Toast.LENGTH_SHORT).show();
    }
}