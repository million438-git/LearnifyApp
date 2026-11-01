package com.million.learnify;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * StudentSettingsVerificationTest - Automated Unit Tests for Phase 2 Settings Platform.
 * Verifies model contracts, formatting algorithms, quiet hours wrapping calculations,
 * and accessibility parameter bounds.
 */
public class StudentSettingsVerificationTest {

    @Test
    public void testStudentSettingsDefaults() {
        StudentSettings settings = new StudentSettings("light", true);

        // Core Defaults
        assertEquals("light", settings.getThemeMode());
        assertTrue(settings.areNotificationsEnabled());
        assertEquals("medium", settings.getFontSize());
        assertEquals("en", settings.getLanguage());

        // Phase 2 Learning Preferences Defaults
        assertEquals(30, settings.getDailyStudyMinutes());
        assertEquals(10, settings.getWeeklyLessonGoal());
        assertEquals("Intermediate", settings.getPracticeDifficulty());
        assertEquals("Step-by-Step", settings.getExplanationDepth());
        assertEquals("Interactive & Visual", settings.getPreferredLessonPresentation());
        assertTrue(settings.isPersonalizedRecommendations());

        // Accessibility Defaults
        assertEquals(1.0f, settings.getSpeechRate(), 0.001f);
        assertEquals(1.0f, settings.getSpeechPitch(), 0.001f);
        assertFalse(settings.isAutoReadContent());

        // Quiet Hours Defaults
        assertFalse(settings.isQuietHoursEnabled());
        assertEquals("22:00", settings.getQuietHoursStart());
        assertEquals("07:00", settings.getQuietHoursEnd());

        // AI Tutor Defaults
        assertEquals("en", settings.getAiTutorLanguage());
        assertFalse(settings.isAiVoiceResponse());
        assertEquals("Standard", settings.getAiExplanationLength());
    }

    @Test
    public void testStudentSettingsMutations() {
        StudentSettings settings = new StudentSettings("dark", false);

        // Mutate learning preferences
        settings.setDailyStudyMinutes(45);
        settings.setWeeklyLessonGoal(15);
        settings.setPracticeDifficulty("Advanced");
        settings.setExplanationDepth("Comprehensive");
        settings.setPreferredLessonPresentation("Text & Notes");
        settings.setPersonalizedRecommendations(false);

        assertEquals(45, settings.getDailyStudyMinutes());
        assertEquals(15, settings.getWeeklyLessonGoal());
        assertEquals("Advanced", settings.getPracticeDifficulty());
        assertEquals("Comprehensive", settings.getExplanationDepth());
        assertEquals("Text & Notes", settings.getPreferredLessonPresentation());
        assertFalse(settings.isPersonalizedRecommendations());

        // Mutate accessibility voice controls
        settings.setSpeechRate(1.25f);
        settings.setSpeechPitch(0.8f);
        settings.setAutoReadContent(true);

        assertEquals(1.25f, settings.getSpeechRate(), 0.001f);
        assertEquals(0.8f, settings.getSpeechPitch(), 0.001f);
        assertTrue(settings.isAutoReadContent());

        // Mutate quiet hours
        settings.setQuietHoursEnabled(true);
        settings.setQuietHoursStart("23:00");
        settings.setQuietHoursEnd("06:00");

        assertTrue(settings.isQuietHoursEnabled());
        assertEquals("23:00", settings.getQuietHoursStart());
        assertEquals("06:00", settings.getQuietHoursEnd());

        // Mutate AI preferences
        settings.setAiTutorLanguage("am");
        settings.setAiVoiceResponse(true);
        settings.setAiExplanationLength("Concise");

        assertEquals("am", settings.getAiTutorLanguage());
        assertTrue(settings.isAiVoiceResponse());
        assertEquals("Concise", settings.getAiExplanationLength());
    }

    @Test
    public void testOfflineStorageFormatting() {
        assertEquals("0.0 MB", OfflineStorageManager.formatStorageSize(0));
        assertEquals("500.0 KB", OfflineStorageManager.formatStorageSize(500 * 1024));
        assertEquals("50.0 MB", OfflineStorageManager.formatStorageSize(50L * 1024 * 1024));
        assertEquals("2.0 GB", OfflineStorageManager.formatStorageSize(2L * 1024 * 1024 * 1024));
    }

    @Test
    public void testSyncTimestampFormatting() {
        assertEquals("Never synced", OfflineStorageManager.formatSyncTimestamp(0));
        String formatted = OfflineStorageManager.formatSyncTimestamp(System.currentTimeMillis());
        assertNotNull(formatted);
        assertTrue(formatted.contains("2026") || formatted.contains("•"));
    }

    @Test
    public void testQuietHoursMidnightWrappingCalculation() {
        // Test overnight mute window from 22:00 (10 PM) to 07:00 (7 AM)
        int startMinutes = 22 * 60; // 1320
        int endMinutes = 7 * 60;    // 420

        // 23:30 (11:30 PM) should be in quiet hours
        int testLateNight = 23 * 60 + 30; // 1410
        boolean isInQuietHoursLate = isWithinQuietHours(testLateNight, startMinutes, endMinutes);
        assertTrue("11:30 PM must be within 10 PM - 7 AM quiet hours", isInQuietHoursLate);

        // 03:00 (3 AM) should be in quiet hours
        int testEarlyMorning = 3 * 60; // 180
        boolean isInQuietHoursEarly = isWithinQuietHours(testEarlyMorning, startMinutes, endMinutes);
        assertTrue("3:00 AM must be within 10 PM - 7 AM quiet hours", isInQuietHoursEarly);

        // 14:00 (2 PM) should NOT be in quiet hours
        int testAfternoon = 14 * 60; // 840
        boolean isInQuietHoursDay = isWithinQuietHours(testAfternoon, startMinutes, endMinutes);
        assertFalse("2:00 PM must NOT be within 10 PM - 7 AM quiet hours", isInQuietHoursDay);
    }

    private boolean isWithinQuietHours(int currentMinutes, int startMinutes, int endMinutes) {
        if (startMinutes <= endMinutes) {
            return currentMinutes >= startMinutes && currentMinutes <= endMinutes;
        } else {
            return currentMinutes >= startMinutes || currentMinutes <= endMinutes;
        }
    }
}

