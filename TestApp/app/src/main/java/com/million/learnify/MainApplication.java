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
        ThemeManager.applyTheme(this);
    }
}
