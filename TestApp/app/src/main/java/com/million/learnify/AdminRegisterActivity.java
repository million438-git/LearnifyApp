package com.million.learnify;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;

import java.util.Random;

/**
 * AdminRegisterActivity handles the administrator registration process.
 * Refactored to use DataRepository for Client-Server readiness and Security.
 * Inherits from BaseActivity for unified theme and repository access.
 */
public class AdminRegisterActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etFullName, etUniqueId, etEmail, etPassword, etConfirmPassword, etRole, etUsername;
    private TextView tvUsernameFeedback;
    private ImageView adminProfileImage;
    private MaterialButton btnRegister;
    private boolean isUsernameAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to register other Admins.
        // If this is the first run, ensure 'admin' exists in DatabaseHelper seed.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        findViewById(R.id.btnAdminBack).setOnClickListener(v -> finish());
        adminProfileImage = findViewById(R.id.adminProfileImage);
        findViewById(R.id.btnAdminCamera).setOnClickListener(v -> openGallery());

        etFullName = findViewById(R.id.etAdminFullName);
        etUniqueId = findViewById(R.id.etAdminUniqueId);
        etEmail = findViewById(R.id.etAdminRegEmail);
        etUsername = findViewById(R.id.etAdminRegUsername);
        etRole = findViewById(R.id.actvAdminRole);
        etPassword = findViewById(R.id.etAdminRegPassword);
        etConfirmPassword = findViewById(R.id.etAdminConfirmPassword);
        btnRegister = findViewById(R.id.btnAdminRegister);
        tvUsernameFeedback = findViewById(R.id.tvAdminUsernameFeedback);
        btnRegister.setEnabled(false);
        btnRegister.setText("PROVISIONING IS SERVER-MANAGED");
        
        etRole.setOnClickListener(v -> {
            Toast.makeText(this, "Default Role: Normal Admin selected.", Toast.LENGTH_SHORT).show();
        });

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void setupListeners() {
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkUsernameUniqueness(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etUsername.getText().toString().isEmpty() && s.length() > 3) {
                    suggestUsername(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkUsernameUniqueness(String username) {
        if (username.isEmpty()) {
            tvUsernameFeedback.setVisibility(View.GONE);
            isUsernameAvailable = false;
            return;
        }

        tvUsernameFeedback.setVisibility(View.VISIBLE);
        if (repository.isUsernameTaken(username)) {
            tvUsernameFeedback.setText("Username already exists");
            tvUsernameFeedback.setTextColor(Color.parseColor("#FF5252"));
            isUsernameAvailable = false;
        } else {
            tvUsernameFeedback.setText("Username is available");
            tvUsernameFeedback.setTextColor(Color.parseColor("#4CAF50"));
            isUsernameAvailable = true;
        }
    }

    private void suggestUsername(String fullName) {
        String base = fullName.toLowerCase().replaceAll("\\s+", "");
        if (base.isEmpty()) return;
        
        String suggestion = base + "_admin" + (new Random().nextInt(90) + 10);
        if (!repository.isUsernameTaken(suggestion)) {
            etUsername.setText(suggestion);
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
            Uri imageUri = data.getData();
            adminProfileImage.setImageURI(imageUri);
            adminProfileImage.setTag(imageUri.toString());
        }
    }

    private void handleRegistration() {
        Toast.makeText(
                this,
                "Administrator accounts and claims must be provisioned by Learnify security staff.",
                Toast.LENGTH_LONG).show();
    }

    private boolean validateInputs() {
        String fullName = etFullName.getText().toString().trim();
        String uniqueId = etUniqueId.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()) { etFullName.setError("Full name required"); return false; }
        if (user.isEmpty()) { etUsername.setError("Username required"); return false; }
        if (uniqueId.isEmpty()) { etUniqueId.setError("Admin ID required"); return false; }
        if (email.isEmpty()) { etEmail.setError("Email required"); return false; }
        if (pass.isEmpty()) { etPassword.setError("Password required"); return false; }

        if (!isUsernameAvailable) {
            Toast.makeText(this, "Please use a unique username", Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return false;
        }

        if (!uniqueId.startsWith("ACE-ADMIN-")) {
            Toast.makeText(this, "SECURITY ALERT: Unauthorized Admin ID detected.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!pass.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        if (pass.length() < 8) {
            etPassword.setError("Password too weak (min 8 chars)");
            return false;
        }

        return true;
    }
}
