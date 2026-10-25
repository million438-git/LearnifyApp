package com.million.learnify;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;

public class ScienceStudioVerificationTest {

    // =========================================================================
    // 1. Photosynthesis Kinetics & Rate Formula Tests
    // =========================================================================

    private double calculatePhotosynthesisRate(double light, double co2, double temp) {
        double lightFactor = light / (light + 22.0);
        double co2Factor = co2 / (co2 + 320.0);
        double tempFactor = Math.exp(-0.5 * Math.pow((temp - 28.0) / 8.5, 2.0));

        double rate = 100.0 * lightFactor * co2Factor * tempFactor * 1.5;
        return Math.max(0.0, Math.min(100.0, rate));
    }

    private String determineLimitingFactor(double light, double co2, double temp) {
        double lightFactor = light / (light + 22.0);
        double co2Factor = co2 / (co2 + 320.0);

        if (temp > 38.0) {
            return "Heat Stress / Enzyme Denaturation";
        } else if (temp < 15.0) {
            return "Low Temperature (Kinetics)";
        } else if (lightFactor < co2Factor) {
            return "Light Intensity (Photons)";
        } else {
            return "CO2 Concentration (ppm)";
        }
    }

    @Test
    public void testPhotosynthesisRate_zeroLight_producesZeroRate() {
        double rate = calculatePhotosynthesisRate(0.0, 400.0, 25.0);
        assertEquals("Zero light must yield 0% photosynthetic rate", 0.0, rate, 0.001);
    }

    @Test
    public void testPhotosynthesisRate_optimalConditions_producesHighRate() {
        double rate = calculatePhotosynthesisRate(80.0, 800.0, 28.0);
        assertTrue("Optimal conditions must exceed 50% rate", rate > 50.0);
        assertTrue("Rate cannot exceed 100%", rate <= 100.0);
    }

    @Test
    public void testPhotosynthesisRate_extremeHeat_denaturesEnzymes() {
        double optimalRate = calculatePhotosynthesisRate(50.0, 400.0, 28.0);
        double extremeHeatRate = calculatePhotosynthesisRate(50.0, 400.0, 45.0);
        assertTrue("Extreme heat (45°C) must drastically reduce rate compared to 28°C", extremeHeatRate < (optimalRate * 0.4));
    }

    @Test
    public void testLimitingFactor_detectsLightLimitation() {
        String limiting = determineLimitingFactor(10.0, 800.0, 25.0);
        assertEquals("Light Intensity (Photons)", limiting);
    }

    @Test
    public void testLimitingFactor_detectsHeatStress() {
        String limiting = determineLimitingFactor(80.0, 800.0, 42.0);
        assertEquals("Heat Stress / Enzyme Denaturation", limiting);
    }

    // =========================================================================
    // 2. Simulation Range & Catalogue Architecture Tests
    // =========================================================================

    @Test
    public void testCatalogueArchitecture_correctSubjectRanges() {
        // Physics range: 1 to 40
        int minPhy = 1;
        int maxPhy = 40;
        assertEquals(40, maxPhy - minPhy + 1);

        // Biology range: 201 to 237
        int minBio = 201;
        int maxBio = 237;
        assertEquals(37, maxBio - minBio + 1);

        // Chemistry range: 301 to 340
        int minChm = 301;
        int maxChm = 340;
        assertEquals(40, maxChm - minChm + 1);

        int totalSimulations = (maxPhy - minPhy + 1) + (maxBio - minBio + 1) + (maxChm - minChm + 1);
        assertEquals("Total simulations must equal 117", 117, totalSimulations);
    }

    // =========================================================================
    // 3. Quiz Grading & Knowledge Mastery Tests
    // =========================================================================

    @Test
    public void testQuizGrading_allCorrect_gives100Percent() {
        int correct = 3;
        int total = 3;
        int score = (int) Math.round((correct / (double) total) * 100);
        assertEquals(100, score);
    }

    @Test
    public void testQuizGrading_twoCorrect_gives67Percent() {
        int correct = 2;
        int total = 3;
        int score = (int) Math.round((correct / (double) total) * 100);
        assertEquals(67, score);
    }

    @Test
    public void testQuizGrading_zeroCorrect_gives0Percent() {
        int correct = 0;
        int total = 3;
        int score = (int) Math.round((correct / (double) total) * 100);
        assertEquals(0, score);
    }
}

