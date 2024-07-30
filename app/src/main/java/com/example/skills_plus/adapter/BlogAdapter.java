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
import com.example.skills_plus.activity.ReadBlogActivity;
import com.example.skills_plus.activity.UpdateDeleteActivity;
import com.example.skills_plus.modal.BlogModal;
import com.example.skills_plus.R; // Replace with your actual resource identifier
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.CardViewHolder> {

    private final Context context;
    private final List<BlogModal> cardList;

    // Constructor for initializing context and cardList
    public BlogAdapter(Context context, List<BlogModal> cardList) {
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Handle potential null cardList gracefully
        if (cardList == null || cardList.isEmpty()) {
            return;
        }

        BlogModal modal = cardList.get(position);
        holder.titleTextView.setText(modal.getTitle());
        holder.descriptionTextView.setText(modal.getDescription());
        holder.timeStampTextView.setText(modal.getTimeStamp());

        // Use Glide to load the image
        Glide.with(context).load(modal.getImage()).placeholder(R.drawable.star_icon)  // Optional placeholder image
                .into(holder.imageView);

        holder.itemView.setOnClickListener((v) -> {
            Intent intent = new Intent(context, UpdateDeleteActivity.class);
            intent.putExtra("title", modal.getTitle());
            intent.putExtra("description", modal.getDescription());
            intent.putExtra("imageUrl", modal.getImage());
            intent.putExtra("timestamp", modal.getTimeStamp());
//            intent.putExtra("blogId", modal.getBlogId()); // Pass the blog ID
            context.startActivity(intent);
        });

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
