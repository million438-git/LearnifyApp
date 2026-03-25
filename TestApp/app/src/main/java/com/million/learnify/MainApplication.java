package com.million.learnify;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
}
