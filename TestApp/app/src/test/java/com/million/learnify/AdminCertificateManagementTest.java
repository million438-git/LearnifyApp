package com.million.learnify;

import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * AdminCertificateManagementTest - Comprehensive Unit & Regression Test Suite:
 * 1. Verifies admin drawer menu preservation for certificate navigation.
 * 2. Verifies activity_admin_issue_certificate.xml preserves all 15 legacy view IDs plus new KPI/Tab/Eligibility views.
 * 3. Verifies item_issued_certificate_admin.xml has all card and details views.
 * 4. Verifies AdminIssueCertificateActivity RBAC, duplicate protection, pre-issuance modal, and database wiring.
 * 5. Verifies CertificateActivity column name defect fix and honest unissued state without fabricated credentials.
 * 6. Verifies DatabaseHelper SQL column aliasing and registry/verification helper methods.
 * 7. Verifies certificate serial ID generation format (LNYF-XXXXXXXX) and regex validation.
 * 8. Verifies CertificateRecord model data integrity.
 */
public class AdminCertificateManagementTest {

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
    public void testAdminDrawer_certificateManagementDestinationPreserved() {
        String menuXml = readProjectFile("src/main/res/menu/admin_drawer_menu.xml");
        assertTrue("admin_drawer_menu.xml must preserve admin_nav_issue_cert",
                menuXml.contains("android:id=\"@+id/admin_nav_issue_cert\""));
    }

    @Test
    public void testActivityLayout_allFifteenOriginalViewsPreserved() {
        String layoutXml = readProjectFile("src/main/res/layout/activity_admin_issue_certificate.xml");

        // The 15 non-negotiable original view IDs
        List<String> originalIds = Arrays.asList(
            "issueCertToolbar",
            "tilSelectStudent",
            "actvSelectStudent",
            "etStudentName",
            "actvCourseName",
            "etStudentId",
            "etIssueDate",
            "etCertTitle",
            "etPresentationText",
            "etBodyText",
            "etCertificateId",
            "etDirectorName",
            "etDirectorRole",
            "btnCancelIssue",
            "btnConfirmIssue"
        );

        for (String id : originalIds) {
            assertTrue("Layout must preserve original view: " + id,
                    layoutXml.contains("android:id=\"@+id/" + id + "\""));
        }
    }

    @Test
    public void testActivityLayout_kpiTabsAndEligibilityViewsPresent() {
        String layoutXml = readProjectFile("src/main/res/layout/activity_admin_issue_certificate.xml");

        // KPI metrics
        List<String> kpiIds = Arrays.asList(
            "tvStatTotalIssued",
            "tvStatEligibleCandidates",
            "tvStatRecentCount"
        );
        for (String id : kpiIds) {
            assertTrue("Layout must contain KPI view: " + id,
                    layoutXml.contains("android:id=\"@+id/" + id + "\""));
        }

        // Mode switch tabs
        List<String> tabIds = Arrays.asList(
            "btnTabIssue",
            "btnTabRegistry",
            "btnTabVerify"
        );
        for (String id : tabIds) {
            assertTrue("Layout must contain Tab button: " + id,
                    layoutXml.contains("android:id=\"@+id/" + id + "\""));
        }

        // Live Eligibility Inspector views
        List<String> eligibilityIds = Arrays.asList(
            "cardEligibilityInspector",
            "tvEligibilityStatusBadge",
            "tvEligibilityPaymentStatus",
            "tvEligibilityExamStatus",
            "tvEligibilityDuplicateWarning"
        );
        for (String id : eligibilityIds) {
            assertTrue("Layout must contain Eligibility view: " + id,
                    layoutXml.contains("android:id=\"@+id/" + id + "\""));
        }

        // Registry & Verification controls
        List<String> registryAndVerifyIds = Arrays.asList(
            "layoutRegistrySection",
            "etSearchRegistry",
            "registryListContainer",
            "tvRegistryRecordsCount",
            "layoutVerifySection",
            "etVerifyCertId",
            "btnRunVerify",
            "cardVerificationResult",
            "tvVerifyResultTitle",
            "tvVerifyResultStatusBadge",
            "tvVerifyResultDetails"
        );
        for (String id : registryAndVerifyIds) {
            assertTrue("Layout must contain Registry/Verify view: " + id,
                    layoutXml.contains("android:id=\"@+id/" + id + "\""));
        }
    }

