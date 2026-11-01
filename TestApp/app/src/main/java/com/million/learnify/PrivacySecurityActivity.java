package com.million.learnify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * PrivacySecurityActivity - Central Production Account Security &amp; Compliance Console.
 * Manages verified 2FA enrollment (RFC 6238 TOTP), device session revocation,
 * dynamic password policy feedback, security audit history, and safe data governance.
 */
public class PrivacySecurityActivity extends BaseActivity {

    private static final String TAG = "PrivacySecurity";

    // Views - Security Posture
    private ImageView ivSecurityShield;
    private TextView tvTopSecurityBadge, tvSecurityScore, tvSecurityScoreSubtitle, tvSecurityRecommendation;
    private ProgressBar pbSecurityScore;

    // Views - Account Security
    private LinearLayout llChangePassword, llTwoFactor, llRecoveryMethods, llSuspiciousAlerts;
    private TextView tvTwoFactorStatusBadge, tvRecoveryEmailPreview;
    private SwitchCompat switchSuspiciousAlerts;

    // Views - Sessions & Devices
    private LinearLayout llSessionsContainer;
    private MaterialButton btnRevokeOtherSessions;

    // Views - Audit Logs
    private TextView tvLastSecurityEventPreview;
    private MaterialButton btnViewSecurityHistory;

    // Views - Data & Danger Zone
    private LinearLayout llExportData, llPrivacyPolicy, llDeleteAccount;

    private DatabaseHelper dbHelper;
    private SecurePreferencesHelper securePrefs;
    private SettingsService settingsService;
    private StudentSettings currentSettings;

