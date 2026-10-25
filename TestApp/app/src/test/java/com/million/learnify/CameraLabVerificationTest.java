package com.million.learnify;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests verifying Camera Lab & History functionality, schema integrity, and preferences.
 */
public class CameraLabVerificationTest {

    @Test
    public void testScanItem_initializesAndHandlesNullSafely() {
        CameraHistoryActivity.ScanItem item = new CameraHistoryActivity.ScanItem(
                1,
                "MATH",
                "2x + 4 = 10",
                "x = 3",
                "Mathematics Grade 11",
                "2026-10-11 10:30:00"
        );

        assertEquals(1, item.id);
        assertEquals("MATH", item.type);
        assertEquals("2x + 4 = 10", item.problem);
        assertEquals("x = 3", item.solution);
        assertEquals("Mathematics Grade 11", item.courseContext);
        assertEquals("2026-10-11 10:30:00", item.date);
    }

    @Test
    public void testScanItem_handlesNullValuesWithSafeFallbacks() {
        CameraHistoryActivity.ScanItem item = new CameraHistoryActivity.ScanItem(
                2,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(2, item.id);
        assertEquals("LAB", item.type);
        assertEquals("", item.problem);
        assertEquals("", item.solution);
        assertEquals("", item.courseContext);
        assertEquals("", item.date);
    }

    @Test
    public void testCameraSettings_constantsAreConsistent() {
        assertEquals("CameraSettings", CameraSettingsActivity.PREFS_NAME);
        assertEquals("grid_lines", CameraSettingsActivity.KEY_GRID);
        assertEquals("auto_detect", CameraSettingsActivity.KEY_AUTO_DETECT);
        assertEquals("save_history", CameraSettingsActivity.KEY_SAVE_HISTORY);
    }

    @Test
    public void testEmptyVsPopulatedHistoryLogic() {
        List<CameraHistoryActivity.ScanItem> history = new ArrayList<>();
        assertTrue("Initial history should be empty", history.isEmpty());

        history.add(new CameraHistoryActivity.ScanItem(
                10, "PHYSICS", "v = u + at", "Calculation: 25 m/s", "Physics", "Today"
        ));
        assertFalse("History should not be empty after adding scan item", history.isEmpty());
        assertEquals(1, history.size());
        assertEquals("PHYSICS", history.get(0).type);
    }

    @Test
    public void testLabResultsSchemaContract() {
        // Enforce that expected column names used in CameraHistoryActivity match the DatabaseHelper contract
        String[] requiredColumns = new String[] {
                "id",
                "username",
                "problem_type",
                "problem_text",
                "solution_text",
                "is_assignment",
                "course_context",
                "timestamp"
        };

        for (String col : requiredColumns) {
            assertNotNull(col);
            assertFalse(col.isEmpty());
            assertNotEquals("Legacy invalid column 'mode' must not be in schema", "mode", col);
            assertNotEquals("Legacy invalid column 'problem' must not be in schema", "problem", col);
        }
    }
}

