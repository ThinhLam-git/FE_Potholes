// Sign_Up_Activity.java
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.authentication_uiux.API.UserApi;
import com.example.authentication_uiux.models.user.SignUpRequest;
import com.example.authentication_uiux.models.user.SignUpResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

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
    private UserApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        setupClickListeners();

        // Initialize Retrofit and ApiService
        Retrofit retrofit = RetrofitClient.getClient();
        apiService = retrofit.create(UserApi.class);
    }

    private void initializeViews() {
        fullNameInput = findViewById(R.id.Username);
        emailInput = findViewById(R.id.Mail);
        passwordInput = findViewById(R.id.Pass);
        confirmPasswordInput = findViewById(R.id.ConfirmPass);
        termsCheckbox = findViewById(R.id.checkBox);
        signUpButton = findViewById(R.id.buttonSignUp);
        googleSignUp = findViewById(R.id.cardViewGG);
        facebookSignUp = findViewById(R.id.cardViewFB);
        backArrow = findViewById(R.id.arrow);
        signInLink = findViewById(R.id.Have2);
        policyLink = findViewById(R.id.Policy);
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> attemptSignUp());

        backArrow.setOnClickListener(view -> {
            Intent intent = new Intent(Sign_Up_Activity.this, welcome.class);
            startActivity(intent);
            finish();
        });

        signInLink.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_Up_Activity.this, Sign_In_Activity.class);
            startActivity(intent);
        });

        policyLink.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(Sign_Up_Activity.this, Term_Policy_Main.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(Sign_Up_Activity.this, "Failed to open Terms and Policy page", Toast.LENGTH_SHORT).show();
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

        signUpUser(fullName, email, password, confirmPassword);
    }

    private void signUpUser(String fullName, String email, String password, String confirmPassword) {
    SignUpRequest signUpRequest = new SignUpRequest();
    signUpRequest.setEmail(email);
    signUpRequest.setUsername(fullName);
    signUpRequest.setPassword(password);
    signUpRequest.setCheckpassword(confirmPassword);
    signUpRequest.setRole("user"); // or "admin" based on your requirement

    Call<SignUpResponse> call = apiService.signUpUser(signUpRequest);
    call.enqueue(new Callback<SignUpResponse>() {
        @Override
        public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
            if (response.isSuccessful()) {
                SignUpResponse signUpResponse = response.body();
                Toast.makeText(Sign_Up_Activity.this, signUpResponse.getMsg(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Sign_Up_Activity.this, Sign_In_Activity.class);
                startActivity(intent);
                finish();
            } else {
                try {
                    String errorBody = response.errorBody().string();
                    Toast.makeText(Sign_Up_Activity.this, "Sign up failed: " + errorBody, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(Sign_Up_Activity.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<SignUpResponse> call, Throwable t) {
            Toast.makeText(Sign_Up_Activity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
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