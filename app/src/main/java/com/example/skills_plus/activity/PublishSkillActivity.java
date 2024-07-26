package com.example.skills_plus.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityPublishSkillBinding;
import com.example.skills_plus.fragment.WriteFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class PublishSkillActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 200;
    private Uri selectedImageUri;

    ActivityPublishSkillBinding binding;

    String Title, Description, Image, PostId;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Inflate the layout using view binding
        binding = ActivityPublishSkillBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle system window insets for proper layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set onClickListener to trigger image selection
        binding.uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        // Set onClickListener to upload data to Firebase when the publish button is clicked
        binding.publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDataToFirebase();
            }
        });





    }

    // Function to open image chooser
    void imageChooser() {
        // Create an instance of intent of type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // Function triggered when user selects the image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is OK and the request code matches
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            // Get the URI of the selected image
            selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                // Update the preview image in the layout
                binding.displayImage.setImageURI(selectedImageUri);
                // Hide the upload button
                binding.uploadImage.setVisibility(View.GONE);
            }
        }
    }

    // Function to upload data to Firebase
    private void uploadDataToFirebase() {
        if (selectedImageUri != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.publishBtn.setVisibility(View.GONE);
            // Initialize Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            // Generate a unique file name for the image
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageReference.child("images/" + fileName);

            // Upload the image to Firebase Storage
            imageRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Get the download URL of the uploaded image
                    imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                // Store the data in the database along with the image URL
                                storeDataInDatabase(downloadUri.toString());
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // Show error message if the image upload fails
                    Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Show a message if no image is selected
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to store data in the database
    private void storeDataInDatabase(String imageUrl) {
        // Get the title and description from the input fields
        String title = binding.addTitle.getText().toString();
        String description = binding.addDescription.getText().toString();
        // Get the current timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Get the current user's UID and name
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        String userName = currentUser.getDisplayName();

        // Reference to the user's posts
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("posts");
        String postId = databaseRef.push().getKey();

        // Prepare the post data
        Map<String, Object> postData = new HashMap<>();
        postData.put("title", title);
        postData.put("description", description);
        postData.put("imageUrl", imageUrl);
        postData.put("timestamp", timestamp);
        postData.put("userName", userName); // Add the user's name to the post data

        // Save the post data to the database
        databaseRef.child(postId).setValue(postData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    // Show a success message and clear the input fields
                    Toast.makeText(getApplicationContext(), "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.publishBtn.setVisibility(View.VISIBLE);
                    binding.addTitle.setText("");
                    binding.addDescription.setText("");
                    binding.displayImage.setImageBitmap(null);
                    // Redirect to another fragment or activity
                    Intent intent = new Intent(getApplicationContext(), WriteFragment.class);
                    finish();
                } else {
                    // Show an error message if the post upload fails
                    Toast.makeText(getApplicationContext(), "Post upload failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}