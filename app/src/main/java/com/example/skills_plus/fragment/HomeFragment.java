package com.example.skills_plus.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.skills_plus.adapter.CardAdapter;
import com.example.skills_plus.databinding.FragmentHomeBinding;
import com.example.skills_plus.modal.CardModal;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding; // Data binding for fragment layout
    private CardAdapter adapter;  // Adapter for RecyclerView
    private List<CardModal> cardList;  // List to hold card data

    private static final String TAG = "HomeFragment";  // Log tag

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize data binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        binding.progressBar.setVisibility(View.VISIBLE);

        // Initialize card list
        cardList = new ArrayList<>();

        // Functionality for displaying data from Firebase
        displayCardView();

        // Set up RecyclerView
        adapter = new CardAdapter(getContext(), cardList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        binding.rvAllBlogs.setLayoutManager(linearLayoutManager);
        binding.rvAllBlogs.setAdapter(adapter);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void displayCardView() {
        // Reference to all users' posts in Firebase Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Add a ValueEventListener to listen for changes in the database
        databaseRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear any existing data (optional)
                cardList.clear();  // Clear the card list before populating with new data
                Log.d(TAG, "Data snapshot: " + snapshot.toString());  // Log the snapshot for debugging

                // Iterate through all users
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                    // Iterate through each user's posts
                    for (DataSnapshot postSnapshot : userSnapshot.child("posts").getChildren()) {
                        // Get the data for each post
                        Map<String, Object> postData = (Map<String, Object>) postSnapshot.getValue();

                        if (postData != null) {
                            // Extract data from the map
                            String title = (String) postData.get("title");
                            String description = (String) postData.get("description");
                            String imageUrl = (String) postData.get("imageUrl");
                            String timestamp = (String) postData.get("timestamp");

                            // Create a CardModal object with the retrieved data
                            CardModal card = new CardModal(title, description, imageUrl, timestamp);

                            // Add the card to the card list
                            cardList.add(card);
                        }
                    }
                }

                // Update the RecyclerView adapter with the new data
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database errors
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(requireContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
