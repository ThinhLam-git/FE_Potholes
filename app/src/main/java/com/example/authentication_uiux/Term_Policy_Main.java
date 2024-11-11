package com.example.authentication_uiux;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.authentication_uiux.models.TermPolicyData;

import java.util.ArrayList;
import java.util.List;

public class Term_Policy_Main extends AppCompatActivity {

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_policy);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<TermPolicyData> items = new ArrayList<>();
        items.add(new TermPolicyData("1. Information Collection and Use", "The app collects data such as your device's IP address, the pages you visit, time spent on the app, and your mobile operating system. Location data is also collected to provide personalized services, analyze user behavior, and improve the app. Anonymized data may be shared with third-party services like Google Play and Facebook to enhance functionality."));
        items.add(new TermPolicyData("2. User Contact and Data Retention", "The app may contact you for important updates or promotional content. Your data will be retained as long as you use the app. To request data deletion, contact smartroad_vnu@gmail.com. If you wish to stop data collection, simply uninstall the app."));
        items.add(new TermPolicyData("3. Third-Party Services", "The app integrates with third-party services (e.g., Google Play Services, Facebook), each with its own privacy policy. The app may share aggregated and anonymized data with these services to improve functionality."));
        items.add(new TermPolicyData("4. Children's Privacy", "The app does not knowingly collect data from children under 13. If such data is discovered, it will be immediately deleted. Parents can contact the team if they believe their child has provided personal information."));
        items.add(new TermPolicyData("5. Security", "The app applies various physical, electronic, and procedural safeguards to protect your data."));
        items.add(new TermPolicyData("6. User Responsibilities", "You are responsible for maintaining the security of your device. The app may require an internet connection, and you are responsible for any related charges. The app may not work properly if your device is rooted or jailbroken."));
        items.add(new TermPolicyData("7. Updates and Changes", "The app may be updated periodically, and you agree to install updates to continue using it. The service may be discontinued at any time without prior notice."));
        items.add(new TermPolicyData("8. Contact", "For any questions regarding these terms or privacy practices, please contact smartroad_vnu@gmail.com."));

        TermPolicyAdapter adapter = new TermPolicyAdapter(items);
        recyclerView.setAdapter(adapter);

        //set up for back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Term_Policy_Main.this, Sign_Up_Activity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}