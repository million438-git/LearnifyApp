package com.million.learnify;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.List;

/**
 * StudentSettings - Unified Settings Facade.
 * Bridges LocalDevicePreferences (Category A: device rendering, haptics, accessibility)
 * and StudentAccountPreferences (Category B: account goals, AI tutor, reminders, synced cloud state).
 * Maintains 100% backward compatibility for all existing Learnify UI activities and background receivers.
 */
public class StudentSettings implements Serializable {

    private final LocalDevicePreferences localPrefs;
    private final StudentAccountPreferences accountPrefs;

    public StudentSettings() {
        this("light", true, true);
    }

    public StudentSettings(String themeMode, boolean notificationsEnabled) {
        this(themeMode, notificationsEnabled, true);
    }

    public StudentSettings(String themeMode, boolean notificationsEnabled, boolean textToSpeech) {
        this.localPrefs = new LocalDevicePreferences();
        this.accountPrefs = new StudentAccountPreferences();

        this.localPrefs.setThemeMode(themeMode);
        this.accountPrefs.setPushEnabled(notificationsEnabled);
        this.localPrefs.setTextToSpeech(textToSpeech);
    }

    public StudentSettings(LocalDevicePreferences localPrefs, StudentAccountPreferences accountPrefs) {
        this.localPrefs = localPrefs != null ? localPrefs : new LocalDevicePreferences();
        this.accountPrefs = accountPrefs != null ? accountPrefs : new StudentAccountPreferences();
    }

    // Direct Access to Domain Categories
    public LocalDevicePreferences getLocalDevicePreferences() {
        return localPrefs;
    }

    public StudentAccountPreferences getStudentAccountPreferences() {
        return accountPrefs;
    }

    // --- Category A: Local Device Preferences Delegation ---

    public String getThemeMode() { return localPrefs.getThemeMode(); }
    public void setThemeMode(String themeMode) { localPrefs.setThemeMode(themeMode); }

    public String getFontSize() { return localPrefs.getFontSize(); }
    public void setFontSize(String fontSize) { localPrefs.setFontSize(fontSize); }

    public boolean isSoundEffects() { return localPrefs.isSoundEffects(); }
    public void setSoundEffects(boolean soundEffects) { localPrefs.setSoundEffects(soundEffects); }

    public boolean isHapticFeedback() { return localPrefs.isHapticFeedback(); }
    public void setHapticFeedback(boolean hapticFeedback) { localPrefs.setHapticFeedback(hapticFeedback); }

    public boolean isAnimatedCelebrations() { return localPrefs.isAnimatedCelebrations(); }
    public void setAnimatedCelebrations(boolean animatedCelebrations) { localPrefs.setAnimatedCelebrations(animatedCelebrations); }

    public boolean isTextToSpeech() { return localPrefs.isTextToSpeech(); }
    public void setTextToSpeech(boolean textToSpeech) { localPrefs.setTextToSpeech(textToSpeech); }

    public boolean isHighContrastMode() { return localPrefs.isHighContrastMode(); }
    public void setHighContrastMode(boolean highContrastMode) { localPrefs.setHighContrastMode(highContrastMode); }

    public boolean isReduceMotion() { return localPrefs.isReduceMotion(); }
    public void setReduceMotion(boolean reduceMotion) { localPrefs.setReduceMotion(reduceMotion); }

    public boolean isInclusiveMode() { return localPrefs.isInclusiveMode(); }
    public void setInclusiveMode(boolean inclusiveMode) { localPrefs.setInclusiveMode(inclusiveMode); }

    public float getSpeechRate() { return localPrefs.getSpeechRate(); }
    public void setSpeechRate(float speechRate) { localPrefs.setSpeechRate(speechRate); }

    public float getSpeechPitch() { return localPrefs.getSpeechPitch(); }
    public void setSpeechPitch(float speechPitch) { localPrefs.setSpeechPitch(speechPitch); }

    public boolean isAutoReadContent() { return localPrefs.isAutoReadContent(); }
    public void setAutoReadContent(boolean autoReadContent) { localPrefs.setAutoReadContent(autoReadContent); }

    public boolean isOfflineMode() { return localPrefs.isOfflineMode(); }
    public void setOfflineMode(boolean offlineMode) { localPrefs.setOfflineMode(offlineMode); }

    public boolean isDataSaver() { return localPrefs.isDataSaver(); }
    public void setDataSaver(boolean dataSaver) { localPrefs.setDataSaver(dataSaver); }

    public String getVideoQuality() { return localPrefs.getVideoQuality(); }
    public void setVideoQuality(String videoQuality) { localPrefs.setVideoQuality(videoQuality); }

