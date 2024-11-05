package com.example.authentication_uiux;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TermPolicyAdapter extends RecyclerView.Adapter<TermPolicyAdapter.ViewHolder> {
    private List<TermPolicyItem> items;
    private int expandedPosition = -1;

    public TermPolicyAdapter(List<TermPolicyItem> items){
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_term_policy, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TermPolicyItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.content.setText(item.getContent());

        //Set up for expansion and collapse
        boolean isExpanded = position == expandedPosition;
        holder.content.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Set click listener for title to goggle visibility
        holder.title.setOnClickListener(v -> {
            expandedPosition = isExpanded ? -1 : position;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount(){
        return items.size();
    }

    public static class ViewHolder extends  RecyclerView.ViewHolder{
        TextView title, content;

        public ViewHolder(View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            content = itemView.findViewById(R.id.item_content);
        }
    }
}
