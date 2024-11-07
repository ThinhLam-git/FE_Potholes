package com.example.authentication_uiux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class welcome extends AppCompatActivity {
    private Button signInButton;
    private Button signUpButton;
    private View languageSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        signInButton = findViewById(R.id.sign_in_button);
        signUpButton = findViewById(R.id.sign_up_button);
        languageSet = findViewById(R.id.language_set);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(welcome.this, Sign_In_Activity.class);
                startActivity(intent1);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(welcome.this, Sign_Up_Activity.class);
                startActivity(intent2);
            }
        });

        languageSet.setOnClickListener(v -> {
            Intent intent3 = new Intent(welcome.this, languageSetting.class);
            startActivity(intent3);
        });

    }
}