    @Test
    public void testRegistryItemLayout_allRowViewsPresent() {
        String itemXml = readProjectFile("src/main/res/layout/item_issued_certificate_admin.xml");

        List<String> rowIds = Arrays.asList(
            "cardIssuedCertItem",
            "tvCertStudentName",
            "tvCertCourse",
            "tvCertStatusBadge",
            "tvCertId",
            "tvCertDate",
            "btnViewCertDetails"
        );

        for (String id : rowIds) {
            assertTrue("item_issued_certificate_admin.xml must contain: " + id,
                    itemXml.contains("android:id=\"@+id/" + id + "\""));
        }
    }

    @Test
    public void testAdminController_securityRbacAndDatabaseWiring() {
        String javaSrc = readProjectFile("src/main/java/com/million/learnify/AdminIssueCertificateActivity.java");

        // Role-Based Access Control
        assertTrue("Must enforce ADMIN role", javaSrc.contains("enforceRole(\"ADMIN\")"));
        assertTrue("Must extend BaseActivity", javaSrc.contains("extends BaseActivity"));

        // Database methods wired
        assertTrue("Must call dbHelper.getAllCertificates()", javaSrc.contains("dbHelper.getAllCertificates()"));
        assertTrue("Must call dbHelper.getCertificateById(", javaSrc.contains("dbHelper.getCertificateById("));
        assertTrue("Must call dbHelper.hasCertificateForCourse(", javaSrc.contains("dbHelper.hasCertificateForCourse("));
        assertTrue("Must call dbHelper.isCertificateIdTaken(", javaSrc.contains("dbHelper.isCertificateIdTaken("));
        assertTrue("Must call dbHelper.saveFullCertificate(", javaSrc.contains("dbHelper.saveFullCertificate("));
        assertTrue("Must notify student upon issuance", javaSrc.contains("dbHelper.addNotification("));
        assertTrue("Must log admin action with session admin username", javaSrc.contains("dbHelper.logAdminAction(currentAdminUsername"));

        // Safeguards
        assertTrue("Must display duplicate warning dialog", javaSrc.contains("Duplicate Credential Warning"));
        assertTrue("Must display final review confirmation dialog", javaSrc.contains("Confirm Certificate Issuance"));
    }

    @Test
    public void testStudentCertificateActivity_crashFixAndHonestState() {
        String certSrc = readProjectFile("src/main/java/com/million/learnify/CertificateActivity.java");

        // Safe column reading helper
        assertTrue("Must define getStringSafe helper to prevent getColumnIndexOrThrow crashes",
                certSrc.contains("private String getStringSafe(Cursor c, String... colNames)"));

        // Honest unissued state
        assertTrue("Must display NO CERTIFICATE ISSUED when student has no certificate",
                certSrc.contains("NO CERTIFICATE ISSUED"));
        assertTrue("Must show UNISSUED status badge", certSrc.contains("\"UNISSUED\""));
        assertTrue("Must disable share button when unissued", certSrc.contains("btnShare.setEnabled(false)"));

        // No random dummy fabrication
        assertFalse("Must NOT fabricate fake randomized ID prefix", certSrc.contains("\"ID-\" + (int)(Math.random()"));
        assertFalse("Must NOT fabricate fake randomized LNYF serial", certSrc.contains("\"LNYF-\" + (int)(Math.random()"));
    }

