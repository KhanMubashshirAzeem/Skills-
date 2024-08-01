package com.example.skills_plus.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.skills_plus.adapter.AllBlogAdapter;
import com.example.skills_plus.databinding.ActivitySavedBlogBinding;
import com.example.skills_plus.modal.AllBlogModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SavedBlogActivity extends AppCompatActivity {

    private ActivitySavedBlogBinding binding;
    private AllBlogAdapter allBlogAdapter;
    private List<AllBlogModal> allBlogModalList;
    private String userId;
    private DatabaseReference userRef, blogRef;
    private int blogsToFetch;
    private int blogsFetched;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedBlogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = FirebaseAuth.getInstance().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users");
        blogRef = FirebaseDatabase.getInstance().getReference("posts");

        allBlogModalList = new ArrayList<>();
        allBlogAdapter = new AllBlogAdapter(this, allBlogModalList);
        binding.savedBlogsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.savedBlogsRecyclerView.setAdapter(allBlogAdapter);

        fetchBookmarkedBlogs();
    }

    private void fetchBookmarkedBlogs() {
        binding.progressBar.setVisibility(View.VISIBLE);
        userRef.child(userId).child("favorites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBlogModalList.clear();
                if (snapshot.exists()) {
                    blogsToFetch = (int) snapshot.getChildrenCount();
                    blogsFetched = 0;
                    for (DataSnapshot blogIdSnapshot : snapshot.getChildren()) {
                        String blogId = blogIdSnapshot.getKey();
                        fetchBlogDetails(blogId);
                    }
                } else {
                    Toast.makeText(SavedBlogActivity.this, "No bookmarks found", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedBlogActivity.this, "Error fetching bookmarks", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchBlogDetails(String blogId) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        // Iterate through each user's posts
                        for (DataSnapshot postSnapshot : userSnapshot.child("posts").getChildren()) {
                            Map<String, Object> postData = (Map<String, Object>) postSnapshot.getValue();
                            if (postData != null && postSnapshot.getKey().equals(blogId)) {
                                // Extract data from the map
                                String blogId1 = postSnapshot.getKey();
                                String title = (String) postData.get("title");
                                String description = (String) postData.get("description");
                                String imageUrl = (String) postData.get("imageUrl");
                                String timestamp = (String) postData.get("timestamp");
                                //Toast.makeText(SavedBlogActivity.this, postSnapshot.getKey() + " and " + blogId, Toast.LENGTH_SHORT).show();

                                // Create a AllBlogModal object with the retrieved data
                                AllBlogModal card1 = new AllBlogModal(blogId1, title, description, imageUrl, timestamp);

                                // Add the card to the card list
                                allBlogModalList.add(card1);
                                allBlogAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
                blogsFetched++;
                if (blogsFetched == blogsToFetch) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SavedBlogActivity.this, "success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedBlogActivity.this, "Error fetching blog details", Toast.LENGTH_SHORT).show();
                blogsFetched++;
                if (blogsFetched == blogsToFetch) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
