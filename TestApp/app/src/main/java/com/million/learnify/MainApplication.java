package com.million.learnify;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import java.util.concurrent.Executors;

public class MainApplication extends Application {

    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Initialize Global Theme first to prevent theme flickering on startup
        ThemeManager.applyTheme(this);

        // 2. Asynchronously warm up Chaquopy Python in background worker
        // This eliminates cold-start main-thread freezing on physical devices
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ensurePythonStarted(getApplicationContext());
                Log.i(TAG, "Chaquopy Python engine warmed up in background.");
            } catch (Exception e) {
                Log.w(TAG, "Background Python warm-up notice: " + e.getMessage());
            }
        });
    }

    /**
     * Thread-safe Python initialization helper for activities requiring Python on-demand.
     */
    public static synchronized void ensurePythonStarted(Context context) {
        if (!Python.isStarted() && context != null) {
            Python.start(new AndroidPlatform(context.getApplicationContext()));
        }
    }
}
