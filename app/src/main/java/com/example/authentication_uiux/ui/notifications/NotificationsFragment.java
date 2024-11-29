package com.example.authentication_uiux.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.authentication_uiux.Profile;
import com.example.authentication_uiux.R;
import com.example.authentication_uiux.welcome;

public class NotificationsFragment extends Fragment {

    private TextView logout;
    private ImageView profileChangeBtn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_setting, container, false);

        // Initialize views
        logout = root.findViewById(R.id.logout);
        profileChangeBtn = root.findViewById(R.id.profile_change_btn);

        // Profile change button click handler
        profileChangeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Profile.class);
            startActivity(intent);
        });

        // Logout button click handler
        logout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), welcome.class);
            startActivity(intent);
            getActivity().finish(); // Close the current activity
        });

        return root;
    }
}