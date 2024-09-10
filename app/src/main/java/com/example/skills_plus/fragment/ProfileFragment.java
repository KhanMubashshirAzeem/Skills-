package com.example.skills_plus.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.skills_plus.R;
import com.example.skills_plus.activity.LoginActivity;
import com.example.skills_plus.activity.SavedBlogActivity;
import com.example.skills_plus.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class ProfileFragment extends Fragment {

    private static final int SELECT_PICTURE = 200;
    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading profile image...");
        progressDialog.setCancelable(false);

        setListeners();
        retrieveUserDetails();

        return binding.getRoot();
    }

    /**
     * Set listeners for buttons and image view.
     */
    private void setListeners() {
        binding.logoutBtn.setOnClickListener(view -> showLogoutConfirmationDialog());
        binding.savedBlog.setOnClickListener(view -> startActivity(new Intent(getContext(), SavedBlogActivity.class)));
        binding.imageProfile.setOnClickListener(view -> chooseImage());
    }

    /**
     * Opens the image chooser to select a profile picture.
     */
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && requestCode == SELECT_PICTURE) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                binding.imageProfile.setImageURI(selectedImageUri);
                uploadImageToDB();
            }
        }
    }

    /**
     * Uploads the selected image to Firebase Storage and saves its URL in Firebase Realtime Database.
     */
    private void uploadImageToDB() {
        if (selectedImageUri != null) {
            progressDialog.show(); // Show progress dialog

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = currentUser.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("userDetail").child("profilePhoto");

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            String filename = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageReference.child("profile/" + filename);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            saveImageUrlToDatabase(databaseRef, downloadUri);
                        } else {
                            showToast("Failed to get download URL");
                            progressDialog.dismiss(); // Dismiss progress dialog
                        }
                    }))
                    .addOnFailureListener(e -> {
                        showToast("Image upload failed: " + e.getMessage());
                        progressDialog.dismiss(); // Dismiss progress dialog
                    });
        } else {
            showToast("No image selected");
        }
    }

    /**
     * Saves the image URL to Firebase Realtime Database.
     *
     * @param databaseRef  Reference to the Firebase database location.
     * @param downloadUri  URI of the uploaded image.
     */
    private void saveImageUrlToDatabase(DatabaseReference databaseRef, Uri downloadUri) {
        databaseRef.setValue(downloadUri.toString()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Image Saved To Database");
                if (isAdded()) { // Ensure the fragment is added before performing Glide operation
                    Glide.with(getContext()).load(downloadUri).into(binding.imageProfile);
                }
            } else {
                showToast("Failed to save image URL to database");
            }
            progressDialog.dismiss(); // Dismiss progress dialog
        });
    }

    /**
     * Retrieves user details and profile photo from Firebase Realtime Database.
     */
    private void retrieveUserDetails() {
        progressDialog.show(); // Show progress dialog

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("userDetail");

            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        setUserData(snapshot);
                    } else {
                        Log.e("UserDetail", "No user details found");
                    }
                    progressDialog.dismiss(); // Dismiss progress dialog
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("UserDetail", "Failed to fetch user details", error.toException());
                    progressDialog.dismiss(); // Dismiss progress dialog
                }
            });
        } else {
            Log.e("UserDetail", "No user logged in");
            progressDialog.dismiss(); // Dismiss progress dialog
        }
    }

    /**
     * Sets the user data (name, email, profile photo) in the UI.
     *
     * @param snapshot  DataSnapshot containing user details.
     */
    private void setUserData(DataSnapshot snapshot) {
        String username = snapshot.child("username").getValue(String.class);
        String useremail = snapshot.child("useremail").getValue(String.class);
        String profilePhotoUrl = snapshot.child("profilePhoto").getValue(String.class);

        if (username != null) {
            binding.userNameProfile.setText(username);
        }

        if (useremail != null) {
            binding.userEmailProfile.setText(useremail);
        }

        if (profilePhotoUrl != null) {
            if (isAdded()) { // Ensure the fragment is added before performing Glide operation
                Glide.with(getContext()).load(profilePhotoUrl).into(binding.imageProfile);
            }
        } else {
            binding.imageProfile.setImageResource(R.drawable.person_icon_bold); // Set your default image resource here
        }
    }

    /**
     * Displays a confirmation dialog for logout.
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    auth.signOut();
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    getActivity().finish(); // Ensure the user cannot return to the ProfileFragment
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Shows a toast message.
     *
     * @param message  Message to be displayed.
     */
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
