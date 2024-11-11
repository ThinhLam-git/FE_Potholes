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

import Controller.ApiService;
import Controller.RetrofitClient;
import Model.AuthResponse;
import Model.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        policyLink = findViewById(R.id.Policy);

        signUpButton.setOnClickListener(v -> registerUser());
    }


    private void registerUser() {
        String username = fullNameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        RegisterRequest request = new RegisterRequest(username,email ,password);

        ApiService apiService = RetrofitClient.getApiService();
        Call<AuthResponse> call = apiService.register(request);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Sign_Up_Activity.this, "Register Successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Sign_Up_Activity.this, "Register Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(Sign_Up_Activity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> attemptSignUp());

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sign_Up_Activity.this, welcome.class);
                startActivity(intent);
                finish();
            }
        });

        signInLink.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_Up_Activity.this, Sign_In_Activity.class);
            startActivity(intent);
        });

        policyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Sign_Up_Activity.this, Term_Policy_Main.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Sign_Up_Activity.this, "Failed to open Terms and Policy page", Toast.LENGTH_SHORT).show();
                }
            }
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
        Intent intent = new Intent(Sign_Up_Activity.this, Sign_In_Activity.class);
        startActivity(intent);
        finish();
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