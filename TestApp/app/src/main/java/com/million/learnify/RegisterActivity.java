package com.million.learnify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Random;

/**
 * RegisterActivity - Professional student onboarding with active email validation.
 * Refactored to use DataRepository for Client-Server readiness and Security.
 */
public class RegisterActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText fullNameEditText, emailEditText, studentIdEditText, usernameEditText, passwordEditText;
    private AutoCompleteTextView gradeDropdown, countryCodeDropdown;
    private TextView tvUsernameFeedback;
    private ImageView profileImage;
    private MailService mailService;
    private boolean isUsernameAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mailService = new MailService(this);

        initViews();
        setupDropdowns();
        setupListeners();
        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        profileImage = findViewById(R.id.profileImage);
        findViewById(R.id.btnCamera).setOnClickListener(v -> openGallery());

        fullNameEditText = findViewById(R.id.regFullName);
        emailEditText = findViewById(R.id.regEmail);
        studentIdEditText = findViewById(R.id.regStudentId);
        gradeDropdown = findViewById(R.id.regGrade);
        usernameEditText = findViewById(R.id.regUsername);
        passwordEditText = findViewById(R.id.regPassword);
        countryCodeDropdown = findViewById(R.id.regCountryCode);
        tvUsernameFeedback = findViewById(R.id.tvUsernameFeedback);

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> handleRegistration());

        findViewById(R.id.loginRedirect).setOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        String[] grades = {"9", "10", "11", "12"};
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, grades);
        gradeDropdown.setAdapter(gradeAdapter);
        gradeDropdown.setOnClickListener(v -> gradeDropdown.showDropDown());

        String[] countries = {
            "🇪🇹 +251 (Ethiopia)", "🇺🇸 +1 (USA)", "🇬🇧 +44 (UK)", "🇰🇪 +254 (Kenya)"
        };
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, countries);
        countryCodeDropdown.setAdapter(countryAdapter);
        countryCodeDropdown.setOnClickListener(v -> countryCodeDropdown.showDropDown());
    }

    private void setupListeners() {
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkUsernameUniqueness(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void checkUsernameUniqueness(String username) {
        if (username.isEmpty()) {
            tvUsernameFeedback.setVisibility(View.GONE);
            isUsernameAvailable = false;
            return;
        }
        tvUsernameFeedback.setVisibility(View.VISIBLE);
        
        // Security Check through Repository
        if (repository.isUsernameTaken(username)) {
            tvUsernameFeedback.setText("Username or Email already in use");
            tvUsernameFeedback.setTextColor(Color.RED);
            isUsernameAvailable = false;
        } else {
            tvUsernameFeedback.setText("Identifier is available");
            tvUsernameFeedback.setTextColor(Color.parseColor("#4CAF50"));
            isUsernameAvailable = true;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImage.setImageURI(data.getData());
            profileImage.setTag(data.getData().toString());
        }
    }

    private void handleRegistration() {
        String name = getText(fullNameEditText);
        String email = getText(emailEditText);
        String studentId = getText(studentIdEditText);
        String grade = gradeDropdown.getText().toString();
        String user = getText(usernameEditText);
        String pass = getText(passwordEditText);
        String img = profileImage.getTag() != null ? profileImage.getTag().toString() : "";

        if (name.isEmpty() || email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Mandatory fields are missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please provide a valid Gmail or work email");
            return;
        }

        if (!isUsernameAvailable) {
            Toast.makeText(this, "Identification conflict detected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Centralized Registration through Repository
        boolean registered = repository.register(name, studentId, grade, user, email, pass, img, "STUDENT");

        if (registered) {
            // First send the verification email simulation
            mailService.sendVerificationEmail(email, user);
            
            // Trigger first-time welcome notification
            NotificationHelper.showInstantNotification(this, "welcome", "Welcome to Learnify", "Glad to have you! Please verify your email to start.");
            
            Toast.makeText(this, "Success! Please check your notifications for the verification code.", Toast.LENGTH_LONG).show();
            
            Intent intent = new Intent(this, EmailVerificationActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("USERNAME", user);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Critical: Data persistence failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled for verification codes", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
