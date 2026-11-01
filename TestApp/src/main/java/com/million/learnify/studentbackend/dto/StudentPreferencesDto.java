package com.million.learnify.studentbackend.dto;

import java.io.Serializable;
import java.util.List;

/**
 * StudentPreferencesDto - Account-Scoped Preferences Synced Across Student Devices.
 */
public class StudentPreferencesDto implements Serializable {

    private String username;
    private int version = 1;
    private long updatedAt = System.currentTimeMillis();

    // Learning Goals
    private int dailyStudyMinutes = 30;
    private int weeklyLessonGoal = 10;
    private int xpGoal = 500;
    private List<String> favoriteSubjects;

    // AI Tutor Preferences
    private String aiTutorLanguage = "en";
    private boolean aiVoiceResponse = false;
    private String aiHelpLevel = "Standard";
    private String aiLearningStyle = "Balanced";
    private String explanationDepth = "Step-by-Step";
    private String practiceDifficulty = "Intermediate";
    private String preferredLessonPresentation = "Interactive & Visual";
    private boolean personalizedRecommendations = true;

    // Study & Notification Reminders
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

    // Profile Visibility & Regional Formatting
    private boolean profileVisibility = true;
    private boolean shareProgress = true;
    private String language = "en";
    private String region = "US";
    private String dateFormat = "MM/DD/YYYY";

    public StudentPreferencesDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getDailyStudyMinutes() {
        return dailyStudyMinutes;
    }

    public void setDailyStudyMinutes(int dailyStudyMinutes) {
        this.dailyStudyMinutes = dailyStudyMinutes;
    }

    public int getWeeklyLessonGoal() {
        return weeklyLessonGoal;
    }

    public void setWeeklyLessonGoal(int weeklyLessonGoal) {
        this.weeklyLessonGoal = weeklyLessonGoal;
    }

    public int getXpGoal() {
        return xpGoal;
    }

    public void setXpGoal(int xpGoal) {
        this.xpGoal = xpGoal;
    }

    public List<String> getFavoriteSubjects() {
        return favoriteSubjects;
    }

    public void setFavoriteSubjects(List<String> favoriteSubjects) {
        this.favoriteSubjects = favoriteSubjects;
    }

    public String getAiTutorLanguage() {
        return aiTutorLanguage;
    }

    public void setAiTutorLanguage(String aiTutorLanguage) {
        this.aiTutorLanguage = aiTutorLanguage;
    }

    public boolean isAiVoiceResponse() {
        return aiVoiceResponse;
    }

    public void setAiVoiceResponse(boolean aiVoiceResponse) {
        this.aiVoiceResponse = aiVoiceResponse;
    }

    public String getAiHelpLevel() {
        return aiHelpLevel;
    }

    public void setAiHelpLevel(String aiHelpLevel) {
        this.aiHelpLevel = aiHelpLevel;
    }

    public String getAiLearningStyle() {
        return aiLearningStyle;
    }

    public void setAiLearningStyle(String aiLearningStyle) {
        this.aiLearningStyle = aiLearningStyle;
    }

    public String getExplanationDepth() {
        return explanationDepth;
    }

    public void setExplanationDepth(String explanationDepth) {
        this.explanationDepth = explanationDepth;
    }

    public String getPracticeDifficulty() {
        return practiceDifficulty;
    }

    public void setPracticeDifficulty(String practiceDifficulty) {
        this.practiceDifficulty = practiceDifficulty;
    }

    public String getPreferredLessonPresentation() {
        return preferredLessonPresentation;
    }

    public void setPreferredLessonPresentation(String preferredLessonPresentation) {
        this.preferredLessonPresentation = preferredLessonPresentation;
    }

    public boolean isPersonalizedRecommendations() {
        return personalizedRecommendations;
    }

    public void setPersonalizedRecommendations(boolean personalizedRecommendations) {
        this.personalizedRecommendations = personalizedRecommendations;
    }

    public boolean isStudyReminder() {
        return studyReminder;
    }

    public void setStudyReminder(boolean studyReminder) {
        this.studyReminder = studyReminder;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isHomeworkAlerts() {
        return homeworkAlerts;
    }

    public void setHomeworkAlerts(boolean homeworkAlerts) {
        this.homeworkAlerts = homeworkAlerts;
    }

    public boolean isExamNotifications() {
        return examNotifications;
    }

    public void setExamNotifications(boolean examNotifications) {
        this.examNotifications = examNotifications;
    }

    public boolean isAiStudyTips() {
        return aiStudyTips;
    }

    public void setAiStudyTips(boolean aiStudyTips) {
        this.aiStudyTips = aiStudyTips;
    }

    public boolean isMotivationEnabled() {
        return motivationEnabled;
    }

    public void setMotivationEnabled(boolean motivationEnabled) {
        this.motivationEnabled = motivationEnabled;
    }

    public boolean isQuietHoursEnabled() {
        return quietHoursEnabled;
    }

    public void setQuietHoursEnabled(boolean quietHoursEnabled) {
        this.quietHoursEnabled = quietHoursEnabled;
    }

    public String getQuietHoursStart() {
        return quietHoursStart;
    }

    public void setQuietHoursStart(String quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }

    public String getQuietHoursEnd() {
        return quietHoursEnd;
    }

    public void setQuietHoursEnd(String quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public boolean isNotifyDailyChallenge() {
        return notifyDailyChallenge;
    }

    public void setNotifyDailyChallenge(boolean notifyDailyChallenge) {
        this.notifyDailyChallenge = notifyDailyChallenge;
    }

    public boolean isNotifyFriendRequests() {
        return notifyFriendRequests;
    }

    public void setNotifyFriendRequests(boolean notifyFriendRequests) {
        this.notifyFriendRequests = notifyFriendRequests;
    }

    public boolean isNotifyFriendActivity() {
        return notifyFriendActivity;
    }

    public void setNotifyFriendActivity(boolean notifyFriendActivity) {
        this.notifyFriendActivity = notifyFriendActivity;
    }

    public boolean isNotifyAchievements() {
        return notifyAchievements;
    }

    public void setNotifyAchievements(boolean notifyAchievements) {
        this.notifyAchievements = notifyAchievements;
    }

    public boolean isNotifyXPMilestones() {
        return notifyXPMilestones;
    }

    public void setNotifyXPMilestones(boolean notifyXPMilestones) {
        this.notifyXPMilestones = notifyXPMilestones;
    }

    public boolean isNotifyStreakAlerts() {
        return notifyStreakAlerts;
    }

    public void setNotifyStreakAlerts(boolean notifyStreakAlerts) {
        this.notifyStreakAlerts = notifyStreakAlerts;
    }

    public boolean isNotifyAnnouncements() {
        return notifyAnnouncements;
    }

    public void setNotifyAnnouncements(boolean notifyAnnouncements) {
        this.notifyAnnouncements = notifyAnnouncements;
    }

    public boolean isProfileVisibility() {
        return profileVisibility;
    }

    public void setProfileVisibility(boolean profileVisibility) {
        this.profileVisibility = profileVisibility;
    }

    public boolean isShareProgress() {
        return shareProgress;
    }

    public void setShareProgress(boolean shareProgress) {
        this.shareProgress = shareProgress;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}

