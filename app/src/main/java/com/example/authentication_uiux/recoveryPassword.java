package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.authentication_uiux.API.UserApi;
import com.example.authentication_uiux.models.user.ChangePasswordRequest;
import com.example.authentication_uiux.models.user.ChangePasswordResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class recoveryPassword extends AppCompatActivity {
    private TextInputEditText verifyCodeInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialButton recoverPasswordButton;
    private ImageButton backButton;
    private TextView signInLink;
    private UserApi apiService;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_password);

        initializeViews();
        setupClickListeners();

        // Initialize Retrofit and ApiService
        Retrofit retrofit = RetrofitClient.getClient();
        apiService = retrofit.create(UserApi.class);

        // Retrieve email from intent
        email = getIntent().getStringExtra("email");
    }

    private void initializeViews() {
        verifyCodeInput = findViewById(R.id.verifyCodeInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        recoverPasswordButton = findViewById(R.id.recoverPasswordButton);
        backButton = findViewById(R.id.backButton);
        signInLink = findViewById(R.id.signInLink);
    }

    private void setupClickListeners() {
        recoverPasswordButton.setOnClickListener(v -> attemptPasswordRecovery());

        backButton.setOnClickListener(v -> finish());

        signInLink.setOnClickListener(v -> {
            Intent intent = new Intent(recoveryPassword.this, Sign_In_Activity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptPasswordRecovery() {
        String verifyCode = verifyCodeInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(verifyCode)) {
            verifyCodeInput.setError("Verification code is required");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordInput.setError("New password is required");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Confirm password is required");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setEmail(email); // Use the email retrieved from the intent
        request.setOtp(verifyCode);
        request.setNewPassword(newPassword);
        request.setCheckNewPassword(confirmPassword);

        Call<ChangePasswordResponse> call = apiService.changePassword(request);
        call.enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(recoveryPassword.this, "Password successfully reset", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(recoveryPassword.this, Sign_In_Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("recoveryPassword", "Error: " + errorBody);
                        Toast.makeText(recoveryPassword.this, "Failed to reset password: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(recoveryPassword.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                Log.e("recoveryPassword", "Error: " + t.getMessage(), t);
                Toast.makeText(recoveryPassword.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}