package com.example.skills_plus.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.skills_plus.adapter.AllBlogAdapter;
import com.example.skills_plus.databinding.FragmentCommunityBinding;
import com.example.skills_plus.modal.AllBlogModal;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private FragmentCommunityBinding binding;
    private AllBlogAdapter adapter;
    private List<AllBlogModal> allBlogsList = new ArrayList<>();
    private List<AllBlogModal> filteredBlogsList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);

        // Show progress bar
        binding.progressBar.setVisibility(View.VISIBLE);

        // Initialize RecyclerView
        initializeRecyclerView();

        // Fetch blogs from Firebase
        fetchBlogsFromFirebase();

        // Setup SearchView functionality
        setupSearchView();

        return binding.getRoot();
    }

    private void initializeRecyclerView() {
        adapter = new AllBlogAdapter(getContext(), filteredBlogsList); // Use filtered list for displaying blogs
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvAllBlogs.setLayoutManager(layoutManager);
        binding.rvAllBlogs.setAdapter(adapter);
    }

    private void fetchBlogsFromFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("blogs");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBlogsList.clear();
                filteredBlogsList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AllBlogModal blog = dataSnapshot.getValue(AllBlogModal.class);
                    if (blog != null) {
                        allBlogsList.add(blog);
                        filteredBlogsList.add(blog);
                    }
                }

                // Update UI
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterBlogs(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBlogs(newText);
                return false;
            }
        });
    }

    private void filterBlogs(String query) {
        filteredBlogsList.clear();
        for (AllBlogModal blog : allBlogsList) {
            if (blog.getTitle() != null && blog.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredBlogsList.add(blog);
            }
        }

        // Update RecyclerView with filtered data
        adapter.notifyDataSetChanged();
    }
}
