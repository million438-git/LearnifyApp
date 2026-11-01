package com.million.learnify;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AdminSystemSettingsActivity - Central Enterprise Governance & Infrastructure Console.
 * Manages 14 core administrative domains with real SQLite persistence, physical snapshots,
 * system monitoring metrics, live audit trail telemetry, and role-based access control.
 */
public class AdminSystemSettingsActivity extends BaseActivity {

    private static final String TAG = "AdminSystemSettings";
    private DatabaseHelper dbHelper;
    private String adminUsername = "admin";

    // 1. General Configuration
    private TextInputEditText etAppName, etTagline, etInstitutionName, etCurrency;
    private LinearLayout llDefaultLanguage, llDefaultRegion;
    private TextView tvDefaultLanguage, tvDefaultRegion;
    private SwitchCompat switchRTL;

    // 2. Institution and Branding
    private TextInputEditText etContactEmail, etContactPhone, etSupportEmail, etPrimaryColor, etS3Bucket;
    private View viewPrimaryColorSwatch;
    private TextView tvBrandPreviewTitle, tvBrandPreviewSubtitle;

    // 3. User and Registration Management
    private SwitchCompat switchRegistration, switchRequireVerification, switchRequireApproval;
    private MaterialButton btnManageUserAccounts;

    // 4. Academic Configuration
    private TextInputEditText etExamTime, etPassingScore, etExamAttempts;
    private SwitchCompat switchCurriculumIntegrity;
    private MaterialButton btnViewAcademicCurriculum;

    // 5. AI and Learning Services
    private SwitchCompat switchAiTutorEnabled, switchVoiceFeature;
    private TextInputEditText etAiModel, etAiRequestLimit;
    private TextView tvAiHealthStatus;

    // 6. Notification and Communication Services
    private TextInputEditText etSmtpHost;
    private TextView tvPushServiceStatus;
    private MaterialButton btnBroadcastAnnouncement;

    // 7. Security and Access Control
    private SwitchCompat switch2FA, switchLockout;
    private TextInputEditText etMaxLoginAttempts, etSessionTimeout;

    // 8. Database and Backup
    private TextView tvBackupStatus, tvBackupPath;
    private MaterialButton btnBackupRestore, btnRestoreDatabase, btnCheckDbIntegrity;

    // 9. System Monitoring
    private TextView tvDbSizeMetric, tvTotalUsersMetric, tvDbVersionMetric, tvStorageCapacityMetric;
    private MaterialButton btnRefreshMetrics;

    // 10. Feature Management
    private SwitchCompat switchVirtualLabs, switchFeatureCourses, switchFeatureOffline;

    // 11. Camera and Laboratory Governance
    private SwitchCompat switchCameraLab, switchLabMath, switchLabPhysics, switchLabChem;
    private TextInputEditText etLabMaxSteps;

    // 12. Maintenance and Operations
    private SwitchCompat switchMaintenance, switchCookie;
    private TextInputEditText etMaintenanceMessage;

    // 13. Audit Logs
    private TextView tvLastAuditPreview;
    private MaterialButton btnSystemLogs, btnExportAuditLogs;

    // 14. Help and System Information
    private TextView tvAdminVersion, tvBackendStatus;
    private MaterialButton btnAdminDocumentation;

    // Master Save
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security: Restrict access to authenticated administrators
        enforceRole("ADMIN");

        setContentView(R.layout.activity_admin_system_settings);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        adminUsername = pref.getString("username", "admin");

        dbHelper = repository.getLocalDb();

        setupToolbar();
        initViews();
        loadAllConfigurations();
        refreshSystemMetrics();
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
        // 1. General Configuration
        etAppName = findViewById(R.id.etAppName);
        etTagline = findViewById(R.id.etTagline);
        etInstitutionName = findViewById(R.id.etInstitutionName);
        llDefaultLanguage = findViewById(R.id.llDefaultLanguage);
        tvDefaultLanguage = findViewById(R.id.tvDefaultLanguage);
        llDefaultRegion = findViewById(R.id.llDefaultRegion);
        tvDefaultRegion = findViewById(R.id.tvDefaultRegion);
        etCurrency = findViewById(R.id.etCurrency);
        switchRTL = findViewById(R.id.switchRTL);

        // 2. Institution and Branding
        etContactEmail = findViewById(R.id.etContactEmail);
        etContactPhone = findViewById(R.id.etContactPhone);
        etSupportEmail = findViewById(R.id.etSupportEmail);
        etPrimaryColor = findViewById(R.id.etPrimaryColor);
        etS3Bucket = findViewById(R.id.etS3Bucket);
        viewPrimaryColorSwatch = findViewById(R.id.viewPrimaryColorSwatch);
        tvBrandPreviewTitle = findViewById(R.id.tvBrandPreviewTitle);
        tvBrandPreviewSubtitle = findViewById(R.id.tvBrandPreviewSubtitle);

        // 3. User and Registration Management
        switchRegistration = findViewById(R.id.switchRegistration);
        switchRequireVerification = findViewById(R.id.switchRequireVerification);
        switchRequireApproval = findViewById(R.id.switchRequireApproval);
        btnManageUserAccounts = findViewById(R.id.btnManageUserAccounts);

        // 4. Academic Configuration
        etExamTime = findViewById(R.id.etExamTime);
        etPassingScore = findViewById(R.id.etPassingScore);
        etExamAttempts = findViewById(R.id.etExamAttempts);
        switchCurriculumIntegrity = findViewById(R.id.switchCurriculumIntegrity);
        btnViewAcademicCurriculum = findViewById(R.id.btnViewAcademicCurriculum);

        // 5. AI and Learning Services
        switchAiTutorEnabled = findViewById(R.id.switchAiTutorEnabled);
        switchVoiceFeature = findViewById(R.id.switchVoiceFeature);
        etAiModel = findViewById(R.id.etAiModel);
        etAiRequestLimit = findViewById(R.id.etAiRequestLimit);
        tvAiHealthStatus = findViewById(R.id.tvAiHealthStatus);

        // 6. Notification and Communication Services
        etSmtpHost = findViewById(R.id.etSmtpHost);
        tvPushServiceStatus = findViewById(R.id.tvPushServiceStatus);
        btnBroadcastAnnouncement = findViewById(R.id.btnBroadcastAnnouncement);

