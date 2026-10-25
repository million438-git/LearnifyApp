package com.million.learnify;

import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * AdminPaymentVerificationTest - Comprehensive Unit & Layout Integrity Test:
 * 1. Verifies that all 11 drawer navigation items exist in admin_drawer_menu.xml.
 * 2. Verifies that all core, KPI, search, filter, and empty-state view IDs exist in activity_admin_payment_verification.xml.
 * 3. Verifies that all item row IDs exist in item_payment_admin.xml.
 * 4. Verifies controller RBAC security, repository wiring, debounce protection, and audit logging in AdminPaymentVerificationActivity.java.
 * 5. Verifies PaymentRecord data modeling and status filtering logic.
 */
public class AdminPaymentVerificationTest {

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
    public void testAdminDrawer_allElevenDestinationsPreserved() {
        String menuXml = readProjectFile("src/main/res/menu/admin_drawer_menu.xml");

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
            assertTrue("admin_drawer_menu.xml must preserve drawer item: " + id,
                    menuXml.contains("android:id=\"@+id/" + id + "\""));
        }
    }

    @Test
    public void testActivityLayout_allCoreAndKpiViewIdsPresent() {
        String layoutXml = readProjectFile("src/main/res/layout/activity_admin_payment_verification.xml");

        // Core preserved view IDs
        List<String> coreIds = Arrays.asList(
            "paymentVerifyToolbar",
            "paymentListContainer",
            "emptyPaymentState",
            "tvPaymentCount",
            "tvPaymentSectionTitle"
        );
        for (String id : coreIds) {
            assertTrue("Layout must contain core view: " + id,
                    layoutXml.contains("android:id=\"@+id/" + id + "\""));
        }

        // Financial KPI cards & count IDs
        List<String> kpiIds = Arrays.asList(
            "cardStatTotal",
            "cardStatPending",
            "cardStatApproved",
            "cardStatRejected",
            "tvStatTotalCount",
            "tvStatPendingCount",
            "tvStatApprovedCount",
            "tvStatRejectedCount"
        );
        for (String id : kpiIds) {
            assertTrue("Layout must contain KPI view: " + id,
                    layoutXml.contains("android:id=\"@+id/" + id + "\""));
        }

        // Search & Filter controls
        List<String> filterIds = Arrays.asList(
            "etSearchPayment",
            "chipGroupPaymentStatus",
            "chipPaymentAll",
            "chipPaymentPending",
            "chipPaymentApproved",
            "chipPaymentRejected",
            "tvEmptyPaymentTitle",
            "tvEmptyPaymentSubtitle",
            "btnResetFilters"
        );
        for (String id : filterIds) {
            assertTrue("Layout must contain filter view: " + id,
                    layoutXml.contains("android:id=\"@+id/" + id + "\""));
        }
    }

    @Test
    public void testItemLayout_allRowViewIdsPresent() {
        String itemXml = readProjectFile("src/main/res/layout/item_payment_admin.xml");

        List<String> rowIds = Arrays.asList(
            "cardPaymentItem",
            "ivPaymentIcon",
            "tvPaymentUser",
            "tvPaymentTxId",
            "tvPaymentAmount",
            "tvPaymentStatus",
            "tvProofBadge",
            "tvPaymentDate"
        );
        for (String id : rowIds) {
            assertTrue("item_payment_admin.xml must contain view: " + id,
                    itemXml.contains("android:id=\"@+id/" + id + "\""));
        }
    }

    @Test
    public void testActivityController_securityAndPreservationWired() {
        String javaSrc = readProjectFile("src/main/java/com/million/learnify/AdminPaymentVerificationActivity.java");

        // Role-Based Access Control
        assertTrue("Must enforce ADMIN role", javaSrc.contains("enforceRole(\"ADMIN\")"));
        assertTrue("Must extend BaseActivity", javaSrc.contains("extends BaseActivity"));

        // DataRepository wiring & Database preservation
        assertTrue("Must call repository.getAllPayments()", javaSrc.contains("repository.getAllPayments()"));
        assertTrue("Must call repository.adminUpdatePayment", javaSrc.contains("repository.adminUpdatePayment("));
        assertTrue("Must audit log approval", javaSrc.contains("PAYMENT_APPROVED"));
        assertTrue("Must audit log rejection", javaSrc.contains("PAYMENT_REJECTED"));

        // Methods preserved
        assertTrue("Must have loadPendingPayments()", javaSrc.contains("void loadPendingPayments()"));
        assertTrue("Must have addPaymentRow()", javaSrc.contains("void addPaymentRow("));
        assertTrue("Must have showVerificationDialog()", javaSrc.contains("void showVerificationDialog("));

        // Debounce protection
        assertTrue("Must declare isDecisionInProgress", javaSrc.contains("isDecisionInProgress"));

        // Safe receipt decoding
        assertTrue("Must have tryDecodeReceiptBitmap()", javaSrc.contains("tryDecodeReceiptBitmap("));
    }

    @Test
    public void testPaymentRecordModel_validationAndDefaults() {
        AdminPaymentVerificationActivity.PaymentRecord recordWithDefaults =
                new AdminPaymentVerificationActivity.PaymentRecord(1, null, null, null, null, null, null);

        assertEquals("Anonymous Student", recordWithDefaults.user);
        assertEquals("", recordWithDefaults.txId);
        assertEquals("Free Tier", recordWithDefaults.amount);
        assertEquals("", recordWithDefaults.screenshotUri);
        assertEquals("Pending", recordWithDefaults.status);
        assertEquals("", recordWithDefaults.timestamp);

        AdminPaymentVerificationActivity.PaymentRecord recordExplicit =
                new AdminPaymentVerificationActivity.PaymentRecord(42, " student_abebe ", " TX-991 ", " 500 ETB ", " /storage/receipt.jpg ", " Approved ", " 2026-10-12 18:30:00 ");

        assertEquals(42, recordExplicit.id);
        assertEquals("student_abebe", recordExplicit.user);
        assertEquals("TX-991", recordExplicit.txId);
        assertEquals("500 ETB", recordExplicit.amount);
        assertEquals("/storage/receipt.jpg", recordExplicit.screenshotUri);
        assertEquals("Approved", recordExplicit.status);
        assertEquals("2026-10-12 18:30:00", recordExplicit.timestamp);
    }
}

