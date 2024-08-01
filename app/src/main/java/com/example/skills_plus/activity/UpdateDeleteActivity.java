package com.example.skills_plus.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityUpdateDeleteBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateDeleteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "UpdateDeleteActivity";

    ActivityUpdateDeleteBinding binding;
    private DatabaseReference blogRef;
    private String blogId;
    private Uri imageUri;
    private String previousImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUpdateDeleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve blog details from intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        previousImageUrl = getIntent().getStringExtra("image");
        String timestamp = getIntent().getStringExtra("timestamp");
        blogId = getIntent().getStringExtra("blogId");
        Log.d(TAG, "Received blogId: " + blogId); // Debug log

        // Check if blogId is null
        if (blogId == null) {
            Log.e(TAG, "blogId is null. Cannot proceed.");
            Toast.makeText(this, "Blog ID is missing. Cannot proceed.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "blogId: " + blogId);

        // Set blog details in UI
        binding.titleUpdate.setText(title);
        binding.descriptionUpdate.setText(description);
        binding.timeUpdate.setText(timestamp);
        Glide.with(this).load(previousImageUrl).into(binding.imageUpdate);

        blogRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("posts").child(blogId);

        binding.updateBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBlog();
            }
        });

        binding.deleteBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBlog();
            }
        });

        binding.imageUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
    }



    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(binding.imageUpdate);
        }
    }

    private void updateBlog() {
        String updatedTitle = binding.titleUpdate.getText().toString().trim();
        String updatedDescription = binding.descriptionUpdate.getText().toString().trim();

        if (TextUtils.isEmpty(updatedTitle) || TextUtils.isEmpty(updatedDescription)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            // Upload new image to Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + blogId);
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String newImageUrl = uri.toString();
                    updateBlogInDatabase(updatedTitle, updatedDescription, newImageUrl);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get new image URL", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Use the previous image URL if no new image is selected
            updateBlogInDatabase(updatedTitle, updatedDescription, previousImageUrl);
        }
    }

    private void updateBlogInDatabase(String title, String description, String imageUrl) {
        // Update blog in Firebase
        blogRef.child("title").setValue(title);
        blogRef.child("description").setValue(description);
        blogRef.child("imageUrl").setValue(imageUrl);

        // Update the timestamp
        String updatedTimestamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        blogRef.child("timestamp").setValue(updatedTimestamp);

        Toast.makeText(this, "Blog updated successfully", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity after updating
    }

    private void deleteBlog() {
        // Delete blog from Firebase
        blogRef.removeValue();

        Toast.makeText(this, "Blog deleted successfully", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity after deletion
    }
}