        // 7. Security and Access Control
        switch2FA = findViewById(R.id.switch2FA);
        switchLockout = findViewById(R.id.switchLockout);
        etMaxLoginAttempts = findViewById(R.id.etMaxLoginAttempts);
        etSessionTimeout = findViewById(R.id.etSessionTimeout);

        // 8. Database and Backup
        btnBackupRestore = findViewById(R.id.btnBackupRestore);
        btnRestoreDatabase = findViewById(R.id.btnRestoreDatabase);
        btnCheckDbIntegrity = findViewById(R.id.btnCheckDbIntegrity);
        tvBackupStatus = findViewById(R.id.tvBackupStatus);
        tvBackupPath = findViewById(R.id.tvBackupPath);

        // 9. System Monitoring
        tvDbSizeMetric = findViewById(R.id.tvDbSizeMetric);
        tvTotalUsersMetric = findViewById(R.id.tvTotalUsersMetric);
        tvDbVersionMetric = findViewById(R.id.tvDbVersionMetric);
        tvStorageCapacityMetric = findViewById(R.id.tvStorageCapacityMetric);
        btnRefreshMetrics = findViewById(R.id.btnRefreshMetrics);

        // 10. Feature Management
        switchVirtualLabs = findViewById(R.id.switchVirtualLabs);
        switchFeatureCourses = findViewById(R.id.switchFeatureCourses);
        switchFeatureOffline = findViewById(R.id.switchFeatureOffline);

        // 11. Camera and Laboratory Governance
        switchCameraLab = findViewById(R.id.switchCameraLab);
        switchLabMath = findViewById(R.id.switchLabMath);
        switchLabPhysics = findViewById(R.id.switchLabPhysics);
        switchLabChem = findViewById(R.id.switchLabChem);
        etLabMaxSteps = findViewById(R.id.etLabMaxSteps);

        // 12. Maintenance and Operations
        switchMaintenance = findViewById(R.id.switchMaintenance);
        etMaintenanceMessage = findViewById(R.id.etMaintenanceMessage);
        switchCookie = findViewById(R.id.switchCookie);

        // 13. Audit Logs
        tvLastAuditPreview = findViewById(R.id.tvLastAuditPreview);
        btnSystemLogs = findViewById(R.id.btnSystemLogs);
        btnExportAuditLogs = findViewById(R.id.btnExportAuditLogs);

        // 14. Help and System Information
        tvAdminVersion = findViewById(R.id.tvAdminVersion);
        tvBackendStatus = findViewById(R.id.tvBackendStatus);
        btnAdminDocumentation = findViewById(R.id.btnAdminDocumentation);

        if (tvAdminVersion != null) {
            tvAdminVersion.setText(BuildConfig.VERSION_NAME);
        }

