package com.example.authentication_uiux.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.authentication_uiux.API.PotholeApi;
import com.example.authentication_uiux.R;
import com.example.authentication_uiux.RetrofitClient;
import com.example.authentication_uiux.models.pothhole.PotholeStatistics;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DashboardFragment extends Fragment {
    private TextView textPH;
    private TextView textKM;
    private PotholeApi potholeApi;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_dashboard, container, false);
        textPH = root.findViewById(R.id.textPH);
        textKM = root.findViewById(R.id.textKM);

        Retrofit retrofit = RetrofitClient.getClient();
        potholeApi = retrofit.create(PotholeApi.class);

        fetchPotholeStatistics();

        return root;
    }

    private void fetchPotholeStatistics() {
        potholeApi.getPotholeStatistics().enqueue(new Callback<PotholeStatistics>() {
            @Override
            public void onResponse(@NonNull Call<PotholeStatistics> call, @NonNull Response<PotholeStatistics> response) {
                if (response.isSuccessful() && response.body() != null) {
                    textPH.setText(String.valueOf(response.body().getTotalPotholes()));
                    textKM.setText(String.valueOf(response.body().getTotalKilometers()));
                } else {
                    textPH.setText("Failed to fetch data");
                    textKM.setText("Failed to fetch data");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PotholeStatistics> call, @NonNull Throwable t) {
                textPH.setText("Error: " + t.getMessage());
                textKM.setText("Error: " + t.getMessage());
            }
        });
    }
}