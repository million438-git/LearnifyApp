package com.example.testapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * EditProfileActivity - Handles profile updates including image selection and database persistence.
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputEditText etFullName, etUsername, etEmail;
    private AutoCompleteTextView actvGrade;
    private ImageView ivEditProfilePic;
    private MaterialButton btnSaveProfile;
    private DatabaseHelper dbHelper;
    private String currentUsername;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        setupToolbar();
        initViews();
        setupDropdowns();
        loadCurrentData();

        findViewById(R.id.btnChangePic).setOnClickListener(v -> openGallery());
        
        btnSaveProfile.setOnClickListener(v -> handleSave());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.editProfileToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        actvGrade = findViewById(R.id.actvGrade);
        ivEditProfilePic = findViewById(R.id.ivEditProfilePic);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void setupDropdowns() {
        String[] grades = {"9", "10", "11", "12"};
        actvGrade.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, grades));
        actvGrade.setOnClickListener(v -> actvGrade.showDropDown());
    }

    private void loadCurrentData() {
        Cursor cursor = dbHelper.getUserDetails(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            etFullName.setText(cursor.getString(0));
            etEmail.setText(cursor.getString(1));
            etUsername.setText(currentUsername);
            
            String imgUriStr = cursor.getString(2);
            if (imgUriStr != null && !imgUriStr.isEmpty()) {
                ivEditProfilePic.setImageURI(Uri.parse(imgUriStr));
            }
            
            String grade = cursor.getString(3);
            actvGrade.setText(grade, false);
            
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            
            // Persist URI Permission so the image stays visible after app restart
            try {
                getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            ivEditProfilePic.setImageURI(selectedImageUri);
        }
    }

    private void handleSave() {
        String newName = etFullName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newGrade = actvGrade.getText().toString().trim();
        String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // PERSIST CHANGES TO DATABASE
        boolean success = dbHelper.updateUserProfile(currentUsername, newName, newEmail, newGrade, imageUriStr);
        
        if (success) {
            Toast.makeText(this, "Identity Updated Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
        }
    }
}