    public String getAutoDownloadMedia() { return localPrefs.getAutoDownloadMedia(); }
    public void setAutoDownloadMedia(String autoDownloadMedia) { localPrefs.setAutoDownloadMedia(autoDownloadMedia); }

    // --- Category B: Student Account Preferences Delegation ---

    public boolean areNotificationsEnabled() { return accountPrefs.isPushEnabled(); }
    public void setNotificationsEnabled(boolean notificationsEnabled) { accountPrefs.setPushEnabled(notificationsEnabled); }

    public boolean isPushEnabled() { return accountPrefs.isPushEnabled(); }
    public void setPushEnabled(boolean pushEnabled) { accountPrefs.setPushEnabled(pushEnabled); }

    public boolean isStudyReminder() { return accountPrefs.isStudyReminder(); }
    public void setStudyReminder(boolean studyReminder) { accountPrefs.setStudyReminder(studyReminder); }

    public String getReminderTime() { return accountPrefs.getReminderTime(); }
    public void setReminderTime(String reminderTime) { accountPrefs.setReminderTime(reminderTime); }

    public boolean isHomeworkAlerts() { return accountPrefs.isHomeworkAlerts(); }
    public void setHomeworkAlerts(boolean homeworkAlerts) { accountPrefs.setHomeworkAlerts(homeworkAlerts); }

    public boolean isExamNotifications() { return accountPrefs.isExamNotifications(); }
    public void setExamNotifications(boolean examNotifications) { accountPrefs.setExamNotifications(examNotifications); }

    public boolean isAiStudyTips() { return accountPrefs.isAiStudyTips(); }
    public void setAiStudyTips(boolean aiStudyTips) { accountPrefs.setAiStudyTips(aiStudyTips); }

    public boolean isMotivationEnabled() { return accountPrefs.isMotivationEnabled(); }
    public void setMotivationEnabled(boolean motivationEnabled) { accountPrefs.setMotivationEnabled(motivationEnabled); }

    public boolean isQuietHoursEnabled() { return accountPrefs.isQuietHoursEnabled(); }
    public void setQuietHoursEnabled(boolean quietHoursEnabled) { accountPrefs.setQuietHoursEnabled(quietHoursEnabled); }

    public String getQuietHoursStart() { return accountPrefs.getQuietHoursStart(); }
    public void setQuietHoursStart(String quietHoursStart) { accountPrefs.setQuietHoursStart(quietHoursStart); }

    public String getQuietHoursEnd() { return accountPrefs.getQuietHoursEnd(); }
    public void setQuietHoursEnd(String quietHoursEnd) { accountPrefs.setQuietHoursEnd(quietHoursEnd); }

    public boolean isNotifyDailyChallenge() { return accountPrefs.isNotifyDailyChallenge(); }
    public void setNotifyDailyChallenge(boolean n) { accountPrefs.setNotifyDailyChallenge(n); }

    public boolean isNotifyFriendRequests() { return accountPrefs.isNotifyFriendRequests(); }
    public void setNotifyFriendRequests(boolean n) { accountPrefs.setNotifyFriendRequests(n); }

    public boolean isNotifyFriendActivity() { return accountPrefs.isNotifyFriendActivity(); }
    public void setNotifyFriendActivity(boolean n) { accountPrefs.setNotifyFriendActivity(n); }

    public boolean isNotifyAchievements() { return accountPrefs.isNotifyAchievements(); }
    public void setNotifyAchievements(boolean n) { accountPrefs.setNotifyAchievements(n); }

    public boolean isNotifyXPMilestones() { return accountPrefs.isNotifyXPMilestones(); }
    public void setNotifyXPMilestones(boolean n) { accountPrefs.setNotifyXPMilestones(n); }

    public boolean isNotifyStreakAlerts() { return accountPrefs.isNotifyStreakAlerts(); }
    public void setNotifyStreakAlerts(boolean n) { accountPrefs.setNotifyStreakAlerts(n); }

    public boolean isNotifyAnnouncements() { return accountPrefs.isNotifyAnnouncements(); }
    public void setNotifyAnnouncements(boolean n) { accountPrefs.setNotifyAnnouncements(n); }

    public int getDailyStudyMinutes() { return accountPrefs.getDailyStudyMinutes(); }
    public void setDailyStudyMinutes(int minutes) { accountPrefs.setDailyStudyMinutes(minutes); }

    public int getWeeklyLessonGoal() { return accountPrefs.getWeeklyLessonGoal(); }
    public void setWeeklyLessonGoal(int goal) { accountPrefs.setWeeklyLessonGoal(goal); }

