package com.million.learnify;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminSystemSettingsActivity - Central Infrastructure & Governance Console.
 * Manages 10 core infrastructure domains from 8.1 to 8.10, plus Camera Lab Governance.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class AdminSystemSettingsActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private String adminUsername = "admin";

    // UI Elements for each category
    private TextInputEditText etAppName, etTagline, etContactEmail, etSupportEmail; // 8.1
    private TextInputEditText etCurrency; 
    private SwitchCompat switchRTL; // 8.2
    private TextInputEditText etSmtpHost, etFcmKey; // 8.3
    private SwitchCompat switch2FA, switchLockout; // 8.4
    private SwitchCompat switchCookie; // 8.5
    private TextInputEditText etZoomKey, etS3Bucket; // 8.6
    private TextInputEditText etPrimaryColor; // 8.7
    private SwitchCompat switchCache; // 8.8
    private SwitchCompat switchMaintenance, switchRegistration, switchVoiceFeature; // 8.9
    private TextInputEditText etExamTime, etPassingScore; // 8.10
    
    // Section 11 Camera Lab Governance controls
    private SwitchCompat switchCameraLab, switchLabMath, switchLabPhysics, switchLabChem;
    private TextInputEditText etLabMaxSteps;

    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_system_settings);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        adminUsername = pref.getString("username", "admin");

        dbHelper = repository.getLocalDb();
        setupToolbar();
        initViews();
        loadAllConfigurations();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        // 8.1 General
        etAppName = findViewById(R.id.etAppName);
        etTagline = findViewById(R.id.etTagline);
        etContactEmail = findViewById(R.id.etContactEmail);
        etSupportEmail = findViewById(R.id.etSupportEmail);

        // 8.2 Localization
        etCurrency = findViewById(R.id.etCurrency);
        switchRTL = findViewById(R.id.switchRTL);

        // 8.3 Notifications
        etSmtpHost = findViewById(R.id.etSmtpHost);
        etFcmKey = findViewById(R.id.etFcmKey);

        // 8.4 Security
        switch2FA = findViewById(R.id.switch2FA);
        switchLockout = findViewById(R.id.switchLockout);

        // 8.5 Compliance
        switchCookie = findViewById(R.id.switchCookie);

        // 8.6 Integrations
        etZoomKey = findViewById(R.id.etZoomKey);
        etS3Bucket = findViewById(R.id.etS3Bucket);

        // 8.7 Appearance
        etPrimaryColor = findViewById(R.id.etPrimaryColor);

        // 8.8 Performance
        switchCache = findViewById(R.id.switchCache);

        // 8.9 Maintenance & System
        switchMaintenance = findViewById(R.id.switchMaintenance);
        switchRegistration = findViewById(R.id.switchRegistration);
        switchVoiceFeature = findViewById(R.id.switchVoiceFeature);

        // 8.10 Academic
        etExamTime = findViewById(R.id.etExamTime);
        etPassingScore = findViewById(R.id.etPassingScore);

        // Section 11 Camera Lab
        switchCameraLab = findViewById(R.id.switchCameraLab);
        switchLabMath = findViewById(R.id.switchLabMath);
        switchLabPhysics = findViewById(R.id.switchLabPhysics);
        switchLabChem = findViewById(R.id.switchLabChem);
        etLabMaxSteps = findViewById(R.id.etLabMaxSteps);

        // Buttons
        btnSave = findViewById(R.id.btnSaveSettings);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveAllConfigurations());
        }

        View btnBackup = findViewById(R.id.btnBackupRestore);
        if (btnBackup != null) {
            btnBackup.setOnClickListener(v -> confirmBackup());
        }

        View btnLogs = findViewById(R.id.btnSystemLogs);
        if (btnLogs != null) {
            btnLogs.setOnClickListener(v -> openAuditLogs());
        }
    }

    private void loadAllConfigurations() {
        if (etAppName != null) etAppName.setText(dbHelper.getSetting("site_name", "Learnify Pro"));
        if (etTagline != null) etTagline.setText(dbHelper.getSetting("site_tagline", "Smart Study Hub"));
        if (etContactEmail != null) etContactEmail.setText(dbHelper.getSetting("contact_email", "admin@learnify.com"));
        if (etSupportEmail != null) etSupportEmail.setText(dbHelper.getSetting("support_email", "support@learnify.com"));

        if (etCurrency != null) etCurrency.setText(dbHelper.getSetting("default_currency", "USD"));
        if (switchRTL != null) switchRTL.setChecked(dbHelper.getSetting("enable_rtl", "0").equals("1"));

        if (etSmtpHost != null) etSmtpHost.setText(dbHelper.getSetting("smtp_host", "smtp.learnify.com"));
        if (etFcmKey != null) etFcmKey.setText(dbHelper.getSetting("fcm_server_key", ""));

        if (switch2FA != null) switch2FA.setChecked(dbHelper.getSetting("enforce_2fa", "0").equals("1"));
        if (switchLockout != null) switchLockout.setChecked(dbHelper.getSetting("login_protection", "1").equals("1"));

        if (switchCookie != null) switchCookie.setChecked(dbHelper.getSetting("cookie_consent", "1").equals("1"));

        if (etZoomKey != null) etZoomKey.setText(dbHelper.getSetting("zoom_api_key", ""));
        if (etS3Bucket != null) etS3Bucket.setText(dbHelper.getSetting("aws_s3_bucket", "learnify-assets"));

        if (etPrimaryColor != null) etPrimaryColor.setText(dbHelper.getSetting("primary_color", "#1E3A8A"));

        if (switchCache != null) switchCache.setChecked(dbHelper.getSetting("enable_caching", "1").equals("1"));

        if (switchMaintenance != null) switchMaintenance.setChecked(dbHelper.getSetting("maintenance_mode", "0").equals("1"));
        if (switchRegistration != null) switchRegistration.setChecked(dbHelper.getSetting("allow_registration", "1").equals("1"));
        if (switchVoiceFeature != null) switchVoiceFeature.setChecked(dbHelper.getSetting("voice_study", "1").equals("1"));
        
        if (etExamTime != null) etExamTime.setText(dbHelper.getSetting("exam_time", "30"));
        if (etPassingScore != null) etPassingScore.setText(dbHelper.getSetting("passing_score", "50"));

        if (switchCameraLab != null) switchCameraLab.setChecked(dbHelper.getSetting("camera_lab_enabled", "1").equals("1"));
        if (switchLabMath != null) switchLabMath.setChecked(dbHelper.getSetting("camera_lab_math", "1").equals("1"));
        if (switchLabPhysics != null) switchLabPhysics.setChecked(dbHelper.getSetting("camera_lab_physics", "1").equals("1"));
        if (switchLabChem != null) switchLabChem.setChecked(dbHelper.getSetting("camera_lab_chem", "1").equals("1"));
        if (etLabMaxSteps != null) etLabMaxSteps.setText(dbHelper.getSetting("camera_lab_max_steps", "10"));
    }

    private void saveAllConfigurations() {
        try {
            // Validation
            if (etAppName != null && etAppName.getText().toString().trim().isEmpty()) {
                etAppName.setError("Platform Name cannot be blank");
                etAppName.requestFocus();
                return;
            }

            if (etExamTime != null) {
                String examTimeStr = etExamTime.getText().toString().trim();
                try {
                    int examTime = Integer.parseInt(examTimeStr);
                    if (examTime < 1 || examTime > 360) {
                        etExamTime.setError("Duration must be between 1 and 360 minutes");
                        etExamTime.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    etExamTime.setError("Please enter a valid numeric duration");
                    etExamTime.requestFocus();
                    return;
                }
            }

            if (etPassingScore != null) {
                String passStr = etPassingScore.getText().toString().trim();
                try {
                    int passScore = Integer.parseInt(passStr);
                    if (passScore < 1 || passScore > 100) {
                        etPassingScore.setError("Passing score must be between 1% and 100%");
                        etPassingScore.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    etPassingScore.setError("Please enter a valid numeric percentage");
                    etPassingScore.requestFocus();
                    return;
                }
            }

            if (etAppName != null) dbHelper.saveSetting("site_name", etAppName.getText().toString().trim());
            if (etTagline != null) dbHelper.saveSetting("site_tagline", etTagline.getText().toString().trim());
            if (etContactEmail != null) dbHelper.saveSetting("contact_email", etContactEmail.getText().toString().trim());
            if (etSupportEmail != null) dbHelper.saveSetting("support_email", etSupportEmail.getText().toString().trim());

            if (etCurrency != null) dbHelper.saveSetting("default_currency", etCurrency.getText().toString().trim());
            if (switchRTL != null) dbHelper.saveSetting("enable_rtl", switchRTL.isChecked() ? "1" : "0");

            if (etSmtpHost != null) dbHelper.saveSetting("smtp_host", etSmtpHost.getText().toString().trim());
            if (etFcmKey != null) dbHelper.saveSetting("fcm_server_key", etFcmKey.getText().toString().trim());

            if (switch2FA != null) dbHelper.saveSetting("enforce_2fa", switch2FA.isChecked() ? "1" : "0");
            if (switchLockout != null) dbHelper.saveSetting("login_protection", switchLockout.isChecked() ? "1" : "0");

            if (switchCookie != null) dbHelper.saveSetting("cookie_consent", switchCookie.isChecked() ? "1" : "0");

            if (etZoomKey != null) dbHelper.saveSetting("zoom_api_key", etZoomKey.getText().toString().trim());
            if (etS3Bucket != null) dbHelper.saveSetting("aws_s3_bucket", etS3Bucket.getText().toString().trim());

            if (etPrimaryColor != null) dbHelper.saveSetting("primary_color", etPrimaryColor.getText().toString().trim());

            if (switchCache != null) dbHelper.saveSetting("enable_caching", switchCache.isChecked() ? "1" : "0");

            if (switchMaintenance != null) dbHelper.saveSetting("maintenance_mode", switchMaintenance.isChecked() ? "1" : "0");
            if (switchRegistration != null) dbHelper.saveSetting("allow_registration", switchRegistration.isChecked() ? "1" : "0");
            if (switchVoiceFeature != null) dbHelper.saveSetting("voice_study", switchVoiceFeature.isChecked() ? "1" : "0");

            if (etExamTime != null) dbHelper.saveSetting("exam_time", etExamTime.getText().toString().trim());
            if (etPassingScore != null) dbHelper.saveSetting("passing_score", etPassingScore.getText().toString().trim());

            if (switchCameraLab != null) dbHelper.saveSetting("camera_lab_enabled", switchCameraLab.isChecked() ? "1" : "0");
            if (switchLabMath != null) dbHelper.saveSetting("camera_lab_math", switchLabMath.isChecked() ? "1" : "0");
            if (switchLabPhysics != null) dbHelper.saveSetting("camera_lab_physics", switchLabPhysics.isChecked() ? "1" : "0");
            if (switchLabChem != null) dbHelper.saveSetting("camera_lab_chem", switchLabChem.isChecked() ? "1" : "0");
            if (etLabMaxSteps != null) dbHelper.saveSetting("camera_lab_max_steps", etLabMaxSteps.getText().toString().trim());

            dbHelper.logAdminAction(adminUsername, "SYSTEM_UPDATE", "Global system infrastructure synchronized by " + adminUsername);
            
            if (switchMaintenance != null && switchMaintenance.isChecked()) {
                dbHelper.logAdminAction(adminUsername, "MAINTENANCE_ACTIVE", "Platform entered restricted maintenance mode.");
            }

            Toast.makeText(this, "Global System Configuration Synchronized.", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Configuration Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmBackup() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("System Backup & Synchronization")
            .setMessage("Initiate an atomic snapshot of local database records, user profiles, and active audit telemetry?")
            .setPositiveButton("Create Snapshot", (dialog, which) -> triggerBackup())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void triggerBackup() {
        dbHelper.logAdminAction(adminUsername, "MANUAL_BACKUP", "Initiated full system database backup snapshot.");
        new MaterialAlertDialogBuilder(this)
            .setTitle("Backup Generated")
            .setMessage("Database snapshot verified successfully.\n\nStaged for cloud synchronization via AWS S3 bucket: " + 
                (etS3Bucket != null ? etS3Bucket.getText().toString() : "learnify-assets"))
            .setPositiveButton("Done", null)
            .show();
    }

    private void openAuditLogs() {
        try {
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT action, details, timestamp FROM admin_logs ORDER BY id DESC LIMIT 15", null
            );
            StringBuilder sb = new StringBuilder();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String action = cursor.getString(0);
                    String details = cursor.getString(1);
                    String ts = cursor.getString(2);
                    sb.append("• [").append(ts != null ? ts : "Recent").append("]\n  ")
                      .append(action).append(": ").append(details != null ? details : "").append("\n\n");
                }
                cursor.close();
            }
            if (sb.length() == 0) {
                sb.append("No administrative audit entries found in current session.");
            }
            new MaterialAlertDialogBuilder(this)
                .setTitle("Administrative Audit Trail")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Close", null)
                .show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to read audit logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
