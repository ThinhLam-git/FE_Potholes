package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {
    LinearLayout setting_change;
    LinearLayout profile_change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        setting_change = findViewById(R.id.setting_change);
        profile_change = findViewById(R.id.profile_change);

        // Setting button listener
        setting_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the setting activity
                Intent intent = new Intent(Home.this, setting.class);
                startActivity(intent);
                finish();
            }
        });

        // Profile button listener
        profile_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the profile activity
                Intent intent = new Intent(Home.this, Profile.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
