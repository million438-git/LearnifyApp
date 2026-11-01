package com.million.learnify;

import android.content.SharedPreferences;

/**
 * LocalDevicePreferences - Category A Preferences.
 * Strictly local to the physical Android device. Never synchronized across devices.
 * Controls device rendering, haptic feedback, device-specific audio, accessibility, and storage.
 */
public class LocalDevicePreferences {

    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_SOUND_EFFECTS = "sound_effects";
    private static final String KEY_HAPTIC = "haptic_feedback";
    private static final String KEY_ANIMATIONS = "animated_celebrations";
    private static final String KEY_TTS = "text_to_speech";
    private static final String KEY_HIGH_CONTRAST = "high_contrast";
    private static final String KEY_REDUCE_MOTION = "reduce_motion";
    private static final String KEY_INCLUSIVE_MODE = "inclusive_mode";
    private static final String KEY_SPEECH_RATE = "speech_rate";
    private static final String KEY_SPEECH_PITCH = "speech_pitch";
    private static final String KEY_AUTO_READ = "auto_read_content";
    private static final String KEY_OFFLINE = "offline_mode";
    private static final String KEY_DATA_SAVER = "data_saver";
    private static final String KEY_VIDEO_QUALITY = "video_quality";
    private static final String KEY_AUTO_DOWNLOAD = "auto_download";

    private String themeMode = "light";
    private String fontSize = "medium";
    private boolean soundEffects = true;
    private boolean hapticFeedback = true;
    private boolean animatedCelebrations = true;
    private boolean textToSpeech = true;
    private boolean highContrastMode = false;
    private boolean reduceMotion = false;
    private boolean inclusiveMode = false;
    private float speechRate = 1.0f;
    private float speechPitch = 1.0f;
    private boolean autoReadContent = false;
    private boolean offlineMode = false;
    private boolean dataSaver = false;
    private String videoQuality = "720p";
    private String autoDownloadMedia = "Wi-Fi only";

    public LocalDevicePreferences() {
    }

    public static LocalDevicePreferences fromPreferences(SharedPreferences prefs) {
        LocalDevicePreferences p = new LocalDevicePreferences();
        if (prefs == null) return p;

        p.setThemeMode(prefs.getString(KEY_THEME, "light"));
        p.setFontSize(prefs.getString(KEY_FONT_SIZE, "medium"));
        p.setSoundEffects(prefs.getBoolean(KEY_SOUND_EFFECTS, true));
        p.setHapticFeedback(prefs.getBoolean(KEY_HAPTIC, true));
        p.setAnimatedCelebrations(prefs.getBoolean(KEY_ANIMATIONS, true));
        p.setTextToSpeech(prefs.getBoolean(KEY_TTS, true));
        p.setHighContrastMode(prefs.getBoolean(KEY_HIGH_CONTRAST, false));
        p.setReduceMotion(prefs.getBoolean(KEY_REDUCE_MOTION, false));
        p.setInclusiveMode(prefs.getBoolean(KEY_INCLUSIVE_MODE, false));
        p.setSpeechRate(prefs.getFloat(KEY_SPEECH_RATE, 1.0f));
        p.setSpeechPitch(prefs.getFloat(KEY_SPEECH_PITCH, 1.0f));
        p.setAutoReadContent(prefs.getBoolean(KEY_AUTO_READ, false));
        p.setOfflineMode(prefs.getBoolean(KEY_OFFLINE, false));
        p.setDataSaver(prefs.getBoolean(KEY_DATA_SAVER, false));
        p.setVideoQuality(prefs.getString(KEY_VIDEO_QUALITY, "720p"));
        p.setAutoDownloadMedia(prefs.getString(KEY_AUTO_DOWNLOAD, "Wi-Fi only"));
        return p;
    }

