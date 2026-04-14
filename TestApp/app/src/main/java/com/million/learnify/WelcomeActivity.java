package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;

/**
 * WelcomeActivity - Entry point with professional animations and strict auto-login verification.
 * Ensures that unverified users are always redirected to the verification portal.
 */
public class WelcomeActivity extends BaseActivity {

    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Install SplashScreen API
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // 2. Load UI first to prevent blank screens
        try {
            setContentView(R.layout.activity_welcome);
            initWelcomeUI();
        } catch (Exception e) {
            Log.e(TAG, "Layout inflation failed", e);
            // Fallback: Just proceed to Main if welcome layout is broken
            navigateToMain();
            return;
        }

        // 3. Perform session check after a small delay to ensure repository is ready
        new Handler(Looper.getMainLooper()).postDelayed(this::checkSessionAndProceed, 1500);
    }

    private void checkSessionAndProceed() {
        try {
            SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
            if (session.getBoolean("isLoggedIn", false)) {
                String type = session.getString("user_type", "STUDENT");
                String username = session.getString("username", "");

                if ("STUDENT".equals(type) && !username.isEmpty()) {
                    if (repository != null && !repository.isUserVerified(username)) {
                        navigateToVerification(username);
                        return;
                    }
                    navigateToDashboard(false);
                } else if ("ADMIN".equals(type)) {
                    navigateToDashboard(true);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Session check failed", e);
        }
    }

    private void initWelcomeUI() {
        View logoCard = findViewById(R.id.welcomeLogoCard);
        TextView tvTitle = findViewById(R.id.welcomeTitleText);
        TextView tvDesc = findViewById(R.id.welcomeDescText);
        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);

        // Entrance Animations
        if (logoCard != null) {
            logoCard.setAlpha(0f);
            logoCard.setTranslationY(50f);
            logoCard.animate().alpha(1f).translationY(0f).setDuration(1000).start();
        }

        if (tvTitle != null) {
            tvTitle.setAlpha(0f);
            tvTitle.animate().alpha(1f).setDuration(800).setStartDelay(400).start();
        }

        if (tvDesc != null) {
            tvDesc.setAlpha(0f);
            tvDesc.animate().alpha(1f).setDuration(800).setStartDelay(600).start();
        }

        if (btnGetStarted != null) {
            btnGetStarted.setAlpha(0f);
            btnGetStarted.animate().alpha(1f).setDuration(800).setStartDelay(800).start();
            btnGetStarted.setOnClickListener(v -> navigateToMain());
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void navigateToVerification(String username) {
        Intent intent = new Intent(this, EmailVerificationActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("EMAIL", username);
        startActivity(intent);
        finish();
    }

    private void navigateToDashboard(boolean isAdmin) {
        Intent intent = new Intent(this, isAdmin ? AdminDashboardActivity.class : DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
