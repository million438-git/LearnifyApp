package com.million.learnify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import java.io.File;
import java.util.Locale;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private SettingsService settingsService;
    private StudentSettings currentSettings;
    
    private ImageView btnSaveSettings;
    private TextView tvReminderTime, tvLanguage, tvRegion, tvDateFormat, tvTheme, tvFontSize, tvCacheSize, tvAutoDownload;
    private MaterialSwitch switchSoundEffects, switchHapticFeedback, switchAnimations, switchReduceMotion, switchOfflineMode, switchCloudSync;
    
    private LinearLayout llAccountSettings, llPrivacySecurity, llNotificationPrefs;
    private LinearLayout llReminderTimeView, llLanguageView, llRegionView, llDateFormatView;
    private LinearLayout llThemeView, llFontSizeView, llClearCache, llAutoDownloadView;
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

        switchSoundEffects = findViewById(R.id.switchSoundEffects);
        switchHapticFeedback = findViewById(R.id.switchHapticFeedback);
        switchAnimations = findViewById(R.id.switchAnimations);
        switchReduceMotion = findViewById(R.id.switchReduceMotion);
        switchOfflineMode = findViewById(R.id.switchOfflineMode);
        switchCloudSync = findViewById(R.id.switchCloudSync);

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

        if (tvReminderTime != null) tvReminderTime.setText("⏰ " + formatTo12Hour(currentSettings.getReminderTime()));
        if (tvLanguage != null) tvLanguage.setText(getLanguageName(currentSettings.getLanguage()) + " ▼");
        if (tvRegion != null) tvRegion.setText(currentSettings.getRegion() + " ▼");
        if (tvDateFormat != null) tvDateFormat.setText(currentSettings.getDateFormat() + " ▼");
        if (tvTheme != null) tvTheme.setText(capitalize(currentSettings.getThemeMode()) + " ▼");
        if (tvFontSize != null) tvFontSize.setText(capitalize(currentSettings.getFontSize()) + " ▼");
        if (tvAutoDownload != null) tvAutoDownload.setText("🔘 " + currentSettings.getAutoDownloadMedia());

        if (switchSoundEffects != null) switchSoundEffects.setChecked(currentSettings.isSoundEffects());
        if (switchHapticFeedback != null) switchHapticFeedback.setChecked(currentSettings.isHapticFeedback());
        if (switchAnimations != null) switchAnimations.setChecked(currentSettings.isAnimatedCelebrations());
        if (switchReduceMotion != null) switchReduceMotion.setChecked(currentSettings.isReduceMotion());
        if (switchOfflineMode != null) switchOfflineMode.setChecked(currentSettings.isOfflineMode());
        if (switchCloudSync != null) switchCloudSync.setChecked(currentSettings.isCloudSync());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String getLanguageName(String code) {
        if ("am".equals(code)) return "Amharic";
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
                tvReminderTime.setText("⏰ " + formatTo12Hour(selectedTime24));
                settingsService.saveSettings(currentSettings);
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
        });
        
        if (switchOfflineMode != null) switchOfflineMode.setOnCheckedChangeListener((v, isChecked) -> {
            currentSettings.setOfflineMode(isChecked);
            settingsService.saveSettings(currentSettings);
        });
        
        if (switchCloudSync != null) switchCloudSync.setOnCheckedChangeListener((v, isChecked) -> {
            currentSettings.setCloudSync(isChecked);
            settingsService.saveSettings(currentSettings);
        });

        if (llLanguageView != null) llLanguageView.setOnClickListener(v -> {
            String[] langs = {"English", "Amharic (አማርኛ)"};
            new MaterialAlertDialogBuilder(this).setTitle("Select App Language").setItems(langs, (dialog, which) -> {
                currentSettings.setLanguage(which == 0 ? "en" : "am");
                tvLanguage.setText(langs[which] + " ▼");
                settingsService.saveSettings(currentSettings);
                Toast.makeText(this, "Language updated. Restart app to apply fully.", Toast.LENGTH_SHORT).show();
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
                String choice = which == 0 ? "light" : (which == 1 ? "dark" : "system");
                currentSettings.setThemeMode(choice);
                tvTheme.setText(options[which] + " ▼");
                settingsService.saveSettings(currentSettings);
                
                int mode = which == 0 ? AppCompatDelegate.MODE_NIGHT_NO : 
                          (which == 1 ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                AppCompatDelegate.setDefaultNightMode(mode);
            }).show();
        });
        
        if (llFontSizeView != null) llFontSizeView.setOnClickListener(v -> {
            String[] sizes = {"Small", "Medium", "Large", "Extra Large"};
            new MaterialAlertDialogBuilder(this).setTitle("Font Size").setItems(sizes, (dialog, which) -> {
                String choice = sizes[which].toLowerCase();
                currentSettings.setFontSize(choice);
                tvFontSize.setText(sizes[which] + " ▼");
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

        if (llLinkedAccounts != null) llLinkedAccounts.setOnClickListener(v -> Toast.makeText(this, "Opening Linked Accounts...", Toast.LENGTH_SHORT).show());
        if (llShareProgress != null) llShareProgress.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "I'm learning on Learnify! Check out my progress.");
            startActivity(Intent.createChooser(intent, "Share via"));
        });
        if (llInviteFriends != null) llInviteFriends.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Join me on Learnify and let's study together! Download now: https://learnify.com/download");
            startActivity(Intent.createChooser(intent, "Invite via"));
        });

        if (llTerms != null) llTerms.setOnClickListener(v -> showModalWebView("Terms of Service", "https://learnify.com/terms"));
        if (llPrivacyPolicy != null) llPrivacyPolicy.setOnClickListener(v -> showModalWebView("Privacy Policy", "https://learnify.com/privacy"));
        if (llRateUs != null) llRateUs.setOnClickListener(v -> Toast.makeText(this, "Thank you for your rating!", Toast.LENGTH_SHORT).show());
        
        if (llDeleteAccount != null) llDeleteAccount.setOnClickListener(v -> {
             new MaterialAlertDialogBuilder(this)
                 .setTitle("Delete Account")
                 .setMessage("Warning: This action is permanent and cannot be undone. All your progress will be lost.")
                 .setPositiveButton("Delete Permanently", (dialog, which) -> Toast.makeText(this, "Account deletion request submitted", Toast.LENGTH_LONG).show())
                 .setNegativeButton("Keep My Account", null)
                 .show();
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
