package com.million.learnify;

public class StudentSettings {

    private String themeMode;
    private boolean notificationsEnabled;
    private boolean textToSpeech;
    private String fontSize;
    private boolean studyReminder;
    private boolean homeworkAlerts;
    private boolean examNotifications;
    private boolean aiStudyTips;
    private boolean motivationEnabled;
    private String reminderTime;
    private String language;
    private boolean highContrastMode;
    private boolean offlineMode;
    private boolean dataSaver;
    private String videoQuality;
    private String aiHelpLevel;
    private String aiLearningStyle;
    private boolean profileVisibility;
    private boolean shareProgress;
    
    // Gameplay and Language & Region
    private boolean soundEffects;
    private boolean hapticFeedback;
    private boolean animatedCelebrations;
    private String region;
    private String dateFormat;
    private boolean reduceMotion;
    private String autoDownloadMedia;
    private boolean cloudSync;

    // Detailed Notification Preferences
    private boolean notifyDailyChallenge;
    private boolean notifyFriendRequests;
    private boolean notifyFriendActivity;
    private boolean notifyAchievements;
    private boolean notifyXPMilestones;
    private boolean notifyStreakAlerts;
    private boolean notifyAnnouncements;
    private boolean pushEnabled;

    // Privacy & Security
    private boolean twoFactorEnabled;

    public StudentSettings(String themeMode, boolean notificationsEnabled) {
        this(themeMode, notificationsEnabled, true);
    }

    public StudentSettings(String themeMode, boolean notificationsEnabled, boolean textToSpeech) {
        this.themeMode = themeMode;
        this.notificationsEnabled = notificationsEnabled;
        this.textToSpeech = textToSpeech;
        this.fontSize = "medium";
        this.studyReminder = true;
        this.homeworkAlerts = true;
        this.examNotifications = true;
        this.aiStudyTips = true;
        this.motivationEnabled = true;
        this.reminderTime = "17:00"; 
        this.language = "en";
        this.highContrastMode = false;
        this.offlineMode = false;
        this.dataSaver = false;
        this.videoQuality = "720p";
        this.aiHelpLevel = "Standard";
        this.aiLearningStyle = "Balanced";
        this.profileVisibility = true;
        this.shareProgress = true;
        
        this.soundEffects = true;
        this.hapticFeedback = true;
        this.animatedCelebrations = true;
        this.region = "US";
        this.dateFormat = "MM/DD/YYYY";
        this.reduceMotion = false;
        this.autoDownloadMedia = "Wi-Fi only";
        this.cloudSync = true;

        // Notification defaults
        this.notifyDailyChallenge = true;
        this.notifyFriendRequests = true;
        this.notifyFriendActivity = true;
        this.notifyAchievements = true;
        this.notifyXPMilestones = true;
        this.notifyStreakAlerts = true;
        this.notifyAnnouncements = true;
        this.pushEnabled = true;

        this.twoFactorEnabled = false;
    }

