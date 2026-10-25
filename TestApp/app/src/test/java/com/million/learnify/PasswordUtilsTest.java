package com.million.learnify;

import org.junit.Test;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Unit tests for PasswordUtils: PBKDF2 hashing, verification, legacy migration, and error handling.
 */
public class PasswordUtilsTest {

    @Test
    public void testHashPassword_generatesValidFormat() {
        String hash = PasswordUtils.hashPassword("MySecurePass123!");
        assertNotNull(hash);
        assertTrue(PasswordUtils.isHashed(hash));
        assertTrue(hash.startsWith("$pbkdf2-sha256$10000$"));
        assertFalse(PasswordUtils.needsUpgrade(hash));
    }

    @Test
    public void testCorrectPassword_verifiesSuccessfully() {
        String password = "CorrectPassword2026#";
        String hash = PasswordUtils.hashPassword(password);
        assertTrue(PasswordUtils.verifyPassword(password, hash));
    }

    @Test
    public void testIncorrectPassword_failsVerification() {
        String hash = PasswordUtils.hashPassword("CorrectPassword2026#");
        assertFalse(PasswordUtils.verifyPassword("WrongPassword", hash));
        assertFalse(PasswordUtils.verifyPassword("correctpassword2026#", hash)); // Case sensitivity check
    }

    @Test
    public void testIdenticalPasswords_receiveDifferentSaltsAndHashes() {
        String password = "StandardPassword";
        String hash1 = PasswordUtils.hashPassword(password);
        String hash2 = PasswordUtils.hashPassword(password);

        assertNotEquals("Identical passwords must receive different salts and hashes", hash1, hash2);
        assertTrue(PasswordUtils.verifyPassword(password, hash1));
        assertTrue(PasswordUtils.verifyPassword(password, hash2));
    }

    @Test
    public void testLegacyPlaintext_verifiesAndFlagsForUpgrade() {
        String legacyPlaintext = "admin123";

        assertFalse(PasswordUtils.isHashed(legacyPlaintext));
        assertTrue(PasswordUtils.needsUpgrade(legacyPlaintext));

        // Legacy match succeeds
        assertTrue(PasswordUtils.verifyPassword("admin123", legacyPlaintext));

        // Incorrect password fails
        assertFalse(PasswordUtils.verifyPassword("wrongpass", legacyPlaintext));
    }

    @Test
    public void testMalformedHash_failsClosed() {
        assertFalse(PasswordUtils.verifyPassword("pass", "$pbkdf2-sha256$not$enough$parts$here"));
        assertFalse(PasswordUtils.verifyPassword("pass", "$pbkdf2-sha256$invalid_iterations$salt$hash"));
        assertFalse(PasswordUtils.verifyPassword("pass", "$pbkdf2-sha256$10$toofewiterations$hash"));
        assertFalse(PasswordUtils.verifyPassword("pass", "$pbkdf2-sha256$10000$short$short"));
        assertFalse(PasswordUtils.verifyPassword("pass", "random_unformatted_string"));
        assertFalse(PasswordUtils.verifyPassword("pass", ""));
    }

    @Test
    public void testNullAndEmptyInputs_handledSafely() {
        String validHash = PasswordUtils.hashPassword("ValidPass123");

        assertFalse(PasswordUtils.verifyPassword(null, validHash));
        assertFalse(PasswordUtils.verifyPassword("", validHash));
        assertFalse(PasswordUtils.verifyPassword("ValidPass123", null));
        assertFalse(PasswordUtils.verifyPassword(null, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHashPassword_nullInputThrows() {
        PasswordUtils.hashPassword(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHashPassword_emptyInputThrows() {
        PasswordUtils.hashPassword("   ");
    }

    @Test
    public void testRandomPlaceholder_cannotBeVerified() {
        String placeholder = PasswordUtils.generateRandomPlaceholder();
        assertNotNull(placeholder);
        assertTrue(PasswordUtils.isHashed(placeholder));

        assertFalse(PasswordUtils.verifyPassword("social_verified", placeholder));
        assertFalse(PasswordUtils.verifyPassword("", placeholder));
        assertFalse(PasswordUtils.verifyPassword("admin123", placeholder));
        assertFalse(PasswordUtils.verifyPassword("123456", placeholder));
    }

    @Test
    public void testBase64RoundTrip() {
        byte[][] testData = new byte[][] {
            new byte[0],
            new byte[] { 1 },
            new byte[] { 1, 2 },
            new byte[] { 1, 2, 3 },
            new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0xFD, 0x00, 0x10 },
            "Testing Base64 Serialization & Deserialization 1234567890".getBytes(StandardCharsets.UTF_8)
        };

        for (byte[] data : testData) {
            String encoded = PasswordUtils.encodeBase64(data);
            byte[] decoded = PasswordUtils.decodeBase64(encoded);
            assertArrayEquals("Failed round-trip for length: " + data.length, data, decoded);
        }
    }
}

