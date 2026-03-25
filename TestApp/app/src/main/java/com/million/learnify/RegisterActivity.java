package com.million.learnify;

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
 * Updated with Grade dropdown and Country Code selection.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText fullNameEditText, emailEditText, studentIdEditText, usernameEditText, passwordEditText, phoneEditText;
    private AutoCompleteTextView gradeDropdown, countryCodeDropdown;
    private ImageView profileImage;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupDropdowns();
    }

    private void initViews() {
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Profile Image Selection
        profileImage = findViewById(R.id.profileImage);
        findViewById(R.id.btnCamera).setOnClickListener(v -> openGallery());

        // Mapping to XML IDs
        fullNameEditText = findViewById(R.id.regFullName);
        emailEditText = findViewById(R.id.regEmail);
        studentIdEditText = findViewById(R.id.regStudentId);
        gradeDropdown = findViewById(R.id.regGrade);
        usernameEditText = findViewById(R.id.regUsername);
        passwordEditText = findViewById(R.id.regPassword);
        phoneEditText = findViewById(R.id.regPhone);
        countryCodeDropdown = findViewById(R.id.regCountryCode);

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> handleRegistration());

        findViewById(R.id.loginRedirect).setOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // 1. Grade Dropdown
        String[] grades = {"9", "10", "11", "12"};
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, grades);
        gradeDropdown.setAdapter(gradeAdapter);
        gradeDropdown.setOnClickListener(v -> gradeDropdown.showDropDown());

        // 2. Country Code Dropdown (10 Countries)
        String[] countries = {
            "🇪🇹 +251 (Ethiopia)",
            "🇺🇸 +1 (USA)",
            "🇬🇧 +44 (UK)",
            "🇰🇪 +254 (Kenya)",
            "🇳🇬 +234 (Nigeria)",
            "🇮🇳 +91 (India)",
            "🇨🇦 +1 (Canada)",
            "🇦🇪 +971 (UAE)",
            "🇿🇦 +27 (South Africa)",
            "🇩🇪 +49 (Germany)"
        };
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, countries);
        countryCodeDropdown.setAdapter(countryAdapter);
        countryCodeDropdown.setOnClickListener(v -> countryCodeDropdown.showDropDown());
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
        String countryCode = countryCodeDropdown.getText().toString().trim();
        String phoneNumber = getText(phoneEditText);
        String imageUriStr = profileImage.getTag() != null ? profileImage.getTag().toString() : "";

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || studentId.isEmpty() || grade.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Full phone number string
        String fullPhone = countryCode + " " + phoneNumber;

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
