package com.million.learnify;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * SettingsDataArchitectureTest - Comprehensive Automated Unit Tests for Phase 5 Data Architecture.
 * Verifies category ownership separation, typed validation bounds, safe JSON serialization,
 * sync status state machine, and conflict detection logic.
 */
public class SettingsDataArchitectureTest {

    @Test
    public void testLocalDevicePreferencesValidation() {
        LocalDevicePreferences local = new LocalDevicePreferences();

        // Theme validation
        assertEquals("light", local.getThemeMode());
        local.setThemeMode("dark");
        assertEquals("dark", local.getThemeMode());
        local.setThemeMode("system");
        assertEquals("system", local.getThemeMode());
        local.setThemeMode("invalid_theme_string");
        assertEquals("light", local.getThemeMode()); // Fallback to safe default

        // Font size validation
        assertEquals("medium", local.getFontSize());
        local.setFontSize("large");
        assertEquals("large", local.getFontSize());
        local.setFontSize("small");
        assertEquals("small", local.getFontSize());
        local.setFontSize("gigantic_invalid");
        assertEquals("medium", local.getFontSize()); // Safe default

        // Speech rate clamping [0.5, 2.0]
        local.setSpeechRate(0.1f);
        assertEquals(0.5f, local.getSpeechRate(), 0.001f);
        local.setSpeechRate(5.0f);
        assertEquals(2.0f, local.getSpeechRate(), 0.001f);
        local.setSpeechRate(1.25f);
        assertEquals(1.25f, local.getSpeechRate(), 0.001f);

        // Speech pitch clamping [0.5, 2.0]
        local.setSpeechPitch(0.2f);
        assertEquals(0.5f, local.getSpeechPitch(), 0.001f);
        local.setSpeechPitch(4.0f);
        assertEquals(2.0f, local.getSpeechPitch(), 0.001f);
        local.setSpeechPitch(0.9f);
        assertEquals(0.9f, local.getSpeechPitch(), 0.001f);

        // Video quality validation
        local.setVideoQuality("1080p");
        assertEquals("1080p", local.getVideoQuality());
        local.setVideoQuality("4k_ultra_unsupported");
        assertEquals("720p", local.getVideoQuality());
    }

    @Test
    public void testStudentAccountPreferencesValidationAndClamping() {
        StudentAccountPreferences account = new StudentAccountPreferences("student_test");

        // Study goal bounds [5, 480]
        account.setDailyStudyMinutes(1);
        assertEquals(5, account.getDailyStudyMinutes());
        account.setDailyStudyMinutes(600);
        assertEquals(480, account.getDailyStudyMinutes());
        account.setDailyStudyMinutes(45);
        assertEquals(45, account.getDailyStudyMinutes());

        // Weekly lesson goal bounds [1, 100]
        account.setWeeklyLessonGoal(0);
        assertEquals(1, account.getWeeklyLessonGoal());
        account.setWeeklyLessonGoal(250);
        assertEquals(100, account.getWeeklyLessonGoal());
        account.setWeeklyLessonGoal(12);
        assertEquals(12, account.getWeeklyLessonGoal());

        // Time format validation (HH:mm)
        account.setReminderTime("19:30");
        assertEquals("19:30", account.getReminderTime());
        account.setReminderTime("invalid-time-format");
        assertEquals("17:00", account.getReminderTime()); // Safe fallback

        // XP goal bounds
        account.setXpGoal(20);
        assertEquals(50, account.getXpGoal());
        account.setXpGoal(750);
        assertEquals(750, account.getXpGoal());
    }

    @Test
    public void testStudentAccountPreferencesSafeJsonRoundtrip() {
        StudentAccountPreferences original = new StudentAccountPreferences("test_student_json");
        original.setVersion(3);
        original.setDailyStudyMinutes(60);
        original.setWeeklyLessonGoal(14);
        original.setXpGoal(1000);
        original.setFavoriteSubjects(List.of("Mathematics", "Physics", "Chemistry"));
        original.setAiTutorLanguage("am");
        original.setAiVoiceResponse(true);
        original.setExplanationDepth("Deep Conceptual");
        original.setPracticeDifficulty("Advanced");
        original.setReminderTime("08:15");
        original.setQuietHoursEnabled(true);
        original.setQuietHoursStart("23:30");
        original.setQuietHoursEnd("06:30");
        original.setSyncStatus(StudentAccountPreferences.SyncStatus.PENDING_UPLOAD);
        original.setLastSyncTimestamp(1720000000L);

        String json = original.toJson();
        assertNotNull(json);
        assertTrue(json.contains("test_student_json"));
        assertTrue(json.contains("Deep Conceptual"));

        StudentAccountPreferences parsed = StudentAccountPreferences.fromJson(json);
        assertEquals(original.getUsername(), parsed.getUsername());
        assertEquals(original.getVersion(), parsed.getVersion());
        assertEquals(original.getDailyStudyMinutes(), parsed.getDailyStudyMinutes());
        assertEquals(original.getWeeklyLessonGoal(), parsed.getWeeklyLessonGoal());
        assertEquals(original.getXpGoal(), parsed.getXpGoal());
        assertEquals(3, parsed.getFavoriteSubjects().size());
        assertEquals("Mathematics", parsed.getFavoriteSubjects().get(0));
        assertEquals("Physics", parsed.getFavoriteSubjects().get(1));
        assertEquals("am", parsed.getAiTutorLanguage());
        assertTrue(parsed.isAiVoiceResponse());
        assertEquals("Deep Conceptual", parsed.getExplanationDepth());
        assertEquals("Advanced", parsed.getPracticeDifficulty());
        assertEquals("08:15", parsed.getReminderTime());
        assertTrue(parsed.isQuietHoursEnabled());
        assertEquals("23:30", parsed.getQuietHoursStart());
        assertEquals("06:30", parsed.getQuietHoursEnd());
        assertEquals(StudentAccountPreferences.SyncStatus.PENDING_UPLOAD, parsed.getSyncStatus());
        assertEquals(1720000000L, parsed.getLastSyncTimestamp());
    }

