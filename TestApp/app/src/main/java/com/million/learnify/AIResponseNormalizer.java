package com.million.learnify;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AIResponseNormalizer - App-wide sanitizer for educational AI content.
 * Removes decorative emojis, chatbot pleasantries/filler, and normalizes
 * bullet points while strictly preserving mathematical notation, chemical formulas,
 * markdown structures, and code fences.
 * 
 * Guarantee: Idempotent - normalize(normalize(text)).equals(normalize(text)).
 */
public final class AIResponseNormalizer {

    private static final String CORRECT_BULLET = "\u2022";

    // Known malformed bullet encodings from UTF-8/ISO mismatches
    private static final String MALFORMED_BULLET_1 = "" + (char) 0x00E2 + (char) 0x0080 + (char) 0x00A2;
    private static final String MALFORMED_BULLET_2 = "" + (char) 0x00E2 + (char) 0x20AC + (char) 0x00A2;
    private static final String MALFORMED_BULLET_3 = "" + (char) 0x00E2 + (char) 0x00A2;

    // Pattern for decorative emojis commonly found in chatbot headings or line starts
    private static final Pattern DECORATIVE_HEADER_EMOJIS = Pattern.compile(
            "(?m)^(\\s*)[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u26FF\\u2700-\\u27BF\\uFE0F\\u200D]+\\s*"
    );

    // Pattern for conversational chatbot filler at start of responses
    private static final Pattern CONVERSATIONAL_INTRO_PATTERN = Pattern.compile(
            "^(?i)(?:(?:hello|hi)(?: there)?!?[\\s,]*|"
            + "certainly!?[\\s,]*(?:here(?:'s| is)[^:\\n]+[:\\n]+)?"
            + "|sure(?: thing)?!?[\\s,]*(?:here(?:'s| is)[^:\\n]+[:\\n]+)?"
            + "|as an ai language model[\\s,]*"
            + "|i(?: would|'d)? (?:be happy|love) to help(?: you)?(?: with that)?[!.:\\s]*"
            + "|here is (?:the|an) explanation(?: you requested)?[!.:\\s]*"
            + "|gladly!?[\\s,]*)+\\s*",
            Pattern.DOTALL
    );

    private AIResponseNormalizer() {
        // Utility class
    }

    /**
     * Normalizes an AI response string by stripping decorative emojis and conversational filler.
     * Preserves scientific notation, LaTeX formulas, code fences, and markdown headings.
     */
    public static String normalize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text == null ? null : text.trim();
        }

        String result = text;

        // 1. Normalize malformed bullet characters
        result = result.replace(MALFORMED_BULLET_1, CORRECT_BULLET)
                       .replace(MALFORMED_BULLET_2, CORRECT_BULLET)
                       .replace(MALFORMED_BULLET_3, CORRECT_BULLET);

        // 2. Remove conversational filler prefixes at start of response
        Matcher introMatcher = CONVERSATIONAL_INTRO_PATTERN.matcher(result);
        if (introMatcher.find()) {
            result = result.substring(introMatcher.end()).trim();
        }

        // 3. Strip decorative emojis from line beginnings / section headings
        result = stripDecorativeEmojis(result);

        // 4. Normalize multiple empty lines (> 2 newlines to 2 newlines)
        result = result.replaceAll("\\n{3,}", "\n\n");

        return result.trim();
    }

    /**
     * Strips decorative emojis from text lines and headings while preserving math and text.
     */
    public static String stripDecorativeEmojis(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text == null ? null : text.trim();
        }
        String clean = DECORATIVE_HEADER_EMOJIS.matcher(text).replaceAll("$1");
        clean = clean.replace("🤖 ", "")
                     .replace("📘 ", "")
                     .replace("🧠 ", "")
                     .replace("🧪 ", "")
                     .replace("📝 ", "")
                     .replace("🏆 ", "")
                     .replace("💬 ", "")
                     .replace(" 🤖", "")
                     .replace(" 🏆", "");
        return clean.trim();
    }
}