    private String currentUsername = "";
    private String currentSessionId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsService = SettingsService.getInstance(this);
        currentSettings = settingsService.loadSettings();
        securePrefs = new SecurePreferencesHelper(this);
        dbHelper = repository.getLocalDb();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_security);

        resolveAuthenticatedUser();
        setupToolbar();
        initViews();
        setupListeners();

        ensureCurrentSessionRegistered();
        loadActiveSessions();
        refreshSecurityPostureUI();
        loadLastSecurityEventPreview();
    }

    private void resolveAuthenticatedUser() {
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = session.getString("username", "");
        if (currentUsername.isEmpty() && securePrefs != null) {
            currentUsername = securePrefs.getString("username", "");
        }
        if (currentUsername.isEmpty()) {
            currentUsername = "student";
        }

        currentSessionId = securePrefs.getSessionId();
        if (currentSessionId.isEmpty()) {
            currentSessionId = UUID.randomUUID().toString();
            securePrefs.saveSessionId(currentSessionId);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.privacyToolbar);
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
        // Security Posture Header
        tvTopSecurityBadge = findViewById(R.id.tvTopSecurityBadge);
        ivSecurityShield = findViewById(R.id.ivSecurityShield);
        tvSecurityScore = findViewById(R.id.tvSecurityScore);
        tvSecurityScoreSubtitle = findViewById(R.id.tvSecurityScoreSubtitle);
        pbSecurityScore = findViewById(R.id.pbSecurityScore);
        tvSecurityRecommendation = findViewById(R.id.tvSecurityRecommendation);

        // Account Security
        llChangePassword = findViewById(R.id.llChangePassword);
        llTwoFactor = findViewById(R.id.llTwoFactor);
        tvTwoFactorStatusBadge = findViewById(R.id.tvTwoFactorStatusBadge);
        llRecoveryMethods = findViewById(R.id.llRecoveryMethods);
        tvRecoveryEmailPreview = findViewById(R.id.tvRecoveryEmailPreview);
        llSuspiciousAlerts = findViewById(R.id.llSuspiciousAlerts);
        switchSuspiciousAlerts = findViewById(R.id.switchSuspiciousAlerts);

        // Sessions & Devices
        llSessionsContainer = findViewById(R.id.llSessionsContainer);
        btnRevokeOtherSessions = findViewById(R.id.btnRevokeOtherSessions);

        // Audit Trail
        tvLastSecurityEventPreview = findViewById(R.id.tvLastSecurityEventPreview);
        btnViewSecurityHistory = findViewById(R.id.btnViewSecurityHistory);

        // Data & Danger Zone
        llExportData = findViewById(R.id.llExportData);
        llPrivacyPolicy = findViewById(R.id.llPrivacyPolicy);
        llDeleteAccount = findViewById(R.id.llDeleteAccount);

        if (switchSuspiciousAlerts != null) {
            switchSuspiciousAlerts.setChecked(securePrefs.isSuspiciousAlertsEnabled());
            switchSuspiciousAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
                securePrefs.saveSuspiciousAlertsEnabled(isChecked);
                dbHelper.recordSecurityEvent(currentUsername, "SUSPICIOUS_ALERTS_TOGGLE",
                        "Suspicious login alerts set to " + (isChecked ? "Enabled" : "Disabled"), "Mobile App");
                refreshSecurityPostureUI();
            });
        }
    }

    private void setupListeners() {
        if (llChangePassword != null) {
            llChangePassword.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showChangePasswordDialog();
            });
        }

        if (llTwoFactor != null) {
            llTwoFactor.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showTwoFactorDialog();
            });
        }

        if (llRecoveryMethods != null) {
            llRecoveryMethods.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showRecoveryMethodsDialog();
            });
        }

        if (btnRevokeOtherSessions != null) {
            btnRevokeOtherSessions.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                confirmRevokeAllOtherSessions();
            });
        }

        if (btnViewSecurityHistory != null) {
            btnViewSecurityHistory.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showSecurityHistoryDialog();
            });
        }

        if (llExportData != null) {
            llExportData.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showExportDataDialog();
            });
        }

        if (llPrivacyPolicy != null) {
            llPrivacyPolicy.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showPrivacyPolicyDialog();
            });
        }

        if (llDeleteAccount != null) {
            llDeleteAccount.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showDeleteAccountDialog();
            });
        }
    }

    // --- 1. Security Posture Calculation & UI ---

    private void refreshSecurityPostureUI() {
        boolean is2faEnabled = securePrefs.isTwoFactorEnabled();
        String recoveryEmail = securePrefs.getRecoveryEmail();
        boolean hasRecoveryEmail = recoveryEmail != null && !recoveryEmail.trim().isEmpty();
        boolean suspiciousAlerts = securePrefs.isSuspiciousAlertsEnabled();
        long lastPwdChange = securePrefs.getLastPasswordChange();
        boolean hasChangedPassword = lastPwdChange > 0;

        int score = 25; // baseline strong credentials
        if (is2faEnabled) score += 35;
        if (hasRecoveryEmail) score += 20;
        if (suspiciousAlerts) score += 10;
        if (hasChangedPassword) score += 10;
        if (score > 100) score = 100;

        if (pbSecurityScore != null) {
            pbSecurityScore.setProgress(score);
        }

        if (tvSecurityScore != null) {
            if (score >= 85) {
                tvSecurityScore.setText("Security Posture: Strong (" + score + "%)");
                if (pbSecurityScore != null) pbSecurityScore.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF10B981));
                if (tvTopSecurityBadge != null) tvTopSecurityBadge.setText("STRONG");
            } else if (score >= 60) {
                tvSecurityScore.setText("Security Posture: Good (" + score + "%)");
                if (pbSecurityScore != null) pbSecurityScore.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF2563EB));
                if (tvTopSecurityBadge != null) tvTopSecurityBadge.setText("GOOD");
            } else {
                tvSecurityScore.setText("Security Posture: Attention Needed (" + score + "%)");
                if (pbSecurityScore != null) pbSecurityScore.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFF59E0B));
                if (tvTopSecurityBadge != null) tvTopSecurityBadge.setText("NEEDS WORK");
            }
        }

        if (tvSecurityRecommendation != null) {
            if (!is2faEnabled) {
                tvSecurityRecommendation.setText("Critical: Activate Two-Factor Authentication to block unauthorized logins.");
            } else if (!hasRecoveryEmail) {
                tvSecurityRecommendation.setText("Tip: Configure a secondary recovery email for emergency account recovery.");
            } else if (!suspiciousAlerts) {
                tvSecurityRecommendation.setText("Tip: Enable suspicious login notifications to be alerted of new devices.");
            } else {
                tvSecurityRecommendation.setText("All recommended authentication gates and safeguards are actively protecting your portal.");
            }
        }

        if (tvTwoFactorStatusBadge != null) {
            tvTwoFactorStatusBadge.setText(is2faEnabled ? "ENABLED" : "DISABLED");
            tvTwoFactorStatusBadge.setTextColor(is2faEnabled ? 0xFF10B981 : 0xFFEF4444);
        }

        if (tvRecoveryEmailPreview != null) {
            tvRecoveryEmailPreview.setText(hasRecoveryEmail
                    ? "Recovery Email: " + recoveryEmail
                    : "Not configured • Tap to set recovery contact");
        }
    }

    private void loadLastSecurityEventPreview() {
        if (tvLastSecurityEventPreview == null) return;
        try (Cursor cursor = dbHelper.getUserSecurityLogs(currentUsername, 1)) {
            if (cursor != null && cursor.moveToFirst()) {
                String eventType = cursor.getString(cursor.getColumnIndexOrThrow("event_type"));
                String ts = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                tvLastSecurityEventPreview.setText("Last event: " + eventType + " (" + (ts != null ? ts : "Recent") + ")");
            } else {
                tvLastSecurityEventPreview.setText("All session tokens and authentication gates active.");
            }
        } catch (Exception ignored) {}
    }

    // --- 2. Device & Session Management ---

    private void ensureCurrentSessionRegistered() {
        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")";
        dbHelper.registerSession(currentSessionId, currentUsername, deviceInfo, "Local Active Connection", true);
    }

    private void loadActiveSessions() {
        if (llSessionsContainer == null) return;
        llSessionsContainer.removeAllViews();

        List<SessionItem> sessionList = new ArrayList<>();
        try (Cursor cursor = dbHelper.getUserSessions(currentUsername)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String sId = cursor.getString(cursor.getColumnIndexOrThrow("session_id"));
                    String dev = cursor.getString(cursor.getColumnIndexOrThrow("device_info"));
                    String ip = cursor.getString(cursor.getColumnIndexOrThrow("ip_address"));
                    boolean isCurr = cursor.getInt(cursor.getColumnIndexOrThrow("is_current")) == 1;
                    String created = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                    String lastActive = cursor.getString(cursor.getColumnIndexOrThrow("last_active"));
                    sessionList.add(new SessionItem(sId, dev, ip, isCurr, created, lastActive));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying sessions", e);
        }

        if (sessionList.isEmpty()) {
            ensureCurrentSessionRegistered();
            sessionList.add(new SessionItem(currentSessionId, Build.MANUFACTURER + " " + Build.MODEL, "Current Connection", true, "Just now", "Just now"));
        }

        for (SessionItem item : sessionList) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_badge_active);
            card.setPadding(32, 24, 32, 24);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 16);
            card.setLayoutParams(lp);

            // Header line: Device info & Current Badge
            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvDevice = new TextView(this);
            tvDevice.setText(item.deviceInfo != null ? item.deviceInfo : "Android Device");
            tvDevice.setTextSize(14f);
            tvDevice.setTypeface(null, android.graphics.Typeface.BOLD);
            tvDevice.setTextColor(0xFF1E293B);
            LinearLayout.LayoutParams devLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvDevice.setLayoutParams(devLp);
            topRow.addView(tvDevice);

            if (item.isCurrent || item.sessionId.equals(currentSessionId)) {
                TextView tvBadge = new TextView(this);
                tvBadge.setText("THIS DEVICE");
                tvBadge.setTextSize(10f);
                tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                tvBadge.setTextColor(0xFF059669);
                topRow.addView(tvBadge);
            } else {
                MaterialButton btnRevoke = new MaterialButton(this, null, com.google.android.material.R.attr.borderlessButtonStyle);
                btnRevoke.setText("Revoke");
                btnRevoke.setTextColor(0xFFEF4444);
                btnRevoke.setTextSize(12f);
                btnRevoke.setOnClickListener(v -> confirmRevokeSingleSession(item));
                topRow.addView(btnRevoke);
            }
            card.addView(topRow);

            // Subtitle line: IP & Last active
            TextView tvSub = new TextView(this);
            tvSub.setText("Network: " + item.ipAddress + " • Active: " + (item.lastActive != null ? item.lastActive : "Recent"));
            tvSub.setTextSize(12f);
            tvSub.setTextColor(0xFF64748B);
            tvSub.setPadding(0, 6, 0, 0);
            card.addView(tvSub);

            llSessionsContainer.addView(card);
        }
    }

    private void confirmRevokeSingleSession(SessionItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Revoke Device Session")
                .setMessage("Terminate active session token for '" + item.deviceInfo + "'? The device will immediately be signed out of Learnify.")
                .setPositiveButton("Revoke Session", (dialog, which) -> {
                    dbHelper.revokeSession(item.sessionId, currentUsername);
                    Toast.makeText(this, "Session terminated.", Toast.LENGTH_SHORT).show();
                    loadActiveSessions();
                    loadLastSecurityEventPreview();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmRevokeAllOtherSessions() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sign Out All Other Devices")
                .setMessage("Are you sure you want to sign out of all other devices? All secondary sessions and cached credentials will be revoked immediately.")
                .setPositiveButton("Sign Out Others", (dialog, which) -> {
                    int count = dbHelper.revokeAllOtherSessions(currentSessionId, currentUsername);
                    Toast.makeText(this, "Terminated " + count + " other active session(s).", Toast.LENGTH_SHORT).show();
                    loadActiveSessions();
                    loadLastSecurityEventPreview();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- 3. Dynamic Password Policy Feedback Dialog ---

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrent = view.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew = view.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = view.findViewById(R.id.etConfirmPassword);

        // Build policy feedback container dynamically
        LinearLayout container = (LinearLayout) view;
        TextView tvPolicyHeader = new TextView(this);
        tvPolicyHeader.setText("Password Strength Policy:");
        tvPolicyHeader.setTextSize(12f);
        tvPolicyHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        tvPolicyHeader.setTextColor(0xFF475569);
        tvPolicyHeader.setPadding(0, 16, 0, 8);
        container.addView(tvPolicyHeader);

        TextView tvLen = createPolicyCheckView("• Minimum 8 characters");
        TextView tvUpper = createPolicyCheckView("• At least one uppercase letter (A-Z)");
        TextView tvLower = createPolicyCheckView("• At least one lowercase letter (a-z)");
        TextView tvNum = createPolicyCheckView("• At least one number (0-9)");
        TextView tvSym = createPolicyCheckView("• At least one special symbol (@#$%...)");

        container.addView(tvLen);
        container.addView(tvUpper);
        container.addView(tvLower);
        container.addView(tvNum);
        container.addView(tvSym);

        if (etNew != null) {
            etNew.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int count, int after) {
                    String pwd = s.toString();
                    updatePolicyView(tvLen, pwd.length() >= 8);
                    updatePolicyView(tvUpper, pwd.matches(".*[A-Z].*"));
                    updatePolicyView(tvLower, pwd.matches(".*[a-z].*"));
                    updatePolicyView(tvNum, pwd.matches(".*[0-9].*"));
                    updatePolicyView(tvSym, pwd.matches(".*[-@#$%^&+=!_~*?].*"));
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Change Account Password")
                .setView(view)
                .setPositiveButton("Update Password", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPass = etCurrent != null && etCurrent.getText() != null ? etCurrent.getText().toString().trim() : "";
            String newPass = etNew != null && etNew.getText() != null ? etNew.getText().toString().trim() : "";
            String confirmPass = etConfirm != null && etConfirm.getText() != null ? etConfirm.getText().toString().trim() : "";

            if (currentPass.isEmpty()) {
                if (etCurrent != null) etCurrent.setError("Current password is required");
                return;
            }

            if (!validatePasswordPolicy(newPass)) {
                if (etNew != null) etNew.setError("Password does not meet all policy rules");
                Toast.makeText(this, "Password must satisfy all 5 security policy criteria", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                if (etConfirm != null) etConfirm.setError("Passwords do not match");
                return;
            }

            if (currentPass.equals(newPass)) {
                if (etNew != null) etNew.setError("New password must differ from current password");
                return;
            }

            boolean isCurrentValid = dbHelper.verifyUserPassword(currentUsername, currentPass);
            if (!isCurrentValid) {
                if (etCurrent != null) etCurrent.setError("Incorrect current password");
                Toast.makeText(this, "Current password verification failed.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sync with Firebase if available
            FirebaseAuth auth = FirebaseAuthSupport.getConfiguredAuth(this);
            FirebaseUser firebaseUser = auth != null ? auth.getCurrentUser() : null;
            if (firebaseUser != null && firebaseUser.getEmail() != null) {
                AuthCredential cred = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPass);
                firebaseUser.reauthenticate(cred).addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        firebaseUser.updatePassword(newPass);
                    }
                });
            }

            boolean updated = dbHelper.updatePassword(currentUsername, newPass);
            if (updated) {
                securePrefs.saveLastPasswordChange(System.currentTimeMillis());
                dbHelper.recordSecurityEvent(currentUsername, "PASSWORD_CHANGED", "Account password updated successfully", Build.MODEL);
                AppFeedbackManager.getInstance(this).performSuccess(llChangePassword);
                Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                refreshSecurityPostureUI();
                loadLastSecurityEventPreview();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to persist new password. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TextView createPolicyCheckView(String label) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(12f);
        tv.setTextColor(0xFF94A3B8);
        tv.setPadding(12, 2, 0, 2);
        return tv;
    }

    private void updatePolicyView(TextView tv, boolean passed) {
        if (tv == null) return;
        tv.setTextColor(passed ? 0xFF059669 : 0xFF94A3B8);
        String text = tv.getText().toString();
        if (passed && text.startsWith("•")) {
            tv.setText("✓" + text.substring(1));
        } else if (!passed && text.startsWith("✓")) {
            tv.setText("•" + text.substring(1));
        }
    }

    private boolean validatePasswordPolicy(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSymbol = password.matches(".*[-@#$%^&+=!_~*?].*");
        return hasUpper && hasLower && hasDigit && hasSymbol;
    }

    // --- 4. Two-Factor Authentication (TOTP Enrollment & Verification) ---

    private void showTwoFactorDialog() {
        boolean currentlyEnabled = securePrefs.isTwoFactorEnabled();

        if (currentlyEnabled) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_account, null);
            TextInputEditText etPwd = view.findViewById(R.id.etDeletePassword);
            TextInputEditText etPhrase = view.findViewById(R.id.etDeleteConfirmPhrase);
            if (etPwd != null) etPwd.setHint("Current Password");
            if (etPhrase != null) etPhrase.setVisibility(View.GONE);

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Disable Two-Factor Authentication")
                    .setMessage("Two-Factor Authentication is currently active. Disabling 2FA reduces account security. Re-enter your current password to confirm disabling:")
                    .setView(view)
                    .setPositiveButton("Disable 2FA", (dialog, which) -> {
                        String pwd = etPwd != null && etPwd.getText() != null ? etPwd.getText().toString().trim() : "";
                        if (!dbHelper.verifyUserPassword(currentUsername, pwd)) {
                            Toast.makeText(this, "Incorrect password. Cannot disable 2FA.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        securePrefs.saveTwoFactorEnabled(false);
                        securePrefs.saveTwoFactorSecret("");
                        dbHelper.saveUserSecuritySettings(currentUsername, "", false, securePrefs.getRecoveryEmail(), "", securePrefs.isSuspiciousAlertsEnabled());
                        dbHelper.recordSecurityEvent(currentUsername, "2FA_DISABLED", "Two-Factor Authentication was deactivated", Build.MODEL);

                        Toast.makeText(this, "2FA has been disabled.", Toast.LENGTH_SHORT).show();
                        refreshSecurityPostureUI();
                        loadLastSecurityEventPreview();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            String generatedSecret = TotpManager.generateSecret();
            String provisioningUri = TotpManager.getProvisioningUri(currentUsername, generatedSecret);

            View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_account, null);
            TextInputEditText etSecretKey = view.findViewById(R.id.etDeletePassword);
            TextInputEditText etCode = view.findViewById(R.id.etDeleteConfirmPhrase);

            if (etSecretKey != null) {
                etSecretKey.setHint("Generated Base32 Secret Key");
                etSecretKey.setText(generatedSecret);
                etSecretKey.setFocusable(false);
                etSecretKey.setClickable(true);
                etSecretKey.setOnClickListener(v -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("Learnify 2FA Secret", generatedSecret));
                    Toast.makeText(this, "Secret copied to clipboard!", Toast.LENGTH_SHORT).show();
                });
            }

            if (etCode != null) {
                etCode.setHint("Enter 6-Digit Authenticator Code");
                etCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            }

            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setTitle("Enroll in Two-Factor Authentication")
                    .setMessage("1. Copy the secret key below into Google Authenticator or Microsoft Authenticator.\n\n"
                            + "2. Enter the generated 6-digit TOTP code to verify and activate:")
                    .setView(view)
                    .setPositiveButton("Verify & Activate", null)
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String codeStr = etCode != null && etCode.getText() != null ? etCode.getText().toString().trim() : "";
                if (codeStr.length() != 6) {
                    if (etCode != null) etCode.setError("Enter 6 digits");
                    return;
                }

                boolean verified = TotpManager.verifyCode(generatedSecret, codeStr);
                if (!verified) {
                    if (etCode != null) etCode.setError("Invalid or expired 6-digit code");
                    Toast.makeText(this, "Code verification failed. Check authenticator clock and try again.", Toast.LENGTH_LONG).show();
                    return;
                }

                List<String> recoveryCodes = TotpManager.generateRecoveryCodes(8);
                StringBuilder codesSb = new StringBuilder();
                for (String c : recoveryCodes) {
                    codesSb.append(c).append(",");
                }
                String joinedCodes = codesSb.toString();

                securePrefs.saveTwoFactorEnabled(true);
                securePrefs.saveTwoFactorSecret(generatedSecret);
                securePrefs.saveRecoveryCodes(joinedCodes);

                dbHelper.saveUserSecuritySettings(currentUsername, generatedSecret, true,
                        securePrefs.getRecoveryEmail(), joinedCodes, securePrefs.isSuspiciousAlertsEnabled());
                dbHelper.recordSecurityEvent(currentUsername, "2FA_ENABLED", "Enrolled and verified RFC 6238 TOTP 2FA", Build.MODEL);

                AppFeedbackManager.getInstance(this).performSuccess(llTwoFactor);
                Toast.makeText(this, "Two-Factor Authentication Activated!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                showBackupCodesDialog(recoveryCodes);
                refreshSecurityPostureUI();
                loadLastSecurityEventPreview();
            });
        }
    }

    private void showBackupCodesDialog(List<String> codes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codes.size(); i++) {
            sb.append("• ").append(codes.get(i)).append("\n");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Save Your Backup Recovery Codes")
                .setMessage("Save these 8 one-time backup recovery codes in a secure location. If you lose access to your authenticator app, these codes are the only way to recover access:\n\n" + sb.toString())
                .setPositiveButton("Copy All Codes", (d, w) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("Learnify Backup Codes", sb.toString()));
                    Toast.makeText(this, "Backup codes copied to clipboard.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("I Have Saved Them", null)
                .show();
    }

    // --- 5. Account Recovery Methods ---

    private void showRecoveryMethodsDialog() {
        String[] options = {"Set / Update Recovery Email", "View 2FA Backup Recovery Codes"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Account Recovery Configuration")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showSetRecoveryEmailDialog();
                    } else {
                        showViewBackupCodesDialog();
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showSetRecoveryEmailDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_account, null);
        TextInputEditText etEmail = view.findViewById(R.id.etDeletePassword);
        TextInputEditText etExtra = view.findViewById(R.id.etDeleteConfirmPhrase);
        if (etExtra != null) etExtra.setVisibility(View.GONE);

        if (etEmail != null) {
            etEmail.setHint("Dedicated Recovery Email");
            etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            etEmail.setText(securePrefs.getRecoveryEmail());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Configure Recovery Email")
                .setMessage("Enter a verified secondary email address. Learnify will dispatch password recovery OTP codes to this address if you are locked out.")
                .setView(view)
                .setPositiveButton("Save Email", (d, w) -> {
                    String email = etEmail != null && etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    securePrefs.saveRecoveryEmail(email);
                    dbHelper.saveUserSecuritySettings(currentUsername, securePrefs.getTwoFactorSecret(),
                            securePrefs.isTwoFactorEnabled(), email, securePrefs.getRecoveryCodes(), securePrefs.isSuspiciousAlertsEnabled());
                    dbHelper.recordSecurityEvent(currentUsername, "RECOVERY_EMAIL_UPDATED", "Updated recovery email to: " + email, Build.MODEL);

                    Toast.makeText(this, "Recovery email updated successfully.", Toast.LENGTH_SHORT).show();
                    refreshSecurityPostureUI();
                    loadLastSecurityEventPreview();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showViewBackupCodesDialog() {
        String rawCodes = securePrefs.getRecoveryCodes();
        if (rawCodes == null || rawCodes.trim().isEmpty()) {
            Toast.makeText(this, "No backup recovery codes active. Enable 2FA to generate codes.", Toast.LENGTH_LONG).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_account, null);
        TextInputEditText etPwd = view.findViewById(R.id.etDeletePassword);
        TextInputEditText etExtra = view.findViewById(R.id.etDeleteConfirmPhrase);
        if (etExtra != null) etExtra.setVisibility(View.GONE);
        if (etPwd != null) etPwd.setHint("Current Password");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Identity")
                .setMessage("Re-enter your account password to decrypt and view your backup recovery codes:")
                .setView(view)
                .setPositiveButton("View Codes", (dialog, which) -> {
                    String pwd = etPwd != null && etPwd.getText() != null ? etPwd.getText().toString().trim() : "";
                    if (!dbHelper.verifyUserPassword(currentUsername, pwd)) {
                        Toast.makeText(this, "Incorrect password. Cannot reveal recovery codes.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] codes = rawCodes.split(",");
                    showBackupCodesDialog(Arrays.asList(codes));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- 6. Security Event History & Audit Log ---

    private void showSecurityHistoryDialog() {
        List<String> eventStrings = new ArrayList<>();
        try (Cursor cursor = dbHelper.getUserSecurityLogs(currentUsername, 25)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("event_type"));
                    String details = cursor.getString(cursor.getColumnIndexOrThrow("event_details"));
                    String dev = cursor.getString(cursor.getColumnIndexOrThrow("device_info"));
                    String ts = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

                    eventStrings.add("• [" + (ts != null ? ts : "Recent") + "]\n  "
                            + type + ": " + (details != null ? details : "") + " (" + (dev != null ? dev : "Device") + ")\n");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching security logs", e);
        }

        if (eventStrings.isEmpty()) {
            eventStrings.add("No recent security events recorded in current session.");
        }

        StringBuilder sb = new StringBuilder();
        for (String ev : eventStrings) {
            sb.append(ev).append("\n");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Security Event Audit History")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Done", null)
                .show();
    }

    // --- 7. Data Export and Governance ---

    private void showExportDataDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Export Learning Records")
                .setMessage("We will generate a verified JSON archive containing your academic profile, enrolled courses, certificates, quiz & exam results, and learning streak statistics.\n\nSensitive security credentials (passwords, salts, auth tokens) are strictly excluded.")
                .setPositiveButton("Export & Share", (dialog, which) -> performDataExport(currentUsername))
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
            File exportFile = new File(exportDir, "learnify_data_export_" + username + ".json");
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
            dbHelper.recordSecurityEvent(username, "DATA_EXPORTED", "Exported personal profile and academic records JSON", Build.MODEL);
            AppFeedbackManager.getInstance(this).performSuccess(llExportData);
            Toast.makeText(this, "Data archive prepared! Select an app to share or save.", Toast.LENGTH_LONG).show();
            loadLastSecurityEventPreview();
        } catch (Exception e) {
            Log.e(TAG, "Failed to export user data", e);
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showPrivacyPolicyDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Data Privacy & Retention Policy")
                .setMessage("• Data Minimization: Learnify collects only necessary academic telemetry (quiz results, lesson progress, attendance) to facilitate your high school study program.\n\n"
                        + "• Local Encryption: Sensitive preferences and tokens are stored in Android Keystore AES-256 GCM encrypted containers.\n\n"
                        + "• Academic Content Preservation: Curriculum resources, exam syllabi, and practice questions remain permanently accessible regardless of subscription tier.\n\n"
                        + "• GDPR & Right to Erasure: Permanent account deletion completely cascades and purges all student records from local databases.\n\n"
                        + "• Inquiries: privacy-officer@learnify.com")
                .setPositiveButton("Close", null)
                .show();
    }

    // --- 8. Permanent Account Deletion ---

    private void showDeleteAccountDialog() {
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
                return;
            }

            if (!"DELETE".equals(enteredPhrase)) {
                if (etConfirmPhrase != null) etConfirmPhrase.setError("Type DELETE in all capital letters");
                return;
            }

            boolean isPasswordCorrect = dbHelper.verifyUserPassword(currentUsername, enteredPassword);
            if (!isPasswordCorrect) {
                if (etPassword != null) etPassword.setError("Incorrect password");
                Toast.makeText(this, "Incorrect password. Cannot proceed.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean deleted = repository.deleteUserAccount(currentUsername);
            if (deleted) {
                if (securePrefs != null) {
                    securePrefs.clearSession();
                }
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

    private static class SessionItem {
        final String sessionId;
        final String deviceInfo;
        final String ipAddress;
        final boolean isCurrent;
        final String createdAt;
        final String lastActive;

        SessionItem(String sessionId, String deviceInfo, String ipAddress, boolean isCurrent, String createdAt, String lastActive) {
            this.sessionId = sessionId;
            this.deviceInfo = deviceInfo;
            this.ipAddress = ipAddress;
            this.isCurrent = isCurrent;
            this.createdAt = createdAt;
            this.lastActive = lastActive;
        }
    }
}
