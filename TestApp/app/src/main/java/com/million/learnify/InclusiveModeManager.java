package com.million.learnify;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityManager;

import java.util.ArrayList;
import java.util.List;

/**
 * InclusiveModeManager - Centralized architectural controller for Learnify Inclusive Learning Mode.
 * Coordinates accessibility settings (text-to-speech, speech rate, high contrast, text scaling,
 * reduced motion, and audio feedback) and respects Android system accessibility services (TalkBack).
 */
public class InclusiveModeManager {

    private static final String PREFS_NAME = "LearnifyInclusivePreferences";

    public static final String KEY_INCLUSIVE_MODE = "inclusive_mode_enabled";
    public static final String KEY_TTS_ENABLED = "inclusive_tts_enabled";
    public static final String KEY_SPEECH_RATE = "inclusive_speech_rate";
    public static final String KEY_HIGH_CONTRAST = "inclusive_high_contrast";
    public static final String KEY_LARGE_TEXT = "inclusive_large_text";
    public static final String KEY_FONT_SCALE = "inclusive_font_scale";
    public static final String KEY_REDUCED_MOTION = "inclusive_reduced_motion";
    public static final String KEY_AUTO_READ = "inclusive_auto_read";
    public static final String KEY_AUDIO_HINTS = "inclusive_audio_hints";

    public static final float RATE_SLOW = 0.75f;
    public static final float RATE_NORMAL = 1.0f;
    public static final float RATE_FAST = 1.25f;
    public static final float RATE_VERY_FAST = 1.5f;

    public static final float FONT_SCALE_NORMAL = 1.0f;
    public static final float FONT_SCALE_LARGE = 1.25f;
    public static final float FONT_SCALE_EXTRA_LARGE = 1.45f;

    private static volatile InclusiveModeManager instance;
    private final SharedPreferences prefs;
    private final Context appContext;
    private final List<OnInclusiveModeChangeListener> listeners = new ArrayList<>();

    public interface OnInclusiveModeChangeListener {
        void onInclusiveModeChanged(boolean isEnabled);
    }

    private InclusiveModeManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static InclusiveModeManager getInstance(Context context) {
        if (instance == null) {
            synchronized (InclusiveModeManager.class) {
                if (instance == null) {
                    instance = new InclusiveModeManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Checks if Inclusive Learning Mode is active.
     * Evaluates explicit preference or auto-detects active system screen readers (TalkBack).
     */
    public boolean isInclusiveModeEnabled() {
        if (prefs.contains(KEY_INCLUSIVE_MODE)) {
            return prefs.getBoolean(KEY_INCLUSIVE_MODE, false);
        }
        // Auto-detect system screen reader or touch exploration if no explicit override set
        return isSystemScreenReaderActive();
    }

    public void setInclusiveModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_INCLUSIVE_MODE, enabled).apply();
        notifyListeners(enabled);
    }

    public boolean isTextToSpeechEnabled() {
        return prefs.getBoolean(KEY_TTS_ENABLED, true);
    }

    public void setTextToSpeechEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply();
    }

    public float getSpeechRate() {
        return prefs.getFloat(KEY_SPEECH_RATE, RATE_NORMAL);
    }

    public void setSpeechRate(float rate) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply();
    }

    public boolean isHighContrastEnabled() {
        return prefs.getBoolean(KEY_HIGH_CONTRAST, false);
    }

    public void setHighContrastEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply();
    }

    public boolean isLargeTextEnabled() {
        return prefs.getBoolean(KEY_LARGE_TEXT, false);
    }

    public void setLargeTextEnabled(boolean enabled) {
        prefs.edit()
             .putBoolean(KEY_LARGE_TEXT, enabled)
             .putFloat(KEY_FONT_SCALE, enabled ? FONT_SCALE_LARGE : FONT_SCALE_NORMAL)
             .apply();
    }

    public float getFontScale() {
        return prefs.getFloat(KEY_FONT_SCALE, FONT_SCALE_NORMAL);
    }

    public boolean isReducedMotionEnabled() {
        return prefs.getBoolean(KEY_REDUCED_MOTION, false);
    }

    public void setReducedMotionEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, enabled).apply();
    }

    public boolean isAutoReadContentEnabled() {
        return prefs.getBoolean(KEY_AUTO_READ, false);
    }

    public boolean isAutoReadEnabled() {
        return isAutoReadContentEnabled();
    }

    public void setAutoReadContentEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_READ, enabled).apply();
    }

    public boolean isAudioHintsEnabled() {
        return prefs.getBoolean(KEY_AUDIO_HINTS, true);
    }

    public void setAudioHintsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUDIO_HINTS, enabled).apply();
    }

    /**
     * Checks if an Android system accessibility service with spoken feedback or touch exploration is active.
     */
    public boolean isSystemScreenReaderActive() {
        AccessibilityManager am = (AccessibilityManager) appContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null || !am.isEnabled()) {
            return false;
        }
        if (am.isTouchExplorationEnabled()) {
            return true;
        }
        List<AccessibilityServiceInfo> services = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
        return services != null && !services.isEmpty();
    }

    public void addListener(OnInclusiveModeChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnInclusiveModeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(boolean enabled) {
        for (OnInclusiveModeChangeListener listener : new ArrayList<>(listeners)) {
            listener.onInclusiveModeChanged(enabled);
        }
    }
}