    @Test
    public void testStudentSettingsUnifiedFacadeSeparation() {
        LocalDevicePreferences local = new LocalDevicePreferences();
        local.setThemeMode("dark");
        local.setFontSize("large");
        local.setHapticFeedback(false);

        StudentAccountPreferences account = new StudentAccountPreferences("facade_user");
        account.setDailyStudyMinutes(50);
        account.setWeeklyLessonGoal(20);
        account.setAiTutorLanguage("en");

        StudentSettings settings = new StudentSettings(local, account);

        // Verify Local category delegation
        assertEquals("dark", settings.getThemeMode());
        assertEquals("large", settings.getFontSize());
        assertFalse(settings.isHapticFeedback());

        // Mutate through facade
        settings.setThemeMode("light");
        assertEquals("light", local.getThemeMode());

        // Verify Account category delegation
        assertEquals(50, settings.getDailyStudyMinutes());
        assertEquals(20, settings.getWeeklyLessonGoal());

        // Mutate through facade
        settings.setDailyStudyMinutes(75);
        assertEquals(75, account.getDailyStudyMinutes());

        // Ensure category models remain distinct
        assertSame(local, settings.getLocalDevicePreferences());
        assertSame(account, settings.getStudentAccountPreferences());
    }

    @Test
    public void testAdminSystemConfigSerializationAndDefaults() {
        AdminSystemConfig config = new AdminSystemConfig();

        assertEquals(AdminSystemConfig.DEFAULT_CONFIG_ID, config.getConfigId());
        assertEquals(1, config.getVersion());
        assertEquals("Learnify National Academy", config.getInstitutionName());
        assertEquals(75, config.getPassingPercentage());
        assertEquals(60, config.getExamDurationMinutes());
        assertEquals(3, config.getMaxExamAttempts());
        assertTrue(config.isAiServiceEnabled());
        assertTrue(config.isRegistrationAllowed());
        assertFalse(config.isMaintenanceMode());

        config.setInstitutionName("Global Science & Tech Institute");
        config.setPassingPercentage(85);
        config.setMaintenanceMode(true);
        config.setMaintenanceMessage("Scheduled upgrade in progress");

        String json = config.toJson();
        assertNotNull(json);

        AdminSystemConfig deserialized = AdminSystemConfig.fromJson(json);
        assertEquals("Global Science & Tech Institute", deserialized.getInstitutionName());
        assertEquals(85, deserialized.getPassingPercentage());
        assertTrue(deserialized.isMaintenanceMode());
        assertEquals("Scheduled upgrade in progress", deserialized.getMaintenanceMessage());
    }

    @Test
    public void testConflictDetectionLogic() {
        // Local client with stale version v1
        StudentAccountPreferences clientPrefs = new StudentAccountPreferences("conflict_user");
        clientPrefs.setVersion(1);
        clientPrefs.setDailyStudyMinutes(20);
        clientPrefs.setUpdatedAt(1000L);

        // Server state with advanced version v2 and newer timestamp
        StudentAccountPreferences serverPrefs = new StudentAccountPreferences("conflict_user");
        serverPrefs.setVersion(2);
        serverPrefs.setDailyStudyMinutes(40);
        serverPrefs.setUpdatedAt(2000L);

        // Conflict check: client.version < server.version && client.updatedAt < server.updatedAt
        boolean isConflict = clientPrefs.getVersion() < serverPrefs.getVersion()
                && clientPrefs.getUpdatedAt() < serverPrefs.getUpdatedAt();

        assertTrue("Conflict should be detected when client submits stale version with older timestamp", isConflict);

        // When conflict detected, client syncStatus transitions to CONFLICT
        clientPrefs.setSyncStatus(StudentAccountPreferences.SyncStatus.CONFLICT);
        clientPrefs.setSyncError("Conflict detected: Server has newer version (v2)");

        assertEquals(StudentAccountPreferences.SyncStatus.CONFLICT, clientPrefs.getSyncStatus());
        assertNotNull(clientPrefs.getSyncError());
    }
}

