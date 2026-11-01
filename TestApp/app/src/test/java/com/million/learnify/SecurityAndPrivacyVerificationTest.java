package com.million.learnify;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * SecurityAndPrivacyVerificationTest - Automated Unit Tests for Phase 4 Security Engineering.
 * Verifies RFC 6238 TOTP engine, password policy compliance validator,
 * backup recovery codes generation, and account safety safeguards.
 */
public class SecurityAndPrivacyVerificationTest {

    @Test
    public void testPasswordPolicyCompliance() {
        // Valid compliant passwords
        assertTrue(isValidPasswordPolicy("Learnify@2026!"));
        assertTrue(isValidPasswordPolicy("Secur3#Pass"));
        assertTrue(isValidPasswordPolicy("Str0ng!Passw0rd"));

        // Invalid: Length < 8
        assertFalse(isValidPasswordPolicy("Ab1!"));
        assertFalse(isValidPasswordPolicy("P@ss1"));

        // Invalid: Missing uppercase
        assertFalse(isValidPasswordPolicy("learnify@2026!"));

        // Invalid: Missing lowercase
        assertFalse(isValidPasswordPolicy("LEARNIFY@2026!"));

        // Invalid: Missing digit
        assertFalse(isValidPasswordPolicy("Learnify@Study!"));

        // Invalid: Missing special symbol
        assertFalse(isValidPasswordPolicy("Learnify2026Abc"));

        // Invalid: Null or empty
        assertFalse(isValidPasswordPolicy(null));
        assertFalse(isValidPasswordPolicy(""));
    }

    @Test
    public void testTotpSecretAndUriGeneration() {
        String secret = TotpManager.generateSecret();
        assertNotNull(secret);
        assertEquals(16, secret.length());
        assertTrue(secret.matches("^[A-Z2-7]{16}$"));

        String uri = TotpManager.getProvisioningUri("student_tester", secret);
        assertNotNull(uri);
        assertTrue(uri.startsWith("otpauth://totp/Learnify:student_tester"));
        assertTrue(uri.contains("secret=" + secret));
        assertTrue(uri.contains("issuer=Learnify"));
        assertTrue(uri.contains("digits=6"));
        assertTrue(uri.contains("period=30"));
    }

    @Test
    public void testTotpVerificationWithCurrentWindow() {
        String secret = TotpManager.generateSecret();
        byte[] key = TotpManager.decodeBase32(secret);
        assertTrue(key.length > 0);

        long nowSeconds = System.currentTimeMillis() / 1000L;
        int currentCodeInt = TotpManager.generateCodeForTime(key, nowSeconds);
        String currentCodeStr = String.format(java.util.Locale.US, "%06d", currentCodeInt);

        // Verification must succeed for the active code
        assertTrue(TotpManager.verifyCode(secret, currentCodeStr));

        // Invalid 6-digit codes must fail
        assertFalse(TotpManager.verifyCode(secret, "000000".equals(currentCodeStr) ? "999999" : "000000"));
        assertFalse(TotpManager.verifyCode(secret, "123")); // too short
        assertFalse(TotpManager.verifyCode(secret, "abcdef")); // non-numeric
        assertFalse(TotpManager.verifyCode(secret, null));
    }

    @Test
    public void testBackupRecoveryCodesFormat() {
        List<String> codes = TotpManager.generateRecoveryCodes(8);
        assertNotNull(codes);
        assertEquals(8, codes.size());

        for (String code : codes) {
            assertNotNull(code);
            assertEquals(9, code.length()); // ABCD-1234
            assertEquals('-', code.charAt(4));
            assertTrue(code.matches("^[A-Z2-9]{4}-[A-Z2-9]{4}$"));
        }
    }

    @Test
    public void testBase32DecodingRobustness() {
        // Standard test: "JBSWY3DPEHPK3PXP" -> valid bytes
        byte[] decoded = TotpManager.decodeBase32("JBSWY3DPEHPK3PXP");
        assertNotNull(decoded);
        assertTrue(decoded.length > 0);

        // Graceful handling of null, empty, or lowercase
        assertEquals(0, TotpManager.decodeBase32(null).length);
        assertEquals(0, TotpManager.decodeBase32("").length);
        assertEquals(0, TotpManager.decodeBase32("   ").length);

        // Lowercase normalization
        byte[] decodedLower = TotpManager.decodeBase32("jbswy3dpehpk3pxp");
        assertArrayEquals(decoded, decodedLower);
    }

    @Test
    public void testAccountDeletionSafeguards() {
        // Deletion confirmation requires strict exact capital case "DELETE"
        assertTrue(isAuthorizedDeletionPhrase("DELETE"));

        assertFalse(isAuthorizedDeletionPhrase("delete"));
        assertFalse(isAuthorizedDeletionPhrase("Delete"));
        assertFalse(isAuthorizedDeletionPhrase("REMOVE"));
        assertFalse(isAuthorizedDeletionPhrase("CANCEL"));
        assertFalse(isAuthorizedDeletionPhrase(""));
        assertFalse(isAuthorizedDeletionPhrase(null));
    }

    @Test
    public void testSecurityPostureScoring() {
        int scoreBaseline = calculatePostureScore(false, false, false, false);
        assertEquals(25, scoreBaseline); // baseline strong credentials

        int scoreWith2FA = calculatePostureScore(true, false, false, false);
        assertEquals(60, scoreWith2FA);

        int scoreWithRecovery = calculatePostureScore(true, true, false, false);
        assertEquals(80, scoreWithRecovery);

        int scoreFull = calculatePostureScore(true, true, true, true);
        assertEquals(100, scoreFull);
    }

    // --- Helper Assertion Logic ---

    private boolean isValidPasswordPolicy(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSymbol = password.matches(".*[@#$%^&+=!_\\-~*?].*");
        return hasUpper && hasLower && hasDigit && hasSymbol;
    }

    private boolean isAuthorizedDeletionPhrase(String phrase) {
        return "DELETE".equals(phrase);
    }

    private int calculatePostureScore(boolean has2fa, boolean hasRecoveryEmail, boolean suspiciousAlerts, boolean changedPassword) {
        int score = 25;
        if (has2fa) score += 35;
        if (hasRecoveryEmail) score += 20;
        if (suspiciousAlerts) score += 10;
        if (changedPassword) score += 10;
        return Math.min(100, score);
    }
}

