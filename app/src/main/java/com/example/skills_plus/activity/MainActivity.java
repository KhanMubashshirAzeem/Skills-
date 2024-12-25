package com.example.skills_plus.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityMainBinding;
import com.example.skills_plus.fragment.ProfileFragment;
import com.example.skills_plus.fragment.CommunityFragment;
import com.example.skills_plus.fragment.WriteFragment;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    SmoothBottomBar bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        replace(new CommunityFragment());
        bottomNavigationView = binding.bottomBar;
        buttomNavSwitcher();
    }

    // Switch case for fragment switching according to their ids
    private void buttomNavSwitcher() {
        bottomNavigationView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                switch (i) {
                    case 0:
                        replace(new CommunityFragment());
                        break;
                    case 1:
                        replace(new WriteFragment());
                        break;
                    case 2:
                        replace(new ProfileFragment());
                        break;
                }
                return true;
            }
        });
    }

    // Replace the Fragments on Frame Layout
    private void replace(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }


}
