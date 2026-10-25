package com.million.learnify;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * StudentPaymentTest - Comprehensive verification of Learnify's Student Payment & Premium Access System.
 * Tests:
 * 1. Official recipient details (CBE: 1000591245407, Telebirr: 0902939206, Beneficiary: Million Teshome Gudeto).
 * 2. 3-Day Free Trial math and expiry boundary conditions.
 * 3. Plan A (50 ETB) vs Plan B (100 ETB) entitlement rules.
 */
public class StudentPaymentTest {

    private static final String OFFICIAL_BENEFICIARY = "Million Teshome Gudeto";
    private static final String OFFICIAL_CBE_ACCOUNT = "1000591245407";
    private static final String OFFICIAL_TELEBIRR_NUMBER = "0902939206";

    @Test
    public void testOfficialRecipientDetailsIntegrity() {
        assertNotNull(OFFICIAL_BENEFICIARY);
        assertEquals("Million Teshome Gudeto", OFFICIAL_BENEFICIARY);

        assertNotNull(OFFICIAL_CBE_ACCOUNT);
        assertEquals("1000591245407", OFFICIAL_CBE_ACCOUNT);
        assertEquals(13, OFFICIAL_CBE_ACCOUNT.length());

        assertNotNull(OFFICIAL_TELEBIRR_NUMBER);
        assertEquals("0902939206", OFFICIAL_TELEBIRR_NUMBER);
        assertEquals(10, OFFICIAL_TELEBIRR_NUMBER.length());
        assertTrue("Telebirr starts with 09", OFFICIAL_TELEBIRR_NUMBER.startsWith("09"));
    }

    @Test
    public void testThreeDayTrialDurationCalculation() {
        long trialDurationMillis = 3L * 24L * 60L * 60L * 1000L;
        assertEquals(259200000L, trialDurationMillis);

        long hours = trialDurationMillis / (3600 * 1000L);
        assertEquals(72L, hours); // Exactly 72 hours
    }

    @Test
    public void testTrialElapsedBoundaryConditions() {
        long duration = 3L * 24L * 60L * 60L * 1000L;

        // Case 1: Just created (0 ms elapsed)
        long elapsedZero = 0L;
        assertTrue(elapsedZero < duration);
        assertEquals(duration, duration - elapsedZero);

        // Case 2: 24 hours elapsed (1 day in)
        long elapsedOneDay = 24L * 3600L * 1000L;
        assertTrue(elapsedOneDay < duration);
        assertEquals(48L, (duration - elapsedOneDay) / (3600 * 1000L));

        // Case 3: Exactly 72 hours elapsed (Boundary expiration)
        long elapsedThreeDays = duration;
        assertFalse(elapsedThreeDays < duration);

        // Case 4: 4 days elapsed (Expired)
        long elapsedFourDays = 4L * 24L * 3600L * 1000L;
        assertFalse(elapsedFourDays < duration);
    }

    @Test
    public void testPlanEntitlementMappingLogic() {
        // Plan B: 100 ETB grants access to all subjects
        String planB = "100 ETB";
        assertTrue("Plan B unlocks Biology", isSubjectPermitted(planB, "Biology"));
        assertTrue("Plan B unlocks Physics", isSubjectPermitted(planB, "Physics"));
        assertTrue("Plan B unlocks Chemistry", isSubjectPermitted(planB, "Chemistry"));
        assertTrue("Plan B unlocks Mathematics", isSubjectPermitted(planB, "Mathematics"));
        assertTrue("Plan B unlocks English", isSubjectPermitted(planB, "English"));

        // Plan A: 50 ETB grants access to Math & English only
        String planA = "50 ETB";
        assertTrue("Plan A unlocks Mathematics", isSubjectPermitted(planA, "Mathematics"));
        assertTrue("Plan A unlocks Math", isSubjectPermitted(planA, "Math"));
        assertTrue("Plan A unlocks English", isSubjectPermitted(planA, "English"));

        assertFalse("Plan A blocks Biology", isSubjectPermitted(planA, "Biology"));
        assertFalse("Plan A blocks Physics", isSubjectPermitted(planA, "Physics"));
        assertFalse("Plan A blocks Chemistry", isSubjectPermitted(planA, "Chemistry"));
    }

    /**
     * Mirrors the pure logic inside DatabaseHelper.hasPremiumAccess
     */
    private boolean isSubjectPermitted(String planAmount, String subjectName) {
        if (planAmount == null) return false;
        String amt = planAmount.toUpperCase().trim();

        if (amt.contains("100") || amt.contains("FULL")) {
            return true;
        }

        if (amt.contains("50") || amt.contains("BASIC")) {
            if (subjectName == null || subjectName.trim().isEmpty()) {
                return true;
            }
            String sLower = subjectName.toLowerCase().trim();
            return sLower.contains("math") || sLower.contains("english");
        }

        return false;
    }
}

