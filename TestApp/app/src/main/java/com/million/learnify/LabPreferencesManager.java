package com.million.learnify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import java.util.Locale;

/**
 * LabPreferencesManager - Coordinates simulation and virtual lab preferences.
 * Bridges Android settings with HTML5, Canvas, and Three.js simulations:
 * - Sound effects in virtual lab equipment
 * - Simulation rendering quality (high, balanced, low)
 * - Reduced motion preferences for kinetic physics and fluid dynamics
 * - Accessibility mode and high contrast for science simulations
 * - Control sensitivity for sliders, pulleys, and calipers
 */
public class LabPreferencesManager {

    private static final String TAG = "LabPreferencesManager";
    private static final String PREFS_LAB = "LearnifyLabPreferences";

    public static final String KEY_LAB_SOUND = "lab_sound_effects";
    public static final String KEY_LAB_QUALITY = "lab_simulation_quality";
    public static final String KEY_LAB_MOTION = "lab_reduce_motion";
    public static final String KEY_LAB_ACCESSIBILITY = "lab_accessibility_mode";
    public static final String KEY_LAB_SENSITIVITY = "lab_control_sensitivity";

    private static volatile LabPreferencesManager instance;
    private final Context appContext;
    private final SharedPreferences prefs;

    private LabPreferencesManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREFS_LAB, Context.MODE_PRIVATE);
    }

    public static LabPreferencesManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LabPreferencesManager.class) {
                if (instance == null) {
                    instance = new LabPreferencesManager(context);
                }
            }
        }
        return instance;
    }

    public boolean isSoundEffectsEnabled() {
        if (prefs.contains(KEY_LAB_SOUND)) {
            return prefs.getBoolean(KEY_LAB_SOUND, true);
        }
        // Fallback to global device sound effects preference
        SettingsService ss = SettingsService.getInstance(appContext);
        StudentSettings settings = ss != null ? ss.loadSettings() : null;
        return settings != null ? settings.isSoundEffects() : true;
    }

    public void setSoundEffectsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_LAB_SOUND, enabled).apply();
    }

    public String getSimulationQuality() {
        return prefs.getString(KEY_LAB_QUALITY, "balanced");
    }

    public void setSimulationQuality(String quality) {
        if ("high".equalsIgnoreCase(quality) || "low".equalsIgnoreCase(quality)) {
            prefs.edit().putString(KEY_LAB_QUALITY, quality.toLowerCase(Locale.US)).apply();
        } else {
            prefs.edit().putString(KEY_LAB_QUALITY, "balanced").apply();
        }
    }

    public boolean isReduceMotionEnabled() {
        if (prefs.contains(KEY_LAB_MOTION)) {
            return prefs.getBoolean(KEY_LAB_MOTION, false);
        }
        InclusiveModeManager imm = InclusiveModeManager.getInstance(appContext);
        return imm != null && imm.isReducedMotionEnabled();
    }

    public void setReduceMotionEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_LAB_MOTION, enabled).apply();
    }

    public boolean isLabAccessibilityEnabled() {
        if (prefs.contains(KEY_LAB_ACCESSIBILITY)) {
            return prefs.getBoolean(KEY_LAB_ACCESSIBILITY, false);
        }
        InclusiveModeManager imm = InclusiveModeManager.getInstance(appContext);
        return imm != null && imm.isInclusiveModeEnabled();
    }

    public void setLabAccessibilityEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_LAB_ACCESSIBILITY, enabled).apply();
    }

    public float getControlSensitivity() {
        return prefs.getFloat(KEY_LAB_SENSITIVITY, 1.0f);
    }

    public void setControlSensitivity(float sensitivity) {
        float clamped = Math.max(0.5f, Math.min(2.0f, sensitivity));
        prefs.edit().putFloat(KEY_LAB_SENSITIVITY, clamped).apply();
    }

    /**
     * Generates JavaScript configuration injection script.
     */
    public String generateConfigScript() {
        boolean sound = isSoundEffectsEnabled();
        String quality = getSimulationQuality();
        boolean motion = isReduceMotionEnabled();
        boolean accessibility = isLabAccessibilityEnabled();
        float sensitivity = getControlSensitivity();

        return String.format(Locale.US,
                "(function() {" +
                "  window.LEARNIFY_LAB_CONFIG = {" +
                "    soundEffects: %b," +
                "    simulationQuality: '%s'," +
                "    reduceMotion: %b," +
                "    accessibilityMode: %b," +
                "    controlSensitivity: %.2f" +
                "  };" +
                "  try {" +
                "    window.dispatchEvent(new CustomEvent('learnifyPreferencesLoaded', { detail: window.LEARNIFY_LAB_CONFIG }));" +
                "    if (typeof window.onLearnifyPreferencesUpdate === 'function') {" +
                "      window.onLearnifyPreferencesUpdate(window.LEARNIFY_LAB_CONFIG);" +
                "    }" +
                "  } catch(e) {}" +
                "})();",
                sound, quality, motion, accessibility, sensitivity);
    }

    /**
     * Injects preferences cleanly into an interactive simulation WebView.
     */
    public void applyToWebView(WebView webView) {
        if (webView == null) return;
        final String script = generateConfigScript();
        webView.post(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(script, null);
                } else {
                    webView.loadUrl("javascript:" + script);
                }
                Log.d(TAG, "Applied laboratory preferences to simulation WebView.");
            } catch (Exception e) {
                Log.w(TAG, "Error applying lab preferences to WebView: " + e.getMessage());
            }
        });
    }
}

