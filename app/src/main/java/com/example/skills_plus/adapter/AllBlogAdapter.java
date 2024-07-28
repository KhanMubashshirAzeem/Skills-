package com.example.skills_plus.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skills_plus.R;
import com.example.skills_plus.activity.ReadBlogActivity;
import com.example.skills_plus.modal.AllBlogModal;

import java.util.List;

public class AllBlogAdapter extends RecyclerView.Adapter<AllBlogAdapter.CardViewHolder> {

    private final Context context;
    private final List<AllBlogModal> allCardList;

    // Constructor for initializing context and cardList
    public AllBlogAdapter(Context context, List<AllBlogModal> allCardList) {
        this.context = context;
        this.allCardList = allCardList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the card item
        View view = LayoutInflater.from(context).inflate(R.layout.all_blog_card_view, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        // Handle potential null cardList gracefully
        if (allCardList == null || allCardList.isEmpty()) {
            return;
        }

        AllBlogModal card = allCardList.get(position);
        holder.titleTextView.setText(card.getTitle());
        holder.descriptionTextView.setText(card.getDescription());
        holder.timeStampTextView.setText(card.getTimeStamp());

        // Use Glide to load the image
        Glide.with(context).load(card.getImage()).placeholder(R.drawable.star_icon).into(holder.imageView);

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReadBlogActivity.class);
            intent.putExtra("title", card.getTitle());
            intent.putExtra("description", card.getDescription());
            intent.putExtra("imageUrl", card.getImage());
            intent.putExtra("timestamp", card.getTimeStamp());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return allCardList != null ? allCardList.size() : 0;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final ImageView imageView;
        private final TextView timeStampTextView;

        // Constructor for initializing views
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find views by their IDs
            titleTextView = itemView.findViewById(R.id.titleAB);
            descriptionTextView = itemView.findViewById(R.id.descriptionAB);
            imageView = itemView.findViewById(R.id.imageAB);
            timeStampTextView = itemView.findViewById(R.id.timeStampAB);
        }
    }
}
