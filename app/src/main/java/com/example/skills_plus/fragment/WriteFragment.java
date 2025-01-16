package com.example.skills_plus.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.skills_plus.activity.PublishBlogActivity;
import com.example.skills_plus.adapter.BlogAdapter;
import com.example.skills_plus.databinding.FragmentWriteBinding;
import com.example.skills_plus.modal.BlogModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WriteFragment extends Fragment {

    private FragmentWriteBinding binding; // Data binding for fragment layout
    private BlogAdapter adapter;  // Adapter for RecyclerView
    private List<BlogModal> cardList;// List to hold card data
    private DatabaseReference blogRef;
    private String userUid;

    private static final String TAG = "WriteFragment";  // Log tag

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize data binding
        binding = FragmentWriteBinding.inflate(inflater, container, false);

        binding.progressBar.setVisibility(View.VISIBLE);

        userUid = FirebaseAuth.getInstance().getUid();
        blogRef = FirebaseDatabase.getInstance().getReference("blogs");

        // Initialize card list
        cardList = new ArrayList<>();

        // Functionality for opening PublishSkillActivity
        openPublishActivity();

        // Functionality for displaying data from Firebase
        //displayCardView();

        fetchBlogs();

        // Set up RecyclerView
        adapter = new BlogAdapter(getContext(), cardList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void fetchBlogs() {
        getUserPublishedBlogs(userUid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cardList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BlogModal blogs = dataSnapshot.getValue(BlogModal.class);
                    if (blogs != null) {
                        cardList.add(blogs);
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getUserPublishedBlogs(String userId, ValueEventListener valueEventListener) {
        blogRef.orderByChild("authorId").equalTo(userId).addValueEventListener(valueEventListener);
    }

//    private void displayCardView() {
//        // Get the current user's UID and name
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(requireContext(), "PLease login", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(getContext(), LoginActivity.class));
//            getActivity().finish();
//            return;
//        }
//
//        String uid = currentUser.getUid();
//
//        // Reference to the user's posts in Firebase Database
//        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("posts");
//
//        // Add a ValueEventListener to listen for changes in the database
//        databaseRef.addValueEventListener(new ValueEventListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // Clear any existing data (optional)
//                cardList.clear();  // Clear the card list before populating with new data
//                Log.d(TAG, "Data snapshot: " + snapshot.toString());  // Log the snapshot for debugging
//                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//                    // Get the data for each post
//                    Map<String, Object> postData = (Map<String, Object>) childSnapshot.getValue();
//
//                    if (postData != null) {
//                        // Extract data from the map
//                        String title = (String) postData.get("title");
//                        String description = (String) postData.get("description");
//                        String imageUrl = (String) postData.get("imageUrl");
//                        String timestamp = (String) postData.get("timestamp");
//                        String blogId = (String) postData.get("blogId");
//
//                        Log.d("WriteFragment", "Retrieved blogId: " + blogId);
//
//
//                        // Create a CardModal object with the retrieved data
//                        BlogModal card = new BlogModal(title, description, imageUrl, timestamp, blogId);
//
//                        // Add the card to the card list
//                        cardList.add(card);
//                    }
//                }
//                // Update the RecyclerView adapter with the new data
//                adapter.notifyDataSetChanged();
//                binding.progressBar.setVisibility(View.GONE);
//            }
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Handle database errors (optional)
//                Log.e(TAG, "Database error: " + error.getMessage());
//                Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


    private void openPublishActivity() {
        binding.floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), PublishBlogActivity.class));
            }
        });
    }


}