package com.example.skills_plus.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityReadBlogBinding;

public class ReadBlogActivity extends AppCompatActivity {

    ActivityReadBlogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadBlogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BackPress();

        // Get data from the intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String timestamp = getIntent().getStringExtra("timestamp");

        // Set data to views
        binding.titleBD.setText(title);
        binding.descriptionBD.setText(description);
        binding.timeStampBD.setText(timestamp);

        // Load image using Glide
        Glide.with(this).load(imageUrl).into(binding.imageBD);
    }

    private void BackPress() {
        binding.blogDetailToolbar.setElevation(8);
        binding.blogDetailToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


}
