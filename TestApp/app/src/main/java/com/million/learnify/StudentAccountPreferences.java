package com.million.learnify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentAccountPreferences - Category B Preferences.
 * Authoritative at the student account level, persisted in local SQLite and synchronized across student devices.
 * Contains learning goals, subjects, AI tutor settings, reminders, and profile sharing configurations.
 */
public class StudentAccountPreferences implements Serializable {

    private static final Gson GSON = new GsonBuilder().create();

    public enum SyncStatus {
        SYNCED,
        PENDING_UPLOAD,
        CONFLICT,
        ERROR
    }

    private String username;
    private int version = 1;
    private long updatedAt = System.currentTimeMillis();
    private SyncStatus syncStatus = SyncStatus.SYNCED;
    private String syncError = null;
    private long lastSyncTimestamp = 0;

    // Learning Goals
    private int dailyStudyMinutes = 30;
    private int weeklyLessonGoal = 10;
    private int xpGoal = 500;
    private List<String> favoriteSubjects = new ArrayList<>();

    // AI Tutor & Pedagogy Preferences
    private String aiTutorLanguage = "en";
    private boolean aiVoiceResponse = false;
    private String aiHelpLevel = "Standard";
    private String aiLearningStyle = "Balanced";
    private String explanationDepth = "Step-by-Step";
    private String practiceDifficulty = "Intermediate";
    private String preferredLessonPresentation = "Interactive & Visual";
    private boolean personalizedRecommendations = true;
    private String aiExplanationLength = "Standard";
    private boolean saveChatHistory = true;

    // Study Reminders & Quiet Hours
    private boolean studyReminder = true;
    private String reminderTime = "17:00";
    private boolean homeworkAlerts = true;
    private boolean examNotifications = true;
    private boolean aiStudyTips = true;
    private boolean motivationEnabled = true;
    private boolean quietHoursEnabled = false;
    private String quietHoursStart = "22:00";
    private String quietHoursEnd = "07:00";

    // Granular Notification Toggles
    private boolean pushEnabled = true;
    private boolean notifyDailyChallenge = true;
    private boolean notifyFriendRequests = true;
    private boolean notifyFriendActivity = true;
    private boolean notifyAchievements = true;
    private boolean notifyXPMilestones = true;
    private boolean notifyStreakAlerts = true;
    private boolean notifyAnnouncements = true;

    // Profile Visibility & Regional Settings
    private boolean profileVisibility = true;
    private boolean shareProgress = true;
    private String language = "en";
    private String region = "US";
    private String dateFormat = "MM/DD/YYYY";

    public StudentAccountPreferences() {
    }

    public StudentAccountPreferences(String username) {
        this.username = username;
    }

    // --- Safe JSON Serialization (Gson) ---

    public String toJson() {
        return GSON.toJson(this);
    }

