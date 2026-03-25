package com.million.learnify;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;
import androidx.core.splashscreen.SplashScreen;
import com.google.android.material.button.MaterialButton;

/**
 * WelcomeActivity - Entry point with normal system transitions.
 */
public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        View logoCard = findViewById(R.id.welcomeLogoCard);
        View titleText = findViewById(R.id.welcomeTitleText);
        View descText = findViewById(R.id.welcomeDescText);
        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);
        View footerText = findViewById(R.id.welcomeFooterText);

        // Entrance animation for content (fade-in only for a polished start)
        if (logoCard != null) {
            logoCard.setAlpha(0f);
            logoCard.animate().alpha(1f).setDuration(800).start();
        }

        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    // System default transition
                } catch (Exception e) {
                    Toast.makeText(this, "Error starting app", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
