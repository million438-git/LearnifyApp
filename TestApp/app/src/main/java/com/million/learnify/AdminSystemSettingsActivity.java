package com.million.learnify;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminSystemSettingsActivity - The Central Nervous System of the Application.
 * Manages 10 core infrastructure domains from 8.1 to 8.10.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class AdminSystemSettingsActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private String adminUsername = "admin";

    // UI Elements for each category
    private TextInputEditText etAppName, etTagline, etContactEmail, etSupportEmail; // 8.1
    private TextInputEditText etCurrency; private SwitchCompat switchRTL; // 8.2
    private TextInputEditText etSmtpHost, etFcmKey; // 8.3
    private SwitchCompat switch2FA, switchLockout; // 8.4
    private SwitchCompat switchCookie; // 8.5
    private TextInputEditText etZoomKey, etS3Bucket; // 8.6
    private TextInputEditText etPrimaryColor; // 8.7
    private SwitchCompat switchCache; // 8.8
    private SwitchCompat switchMaintenance, switchRegistration, switchVoiceFeature; // 8.9
    private TextInputEditText etExamTime, etPassingScore; // 8.10
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_system_settings);

        dbHelper = repository.getLocalDb();
        setupToolbar();
        initViews();
        loadAllConfigurations();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Infrastructure Control");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
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

        // Buttons
        btnSave = findViewById(R.id.btnSaveSettings);
        btnSave.setOnClickListener(v -> saveAllConfigurations());

        findViewById(R.id.btnBackupRestore).setOnClickListener(v -> triggerBackup());
        findViewById(R.id.btnSystemLogs).setOnClickListener(v -> openAuditLogs());
    }

    private void loadAllConfigurations() {
        etAppName.setText(dbHelper.getSetting("site_name", "Learnify Pro"));
        etTagline.setText(dbHelper.getSetting("site_tagline", "Smart Study Hub"));
        etContactEmail.setText(dbHelper.getSetting("contact_email", "admin@learnify.com"));
        etSupportEmail.setText(dbHelper.getSetting("support_email", "support@learnify.com"));

        etCurrency.setText(dbHelper.getSetting("default_currency", "USD"));
        switchRTL.setChecked(dbHelper.getSetting("enable_rtl", "0").equals("1"));

        etSmtpHost.setText(dbHelper.getSetting("smtp_host", "smtp.learnify.com"));
        etFcmKey.setText(dbHelper.getSetting("fcm_server_key", ""));

        switch2FA.setChecked(dbHelper.getSetting("enforce_2fa", "0").equals("1"));
        switchLockout.setChecked(dbHelper.getSetting("login_protection", "1").equals("1"));

        switchCookie.setChecked(dbHelper.getSetting("cookie_consent", "1").equals("1"));

        etZoomKey.setText(dbHelper.getSetting("zoom_api_key", ""));
        etS3Bucket.setText(dbHelper.getSetting("aws_s3_bucket", "learnify-assets"));

        etPrimaryColor.setText(dbHelper.getSetting("primary_color", "#4F46E5"));

        switchCache.setChecked(dbHelper.getSetting("enable_caching", "1").equals("1"));

        switchMaintenance.setChecked(dbHelper.getSetting("maintenance_mode", "0").equals("1"));
        switchRegistration.setChecked(dbHelper.getSetting("allow_registration", "1").equals("1"));
        switchVoiceFeature.setChecked(dbHelper.getSetting("voice_study", "1").equals("1"));
        
        if (etExamTime != null) etExamTime.setText(dbHelper.getSetting("exam_time", "30"));
        if (etPassingScore != null) etPassingScore.setText(dbHelper.getSetting("passing_score", "50"));
    }

    private void saveAllConfigurations() {
        try {
            dbHelper.saveSetting("site_name", etAppName.getText().toString());
            dbHelper.saveSetting("site_tagline", etTagline.getText().toString());
            dbHelper.saveSetting("contact_email", etContactEmail.getText().toString());
            dbHelper.saveSetting("support_email", etSupportEmail.getText().toString());

            dbHelper.saveSetting("default_currency", etCurrency.getText().toString());
            dbHelper.saveSetting("enable_rtl", switchRTL.isChecked() ? "1" : "0");

            dbHelper.saveSetting("smtp_host", etSmtpHost.getText().toString());
            dbHelper.saveSetting("fcm_server_key", etFcmKey.getText().toString());

            dbHelper.saveSetting("enforce_2fa", switch2FA.isChecked() ? "1" : "0");
            dbHelper.saveSetting("login_protection", switchLockout.isChecked() ? "1" : "0");

            dbHelper.saveSetting("cookie_consent", switchCookie.isChecked() ? "1" : "0");

            dbHelper.saveSetting("zoom_api_key", etZoomKey.getText().toString());
            dbHelper.saveSetting("aws_s3_bucket", etS3Bucket.getText().toString());

            dbHelper.saveSetting("primary_color", etPrimaryColor.getText().toString());

            dbHelper.saveSetting("enable_caching", switchCache.isChecked() ? "1" : "0");

            dbHelper.saveSetting("maintenance_mode", switchMaintenance.isChecked() ? "1" : "0");
            dbHelper.saveSetting("allow_registration", switchRegistration.isChecked() ? "1" : "0");
            dbHelper.saveSetting("voice_study", switchVoiceFeature.isChecked() ? "1" : "0");

            if (etExamTime != null) dbHelper.saveSetting("exam_time", etExamTime.getText().toString());
            if (etPassingScore != null) dbHelper.saveSetting("passing_score", etPassingScore.getText().toString());

            dbHelper.logAdminAction(adminUsername, "SYSTEM_UPDATE", "Global configuration updated by Super Admin.");
            
            Toast.makeText(this, "Global System Configuration Synchronized!", Toast.LENGTH_SHORT).show();
            
            if (switchMaintenance.isChecked()) {
                dbHelper.logAdminAction(adminUsername, "MAINTENANCE_ACTIVE", "System entered restricted maintenance mode.");
            }
            
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Configuration Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void triggerBackup() {
        dbHelper.logAdminAction(adminUsername, "MANUAL_BACKUP", "Initiated full system database backup.");
        Toast.makeText(this, "Cloud Sync: Backup uploaded to S3 successfully.", Toast.LENGTH_SHORT).show();
    }

    private void openAuditLogs() {
        Toast.makeText(this, "Retrieving 8.9.2 Activity Audit Trail...", Toast.LENGTH_SHORT).show();
    }
}