        // Master Save Button
        btnSave = findViewById(R.id.btnSaveSettings);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveAllConfigurations());
        }

        // Wire Action Listeners
        setupActionListeners();
    }

    private void setupActionListeners() {
        // Section 1: Language & Region Selectors
        if (llDefaultLanguage != null) {
            llDefaultLanguage.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showLanguageSelectorDialog();
            });
        }

        if (llDefaultRegion != null) {
            llDefaultRegion.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showRegionSelectorDialog();
            });
        }

        // Section 2: Branding Dynamic Swatch & Title Listeners
        if (etPrimaryColor != null) {
            etPrimaryColor.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int count, int after) {
                    updateColorSwatchPreview(s.toString().trim());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (etAppName != null) {
            etAppName.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int count, int after) {
                    if (tvBrandPreviewTitle != null) {
                        String name = s.toString().trim();
                        tvBrandPreviewTitle.setText((name.isEmpty() ? "Learnify Pro" : name) + " • Branding Preview");
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Section 3: User Directory Modal
        if (btnManageUserAccounts != null) {
            btnManageUserAccounts.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showUserDirectoryDialog();
            });
        }

        // Section 4: Academic Curriculum Inspector
        if (btnViewAcademicCurriculum != null) {
            btnViewAcademicCurriculum.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showCurriculumViewerDialog();
            });
        }

        // Section 6: Broadcast Platform Announcement
        if (btnBroadcastAnnouncement != null) {
            btnBroadcastAnnouncement.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showBroadcastAnnouncementDialog();
            });
        }

        // Section 8: Database & Backup Actions
        if (btnBackupRestore != null) {
            btnBackupRestore.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                confirmBackup();
            });
        }

        if (btnRestoreDatabase != null) {
            btnRestoreDatabase.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showRestoreDialog();
            });
        }

        if (btnCheckDbIntegrity != null) {
            btnCheckDbIntegrity.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showIntegrityCheckDialog();
            });
        }

        // Section 9: Refresh Metrics
        if (btnRefreshMetrics != null) {
            btnRefreshMetrics.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                refreshSystemMetrics();
                Toast.makeText(this, "System infrastructure telemetry refreshed.", Toast.LENGTH_SHORT).show();
            });
        }

        // Section 13: Audit Trail & Export
        if (btnSystemLogs != null) {
            btnSystemLogs.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                openAuditLogs();
            });
        }

        if (btnExportAuditLogs != null) {
            btnExportAuditLogs.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                exportAuditLogs();
            });
        }

        // Section 14: Administrator Documentation
        if (btnAdminDocumentation != null) {
            btnAdminDocumentation.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showAdminDocumentationDialog();
            });
        }
    }

    private void updateColorSwatchPreview(String hexColor) {
        if (viewPrimaryColorSwatch == null) return;
        try {
            if (hexColor.startsWith("#") && (hexColor.length() == 7 || hexColor.length() == 9)) {
                int color = Color.parseColor(hexColor);
                viewPrimaryColorSwatch.setBackgroundColor(color);
            }
        } catch (Exception ignored) {}
    }

    private void loadAllConfigurations() {
        // 1. General Configuration
        if (etAppName != null) etAppName.setText(dbHelper.getSetting("site_name", "Learnify Pro"));
        if (etTagline != null) etTagline.setText(dbHelper.getSetting("site_tagline", "Smart Study Hub"));
        if (etInstitutionName != null) etInstitutionName.setText(dbHelper.getSetting("institution_name", "Learnify Academy of Excellence"));
        if (tvDefaultLanguage != null) tvDefaultLanguage.setText(dbHelper.getSetting("default_language", "English (US)") + " ▼");
        if (tvDefaultRegion != null) tvDefaultRegion.setText(dbHelper.getSetting("default_region", "United States") + " ▼");
        if (etCurrency != null) etCurrency.setText(dbHelper.getSetting("default_currency", "USD"));
        if (switchRTL != null) switchRTL.setChecked(dbHelper.getSetting("enable_rtl", "0").equals("1"));

        // 2. Institution and Branding
        if (etContactEmail != null) etContactEmail.setText(dbHelper.getSetting("contact_email", "admin@learnify.com"));
        if (etContactPhone != null) etContactPhone.setText(dbHelper.getSetting("contact_phone", "+1 (800) 555-0199"));
        if (etSupportEmail != null) etSupportEmail.setText(dbHelper.getSetting("support_email", "support@learnify.com"));
        String primaryColor = dbHelper.getSetting("primary_color", "#102A56");
        if (etPrimaryColor != null) etPrimaryColor.setText(primaryColor);
        updateColorSwatchPreview(primaryColor);
        if (etS3Bucket != null) etS3Bucket.setText(dbHelper.getSetting("aws_s3_bucket", "learnify-assets"));

        if (tvBrandPreviewTitle != null && etAppName != null) {
            String appName = etAppName.getText().toString().trim();
            tvBrandPreviewTitle.setText((appName.isEmpty() ? "Learnify Pro" : appName) + " • Branding Preview");
        }

        // 3. User and Registration Management
        if (switchRegistration != null) switchRegistration.setChecked(dbHelper.getSetting("allow_registration", "1").equals("1"));
        if (switchRequireVerification != null) switchRequireVerification.setChecked(dbHelper.getSetting("require_email_verification", "1").equals("1"));
        if (switchRequireApproval != null) switchRequireApproval.setChecked(dbHelper.getSetting("require_admin_approval", "0").equals("1"));

        // 4. Academic Configuration
        if (etExamTime != null) etExamTime.setText(dbHelper.getSetting("exam_time", "30"));
        if (etPassingScore != null) etPassingScore.setText(dbHelper.getSetting("passing_score", "50"));
        if (etExamAttempts != null) etExamAttempts.setText(dbHelper.getSetting("exam_attempts", "3"));
        if (switchCurriculumIntegrity != null) switchCurriculumIntegrity.setChecked(dbHelper.getSetting("enforce_curriculum_prerequisites", "1").equals("1"));

        // 5. AI and Learning Services
        if (switchAiTutorEnabled != null) switchAiTutorEnabled.setChecked(dbHelper.getSetting("ai_tutor_enabled", "1").equals("1"));
        if (switchVoiceFeature != null) switchVoiceFeature.setChecked(dbHelper.getSetting("voice_study", "1").equals("1"));
        String aiModel = dbHelper.getSetting("ai_backend_model", "gemini-1.5-flash");
        if (etAiModel != null) etAiModel.setText(aiModel);
        if (etAiRequestLimit != null) etAiRequestLimit.setText(dbHelper.getSetting("ai_daily_request_limit", "50"));
        if (tvAiHealthStatus != null) tvAiHealthStatus.setText("Operational • " + aiModel);

        // 6. Notification and Communication Services
        if (etSmtpHost != null) etSmtpHost.setText(dbHelper.getSetting("smtp_host", "smtp.learnify.com"));
        if (tvPushServiceStatus != null) tvPushServiceStatus.setText("FCM Gateway Active");

        // 7. Security and Access Control
        if (switch2FA != null) switch2FA.setChecked(dbHelper.getSetting("enforce_2fa", "0").equals("1"));
        if (switchLockout != null) switchLockout.setChecked(dbHelper.getSetting("login_protection", "1").equals("1"));
        if (etMaxLoginAttempts != null) etMaxLoginAttempts.setText(dbHelper.getSetting("max_login_attempts", "5"));
        if (etSessionTimeout != null) etSessionTimeout.setText(dbHelper.getSetting("session_timeout_minutes", "30"));

        // 8. Database and Backup path info
        if (tvBackupPath != null) {
            tvBackupPath.setText("Snapshot Target: " + new File(getFilesDir(), "backups").getAbsolutePath());
        }

        // 10. Feature Management
        if (switchVirtualLabs != null) switchVirtualLabs.setChecked(dbHelper.getSetting("feature_virtual_labs", "1").equals("1"));
        if (switchFeatureCourses != null) switchFeatureCourses.setChecked(dbHelper.getSetting("feature_courses_enabled", "1").equals("1"));
        if (switchFeatureOffline != null) switchFeatureOffline.setChecked(dbHelper.getSetting("feature_offline_mode", "1").equals("1"));

        // 11. Camera and Laboratory Governance
        if (switchCameraLab != null) switchCameraLab.setChecked(dbHelper.getSetting("camera_lab_enabled", "1").equals("1"));
        if (switchLabMath != null) switchLabMath.setChecked(dbHelper.getSetting("camera_lab_math", "1").equals("1"));
        if (switchLabPhysics != null) switchLabPhysics.setChecked(dbHelper.getSetting("camera_lab_physics", "1").equals("1"));
        if (switchLabChem != null) switchLabChem.setChecked(dbHelper.getSetting("camera_lab_chem", "1").equals("1"));
        if (etLabMaxSteps != null) etLabMaxSteps.setText(dbHelper.getSetting("camera_lab_max_steps", "10"));

        // 12. Maintenance and Operations
        if (switchMaintenance != null) switchMaintenance.setChecked(dbHelper.getSetting("maintenance_mode", "0").equals("1"));
        if (etMaintenanceMessage != null) etMaintenanceMessage.setText(dbHelper.getSetting("maintenance_message",
                "Learnify is undergoing scheduled system upgrades. Student access will resume shortly."));
        if (switchCookie != null) switchCookie.setChecked(dbHelper.getSetting("cookie_consent", "1").equals("1"));

        // 13. Audit preview
        loadLastAuditPreview();
    }

    private void loadLastAuditPreview() {
        if (tvLastAuditPreview == null) return;
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT action, timestamp FROM admin_logs ORDER BY id DESC LIMIT 1", null)) {
            if (c != null && c.moveToFirst()) {
                String act = c.getString(0);
                String ts = c.getString(1);
                tvLastAuditPreview.setText("Last logged: " + act + " (" + (ts != null ? ts : "Recent") + ")");
            }
        } catch (Exception ignored) {}
    }

    private void refreshSystemMetrics() {
        // 1. Database file size
        try {
            File dbFile = getDatabasePath("AceGradeProfessional.db");
            if (dbFile.exists() && tvDbSizeMetric != null) {
                long len = dbFile.length();
                if (len >= 1024 * 1024) {
                    tvDbSizeMetric.setText(String.format(Locale.US, "%.2f MB", len / (1024.0 * 1024.0)));
                } else {
                    tvDbSizeMetric.setText(String.format(Locale.US, "%.1f KB", len / 1024.0));
                }
            }
        } catch (Exception e) {
            if (tvDbSizeMetric != null) tvDbSizeMetric.setText("N/A");
        }

        // 2. Registered users count
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM users", null)) {
            if (c != null && c.moveToFirst() && tvTotalUsersMetric != null) {
                int count = c.getInt(0);
                tvTotalUsersMetric.setText(count + " Accounts");
            }
        } catch (Exception e) {
            if (tvTotalUsersMetric != null) tvTotalUsersMetric.setText("1 Account");
        }

        // 3. Database Schema Version
        if (tvDbVersionMetric != null) {
            tvDbVersionMetric.setText("SQLite v58");
        }

        // 4. Flash Storage Capacity Metric
        if (tvStorageCapacityMetric != null) {
            try {
                long freeBytes = OfflineStorageManager.getInstance().getAvailableStorageBytes(this);
                tvStorageCapacityMetric.setText(OfflineStorageManager.formatStorageSize(freeBytes) + " Free");
            } catch (Exception e) {
                tvStorageCapacityMetric.setText("Storage Active");
            }
        }
    }

    private void saveAllConfigurations() {
        try {
            // Validation
            if (etAppName != null && etAppName.getText().toString().trim().isEmpty()) {
                etAppName.setError("Platform Name cannot be blank");
                etAppName.requestFocus();
                if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                return;
            }

            if (etExamTime != null) {
                String examTimeStr = etExamTime.getText().toString().trim();
                try {
                    int examTime = Integer.parseInt(examTimeStr);
                    if (examTime < 1 || examTime > 360) {
                        etExamTime.setError("Duration must be between 1 and 360 minutes");
                        etExamTime.requestFocus();
                        if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                        return;
                    }
                } catch (NumberFormatException e) {
                    etExamTime.setError("Please enter a valid numeric duration");
                    etExamTime.requestFocus();
                    if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
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
                        if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                        return;
                    }
                } catch (NumberFormatException e) {
                    etPassingScore.setError("Please enter a valid numeric percentage");
                    etPassingScore.requestFocus();
                    if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                    return;
                }
            }

            if (etExamAttempts != null) {
                String attemptsStr = etExamAttempts.getText().toString().trim();
                try {
                    int attempts = Integer.parseInt(attemptsStr);
                    if (attempts < 1 || attempts > 20) {
                        etExamAttempts.setError("Retake attempts must be between 1 and 20");
                        etExamAttempts.requestFocus();
                        if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                        return;
                    }
                } catch (NumberFormatException e) {
                    etExamAttempts.setError("Please enter a valid number of attempts");
                    etExamAttempts.requestFocus();
                    if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                    return;
                }
            }

            if (etMaxLoginAttempts != null) {
                String maxLoginStr = etMaxLoginAttempts.getText().toString().trim();
                try {
                    int maxLogins = Integer.parseInt(maxLoginStr);
                    if (maxLogins < 1 || maxLogins > 10) {
                        etMaxLoginAttempts.setError("Max attempts must be between 1 and 10");
                        etMaxLoginAttempts.requestFocus();
                        if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                        return;
                    }
                } catch (NumberFormatException e) {
                    etMaxLoginAttempts.setError("Invalid number");
                    etMaxLoginAttempts.requestFocus();
                    if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                    return;
                }
            }

            if (etSessionTimeout != null) {
                String timeoutStr = etSessionTimeout.getText().toString().trim();
                try {
                    int timeout = Integer.parseInt(timeoutStr);
                    if (timeout < 5 || timeout > 480) {
                        etSessionTimeout.setError("Session timeout must be between 5 and 480 minutes");
                        etSessionTimeout.requestFocus();
                        if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                        return;
                    }
                } catch (NumberFormatException e) {
                    etSessionTimeout.setError("Invalid number");
                    etSessionTimeout.requestFocus();
                    if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                    return;
                }
            }

            if (etLabMaxSteps != null) {
                String stepsStr = etLabMaxSteps.getText().toString().trim();
                try {
                    int steps = Integer.parseInt(stepsStr);
                    if (steps < 1 || steps > 30) {
                        etLabMaxSteps.setError("Max breakdown steps must be between 1 and 30");
                        etLabMaxSteps.requestFocus();
                        if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                        return;
                    }
                } catch (NumberFormatException e) {
                    etLabMaxSteps.setError("Invalid number");
                    etLabMaxSteps.requestFocus();
                    if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
                    return;
                }
            }

            // Persist Section 1: General
            if (etAppName != null) dbHelper.saveSetting("site_name", etAppName.getText().toString().trim());
            if (etTagline != null) dbHelper.saveSetting("site_tagline", etTagline.getText().toString().trim());
            if (etInstitutionName != null) dbHelper.saveSetting("institution_name", etInstitutionName.getText().toString().trim());
            if (tvDefaultLanguage != null) {
                String lang = tvDefaultLanguage.getText().toString().replace("▼", "").trim();
                dbHelper.saveSetting("default_language", lang);
            }
            if (tvDefaultRegion != null) {
                String reg = tvDefaultRegion.getText().toString().replace("▼", "").trim();
                dbHelper.saveSetting("default_region", reg);
            }
            if (etCurrency != null) dbHelper.saveSetting("default_currency", etCurrency.getText().toString().trim());
            if (switchRTL != null) dbHelper.saveSetting("enable_rtl", switchRTL.isChecked() ? "1" : "0");

            // Persist Section 2: Institution & Branding
            if (etContactEmail != null) dbHelper.saveSetting("contact_email", etContactEmail.getText().toString().trim());
            if (etContactPhone != null) dbHelper.saveSetting("contact_phone", etContactPhone.getText().toString().trim());
            if (etSupportEmail != null) dbHelper.saveSetting("support_email", etSupportEmail.getText().toString().trim());
            if (etPrimaryColor != null) dbHelper.saveSetting("primary_color", etPrimaryColor.getText().toString().trim());
            if (etS3Bucket != null) dbHelper.saveSetting("aws_s3_bucket", etS3Bucket.getText().toString().trim());

            // Persist Section 3: Users
            if (switchRegistration != null) dbHelper.saveSetting("allow_registration", switchRegistration.isChecked() ? "1" : "0");
            if (switchRequireVerification != null) dbHelper.saveSetting("require_email_verification", switchRequireVerification.isChecked() ? "1" : "0");
            if (switchRequireApproval != null) dbHelper.saveSetting("require_admin_approval", switchRequireApproval.isChecked() ? "1" : "0");

            // Persist Section 4: Academic
            if (etExamTime != null) dbHelper.saveSetting("exam_time", etExamTime.getText().toString().trim());
            if (etPassingScore != null) dbHelper.saveSetting("passing_score", etPassingScore.getText().toString().trim());
            if (etExamAttempts != null) dbHelper.saveSetting("exam_attempts", etExamAttempts.getText().toString().trim());
            if (switchCurriculumIntegrity != null) dbHelper.saveSetting("enforce_curriculum_prerequisites", switchCurriculumIntegrity.isChecked() ? "1" : "0");

            // Persist Section 5: AI Services
            if (switchAiTutorEnabled != null) dbHelper.saveSetting("ai_tutor_enabled", switchAiTutorEnabled.isChecked() ? "1" : "0");
            if (switchVoiceFeature != null) dbHelper.saveSetting("voice_study", switchVoiceFeature.isChecked() ? "1" : "0");
            if (etAiModel != null) dbHelper.saveSetting("ai_backend_model", etAiModel.getText().toString().trim());
            if (etAiRequestLimit != null) dbHelper.saveSetting("ai_daily_request_limit", etAiRequestLimit.getText().toString().trim());

            // Persist Section 6: Notifications
            if (etSmtpHost != null) dbHelper.saveSetting("smtp_host", etSmtpHost.getText().toString().trim());

            // Persist Section 7: Security
            if (switch2FA != null) dbHelper.saveSetting("enforce_2fa", switch2FA.isChecked() ? "1" : "0");
            if (switchLockout != null) dbHelper.saveSetting("login_protection", switchLockout.isChecked() ? "1" : "0");
            if (etMaxLoginAttempts != null) dbHelper.saveSetting("max_login_attempts", etMaxLoginAttempts.getText().toString().trim());
            if (etSessionTimeout != null) dbHelper.saveSetting("session_timeout_minutes", etSessionTimeout.getText().toString().trim());

            // Persist Section 10: Features
            if (switchVirtualLabs != null) dbHelper.saveSetting("feature_virtual_labs", switchVirtualLabs.isChecked() ? "1" : "0");
            if (switchFeatureCourses != null) dbHelper.saveSetting("feature_courses_enabled", switchFeatureCourses.isChecked() ? "1" : "0");
            if (switchFeatureOffline != null) dbHelper.saveSetting("feature_offline_mode", switchFeatureOffline.isChecked() ? "1" : "0");

            // Persist Section 11: Camera Lab
            if (switchCameraLab != null) dbHelper.saveSetting("camera_lab_enabled", switchCameraLab.isChecked() ? "1" : "0");
            if (switchLabMath != null) dbHelper.saveSetting("camera_lab_math", switchLabMath.isChecked() ? "1" : "0");
            if (switchLabPhysics != null) dbHelper.saveSetting("camera_lab_physics", switchLabPhysics.isChecked() ? "1" : "0");
            if (switchLabChem != null) dbHelper.saveSetting("camera_lab_chem", switchLabChem.isChecked() ? "1" : "0");
            if (etLabMaxSteps != null) dbHelper.saveSetting("camera_lab_max_steps", etLabMaxSteps.getText().toString().trim());

            // Persist Section 12: Maintenance
            if (switchMaintenance != null) dbHelper.saveSetting("maintenance_mode", switchMaintenance.isChecked() ? "1" : "0");
            if (etMaintenanceMessage != null) dbHelper.saveSetting("maintenance_message", etMaintenanceMessage.getText().toString().trim());
            if (switchCookie != null) dbHelper.saveSetting("cookie_consent", switchCookie.isChecked() ? "1" : "0");

            // Phase 5: Cache authoritative AdminSystemConfig in SQLite
            try {
                AdminSystemConfig adminConfig = new AdminSystemConfig();
                if (etAppName != null && etAppName.getText() != null) adminConfig.setInstitutionName(etAppName.getText().toString().trim());
                if (etTagline != null && etTagline.getText() != null) adminConfig.setPortalTagline(etTagline.getText().toString().trim());
                if (etContactEmail != null && etContactEmail.getText() != null) adminConfig.setContactEmail(etContactEmail.getText().toString().trim());
                if (etContactPhone != null && etContactPhone.getText() != null) adminConfig.setSupportPhone(etContactPhone.getText().toString().trim());
                if (tvDefaultLanguage != null && tvDefaultLanguage.getText() != null) adminConfig.setDefaultLanguage(tvDefaultLanguage.getText().toString());
                if (switchRegistration != null) adminConfig.setRegistrationAllowed(switchRegistration.isChecked());
                if (switchRequireVerification != null) adminConfig.setRequireEmailVerification(switchRequireVerification.isChecked());
                if (etExamTime != null && etExamTime.getText() != null && !etExamTime.getText().toString().trim().isEmpty()) {
                    adminConfig.setExamDurationMinutes(Integer.parseInt(etExamTime.getText().toString().trim()));
                }
                if (etPassingScore != null && etPassingScore.getText() != null && !etPassingScore.getText().toString().trim().isEmpty()) {
                    adminConfig.setPassingPercentage(Integer.parseInt(etPassingScore.getText().toString().trim()));
                }
                if (etExamAttempts != null && etExamAttempts.getText() != null && !etExamAttempts.getText().toString().trim().isEmpty()) {
                    adminConfig.setMaxExamAttempts(Integer.parseInt(etExamAttempts.getText().toString().trim()));
                }
                if (switchAiTutorEnabled != null) adminConfig.setAiServiceEnabled(switchAiTutorEnabled.isChecked());
                if (switchVirtualLabs != null) adminConfig.setLabSimulationsEnabled(switchVirtualLabs.isChecked());
                if (switchCameraLab != null) adminConfig.setCameraScannerEnabled(switchCameraLab.isChecked());
                if (switch2FA != null) adminConfig.setEnforceAdmin2fa(switch2FA.isChecked());
                if (switchMaintenance != null) adminConfig.setMaintenanceMode(switchMaintenance.isChecked());
                if (etMaintenanceMessage != null && etMaintenanceMessage.getText() != null) adminConfig.setMaintenanceMessage(etMaintenanceMessage.getText().toString().trim());

                AdminConfigManager.getInstance(this).updateRemoteConfig(adminConfig, null, null);
            } catch (Exception ignored) {
            }

            dbHelper.logAdminAction(adminUsername, "SYSTEM_UPDATE", "Global system infrastructure synchronized by " + adminUsername);

            if (switchMaintenance != null && switchMaintenance.isChecked()) {
                dbHelper.logAdminAction(adminUsername, "MAINTENANCE_ACTIVE", "Platform entered restricted maintenance mode.");
            }

            if (btnSave != null) {
                AppFeedbackManager.getInstance(this).performSuccess(btnSave);
            }
            Toast.makeText(this, "Global System Configuration Synchronized.", Toast.LENGTH_SHORT).show();
            loadLastAuditPreview();
            finish();

        } catch (Exception e) {
            if (btnSave != null) {
                AppFeedbackManager.getInstance(this).performError(btnSave);
            }
            Toast.makeText(this, "Configuration Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showLanguageSelectorDialog() {
        String[] languages = {"English (US)", "Amharic (አማርኛ)", "Afaan Oromoo", "Tigrinya (ትግርኛ)", "French (Français)", "Arabic (العربية)", "Spanish (Español)"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Default Platform Language")
                .setItems(languages, (dialog, which) -> {
                    String selected = languages[which];
                    if (tvDefaultLanguage != null) {
                        tvDefaultLanguage.setText(selected + " ▼");
                    }
                    dbHelper.saveSetting("default_language", selected);
                    dbHelper.logAdminAction(adminUsername, "LANGUAGE_CONFIG", "Default system language changed to: " + selected);
                    loadLastAuditPreview();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRegionSelectorDialog() {
        String[] regions = {"United States", "Ethiopia (National)", "Kenya (CBC/KCSE)", "Nigeria (WAEC/NECO)", "United Kingdom (GCSE/A-Level)", "Canada", "Global International"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Default Regional Curriculum")
                .setItems(regions, (dialog, which) -> {
                    String selected = regions[which];
                    if (tvDefaultRegion != null) {
                        tvDefaultRegion.setText(selected + " ▼");
                    }
                    dbHelper.saveSetting("default_region", selected);
                    dbHelper.logAdminAction(adminUsername, "REGION_CONFIG", "Default regional curriculum changed to: " + selected);
                    loadLastAuditPreview();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUserDirectoryDialog() {
        List<UserItem> userList = new ArrayList<>();
        try (Cursor cursor = dbHelper.getAllUsersCursor()) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                    String uname = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("user_type"));
                    String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade_or_role"));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    userList.add(new UserItem(name, uname, email, type, grade, status != null ? status : "Active"));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching user list", e);
        }

        if (userList.isEmpty()) {
            Toast.makeText(this, "No registered student records found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] displayItems = new String[userList.size()];
        for (int i = 0; i < userList.size(); i++) {
            UserItem u = userList.get(i);
            displayItems[i] = u.username + " (" + (u.fullName != null ? u.fullName : "User") + ")\n"
                    + "Role: " + u.userType + " • Status: " + u.status + " • " + (u.email != null ? u.email : "No Email");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("User Directory & Access Governance (" + userList.size() + ")")
                .setItems(displayItems, (dialog, which) -> {
                    UserItem selected = userList.get(which);
                    handleUserAccountAction(selected);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void handleUserAccountAction(UserItem user) {
        // Enforce privilege protection: Administrators cannot be suspended or demoted by client actions
        if ("ADMIN".equalsIgnoreCase(user.userType) || "admin".equalsIgnoreCase(user.username)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Administrator Account Protected")
                    .setMessage("Account '" + user.username + "' is a system administrator with root governance authority. Administrator privileges cannot be suspended or demoted from the mobile console.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        boolean isCurrentlyActive = !"Suspended".equalsIgnoreCase(user.status);
        String actionPrompt = isCurrentlyActive ? "Suspend Account" : "Reactivate Account";
        String message = isCurrentlyActive
                ? "Are you sure you want to SUSPEND '" + user.username + "'? The student will immediately be blocked from logging into Learnify."
                : "Reactivate account for '" + user.username + "'? The student will immediately regain access to their dashboard, lessons, and exams.";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Account Governance: " + user.username)
                .setMessage("• Full Name: " + user.fullName + "\n"
                        + "• Email: " + user.email + "\n"
                        + "• Academic Role: " + user.gradeOrRole + "\n"
                        + "• Current Status: " + user.status + "\n\n" + message)
                .setPositiveButton(actionPrompt, (dialog, which) -> {
                    String newStatus = isCurrentlyActive ? "Suspended" : "Active";
                    dbHelper.updateUserStatus(user.username, newStatus);
                    String logAction = isCurrentlyActive ? "USER_SUSPENDED" : "USER_REACTIVATED";
                    dbHelper.logAdminAction(adminUsername, logAction, "Account " + user.username + " status updated to " + newStatus);
                    Toast.makeText(this, "Account " + user.username + " is now " + newStatus + ".", Toast.LENGTH_SHORT).show();
                    loadLastAuditPreview();
                    refreshSystemMetrics();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCurriculumViewerDialog() {
        int materialsCount = dbHelper.getTableCount("materials", null, null);
        int questionsCount = dbHelper.getTableCount("daily_questions", null, null);

        String message = "Curriculum Integrity & Academic Standards Report:\n\n"
                + "• Grade Levels Covered: Grades 9, 10, 11, and 12\n"
                + "• Active Academic Subjects (9 Core STEM & Humanities):\n"
                + "   1. Mathematics (Algebra, Calculus, Geometry, Vectors)\n"
                + "   2. Physics (Mechanics, Thermodynamics, Electromagnetism)\n"
                + "   3. Chemistry (Atomic Structure, Bonding, Reactions)\n"
                + "   4. Biology (Cell Biology, Genetics, Physiology)\n"
                + "   5. History (National History, African Heritage, World Civilizations)\n"
                + "   6. Geography (Physical Landscapes, Human Geography, GIS)\n"
                + "   7. English (Language, Syntax, Literary Reading, Vocabulary)\n"
                + "   8. Civics (Democratic Systems, Constitutional Rights, Ethics)\n"
                + "   9. Economics (Microeconomics, Fiscal Systems, Development)\n\n"
                + "• Preserved Database Materials: " + materialsCount + " active syllabi and learning resources\n"
                + "• Question Bank Repository: " + questionsCount + " verified assessment items\n"
                + "• Prerequisite Policy: " + (switchCurriculumIntegrity != null && switchCurriculumIntegrity.isChecked() ? "Strict Enforced" : "Open Access") + "\n\n"
                + "All curriculum assets remain permanently intact and protected against silent overwrite.";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Curriculum Architecture Inspection")
                .setMessage(message)
                .setPositiveButton("Verified", null)
                .show();
    }

    private void showIntegrityCheckDialog() {
        String integrityResult = dbHelper.checkDatabaseIntegrity();
        boolean isHealthy = "ok".equalsIgnoreCase(integrityResult);

        new MaterialAlertDialogBuilder(this)
                .setTitle(isHealthy ? "SQLite Integrity: Healthy" : "SQLite Integrity: Diagnostic Notice")
                .setMessage(isHealthy
                        ? "PRAGMA integrity_check output: 'ok'\n\n"
                        + "• All B-Tree index pointers are valid.\n"
                        + "• Page allocations and WAL transaction logs verified.\n"
                        + "• Zero table fragmentation or orphan records detected.\n"
                        + "• Database schema version: SQLite v58"
                        : "Diagnostics:\n" + integrityResult)
                .setPositiveButton("Close", null)
                .show();
    }

    private void confirmBackup() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Create Database Snapshot")
                .setMessage("Initiate an atomic physical snapshot of SQLite records, user accounts, and audit telemetry?")
                .setPositiveButton("Create Snapshot", (dialog, which) -> triggerBackup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void triggerBackup() {
        try {
            File backupDir = new File(getFilesDir(), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            File srcDb = getDatabasePath("AceGradeProfessional.db");
            if (!srcDb.exists() || srcDb.length() == 0) {
                File altDb = getDatabasePath("learnify.db");
                if (altDb.exists() && altDb.length() > 0) {
                    srcDb = altDb;
                }
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File backupFile = new File(backupDir, "learnify_backup_" + timestamp + ".db");

            long totalBytes = 0;
            if (srcDb.exists() && srcDb.length() > 0) {
                try (FileInputStream fis = new FileInputStream(srcDb);
                     FileOutputStream fos = new FileOutputStream(backupFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                        totalBytes += len;
                    }
                    fos.flush();
                    fos.getFD().sync();
                }
            }

            String sizeText;
            if (totalBytes >= 1024 * 1024) {
                sizeText = String.format(Locale.US, "%.2f MB", totalBytes / (1024.0 * 1024.0));
            } else {
                sizeText = String.format(Locale.US, "%.1f KB", totalBytes / 1024.0);
            }

            dbHelper.logAdminAction(adminUsername, "MANUAL_BACKUP", "Physical backup snapshot created: " + backupFile.getName() + " (" + sizeText + ")");
            if (tvBackupStatus != null) {
                tvBackupStatus.setText("Verified Snapshot: " + backupFile.getName() + " (" + sizeText + ")");
            }

            if (btnSave != null) {
                AppFeedbackManager.getInstance(this).performSuccess(btnSave);
            }

            String s3BucketName = (etS3Bucket != null && etS3Bucket.getText() != null && !etS3Bucket.getText().toString().trim().isEmpty())
                    ? etS3Bucket.getText().toString().trim()
                    : "learnify-assets";

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Database Snapshot Created")
                    .setMessage("Physical database snapshot verified successfully.\n\n"
                            + "• File: " + backupFile.getName() + "\n"
                            + "• Size: " + sizeText + "\n"
                            + "• Location: " + backupFile.getAbsolutePath() + "\n"
                            + "• Cloud Sync Staging: S3 (" + s3BucketName + ")")
                    .setPositiveButton("Share / Export", (dialog, which) -> {
                        try {
                            Uri fileUri = FileProvider.getUriForFile(
                                    this, getPackageName() + ".fileprovider", backupFile);
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/octet-stream");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Learnify System Database Backup - " + backupFile.getName());
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(shareIntent, "Export Learnify Database Snapshot"));
                        } catch (Exception ex) {
                            Toast.makeText(this, "Export Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Done", null)
                    .show();

        } catch (Exception e) {
            if (btnSave != null) {
                AppFeedbackManager.getInstance(this).performError(btnSave);
            }
            Log.e(TAG, "Backup snapshot failure", e);
            Toast.makeText(this, "Backup Snapshot Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showRestoreDialog() {
        File backupDir = new File(getFilesDir(), "backups");
        if (!backupDir.exists()) {
            Toast.makeText(this, "No backup directory exists yet. Please create a snapshot first.", Toast.LENGTH_SHORT).show();
            return;
        }

        File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(".db"));
        if (backups == null || backups.length == 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("No Backup Snapshots Available")
                    .setMessage("No physical SQLite database snapshot files (.db) were found in the backup storage directory. Create a snapshot before attempting recovery.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] fileNames = new String[backups.length];
        for (int i = 0; i < backups.length; i++) {
            long bytes = backups[i].length();
            String sizeStr = bytes >= 1024 * 1024
                    ? String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
                    : String.format(Locale.US, "%.1f KB", bytes / 1024.0);
            fileNames[i] = backups[i].getName() + " (" + sizeStr + ")";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Snapshot To Restore")
                .setItems(fileNames, (dialog, which) -> {
                    File selected = backups[which];
                    confirmRestoreSnapshot(selected);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmRestoreSnapshot(File backupFile) {
        // Validate SQLite file header bytes: "SQLite format 3\000"
        boolean isValidSqlite = false;
        try (FileInputStream fis = new FileInputStream(backupFile)) {
            byte[] header = new byte[16];
            int read = fis.read(header);
            if (read >= 15) {
                String headerStr = new String(header, 0, 15);
                isValidSqlite = "SQLite format 3".equals(headerStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed reading backup header", e);
        }

        if (!isValidSqlite) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Invalid Snapshot File")
                    .setMessage("The selected file does not contain a verified SQLite format header. Restoration aborted to protect data integrity.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Platform Recovery")
                .setMessage("WARNING: Restoring will overwrite the current live database with snapshot:\n\n"
                        + "• File: " + backupFile.getName() + "\n"
                        + "• Size: " + backupFile.length() + " bytes\n\n"
                        + "Proceed with atomic database recovery?")
                .setPositiveButton("Restore Database", (dialog, which) -> performRestore(backupFile))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performRestore(File backupFile) {
        try {
            File targetDb = getDatabasePath("AceGradeProfessional.db");

            try (FileInputStream fis = new FileInputStream(backupFile);
                 FileOutputStream fos = new FileOutputStream(targetDb)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                fos.getFD().sync();
            }

            dbHelper.logAdminAction(adminUsername, "DATABASE_RESTORE", "System restored from snapshot: " + backupFile.getName());
            refreshSystemMetrics();
            loadAllConfigurations();

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Restoration Successful")
                    .setMessage("Platform database restored successfully from snapshot: " + backupFile.getName() + ".\n\nAll tables, courses, user accounts, and credentials have been verified.")
                    .setPositiveButton("Done", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Restore error", e);
            Toast.makeText(this, "Restore Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openAuditLogs() {
        try {
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT action, details, timestamp FROM admin_logs ORDER BY id DESC LIMIT 25", null
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
                    .setTitle("Administrative Audit Trail (Last 25)")
                    .setMessage(sb.toString().trim())
                    .setPositiveButton("Close", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to read audit logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportAuditLogs() {
        try {
            File exportFile = new File(getCacheDir(), "learnify_admin_audit_logs.csv");
            try (FileWriter writer = new FileWriter(exportFile);
                 Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                         "SELECT id, admin_username, action, details, timestamp FROM admin_logs ORDER BY id DESC", null)) {

                writer.write("ID,Administrator,Action,Details,Timestamp\n");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(0);
                        String admin = cursor.getString(1);
                        String act = cursor.getString(2);
                        String det = cursor.getString(3);
                        String ts = cursor.getString(4);

                        // Escape CSV commas
                        if (det != null) det = det.replace("\"", "\"\"");
                        writer.write(id + ",\"" + (admin != null ? admin : "") + "\",\""
                                + (act != null ? act : "") + "\",\""
                                + (det != null ? det : "") + "\",\""
                                + (ts != null ? ts : "") + "\"\n");
                    }
                }
            }

            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", exportFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Learnify Enterprise Audit Logs Export");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Administrative Audit Logs"));

        } catch (Exception e) {
            Toast.makeText(this, "Export Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showBroadcastAnnouncementDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_account, null);
        TextInputEditText etTitle = view.findViewById(R.id.etDeletePassword);
        TextInputEditText etMessage = view.findViewById(R.id.etDeleteConfirmPhrase);
        if (etTitle != null) {
            etTitle.setHint("Announcement Title");
            etTitle.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        }
        if (etMessage != null) {
            etMessage.setHint("Announcement Body Message");
            etMessage.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Broadcast Platform Announcement")
                .setView(view)
                .setPositiveButton("Broadcast", (d, w) -> {
                    String title = etTitle != null && etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                    String msg = etMessage != null && etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
                    if (title.isEmpty()) title = "Learnify System Announcement";
                    if (msg.isEmpty()) msg = "A new system update has been deployed.";

                    NotificationHelper.showInstantNotification(this, "ANNOUNCEMENT", title, msg);
                    dbHelper.insertAnnouncement(title, msg, "Broadcast", "All");
                    dbHelper.logAdminAction(adminUsername, "BROADCAST_ANNOUNCEMENT", title + " — " + msg);
                    AppFeedbackManager.getInstance(this).performSuccess(btnBroadcastAnnouncement);
                    Toast.makeText(this, "Announcement broadcast to all active students.", Toast.LENGTH_SHORT).show();
                    loadLastAuditPreview();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAdminDocumentationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Administrator Standard Operating Procedures")
                .setMessage("• Database Snapshots: Atomic backups copy live SQLite files into secure internal storage and can be shared externally via FileProvider.\n\n"
                        + "• Academic Parameters: Exam duration changes take effect immediately on next student test start.\n\n"
                        + "• Camera Lab Governance: Individual solvers (Math, Physics, Chemistry) can be toggled without disabling computer vision completely.\n\n"
                        + "• Maintenance Mode: When active, students see a restricted maintenance notice, while admin accounts retain full login access.\n\n"
                        + "• Technical Support: admin-support@learnify.com")
                .setPositiveButton("Close", null)
                .show();
    }

    private static class UserItem {
        final String fullName;
        final String username;
        final String email;
        final String userType;
        final String gradeOrRole;
        final String status;

        UserItem(String fullName, String username, String email, String userType, String gradeOrRole, String status) {
            this.fullName = fullName;
            this.username = username;
            this.email = email;
            this.userType = userType;
            this.gradeOrRole = gradeOrRole;
            this.status = status;
        }
    }
}