    // Getters and Setters
    public String getThemeMode() { return themeMode; }
    public void setThemeMode(String themeMode) { this.themeMode = themeMode; }
    public boolean areNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
    public boolean isTextToSpeech() { return textToSpeech; }
    public void setTextToSpeech(boolean textToSpeech) { this.textToSpeech = textToSpeech; }
    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }
    public boolean isStudyReminder() { return studyReminder; }
    public void setStudyReminder(boolean studyReminder) { this.studyReminder = studyReminder; }
    public boolean isHomeworkAlerts() { return homeworkAlerts; }
    public void setHomeworkAlerts(boolean homeworkAlerts) { this.homeworkAlerts = homeworkAlerts; }
    public boolean isExamNotifications() { return examNotifications; }
    public void setExamNotifications(boolean examNotifications) { this.examNotifications = examNotifications; }
    public boolean isAiStudyTips() { return aiStudyTips; }
    public void setAiStudyTips(boolean aiStudyTips) { this.aiStudyTips = aiStudyTips; }
    public boolean isMotivationEnabled() { return motivationEnabled; }
    public void setMotivationEnabled(boolean motivationEnabled) { this.motivationEnabled = motivationEnabled; }
    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public boolean isHighContrastMode() { return highContrastMode; }
    public void setHighContrastMode(boolean highContrastMode) { this.highContrastMode = highContrastMode; }
    public boolean isOfflineMode() { return offlineMode; }
    public void setOfflineMode(boolean offlineMode) { this.offlineMode = offlineMode; }
    public boolean isDataSaver() { return dataSaver; }
    public void setDataSaver(boolean dataSaver) { this.dataSaver = dataSaver; }
    public String getVideoQuality() { return videoQuality; }
    public void setVideoQuality(String videoQuality) { this.videoQuality = videoQuality; }
    public String getAiHelpLevel() { return aiHelpLevel; }
    public void setAiHelpLevel(String aiHelpLevel) { this.aiHelpLevel = aiHelpLevel; }
    public String getAiLearningStyle() { return aiLearningStyle; }
    public void setAiLearningStyle(String aiLearningStyle) { this.aiLearningStyle = aiLearningStyle; }
    public boolean isProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(boolean profileVisibility) { this.profileVisibility = profileVisibility; }
    public boolean isShareProgress() { return shareProgress; }
    public void setShareProgress(boolean shareProgress) { this.shareProgress = shareProgress; }
    public boolean isSoundEffects() { return soundEffects; }
    public void setSoundEffects(boolean soundEffects) { this.soundEffects = soundEffects; }
    public boolean isHapticFeedback() { return hapticFeedback; }
    public void setHapticFeedback(boolean hapticFeedback) { this.hapticFeedback = hapticFeedback; }
    public boolean isAnimatedCelebrations() { return animatedCelebrations; }
    public void setAnimatedCelebrations(boolean animatedCelebrations) { this.animatedCelebrations = animatedCelebrations; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
    public boolean isReduceMotion() { return reduceMotion; }
    public void setReduceMotion(boolean reduceMotion) { this.reduceMotion = reduceMotion; }
    public String getAutoDownloadMedia() { return autoDownloadMedia; }
    public void setAutoDownloadMedia(String autoDownloadMedia) { this.autoDownloadMedia = autoDownloadMedia; }
    public boolean isCloudSync() { return cloudSync; }
    public void setCloudSync(boolean cloudSync) { this.cloudSync = cloudSync; }

    public boolean isNotifyDailyChallenge() { return notifyDailyChallenge; }
    public void setNotifyDailyChallenge(boolean notifyDailyChallenge) { this.notifyDailyChallenge = notifyDailyChallenge; }
    public boolean isNotifyFriendRequests() { return notifyFriendRequests; }
    public void setNotifyFriendRequests(boolean notifyFriendRequests) { this.notifyFriendRequests = notifyFriendRequests; }
    public boolean isNotifyFriendActivity() { return notifyFriendActivity; }
    public void setNotifyFriendActivity(boolean notifyFriendActivity) { this.notifyFriendActivity = notifyFriendActivity; }
    public boolean isNotifyAchievements() { return notifyAchievements; }
    public void setNotifyAchievements(boolean notifyAchievements) { this.notifyAchievements = notifyAchievements; }
    public boolean isNotifyXPMilestones() { return notifyXPMilestones; }
    public void setNotifyXPMilestones(boolean notifyXPMilestones) { this.notifyXPMilestones = notifyXPMilestones; }
    public boolean isNotifyStreakAlerts() { return notifyStreakAlerts; }
    public void setNotifyStreakAlerts(boolean notifyStreakAlerts) { this.notifyStreakAlerts = notifyStreakAlerts; }
    public boolean isNotifyAnnouncements() { return notifyAnnouncements; }
    public void setNotifyAnnouncements(boolean notifyAnnouncements) { this.notifyAnnouncements = notifyAnnouncements; }
    public boolean isPushEnabled() { return pushEnabled; }
    public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
}
