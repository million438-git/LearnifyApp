package com.example.testapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

/**
 * AdminRegisterActivity handles the administrator registration process.
 * Updated to support profile image selection from gallery.
 */
public class AdminRegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etFullName, etUniqueId, etEmail, etPassword, etConfirmPassword, etRole;
    private ImageView adminProfileImage;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        initViews();

        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                Toast.makeText(this, "Verification Successful! Sent for Super-Admin Approval.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void initViews() {
        // Back Button
        findViewById(R.id.btnAdminBack).setOnClickListener(v -> finish());

        // Profile Image Selection
        adminProfileImage = findViewById(R.id.adminProfileImage);
        findViewById(R.id.btnAdminCamera).setOnClickListener(v -> openGallery());

        // Input Mappings
        etFullName = findViewById(R.id.etAdminFullName);
        etUniqueId = findViewById(R.id.etAdminUniqueId);
        etEmail = findViewById(R.id.etAdminRegEmail);
        etRole = findViewById(R.id.actvAdminRole);
        etPassword = findViewById(R.id.etAdminRegPassword);
        etConfirmPassword = findViewById(R.id.etAdminConfirmPassword);
        btnRegister = findViewById(R.id.btnAdminRegister);
        
        etRole.setOnClickListener(v -> {
            Toast.makeText(this, "Default Role: Normal Admin selected.", Toast.LENGTH_SHORT).show();
        });
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

    private boolean validateInputs() {
        String fullName = etFullName.getText().toString().trim();
        String uniqueId = etUniqueId.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || uniqueId.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "All security fields are mandatory", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!uniqueId.startsWith("ACE-ADMIN-")) {
            Toast.makeText(this, "SECURITY ALERT: Unauthorized Admin ID detected.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Security Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (pass.length() < 8) {
            Toast.makeText(this, "Password too weak for Admin Access", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
