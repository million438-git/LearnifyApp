package com.million.learnify;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PasswordUtils - Cryptographically secure password hashing and verification utility.
 *
 * Implements PBKDF2 with HMAC-SHA-256, cryptographically secure random salts,
 * constant-time verification, and controlled legacy plaintext migration.
 */
public final class PasswordUtils {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "$pbkdf2-sha256$";
    private static final int ITERATIONS = 10000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final char[] BASE64_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private PasswordUtils() {
        // Utility class; prevent instantiation
    }

    /**
     * Hashes a plaintext password using PBKDF2 with HMAC-SHA-256 and a fresh random salt.
     *
     * @param password Plaintext password to hash.
     * @return Serialized hash in standard modular format: $pbkdf2-sha256$<iterations>$<salt_b64>$<hash_b64>
     */
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }

        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        return PREFIX + ITERATIONS + "$" + encodeBase64(salt) + "$" + encodeBase64(hash);
    }

    /**
     * Verifies a candidate password against a stored password string.
     * Supports both PBKDF2 hashes and legacy plaintext records (for controlled migration).
     *
     * @param candidatePassword The password entered by the user.
     * @param storedPassword    The password string stored in the database.
     * @return true if credentials match; false otherwise. Fails closed on any error.
     */
    public static boolean verifyPassword(String candidatePassword, String storedPassword) {
        if (candidatePassword == null || storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }

        if (isHashed(storedPassword)) {
            try {
                String content = storedPassword.substring(PREFIX.length());
                String[] parts = content.split("\\$");
                if (parts.length != 3) {
                    return false; // Malformed format, fail closed
                }

                int iterations = Integer.parseInt(parts[0]);
                if (iterations < 1000) {
                    return false; // Unacceptable work factor, fail closed
                }

                byte[] salt = decodeBase64(parts[1]);
                byte[] expectedHash = decodeBase64(parts[2]);

                if (salt.length < 8 || expectedHash.length < 16) {
                    return false; // Inadequate key or salt length, fail closed
                }

                byte[] candidateHash = pbkdf2(candidatePassword.toCharArray(), salt, iterations, expectedHash.length * 8);

                return MessageDigest.isEqual(expectedHash, candidateHash);
            } catch (Exception e) {
                // Fail closed on any parsing, format, or cryptographic error
                return false;
            }
        } else {
            // Legacy plaintext fallback: use constant-time comparison to prevent timing leaks
            byte[] candidateBytes = candidatePassword.getBytes(StandardCharsets.UTF_8);
            byte[] storedBytes = storedPassword.getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(candidateBytes, storedBytes);
        }
    }

    /**
     * Checks if the stored password string is already in the PBKDF2 hashed format.
     */
    public static boolean isHashed(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(PREFIX);
    }

    /**
     * Determines whether a stored password requires an automatic upgrade to PBKDF2.
     */
    public static boolean needsUpgrade(String storedPassword) {
        return storedPassword != null && !isHashed(storedPassword);
    }

    /**
     * Generates a cryptographically strong unpredictable placeholder for accounts that do not
     * use password authentication (e.g. social login), preventing credential guessing.
     */
    public static String generateRandomPlaceholder() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return PREFIX + "social$" + encodeBase64(randomBytes);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException("Cryptographic error generating password hash", e);
        }
    }

    /**
     * Self-contained Base64 encoder compatible with all Android API levels and host JVM unit tests.
     */
    static String encodeBase64(byte[] data) {
        if (data == null) return "";
        StringBuilder sb = new StringBuilder((data.length * 4 + 2) / 3);
        int i = 0;
        while (i < data.length) {
            int b0 = data[i++] & 0xFF;
            int b1 = (i < data.length) ? (data[i++] & 0xFF) : -1;
            int b2 = (i < data.length) ? (data[i++] & 0xFF) : -1;

            sb.append(BASE64_ALPHABET[b0 >>> 2]);
            if (b1 != -1) {
                sb.append(BASE64_ALPHABET[((b0 & 0x03) << 4) | (b1 >>> 4)]);
                if (b2 != -1) {
                    sb.append(BASE64_ALPHABET[((b1 & 0x0F) << 2) | (b2 >>> 6)]);
                    sb.append(BASE64_ALPHABET[b2 & 0x3F]);
                } else {
                    sb.append(BASE64_ALPHABET[(b1 & 0x0F) << 2]);
                    sb.append('=');
                }
            } else {
                sb.append(BASE64_ALPHABET[(b0 & 0x03) << 4]);
                sb.append("==");
            }
        }
        return sb.toString();
    }

    /**
     * Self-contained Base64 decoder compatible with all Android API levels and host JVM unit tests.
     */
    static byte[] decodeBase64(String s) {
        if (s == null) return new byte[0];
        s = s.replaceAll("\\s+", "");
        if (s.isEmpty()) return new byte[0];

        int pad = 0;
        for (int i = s.length() - 1; i >= 0 && s.charAt(i) == '='; i--) {
            pad++;
        }
        int len = (s.length() * 6) / 8 - pad;
        if (len < 0) return new byte[0];
        byte[] result = new byte[len];

        int buffer = 0;
        int bits = 0;
        int idx = 0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '=') break;
            int val;
            if (c >= 'A' && c <= 'Z') val = c - 'A';
            else if (c >= 'a' && c <= 'z') val = c - 'a' + 26;
            else if (c >= '0' && c <= '9') val = c - '0' + 52;
            else if (c == '+') val = 62;
            else if (c == '/') val = 63;
            else return new byte[0]; // invalid character, fail closed

            buffer = (buffer << 6) | val;
            bits += 6;
            if (bits >= 8) {
                bits -= 8;
                if (idx < len) {
                    result[idx++] = (byte) ((buffer >>> bits) & 0xFF);
                }
            }
        }
        return result;
    }
}

