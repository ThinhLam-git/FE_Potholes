package com.example.authentication_uiux;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.authentication_uiux.models.RankData;

import java.util.List;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.RankViewHolder> {
    private List<RankData> rankData;

    public RankAdapter(List<RankData> rankData) {
        this.rankData = rankData;
    }

    @NonNull
    @Override
    public RankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_rank_item, parent, false);
        return new RankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankViewHolder holder, int position) {
        RankData item = rankData.get(position);
        holder.bind(item, position + 4); // +4 because top 3 are displayed separately
    }

    @Override
    public int getItemCount() {
        return rankData.size();
    }

    static class RankViewHolder extends RecyclerView.ViewHolder {
        TextView rankNumber;
        ImageView avatar;
        TextView name;
        TextView score;

        RankViewHolder(@NonNull View itemView) {
            super(itemView);
            rankNumber = itemView.findViewById(R.id.rank_number);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            score = itemView.findViewById(R.id.score);
        }

        void bind(RankData item, int position) {
            rankNumber.setText(String.valueOf(position));
            avatar.setImageResource(item.getAvatarResource());
            name.setText(item.getName());
            score.setText(String.valueOf(item.getScore()));
        }
    }
}