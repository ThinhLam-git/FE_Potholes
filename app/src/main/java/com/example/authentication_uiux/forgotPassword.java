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

        //Khởi tạo các Views
        emailInput = findViewById(R.id.emailInput);
        sendEmailButton = findViewById(R.id.sendEmailButton);
        signInLink = findViewById(R.id.signInLink);
        backButton = findViewById(R.id.backButton);

        //Set up for back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Set up for send email button
        sendEmailButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    emailInput.setError("Email is required!");
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInput.setError("Invalid email format!");
                } else{
                    sendPasswordResetEmail(email);
                }
            }
        });

        //Set up for signInLink
        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(forgotPassword.this, tenLayoutCuaSignIn.class);
                finish(); //close this Activity
            }
        });
    }

    //Method to handle sending a verify code to reset password
    public void sendPasswordResetEmail(String email){
        //Implement sending a verify code to reset password
        //Giả sử, mình sử dụng Firebase Authentication để gửi email
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        auth.sendPasswordResetEmail(email)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(forgotPassword.this, "Reset email sent. Check your inbox.", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(forgotPassword.this, "Error sending reset email.", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }
}