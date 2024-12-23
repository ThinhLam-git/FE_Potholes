package com.example.authentication_uiux.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.authentication_uiux.API.PotholeApi;
import com.example.authentication_uiux.R;
import com.example.authentication_uiux.Rank;
import com.example.authentication_uiux.RetrofitClient;
import com.example.authentication_uiux.models.pothhole.PotholeStatistics;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DashboardFragment extends Fragment {
    private TextView textPH;
    private TextView textKM;
    private BarChart barChart;
    private PotholeApi potholeApi;
    private Button rankButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_dashboard, container, false);
        textPH = root.findViewById(R.id.textPH);
        textKM = root.findViewById(R.id.textKM);
        barChart = root.findViewById(R.id.barChart);
        rankButton = root.findViewById(R.id.rank_button);

        Retrofit retrofit = RetrofitClient.getClient();
        potholeApi = retrofit.create(PotholeApi.class);

        fetchPotholeStatistics();
        setupBarChart();

        rankButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Rank.class);
            startActivity(intent);
        });

        return root;
    }

    private void fetchPotholeStatistics() {
        potholeApi.getPotholeStatistics().enqueue(new Callback<PotholeStatistics>() {
            @Override
            public void onResponse(@NonNull Call<PotholeStatistics> call, @NonNull Response<PotholeStatistics> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PotholeStatistics stats = response.body();
                    textPH.setText(String.valueOf(stats.getTotalPotholes()));
                    textKM.setText(String.valueOf(stats.getTotalKilometers()));
                    updateBarChart(stats);
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

    private void setupBarChart() {
        barChart.getDescription().setEnabled(true);
        barChart.animateY(2000);
    }

    private void updateBarChart(PotholeStatistics statistics) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int[] potholesLast7Days = statistics.getPotholesLast7Days();
        for (int i = 0; i < potholesLast7Days.length; i++) {
            barEntries.add(new BarEntry(i + 1, potholesLast7Days[i]));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Potholes Detected in Last 7 Days");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(R.color.black);
        barDataSet.setValueTextSize(16f);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }
}