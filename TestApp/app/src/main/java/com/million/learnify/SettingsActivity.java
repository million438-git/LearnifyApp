package com.million.learnify;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.ArrayList;
import java.util.List;
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
    }

    private void loadSettingsToUI() {
        tvReminderTime.setText("⏰ " + formatTo12Hour(currentSettings.getReminderTime()));
        tvLanguage.setText(getLanguageName(currentSettings.getLanguage()) + " ▼");
        tvRegion.setText(currentSettings.getRegion() + " ▼");
        tvDateFormat.setText(currentSettings.getDateFormat() + " ▼");
        tvTheme.setText(capitalize(currentSettings.getThemeMode()) + " ▼");
        tvFontSize.setText(capitalize(currentSettings.getFontSize()) + " ▼");
        tvAutoDownload.setText("🔘 " + currentSettings.getAutoDownloadMedia());

        switchSoundEffects.setChecked(currentSettings.isSoundEffects());
        switchHapticFeedback.setChecked(currentSettings.isHapticFeedback());
        switchAnimations.setChecked(currentSettings.isAnimatedCelebrations());
        switchReduceMotion.setChecked(currentSettings.isReduceMotion());
        switchOfflineMode.setChecked(currentSettings.isOfflineMode());
        switchCloudSync.setChecked(currentSettings.isCloudSync());
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
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        });

        llAccountSettings.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        llPrivacySecurity.setOnClickListener(v -> startActivity(new Intent(this, PrivacySecurityActivity.class)));
        llNotificationPrefs.setOnClickListener(v -> startActivity(new Intent(this, NotificationPreferencesActivity.class)));

        llReminderTimeView.setOnClickListener(v -> {
            String[] timeParts = currentSettings.getReminderTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            new android.app.TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
                String selectedTime24 = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
                currentSettings.setReminderTime(selectedTime24);
                tvReminderTime.setText("⏰ " + formatTo12Hour(selectedTime24));
                settingsService.saveSettings(currentSettings);
            }, hour, minute, false).show();
        });

        switchSoundEffects.setOnCheckedChangeListener((v, isChecked) -> currentSettings.setSoundEffects(isChecked));
        switchHapticFeedback.setOnCheckedChangeListener((v, isChecked) -> currentSettings.setHapticFeedback(isChecked));
        switchAnimations.setOnCheckedChangeListener((v, isChecked) -> currentSettings.setAnimatedCelebrations(isChecked));

        llLanguageView.setOnClickListener(v -> {
            String[] langs = {"English", "Amharic"};
            new MaterialAlertDialogBuilder(this).setTitle("Language").setItems(langs, (dialog, which) -> {
                currentSettings.setLanguage(which == 0 ? "en" : "am");
                tvLanguage.setText(langs[which] + " ▼");
                settingsService.saveSettings(currentSettings);
                recreate();
            }).show();
        });

        llThemeView.setOnClickListener(v -> {
            String[] options = {"Light", "Dark", "System"};
            new MaterialAlertDialogBuilder(this).setTitle("Theme").setItems(options, (dialog, which) -> {
                String choice = options[which].toLowerCase();
                currentSettings.setThemeMode(choice);
                tvTheme.setText(options[which] + " ▼");
                settingsService.saveSettings(currentSettings);
                AppCompatDelegate.setDefaultNightMode(choice.equals("dark") ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }).show();
        });

        llClearCache.setOnClickListener(v -> {
            clearAppCache();
            updateCacheSizeDisplay();
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
        });

        llTerms.setOnClickListener(v -> showModalWebView("Terms", "https://learnify.com/terms"));
        llPrivacyPolicy.setOnClickListener(v -> showModalWebView("Privacy", "https://learnify.com/privacy"));
        llDeleteAccount.setOnClickListener(v -> {
             new MaterialAlertDialogBuilder(this).setTitle("Delete Account").setMessage("Are you sure? This is permanent.")
                     .setPositiveButton("Delete", (dialog, which) -> Toast.makeText(this, "Account delete requested", Toast.LENGTH_SHORT).show())
                     .setNegativeButton("Cancel", null).show();
        });
    }

    private void updateCacheSizeDisplay() {
        long size = getDirSize(getCacheDir());
        tvCacheSize.setText((size / (1024 * 1024)) + " MB");
    }

    private long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) size += getDirSize(file);
            else if (file != null && file.isFile()) size += file.length();
        }
        return size;
    }

    private void clearAppCache() {
        try {
            deleteDir(getCacheDir());
        } catch (Exception e) {}
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) return false;
            }
        }
        return dir.delete();
    }

    private void showModalWebView(String title, String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        WebView wv = new WebView(this);
        wv.loadUrl(url);
        wv.setWebViewClient(new WebViewClient());
        builder.setView(wv);
        builder.setPositiveButton("Close", null);
        builder.show();
    }
}
