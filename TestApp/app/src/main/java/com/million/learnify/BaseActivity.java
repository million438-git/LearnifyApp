package com.million.learnify;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Locale;

/**
 * BaseActivity - Global handler for Appearance settings.
 * Ensures Theme, Font Size, and Reduce Motion are applied to every Activity.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        SettingsService ss = new SettingsService(newBase);
        StudentSettings settings = ss.loadSettings();
        
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        
        // 1. Apply Font Scale
        config.fontScale = getFontScale(settings.getFontSize());
        
        // 2. Apply Locale (Language)
        Locale locale = new Locale(settings.getLanguage());
        Locale.setDefault(locale);
        config.setLocale(locale);
        
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SettingsService ss = new SettingsService(this);
        StudentSettings settings = ss.loadSettings();
        
        // 3. Apply Theme
        applyTheme(settings.getThemeMode());
        
        super.onCreate(savedInstanceState);
        
        // 4. Apply Reduce Motion (Disable Window Transitions)
        if (settings.isReduceMotion()) {
            getWindow().setWindowAnimations(0);
        }
    }

    private void applyTheme(String theme) {
        if ("dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if ("light".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private float getFontScale(String size) {
        switch (size) {
            case "small": return 0.85f;
            case "large": return 1.25f;
            default: return 1.0f; // medium
        }
    }
}
