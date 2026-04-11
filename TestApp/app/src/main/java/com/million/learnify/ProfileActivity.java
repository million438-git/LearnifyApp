package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

/**
 * ProfileActivity - Updated to display student profile identity and active academic stats.
 * Refactored to use DataRepository for Client-Server readiness.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class ProfileActivity extends BaseActivity {

    private String currentUsername;
    private ImageView ivProfilePic;
    private TextView tvName, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Student users to enter this portal.
        enforceRole("STUDENT");
        
        setContentView(R.layout.activity_profile);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        initViews();
        setupToolbar();
        displayUserData();
    }

    private void initViews() {
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);

        MaterialButton btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardAvgScore).setOnClickListener(v -> {
            Toast.makeText(this, "Detailed Score Report coming soon!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.cardQuizzes).setOnClickListener(v -> {
            Toast.makeText(this, "Opening Quiz History...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayUserData() {
        // Fetch details via Repository (Abstraction Layer)
        Cursor cursor = repository.getUserDetails(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));

            tvName.setText(fullName);
            tvEmail.setText(email);

            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                try {
                    ivProfilePic.setImageURI(Uri.parse(imageUriStr));
                    ivProfilePic.setPadding(0, 0, 0, 0); 
                } catch (Exception e) {
                    ivProfilePic.setImageResource(android.R.drawable.ic_menu_myplaces);
                }
            }
            cursor.close();
        } else {
            tvName.setText(currentUsername);
            tvEmail.setText(currentUsername.toLowerCase() + "@acegrade.com");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from Edit Profile
        displayUserData();
    }
}
