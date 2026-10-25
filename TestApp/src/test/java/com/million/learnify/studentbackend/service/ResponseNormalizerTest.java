package com.million.learnify.studentbackend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseNormalizerTest {

    @Test
    @DisplayName("Should remove decorative emojis from standard 4-part AI headings")
    void testRemoveDecorativeHeadingEmojis() {
        String input = """
                📘 Topic Overview
                Photosynthesis is the biological process by which plants synthesize nutrients.

                🧠 Key Points
                • Uses sunlight, carbon dioxide, and water
                • Produces glucose and oxygen

                🧪 Simple Example
                Green leaves placed in sunlight produce oxygen bubbles in an aquatic beaker.

                📝 Quick Summary
                Photosynthesis converts radiant solar energy into stable chemical energy.
                """;

        String normalized = ResponseNormalizer.normalize(input);

        assertFalse(normalized.contains("📘"));
        assertFalse(normalized.contains("🧠"));
        assertFalse(normalized.contains("🧪"));
        assertFalse(normalized.contains("📝"));

        assertTrue(normalized.contains("Topic Overview"));
        assertTrue(normalized.contains("Key Points"));
        assertTrue(normalized.contains("Simple Example"));
        assertTrue(normalized.contains("Quick Summary"));
        assertTrue(normalized.contains("• Uses sunlight"));
    }

    @Test
    @DisplayName("Should strip conversational filler intros")
    void testStripConversationalIntro() {
        String input = "Certainly! Here is an explanation of Newton's laws:\n\nTopic Overview\nObjects remain at rest...";
        String normalized = ResponseNormalizer.normalize(input);

        assertFalse(normalized.toLowerCase().contains("certainly"));
        assertTrue(normalized.startsWith("Topic Overview"));

        String input2 = "Hello! I'd be happy to help you with that!\n\nTopic Overview\nMitochondria are the powerhouse...";
        String normalized2 = ResponseNormalizer.normalize(input2);
        assertFalse(normalized2.toLowerCase().contains("happy to help"));
        assertTrue(normalized2.startsWith("Topic Overview"));
    }

    @Test
    @DisplayName("Should remove bot emojis from status and error messages")
    void testRemoveBotEmojisFromStatus() {
        String notice = "🤖 EduAI Configuration Notice:\n\nKey missing.";
        String normalized = ResponseNormalizer.normalize(notice);

        assertFalse(normalized.contains("🤖"));
        assertTrue(normalized.contains("EduAI Configuration Notice:"));

        String error = "🤖 EduAI Error: Network timeout.";
        String normalizedErr = ResponseNormalizer.normalize(error);
        assertFalse(normalizedErr.contains("🤖"));
        assertEquals("EduAI Error: Network timeout.", normalizedErr);
    }

    @Test
    @DisplayName("Should preserve mathematical formulas, symbols, and chemical equations")
    void testPreserveScienceAndMath() {
        String input = """
                Topic Overview
                Calculus derivative definition:
                $f'(x) = \\lim_{h \\to 0} \\frac{f(x+h) - f(x)}{h}$

                Key Points
                • For $f(x) = x^2$, $f'(x) = 2x$.
                • Chemical combustion: $CH_4 + 2O_2 \\rightarrow CO_2 + 2H_2O$
                • Temperature: $25^\\circ\\text{C}$ to $100^\\circ\\text{C}$.

                Quick Summary
                Derivatives measure instantaneous rates of change.
                """;

        String normalized = ResponseNormalizer.normalize(input);

        assertTrue(normalized.contains("$f'(x) = \\lim_{h \\to 0} \\frac{f(x+h) - f(x)}{h}$"));
        assertTrue(normalized.contains("$CH_4 + 2O_2 \\rightarrow CO_2 + 2H_2O$"));
        assertTrue(normalized.contains("$25^\\circ\\text{C}$"));
    }

    @Test
    @DisplayName("Should preserve code fences and indentation")
    void testPreserveCodeFences() {
        String input = """
                Topic Overview
                Here is a Python function:
                ```python
                def solve(x, y):
                    return x ** 2 + y ** 2
                ```
                """;

        String normalized = ResponseNormalizer.normalize(input);
        assertTrue(normalized.contains("```python\ndef solve(x, y):\n    return x ** 2 + y ** 2\n```"));
    }

    @Test
    @DisplayName("Should guarantee idempotence")
    void testIdempotence() {
        String input = """
                🤖 EduAI Topic Overview
                📘 Newton's second law: $F = ma$.
                🧠 Key Points:
                • Force equals mass times acceleration.
                """;

        String once = ResponseNormalizer.normalize(input);
        String twice = ResponseNormalizer.normalize(once);

        assertEquals(once, twice);
    }
}

