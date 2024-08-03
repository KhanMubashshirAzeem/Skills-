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
import com.example.skills_plus.modal.AllBlogModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllBlogAdapter extends RecyclerView.Adapter<AllBlogAdapter.CardViewHolder> {

    private final Context context;
    private final List<AllBlogModal> allCardList;
    private final FirebaseDatabase database;
    private final DatabaseReference dbRef;
    private final FirebaseAuth auth;

    // Constructor for initializing context and cardList
    public AllBlogAdapter(Context context, List<AllBlogModal> allCardList) {
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
        AllBlogModal blog = allCardList.get(position);

        // Handle potential null cardList gracefully
        if (allCardList == null || allCardList.isEmpty()) {
            return;
        }

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
        holder.bookmarkBtn.setOnClickListener(view -> bookmarkMethod(holder, blog));
        // Update the bookmark icon based on the user's favorites
        updateBookmarkIcon(holder, blog.getBlogId());


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
        private final ImageView bookmarkBtn;

        // Constructor for initializing views
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

// ********************** Custom method for bookmark feature ************************************
    private void bookmarkMethod(CardViewHolder holder, AllBlogModal blog) {
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

    // *************************** Custom feature for update bookmark ******************************

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
                // Handle error
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



}


//Summary of Changes:
//Bind the Bookmark Button Click Event: In onBindViewHolder, add an onClick listener for the bookmark button.
//Implement bookmarkMethod: Add or remove the blog from the user's favorites and update the bookmark icon accordingly.
//Update Bookmark Icon: Check if the blog is already bookmarked and set the correct icon in updateBookmarkIcon.
