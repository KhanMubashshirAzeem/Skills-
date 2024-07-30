package com.example.skills_plus.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.skills_plus.adapter.AllBlogAdapter;
import com.example.skills_plus.databinding.FragmentSavedBlogBinding;
import com.example.skills_plus.modal.AllBlogModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SavedBlogFragment extends Fragment {

    private FragmentSavedBlogBinding binding;
    private AllBlogAdapter allBlogAdapter;
    private List<AllBlogModal> allBlogModalList;
    private String userId;
    private DatabaseReference userRef, blogRef;
    private int blogsToFetch;
    private int blogsFetched;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedBlogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favorites");
        blogRef = FirebaseDatabase.getInstance().getReference("posts");

        allBlogModalList = new ArrayList<>();
        allBlogAdapter = new AllBlogAdapter(requireContext(), allBlogModalList);
        binding.savedBlogsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.savedBlogsRecyclerView.setAdapter(allBlogAdapter);

        fetchBookmarkedBlogs();
    }

    private void fetchBookmarkedBlogs() {
        binding.progressBar.setVisibility(View.VISIBLE);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    blogsToFetch = (int) snapshot.getChildrenCount();
                    blogsFetched = 0;
                    for (DataSnapshot blogIdSnapshot : snapshot.getChildren()) {
                        String blogId = blogIdSnapshot.getKey();
                        fetchBlogDetails(blogId);
                    }
                } else {
                    Toast.makeText(getContext(), "No bookmarks found", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching bookmarks", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchBlogDetails(String blogId) {
        blogRef.child(blogId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    AllBlogModal blog = snapshot.getValue(AllBlogModal.class);
                    if (blog != null) {
                        allBlogModalList.add(blog);
                        allBlogAdapter.notifyDataSetChanged();
                    }
                }
                blogsFetched++;
                if (blogsFetched == blogsToFetch) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching blog details", Toast.LENGTH_SHORT).show();
                blogsFetched++;
                if (blogsFetched == blogsToFetch) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

}
