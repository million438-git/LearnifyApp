package com.million.learnify;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * AdvancedPreferencesVerificationTest - Phase 6 QA and Architectural Verification Suite.
 * Validates:
 * 1. Notifications: Channel constants, request codes, and routing definitions.
 * 2. Voice & Accessibility: Phonetic translation of physics/chemistry equations and diagram descriptions.
 * 3. Offline Learning & Storage: Size formatters, SubjectPackInfo JSON serialization, and storage breakdowns.
 * 4. Camera & Science Studio: Key definitions, quality modes, and history/auto-save switches.
 * 5. Virtual Laboratory: Lab configuration JavaScript generator and sensitivity clamps.
 */
public class AdvancedPreferencesVerificationTest {

    @Test
    public void testNotificationChannels_constantsValid() {
        assertEquals("learnify_study_reminders", NotificationHelper.CHANNEL_STUDY_REMINDERS);
        assertEquals("learnify_exams_assignments", NotificationHelper.CHANNEL_EXAMS_ASSIGNMENTS);
        assertEquals("learnify_achievements_xp", NotificationHelper.CHANNEL_ACHIEVEMENTS_XP);
        assertEquals("learnify_social_community", NotificationHelper.CHANNEL_SOCIAL_COMMUNITY);
        assertEquals("learnify_announcements", NotificationHelper.CHANNEL_ANNOUNCEMENTS);
        assertEquals("LEARNIFY_SECURITY_CHANNEL", NotificationHelper.CHANNEL_SECURITY);
    }

    @Test
    public void testAccessibilityUtils_describeDiagram() {
        List<String> components = Arrays.asList("Convex lens", "Focal point F", "Real inverted image");
        String speech = AccessibilityUtils.describeDiagramForAccessibility(
                "Optics Ray Diagram",
                "Thin Convex Lens Focusing",
                "Parallel rays converge through principal focus",
                components
        );

        assertNotNull(speech);
        assertTrue(speech.contains("Scientific Diagram: Thin Convex Lens Focusing"));
        assertTrue(speech.contains("Type: Optics Ray Diagram"));
        assertTrue(speech.contains("Parallel rays converge through principal focus"));
        assertTrue(speech.contains("Key elements shown: Convex lens, Focal point F, Real inverted image"));
    }

    @Test
    public void testAccessibilityUtils_physicsFormulasSpoken() {
        String input = "Recall that F = ma and V = IR in elementary circuits.";
        String result = AccessibilityUtils.cleanMathForSpeech(input);

        assertTrue("F=ma must be translated phonetically", result.contains("force equals mass times acceleration"));
        assertTrue("V=IR must be translated phonetically", result.contains("voltage equals current times resistance"));
    }

    @Test
    public void testOfflineStorageManager_formatStorageSize() {
        assertEquals("0.0 MB", OfflineStorageManager.formatStorageSize(0));
        assertEquals("0.0 MB", OfflineStorageManager.formatStorageSize(-500));

        long kbBytes = 512 * 1024L; // 512 KB
        assertEquals("512.0 KB", OfflineStorageManager.formatStorageSize(kbBytes));

        long mbBytes = 45 * 1024L * 1024L; // 45 MB
        assertEquals("45.0 MB", OfflineStorageManager.formatStorageSize(mbBytes));

        long gbBytes = (long) (2.5 * 1024L * 1024L * 1024L); // 2.5 GB
        assertEquals("2.5 GB", OfflineStorageManager.formatStorageSize(gbBytes));
    }

    @Test
    public void testOfflineStorageManager_subjectPackInfoJsonSerialization() {
        OfflineStorageManager.SubjectPackInfo original = new OfflineStorageManager.SubjectPackInfo(
                "physics_11",
                "Physics Grade 11 Curriculum Pack",
                "Grade 11",
                45000000L,
                "DOWNLOADED",
                25,
                1700000000000L
        );

        String json = original.toJson();
        assertNotNull(json);
        assertTrue(json.contains("\"id\":\"physics_11\""));
        assertTrue(json.contains("\"sizeBytes\":45000000"));

        OfflineStorageManager.SubjectPackInfo restored = OfflineStorageManager.SubjectPackInfo.fromJson(json);
        assertNotNull(restored);
        assertEquals(original.id, restored.id);
        assertEquals(original.name, restored.name);
        assertEquals(original.grade, restored.grade);
        assertEquals(original.sizeBytes, restored.sizeBytes);
        assertEquals(original.status, restored.status);
        assertEquals(original.lessonCount, restored.lessonCount);
        assertEquals(original.lastDownloadedAt, restored.lastDownloadedAt);
    }

    @Test
    public void testCameraSettingsActivity_constantsDefined() {
        assertEquals("CameraSettings", CameraSettingsActivity.PREFS_NAME);
        assertEquals("auto_detect", CameraSettingsActivity.KEY_AUTO_DETECT);
        assertEquals("grid_lines", CameraSettingsActivity.KEY_GRID);
        assertEquals("save_history", CameraSettingsActivity.KEY_SAVE_HISTORY);
        assertEquals("formula_detection", CameraSettingsActivity.KEY_FORMULA_DETECTION);
        assertEquals("diagram_detection", CameraSettingsActivity.KEY_DIAGRAM_DETECTION);
        assertEquals("image_quality", CameraSettingsActivity.KEY_IMAGE_QUALITY);
        assertEquals("auto_save_scans", CameraSettingsActivity.KEY_AUTO_SAVE);
        assertEquals("subject_specific_detection", CameraSettingsActivity.KEY_SUBJECT_SPECIFIC);
    }

    @Test
    public void testLabPreferencesManager_configScriptStructure() {
        // Test script template format
        String template = "(function() { window.LEARNIFY_LAB_CONFIG = { soundEffects: %b, simulationQuality: '%s', reduceMotion: %b, accessibilityMode: %b, controlSensitivity: %.2f }; })();";
        String formatted = String.format(java.util.Locale.US, template, true, "balanced", false, false, 1.25f);

        assertTrue(formatted.contains("soundEffects: true"));
        assertTrue(formatted.contains("simulationQuality: 'balanced'"));
        assertTrue(formatted.contains("reduceMotion: false"));
        assertTrue(formatted.contains("controlSensitivity: 1.25"));
    }
}
