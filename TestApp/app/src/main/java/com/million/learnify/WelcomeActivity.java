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
 * WelcomeActivity - Entry point with Motion Styles.
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

        // Initial states for animation
        if (logoCard != null) {
            logoCard.setAlpha(0f);
            logoCard.setScaleX(0.8f);
            logoCard.setScaleY(0.8f);
            
            // Step 1: Subtle fade-in and scale-up of logo
            logoCard.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(800)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
        }

        // Staggered fade-in for text elements
        animateFadeUp(titleText, 400);
        animateFadeUp(descText, 500);
        animateFadeUp(btnGetStarted, 600);
        animateFadeUp(footerText, 700);

        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                // Button tap feedback
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    
                    try {
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        // Cross-fade to portal selection
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error starting app", Toast.LENGTH_SHORT).show();
                    }
                }).start();
            });
        }
    }

    private void animateFadeUp(View view, long delay) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(30f);
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(delay)
            .start();
    }
}
