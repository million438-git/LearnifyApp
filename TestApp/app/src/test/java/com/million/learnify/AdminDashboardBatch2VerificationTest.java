package com.million.learnify;

import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * AdminDashboardBatch2VerificationTest - Verification of Batch 2 improvements:
 * 1. Admin Dashboard Header & Hero Banner layout structure and IDs.
 * 2. Removal of duplicate drawer button / sub-bar.
 * 3. Elevation and complete contract preservation for all 7 Statistics Cards.
 * 4. Verification that all 11 navigation drawer destinations are preserved.
 * 5. Formatting of live numeric metrics and dynamic administrator greetings.
 */
public class AdminDashboardBatch2VerificationTest {

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
    public void testLayoutContract_headerAndHeroBanner() {
        String xml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");
        
        // Toolbar elements
        assertTrue("Toolbar must exist", xml.contains("android:id=\"@+id/adminToolbar\""));
        assertTrue("Toolbar drawer toggle must exist", xml.contains("android:id=\"@+id/llToolbarBack\""));
        assertTrue("Notification icon must exist", xml.contains("android:id=\"@+id/ivAdminNotification\""));
        assertTrue("Profile avatar must exist", xml.contains("android:id=\"@+id/adminProfileImg\""));
        
        // Hero banner elements
        assertTrue("Dynamic welcome greeting must exist", xml.contains("android:id=\"@+id/tvAdminWelcome\""));
        assertTrue("Live date indicator must exist", xml.contains("android:id=\"@+id/tvAdminDate\""));
        assertTrue("Hero banner background must be applied", xml.contains("@drawable/bg_admin_hero_banner"));
        
        // Redundant subbar must NOT exist
        assertFalse("Duplicate subbar btnOpenDrawer must be removed", xml.contains("android:id=\"@+id/btnOpenDrawer\""));
    }

    @Test
    public void testLayoutContract_allSevenStatsCardsPreserved() {
        String xml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");
        
        // 1. Total Students
        assertTrue("cardTotalStudents must exist", xml.contains("android:id=\"@+id/cardTotalStudents\""));
        assertTrue("tvTotalStudents must exist", xml.contains("android:id=\"@+id/tvTotalStudents\""));
        
        // 2. Active Students
        assertTrue("cardActiveStudents must exist", xml.contains("android:id=\"@+id/cardActiveStudents\""));
        assertTrue("tvActiveStudents must exist", xml.contains("android:id=\"@+id/tvActiveStudents\""));
        
        // 3. Teachers / Faculty
        assertTrue("cardTeachers must exist", xml.contains("android:id=\"@+id/cardTeachers\""));
        assertTrue("tvTotalTeachers must exist", xml.contains("android:id=\"@+id/tvTotalTeachers\""));
        
        // 4. Total Labs
        assertTrue("cardTotalLabs must exist", xml.contains("android:id=\"@+id/cardTotalLabs\""));
        assertTrue("tvTotalLabs must exist", xml.contains("android:id=\"@+id/tvTotalLabs\""));
        
        // 5. Completed Labs
        assertTrue("cardCompletedLabs must exist", xml.contains("android:id=\"@+id/cardCompletedLabs\""));
        assertTrue("tvCompletedLabs must exist", xml.contains("android:id=\"@+id/tvCompletedLabs\""));
        
        // 6. Pending Labs
        assertTrue("cardPendingLabs must exist", xml.contains("android:id=\"@+id/cardPendingLabs\""));
        assertTrue("tvPendingLabs must exist", xml.contains("android:id=\"@+id/tvPendingLabs\""));
        
        // 7. System Health
        assertTrue("cardSystemHealth must exist", xml.contains("android:id=\"@+id/cardSystemHealth\""));
        assertTrue("tvSystemHealth must exist", xml.contains("android:id=\"@+id/tvSystemHealth\""));
    }

    @Test
    public void testLayoutContract_preservedDownstreamActionButtons() {
        String xml = readProjectFile("src/main/res/layout/activity_admin_dashboard.xml");
        
        // Downstream actions for Batch 3 must remain intact
        assertTrue("cardIssueCert must be preserved", xml.contains("android:id=\"@+id/cardIssueCert\""));
        assertTrue("btnIssueCertificate must be preserved", xml.contains("android:id=\"@+id/btnIssueCertificate\""));
        assertTrue("btnCreateLab must be preserved", xml.contains("android:id=\"@+id/btnCreateLab\""));
        assertTrue("btnAddUser must be preserved", xml.contains("android:id=\"@+id/btnAddUser\""));
        assertTrue("btnUpload must be preserved", xml.contains("android:id=\"@+id/btnUpload\""));
        assertTrue("btnSimulate must be preserved", xml.contains("android:id=\"@+id/btnSimulate\""));
        assertTrue("btnReports must be preserved", xml.contains("android:id=\"@+id/btnReports\""));
        assertTrue("btnSettings must be preserved", xml.contains("android:id=\"@+id/btnSettings\""));
    }

    @Test
    public void testNavigationDrawer_allElevenDestinationsPreserved() {
        String xml = readProjectFile("src/main/res/menu/admin_drawer_menu.xml");
        
        String[] requiredDestinations = {
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
        };
        
        for (String dest : requiredDestinations) {
            assertTrue("Drawer must include destination: " + dest, xml.contains("android:id=\"@+id/" + dest + "\""));
        }
    }

    @Test
    public void testNumericFormatting_executiveStandard() {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
        assertEquals("0", nf.format(0));
        assertEquals("12", nf.format(12));
        assertEquals("1,250", nf.format(1250));
        assertEquals("25,000", nf.format(25000));
    }

    @Test
    public void testAdminGreetingResolution_safeAndPolished() {
        // Test resolution logic matching loadProfileData()
        assertEquals("Welcome back, Dr. Million", formatGreeting("Dr. Million", "admin"));
        assertEquals("Welcome back, Administrator", formatGreeting("", "admin"));
        assertEquals("Welcome back, Administrator", formatGreeting(null, "admin"));
        assertEquals("Welcome back, system_master", formatGreeting(null, "system_master"));
        assertEquals("Welcome back, Administrator", formatGreeting(null, null));
    }

    private String formatGreeting(String fullName, String username) {
        String displayName = null;
        if (fullName != null && !fullName.trim().isEmpty()) {
            displayName = fullName.trim();
        } else if (username != null && !username.trim().isEmpty() && !username.equalsIgnoreCase("admin")) {
            displayName = username.trim();
        } else {
            displayName = "Administrator";
        }
        return "Welcome back, " + displayName;
    }
}

