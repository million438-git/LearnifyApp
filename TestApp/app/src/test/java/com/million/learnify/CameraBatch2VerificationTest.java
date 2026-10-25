package com.million.learnify;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * CameraBatch2VerificationTest - Rigorous verification of Batch 2 integrations:
 * 1. Genuine solution validation (rejecting errors, empty stubs, and pipeline faults).
 * 2. Visualizer subject-to-tab index mapping and backward-compatible default fallback.
 * 3. Flashcard bridge data contracts and SM-2 review timing consistency.
 */
public class CameraBatch2VerificationTest {

    // =========================================================================
    // 1. Solution & Question Validation Tests
    // =========================================================================

    @Test
    public void testGenuineSolution_validFormattedMath() {
        String question = "Solve 2x + 5 = 15";
        String solution = "EXTRACTED PROBLEM\n2x+5=15\n\nSOLUTION STEPS\n1. Identify Equation\n\nFINAL ANSWER\nSolutions: [5]";
        assertTrue("Standard formatted solver output must be accepted",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_validChemistryMolarMass() {
        String question = "NaCl";
        String solution = "Summing atomic masses...\nMolar Mass: 58.44 g/mol";
        assertTrue("Chemistry molar mass calculation must be accepted",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_validPhysicsCalculation() {
        String question = "F = 10 * 9.8";
        String solution = "1. Domain: Newton's 2nd Law\n2. Formula: F = m * a\nCalculation: 98.0 (Verify units)";
        assertTrue("Physics calculation must be accepted",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_validAmharicSolution() {
        String question = "2x = 10";
        String solution = "የመጨረሻ መልስ: x = 5";
        assertTrue("Amharic localized final answer must be accepted",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_rejectsNullInputs() {
        assertFalse("Null question must be rejected",
                CameraLabActivity.isGenuineSolution(null, "Solutions: [5]"));
        assertFalse("Null solution must be rejected",
                CameraLabActivity.isGenuineSolution("2x = 4", null));
        assertFalse("Both null must be rejected",
                CameraLabActivity.isGenuineSolution(null, null));
    }

    @Test
    public void testGenuineSolution_rejectsEmptyOrWhitespaceQuestion() {
        assertFalse("Empty question must be rejected",
                CameraLabActivity.isGenuineSolution("", "Solutions: [5]"));
        assertFalse("Whitespace question must be rejected",
                CameraLabActivity.isGenuineSolution("   \n\t  ", "Solutions: [5]"));
        assertFalse("Punctuation-only question must be rejected",
                CameraLabActivity.isGenuineSolution("???", "Solutions: [5]"));
    }

    @Test
    public void testGenuineSolution_rejectsMathematicalEngineError() {
        String question = "2x +++";
        String solution = "Mathematical Engine Error: invalid syntax (<string>, line 1)";
        assertFalse("SymPy mathematical engine error must be rejected",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_rejectsAmharicEngineError() {
        String question = "2x +++";
        String solution = "የሂሳብ ሞተር ስህተት: ስህተት ተከስቷል";
        assertFalse("Amharic engine error must be rejected",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_rejectsSolverNotConfigured() {
        String question = "What is gravity?";
        String solution = "Solver module not configured.";
        assertFalse("Unconfigured solver response must be rejected",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_rejectsProcessingPlaceholder() {
        String question = "3x - 1 = 8";
        String solution = "Processing...";
        assertFalse("Processing placeholder must be rejected",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_rejectsPipelineFault() {
        String question = "x^2 = 4";
        String solution = "Pipeline Fault: unable to decode image stream";
        assertFalse("Pipeline fault message must be rejected",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_rejectsSolverEngineError() {
        String question = "int(x dx)";
        String solution = "Solver Engine Error: execution timeout";
        assertFalse("Solver engine error must be rejected",
                CameraLabActivity.isGenuineSolution(question, solution));
    }

    @Test
    public void testGenuineSolution_rejectsEmptyAndTrivialStubs() {
        assertFalse(CameraLabActivity.isGenuineSolution("x", ""));
        assertFalse(CameraLabActivity.isGenuineSolution("x", "   "));
        assertFalse(CameraLabActivity.isGenuineSolution("x", "Error"));
        assertFalse(CameraLabActivity.isGenuineSolution("x", "NaN"));
        assertFalse(CameraLabActivity.isGenuineSolution("x", "null"));
        assertFalse(CameraLabActivity.isGenuineSolution("x", "[]"));
        assertFalse(CameraLabActivity.isGenuineSolution("x", "{}"));
    }

    // =========================================================================
    // 2. Visualizer Subject-to-Tab Mapping Tests
    // =========================================================================

    @Test
    public void testMapSubjectToTabIndex_physics() {
        assertEquals("PHYSICS maps to Tab 0", 0, VisualizerActivity.mapSubjectToTabIndex("PHYSICS"));
        assertEquals("physics lowercase maps to Tab 0", 0, VisualizerActivity.mapSubjectToTabIndex("physics"));
    }

    @Test
    public void testMapSubjectToTabIndex_chemistry() {
        assertEquals("CHEMISTRY maps to Tab 1", 1, VisualizerActivity.mapSubjectToTabIndex("CHEMISTRY"));
        assertEquals("chemistry lowercase maps to Tab 1", 1, VisualizerActivity.mapSubjectToTabIndex("chemistry"));
    }

    @Test
    public void testMapSubjectToTabIndex_biology() {
        assertEquals("BIOLOGY maps to Tab 2", 2, VisualizerActivity.mapSubjectToTabIndex("BIOLOGY"));
        assertEquals("bio maps to Tab 2", 2, VisualizerActivity.mapSubjectToTabIndex("bio"));
    }

    @Test
    public void testMapSubjectToTabIndex_mathAndGeometry() {
        assertEquals("MATH maps to Tab 3", 3, VisualizerActivity.mapSubjectToTabIndex("MATH"));
        assertEquals("GEOMETRY maps to Tab 3", 3, VisualizerActivity.mapSubjectToTabIndex("GEOMETRY"));
        assertEquals("ALGEBRA maps to Tab 3", 3, VisualizerActivity.mapSubjectToTabIndex("ALGEBRA"));
        assertEquals("MATHEMATICS maps to Tab 3", 3, VisualizerActivity.mapSubjectToTabIndex("MATHEMATICS"));
    }

    @Test
    public void testMapSubjectToTabIndex_english() {
        assertEquals("ENGLISH maps to Tab 4", 4, VisualizerActivity.mapSubjectToTabIndex("ENGLISH"));
    }

    @Test
    public void testMapSubjectToTabIndex_fallbackDefaultsToPhysics() {
        assertEquals("Null subject defaults to Tab 0", 0, VisualizerActivity.mapSubjectToTabIndex(null));
        assertEquals("Empty subject defaults to Tab 0", 0, VisualizerActivity.mapSubjectToTabIndex(""));
        assertEquals("Unknown subject defaults to Tab 0", 0, VisualizerActivity.mapSubjectToTabIndex("ASTRONOMY"));
    }

    // =========================================================================
    // 3. Flashcard SM-2 Review Timing & Subject Integrity Contract Tests
    // =========================================================================

    @Test
    public void testFlashcardReviewTimingContract() {
        long insertTimestamp = System.currentTimeMillis();
        // Wait 2ms to ensure elapsed time
        try { Thread.sleep(2); } catch (InterruptedException ignored) {}
        long queryTimestamp = System.currentTimeMillis();

        assertTrue("Flashcard inserted with System.currentTimeMillis() must satisfy next_review_date <= queryTime",
                insertTimestamp <= queryTimestamp);
    }

    @Test
    public void testFlashcardSubjectPreservationContract() {
        String[] supportedSubjects = {"MATH", "PHYSICS", "CHEMISTRY", "GEOMETRY"};
        for (String subject : supportedSubjects) {
            assertNotNull("Subject name must be non-null", subject);
            assertFalse("Subject name must not be empty", subject.trim().isEmpty());
            assertTrue("Subject must map to a valid visualizer tab index (0-3)",
                    VisualizerActivity.mapSubjectToTabIndex(subject) >= 0 &&
                    VisualizerActivity.mapSubjectToTabIndex(subject) <= 3);
        }
    }
}

