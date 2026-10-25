package com.million.learnify;

import androidx.appcompat.app.AppCompatDelegate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ThemeManager theme mode mapping.
 */
public class ThemeManagerTest {

    @Test
    public void testMapThemeToNightMode_light() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemeManager.mapThemeToNightMode("light"));
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemeManager.mapThemeToNightMode("Light"));
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemeManager.mapThemeToNightMode("LIGHT"));
    }

    @Test
    public void testMapThemeToNightMode_dark() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ThemeManager.mapThemeToNightMode("dark"));
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ThemeManager.mapThemeToNightMode("Dark"));
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ThemeManager.mapThemeToNightMode("DARK"));
    }

    @Test
    public void testMapThemeToNightMode_system() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeManager.mapThemeToNightMode("system"));
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeManager.mapThemeToNightMode("System"));
    }

    @Test
    public void testMapThemeToNightMode_nullOrUnknownFallback() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeManager.mapThemeToNightMode(null));
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeManager.mapThemeToNightMode(""));
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeManager.mapThemeToNightMode("auto"));
    }
}

