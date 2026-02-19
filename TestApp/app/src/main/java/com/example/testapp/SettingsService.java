package com.example.testapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SettingsService handles the persistence of the expanded StudentSettings model.
 * Manages local storage via SharedPreferences and includes synchronization hooks.
 */
public class SettingsService {
    private static final String PREF_NAME = "StudentSettingsPref";
    
    // Keys for SharedPreferences
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_TEXT_TO_SPEECH = "text_to_speech";
    private static final String KEY_OFFLINE_MODE = "offline_mode";
    private static final String KEY_VIDEO_QUALITY = "video_quality";
    private static final String KEY_DATA_SAVER = "data_saver";
    
    // Notifications Section
    private static final String KEY_STUDY_REMINDER = "study_reminder";
    private static final String KEY_HOMEWORK_ALERTS = "homework_alerts";
    private static final String KEY_EXAM_NOTIFICATIONS = "exam_notifications";
    private static final String KEY_AI_STUDY_TIPS = "ai_study_tips";
    private static final String KEY_REMINDER_TIME = "reminder_time";

    private static final String KEY_AI_HELP_LEVEL = "ai_help_level";
    private static final String KEY_AI_LEARNING_STYLE = "ai_learning_style";
    private static final String KEY_PROFILE_VISIBILITY = "profile_visibility";
    private static final String KEY_SHARE_PROGRESS = "share_progress";
    private static final String KEY_LAST_UPDATED = "last_updated";

    private final SharedPreferences preferences;

    public SettingsService(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Loads settings from local storage into the StudentSettings model.
     */
    public StudentSettings loadSettings() {
        StudentSettings settings = new StudentSettings();
        
        settings.setStudentId(preferences.getString(KEY_STUDENT_ID, ""));
        settings.setThemeMode(preferences.getString(KEY_THEME_MODE, "light"));
        settings.setFontSize(preferences.getString(KEY_FONT_SIZE, "medium"));
        settings.setLanguage(preferences.getString(KEY_LANGUAGE, "en"));
        settings.setTextToSpeech(preferences.getBoolean(KEY_TEXT_TO_SPEECH, false));
        
        settings.setOfflineMode(preferences.getBoolean(KEY_OFFLINE_MODE, false));
        settings.setVideoQuality(preferences.getString(KEY_VIDEO_QUALITY, "medium"));
        settings.setDataSaver(preferences.getBoolean(KEY_DATA_SAVER, false));
        
        settings.setStudyReminder(preferences.getBoolean(KEY_STUDY_REMINDER, true));
        settings.setHomeworkAlerts(preferences.getBoolean(KEY_HOMEWORK_ALERTS, true));
        settings.setExamNotifications(preferences.getBoolean(KEY_EXAM_NOTIFICATIONS, true));
        settings.setAiStudyTips(preferences.getBoolean(KEY_AI_STUDY_TIPS, true));
        settings.setReminderTime(preferences.getString(KEY_REMINDER_TIME, "08:00"));
        
        settings.setAiHelpLevel(preferences.getString(KEY_AI_HELP_LEVEL, "basic"));
        settings.setAiLearningStyle(preferences.getString(KEY_AI_LEARNING_STYLE, "example"));
        
        settings.setProfileVisibility(preferences.getBoolean(KEY_PROFILE_VISIBILITY, true));
        settings.setShareProgress(preferences.getBoolean(KEY_SHARE_PROGRESS, true));
        
        settings.setLastUpdated(preferences.getLong(KEY_LAST_UPDATED, System.currentTimeMillis()));
        
        return settings;
    }

    /**
     * Saves the entire StudentSettings model to local storage and syncs with cloud.
     */
    public void saveSettings(StudentSettings settings) {
        long currentTime = System.currentTimeMillis();
        settings.setLastUpdated(currentTime);

        preferences.edit()
                .putString(KEY_STUDENT_ID, settings.getStudentId())
                .putString(KEY_THEME_MODE, settings.getThemeMode())
                .putString(KEY_FONT_SIZE, settings.getFontSize())
                .putString(KEY_LANGUAGE, settings.getLanguage())
                .putBoolean(KEY_TEXT_TO_SPEECH, settings.isTextToSpeech())
                .putBoolean(KEY_OFFLINE_MODE, settings.isOfflineMode())
                .putString(KEY_VIDEO_QUALITY, settings.getVideoQuality())
                .putBoolean(KEY_DATA_SAVER, settings.isDataSaver())
                .putBoolean(KEY_STUDY_REMINDER, settings.isStudyReminder())
                .putBoolean(KEY_HOMEWORK_ALERTS, settings.isHomeworkAlerts())
                .putBoolean(KEY_EXAM_NOTIFICATIONS, settings.isExamNotifications())
                .putBoolean(KEY_AI_STUDY_TIPS, settings.isAiStudyTips())
                .putString(KEY_REMINDER_TIME, settings.getReminderTime())
                .putString(KEY_AI_HELP_LEVEL, settings.getAiHelpLevel())
                .putString(KEY_AI_LEARNING_STYLE, settings.getAiLearningStyle())
                .putBoolean(KEY_PROFILE_VISIBILITY, settings.isProfileVisibility())
                .putBoolean(KEY_SHARE_PROGRESS, settings.isShareProgress())
                .putLong(KEY_LAST_UPDATED, currentTime)
                .apply();

        syncWithCloud(settings);
    }

    private void syncWithCloud(StudentSettings settings) {
        // TODO: Implement Cloud Sync
    }
}
