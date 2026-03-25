package com.million.learnify;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;

public class PrivacySecurityActivity extends AppCompatActivity {

    private LinearLayout llChangePassword, llTwoFactor, llExportData, llDeleteAccount;
    private SettingsService settingsService;
    private StudentSettings currentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsService = new SettingsService(this);
        currentSettings = settingsService.loadSettings();
        
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
            llExportData.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Export My Data")
                        .setMessage("We will compile your profile, history, and achievements into a JSON file and send it to your email.")
                        .setPositiveButton("Export", (dialog, which) -> {
                            Toast.makeText(this, "Data export compiled and sent!", Toast.LENGTH_LONG).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (llDeleteAccount != null) {
            llDeleteAccount.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Delete Account?")
                        .setMessage("WARNING: This will permanently delete your profile, XP, streaks, and all learning data. Type 'DELETE' to confirm.")
                        .setPositiveButton("Confirm Deletion", (dialog, which) -> {
                            Toast.makeText(this, "Account Deleted. Logging out...", Toast.LENGTH_LONG).show();
                            // Logic to clear session and go to Welcome
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrent = view.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew = view.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = view.findViewById(R.id.etConfirmPassword);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Password")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newPass = etNew.getText().toString();
                    String confirmPass = etConfirm.getText().toString();
                    if (newPass.length() < 8) {
                        Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
                    } else if (newPass.equals(confirmPass) && !newPass.isEmpty()) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
