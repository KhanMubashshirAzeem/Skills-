package com.example.skills_plus.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.skills_plus.activity.PublishSkillActivity;
import com.example.skills_plus.adapter.CardAdapter;
import com.example.skills_plus.databinding.FragmentWriteBinding;
import com.example.skills_plus.modal.CardModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WriteFragment extends Fragment {

    private FragmentWriteBinding binding; // Data binding for fragment layout
    private CardAdapter adapter;  // Adapter for RecyclerView
    private List<CardModal> cardList;  // List to hold card data

    private static final String TAG = "WriteFragment";  // Log tag

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize data binding
        binding = FragmentWriteBinding.inflate(inflater, container, false);

        // Initialize card list
        cardList = new ArrayList<>();

        // Set up RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CardAdapter(getContext(), cardList);
        binding.recyclerView.setAdapter(adapter);


        // Functionality for opening PublishSkillActivity
        openPublishActivity();

        // Functionality for displaying data from Firebase
        displayCardView();

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void displayCardView() {
        // Get the current user's UID and name
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String userName = currentUser.getDisplayName();

        // Reference to the user's posts in Firebase Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("posts");

        // Add a ValueEventListener to listen for changes in the database
        databaseRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear any existing data (optional)
                cardList.clear();  // Clear the card list before populating with new data
                Log.d(TAG, "Data snapshot: " + snapshot.toString());  // Log the snapshot for debugging
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    // Get the data for each post
                    Map<String, Object> postData = (Map<String, Object>) childSnapshot.getValue();

                    if (postData != null) {
                        // Extract data from the map
                        String title = (String) postData.get("title");
                        String description = (String) postData.get("description");
                        String imageUrl = (String) postData.get("imageUrl");
                        String timestamp = (String) postData.get("timestamp");

                        // Create a CardModal object with the retrieved data
                        CardModal card = new CardModal(userName, title, description, imageUrl, timestamp);

                        // Add the card to the card list
                        cardList.add(card);
                    }
                }
                // Update the RecyclerView adapter with the new data
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Data loaded", Toast.LENGTH_SHORT).show();   // Facing Error
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database errors (optional)
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(requireContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void openPublishActivity() {
        binding.floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), PublishSkillActivity.class));
            }
        });
    }


}

