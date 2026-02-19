package com.example.testapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * RegisterActivity handles the student registration process.
 * Fixed the Grade selection field to be interactive and clickable.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText fullNameEditText, emailEditText, studentIdEditText, usernameEditText, passwordEditText;
    private AutoCompleteTextView gradeDropdown;
    private ImageView profileImage;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupGradeDropdown();
    }

    private void initViews() {
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Profile Image Selection
        profileImage = findViewById(R.id.profileImage);
        findViewById(R.id.btnCamera).setOnClickListener(v -> openGallery());

        // Mapping to new XML IDs
        fullNameEditText = findViewById(R.id.regFullName);
        emailEditText = findViewById(R.id.regEmail);
        studentIdEditText = findViewById(R.id.regStudentId);
        gradeDropdown = findViewById(R.id.regGrade);
        usernameEditText = findViewById(R.id.regUsername);
        passwordEditText = findViewById(R.id.regPassword);

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> handleRegistration());

        findViewById(R.id.loginRedirect).setOnClickListener(v -> finish());
    }

    private void setupGradeDropdown() {
        // Defined standard grades: 9, 10, 11, 12
        String[] grades = {"9", "10", "11", "12"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, grades);
        gradeDropdown.setAdapter(adapter);
        
        // Ensure the dropdown opens on click
        gradeDropdown.setOnClickListener(v -> gradeDropdown.showDropDown());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            profileImage.setTag(imageUri.toString());
        }
    }

    private void handleRegistration() {
        String fullName = getText(fullNameEditText);
        String email = getText(emailEditText);
        String studentId = getText(studentIdEditText);
        String grade = gradeDropdown.getText().toString().trim();
        String username = getText(usernameEditText);
        String password = getText(passwordEditText);
        String imageUriStr = profileImage.getTag() != null ? profileImage.getTag().toString() : "";

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || studentId.isEmpty() || grade.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fixed: Added the 'STUDENT' user_type parameter to match DatabaseHelper.registerUser signature
        boolean isRegistered = dbHelper.registerUser(fullName, studentId, grade, username, email, password, imageUriStr, "STUDENT");

        if (isRegistered) {
            Toast.makeText(this, "Welcome to AceGrade, " + fullName + "! Please login.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Username might already exist.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(EditText editText) {
        return (editText.getText() != null) ? editText.getText().toString().trim() : "";
    }
}
