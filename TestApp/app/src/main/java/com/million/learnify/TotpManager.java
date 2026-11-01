package com.million.learnify;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * TotpManager - Production RFC 6238 Time-based One-Time Password Engine.
 * Supports cryptographically secure Base32 secret generation,
 * standard otpauth URI formatting for Authenticator applications,
 * 6-digit TOTP verification with clock drift tolerance windows,
 * and single-use alphanumeric backup recovery code generation.
 */
public class TotpManager {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int DIGIT_MODULO = 1000000;

    /**
     * Generates a cryptographically random 16-character Base32 secret key.
     */
    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(BASE32_ALPHABET.charAt(random.nextInt(BASE32_ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
     * Generates a standard RFC 6238 provisioning URI for Authenticator apps.
     */
    public static String getProvisioningUri(String accountName, String secret) {
        String cleanUser = (accountName != null && !accountName.trim().isEmpty())
                ? accountName.trim()
                : "Student";
        return String.format(Locale.US,
                "otpauth://totp/Learnify:%s?secret=%s&issuer=Learnify&algorithm=SHA1&digits=%d&period=%d",
                cleanUser, secret, CODE_DIGITS, TIME_STEP_SECONDS);
    }

    /**
     * Verifies a candidate 6-digit code against the Base32 secret with clock drift tolerance.
     * Checks windows: -1 (past 30s), 0 (current 30s), +1 (next 30s).
     */
    public static boolean verifyCode(String secretBase32, String codeStr) {
        if (secretBase32 == null || codeStr == null) return false;
        String cleanCode = codeStr.trim().replaceAll("\\s+", "");
        if (cleanCode.length() != CODE_DIGITS) return false;

        int candidateCode;
        try {
            candidateCode = Integer.parseInt(cleanCode);
        } catch (NumberFormatException e) {
            return false;
        }

        byte[] key = decodeBase32(secretBase32);
        if (key.length == 0) return false;

        long nowSeconds = System.currentTimeMillis() / 1000L;

        // Check -1, 0, and +1 time windows for clock skew tolerance
        for (int window = -1; window <= 1; window++) {
            long checkTime = nowSeconds + (window * TIME_STEP_SECONDS);
            int generatedCode = generateCodeForTime(key, checkTime);
            if (generatedCode == candidateCode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the TOTP integer code for a given timestamp using HMAC-SHA1.
     */
    public static int generateCodeForTime(byte[] key, long timeSeconds) {
        long step = timeSeconds / TIME_STEP_SECONDS;
        byte[] data = new byte[8];
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (step & 0xFF);
            step >>= 8;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            return binary % DIGIT_MODULO;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Generates a list of single-use backup recovery codes formatted like `ABCD-1234`.
     */
    public static List<String> generateRecoveryCodes(int count) {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Unambiguous alphanumeric
        List<String> codes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder(9);
            for (int j = 0; j < 8; j++) {
                if (j == 4) sb.append("-");
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            codes.add(sb.toString());
        }
        return codes;
    }

    /**
     * Decodes a Base32 string into binary bytes.
     */
    public static byte[] decodeBase32(String base32) {
        if (base32 == null) return new byte[0];
        String clean = base32.toUpperCase(Locale.US).replaceAll("[^A-Z2-7]", "");
        if (clean.isEmpty()) return new byte[0];

        ByteBuffer buffer = ByteBuffer.allocate((clean.length() * 5) / 8 + 1);
        int currentByte = 0;
        int bitsRemaining = 8;

        for (int i = 0; i < clean.length(); i++) {
            int val = BASE32_ALPHABET.indexOf(clean.charAt(i));
            if (val < 0) continue;

            if (bitsRemaining > 5) {
                currentByte = (currentByte << 5) | val;
                bitsRemaining -= 5;
            } else {
                int mask = 5 - bitsRemaining;
                currentByte = (currentByte << bitsRemaining) | (val >> mask);
                buffer.put((byte) currentByte);
                currentByte = val & ((1 << mask) - 1);
                bitsRemaining = 8 - mask;
            }
        }

        byte[] result = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, result, 0, result.length);
        return result;
    }
}

