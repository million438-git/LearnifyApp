package com.million.learnify;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.File;
import java.util.Locale;

/**
 * SettingsActivity - Production-Grade Student Settings & Personalization Platform.
 * Consolidated 9-Section Architecture:
 * 1. Account and Profile
 * 2. Appearance
 * 3. Learning Preferences & Regional Alignment
 * 4. Accessibility and Voice
 * 5. Notifications and Reminders
 * 6. Offline Learning and Storage
 * 7. AI Tutor Preferences
 * 8. Privacy and Security
 * 9. Help and About Learnify
 */
public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    private SettingsService settingsService;
    private StudentSettings currentSettings;
    private DatabaseHelper dbHelper;
    private OfflineStorageManager storageManager;

    private String currentUsername = "Student";

    // Top action
    private MaterialButton btnSaveSettings;

    // Preference Indicators
    private TextView tvTheme, tvFontSize;
    private TextView tvDailyGoal, tvDailyStudyDuration, tvWeeklyLessonGoal, tvPracticeDifficulty, tvExplanationDepth, tvLessonPresentation;
    private TextView tvLanguage, tvRegion, tvDateFormat;
    private TextView tvSpeechRate, tvSpeechPitch;
    private TextView tvReminderTime;
    private TextView tvAvailableStorage, tvDownloadedMaterialsSize, tvCacheSize, tvSyncStatus, tvSyncDetails, tvAutoDownload;
    private TextView tvAiTutorLanguage, tvAiExplanationLength, tvAiHelpLevel, tvAiLearningStyle;
    private TextView tvProfileVisibility;
    private TextView tvRateUs, tvVersion;

    // Switches
    private MaterialSwitch switchReduceMotion;
    private MaterialSwitch switchPersonalizedRecommendations, switchSoundEffects, switchHapticFeedback, switchAnimations;
    private MaterialSwitch switchInclusiveMode, switchTextToSpeech, switchHighContrast, switchAutoRead;
    private MaterialSwitch switchPushNotifications;
    private MaterialSwitch switchOfflineMode, switchDataSaver;
    private MaterialSwitch switchAiVoiceResponse;
    private MaterialSwitch switchShareProgress, switchCloudSync;

    // Clickable Row Containers
    private MaterialCardView cardProfileSummary;
    private ImageView ivProfileAvatar;
    private TextView tvProfileName, tvProfileUsername, tvProfileBadge;
    private LinearLayout llAccountSettings, llPrivacySecurity;
    private LinearLayout llThemeView, llFontSizeView;
    private LinearLayout llDailyGoalView, llDailyStudyDurationView, llWeeklyLessonGoalView, llPracticeDifficultyView, llExplanationDepthView, llLessonPresentationView;
    private LinearLayout llLanguageView, llRegionView, llDateFormatView;
    private LinearLayout llSpeechRateView, llSpeechPitchView;
    private LinearLayout llReminderTimeView, llNotificationPrefs;
    private LinearLayout llDownloadedMaterials, llClearCache, llDeleteDownloads, llSyncStatus, llAutoDownloadView;
    private LinearLayout llAiTutorLanguageView, llAiExplanationLengthView, llAiHelpLevelView, llAiLearningStyleView, llClearChatHistory;
    private LinearLayout llProfileVisibilityView, llDataExportDelete;
    private LinearLayout llHelpSupport, llRateUs, llTerms, llPrivacyPolicy, llSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            settingsService = SettingsService.getInstance(this);
            currentSettings = settingsService.loadSettings();
            if (currentSettings == null) {
                currentSettings = new StudentSettings("light", true);
            }
            dbHelper = new DatabaseHelper(this);
            storageManager = OfflineStorageManager.getInstance();

            SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
            currentUsername = session.getString("username", "Student");

            setContentView(R.layout.activity_settings);

            setupToolbar();
            initViews();
            loadProfileSummary();
            loadSettingsToUI();
            setupListeners();

            updateStorageDisplays();
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
        Toolbar toolbar = findViewById(R.id.settingsToolbar);
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

        // Section 1: Profile Summary
        cardProfileSummary = findViewById(R.id.cardProfileSummary);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileBadge = findViewById(R.id.tvProfileBadge);
        llAccountSettings = findViewById(R.id.llAccountSettings);
        llPrivacySecurity = findViewById(R.id.llPrivacySecurity);

        // Section 2: Appearance
        llThemeView = findViewById(R.id.llTheme);
        tvTheme = findViewById(R.id.tvTheme);
        llFontSizeView = findViewById(R.id.llFontSize);
        tvFontSize = findViewById(R.id.tvFontSize);
        switchReduceMotion = findViewById(R.id.switchReduceMotion);

        // Section 3: Learning Preferences
        llDailyGoalView = findViewById(R.id.llDailyGoal);
        tvDailyGoal = findViewById(R.id.tvDailyGoal);
        llDailyStudyDurationView = findViewById(R.id.llDailyStudyDuration);
        tvDailyStudyDuration = findViewById(R.id.tvDailyStudyDuration);
        llWeeklyLessonGoalView = findViewById(R.id.llWeeklyLessonGoal);
        tvWeeklyLessonGoal = findViewById(R.id.tvWeeklyLessonGoal);
        llPracticeDifficultyView = findViewById(R.id.llPracticeDifficulty);
        tvPracticeDifficulty = findViewById(R.id.tvPracticeDifficulty);
        llExplanationDepthView = findViewById(R.id.llExplanationDepth);
        tvExplanationDepth = findViewById(R.id.tvExplanationDepth);
        llLessonPresentationView = findViewById(R.id.llLessonPresentation);
        tvLessonPresentation = findViewById(R.id.tvLessonPresentation);
        switchPersonalizedRecommendations = findViewById(R.id.switchPersonalizedRecommendations);
        switchSoundEffects = findViewById(R.id.switchSoundEffects);
        switchHapticFeedback = findViewById(R.id.switchHapticFeedback);
        switchAnimations = findViewById(R.id.switchAnimations);

        // Section 4: Language & Region
        llLanguageView = findViewById(R.id.llLanguage);
        tvLanguage = findViewById(R.id.tvLanguage);
        llRegionView = findViewById(R.id.llRegion);
        tvRegion = findViewById(R.id.tvRegion);
        llDateFormatView = findViewById(R.id.llDateFormat);
        tvDateFormat = findViewById(R.id.tvDateFormat);

        // Section 5: Accessibility & Voice
        switchInclusiveMode = findViewById(R.id.switchInclusiveMode);
        switchTextToSpeech = findViewById(R.id.switchTextToSpeech);
        llSpeechRateView = findViewById(R.id.llSpeechRate);
        tvSpeechRate = findViewById(R.id.tvSpeechRate);
        llSpeechPitchView = findViewById(R.id.llSpeechPitch);
        tvSpeechPitch = findViewById(R.id.tvSpeechPitch);
        switchHighContrast = findViewById(R.id.switchHighContrast);
        switchAutoRead = findViewById(R.id.switchAutoRead);

        // Section 6: Notifications & Reminders
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        llReminderTimeView = findViewById(R.id.llReminderTime);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        llNotificationPrefs = findViewById(R.id.llNotificationPrefs);

        // Section 7: Offline Learning & Storage
        tvAvailableStorage = findViewById(R.id.tvAvailableStorage);
        llDownloadedMaterials = findViewById(R.id.llDownloadedMaterials);
        tvDownloadedMaterialsSize = findViewById(R.id.tvDownloadedMaterialsSize);
        llClearCache = findViewById(R.id.llClearCache);
        tvCacheSize = findViewById(R.id.tvCacheSize);
        llDeleteDownloads = findViewById(R.id.llDeleteDownloads);
        llSyncStatus = findViewById(R.id.llSyncStatus);
        tvSyncStatus = findViewById(R.id.tvSyncStatus);
        tvSyncDetails = findViewById(R.id.tvSyncDetails);
        switchOfflineMode = findViewById(R.id.switchOfflineMode);
        llAutoDownloadView = findViewById(R.id.llAutoDownload);
        tvAutoDownload = findViewById(R.id.tvAutoDownload);
        switchDataSaver = findViewById(R.id.switchDataSaver);

        // Section 8: AI Tutor Preferences
        llAiTutorLanguageView = findViewById(R.id.llAiTutorLanguage);
        tvAiTutorLanguage = findViewById(R.id.tvAiTutorLanguage);
        switchAiVoiceResponse = findViewById(R.id.switchAiVoiceResponse);
        llAiExplanationLengthView = findViewById(R.id.llAiExplanationLength);
        tvAiExplanationLength = findViewById(R.id.tvAiExplanationLength);
        llAiHelpLevelView = findViewById(R.id.llAiHelpLevel);
        tvAiHelpLevel = findViewById(R.id.tvAiHelpLevel);
        llAiLearningStyleView = findViewById(R.id.llAiLearningStyle);
        tvAiLearningStyle = findViewById(R.id.tvAiLearningStyle);
        llClearChatHistory = findViewById(R.id.llClearChatHistory);

        // Section 9: Privacy & Security
        llProfileVisibilityView = findViewById(R.id.llProfileVisibility);
        tvProfileVisibility = findViewById(R.id.tvProfileVisibility);
        switchShareProgress = findViewById(R.id.switchShareProgress);
        switchCloudSync = findViewById(R.id.switchCloudSync);
        llDataExportDelete = findViewById(R.id.llDataExportDelete);

        // Section 10: Help and About Learnify
        llHelpSupport = findViewById(R.id.llHelpSupport);
        llRateUs = findViewById(R.id.llRateUs);
        tvRateUs = findViewById(R.id.tvRateUs);
        llTerms = findViewById(R.id.llTerms);
        llPrivacyPolicy = findViewById(R.id.llPrivacyPolicy);
        tvVersion = findViewById(R.id.tvVersion);
        llSignOut = findViewById(R.id.llSignOut);

        if (tvVersion != null) {
            tvVersion.setText(BuildConfig.VERSION_NAME + " • Android Native");
        }
    }

    private void loadProfileSummary() {
        try {
            Cursor cursor = dbHelper.getUserDetails(currentUsername);
            if (cursor != null && cursor.moveToFirst()) {
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade_or_role"));
                String imgUriStr = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));
                int xpGoal = cursor.getInt(cursor.getColumnIndexOrThrow("xp_goal"));

                if (tvProfileName != null && fullName != null && !fullName.isEmpty()) {
                    tvProfileName.setText(fullName);
                }
                if (tvProfileUsername != null) {
                    tvProfileUsername.setText("@" + currentUsername);
                }
                if (tvProfileBadge != null && grade != null && !grade.isEmpty()) {
                    tvProfileBadge.setText(grade.toUpperCase(Locale.US) + " • ACTIVE");
                }
                if (tvDailyGoal != null && xpGoal > 0) {
                    tvDailyGoal.setText(xpGoal + " XP ▼");
                }
                if (ivProfileAvatar != null && imgUriStr != null && !imgUriStr.isEmpty()) {
                    try {
                        ivProfileAvatar.setImageURI(Uri.parse(imgUriStr));
                    } catch (Exception ignored) {}
                }
                cursor.close();
            } else {
                if (tvProfileUsername != null) tvProfileUsername.setText("@" + currentUsername);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile summary", e);
        }
    }

    private void loadSettingsToUI() {
        if (currentSettings == null) return;

        // Section 2: Appearance
        if (tvTheme != null) tvTheme.setText(capitalize(currentSettings.getThemeMode()) + " ▼");
        if (tvFontSize != null) tvFontSize.setText(capitalize(currentSettings.getFontSize()) + " ▼");
        if (switchReduceMotion != null) switchReduceMotion.setChecked(currentSettings.isReduceMotion());

        // Section 3: Learning Preferences
        if (tvDailyStudyDuration != null) tvDailyStudyDuration.setText(currentSettings.getDailyStudyMinutes() + " Mins ▼");
        if (tvWeeklyLessonGoal != null) tvWeeklyLessonGoal.setText(currentSettings.getWeeklyLessonGoal() + " Lessons ▼");
        if (tvPracticeDifficulty != null) tvPracticeDifficulty.setText(currentSettings.getPracticeDifficulty() + " ▼");
        if (tvExplanationDepth != null) tvExplanationDepth.setText(currentSettings.getExplanationDepth() + " ▼");
        if (tvLessonPresentation != null) tvLessonPresentation.setText(currentSettings.getPreferredLessonPresentation() + " ▼");
        if (switchPersonalizedRecommendations != null) switchPersonalizedRecommendations.setChecked(currentSettings.isPersonalizedRecommendations());
        if (switchSoundEffects != null) switchSoundEffects.setChecked(currentSettings.isSoundEffects());
        if (switchHapticFeedback != null) switchHapticFeedback.setChecked(currentSettings.isHapticFeedback());
        if (switchAnimations != null) switchAnimations.setChecked(currentSettings.isAnimatedCelebrations());

        // Section 4: Language & Region
        if (tvLanguage != null) tvLanguage.setText(getLanguageName(currentSettings.getLanguage()) + " ▼");
        if (tvRegion != null) tvRegion.setText(currentSettings.getRegion() + " ▼");
        if (tvDateFormat != null) tvDateFormat.setText(currentSettings.getDateFormat() + " ▼");

        // Section 5: Accessibility & Voice
        if (switchInclusiveMode != null) switchInclusiveMode.setChecked(currentSettings.isInclusiveMode());
        if (switchTextToSpeech != null) switchTextToSpeech.setChecked(currentSettings.isTextToSpeech());
        if (tvSpeechRate != null) tvSpeechRate.setText(formatSpeechRate(currentSettings.getSpeechRate()) + " ▼");
        if (tvSpeechPitch != null) tvSpeechPitch.setText(formatSpeechPitch(currentSettings.getSpeechPitch()) + " ▼");
        if (switchHighContrast != null) switchHighContrast.setChecked(currentSettings.isHighContrastMode());
        if (switchAutoRead != null) switchAutoRead.setChecked(currentSettings.isAutoReadContent());

        // Section 6: Notifications & Reminders
        if (switchPushNotifications != null) switchPushNotifications.setChecked(currentSettings.isPushEnabled());
        if (tvReminderTime != null) tvReminderTime.setText(formatTo12Hour(currentSettings.getReminderTime()));

        // Section 7: Offline Learning & Storage
        if (switchOfflineMode != null) switchOfflineMode.setChecked(currentSettings.isOfflineMode());
        if (tvAutoDownload != null) tvAutoDownload.setText(currentSettings.getAutoDownloadMedia() + " ▼");
        if (switchDataSaver != null) switchDataSaver.setChecked(currentSettings.isDataSaver());

        // Section 8: AI Tutor Preferences
        if (tvAiTutorLanguage != null) tvAiTutorLanguage.setText(getAiTutorLanguageName(currentSettings.getAiTutorLanguage()) + " ▼");
        if (switchAiVoiceResponse != null) switchAiVoiceResponse.setChecked(currentSettings.isAiVoiceResponse());
        if (tvAiExplanationLength != null) tvAiExplanationLength.setText(currentSettings.getAiExplanationLength() + " ▼");
        if (tvAiHelpLevel != null) tvAiHelpLevel.setText(currentSettings.getAiHelpLevel() + " ▼");
        if (tvAiLearningStyle != null) tvAiLearningStyle.setText(currentSettings.getAiLearningStyle() + " ▼");

        // Section 9: Privacy & Security
        if (tvProfileVisibility != null) tvProfileVisibility.setText((currentSettings.isProfileVisibility() ? "Public" : "Private") + " ▼");
        if (switchShareProgress != null) switchShareProgress.setChecked(currentSettings.isShareProgress());
        if (switchCloudSync != null) switchCloudSync.setChecked(currentSettings.isCloudSync());
    }

    private void updateStorageDisplays() {
        if (storageManager == null) return;
        try {
            if (tvAvailableStorage != null) {
                long avail = storageManager.getAvailableStorageBytes(this);
                tvAvailableStorage.setText(OfflineStorageManager.formatStorageSize(avail) + " Free");
            }
            if (tvDownloadedMaterialsSize != null) {
                long downloaded = storageManager.getDownloadedMaterialsSizeBytes(this);
                tvDownloadedMaterialsSize.setText(OfflineStorageManager.formatStorageSize(downloaded));
            }
            if (tvCacheSize != null) {
                long cache = storageManager.getCacheSizeBytes(this);
                tvCacheSize.setText(OfflineStorageManager.formatStorageSize(cache));
            }
            if (tvSyncStatus != null) {
                tvSyncStatus.setText(currentSettings.getLastSyncStatus() + " ▼");
            }
            if (tvSyncDetails != null) {
                tvSyncDetails.setText("Last synced: " + OfflineStorageManager.formatSyncTimestamp(currentSettings.getLastSyncTimestamp()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating storage displays", e);
        }
    }

    private String formatSpeechRate(float rate) {
        if (rate <= 0.8f) return "0.75x Slow";
        if (rate <= 1.1f) return "1.0x Normal";
        if (rate <= 1.35f) return "1.25x Fast";
        return "1.5x Very Fast";
    }

    private String formatSpeechPitch(float pitch) {
        if (pitch <= 0.85f) return "0.8x Low";
        if (pitch <= 1.15f) return "1.0x Normal";
        return "1.25x High";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase(Locale.US) + str.substring(1);
    }

    private String getLanguageName(String code) {
        if ("am".equals(code)) return "Amharic (አማርኛ)";
        if ("om".equals(code)) return "Oromo (Afaan Oromoo)";
        if ("ti".equals(code)) return "Tigrinya (ትግርኛ)";
        if ("es".equals(code)) return "Spanish (Español)";
        if ("fr".equals(code)) return "French (Français)";
        if ("ar".equals(code)) return "Arabic (العربية)";
        return "English";
    }

    private String getAiTutorLanguageName(String code) {
        if ("am".equals(code)) return "Amharic (አማርኛ)";
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
        // Save Button
        if (btnSaveSettings != null) {
            btnSaveSettings.setOnClickListener(v -> {
                settingsService.saveSettings(currentSettings);
                NotificationHelper.scheduleStudyReminder(this, currentSettings.getReminderTime());
                AppFeedbackManager.getInstance(this).performSuccess(v);
                AccessibilityUtils.announceForAccessibility(v, "All settings saved successfully");
                Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
            });
        }

        // Section 1: Account & Profile
        View.OnClickListener openProfile = v -> {
            AppFeedbackManager.getInstance(this).performTap(v);
            startActivity(new Intent(this, EditProfileActivity.class));
        };
        if (cardProfileSummary != null) cardProfileSummary.setOnClickListener(openProfile);
        if (llAccountSettings != null) llAccountSettings.setOnClickListener(openProfile);

        if (llPrivacySecurity != null) {
            llPrivacySecurity.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                startActivity(new Intent(this, PrivacySecurityActivity.class));
            });
        }

        // Section 2: Appearance
        if (llThemeView != null) {
            llThemeView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] options = {"Light", "Dark", "System Default"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Select Theme Mode")
                        .setItems(options, (dialog, which) -> {
                            String choice = which == 0 ? ThemeManager.THEME_LIGHT : (which == 1 ? ThemeManager.THEME_DARK : ThemeManager.THEME_SYSTEM);
                            currentSettings.setThemeMode(choice);
                            if (tvTheme != null) tvTheme.setText(options[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                            ThemeManager.setThemeMode(this, choice);
                        }).show();
            });
        }

        if (llFontSizeView != null) {
            llFontSizeView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] sizes = {"Small", "Medium", "Large", "Extra Large"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Typography & Font Scale")
                        .setItems(sizes, (dialog, which) -> {
                            String choice = sizes[which].toLowerCase(Locale.US);
                            currentSettings.setFontSize(choice);
                            if (tvFontSize != null) tvFontSize.setText(sizes[which] + " ▼");
                            boolean isLarge = "large".equals(choice) || "extra large".equals(choice);
                            InclusiveModeManager.getInstance(this).setLargeTextEnabled(isLarge);
                            settingsService.saveSettings(currentSettings);
                        }).show();
            });
        }

        if (switchReduceMotion != null) {
            switchReduceMotion.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setReduceMotion(isChecked);
                settingsService.saveSettings(currentSettings);
                InclusiveModeManager.getInstance(this).setReducedMotionEnabled(isChecked);
            });
        }

        // Section 3: Learning Preferences
        if (llDailyGoalView != null) {
            llDailyGoalView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] goals = {"250 XP (Casual)", "500 XP (Regular)", "1000 XP (Serious)", "1500 XP (Intense)"};
                final int[] xpValues = {250, 500, 1000, 1500};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Daily Study Target (XP)")
                        .setItems(goals, (dialog, which) -> {
                            int targetXp = xpValues[which];
                            if (tvDailyGoal != null) tvDailyGoal.setText(targetXp + " XP ▼");
                            ContentValues cv = new ContentValues();
                            cv.put("xp_goal", targetXp);
                            dbHelper.getWritableDatabase().update("users", cv, "LOWER(username)=LOWER(?)", new String[]{currentUsername.trim()});
                            AppFeedbackManager.getInstance(this).performSuccess(tvDailyGoal);
                        }).show();
            });
        }

        if (llDailyStudyDurationView != null) {
            llDailyStudyDurationView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] durations = {"15 Minutes (Sprint)", "30 Minutes (Balanced)", "45 Minutes (Dedicated)", "60 Minutes (Intensive)"};
                final int[] minuteValues = {15, 30, 45, 60};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Daily Study Duration")
                        .setItems(durations, (dialog, which) -> {
                            int mins = minuteValues[which];
                            currentSettings.setDailyStudyMinutes(mins);
                            if (tvDailyStudyDuration != null) tvDailyStudyDuration.setText(mins + " Mins ▼");
                            settingsService.saveSettings(currentSettings);
                            AppFeedbackManager.getInstance(this).performSuccess(tvDailyStudyDuration);
                        }).show();
            });
        }

        if (llWeeklyLessonGoalView != null) {
            llWeeklyLessonGoalView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] goals = {"5 Lessons / week", "10 Lessons / week", "15 Lessons / week", "20 Lessons / week"};
                final int[] lessonValues = {5, 10, 15, 20};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Weekly Lesson Target")
                        .setItems(goals, (dialog, which) -> {
                            int lessons = lessonValues[which];
                            currentSettings.setWeeklyLessonGoal(lessons);
                            if (tvWeeklyLessonGoal != null) tvWeeklyLessonGoal.setText(lessons + " Lessons ▼");
                            settingsService.saveSettings(currentSettings);
                            AppFeedbackManager.getInstance(this).performSuccess(tvWeeklyLessonGoal);
                        }).show();
            });
        }

        if (llPracticeDifficultyView != null) {
            llPracticeDifficultyView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] difficulties = {"Beginner (Foundational drills)", "Intermediate (Standard curriculum)", "Advanced (Exam & Olympiad challenge)"};
                final String[] diffKeys = {"Beginner", "Intermediate", "Advanced"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Practice Difficulty")
                        .setItems(difficulties, (dialog, which) -> {
                            currentSettings.setPracticeDifficulty(diffKeys[which]);
                            if (tvPracticeDifficulty != null) tvPracticeDifficulty.setText(diffKeys[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                            AppFeedbackManager.getInstance(this).performSuccess(tvPracticeDifficulty);
                        }).show();
            });
        }

        if (llExplanationDepthView != null) {
            llExplanationDepthView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] depths = {"Concise (Key formulas & takeaways)", "Step-by-Step (Methodical progression)", "Comprehensive (Theory, proofs & context)"};
                final String[] depthKeys = {"Concise", "Step-by-Step", "Comprehensive"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Preferred Explanation Depth")
                        .setItems(depths, (dialog, which) -> {
                            currentSettings.setExplanationDepth(depthKeys[which]);
                            if (tvExplanationDepth != null) tvExplanationDepth.setText(depthKeys[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                            AppFeedbackManager.getInstance(this).performSuccess(tvExplanationDepth);
                        }).show();
            });
        }

        if (llLessonPresentationView != null) {
            llLessonPresentationView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] presentations = {"Interactive & Visual (Visualizers & experiments)", "Text & Notes (Deep reading & formulas)", "Audio-First (Narrated lessons)"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Lesson Presentation Format")
                        .setItems(presentations, (dialog, which) -> {
                            currentSettings.setPreferredLessonPresentation(presentations[which]);
                            if (tvLessonPresentation != null) tvLessonPresentation.setText(presentations[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                            AppFeedbackManager.getInstance(this).performSuccess(tvLessonPresentation);
                        }).show();
            });
        }

        if (switchPersonalizedRecommendations != null) {
            switchPersonalizedRecommendations.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setPersonalizedRecommendations(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (switchSoundEffects != null) {
            switchSoundEffects.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setSoundEffects(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (switchHapticFeedback != null) {
            switchHapticFeedback.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setHapticFeedback(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (switchAnimations != null) {
            switchAnimations.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setAnimatedCelebrations(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        // Section 4: Language & Region
        if (llLanguageView != null) {
            llLanguageView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] langs = {"English", "Amharic (አማርኛ)", "Oromo (Afaan Oromoo)", "Tigrinya (ትግርኛ)", "Spanish (Español)", "French (Français)", "Arabic (العربية)"};
                String[] codes = {"en", "am", "om", "ti", "es", "fr", "ar"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Select Interface Language")
                        .setItems(langs, (dialog, which) -> {
                            String chosenCode = codes[which];
                            currentSettings.setLanguage(chosenCode);
                            if (tvLanguage != null) tvLanguage.setText(langs[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                            LocaleHelper.setLocale(this, chosenCode);
                            Toast.makeText(this, "Language set to " + langs[which] + ". Applying...", Toast.LENGTH_SHORT).show();
                            // Recreate to immediately apply localized strings & RTL layout direction
                            recreate();
                        }).show();
            });
        }

        if (llRegionView != null) {
            llRegionView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] regions = {"United States", "Ethiopia", "United Kingdom", "Canada", "International Standard"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Select Regional Curriculum")
                        .setItems(regions, (dialog, which) -> {
                            currentSettings.setRegion(regions[which]);
                            if (tvRegion != null) tvRegion.setText(regions[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                        }).show();
            });
        }

        if (llDateFormatView != null) {
            llDateFormatView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] formats = {"MM/DD/YYYY", "DD/MM/YYYY", "YYYY-MM-DD"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Select Date Format")
                        .setItems(formats, (dialog, which) -> {
                            currentSettings.setDateFormat(formats[which]);
                            if (tvDateFormat != null) tvDateFormat.setText(formats[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                        }).show();
            });
        }

        // Section 5: Accessibility & Voice
        if (switchInclusiveMode != null) {
            switchInclusiveMode.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setInclusiveMode(isChecked);
                InclusiveModeManager.getInstance(this).setInclusiveModeEnabled(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (switchTextToSpeech != null) {
            switchTextToSpeech.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setTextToSpeech(isChecked);
                InclusiveModeManager.getInstance(this).setTextToSpeechEnabled(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (llSpeechRateView != null) {
            llSpeechRateView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showSpeechRateDialog();
            });
        }

        if (llSpeechPitchView != null) {
            llSpeechPitchView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showSpeechPitchDialog();
            });
        }

        if (switchHighContrast != null) {
            switchHighContrast.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setHighContrastMode(isChecked);
                InclusiveModeManager.getInstance(this).setHighContrastEnabled(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (switchAutoRead != null) {
            switchAutoRead.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setAutoReadContent(isChecked);
                InclusiveModeManager.getInstance(this).setAutoReadContentEnabled(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        // Section 6: Notifications & Reminders
        if (switchPushNotifications != null) {
            switchPushNotifications.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setPushEnabled(isChecked);
                settingsService.saveSettings(currentSettings);
                if (isChecked) {
                    NotificationHelper.scheduleStudyReminder(this, currentSettings.getReminderTime());
                } else {
                    NotificationHelper.cancelStudyReminder(this);
                }
            });
        }

        if (llReminderTimeView != null) {
            llReminderTimeView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String time = currentSettings.getReminderTime();
                if (time == null || !time.contains(":")) time = "17:00";
                String[] timeParts = time.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                new android.app.TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
                    String selectedTime24 = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
                    currentSettings.setReminderTime(selectedTime24);
                    if (tvReminderTime != null) tvReminderTime.setText(formatTo12Hour(selectedTime24));
                    settingsService.saveSettings(currentSettings);
                    NotificationHelper.scheduleStudyReminder(this, selectedTime24);
                    AppFeedbackManager.getInstance(this).performSuccess(tvReminderTime);
                }, hour, minute, false).show();
            });
        }

        if (llNotificationPrefs != null) {
            llNotificationPrefs.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                startActivity(new Intent(this, NotificationPreferencesActivity.class));
            });
        }

        // Section 7: Offline Learning & Storage
        if (switchOfflineMode != null) {
            switchOfflineMode.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setOfflineMode(isChecked);
                settingsService.saveSettings(currentSettings);
                Toast.makeText(this, isChecked ? "Offline study engine active." : "Cloud AI and online sync active.", Toast.LENGTH_SHORT).show();
            });
        }

        if (llAutoDownloadView != null) {
            llAutoDownloadView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] options = {"Wi-Fi only", "Wi-Fi and Cellular", "Never"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Auto-Download Media")
                        .setItems(options, (dialog, which) -> {
                            currentSettings.setAutoDownloadMedia(options[which]);
                            if (tvAutoDownload != null) tvAutoDownload.setText(options[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                        }).show();
            });
        }

        if (switchDataSaver != null) {
            switchDataSaver.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setDataSaver(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (llDownloadedMaterials != null) {
            llDownloadedMaterials.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                long bytes = storageManager.getDownloadedMaterialsSizeBytes(this);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Downloaded Study Materials")
                        .setMessage("Total offline course footprint: " + OfflineStorageManager.formatStorageSize(bytes) +
                                "\n\nDownloaded materials include interactive lab models, lesson notes, and offline quiz banks for Grade 9-12.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }

        if (llClearCache != null) {
            llClearCache.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Clear Temporary Storage")
                        .setMessage("This will remove temporary cache files to reclaim storage. Your study progress, notes, and offline curriculum are completely safe.")
                        .setPositiveButton("Clear Storage", (d, w) -> {
                            storageManager.clearTemporaryCacheOnly(this);
                            updateStorageDisplays();
                            AppFeedbackManager.getInstance(this).performSuccess(llClearCache);
                            Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (llDeleteDownloads != null) {
            llDeleteDownloads.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Delete Downloaded Resources?")
                        .setMessage("This will remove all downloaded course packs and offline media to reclaim device storage. Your grades, progress, and account data remain completely safe.")
                        .setPositiveButton("Delete Downloads", (d, w) -> {
                            storageManager.deleteDownloadedMaterials(this);
                            updateStorageDisplays();
                            AppFeedbackManager.getInstance(this).performSuccess(llDeleteDownloads);
                            Toast.makeText(this, "Downloaded materials removed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (llSyncStatus != null) {
            llSyncStatus.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                if (tvSyncStatus != null) tvSyncStatus.setText("Syncing...");
                SettingsSyncManager.getInstance(this).syncNow(currentUsername, null, new SettingsSyncManager.SyncCallback() {
                    @Override
                    public void onSyncSuccess(long lastSyncTimestamp) {
                        currentSettings.setLastSyncTimestamp(lastSyncTimestamp);
                        currentSettings.setLastSyncStatus("Synchronized");
                        currentSettings.setLastSyncError("None");
                        updateStorageDisplays();
                        AppFeedbackManager.getInstance(SettingsActivity.this).performSuccess(llSyncStatus);
                        Toast.makeText(SettingsActivity.this, "Cloud preferences synchronized", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSyncConflict(String message) {
                        currentSettings.setLastSyncStatus("Conflict");
                        currentSettings.setLastSyncError(message);
                        updateStorageDisplays();
                        AppFeedbackManager.getInstance(SettingsActivity.this).performError(llSyncStatus);
                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSyncFailed(String reason) {
                        currentSettings.setLastSyncStatus("Pending");
                        currentSettings.setLastSyncError(reason);
                        updateStorageDisplays();
                        Toast.makeText(SettingsActivity.this, reason, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Section 8: AI Tutor Preferences
        if (llAiTutorLanguageView != null) {
            llAiTutorLanguageView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] langs = {"English", "Amharic (አማርኛ)"};
                final String[] codes = {"en", "am"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("AI Tutor Language")
                        .setItems(langs, (dialog, which) -> {
                            currentSettings.setAiTutorLanguage(codes[which]);
                            if (tvAiTutorLanguage != null) tvAiTutorLanguage.setText(langs[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                            AppFeedbackManager.getInstance(this).performSuccess(tvAiTutorLanguage);
                        }).show();
            });
        }

        if (switchAiVoiceResponse != null) {
            switchAiVoiceResponse.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setAiVoiceResponse(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (llAiExplanationLengthView != null) {
            llAiExplanationLengthView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] lengths = {"Concise (Key answers & quick recap)", "Standard (Balanced explanations)", "Comprehensive (Deep dive & examples)"};
                final String[] lengthKeys = {"Concise", "Standard", "Comprehensive"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("AI Explanation Length")
                        .setItems(lengths, (dialog, which) -> {
                            currentSettings.setAiExplanationLength(lengthKeys[which]);
                            if (tvAiExplanationLength != null) tvAiExplanationLength.setText(lengthKeys[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                            AppFeedbackManager.getInstance(this).performSuccess(tvAiExplanationLength);
                        }).show();
            });
        }

        if (llAiHelpLevelView != null) {
            llAiHelpLevelView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] levels = {"Socratic Hints (Guides inquiry)", "Step-by-Step (Detailed walkthrough)", "Comprehensive Solutions (Complete breakdown)"};
                final String[] keys = {"Hints", "Step-by-Step", "Comprehensive"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("EduAI Assistance Level")
                        .setItems(levels, (dialog, which) -> {
                            currentSettings.setAiHelpLevel(keys[which]);
                            if (tvAiHelpLevel != null) tvAiHelpLevel.setText(keys[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                        }).show();
            });
        }

        if (llAiLearningStyleView != null) {
            llAiLearningStyleView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] styles = {"Balanced", "Conceptual & Theoretical", "Practical & Visual", "Exam-Focused & Drills"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("EduAI Learning Style")
                        .setItems(styles, (dialog, which) -> {
                            currentSettings.setAiLearningStyle(styles[which]);
                            if (tvAiLearningStyle != null) tvAiLearningStyle.setText(styles[which] + " ▼");
                            settingsService.saveSettings(currentSettings);
                        }).show();
            });
        }

        if (llClearChatHistory != null) {
            llClearChatHistory.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Reset EduAI Chat History")
                        .setMessage("Are you sure you want to clear your conversation history with your AI tutor? This action cannot be undone.")
                        .setPositiveButton("Clear History", (dialog, which) -> {
                            dbHelper.clearChatHistory(currentUsername);
                            AppFeedbackManager.getInstance(this).performSuccess(llClearChatHistory);
                            Toast.makeText(this, "AI chat history cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        // Section 9: Privacy & Security
        if (llProfileVisibilityView != null) {
            llProfileVisibilityView.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                String[] visibilities = {"Public (All students)", "Private (Only you)"};
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Profile Visibility")
                        .setItems(visibilities, (dialog, which) -> {
                            boolean isPublic = which == 0;
                            currentSettings.setProfileVisibility(isPublic);
                            if (tvProfileVisibility != null) tvProfileVisibility.setText((isPublic ? "Public" : "Private") + " ▼");
                            settingsService.saveSettings(currentSettings);
                        }).show();
            });
        }

        if (switchShareProgress != null) {
            switchShareProgress.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setShareProgress(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (switchCloudSync != null) {
            switchCloudSync.setOnCheckedChangeListener((v, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                currentSettings.setCloudSync(isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }

        if (llDataExportDelete != null) {
            llDataExportDelete.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                startActivity(new Intent(this, PrivacySecurityActivity.class));
            });
        }

        // Section 10: Help and About Learnify
        if (llHelpSupport != null) {
            llHelpSupport.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Learnify Help Center")
                        .setMessage("• EduAI Assistance: Ask questions anytime in the AI Chat tab.\n" +
                                "• Offline Studies: Download lessons or enable Offline Mode for uninterrupted study.\n" +
                                "• Contact Support: support@learnify.com\n" +
                                "• Documentation: https://learnify.com/docs")
                        .setPositiveButton("Close", null)
                        .show();
            });
        }

        if (llRateUs != null) {
            llRateUs.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showRateUsDialog();
            });
        }

        if (llTerms != null) {
            llTerms.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showModalWebView("Terms of Service", "https://learnify.com/terms");
            });
        }

        if (llPrivacyPolicy != null) {
            llPrivacyPolicy.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                showModalWebView("Privacy Policy", "https://learnify.com/privacy");
            });
        }

        if (llSignOut != null) {
            llSignOut.setOnClickListener(v -> {
                AppFeedbackManager.getInstance(this).performTap(v);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Sign Out")
                        .setMessage("Are you sure you want to end your current session? You can sign back in anytime.")
                        .setPositiveButton("Sign Out", (dialog, which) -> {
                            FirebaseSessionManager.signOut(this, FirebaseAuthSupport.getConfiguredAuth(this));
                            Intent intent = new Intent(this, WelcomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void showSpeechRateDialog() {
        String[] options = {"0.75x (Slow)", "1.0x (Normal)", "1.25x (Fast)", "1.5x (Very Fast)"};
        final float[] rates = {InclusiveModeManager.RATE_SLOW, InclusiveModeManager.RATE_NORMAL, InclusiveModeManager.RATE_FAST, InclusiveModeManager.RATE_VERY_FAST};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Voice Reading Pace")
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

    private void showSpeechPitchDialog() {
        String[] options = {"0.8x (Low Tone)", "1.0x (Natural Tone)", "1.25x (High Tone)"};
        final float[] pitches = {InclusiveModeManager.PITCH_LOW, InclusiveModeManager.PITCH_NORMAL, InclusiveModeManager.PITCH_HIGH};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Voice Pitch & Tone")
                .setItems(options, (dialog, which) -> {
                    float selectedPitch = pitches[which];
                    currentSettings.setSpeechPitch(selectedPitch);
                    InclusiveModeManager.getInstance(this).setSpeechPitch(selectedPitch);
                    settingsService.saveSettings(currentSettings);
                    if (tvSpeechPitch != null) {
                        tvSpeechPitch.setText(formatSpeechPitch(selectedPitch) + " ▼");
                    }
                    AccessibilityUtils.announceForAccessibility(tvSpeechPitch, "Speech pitch set to " + options[which]);
                })
                .show();
    }

    private void showRateUsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rate_app, null);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Rate Learnify")
                .setView(dialogView)
                .setPositiveButton("Submit Review", (dialog, which) -> {
                    AppFeedbackManager.getInstance(this).performSuccess(btnSaveSettings);
                    Toast.makeText(this, "Thank you for supporting Learnify!", Toast.LENGTH_SHORT).show();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (Exception e) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                        } catch (Exception ignored) {}
                    }
                })
                .setNegativeButton("Maybe Later", null)
                .show();
    }

    private void showModalWebView(String title, String url) {
        android.webkit.WebView webView = new android.webkit.WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 850));
        webView.getSettings().setJavaScriptEnabled(false);

        String fallbackHtml = "<html><body style='padding:16px; font-family:sans-serif; color:#0F172A; line-height:1.6;'>" +
                "<h3>" + title + "</h3>" +
                "<p>Learnify is dedicated to empowering students globally with high-quality curriculum, adaptive AI study aids, and interactive simulations.</p>" +
                "<ul><li><b>Student Privacy:</b> Academic records and credentials are authenticated securely and never sold to third parties.</li>" +
                "<li><b>Academic Integrity:</b> Solutions, quizzes, and simulations align with certified STEM curricula.</li>" +
                "<li><b>Offline Reliability:</b> Offline mode protects study continuity without requiring cellular bandwidth.</li></ul>" +
                "<p>For full official documentation, visit learnify.com when online.</p></body></html>";

        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
                webView.loadDataWithBaseURL(null, fallbackHtml, "text/html", "UTF-8", null);
            }
        });

        webView.loadDataWithBaseURL(null, fallbackHtml, "text/html", "UTF-8", null);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(webView)
                .setPositiveButton("Close", null)
                .show();
    }
}
