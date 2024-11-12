package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.authentication_uiux.API.UserApi;
import com.example.authentication_uiux.models.user.ForgotPasswordRequest;
import com.example.authentication_uiux.models.user.ForgotPasswordResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class forgotPassword extends AppCompatActivity {
    private TextInputEditText emailInput;
    private MaterialButton sendEmailButton;
    private TextView signInLink;
    private ImageButton backButton;
    private UserApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupClickListeners();

        // Initialize Retrofit and ApiService
        Retrofit retrofit = RetrofitClient.getClient();
        apiService = retrofit.create(UserApi.class);
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

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email);

        Call<ForgotPasswordResponse> call = apiService.forgotPassword(request);
        call.enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(forgotPassword.this, "Recovery email sent", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(forgotPassword.this, recoveryPassword.class);
                    intent.putExtra("email", email); // Pass the email to the next activity
                    startActivity(intent);
                } else {
                    Toast.makeText(forgotPassword.this, "Email maybe not exist, Failed to send recovery email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                Toast.makeText(forgotPassword.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}