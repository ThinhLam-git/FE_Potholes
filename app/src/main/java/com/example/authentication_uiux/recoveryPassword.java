package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class recoveryPassword extends AppCompatActivity {

    //Khởi tạo các thành phần trong layout
    private TextInputEditText verifyCodeInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialButton recoverPasswordButton;
    private ImageButton backButton;
    private TextView signInLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_password);

        initializeViews();
        setupClickListeners();
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

        // TODO: Implement actual password recovery logic here
        Toast.makeText(this, "Password successfully reset", Toast.LENGTH_SHORT).show();

        // Navigate back to sign in
        Intent intent = new Intent(recoveryPassword.this, Sign_In_Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}