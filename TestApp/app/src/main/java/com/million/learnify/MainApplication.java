package com.million.learnify;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 1. Initialize Python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // 2. Initialize Global Theme to prevent crashes on startup
        applySavedTheme();
    }

    private void applySavedTheme() {
        SettingsService settingsService = new SettingsService(this);
        String theme = settingsService.loadSettings().getThemeMode();
        
        int mode;
        if ("dark".equals(theme)) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if ("light".equals(theme)) {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
