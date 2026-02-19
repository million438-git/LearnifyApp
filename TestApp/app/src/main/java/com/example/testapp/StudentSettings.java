package com.example.testapp;

/**
 * Model class for Student Settings.
 * Encapsulates all user preferences in a single object.
 */
public class StudentSettings {
    private String studentId;
    private String themeMode; // light | dark | auto
    private String fontSize; // small | medium | large
    private String language; // en | local
    private boolean textToSpeech;
    private boolean highContrastMode;
    
    private boolean offlineMode;
    private String videoQuality; // low | medium | high
    private boolean dataSaver;
    
    private boolean studyReminder;
    private boolean homeworkAlerts;
    private boolean examNotifications;
    private boolean aiStudyTips;
    private String reminderTime; // e.g., "08:00"
    
    private String aiHelpLevel; // basic | detailed
    private String aiLearningStyle; // example | practice
    
    private boolean profileVisibility;
    private boolean shareProgress;
    
    private long lastUpdated;

    public StudentSettings() {
        // Default values
        this.themeMode = "light";
        this.fontSize = "medium";
        this.language = "en";
        this.textToSpeech = false;
        this.highContrastMode = false;
        this.offlineMode = false;
        this.videoQuality = "medium";
        this.dataSaver = false;
        this.studyReminder = true;
        this.homeworkAlerts = true;
        this.examNotifications = true;
        this.aiStudyTips = true;
        this.reminderTime = "08:00";
        this.aiHelpLevel = "basic";
        this.aiLearningStyle = "example";
        this.profileVisibility = true;
        this.shareProgress = true;
        this.lastUpdated = System.currentTimeMillis();
    }

    public StudentSettings(boolean darkMode, boolean notificationsEnabled, boolean copyPasteEnabled, String language) {
        this();
        this.themeMode = darkMode ? "dark" : "light";
        this.studyReminder = notificationsEnabled;
        this.language = language;
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getThemeMode() { return themeMode; }
    public void setThemeMode(String themeMode) { this.themeMode = themeMode; }

    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isTextToSpeech() { return textToSpeech; }
    public void setTextToSpeech(boolean textToSpeech) { this.textToSpeech = textToSpeech; }

    public boolean isHighContrastMode() { return highContrastMode; }
    public void setHighContrastMode(boolean highContrastMode) { this.highContrastMode = highContrastMode; }

    public boolean isOfflineMode() { return offlineMode; }
    public void setOfflineMode(boolean offlineMode) { this.offlineMode = offlineMode; }

    public String getVideoQuality() { return videoQuality; }
    public void setVideoQuality(String videoQuality) { this.videoQuality = videoQuality; }

    public boolean isDataSaver() { return dataSaver; }
    public void setDataSaver(boolean dataSaver) { this.dataSaver = dataSaver; }

    public boolean isStudyReminder() { return studyReminder; }
    public void setStudyReminder(boolean studyReminder) { this.studyReminder = studyReminder; }

    public boolean isHomeworkAlerts() { return homeworkAlerts; }
    public void setHomeworkAlerts(boolean homeworkAlerts) { this.homeworkAlerts = homeworkAlerts; }

    public boolean isExamNotifications() { return examNotifications; }
    public void setExamNotifications(boolean examNotifications) { this.examNotifications = examNotifications; }

    public boolean isAiStudyTips() { return aiStudyTips; }
    public void setAiStudyTips(boolean aiStudyTips) { this.aiStudyTips = aiStudyTips; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getAiHelpLevel() { return aiHelpLevel; }
    public void setAiHelpLevel(String aiHelpLevel) { this.aiHelpLevel = aiHelpLevel; }

    public String getAiLearningStyle() { return aiLearningStyle; }
    public void setAiLearningStyle(String aiLearningStyle) { this.aiLearningStyle = aiLearningStyle; }

    public boolean isProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(boolean profileVisibility) { this.profileVisibility = profileVisibility; }

    public boolean isShareProgress() { return shareProgress; }
    public void setShareProgress(boolean shareProgress) { this.shareProgress = shareProgress; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
