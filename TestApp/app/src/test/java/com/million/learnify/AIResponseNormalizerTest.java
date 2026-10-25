package com.million.learnify;

import org.junit.Test;
import static org.junit.Assert.*;

public class AIResponseNormalizerTest {

    @Test
    public void testRemoveDecorativeHeadingEmojis() {
        String input = "📘 Topic Overview\nPhotosynthesis converts light energy into chemical energy.\n\n"
                     + "🧠 Key Points\n• Occurs in chloroplasts\n• Uses chlorophyll\n\n"
                     + "🧪 Simple Example\nAquatic plant in sunlight produces oxygen bubbles.\n\n"
                     + "📝 Quick Summary\nPlants produce their own food and oxygen.";

        String normalized = AIResponseNormalizer.normalize(input);

        assertFalse(normalized.contains("📘"));
        assertFalse(normalized.contains("🧠"));
        assertFalse(normalized.contains("🧪"));
        assertFalse(normalized.contains("📝"));

        assertTrue(normalized.contains("Topic Overview"));
        assertTrue(normalized.contains("Key Points"));
        assertTrue(normalized.contains("Simple Example"));
        assertTrue(normalized.contains("Quick Summary"));
        assertTrue(normalized.contains("• Occurs in chloroplasts"));
    }

    @Test
    public void testStripConversationalIntro() {
        String input = "Certainly! Here is an explanation of Newton's laws:\n\nTopic Overview\nObjects remain at rest...";
        String normalized = AIResponseNormalizer.normalize(input);

        assertFalse(normalized.toLowerCase().contains("certainly"));
        assertTrue(normalized.startsWith("Topic Overview"));

        String input2 = "Hello! I'd be happy to help you with that!\n\nTopic Overview\nMitochondria produce ATP...";
        String normalized2 = AIResponseNormalizer.normalize(input2);
        assertFalse(normalized2.toLowerCase().contains("happy to help"));
        assertTrue(normalized2.startsWith("Topic Overview"));
    }

    @Test
    public void testRemoveBotEmojisFromOfflineAndStatus() {
        String input = "Offline AI Tutor 🤖\n\nPhotosynthesis details...";
        String normalized = AIResponseNormalizer.normalize(input);

        assertFalse(normalized.contains("🤖"));
        assertTrue(normalized.contains("Offline AI Tutor"));

        String quiz = "Offline AI Challenge 🏆\n\nCan you solve these?";
        String normalizedQuiz = AIResponseNormalizer.normalize(quiz);
        assertFalse(normalizedQuiz.contains("🏆"));
        assertTrue(normalizedQuiz.contains("Offline AI Challenge"));
    }

    @Test
    public void testPreserveMathAndScience() {
        String input = "Topic Overview\nDerivative definition:\n"
                     + "$f'(x) = \\lim_{h \\to 0} \\frac{f(x+h) - f(x)}{h}$\n\n"
                     + "Key Points\n"
                     + "• Reaction: $2H_2 + O_2 \\rightarrow 2H_2O$\n"
                     + "• Temperature: $25^\\circ\\text{C}$";

        String normalized = AIResponseNormalizer.normalize(input);

        assertTrue(normalized.contains("$f'(x) = \\lim_{h \\to 0} \\frac{f(x+h) - f(x)}{h}$"));
        assertTrue(normalized.contains("$2H_2 + O_2 \\rightarrow 2H_2O$"));
        assertTrue(normalized.contains("$25^\\circ\\text{C}$"));
    }

    @Test
    public void testPreserveCodeFences() {
        String input = "Topic Overview\nHere is code:\n```java\nint x = 10;\n```";
        String normalized = AIResponseNormalizer.normalize(input);

        assertTrue(normalized.contains("```java\nint x = 10;\n```"));
    }

    @Test
    public void testIdempotence() {
        String input = "📘 Topic Overview\n$E = mc^2$\n🧠 Key Points\n• Energy equals mass times $c^2$.";
        String once = AIResponseNormalizer.normalize(input);
        String twice = AIResponseNormalizer.normalize(once);

        assertEquals(once, twice);
    }
}

