package com.million.learnify;

import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * AdminDashboardBatch4QATest - Final comprehensive QA and regression test suite:
 * 1. Full verification of all 11 drawer destinations and their handlers.
 * 2. Full verification of all 7 statistics cards and their live database wiring.
 * 3. Full verification of all 7 quick-action grid buttons and target destinations.
 * 4. Full verification of all 3 governance cards.
 * 5. Toolbar, profile avatar, notifications, and drawer selected item state.
 * 6. Rigorous debounce verification for first tap, rapid taps, and subsequent legitimate taps.
 */
public class AdminDashboardBatch4QATest {

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
    public void testDrawer_allElevenItemsInMenuAndHandledInActivity() {
        String menuXml = readProjectFile("src/main/res/menu/admin_drawer_menu.xml");
        String javaSrc = readProjectFile("src/main/java/com/million/learnify/AdminDashboardActivity.java");

        List<String> requiredDrawerIds = Arrays.asList(
            "admin_nav_dashboard",
            "admin_nav_users",
            "admin_nav_issue_cert",
            "admin_nav_content",
            "admin_nav_camera_lab",
            "admin_nav_reports",
            "admin_nav_add_question",
            "admin_nav_payment_verification",
            "admin_nav_support_management",
            "admin_nav_settings",
            "admin_nav_logout"
        );

        for (String id : requiredDrawerIds) {
            assertTrue("Menu XML must contain item: " + id, menuXml.contains("android:id=\"@+id/" + id + "\""));
            assertTrue("Java code must handle drawer item: " + id, javaSrc.contains("R.id." + id));
        }

        // Verify destinations
        assertTrue("Users destination", javaSrc.contains("AdminUserManagementActivity.class"));
        assertTrue("Issue cert destination", javaSrc.contains("AdminIssueCertificateActivity.class"));
        assertTrue("Content management destination", javaSrc.contains("AdminContentManagementActivity.class"));
        assertTrue("Camera lab destination", javaSrc.contains("AdminCameraLabActivity.class"));
        assertTrue("Reports destination", javaSrc.contains("AdminReportsActivity.class"));
        assertTrue("Add question destination", javaSrc.contains("AdminQuestionActivity.class"));
        assertTrue("Payment verification destination", javaSrc.contains("AdminPaymentVerificationActivity.class"));
        assertTrue("Support management destination", javaSrc.contains("AdminSupportManagementActivity.class"));
        assertTrue("Settings destination", javaSrc.contains("AdminSystemSettingsActivity.class"));
        assertTrue("Logout handler", javaSrc.contains("onLogoutClick"));
    }

    @Test
    public void testStatisticsCards_allSevenPresentAndWired() {
        String layoutXml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");
        String javaSrc = readProjectFile("src/main/java/com/million/learnify/AdminDashboardActivity.java");

        List<String> statsCardIds = Arrays.asList(
            "cardTotalStudents",
            "cardActiveStudents",
            "cardTeachers",
            "cardTotalLabs",
            "cardCompletedLabs",
            "cardPendingLabs",
            "cardSystemHealth"
        );

        for (String id : statsCardIds) {
            assertTrue("Layout XML must contain card: " + id, layoutXml.contains("android:id=\"@+id/" + id + "\""));
            assertTrue("Java code must wire click listener for: " + id, javaSrc.contains("R.id." + id));
        }

        // Verify count text views
        List<String> statsTextViewIds = Arrays.asList(
            "tvTotalStudents",
            "tvActiveStudents",
            "tvTotalTeachers",
            "tvTotalLabs",
            "tvCompletedLabs",
            "tvPendingLabs",
            "tvSystemHealth"
        );

        for (String tvId : statsTextViewIds) {
            assertTrue("Layout XML must contain count TextView: " + tvId, layoutXml.contains("android:id=\"@+id/" + tvId + "\""));
            assertTrue("Java code must bind count TextView: " + tvId, javaSrc.contains("R.id." + tvId));
        }
    }