    public void saveToPreferences(SharedPreferences prefs) {
        if (prefs == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_THEME, getThemeMode());
        editor.putString(KEY_FONT_SIZE, getFontSize());
        editor.putBoolean(KEY_SOUND_EFFECTS, isSoundEffects());
        editor.putBoolean(KEY_HAPTIC, isHapticFeedback());
        editor.putBoolean(KEY_ANIMATIONS, isAnimatedCelebrations());
        editor.putBoolean(KEY_TTS, isTextToSpeech());
        editor.putBoolean(KEY_HIGH_CONTRAST, isHighContrastMode());
        editor.putBoolean(KEY_REDUCE_MOTION, isReduceMotion());
        editor.putBoolean(KEY_INCLUSIVE_MODE, isInclusiveMode());
        editor.putFloat(KEY_SPEECH_RATE, getSpeechRate());
        editor.putFloat(KEY_SPEECH_PITCH, getSpeechPitch());
        editor.putBoolean(KEY_AUTO_READ, isAutoReadContent());
        editor.putBoolean(KEY_OFFLINE, isOfflineMode());
        editor.putBoolean(KEY_DATA_SAVER, isDataSaver());
        editor.putString(KEY_VIDEO_QUALITY, getVideoQuality());
        editor.putString(KEY_AUTO_DOWNLOAD, getAutoDownloadMedia());
        editor.apply();
    }

    // Getters & Validated Setters
    public String getThemeMode() {
        return themeMode != null ? themeMode : "light";
    }

    public void setThemeMode(String themeMode) {
        if ("dark".equalsIgnoreCase(themeMode) || "system".equalsIgnoreCase(themeMode)) {
            this.themeMode = themeMode.toLowerCase();
        } else {
            this.themeMode = "light";
        }
    }

    public String getFontSize() {
        return fontSize != null ? fontSize : "medium";
    }

    public void setFontSize(String fontSize) {
        if ("small".equalsIgnoreCase(fontSize) || "large".equalsIgnoreCase(fontSize)) {
            this.fontSize = fontSize.toLowerCase();
        } else {
            this.fontSize = "medium";
        }
    }

    public boolean isSoundEffects() { return soundEffects; }
    public void setSoundEffects(boolean soundEffects) { this.soundEffects = soundEffects; }

    public boolean isHapticFeedback() { return hapticFeedback; }
    public void setHapticFeedback(boolean hapticFeedback) { this.hapticFeedback = hapticFeedback; }

    public boolean isAnimatedCelebrations() { return animatedCelebrations; }
    public void setAnimatedCelebrations(boolean animatedCelebrations) { this.animatedCelebrations = animatedCelebrations; }

    public boolean isTextToSpeech() { return textToSpeech; }
    public void setTextToSpeech(boolean textToSpeech) { this.textToSpeech = textToSpeech; }

    public boolean isHighContrastMode() { return highContrastMode; }
    public void setHighContrastMode(boolean highContrastMode) { this.highContrastMode = highContrastMode; }

    public boolean isReduceMotion() { return reduceMotion; }
    public void setReduceMotion(boolean reduceMotion) { this.reduceMotion = reduceMotion; }

    public boolean isInclusiveMode() { return inclusiveMode; }
    public void setInclusiveMode(boolean inclusiveMode) { this.inclusiveMode = inclusiveMode; }

    public float getSpeechRate() { return speechRate; }
    public void setSpeechRate(float speechRate) {
        this.speechRate = Math.max(0.5f, Math.min(2.0f, speechRate));
    }

    public float getSpeechPitch() { return speechPitch; }
    public void setSpeechPitch(float speechPitch) {
        this.speechPitch = Math.max(0.5f, Math.min(2.0f, speechPitch));
    }

    public boolean isAutoReadContent() { return autoReadContent; }
    public void setAutoReadContent(boolean autoReadContent) { this.autoReadContent = autoReadContent; }

    public boolean isOfflineMode() { return offlineMode; }
    public void setOfflineMode(boolean offlineMode) { this.offlineMode = offlineMode; }

    public boolean isDataSaver() { return dataSaver; }
    public void setDataSaver(boolean dataSaver) { this.dataSaver = dataSaver; }

    public String getVideoQuality() {
        return videoQuality != null ? videoQuality : "720p";
    }

    public void setVideoQuality(String videoQuality) {
        if ("480p".equalsIgnoreCase(videoQuality) || "1080p".equalsIgnoreCase(videoQuality)) {
            this.videoQuality = videoQuality.toLowerCase();
        } else {
            this.videoQuality = "720p";
        }
    }

    public String getAutoDownloadMedia() {
        return autoDownloadMedia != null ? autoDownloadMedia : "Wi-Fi only";
    }

    public void setAutoDownloadMedia(String autoDownloadMedia) {
        this.autoDownloadMedia = autoDownloadMedia != null ? autoDownloadMedia : "Wi-Fi only";
    }
}

