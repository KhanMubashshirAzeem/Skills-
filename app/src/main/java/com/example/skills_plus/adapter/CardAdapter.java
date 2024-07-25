package com.example.skills_plus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skills_plus.modal.CardModal;
import com.example.skills_plus.R; // Replace with your actual resource identifier

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private final Context context;
    private final List<CardModal> cardList;

    // Constructor for initializing context and cardList
    public CardAdapter(Context context, List<CardModal> cardList) {
        this.context = context;
        this.cardList = cardList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the card item
        View view = LayoutInflater.from(context).inflate(R.layout.blog_card_view, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        // Handle potential null cardList gracefully
        if (cardList == null || cardList.isEmpty()) {
            return;
        }

        CardModal card = cardList.get(position);
        holder.titleTextView.setText(card.getTitle());
        holder.descriptionTextView.setText(card.getDescription());
        holder.timeStampTextView.setText(card.getTimeStamp());

        // Use Glide to load the image
        Glide.with(context)
                .load(card.getImage())
                .placeholder(R.drawable.star_icon)  // Optional placeholder image
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return cardList != null ? cardList.size() : 0;
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
            titleTextView = itemView.findViewById(R.id.title_);
            descriptionTextView = itemView.findViewById(R.id.description);
            imageView = itemView.findViewById(R.id.image);
            timeStampTextView = itemView.findViewById(R.id.timeStamp);
        }
    }
}
