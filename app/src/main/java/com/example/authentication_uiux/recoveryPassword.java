package com.example.authentication_uiux;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class recoveryPassword extends AppCompatActivity {

    //Khởi tạo các thành phần trong layout
    private TextInputEditText verifyCodeInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialButton recoverPasswordButton;
    private TextView signInLink;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_password);

        //Khởi tao các View
        verifyCodeInput = findViewById(R.id.verifyCodeInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        recoverPasswordButton = findViewById(R.id.recoverPasswordButton);
        signInLink = findViewById(R.id.signInLink);
        backButton = findViewById(R.id.backButton);

        //Tạo event click cho nút back
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Quay lại màn hình trước (Forgot Passwor)
                finish();
            }
        });

        recoverPasswordButton.setOnClickListener(v -> {
           if(validateInputs()){
               recoverPassword();
           }
        });

        //Click vào span Đăng nhập
//        signInLink.setOnClickListener(v -> {
//           Intent intent = new Intent(RecoveryPassword_UI.this, SignInActivity.class);
//           startActivity(intent);
//           finish();
//        });
    }

    private boolean validateInputs(){
        String verifyCode = verifyCodeInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if(verifyCode.isEmpty()){
            verifyCodeInput.setError("Please enter verification code");
            return false;
        }

        if(newPassword.isEmpty()){
            newPasswordInput.setError("Please enter new password");
            return false;
        }

        if(confirmPassword.isEmpty()){
            confirmPasswordInput.setError("Please enter confirm password");
            return false;
        }

        //Kiểm tra mặt khẩu mới và xác nhận mật khẩu có giống nhau không
        if(!newPassword.equals(confirmPassword)){
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void recoverPassword(){
        //Gửi yêu cầu khôi phục mật khẩu đến server
        //Sau khi khôi phục thành công, chuyển hướng đến màn hình đăng nhập

    }
}