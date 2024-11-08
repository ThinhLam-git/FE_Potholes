package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class setting extends AppCompatActivity {
    LinearLayout profile_change;
    LinearLayout home_change;
    TextView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        logout = findViewById(R.id.logout);
        home_change = findViewById(R.id.home_change);
        profile_change = findViewById(R.id.profile_change);

        // Setting button listener
        home_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the setting activity
                Intent intent = new Intent(setting.this, Home.class);
                startActivity(intent);
                finish();
            }
        });

        // Profile button listener
        profile_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the profile activity
                Intent intent = new Intent(setting.this, Profile.class);
                startActivity(intent);
                finish();
            }
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