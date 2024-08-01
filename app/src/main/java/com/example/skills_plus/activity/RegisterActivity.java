package com.example.skills_plus.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        navigateFromActivities();
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    private void createAccount() {
        String username = binding.etUsernameRegister.getText().toString();
        String email = binding.etEmailRegister.getText().toString();
        String password = binding.etPasswordRegister.getText().toString();
        String confirmPassword = binding.etConfirmPassRegister.getText().toString();

        boolean isValidated = validateData(email, password, confirmPassword);
        if (!isValidated) {
            return;
        }

        createAccountUsingFirebase(email, password, username);
    }

    private void createAccountUsingFirebase(String email, String password, String username) {
        changeInProgress(true);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Account Created", Toast.LENGTH_SHORT).show();
                    FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser != null) {
                        storeUsersDetail(currentUser, username, email);
                        showAlertDialog();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storeUsersDetail(FirebaseUser currentUser, String username, String email) {
        String uid = currentUser.getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("userDetail");

        // Create a user map
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("useremail", email);

        // Store user details in the database
        databaseRef.setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "User details stored successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to store user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean validateData(String email, String password, String confirmPassword) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailRegister.setError("Email is Invalid");
            return false;
        }
        if (password.length() < 6) {
            binding.etPasswordRegister.setError("Password length should be greater than 6");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassRegister.setError("Password not matched");
            return false;
        }
        return true;
    }

    void changeInProgress(boolean inProgress) {
        if (inProgress) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnRegister.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setVisibility(View.VISIBLE);
        }
    }

    private void navigateFromActivities() {
        binding.loginInReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("Attention!");
        builder.setMessage("Check your Email for verification");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                auth.getCurrentUser().sendEmailVerification();
                auth.signOut();
                finish();
            }
        });

        // Create and show the Alert Dialog box
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
