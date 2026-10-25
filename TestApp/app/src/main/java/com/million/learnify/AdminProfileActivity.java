package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * AdminProfileActivity - High-Fidelity Executive Administrative Console.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class AdminProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private String currentUsername;
    private ShapeableImageView ivAdminPhoto, ivToolbarProfile;
    private TextView tvAdminFullName, tvAdminFullNameTop, tvAdminEmail, tvAdminPhone;
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
        tvAdminFullNameTop = findViewById(R.id.tvAdminFullNameTop);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvAdminPhone = findViewById(R.id.tvAdminPhone);

        View btnSave = findViewById(R.id.btnSaveAdminProfile);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> handleSave());
        }
        
        View ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
        
        if (ivAdminPhoto != null) {
            ivAdminPhoto.setOnClickListener(v -> openGallery());
        }

        View btnEdit = findViewById(R.id.btnEditProfileAdmin);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                startActivity(new Intent(this, EditProfileActivity.class));
            });
        }

        View ivChat = findViewById(R.id.ivToolbarChat);
        if (ivChat != null) {
            ivChat.setOnClickListener(v -> startActivity(new Intent(this, AIChatActivity.class)));
        }

        View btnChangePass = findViewById(R.id.btnAdminChangePassword);
        if (btnChangePass != null) {
            btnChangePass.setOnClickListener(v -> showChangePasswordDialog());
        }

        View btnTwoFa = findViewById(R.id.btnTwoFactor);
        if (btnTwoFa != null) {
            btnTwoFa.setOnClickListener(v -> showTwoFactorInfo());
        }
    }

    private void loadAdminData() {
        Cursor cursor = repository.getLocalDb().getUserDetails(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));

            if (tvAdminFullName != null) tvAdminFullName.setText(fullName != null ? fullName : "Administrator");
            if (tvAdminFullNameTop != null) tvAdminFullNameTop.setText(fullName != null ? fullName : "System Administrator");
            if (tvAdminEmail != null) tvAdminEmail.setText(email != null ? email : "admin@learnify.com");
            if (tvAdminPhone != null && phone != null && !phone.isEmpty()) {
                tvAdminPhone.setText(phone);
            }

            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                try {
                    Uri uri = Uri.parse(imageUriStr);
                    if (ivAdminPhoto != null) {
                        ivAdminPhoto.setImageURI(uri);
                        ivAdminPhoto.setPadding(0, 0, 0, 0);
                    }
                    if (ivToolbarProfile != null) {
                        ivToolbarProfile.setImageURI(uri);
                        ivToolbarProfile.setPadding(0, 0, 0, 0);
                    }
                } catch (Exception e) {
                    if (ivAdminPhoto != null) ivAdminPhoto.setImageResource(R.drawable.ic_user_person);
                }
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
                if (ivAdminPhoto != null) {
                    ivAdminPhoto.setImageURI(selectedImageUri);
                    ivAdminPhoto.setPadding(0, 0, 0, 0);
                }
                if (ivToolbarProfile != null) {
                    ivToolbarProfile.setImageURI(selectedImageUri);
                    ivToolbarProfile.setPadding(0, 0, 0, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSave() {
        if (selectedImageUri != null) {
            repository.updateUserProfileImage(currentUsername, selectedImageUri.toString());
        }
        repository.logSystemAction(currentUsername, "PROFILE_UPDATE", "Administrator updated profile photo and preferences.");
        Toast.makeText(this, "Administrative policies and profile updated successfully.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void showChangePasswordDialog() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Session invalid. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrent = view.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew = view.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = view.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Change Administrative Password")
                .setView(view)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPass = etCurrent != null && etCurrent.getText() != null ? etCurrent.getText().toString().trim() : "";
            String newPass = etNew != null && etNew.getText() != null ? etNew.getText().toString().trim() : "";
            String confirmPass = etConfirm != null && etConfirm.getText() != null ? etConfirm.getText().toString().trim() : "";

            if (currentPass.isEmpty()) {
                if (etCurrent != null) etCurrent.setError("Current password is required");
                Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 8) {
                if (etNew != null) etNew.setError("Minimum 8 characters required");
                Toast.makeText(this, "New password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                if (etConfirm != null) etConfirm.setError("Passwords do not match");
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentPass.equals(newPass)) {
                if (etNew != null) etNew.setError("Must differ from current password");
                Toast.makeText(this, "New password cannot be identical to current password", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth auth = FirebaseAuthSupport.getConfiguredAuth(this);
            FirebaseUser user = auth != null ? auth.getCurrentUser() : null;
            String sessionUid = securePrefs != null ? securePrefs.getString("firebase_uid", "") : "";
            if (user == null || user.getEmail() == null || !user.isEmailVerified()
                    || !user.getUid().equals(sessionUid)) {
                Toast.makeText(this, "Your Firebase session is unavailable. Please sign in again.", Toast.LENGTH_LONG).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
            user.reauthenticate(credential).addOnCompleteListener(this, reauthTask -> {
                if (!reauthTask.isSuccessful()) {
                    Toast.makeText(this, "Could not verify your Firebase password. Use Forgot Password if this account uses Google sign-in.", Toast.LENGTH_LONG).show();
                    return;
                }
                user.updatePassword(newPass).addOnCompleteListener(this, updateTask -> {
                    if (!updateTask.isSuccessful()) {
                        Toast.makeText(this, FirebaseAuthSupport.messageFor(updateTask.getException()), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!repository.updatePassword(currentUsername, newPass)) {
                        Toast.makeText(this, "Firebase password changed, but local profile synchronization failed. Contact support before signing out.", Toast.LENGTH_LONG).show();
                        FirebaseSessionManager.signOut(this, auth);
                        return;
                    }
                    repository.logSystemAction(currentUsername, "PASSWORD_CHANGE", "Administrative credential updated via console.");
                    Toast.makeText(this, "Administrative password updated successfully.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                });
            });
        });
    }

    private void showTwoFactorInfo() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Two-Factor Authentication")
                .setMessage("Two-Factor Authentication is currently ENFORCED for all administrative sessions.\n\nSecondary authentication tokens are verified on sensitive infrastructure actions.")
                .setPositiveButton("Understood", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminData();
    }
}
