package com.million.learnify;

import android.content.Context;
import android.util.Log;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * ThemeManager - Centralized Theme Controller for Learnify.
 * Manages Light, Dark, and System Default theme modes cleanly and reliably.
 */
public final class ThemeManager {
    private static final String TAG = "ThemeManager";

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    private ThemeManager() {
        // Utility class
    }

    /**
     * Initializes and applies the user's saved theme mode globally.
     * Safe to invoke from Application.onCreate() before any Activity is instantiated.
     */
    public static void applyTheme(Context context) {
        if (context == null) return;
        try {
            SettingsService settingsService = new SettingsService(context.getApplicationContext());
            StudentSettings settings = settingsService.loadSettings();
            String themeMode = (settings != null && settings.getThemeMode() != null) 
                    ? settings.getThemeMode() 
                    : THEME_SYSTEM;

            int targetMode = mapThemeToNightMode(themeMode);
            if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
                AppCompatDelegate.setDefaultNightMode(targetMode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply saved theme", e);
        }
    }

    /**
     * Persists the selected theme mode to SettingsService and applies it globally.
     */
    public static void setThemeMode(Context context, String themeMode) {
        if (context == null) return;
        try {
            SettingsService settingsService = new SettingsService(context.getApplicationContext());
            StudentSettings settings = settingsService.loadSettings();
            if (settings != null) {
                settings.setThemeMode(themeMode);
                settingsService.saveSettings(settings);
            }

            int targetMode = mapThemeToNightMode(themeMode);
            if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
                AppCompatDelegate.setDefaultNightMode(targetMode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set theme mode: " + themeMode, e);
        }
    }

    /**
     * Returns the currently saved theme mode string ("light", "dark", or "system").
     */
    public static String getThemeMode(Context context) {
        if (context == null) return THEME_SYSTEM;
        try {
            SettingsService settingsService = new SettingsService(context.getApplicationContext());
            StudentSettings settings = settingsService.loadSettings();
            if (settings != null && settings.getThemeMode() != null) {
                return settings.getThemeMode();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get theme mode", e);
        }
        return THEME_SYSTEM;
    }

    /**
     * Maps theme mode string to AppCompatDelegate night mode constants.
     */
    public static int mapThemeToNightMode(String themeMode) {
        if (THEME_DARK.equalsIgnoreCase(themeMode)) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        } else if (THEME_LIGHT.equalsIgnoreCase(themeMode)) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}

