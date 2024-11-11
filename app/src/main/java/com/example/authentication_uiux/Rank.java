package com.example.authentication_uiux;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.authentication_uiux.models.RankData;

import java.util.ArrayList;
import java.util.List;

public class Rank extends AppCompatActivity {
    private RecyclerView rankingsRecyclerView;
    private RankAdapter rankAdapter;
    private List<RankData> rankData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        // Initialize views
        rankingsRecyclerView = findViewById(R.id.rankings_recycler_view);
        ImageButton backButton = findViewById(R.id.back_button);

        // Setup back button
        backButton.setOnClickListener(v -> finish());

        // Initialize ranking data
        initializeRankData();

        // Setup RecyclerView
        setupRecyclerView();
    }

    private void initializeRankData() {
        rankData = new ArrayList<>();

        // Add sample data
        rankData.add(new RankData("NguyenVanA", R.drawable.avatar_nguyenvana, 80));
        rankData.add(new RankData("NguyenVanB", R.drawable.avatar_nguyenvanb, 75));
        rankData.add(new RankData("You", R.drawable.avatar_you, 75));
        rankData.add(new RankData("NguyenVanD", R.drawable.avatar_nguyenvand, 70));
        rankData.add(new RankData("NguyenVanE", R.drawable.avatar_nguyenvane, 60));
        rankData.add(new RankData("NguyenVanF", R.drawable.avatar_nguyenvanf, 55));
    }

    private void setupRecyclerView() {
        rankAdapter = new RankAdapter(rankData);
        rankingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rankingsRecyclerView.setAdapter(rankAdapter);
    }
}