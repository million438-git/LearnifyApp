package com.example.testapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private SettingsService settingsService;
    private StudentSettings currentSettings;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    // Appearance
    private SwitchCompat switchDarkMode;
    private TextView tvFontSize;

    // Notifications & Reminders
    private SwitchCompat switchStudyReminder, switchHomeworkAlerts, switchExamNotifications, switchAIStudyTips;
    private TextView tvReminderTime;

    // Language & Accessibility
    private TextView tvLanguage;
    private SwitchCompat switchTTS, switchHighContrast;

    // Offline & Data Usage
    private SwitchCompat switchOffline, switchDataSaver;
    private TextView tvVideoQuality;

    // AI Assistant
    private TextView tvAIHelpLevel, tvAILearningStyle;

    // Privacy & Safety
    private SwitchCompat switchProfileVisibility, switchShareProgress;
    private TextView tvClearHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);
        settingsService = new SettingsService(this);
        currentSettings = settingsService.loadSettings();

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        setupToolbar();
        initViews();
        loadSettingsToUI();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        // Appearance
        switchDarkMode = findViewById(R.id.switchDarkMode);
        tvFontSize = findViewById(R.id.tvFontSize);

        // Notifications & Reminders
        switchStudyReminder = findViewById(R.id.switchStudyReminder);
        switchHomeworkAlerts = findViewById(R.id.switchHomeworkAlerts);
        switchExamNotifications = findViewById(R.id.switchExamNotifications);
        switchAIStudyTips = findViewById(R.id.switchAIStudyTips);
        tvReminderTime = findViewById(R.id.tvReminderTime);

        // Language & Accessibility
        tvLanguage = findViewById(R.id.tvLanguage);
        switchTTS = findViewById(R.id.switchTTS);
        switchHighContrast = findViewById(R.id.switchHighContrast);

        // Offline & Data Usage
        switchOffline = findViewById(R.id.switchOffline);
        switchDataSaver = findViewById(R.id.switchDataSaver);
        tvVideoQuality = findViewById(R.id.tvVideoQuality);

        // AI Assistant
        tvAIHelpLevel = findViewById(R.id.tvAIHelpLevel);
        tvAILearningStyle = findViewById(R.id.tvAILearningStyle);

        // Privacy & Safety
        switchProfileVisibility = findViewById(R.id.switchProfileVisibility);
        switchShareProgress = findViewById(R.id.switchShareProgress);
        
        // Use general findView for the clickable layout items
        tvClearHistory = findViewById(R.id.tvClearHistory);
    }

    private void loadSettingsToUI() {
        // Appearance
        if (switchDarkMode != null) switchDarkMode.setChecked("dark".equals(currentSettings.getThemeMode()));
        if (tvFontSize != null) tvFontSize.setText(currentSettings.getFontSize());

        // Notifications
        if (switchStudyReminder != null) switchStudyReminder.setChecked(currentSettings.isStudyReminder());
        if (switchHomeworkAlerts != null) switchHomeworkAlerts.setChecked(currentSettings.isHomeworkAlerts());
        if (switchExamNotifications != null) switchExamNotifications.setChecked(currentSettings.isExamNotifications());
        if (switchAIStudyTips != null) switchAIStudyTips.setChecked(currentSettings.isAiStudyTips());
        if (tvReminderTime != null) tvReminderTime.setText(currentSettings.getReminderTime());

        // Language & Accessibility
        if (tvLanguage != null) tvLanguage.setText(currentSettings.getLanguage().equals("en") ? "English" : "Local Language");
        if (switchTTS != null) switchTTS.setChecked(currentSettings.isTextToSpeech());
        if (switchHighContrast != null) switchHighContrast.setChecked(currentSettings.isHighContrastMode());

        // Offline
        if (switchOffline != null) switchOffline.setChecked(currentSettings.isOfflineMode());
        if (switchDataSaver != null) switchDataSaver.setChecked(currentSettings.isDataSaver());
        if (tvVideoQuality != null) tvVideoQuality.setText(currentSettings.getVideoQuality());

        // AI
        if (tvAIHelpLevel != null) tvAIHelpLevel.setText(currentSettings.getAiHelpLevel());
        if (tvAILearningStyle != null) tvAILearningStyle.setText(currentSettings.getAiLearningStyle());

        // Privacy & Safety
        if (switchProfileVisibility != null) switchProfileVisibility.setChecked(currentSettings.isProfileVisibility());
        if (switchShareProgress != null) switchShareProgress.setChecked(currentSettings.isShareProgress());
    }

    private void setupListeners() {
        // Dark Mode
        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String newMode = isChecked ? "dark" : "light";
                currentSettings.setThemeMode(newMode);
                settingsService.saveSettings(currentSettings);
                AppCompatDelegate.setDefaultNightMode(isChecked ? 
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            });
        }

        // Reminder Time Picker
        if (findViewById(R.id.llReminderTime) != null) {
            findViewById(R.id.llReminderTime).setOnClickListener(v -> {
                String[] timeParts = currentSettings.getReminderTime().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
                    String selectedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    if (tvReminderTime != null) tvReminderTime.setText(selectedTime);
                    currentSettings.setReminderTime(selectedTime);
                    settingsService.saveSettings(currentSettings);
                }, hour, minute, true);
                timePickerDialog.show();
            });
        }

        // Change Password Logic
        if (findViewById(R.id.llChangePassword) != null) {
            findViewById(R.id.llChangePassword).setOnClickListener(v -> showChangePasswordDialog());
        }

        // Sign Out
        if (findViewById(R.id.llSignOut) != null) {
            findViewById(R.id.llSignOut).setOnClickListener(v -> {
                getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Clear History
        if (tvClearHistory != null) {
            tvClearHistory.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Clear Learning History?")
                        .setMessage("This will permanently delete your progress logs.")
                        .setPositiveButton("Clear Everything", (dialog, which) -> {
                            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        // Help & Support
        if (findViewById(R.id.llHelpSupport) != null) {
            findViewById(R.id.llHelpSupport).setOnClickListener(v -> 
                startActivity(new Intent(this, HelpSupportActivity.class)));
        }
        
        // About
        if (findViewById(R.id.llAboutApp) != null) {
            findViewById(R.id.llAboutApp).setOnClickListener(v -> 
                startActivity(new Intent(this, AboutActivity.class)));
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etNewPass = new EditText(this);
        etNewPass.setHint("Enter New Password");
        etNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPass);

        final EditText etConfirmPass = new EditText(this);
        etConfirmPass.setHint("Confirm New Password");
        etConfirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirmPass);

        builder.setView(layout);

        builder.setPositiveButton("UPDATE", (dialog, which) -> {
            String newPass = etNewPass.getText().toString().trim();
            String confirmPass = etConfirmPass.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.updatePassword(currentUsername, newPass);
                if (success) {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }
}