    @Test
    public void testDatabaseHelper_columnAliasesAndCertificateQueries() {
        String dbSrc = readProjectFile("src/main/java/com/million/learnify/DatabaseHelper.java");

        // Column aliases in getCustomCertificate()
        assertTrue("Must alias title as cert_title for student view compatibility",
                dbSrc.contains("title AS cert_title"));
        assertTrue("Must alias presentation as presentation_text",
                dbSrc.contains("presentation AS presentation_text"));
        assertTrue("Must alias body as body_text",
                dbSrc.contains("body AS body_text"));
        assertTrue("Must alias course as course_name",
                dbSrc.contains("course AS course_name"));
        assertTrue("Must alias date as issue_date",
                dbSrc.contains("date AS issue_date"));
        assertTrue("Must alias director as director_name",
                dbSrc.contains("director AS director_name"));
        assertTrue("Must alias role as director_role",
                dbSrc.contains("role AS director_role"));

        // Registry & verification helper queries
        assertTrue("Must have getAllCertificates()", dbSrc.contains("public Cursor getAllCertificates()"));
        assertTrue("Must have getCertificateById(String certId)", dbSrc.contains("public Cursor getCertificateById(String certId)"));
        assertTrue("Must have hasCertificateForCourse", dbSrc.contains("public boolean hasCertificateForCourse(String username, String course)"));
        assertTrue("Must have isCertificateIdTaken", dbSrc.contains("public boolean isCertificateIdTaken(String certId)"));
        assertTrue("Must have getCertificateCount", dbSrc.contains("public int getCertificateCount()"));
        assertTrue("Must have getEligibleStudentCount", dbSrc.contains("public int getEligibleStudentCount()"));
    }

    @Test
    public void testCertificateIdGenerationAndValidation() {
        for (int i = 0; i < 20; i++) {
            String certId = AdminIssueCertificateActivity.generateCertificateIdString();
            assertNotNull("Generated ID should not be null", certId);
            assertTrue("ID must start with LNYF- prefix: " + certId, certId.startsWith("LNYF-"));
            assertEquals("ID must be exactly 13 characters: " + certId, 13, certId.length());
            assertTrue("ID must match validation regex: " + certId,
                    AdminIssueCertificateActivity.isValidCertificateId(certId));
        }

        // Negative cases
        assertFalse(AdminIssueCertificateActivity.isValidCertificateId(null));
        assertFalse(AdminIssueCertificateActivity.isValidCertificateId(""));
        assertFalse(AdminIssueCertificateActivity.isValidCertificateId("CERT-12345678"));
        assertFalse(AdminIssueCertificateActivity.isValidCertificateId("LNYF-123")); // Too short
        assertFalse(AdminIssueCertificateActivity.isValidCertificateId("LNYF-123456789")); // Too long
        assertFalse(AdminIssueCertificateActivity.isValidCertificateId("LNYF-lowercase1")); // Must be uppercase
    }

    @Test
    public void testCertificateRecordModel_constructorAndFieldIntegrity() {
        AdminIssueCertificateActivity.CertificateRecord record =
                new AdminIssueCertificateActivity.CertificateRecord(
                        10,
                        "adam",
                        "CERTIFICATE OF COMPLETION",
                        "This is proudly presented to",
                        "Adam Test",
                        "For outstanding academic excellence",
                        "Mathematics 101",
                        "2026-10-12",
                        "STU-001",
                        "LNYF-A1B2C3D4",
                        "Dr. Million M.",
                        "Academic Director",
                        "2026-10-12 21:00:00"
                );

        assertEquals(10, record.id);
        assertEquals("adam", record.username);
        assertEquals("CERTIFICATE OF COMPLETION", record.title);
        assertEquals("This is proudly presented to", record.presentation);
        assertEquals("Adam Test", record.studentName);
        assertEquals("For outstanding academic excellence", record.body);
        assertEquals("Mathematics 101", record.course);
        assertEquals("2026-10-12", record.date);
        assertEquals("STU-001", record.studentId);
        assertEquals("LNYF-A1B2C3D4", record.certificateId);
        assertEquals("Dr. Million M.", record.director);
        assertEquals("Academic Director", record.role);
        assertEquals("2026-10-12 21:00:00", record.timestamp);
    }
}
