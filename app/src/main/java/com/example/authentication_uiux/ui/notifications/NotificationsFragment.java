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
import androidx.lifecycle.ViewModelProvider;

import com.example.authentication_uiux.Profile;
import com.example.authentication_uiux.R;
import com.example.authentication_uiux.databinding.FragmentNotificationsBinding;
import com.example.authentication_uiux.welcome;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private TextView logout;
    private ImageView profileChangeBtn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_setting, container, false);

        // Khởi tạo các view
        logout = root.findViewById(R.id.logout);
        profileChangeBtn = root.findViewById(R.id.profile_change_btn);

        // Xử lý sự kiện click cho nút profile
        profileChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Profile.class);
                startActivity(intent);
                // Không gọi finish() vì đang ở trong Fragment
            }
        });

        // Xử lý sự kiện click cho logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), welcome.class);
                startActivity(intent);
                // Không gọi finish() vì đang ở trong Fragment
                getActivity().finish(); // Đóng activity chứa fragment nếu cần
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}