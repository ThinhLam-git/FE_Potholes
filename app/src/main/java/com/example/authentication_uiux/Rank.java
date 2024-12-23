package com.example.authentication_uiux;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.authentication_uiux.API.PotholeApi;
import com.example.authentication_uiux.RetrofitClient;
import com.example.authentication_uiux.models.RankData;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Rank extends AppCompatActivity {
    private RecyclerView rankingsRecyclerView;
    private RankAdapter rankAdapter;
    private List<RankData> rankData;
    private PotholeApi potholeApi;
    private RankData currentUserRank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        // Initialize views
        rankingsRecyclerView = findViewById(R.id.rankings_recycler_view);
        ImageButton backButton = findViewById(R.id.back_button);

        // Setup back button
        backButton.setOnClickListener(v -> finish());

        // Initialize Retrofit
        Retrofit retrofit = RetrofitClient.getClient();
        potholeApi = retrofit.create(PotholeApi.class);

        // Fetch ranking data
        fetchRankData();
    }

    private void fetchRankData() {
        potholeApi.getUserRankings().enqueue(new Callback<List<RankData>>() {
            @Override
            public void onResponse(Call<List<RankData>> call, Response<List<RankData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rankData = response.body();
                    if (rankData.size() > 10) {
                        currentUserRank = rankData.get(rankData.size() - 1); // Assuming the last item is the current user's rank
                        rankData = rankData.subList(0, 10); // Get only top 10
                        if (!rankData.contains(currentUserRank)) {
                            rankData.add(currentUserRank); // Add current user's rank if not in top 10
                        }
                    }
                    setupRecyclerView();
                }
            }

            @Override
            public void onFailure(Call<List<RankData>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    private void setupRecyclerView() {
        rankAdapter = new RankAdapter(rankData);
        rankingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rankingsRecyclerView.setAdapter(rankAdapter);
    }
}