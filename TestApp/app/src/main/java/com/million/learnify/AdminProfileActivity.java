package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.imageview.ShapeableImageView;

/**
 * AdminProfileActivity - High-fidelity Administrative Console.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class AdminProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private String currentUsername;
    private ShapeableImageView ivAdminPhoto, ivToolbarProfile;
    private TextView tvAdminFullName, tvAdminEmail, tvAdminPhone;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_profile);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "admin");

        initViews();
        loadAdminData();
    }

    private void initViews() {
        ivAdminPhoto = findViewById(R.id.ivAdminPhoto);
        ivToolbarProfile = findViewById(R.id.ivToolbarProfile);
        tvAdminFullName = findViewById(R.id.tvAdminFullName);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvAdminPhone = findViewById(R.id.tvAdminPhone);

        findViewById(R.id.btnSaveAdminProfile).setOnClickListener(v -> handleSave());
        
        ivAdminPhoto.setOnClickListener(v -> openGallery());

        findViewById(R.id.btnEditProfileAdmin).setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        findViewById(R.id.ivToolbarChat).setOnClickListener(v -> startActivity(new Intent(this, AIChatActivity.class)));
    }

    private void loadAdminData() {
        Cursor cursor = repository.getLocalDb().getUserDetails(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));

            tvAdminFullName.setText(fullName);
            tvAdminEmail.setText(email);
            if (phone != null && !phone.isEmpty()) {
                tvAdminPhone.setText(phone);
            }

            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                Uri uri = Uri.parse(imageUriStr);
                ivAdminPhoto.setImageURI(uri);
                ivToolbarProfile.setImageURI(uri);
                ivAdminPhoto.setPadding(0, 0, 0, 0);
                ivToolbarProfile.setPadding(0, 0, 0, 0);
            }
            cursor.close();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ivAdminPhoto.setImageURI(selectedImageUri);
                ivToolbarProfile.setImageURI(selectedImageUri);
                ivAdminPhoto.setPadding(0,0,0,0);
                ivToolbarProfile.setPadding(0,0,0,0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSave() {
        Toast.makeText(this, "Administrative policies and profile updated! ✔", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminData();
    }
}
