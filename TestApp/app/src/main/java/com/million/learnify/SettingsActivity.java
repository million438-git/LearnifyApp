package com.million.learnify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.Locale;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private SettingsService settingsService;
    private StudentSettings currentSettings;
    
    private ImageView btnSaveSettings;
    private TextView tvReminderTime, tvLanguage, tvRegion, tvDateFormat, tvTheme, tvFontSize, tvCacheSize, tvAutoDownload;
    private TextView tvSpeechRate;
    private MaterialSwitch switchSoundEffects, switchHapticFeedback, switchAnimations, switchReduceMotion, switchOfflineMode, switchCloudSync;
    private MaterialSwitch switchInclusiveMode, switchTextToSpeech, switchHighContrast, switchAutoRead;
    
    private LinearLayout llAccountSettings, llPrivacySecurity, llNotificationPrefs;
    private LinearLayout llReminderTimeView, llLanguageView, llRegionView, llDateFormatView;
    private LinearLayout llThemeView, llFontSizeView, llClearCache, llAutoDownloadView;
    private LinearLayout llSpeechRateView;
    private LinearLayout llLinkedAccounts, llShareProgress, llInviteFriends;
    private LinearLayout llTerms, llPrivacyPolicy, llRateUs, llDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            settingsService = new SettingsService(this);
            currentSettings = settingsService.loadSettings();
            if (currentSettings == null) {
                currentSettings = new StudentSettings("light", true);
            }
            
            setContentView(R.layout.activity_settings);

            setupToolbar();
            initViews();
            loadSettingsToUI();
            setupListeners();
            
            updateCacheSizeDisplay();
            requestNotificationPermission();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "UI Load Error", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.settingsToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        tvLanguage = findViewById(R.id.tvLanguage);
        tvRegion = findViewById(R.id.tvRegion);
        tvDateFormat = findViewById(R.id.tvDateFormat);
        tvTheme = findViewById(R.id.tvTheme);
        tvFontSize = findViewById(R.id.tvFontSize);
        tvCacheSize = findViewById(R.id.tvCacheSize);
        tvAutoDownload = findViewById(R.id.tvAutoDownload);
        tvSpeechRate = findViewById(R.id.tvSpeechRate);

        switchSoundEffects = findViewById(R.id.switchSoundEffects);
        switchHapticFeedback = findViewById(R.id.switchHapticFeedback);
        switchAnimations = findViewById(R.id.switchAnimations);
        switchReduceMotion = findViewById(R.id.switchReduceMotion);
        switchOfflineMode = findViewById(R.id.switchOfflineMode);
        switchCloudSync = findViewById(R.id.switchCloudSync);

        switchInclusiveMode = findViewById(R.id.switchInclusiveMode);
        switchTextToSpeech = findViewById(R.id.switchTextToSpeech);
        switchHighContrast = findViewById(R.id.switchHighContrast);
        switchAutoRead = findViewById(R.id.switchAutoRead);

        llAccountSettings = findViewById(R.id.llAccountSettings);
        llPrivacySecurity = findViewById(R.id.llPrivacySecurity);
        llNotificationPrefs = findViewById(R.id.llNotificationPrefs);
        
        llReminderTimeView = findViewById(R.id.llReminderTime);
        llLanguageView = findViewById(R.id.llLanguage);
        llRegionView = findViewById(R.id.llRegion);
        llDateFormatView = findViewById(R.id.llDateFormat);
        llThemeView = findViewById(R.id.llTheme);
        llFontSizeView = findViewById(R.id.llFontSize);
        llClearCache = findViewById(R.id.llClearCache);
        llAutoDownloadView = findViewById(R.id.llAutoDownload);
        llSpeechRateView = findViewById(R.id.llSpeechRate);
        
        llLinkedAccounts = findViewById(R.id.llLinkedAccounts);
        llShareProgress = findViewById(R.id.llShareProgress);
        llInviteFriends = findViewById(R.id.llInviteFriends);

        llTerms = findViewById(R.id.llTerms);
        llPrivacyPolicy = findViewById(R.id.llPrivacyPolicy);
        llRateUs = findViewById(R.id.llRateUs);
        llDeleteAccount = findViewById(R.id.llDeleteAccount);
        
        TextView tvVersion = findViewById(R.id.tvVersion);
        if (tvVersion != null) {
            tvVersion.setText("2.8 Professional");
        }
    }

    private void loadSettingsToUI() {
        if (currentSettings == null) return;

        if (tvReminderTime != null) tvReminderTime.setText(formatTo12Hour(currentSettings.getReminderTime()));
        if (tvLanguage != null) tvLanguage.setText(getLanguageName(currentSettings.getLanguage()) + " ▼");
        if (tvRegion != null) tvRegion.setText(currentSettings.getRegion() + " ▼");
        if (tvDateFormat != null) tvDateFormat.setText(currentSettings.getDateFormat() + " ▼");
        if (tvTheme != null) tvTheme.setText(capitalize(currentSettings.getThemeMode()) + " ▼");
        if (tvFontSize != null) tvFontSize.setText(capitalize(currentSettings.getFontSize()) + " ▼");
        if (tvAutoDownload != null) tvAutoDownload.setText(currentSettings.getAutoDownloadMedia());

        if (switchSoundEffects != null) switchSoundEffects.setChecked(currentSettings.isSoundEffects());
        if (switchHapticFeedback != null) switchHapticFeedback.setChecked(currentSettings.isHapticFeedback());
        if (switchAnimations != null) switchAnimations.setChecked(currentSettings.isAnimatedCelebrations());
        if (switchReduceMotion != null) switchReduceMotion.setChecked(currentSettings.isReduceMotion());
        if (switchOfflineMode != null) switchOfflineMode.setChecked(currentSettings.isOfflineMode());
        if (switchCloudSync != null) switchCloudSync.setChecked(currentSettings.isCloudSync());

        if (switchInclusiveMode != null) switchInclusiveMode.setChecked(currentSettings.isInclusiveMode());
        if (switchTextToSpeech != null) switchTextToSpeech.setChecked(currentSettings.isTextToSpeech());
        if (switchHighContrast != null) switchHighContrast.setChecked(currentSettings.isHighContrastMode());
        if (switchAutoRead != null) switchAutoRead.setChecked(currentSettings.isAutoReadContent());
        if (tvSpeechRate != null) tvSpeechRate.setText(formatSpeechRate(currentSettings.getSpeechRate()) + " ▼");
    }

    private String formatSpeechRate(float rate) {
        if (rate <= 0.8f) return "0.75x Slow";
        if (rate <= 1.1f) return "1.0x Normal";
        if (rate <= 1.35f) return "1.25x Fast";
        return "1.5x Very Fast";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String getLanguageName(String code) {
        if ("am".equals(code)) return "Amharic (አማርኛ)";
        if ("om".equals(code)) return "Oromo (Afaan Oromoo)";
        if ("ti".equals(code)) return "Tigrinya (ትግርኛ)";
        return "English";
    }

    private String formatTo12Hour(String time24) {
        if (time24 == null) return "05:00 PM";
        try {
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String suffix = (hour >= 12) ? "PM" : "AM";
            int hour12 = (hour == 0) ? 12 : (hour > 12 ? hour - 12 : hour);
            return String.format(Locale.US, "%02d:%02d %s", hour12, minute, suffix);
        } catch (Exception e) {
            return time24;
        }
    }

    private void setupListeners() {
        if (btnSaveSettings != null) btnSaveSettings.setOnClickListener(v -> {
            settingsService.saveSettings(currentSettings);
            NotificationHelper.scheduleStudyReminder(this, currentSettings.getReminderTime());
            AccessibilityUtils.announceForAccessibility(v, "Settings saved successfully");
            Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
        });

        if (llAccountSettings != null) llAccountSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });
        
        if (llPrivacySecurity != null) llPrivacySecurity.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivacySecurityActivity.class);
            startActivity(intent);
        });
        
        if (llNotificationPrefs != null) llNotificationPrefs.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationPreferencesActivity.class);
            startActivity(intent);
        });

        if (llReminderTimeView != null) llReminderTimeView.setOnClickListener(v -> {
            String time = currentSettings.getReminderTime();
            if (time == null) time = "17:00";
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            new android.app.TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
                String selectedTime24 = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
                currentSettings.setReminderTime(selectedTime24);
                tvReminderTime.setText(formatTo12Hour(selectedTime24));
                settingsService.saveSettings(currentSettings);
                NotificationHelper.scheduleStudyReminder(this, selectedTime24);
            }, hour, minute, false).show();
        });

        if (switchSoundEffects != null) switchSoundEffects.setOnCheckedChangeListener((v, isChecked) -> {
            currentSettings.setSoundEffects(isChecked);
            settingsService.saveSettings(currentSettings);
        });
        
        if (switchHapticFeedback != null) switchHapticFeedback.setOnCheckedChangeListener((v, isChecked) -> {
            currentSettings.setHapticFeedback(isChecked);
            settingsService.saveSettings(currentSettings);
        });
        
        if (switchAnimations != null) switchAnimations.setOnCheckedChangeListener((v, isChecked) -> {
            currentSettings.setAnimatedCelebrations(isChecked);
            settingsService.saveSettings(currentSettings);
        });
        
        if (switchReduceMotion != null) switchReduceMotion.setOnCheckedChangeListener((v, isChecked) -> {
            currentSettings.setReduceMotion(isChecked);
            settingsService.saveSettings(currentSettings);
            InclusiveModeManager.getInstance(this).setReducedMotionEnabled(isChecked);
            AccessibilityUtils.announceForAccessibility(v, isChecked ? "Reduced motion enabled" : "Reduced motion disabled");
        });
        
        if (switchOfflineMode != null) switchOfflineMode.setOnCheckedChangeListener((v, isChecked) -> {
            currentSettings.setOfflineMode(isChecked);
            settingsService.saveSettings(currentSettings);
        });
        
        if (switchCloudSync != null) switchCloudSync.setOnCheckedChangeListener((v, isChecked) -> {
            currentSettings.setCloudSync(isChecked);
            settingsService.saveSettings(currentSettings);
        });

        // Inclusive Learning & Accessibility Listeners
        if (switchInclusiveMode != null) {
            switchInclusiveMode.setOnCheckedChangeListener((btn, isChecked) -> {
                currentSettings.setInclusiveMode(isChecked);
                InclusiveModeManager.getInstance(this).setInclusiveModeEnabled(isChecked);
                settingsService.saveSettings(currentSettings);
                AccessibilityUtils.announceForAccessibility(btn, isChecked ? "Inclusive Learning Mode enabled" : "Inclusive Learning Mode disabled");
            });
        }

        if (switchTextToSpeech != null) {
            switchTextToSpeech.setOnCheckedChangeListener((btn, isChecked) -> {
                currentSettings.setTextToSpeech(isChecked);
                InclusiveModeManager.getInstance(this).setTextToSpeechEnabled(isChecked);
                settingsService.saveSettings(currentSettings);
                AccessibilityUtils.announceForAccessibility(btn, isChecked ? "Read Content Aloud enabled" : "Read Content Aloud disabled");
            });
        }

        if (llSpeechRateView != null) {
            llSpeechRateView.setOnClickListener(v -> showSpeechRateDialog());
        }

        if (switchHighContrast != null) {
            switchHighContrast.setOnCheckedChangeListener((btn, isChecked) -> {
                currentSettings.setHighContrastMode(isChecked);
                InclusiveModeManager.getInstance(this).setHighContrastEnabled(isChecked);
                settingsService.saveSettings(currentSettings);
                AccessibilityUtils.announceForAccessibility(btn, isChecked ? "High Contrast Display enabled" : "High Contrast Display disabled");
            });
        }

        if (switchAutoRead != null) {
            switchAutoRead.setOnCheckedChangeListener((btn, isChecked) -> {
                currentSettings.setAutoReadContent(isChecked);
                InclusiveModeManager.getInstance(this).setAutoReadContentEnabled(isChecked);
                settingsService.saveSettings(currentSettings);
                AccessibilityUtils.announceForAccessibility(btn, isChecked ? "Auto-Read Lessons enabled" : "Auto-Read Lessons disabled");
            });
        }

        if (llLanguageView != null) llLanguageView.setOnClickListener(v -> {
            String[] langs = {"English", "Amharic (አማርኛ)", "Oromo (Afaan Oromoo)", "Tigrinya (ትግርኛ)"};
            String[] codes = {"en", "am", "om", "ti"};
            new MaterialAlertDialogBuilder(this).setTitle("Select App Language").setItems(langs, (dialog, which) -> {
                currentSettings.setLanguage(codes[which]);
                tvLanguage.setText(langs[which] + " ▼");
                settingsService.saveSettings(currentSettings);
                
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }).show();
        });
        
        if (llRegionView != null) llRegionView.setOnClickListener(v -> {
            String[] regions = {"United States", "Ethiopia", "United Kingdom", "Canada"};
            new MaterialAlertDialogBuilder(this).setTitle("Select Region").setItems(regions, (dialog, which) -> {
                currentSettings.setRegion(regions[which]);
                tvRegion.setText(regions[which] + " ▼");
                settingsService.saveSettings(currentSettings);
            }).show();
        });

        if (llThemeView != null) llThemeView.setOnClickListener(v -> {
            String[] options = {"Light", "Dark", "System Default"};
            new MaterialAlertDialogBuilder(this).setTitle("Select Theme").setItems(options, (dialog, which) -> {
                String choice = which == 0 ? ThemeManager.THEME_LIGHT : (which == 1 ? ThemeManager.THEME_DARK : ThemeManager.THEME_SYSTEM);
                currentSettings.setThemeMode(choice);
                if (tvTheme != null) tvTheme.setText(options[which] + " ▼");
                ThemeManager.setThemeMode(this, choice);
            }).show();
        });
        
        if (llFontSizeView != null) llFontSizeView.setOnClickListener(v -> {
            String[] sizes = {"Small", "Medium", "Large", "Extra Large"};
            new MaterialAlertDialogBuilder(this).setTitle("Font Size").setItems(sizes, (dialog, which) -> {
                String choice = sizes[which].toLowerCase();
                currentSettings.setFontSize(choice);
                tvFontSize.setText(sizes[which] + " ▼");
                boolean isLarge = "large".equals(choice) || "extra large".equals(choice);
                InclusiveModeManager.getInstance(this).setLargeTextEnabled(isLarge);
                settingsService.saveSettings(currentSettings);
            }).show();
        });

        if (llDateFormatView != null) llDateFormatView.setOnClickListener(v -> {
            String[] formats = {"MM/DD/YYYY", "DD/MM/YYYY", "YYYY-MM-DD"};
            new MaterialAlertDialogBuilder(this).setTitle("Select Date Format").setItems(formats, (dialog, which) -> {
                currentSettings.setDateFormat(formats[which]);
                if (tvDateFormat != null) tvDateFormat.setText(formats[which] + " ▼");
                settingsService.saveSettings(currentSettings);
            }).show();
        });

        if (llAutoDownloadView != null) llAutoDownloadView.setOnClickListener(v -> {
            String[] options = {"Wi-Fi only", "Wi-Fi and Cellular", "Never"};
            new MaterialAlertDialogBuilder(this).setTitle("Auto-Download Media").setItems(options, (dialog, which) -> {
                currentSettings.setAutoDownloadMedia(options[which]);
                if (tvAutoDownload != null) tvAutoDownload.setText(options[which]);
                settingsService.saveSettings(currentSettings);
            }).show();
        });

        if (llClearCache != null) llClearCache.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Clear Cache")
                .setMessage("This will free up storage space. Offline content will be preserved.")
                .setPositiveButton("Clear", (d, w) -> {
                    clearAppCache();
                    updateCacheSizeDisplay();
                    Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        if (llLinkedAccounts != null) llLinkedAccounts.setOnClickListener(v -> {
            Intent intent = new Intent(this, LinkedAccountsActivity.class);
            startActivity(intent);
        });
        if (llShareProgress != null) llShareProgress.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "I'm learning on Learnify! Check out my progress.");
            startActivity(Intent.createChooser(intent, "Share via"));
        });
        if (llInviteFriends != null) llInviteFriends.setOnClickListener(v -> {
            Intent intent = new Intent(this, InviteFriendsActivity.class);
            startActivity(intent);
        });

        if (llTerms != null) llTerms.setOnClickListener(v -> showModalWebView("Terms of Service", "https://learnify.com/terms"));
        if (llPrivacyPolicy != null) llPrivacyPolicy.setOnClickListener(v -> showModalWebView("Privacy Policy", "https://learnify.com/privacy"));
        if (llRateUs != null) llRateUs.setOnClickListener(v -> Toast.makeText(this, "Thank you for your rating!", Toast.LENGTH_SHORT).show());
        
        if (llDeleteAccount != null) llDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showSpeechRateDialog() {
        String[] options = {"0.75x (Slow)", "1.0x (Normal)", "1.25x (Fast)", "1.5x (Very Fast)"};
        final float[] rates = {InclusiveModeManager.RATE_SLOW, InclusiveModeManager.RATE_NORMAL, InclusiveModeManager.RATE_FAST, InclusiveModeManager.RATE_VERY_FAST};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Speech Rate")
                .setItems(options, (dialog, which) -> {
                    float selectedRate = rates[which];
                    currentSettings.setSpeechRate(selectedRate);
                    InclusiveModeManager.getInstance(this).setSpeechRate(selectedRate);
                    settingsService.saveSettings(currentSettings);
                    if (tvSpeechRate != null) {
                        tvSpeechRate.setText(formatSpeechRate(selectedRate) + " ▼");
                    }
                    AccessibilityUtils.announceForAccessibility(tvSpeechRate, "Speech rate set to " + options[which]);
                })
                .show();
    }

    private String getAuthenticatedUser() {
        android.content.SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        String user = session.getString("username", "");
        if ((user == null || user.trim().isEmpty()) && securePrefs != null) {
            user = securePrefs.getString("username", "");
        }
        return user != null ? user.trim() : "";
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

    private void updateCacheSizeDisplay() {
        if (tvCacheSize == null) return;
        new Thread(() -> {
            long size = getDirSize(getCacheDir());
            runOnUiThread(() -> tvCacheSize.setText((size / (1024 * 1024)) + " MB"));
        }).start();
    }

    private long getDirSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) size += getDirSize(file);
                else size += file.length();
            }
        }
        return size;
    }

    private void clearAppCache() {
        try {
            File dir = getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            Log.e(TAG, "Clear cache failed", e);
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    private void showModalWebView(String title, String url) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(title);
        WebView wv = new WebView(this);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl(url);
        wv.setWebViewClient(new WebViewClient());
        builder.setView(wv);
        builder.setPositiveButton("Close", null);
        builder.show();
    }
}
