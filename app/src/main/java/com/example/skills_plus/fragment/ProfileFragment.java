package com.example.skills_plus.fragment;

import android.app.AlertDialog;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutConfirmationDialog();
            }
        });

        binding.savedBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SavedBlogActivity.class));
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        retrieveUserDetails();

        return binding.getRoot();
    }

    private void chooseImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
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

    private void uploadImageToDB() {
        if (selectedImageUri != null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = currentUser.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("userDetail").child("profilePhoto");

            // Initialize Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            // Generating the unique file name for the image
            String filename = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageReference.child("profile/" + filename);

            imageRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Get the download URL of the uploaded image
                    imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                // Store the data in the database along with the image URL
                                databaseRef.setValue(downloadUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Image Saved To Database", Toast.LENGTH_SHORT).show();
                                            // Load the new profile photo into the ImageView
                                            Glide.with(getContext()).load(downloadUri).into(binding.imageProfile);
                                        } else {
                                            Toast.makeText(getContext(), "Failed to save image URL to database", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void retrieveUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("userDetail");

            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String useremail = snapshot.child("useremail").getValue(String.class);
                        String profilePhotoUrl = snapshot.child("profilePhoto").getValue(String.class);

                        binding.usernameProfile.setText(username);
                        binding.useremailProfile.setText(useremail);

                        if (profilePhotoUrl != null) {
                            Glide.with(getContext()).load(profilePhotoUrl).into(binding.imageProfile);
                        } else {
                            binding.imageProfile.setImageResource(R.drawable.person_icon_bold); // Set your default image resource here
                        }
                    } else {
                        Log.e("UserDetail", "No user details found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("UserDetail", "Failed to fetch user details", error.toException());
                }
            });
        } else {
            Log.e("UserDetail", "No user logged in");
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        auth.signOut();
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish(); // Ensure the user cannot return to the ProfileFragment
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