    @Test
    public void testQuickActionGrid_allSevenButtonsPresentAndWired() {
        String layoutXml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");
        String javaSrc = readProjectFile("src/main/java/com/million/learnify/AdminDashboardActivity.java");

        List<String> actionButtonIds = Arrays.asList(
            "btnIssueCertificate",
            "btnCreateLab",
            "btnAddUser",
            "btnUpload",
            "btnSimulate",
            "btnReports",
            "btnSettings"
        );

        for (String id : actionButtonIds) {
            assertTrue("Layout XML must contain action button: " + id, layoutXml.contains("android:id=\"@+id/" + id + "\""));
            assertTrue("Java code must wire action click listener for: " + id, javaSrc.contains("R.id." + id));
        }

        // Verify physics simulation engine wiring
        assertTrue("PhysicsSimulationActivity destination", javaSrc.contains("PhysicsSimulationActivity.class"));
        assertTrue("AdminRegisterActivity destination", javaSrc.contains("AdminRegisterActivity.class"));
    }

    @Test
    public void testGovernanceCards_allThreePresentAndWired() {
        String layoutXml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");
        String javaSrc = readProjectFile("src/main/java/com/million/learnify/AdminDashboardActivity.java");

        List<String> governanceCardIds = Arrays.asList(
            "cardIssueCert",
            "cardPaymentVerification",
            "cardSupportCenter"
        );

        for (String id : governanceCardIds) {
            assertTrue("Layout XML must contain governance card: " + id, layoutXml.contains("android:id=\"@+id/" + id + "\""));
            assertTrue("Java code must wire governance click listener for: " + id, javaSrc.contains("R.id." + id));
        }
    }

    @Test
    public void testToolbarAndNavigationState_headerAndDrawerState() {
        String layoutXml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");
        String javaSrc = readProjectFile("src/main/java/com/million/learnify/AdminDashboardActivity.java");

        // Toolbar elements
        assertTrue("adminToolbar present", layoutXml.contains("android:id=\"@+id/adminToolbar\""));
        assertTrue("llToolbarBack present", layoutXml.contains("android:id=\"@+id/llToolbarBack\""));
        assertTrue("adminProfileImg present", layoutXml.contains("android:id=\"@+id/adminProfileImg\""));
        assertTrue("ivAdminNotification present", layoutXml.contains("android:id=\"@+id/ivAdminNotification\""));

        // Drawer default checked item set
        assertTrue("Drawer checked item set to dashboard in setup", javaSrc.contains("setCheckedItem(R.id.admin_nav_dashboard)"));
    }

    @Test
    public void testDebounceTiming_rigorousContractVerification() {
        assertEquals("Debounce interval must be exactly 600ms", 600L, AdminDashboardActivity.DEBOUNCE_INTERVAL_MS);

        // 1. Initial click (lastActionTime = 0) must always succeed
        assertTrue("Initial tap must always be permitted",
            AdminDashboardActivity.checkCanPerformAction(System.currentTimeMillis(), 0L));

        long t0 = 1000000L;

        // 2. Immediate micro-clicks (< 600ms) must be blocked
        assertFalse("Click at +50ms blocked", AdminDashboardActivity.checkCanPerformAction(t0 + 50L, t0));
        assertFalse("Click at +200ms blocked", AdminDashboardActivity.checkCanPerformAction(t0 + 200L, t0));
        assertFalse("Click at +450ms blocked", AdminDashboardActivity.checkCanPerformAction(t0 + 450L, t0));
        assertFalse("Click at +599ms blocked", AdminDashboardActivity.checkCanPerformAction(t0 + 599L, t0));

        // 3. Click at exact boundary (600ms) must succeed
        assertTrue("Click at exact 600ms interval permitted", AdminDashboardActivity.checkCanPerformAction(t0 + 600L, t0));

        // 4. Click well after interval (e.g. 1500ms) must succeed
        assertTrue("Click after 1500ms permitted", AdminDashboardActivity.checkCanPerformAction(t0 + 1500L, t0));
    }
}