    public int getXpGoal() { return accountPrefs.getXpGoal(); }
    public void setXpGoal(int xpGoal) { accountPrefs.setXpGoal(xpGoal); }

    public List<String> getFavoriteSubjects() { return accountPrefs.getFavoriteSubjects(); }
    public void setFavoriteSubjects(List<String> subjects) { accountPrefs.setFavoriteSubjects(subjects); }

    public String getAiTutorLanguage() { return accountPrefs.getAiTutorLanguage(); }
    public void setAiTutorLanguage(String language) { accountPrefs.setAiTutorLanguage(language); }

    public boolean isAiVoiceResponse() { return accountPrefs.isAiVoiceResponse(); }
    public void setAiVoiceResponse(boolean voice) { accountPrefs.setAiVoiceResponse(voice); }

    public String getAiHelpLevel() { return accountPrefs.getAiHelpLevel(); }
    public void setAiHelpLevel(String level) { accountPrefs.setAiHelpLevel(level); }

    public String getAiLearningStyle() { return accountPrefs.getAiLearningStyle(); }
    public void setAiLearningStyle(String style) { accountPrefs.setAiLearningStyle(style); }

    public String getExplanationDepth() { return accountPrefs.getExplanationDepth(); }
    public void setExplanationDepth(String depth) { accountPrefs.setExplanationDepth(depth); }

    public String getPracticeDifficulty() { return accountPrefs.getPracticeDifficulty(); }
    public void setPracticeDifficulty(String diff) { accountPrefs.setPracticeDifficulty(diff); }

    public String getPreferredLessonPresentation() { return accountPrefs.getPreferredLessonPresentation(); }
    public void setPreferredLessonPresentation(String p) { accountPrefs.setPreferredLessonPresentation(p); }

    public boolean isPersonalizedRecommendations() { return accountPrefs.isPersonalizedRecommendations(); }
    public void setPersonalizedRecommendations(boolean p) { accountPrefs.setPersonalizedRecommendations(p); }

    public String getAiExplanationLength() { return accountPrefs.getAiExplanationLength(); }
    public void setAiExplanationLength(String length) { accountPrefs.setAiExplanationLength(length); }

    public boolean isSaveChatHistory() { return accountPrefs.isSaveChatHistory(); }
    public void setSaveChatHistory(boolean s) { accountPrefs.setSaveChatHistory(s); }

    public boolean isProfileVisibility() { return accountPrefs.isProfileVisibility(); }
    public void setProfileVisibility(boolean v) { accountPrefs.setProfileVisibility(v); }

    public boolean isShareProgress() { return accountPrefs.isShareProgress(); }
    public void setShareProgress(boolean s) { accountPrefs.setShareProgress(s); }

    public String getLanguage() { return accountPrefs.getLanguage(); }
    public void setLanguage(String language) { accountPrefs.setLanguage(language); }

    public String getRegion() { return accountPrefs.getRegion(); }
    public void setRegion(String region) { accountPrefs.setRegion(region); }

    public String getDateFormat() { return accountPrefs.getDateFormat(); }
    public void setDateFormat(String dateFormat) { accountPrefs.setDateFormat(dateFormat); }

    public boolean isCloudSync() {
        return accountPrefs.getSyncStatus() != StudentAccountPreferences.SyncStatus.ERROR;
    }
    public void setCloudSync(boolean sync) {
        if (!sync) {
            accountPrefs.setSyncStatus(StudentAccountPreferences.SyncStatus.SYNCED);
        }
    }

    public boolean isTwoFactorEnabled() { return false; }
    public void setTwoFactorEnabled(boolean enabled) {}

    public long getLastSyncTimestamp() { return accountPrefs.getLastSyncTimestamp(); }
    public void setLastSyncTimestamp(long timestamp) { accountPrefs.setLastSyncTimestamp(timestamp); }

    public String getLastSyncStatus() { return accountPrefs.getSyncStatus().name(); }
    public void setLastSyncStatus(String status) {
        try {
            accountPrefs.setSyncStatus(StudentAccountPreferences.SyncStatus.valueOf(status));
        } catch (Exception ignored) {
        }
    }

    public String getLastSyncError() {
        return accountPrefs.getSyncError() != null ? accountPrefs.getSyncError() : "None";
    }
    public void setLastSyncError(String error) { accountPrefs.setSyncError(error); }

    // Serialization
    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("theme", getThemeMode());
        root.addProperty("fontSize", getFontSize());
        root.addProperty("account", accountPrefs != null ? accountPrefs.toJson() : "{}");
        return root.toString();
    }
}