    public static StudentAccountPreferences fromJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new StudentAccountPreferences();
        }
        try {
            StudentAccountPreferences p = GSON.fromJson(jsonString, StudentAccountPreferences.class);
            return p != null ? p : new StudentAccountPreferences();
        } catch (Exception e) {
            return new StudentAccountPreferences();
        }
    }

    // --- Validated Getters & Setters ---

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = Math.max(1, version); }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public SyncStatus getSyncStatus() { return syncStatus != null ? syncStatus : SyncStatus.SYNCED; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }

    public String getSyncError() { return syncError; }
    public void setSyncError(String syncError) { this.syncError = syncError; }

    public long getLastSyncTimestamp() { return lastSyncTimestamp; }
    public void setLastSyncTimestamp(long lastSyncTimestamp) { this.lastSyncTimestamp = lastSyncTimestamp; }

    public int getDailyStudyMinutes() { return dailyStudyMinutes; }
    public void setDailyStudyMinutes(int dailyStudyMinutes) {
        this.dailyStudyMinutes = Math.max(5, Math.min(480, dailyStudyMinutes));
    }

    public int getWeeklyLessonGoal() { return weeklyLessonGoal; }
    public void setWeeklyLessonGoal(int weeklyLessonGoal) {
        this.weeklyLessonGoal = Math.max(1, Math.min(100, weeklyLessonGoal));
    }

    public int getXpGoal() { return xpGoal; }
    public void setXpGoal(int xpGoal) { this.xpGoal = Math.max(50, xpGoal); }

    public List<String> getFavoriteSubjects() { return favoriteSubjects; }
    public void setFavoriteSubjects(List<String> favoriteSubjects) {
        this.favoriteSubjects = favoriteSubjects != null ? favoriteSubjects : new ArrayList<>();
    }

    public String getAiTutorLanguage() { return aiTutorLanguage != null ? aiTutorLanguage : "en"; }
    public void setAiTutorLanguage(String aiTutorLanguage) { this.aiTutorLanguage = aiTutorLanguage; }

    public boolean isAiVoiceResponse() { return aiVoiceResponse; }
    public void setAiVoiceResponse(boolean aiVoiceResponse) { this.aiVoiceResponse = aiVoiceResponse; }

    public String getAiHelpLevel() { return aiHelpLevel != null ? aiHelpLevel : "Standard"; }
    public void setAiHelpLevel(String aiHelpLevel) { this.aiHelpLevel = aiHelpLevel; }

    public String getAiLearningStyle() { return aiLearningStyle != null ? aiLearningStyle : "Balanced"; }
    public void setAiLearningStyle(String aiLearningStyle) { this.aiLearningStyle = aiLearningStyle; }

    public String getExplanationDepth() { return explanationDepth != null ? explanationDepth : "Step-by-Step"; }
    public void setExplanationDepth(String explanationDepth) { this.explanationDepth = explanationDepth; }

    public String getPracticeDifficulty() { return practiceDifficulty != null ? practiceDifficulty : "Intermediate"; }
    public void setPracticeDifficulty(String practiceDifficulty) { this.practiceDifficulty = practiceDifficulty; }

    public String getPreferredLessonPresentation() { return preferredLessonPresentation != null ? preferredLessonPresentation : "Interactive & Visual"; }
    public void setPreferredLessonPresentation(String p) { this.preferredLessonPresentation = p; }

    public String getAiExplanationLength() { return aiExplanationLength != null ? aiExplanationLength : "Standard"; }
    public void setAiExplanationLength(String length) { this.aiExplanationLength = length != null ? length : "Standard"; }

    public boolean isSaveChatHistory() { return saveChatHistory; }
    public void setSaveChatHistory(boolean saveChatHistory) { this.saveChatHistory = saveChatHistory; }

    public boolean isPersonalizedRecommendations() { return personalizedRecommendations; }
    public void setPersonalizedRecommendations(boolean p) { this.personalizedRecommendations = p; }

    public boolean isStudyReminder() { return studyReminder; }
    public void setStudyReminder(boolean studyReminder) { this.studyReminder = studyReminder; }

    public String getReminderTime() { return reminderTime != null ? reminderTime : "17:00"; }
    public void setReminderTime(String reminderTime) {
        if (reminderTime != null && reminderTime.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            this.reminderTime = reminderTime;
        } else {
            this.reminderTime = "17:00";
        }
    }

    public boolean isHomeworkAlerts() { return homeworkAlerts; }
    public void setHomeworkAlerts(boolean homeworkAlerts) { this.homeworkAlerts = homeworkAlerts; }

    public boolean isExamNotifications() { return examNotifications; }
    public void setExamNotifications(boolean examNotifications) { this.examNotifications = examNotifications; }

    public boolean isAiStudyTips() { return aiStudyTips; }
    public void setAiStudyTips(boolean aiStudyTips) { this.aiStudyTips = aiStudyTips; }

    public boolean isMotivationEnabled() { return motivationEnabled; }
    public void setMotivationEnabled(boolean motivationEnabled) { this.motivationEnabled = motivationEnabled; }

    public boolean isQuietHoursEnabled() { return quietHoursEnabled; }
    public void setQuietHoursEnabled(boolean quietHoursEnabled) { this.quietHoursEnabled = quietHoursEnabled; }

    public String getQuietHoursStart() { return quietHoursStart != null ? quietHoursStart : "22:00"; }
    public void setQuietHoursStart(String s) { this.quietHoursStart = s; }

    public String getQuietHoursEnd() { return quietHoursEnd != null ? quietHoursEnd : "07:00"; }
    public void setQuietHoursEnd(String e) { this.quietHoursEnd = e; }

    public boolean isPushEnabled() { return pushEnabled; }
    public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }

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

    public boolean isProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(boolean profileVisibility) { this.profileVisibility = profileVisibility; }

    public boolean isShareProgress() { return shareProgress; }
    public void setShareProgress(boolean shareProgress) { this.shareProgress = shareProgress; }

    public String getLanguage() { return language != null ? language : "en"; }
    public void setLanguage(String language) { this.language = language; }

    public String getRegion() { return region != null ? region : "US"; }
    public void setRegion(String region) { this.region = region; }

    public String getDateFormat() { return dateFormat != null ? dateFormat : "MM/DD/YYYY"; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
}
