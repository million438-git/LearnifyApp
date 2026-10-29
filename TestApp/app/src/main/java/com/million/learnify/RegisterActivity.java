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
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * RegisterActivity - Professional student onboarding with active input validation,
 * local SQLite preflight checks, and robust Firebase Authentication handling.
 */
public class RegisterActivity extends BaseActivity {

    private static final String TAG = "RegisterActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText fullNameEditText, emailEditText, studentIdEditText, usernameEditText, passwordEditText, phoneEditText;
    private AutoCompleteTextView gradeDropdown, countryCodeDropdown;
    private TextView tvUsernameFeedback;
    private ImageView profileImage;
    private MaterialButton registerButton;
    private boolean isSubmitting = false;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);

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
        phoneEditText = findViewById(R.id.regPhone);
        tvUsernameFeedback = findViewById(R.id.tvUsernameFeedback);

        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> handleRegistration());

        TextView loginRedirect = findViewById(R.id.loginRedirect);
        if (loginRedirect != null) {
            loginRedirect.setText(Html.fromHtml("Already have an account? <font color='#00D8FF'><b>Sign In</b></font>", Html.FROM_HTML_MODE_LEGACY));
            loginRedirect.setOnClickListener(v -> finish());
        }
    }

    private void setupDropdowns() {
        String[] grades = {"9", "10", "11", "12"};
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(this, 
                R.layout.item_dropdown_luxury, grades);
        gradeDropdown.setAdapter(gradeAdapter);
        gradeDropdown.setOnClickListener(v -> gradeDropdown.showDropDown());

        String[] countries = {
            "+251 (Ethiopia)", "+1 (USA)", "+44 (UK)", "+254 (Kenya)"
        };
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                R.layout.item_dropdown_luxury, countries);
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

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailEditText.getError() != null) {
                    emailEditText.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void checkUsernameUniqueness(String username) {
        if (username.isEmpty()) {
            tvUsernameFeedback.setVisibility(View.GONE);
            return;
        }
        tvUsernameFeedback.setVisibility(View.VISIBLE);
        
        if (repository != null && repository.isUsernameTaken(username)) {
            tvUsernameFeedback.setText("Username is already taken");
            tvUsernameFeedback.setTextColor(Color.parseColor("#FF5252"));
        } else {
            tvUsernameFeedback.setText("Username is available");
            tvUsernameFeedback.setTextColor(ContextCompat.getColor(this, R.color.learnify_science_mint));
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
        if (isSubmitting) {
            return;
        }

        // Clear previous field errors
        fullNameEditText.setError(null);
        usernameEditText.setError(null);
        emailEditText.setError(null);
        passwordEditText.setError(null);
        if (studentIdEditText != null) studentIdEditText.setError(null);

        String name = getText(fullNameEditText);
        String user = getText(usernameEditText);
        String email = getText(emailEditText);
        String pass = getText(passwordEditText);
        String studentId = getText(studentIdEditText);
        String grade = gradeDropdown.getText() != null ? gradeDropdown.getText().toString().trim() : "";
        if (TextUtils.isEmpty(grade)) {
            grade = "10";
        }
        String phone = phoneEditText != null ? getText(phoneEditText) : "";
        String countryCode = countryCodeDropdown != null && countryCodeDropdown.getText() != null
                ? countryCodeDropdown.getText().toString().trim() : "";
        String fullPhone = "";
        if (!TextUtils.isEmpty(phone)) {
            if (!TextUtils.isEmpty(countryCode) && countryCode.contains(" ")) {
                String prefix = countryCode.substring(0, countryCode.indexOf(" "));
                fullPhone = prefix + " " + phone;
            } else {
                fullPhone = phone;
            }
        }
        String img = profileImage.getTag() != null ? profileImage.getTag().toString() : "";

        // 1. Full name validation
        if (TextUtils.isEmpty(name)) {
            fullNameEditText.setError("Full name is required");
            fullNameEditText.requestFocus();
            return;
        }
        if (name.length() < 2) {
            fullNameEditText.setError("Please enter a valid full name");
            fullNameEditText.requestFocus();
            return;
        }

        // 2. Username validation
        if (TextUtils.isEmpty(user)) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return;
        }
        if (user.length() < 3) {
            usernameEditText.setError("Username must be at least 3 characters");
            usernameEditText.requestFocus();
            return;
        }
        if (user.contains(" ")) {
            usernameEditText.setError("Username cannot contain spaces");
            usernameEditText.requestFocus();
            return;
        }
        if (repository != null && repository.isUsernameTaken(user)) {
            usernameEditText.setError("This username is already taken. Please choose another.");
            usernameEditText.requestFocus();
            return;
        }

        // 3. Email validation
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email address is required");
            emailEditText.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            return;
        }
        if (repository != null && repository.isEmailRegistered(email)) {
            emailEditText.setError("An account with this email already exists. Please sign in.");
            emailEditText.requestFocus();
            return;
        }

        // 4. Password validation
        if (TextUtils.isEmpty(pass)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters long");
            passwordEditText.requestFocus();
            return;
        }

        // 5. Firebase Availability
        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);
        if (firebaseAuth == null) {
            Toast.makeText(this, "Firebase is not configured for this build. Contact Learnify support.", Toast.LENGTH_LONG).show();
            return;
        }

        setSubmitting(true);

        final String finalGrade = grade;
        final String finalPhone = fullPhone;

        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                setSubmitting(false);
                Exception ex = task.getException();
                Log.e(TAG, "Student registration failed on Firebase Auth", ex);
                String errorMsg = FirebaseAuthSupport.messageFor(ex);
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();

                if (errorMsg.contains("already exists")) {
                    emailEditText.setError("This email is already registered. Please sign in.");
                    emailEditText.requestFocus();
                } else if (errorMsg.contains("too weak")) {
                    passwordEditText.setError(errorMsg);
                    passwordEditText.requestFocus();
                }
                return;
            }

            FirebaseUser firebaseUser = task.getResult().getUser();
            try {
                FirebaseSessionManager.ensureStudentProfile(
                        this, firebaseUser, name, user, studentId, finalGrade, img, finalPhone);
            } catch (Exception e) {
                setSubmitting(false);
                Log.e(TAG, "Failed to persist local student profile", e);
                Toast.makeText(this, "Could not save student profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            // Attempt email verification send
            firebaseUser.sendEmailVerification().addOnCompleteListener(this, verificationTask -> {
                setSubmitting(false);
                if (!verificationTask.isSuccessful()) {
                    Exception vEx = verificationTask.getException();
                    Log.w(TAG, "Email verification link could not be sent immediately", vEx);
                    Toast.makeText(this, "Account created! Could not send verification email: "
                            + FirebaseAuthSupport.messageFor(vEx) + ". You can resend it on the next screen.", Toast.LENGTH_LONG).show();
                } else {
                    NotificationHelper.showInstantNotification(
                            this, "welcome", "Welcome to Learnify", "Verify your email to activate your account.");
                    Toast.makeText(this, "Account created! Check your email for verification link.", Toast.LENGTH_LONG).show();
                }

                Intent intent = new Intent(this, EmailVerificationActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("USERNAME", user);
                intent.putExtra("PROVIDER", "Password");
                startActivity(intent);
                finish();
            });
        });
    }

    private void setSubmitting(boolean submitting) {
        this.isSubmitting = submitting;
        if (registerButton != null) {
            registerButton.setEnabled(!submitting);
            registerButton.setText(submitting ? "CREATING ACCOUNT..." : "CREATE ACCOUNT");
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
