package com.example.skills_plus.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skills_plus.R;
import com.example.skills_plus.activity.ReadBlogActivity;
import com.example.skills_plus.modal.CommunityBlogModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityBlogAdapter extends RecyclerView.Adapter<CommunityBlogAdapter.CardViewHolder> {

    private final Context context;
    private final List<CommunityBlogModal> allCardList;
    private final FirebaseDatabase database;
    private final DatabaseReference dbRef;
    private final FirebaseAuth auth;

    // Constructor
    public CommunityBlogAdapter(Context context, List<CommunityBlogModal> allCardList) {
        this.context = context;
        this.allCardList = allCardList;
        this.database = FirebaseDatabase.getInstance();
        this.dbRef = database.getReference();
        this.auth = FirebaseAuth.getInstance();
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
        CommunityBlogModal blog = allCardList.get(position);

        // Handle potential null cardList gracefully
        if (allCardList == null || allCardList.isEmpty()) {
            return;
        }

        // Set data to views
        holder.titleTextView.setText(blog.getTitle());
        holder.descriptionTextView.setText(blog.getDescription());
        holder.timeStampTextView.setText(blog.getTimeStamp());

        // Use Glide to load the image
        Glide.with(context).load(blog.getImage()).placeholder(R.drawable.star_icon).into(holder.imageView);

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReadBlogActivity.class);
            intent.putExtra("title", blog.getTitle());
            intent.putExtra("description", blog.getDescription());
            intent.putExtra("imageUrl", blog.getImage());
            intent.putExtra("timestamp", blog.getTimeStamp());
            intent.putExtra("blogId", blog.getBlogId()); // Pass the blog ID
            context.startActivity(intent);
        });

        // Set click listener for the bookmark button
        if (auth.getCurrentUser() != null) {
            updateBookmarkIcon(holder, blog.getBlogId());
            holder.bookmarkBtn.setOnClickListener(view -> bookmarkMethod(holder, blog));
        } else {
            // Disable bookmark feature for non-logged-in users
            holder.bookmarkBtn.setImageResource(R.drawable.bookmark_icon_gray);
            holder.bookmarkBtn.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return allCardList != null ? allCardList.size() : 0;
    }

    // ViewHolder class
    public static class CardViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final ImageView imageView;
        private final TextView timeStampTextView;
        private final ImageView bookmarkBtn;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find views by their IDs
            titleTextView = itemView.findViewById(R.id.titleAB);
            descriptionTextView = itemView.findViewById(R.id.descriptionAB);
            imageView = itemView.findViewById(R.id.imageAB);
            timeStampTextView = itemView.findViewById(R.id.timeStampAB);
            bookmarkBtn = itemView.findViewById(R.id.bookmarkBtn);
        }
    }

    // Custom method for bookmark feature
    private void bookmarkMethod(CardViewHolder holder, CommunityBlogModal blog) {
        String userUid = auth.getCurrentUser().getUid();
        String blogId = blog.getBlogId();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);

        userRef.child("favorites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Boolean> bookmarks = (Map<String, Boolean>) snapshot.getValue();

                if (bookmarks == null) {
                    bookmarks = new HashMap<>();
                }

                if (bookmarks.containsKey(blogId)) {
                    bookmarks.remove(blogId); // Remove bookmark if already exists
                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark_icon_gray); // Unbookmark icon
                } else {
                    bookmarks.put(blogId, true); // Add bookmark if it doesn't exist
                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark_icon_blue); // Bookmark icon
                }

                userRef.child("favorites").setValue(bookmarks).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Bookmark updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update bookmark", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error fetching bookmarks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Custom method for updating bookmark icon
    private void updateBookmarkIcon(CardViewHolder holder, String blogId) {
        String userUid = auth.getCurrentUser().getUid();
        DatabaseReference userRef = dbRef.child("users").child(userUid).child("favorites");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(blogId)) {
                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark_icon_blue); // Bookmarked icon
                } else {
                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark_icon_gray); // Default bookmark icon
                }
                notifyItemChanged(holder.getPosition());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
