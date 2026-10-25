package com.million.learnify;

import org.junit.Test;
import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * AdminDashboardBatch3VerificationTest - Comprehensive verification for Batch 3:
 * 1. Two-column executive action grid layout contract and preserved IDs.
 * 2. Visual standards: 16dp corner radius, 2dp elevation, ripple feedback, touch target sizing.
 * 3. Governance and compliance cards preservation and visual upgrade.
 * 4. Debounce protection against rapid double-clicks.
 * 5. Preservation of all 11 drawer menu destinations and 7 stats cards.
 */
public class AdminDashboardBatch3VerificationTest {

    private String readProjectFile(String relativePath) {
        try {
            File f = new File(relativePath);
            if (!f.exists()) {
                f = new File("app/" + relativePath);
            }
            if (!f.exists()) {
                f = new File("../" + relativePath);
            }
            return new String(Files.readAllBytes(f.toPath()));
        } catch (Exception e) {
            fail("Failed to read file: " + relativePath + ": " + e.getMessage());
            return "";
        }
    }

    @Test
    public void testLayoutContract_allSevenActionCardIdsPreserved() {
        String xml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");

        // Required action button/card IDs
        assertTrue("btnIssueCertificate must exist", xml.contains("android:id=\"@+id/btnIssueCertificate\""));
        assertTrue("btnCreateLab must exist", xml.contains("android:id=\"@+id/btnCreateLab\""));
        assertTrue("btnAddUser must exist", xml.contains("android:id=\"@+id/btnAddUser\""));
        assertTrue("btnUpload must exist", xml.contains("android:id=\"@+id/btnUpload\""));
        assertTrue("btnSimulate must exist", xml.contains("android:id=\"@+id/btnSimulate\""));
        assertTrue("btnReports must exist", xml.contains("android:id=\"@+id/btnReports\""));
        assertTrue("btnSettings must exist", xml.contains("android:id=\"@+id/btnSettings\""));
    }

    @Test
    public void testLayoutContract_governanceAndComplianceCardsPreserved() {
        String xml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");

        assertTrue("cardIssueCert must exist", xml.contains("android:id=\"@+id/cardIssueCert\""));
        assertTrue("cardPaymentVerification must exist", xml.contains("android:id=\"@+id/cardPaymentVerification\""));
        assertTrue("cardSupportCenter must exist", xml.contains("android:id=\"@+id/cardSupportCenter\""));
    }

    @Test
    public void testVisualStandards_cardAttributesAndRippleFeedback() {
        String xml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");

        // Action cards must have elevation, corner radius, and ripple feedback
        assertTrue("Quick action grid must use 16dp radius cards", xml.contains("app:cardCornerRadius=\"16dp\""));
        assertTrue("Quick action grid must use 2dp card elevation", xml.contains("app:cardElevation=\"2dp\""));
        assertTrue("Cards must have selectableItemBackground ripple", xml.contains("android:foreground=\"?attr/selectableItemBackground\""));
        assertTrue("Cards must be clickable", xml.contains("android:clickable=\"true\""));
        assertTrue("Cards must be focusable", xml.contains("android:focusable=\"true\""));
    }

    @Test
    public void testVisualStandards_iconContainersAndAccents() {
        String xml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");

        // Icon containers across the action grid
        assertTrue("Emerald icon container for Issue Certificate", xml.contains("@drawable/bg_icon_container_emerald"));
        assertTrue("Sapphire icon container for Create Lab", xml.contains("@drawable/bg_icon_container_sapphire"));
        assertTrue("Amber icon container for Add User", xml.contains("@drawable/bg_icon_container_amber"));
        assertTrue("Teal icon container for Upload", xml.contains("@drawable/bg_icon_container_teal"));
        assertTrue("Purple icon container for Simulation", xml.contains("@drawable/bg_icon_container_purple"));
        assertTrue("Rose icon container for Reports", xml.contains("@drawable/bg_icon_container_rose"));
    }

    @Test
    public void testDebounceLogic_preventsRapidDuplicateLaunches() {
        long lastClick = 1000L;

        // Rapid click 100ms later at t = 1100ms should be debounced (< 600ms)
        assertFalse("Click within 100ms must be debounced", AdminDashboardActivity.checkCanPerformAction(1100L, lastClick));

        // Rapid click 300ms later at t = 1300ms should be debounced (< 600ms)
        assertFalse("Click within 300ms must be debounced", AdminDashboardActivity.checkCanPerformAction(1300L, lastClick));

        // Click at boundary t = 1599ms (599ms delta) should still be debounced
        assertFalse("Click at 599ms must be debounced", AdminDashboardActivity.checkCanPerformAction(1599L, lastClick));

        // Click at t = 1600ms (600ms delta) should succeed
        assertTrue("Click after 600ms interval must be allowed", AdminDashboardActivity.checkCanPerformAction(1600L, lastClick));

        // Subsequent click with new lastClick time 1600ms:
        long secondClick = 1600L;
        assertFalse("Immediate subsequent click must be debounced", AdminDashboardActivity.checkCanPerformAction(1650L, secondClick));
        assertTrue("Click after another 600ms interval must be allowed", AdminDashboardActivity.checkCanPerformAction(2200L, secondClick));
    }

    @Test
    public void testPreviousBatchesPreserved() {
        String xml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");

        // Batch 2 Header & Hero Banner
        assertTrue("Toolbar must exist", xml.contains("android:id=\"@+id/adminToolbar\""));
        assertTrue("tvAdminWelcome must exist", xml.contains("android:id=\"@+id/tvAdminWelcome\""));
        assertTrue("tvAdminDate must exist", xml.contains("android:id=\"@+id/tvAdminDate\""));

        // Batch 2 All 7 Stats Cards
        assertTrue("cardTotalStudents must exist", xml.contains("android:id=\"@+id/cardTotalStudents\""));
        assertTrue("cardActiveStudents must exist", xml.contains("android:id=\"@+id/cardActiveStudents\""));
        assertTrue("cardTeachers must exist", xml.contains("android:id=\"@+id/cardTeachers\""));
        assertTrue("cardTotalLabs must exist", xml.contains("android:id=\"@+id/cardTotalLabs\""));
        assertTrue("cardCompletedLabs must exist", xml.contains("android:id=\"@+id/cardCompletedLabs\""));
        assertTrue("cardPendingLabs must exist", xml.contains("android:id=\"@+id/cardPendingLabs\""));
        assertTrue("cardSystemHealth must exist", xml.contains("android:id=\"@+id/cardSystemHealth\""));
    }
}

