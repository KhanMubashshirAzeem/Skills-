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

import com.example.skills_plus.R;
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

    FragmentSavedBlogBinding binding;
    AllBlogAdapter allBlogAdapter;
    List<AllBlogModal> allBlogModalList;
    String userId;
    DatabaseReference userRef, blogRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSavedBlogBinding.inflate(inflater, container, false);

        userId = FirebaseAuth.getInstance().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favorites");
        blogRef = FirebaseDatabase.getInstance().getReference("posts");


        return binding.getRoot();
    }


}
