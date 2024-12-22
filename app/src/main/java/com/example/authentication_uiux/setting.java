package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class setting extends AppCompatActivity {
    TextView logout;
    ImageView profileChangeBtn;
    ImageView languageChangeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        logout = findViewById(R.id.logout);
        profileChangeBtn = findViewById(R.id.profile_change_btn);
        languageChangeBtn = findViewById(R.id.language_change_btn);

        profileChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the profile activity
                Intent intent = new Intent(setting.this, Profile.class);
                startActivity(intent);
                finish();
            }
        });

        languageChangeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(setting.this, languageSetting.class);
            startActivity(intent);
        });

        //Logout click listener
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the setting activity
                Intent intent = new Intent(setting.this, welcome.class);
                startActivity(intent);
                finish();
            }
        });
    }
}