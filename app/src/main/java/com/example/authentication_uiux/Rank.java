package com.example.authentication_uiux;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Rank extends AppCompatActivity {
    private RecyclerView rankingsRecyclerView;
    private RankAdapter rankAdapter;
    private List<RankItem> rankItems;

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
        rankItems = new ArrayList<>();

        // Add sample data
        rankItems.add(new RankItem("NguyenVanA", R.drawable.avatar_nguyenvana, 80));
        rankItems.add(new RankItem("NguyenVanB", R.drawable.avatar_nguyenvanb, 75));
        rankItems.add(new RankItem("You", R.drawable.avatar_you, 75));
        rankItems.add(new RankItem("NguyenVanD", R.drawable.avatar_nguyenvand, 70));
        rankItems.add(new RankItem("NguyenVanE", R.drawable.avatar_nguyenvane, 60));
        rankItems.add(new RankItem("NguyenVanF", R.drawable.avatar_nguyenvanf, 55));
    }

    private void setupRecyclerView() {
        rankAdapter = new RankAdapter(rankItems);
        rankingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rankingsRecyclerView.setAdapter(rankAdapter);
    }
}