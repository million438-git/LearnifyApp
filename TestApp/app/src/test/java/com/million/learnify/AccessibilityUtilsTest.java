package com.million.learnify;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * AccessibilityUtilsTest - Unit tests for phonetic math translation, HTML cleaning,
 * and scientific notation spoken accessibility.
 */
public class AccessibilityUtilsTest {

    @Test
    public void testCleanMathForSpeech_chemicalFormulas() {
        String input = "Water is composed of H2O and releases O2 during photosynthesis.";
        String result = AccessibilityUtils.cleanMathForSpeech(input);

        assertTrue("H2O must expand to phonetic speech", result.contains("H 2 O, water"));
        assertTrue("O2 must expand to oxygen gas", result.contains("O 2, oxygen gas"));
    }

    @Test
    public void testCleanMathForSpeech_complexChemicals() {
        String input = "The reaction produces CO2, NaCl, and H2SO4.";
        String result = AccessibilityUtils.cleanMathForSpeech(input);

        assertTrue("CO2 must expand to carbon dioxide", result.contains("C O 2, carbon dioxide"));
        assertTrue("NaCl must expand to sodium chloride", result.contains("N A C L, sodium chloride"));
        assertTrue("H2SO4 must expand to sulfuric acid", result.contains("H 2 S O 4, sulfuric acid"));
    }

    @Test
    public void testCleanMathForSpeech_physicsFormulasAndUnits() {
        String input = "Acceleration is 9.8 m/s^2 and velocity is 25 m/s.";
        String result = AccessibilityUtils.cleanMathForSpeech(input);

        assertTrue("m/s^2 must expand to meters per second squared", result.contains("meters per second squared"));
        assertTrue("m/s must expand to meters per second", result.contains("meters per second"));
    }

    @Test
    public void testCleanMathForSpeech_latexPowersAndFractions() {
        String input = "$E = mc^2$ and $a = \\frac{F}{m}$.";
        String result = AccessibilityUtils.cleanMathForSpeech(input);

        assertTrue("c^2 should be converted to squared", result.contains("squared"));
        assertTrue("Fractions should expand clearly", result.contains("fraction F over m"));
        assertFalse("LaTeX dollar signs must be stripped", result.contains("$"));
    }

    @Test
    public void testCleanMathForSpeech_squareRootsAndGreekLetters() {
        String input = "Compute \\sqrt{x} with angle \\theta and wavelength \\lambda.";
        String result = AccessibilityUtils.cleanMathForSpeech(input);

        assertTrue("Square root must expand", result.contains("square root of x"));
        assertTrue("Theta must expand phonetically", result.contains("theta"));
        assertTrue("Lambda must expand phonetically", result.contains("lambda"));
    }

    @Test
    public void testCleanHtmlForSpeech_stripsTagsAndStyles() {
        String html = "<html><head><style>body { color: red; }</style></head>"
                    + "<body><h1>Photosynthesis</h1><p>Process in <b>green plants</b>.</p>"
                    + "<ul><li>Uses sunlight</li><li>Releases O2</li></ul></body></html>";
        String result = AccessibilityUtils.cleanHtmlForSpeech(html);

        assertFalse("HTML tags must be stripped", result.contains("<h1>") || result.contains("<p>"));
        assertFalse("CSS styles must be stripped", result.contains("color: red"));
        assertTrue("Main text must be preserved", result.contains("Photosynthesis"));
        assertTrue("List items must be preserved", result.contains("Uses sunlight"));
        assertTrue("O2 chemical formula in HTML must be translated", result.contains("O 2, oxygen gas"));
    }

    @Test
    public void testCleanMathForSpeech_nullOrEmptySafelyHandled() {
        assertEquals("", AccessibilityUtils.cleanMathForSpeech(null));
        assertEquals("", AccessibilityUtils.cleanMathForSpeech("   "));
        assertEquals("", AccessibilityUtils.cleanHtmlForSpeech(null));
    }
}
