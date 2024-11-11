package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.authentication_uiux.API.UserApi;
import com.example.authentication_uiux.models.user.VerifyOtpRequest;
import com.example.authentication_uiux.models.user.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class VerifyOtpActivity extends AppCompatActivity {
    private EditText otpInput;
    private Button verifyOtpButton;
    private UserApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        otpInput = findViewById(R.id.otpInput);

        // Initialize Retrofit and ApiService
        Retrofit retrofit = RetrofitClient.getClient("http://192.168.124.155:3000");
        apiService = retrofit.create(UserApi.class);

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptVerifyOtp();
            }
        });
    }

    private void attemptVerifyOtp() {
        String otp = otpInput.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            otpInput.setError("OTP is required");
            return;
        }

        verifyOtp(otp);
    }

    private void verifyOtp(String otp) {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest();
        verifyOtpRequest.setOtp(otp);

        Call<VerifyOtpResponse> call = apiService.verifyOtp(verifyOtpRequest);
        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                if (response.isSuccessful()) {
                    VerifyOtpResponse verifyOtpResponse = response.body();
                    Toast.makeText(VerifyOtpActivity.this, verifyOtpResponse.getMsg(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifyOtpActivity.this, Sign_In_Activity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(VerifyOtpActivity.this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                Toast.makeText(VerifyOtpActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}