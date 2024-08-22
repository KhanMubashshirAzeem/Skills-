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

import com.example.skills_plus.adapter.AllBlogAdapter;
import com.example.skills_plus.databinding.FragmentHomeBinding;
import com.example.skills_plus.modal.AllBlogModal;
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
    private AllBlogAdapter adapter;  // Adapter for RecyclerView
    private List<AllBlogModal> allCardList;  // List to hold card data

    private static final String TAG = "HomeFragment";  // Log tag

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize data binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        binding.progressBar.setVisibility(View.VISIBLE);

        // Initialize card list
        allCardList = new ArrayList<>();

        // Functionality for displaying data from Firebase
        fetchCardView();

        // Set up RecyclerView
        adapter = new AllBlogAdapter(getContext(), allCardList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        binding.rvAllBlogs.setLayoutManager(linearLayoutManager);
        binding.rvAllBlogs.setAdapter(adapter);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void fetchCardView() {

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("blogs");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCardList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AllBlogModal blogs = dataSnapshot.getValue(AllBlogModal.class);
                    allCardList.add(blogs);
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
