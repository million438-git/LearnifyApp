package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class PrivacySecurityActivity extends AppCompatActivity {

    private LinearLayout llChangePassword, llTwoFactor, llExportData, llDeleteAccount;
    private SettingsService settingsService;
    private StudentSettings currentSettings;
    private DataRepository repository;
    private SecurePreferencesHelper securePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsService = new SettingsService(this);
        currentSettings = settingsService.loadSettings();
        repository = DataRepository.getInstance(this);
        securePrefs = new SecurePreferencesHelper(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_security);

        setupToolbar();
        initViews();
        setupListeners();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.privacyToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        llChangePassword = findViewById(R.id.llChangePassword);
        llTwoFactor = findViewById(R.id.llTwoFactor);
        llExportData = findViewById(R.id.llExportData);
        llDeleteAccount = findViewById(R.id.llDeleteAccount);
    }

    private void setupListeners() {
        if (llChangePassword != null) {
            llChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        if (llTwoFactor != null) {
            llTwoFactor.setOnClickListener(v -> {
                boolean isEnabled = currentSettings.isTwoFactorEnabled();
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Two-Factor Authentication")
                        .setMessage("Status: " + (isEnabled ? "Enabled" : "Disabled") + "\n\n2FA adds an extra layer of security to your account by requiring more than just a password to log in.")
                        .setPositiveButton(isEnabled ? "Disable" : "Enable", (dialog, which) -> {
                            currentSettings.setTwoFactorEnabled(!isEnabled);
                            settingsService.saveSettings(currentSettings);
                            Toast.makeText(this, "2FA " + (currentSettings.isTwoFactorEnabled() ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (llExportData != null) {
            llExportData.setOnClickListener(v -> showExportDataDialog());
        }

        if (llDeleteAccount != null) {
            llDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }
    }

    private void showExportDataDialog() {
        String username = getAuthenticatedUser();
        if (username.isEmpty()) {
            Toast.makeText(this, "Session invalid. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Export Learning Records")
                .setMessage("We will generate a verified JSON archive containing your academic profile, enrolled courses, certificates, quiz & exam results, and learning streak statistics.\n\nSensitive security credentials (passwords, salts, auth tokens) are strictly excluded.")
                .setPositiveButton("Export & Share", (dialog, which) -> performDataExport(username))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDataExport(String username) {
        try {
            JSONObject exportData = repository.exportUserData(username);
            File exportDir = new File(getCacheDir(), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File exportFile = new File(exportDir, "learnify_data_export.json");
            try (FileOutputStream fos = new FileOutputStream(exportFile)) {
                fos.write(exportData.toString(4).getBytes(StandardCharsets.UTF_8));
            }

            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", exportFile);

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("application/json");
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Learnify Academic Data Export - " + username);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Attached is the educational progress export for Learnify user @" + username + ".");
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(sendIntent, "Export My Data"));
            Toast.makeText(this, "Data archive prepared! Select an app to share or save.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("PrivacySecurity", "Failed to export user data", e);
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteAccountDialog() {
        String username = getAuthenticatedUser();
        if (username.isEmpty()) {
            Toast.makeText(this, "Session invalid. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_account, null);
        TextInputEditText etPassword = view.findViewById(R.id.etDeletePassword);
        TextInputEditText etConfirmPhrase = view.findViewById(R.id.etDeleteConfirmPhrase);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Account Deletion")
                .setView(view)
                .setPositiveButton("Delete Permanently", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String enteredPassword = etPassword != null && etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            String enteredPhrase = etConfirmPhrase != null && etConfirmPhrase.getText() != null ? etConfirmPhrase.getText().toString().trim() : "";

            if (enteredPassword.isEmpty()) {
                if (etPassword != null) etPassword.setError("Password is required");
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!"DELETE".equals(enteredPhrase)) {
                if (etConfirmPhrase != null) etConfirmPhrase.setError("Type DELETE in all capital letters");
                Toast.makeText(this, "Please type DELETE to confirm", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isPasswordCorrect = repository.verifyUserPassword(username, enteredPassword);
            if (!isPasswordCorrect) {
                if (etPassword != null) etPassword.setError("Incorrect password");
                Toast.makeText(this, "Incorrect password. Cannot proceed.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean deleted = repository.deleteUserAccount(username);
            if (deleted) {
                FirebaseSessionManager.signOut(this, FirebaseAuthSupport.getConfiguredAuth(this));

                Toast.makeText(this, "Account and associated data deleted permanently.", Toast.LENGTH_LONG).show();
                dialog.dismiss();

                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Account deletion failed. Please contact administrator.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getAuthenticatedUser() {
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        String user = session.getString("username", "");
        if ((user == null || user.trim().isEmpty()) && securePrefs != null) {
            user = securePrefs.getString("username", "");
        }
        return user != null ? user.trim() : "";
    }

    private void refreshUserSession() {
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        session.edit()
                .putBoolean("isLoggedIn", true)
                .putLong("last_password_change", System.currentTimeMillis())
                .apply();

        if (securePrefs != null) {
            securePrefs.saveBoolean("isLoggedIn", true);
        }
    }

    private void showChangePasswordDialog() {
        String username = getAuthenticatedUser();
        if (username.isEmpty()) {
            Toast.makeText(this, "Session invalid. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrent = view.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew = view.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = view.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Change Password")
                .setView(view)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Custom listener prevents premature dialog dismissal when inputs fail validation
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
            String sessionUid = securePrefs.getString("firebase_uid", "");
            if (user == null || user.getEmail() == null || !user.isEmailVerified()
                    || !user.getUid().equals(sessionUid)) {
                Toast.makeText(this, "Your Firebase session is unavailable. Please sign in again.", Toast.LENGTH_LONG).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
            user.reauthenticate(credential).addOnCompleteListener(this, reauthTask -> {
                if (!reauthTask.isSuccessful()) {
                    Toast.makeText(this, "Could not verify your current Firebase password. Please try again or use Forgot Password.", Toast.LENGTH_LONG).show();
                    return;
                }
                user.updatePassword(newPass).addOnCompleteListener(this, updateTask -> {
                    if (!updateTask.isSuccessful()) {
                        Toast.makeText(this, FirebaseAuthSupport.messageFor(updateTask.getException()), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!repository.updatePassword(username, newPass)) {
                        Toast.makeText(this, "Firebase password changed, but local profile synchronization failed. Contact support before signing out.", Toast.LENGTH_LONG).show();
                        FirebaseSessionManager.signOut(this, auth);
                        return;
                    }
                    refreshUserSession();
                    repository.logSystemAction(username, "PASSWORD_CHANGE", "User successfully changed account password");
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            });
        });
    }
}
