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
import com.example.authentication_uiux.models.user.LoginRequest;
import com.example.authentication_uiux.models.user.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private UserApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initializeViews();
        setupClickListeners();

        // Initialize Retrofit and ApiService
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.124.155:3000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(UserApi.class);
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

        backArrow.setOnClickListener(view -> {
            Intent intent = new Intent(Sign_In_Activity.this, welcome.class);
            startActivity(intent);
            finish(); // Optional: Close this activity
        });

        signUpLink.setOnClickListener(view -> {
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

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        Call<LoginResponse> call = apiService.loginUser(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    Toast.makeText(Sign_In_Activity.this, loginResponse.getMsg(), Toast.LENGTH_SHORT).show();
                    if (loginResponse.isSuccess()) {
                        // Save token and navigate to the main activity
                        String token = loginResponse.getToken();
                        // Save token in shared preferences or any secure storage
                        Intent intent = new Intent(Sign_In_Activity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(Sign_In_Activity.this, "Login failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(Sign_In_Activity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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