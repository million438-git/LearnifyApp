package com.million.learnify;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsService {

    private static final String PREFS_NAME = "StudentSettings";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_REMINDER_TIME = "reminder_time";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_TTS = "text_to_speech";
    private static final String KEY_HIGH_CONTRAST = "high_contrast";
    private static final String KEY_OFFLINE = "offline_mode";
    private static final String KEY_DATA_SAVER = "data_saver";
    private static final String KEY_VIDEO_QUALITY = "video_quality";
    private static final String KEY_AI_HELP = "ai_help_level";
    private static final String KEY_AI_LEARNING = "ai_learning_style";
    private static final String KEY_PROFILE_VISIBILITY = "profile_visibility";
    private static final String KEY_SHARE_PROGRESS = "share_progress";
    private static final String KEY_REDUCE_MOTION = "reduce_motion";
    
    // Gameplay
    private static final String KEY_SOUND_EFFECTS = "sound_effects";
    private static final String KEY_HAPTIC = "haptic_feedback";
    private static final String KEY_ANIMATIONS = "animated_celebrations";
    private static final String KEY_REGION = "region";
    private static final String KEY_DATE_FORMAT = "date_format";
    private static final String KEY_AUTO_DOWNLOAD = "auto_download";
    private static final String KEY_CLOUD_SYNC = "cloud_sync";

    // Detailed Notifications
    private static final String KEY_NOTIFY_CHALLENGE = "notify_challenge";
    private static final String KEY_NOTIFY_FRIEND_REQ = "notify_friend_req";
    private static final String KEY_NOTIFY_FRIEND_ACT = "notify_friend_act";
    private static final String KEY_NOTIFY_ACHIEVEMENTS = "notify_achievements";
    private static final String KEY_NOTIFY_XP = "notify_xp";
    private static final String KEY_NOTIFY_STREAK = "notify_streak";
    private static final String KEY_NOTIFY_ANNOUNCEMENTS = "notify_announcements";
    private static final String KEY_PUSH_ENABLED = "push_enabled";

    // Privacy
    private static final String KEY_2FA = "two_factor_enabled";

    // Inclusive Learning Mode
    public static final String KEY_INCLUSIVE_MODE = "inclusive_mode";
    public static final String KEY_SPEECH_RATE = "speech_rate";
    public static final String KEY_AUTO_READ = "auto_read_content";

    private final SharedPreferences sharedPreferences;
    private final Context context;

    public SettingsService(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSettings(StudentSettings settings) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_THEME, settings.getThemeMode());
        editor.putBoolean(KEY_NOTIFICATIONS, settings.areNotificationsEnabled());
        editor.putString(KEY_FONT_SIZE, settings.getFontSize());
        editor.putString(KEY_REMINDER_TIME, settings.getReminderTime());
        editor.putString(KEY_LANGUAGE, settings.getLanguage());
        editor.putBoolean(KEY_TTS, settings.isTextToSpeech());
        editor.putBoolean(KEY_HIGH_CONTRAST, settings.isHighContrastMode());
        editor.putBoolean(KEY_OFFLINE, settings.isOfflineMode());
        editor.putBoolean(KEY_DATA_SAVER, settings.isDataSaver());
        editor.putString(KEY_VIDEO_QUALITY, settings.getVideoQuality());
        editor.putString(KEY_AI_HELP, settings.getAiHelpLevel());
        editor.putString(KEY_AI_LEARNING, settings.getAiLearningStyle());
        editor.putBoolean(KEY_PROFILE_VISIBILITY, settings.isProfileVisibility());
        editor.putBoolean(KEY_SHARE_PROGRESS, settings.isShareProgress());
        editor.putBoolean(KEY_REDUCE_MOTION, settings.isReduceMotion());
        
        editor.putBoolean(KEY_SOUND_EFFECTS, settings.isSoundEffects());
        editor.putBoolean(KEY_HAPTIC, settings.isHapticFeedback());
        editor.putBoolean(KEY_ANIMATIONS, settings.isAnimatedCelebrations());
        editor.putString(KEY_REGION, settings.getRegion());
        editor.putString(KEY_DATE_FORMAT, settings.getDateFormat());
        editor.putString(KEY_AUTO_DOWNLOAD, settings.getAutoDownloadMedia());
        editor.putBoolean(KEY_CLOUD_SYNC, settings.isCloudSync());

        editor.putBoolean(KEY_NOTIFY_CHALLENGE, settings.isNotifyDailyChallenge());
        editor.putBoolean(KEY_NOTIFY_FRIEND_REQ, settings.isNotifyFriendRequests());
        editor.putBoolean(KEY_NOTIFY_FRIEND_ACT, settings.isNotifyFriendActivity());
        editor.putBoolean(KEY_NOTIFY_ACHIEVEMENTS, settings.isNotifyAchievements());
        editor.putBoolean(KEY_NOTIFY_XP, settings.isNotifyXPMilestones());
        editor.putBoolean(KEY_NOTIFY_STREAK, settings.isNotifyStreakAlerts());
        editor.putBoolean(KEY_NOTIFY_ANNOUNCEMENTS, settings.isNotifyAnnouncements());
        editor.putBoolean(KEY_PUSH_ENABLED, settings.isPushEnabled());
        editor.putBoolean(KEY_2FA, settings.isTwoFactorEnabled());

        editor.putBoolean(KEY_INCLUSIVE_MODE, settings.isInclusiveMode());
        editor.putFloat(KEY_SPEECH_RATE, settings.getSpeechRate());
        editor.putBoolean(KEY_AUTO_READ, settings.isAutoReadContent());
        
        editor.apply();

        // Synchronize with centralized InclusiveModeManager
        try {
            InclusiveModeManager imm = InclusiveModeManager.getInstance(context);
            imm.setInclusiveModeEnabled(settings.isInclusiveMode());
            imm.setTextToSpeechEnabled(settings.isTextToSpeech());
            imm.setSpeechRate(settings.getSpeechRate());
            imm.setHighContrastEnabled(settings.isHighContrastMode());
            imm.setReducedMotionEnabled(settings.isReduceMotion());
            imm.setAutoReadContentEnabled(settings.isAutoReadContent());
        } catch (Exception ignored) {
        }
    }

    public StudentSettings loadSettings() {
        String themeMode = sharedPreferences.getString(KEY_THEME, "light");
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
        StudentSettings settings = new StudentSettings(themeMode, notificationsEnabled);
        
        settings.setFontSize(sharedPreferences.getString(KEY_FONT_SIZE, "medium"));
        settings.setReminderTime(sharedPreferences.getString(KEY_REMINDER_TIME, "17:00"));
        settings.setLanguage(sharedPreferences.getString(KEY_LANGUAGE, "en"));
        settings.setTextToSpeech(sharedPreferences.getBoolean(KEY_TTS, true));
        settings.setHighContrastMode(sharedPreferences.getBoolean(KEY_HIGH_CONTRAST, false));
        settings.setOfflineMode(sharedPreferences.getBoolean(KEY_OFFLINE, false));
        settings.setDataSaver(sharedPreferences.getBoolean(KEY_DATA_SAVER, false));
        settings.setVideoQuality(sharedPreferences.getString(KEY_VIDEO_QUALITY, "720p"));
        settings.setAiHelpLevel(sharedPreferences.getString(KEY_AI_HELP, "Standard"));
        settings.setAiLearningStyle(sharedPreferences.getString(KEY_AI_LEARNING, "Balanced"));
        settings.setProfileVisibility(sharedPreferences.getBoolean(KEY_PROFILE_VISIBILITY, true));
        settings.setShareProgress(sharedPreferences.getBoolean(KEY_SHARE_PROGRESS, true));
        settings.setReduceMotion(sharedPreferences.getBoolean(KEY_REDUCE_MOTION, false));
        
        settings.setSoundEffects(sharedPreferences.getBoolean(KEY_SOUND_EFFECTS, true));
        settings.setHapticFeedback(sharedPreferences.getBoolean(KEY_HAPTIC, true));
        settings.setAnimatedCelebrations(sharedPreferences.getBoolean(KEY_ANIMATIONS, true));
        settings.setRegion(sharedPreferences.getString(KEY_REGION, "US"));
        settings.setDateFormat(sharedPreferences.getString(KEY_DATE_FORMAT, "MM/DD/YYYY"));
        settings.setAutoDownloadMedia(sharedPreferences.getString(KEY_AUTO_DOWNLOAD, "Wi-Fi only"));
        settings.setCloudSync(sharedPreferences.getBoolean(KEY_CLOUD_SYNC, true));

        settings.setNotifyDailyChallenge(sharedPreferences.getBoolean(KEY_NOTIFY_CHALLENGE, true));
        settings.setNotifyFriendRequests(sharedPreferences.getBoolean(KEY_NOTIFY_FRIEND_REQ, true));
        settings.setNotifyFriendActivity(sharedPreferences.getBoolean(KEY_NOTIFY_FRIEND_ACT, true));
        settings.setNotifyAchievements(sharedPreferences.getBoolean(KEY_NOTIFY_ACHIEVEMENTS, true));
        settings.setNotifyXPMilestones(sharedPreferences.getBoolean(KEY_NOTIFY_XP, true));
        settings.setNotifyStreakAlerts(sharedPreferences.getBoolean(KEY_NOTIFY_STREAK, true));
        settings.setNotifyAnnouncements(sharedPreferences.getBoolean(KEY_NOTIFY_ANNOUNCEMENTS, true));
        settings.setPushEnabled(sharedPreferences.getBoolean(KEY_PUSH_ENABLED, true));
        settings.setTwoFactorEnabled(sharedPreferences.getBoolean(KEY_2FA, false));

        settings.setInclusiveMode(sharedPreferences.getBoolean(KEY_INCLUSIVE_MODE, false));
        settings.setSpeechRate(sharedPreferences.getFloat(KEY_SPEECH_RATE, 1.0f));
        settings.setAutoReadContent(sharedPreferences.getBoolean(KEY_AUTO_READ, false));
        
        return settings;
    }
}
