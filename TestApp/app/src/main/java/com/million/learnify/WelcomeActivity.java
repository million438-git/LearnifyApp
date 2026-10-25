package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;

/**
 * WelcomeActivity - Entry point for the Learnify educational platform.
 * Features the approved Futuristic Luxury visual identity, responsive scrollable
 * presentation, smooth entrance choreography, and strict session/verification preservation.
 */
public class WelcomeActivity extends BaseActivity {

    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Install AndroidX SplashScreen API
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // 2. Load Welcome UI
        try {
            setContentView(R.layout.activity_welcome);
            initWelcomeUI();
        } catch (Exception e) {
            Log.e(TAG, "Layout inflation failed", e);
            // Fallback: Proceed to portal hub if inflation fails
            navigateToMain();
            return;
        }

        // 3. Strictly preserve auto-login session routing
        new Handler(Looper.getMainLooper()).postDelayed(this::checkSessionAndProceed, 1200);
    }

    /**
     * Preserves existing session verification and routing:
     * - Authenticated & verified student -> Student Dashboard
     * - Authenticated unverified student -> Email Verification Portal
     * - Authenticated administrator -> Admin Dashboard
     * - Unauthenticated user -> Remains on Welcome page
     */
    private void checkSessionAndProceed() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
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

    /**
     * Binds all navigation actions and executes tasteful, non-blocking entrance choreography.
     */
    private void initWelcomeUI() {
        // Section E & H: Get Started buttons (Direct to RegisterActivity)
        View btnGetStarted = findViewById(R.id.btnGetStarted);
        View btnGetStartedBottom = findViewById(R.id.btnGetStartedBottom);

        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> animateButtonAndExecute(v, this::navigateToRegister));
        }
        if (btnGetStartedBottom != null) {
            btnGetStartedBottom.setOnClickListener(v -> animateButtonAndExecute(v, this::navigateToRegister));
        }

        // Section E & H: Sign In buttons (Direct to StudentLoginActivity)
        View btnSignIn = findViewById(R.id.btnSignIn);
        View btnSignInBottom = findViewById(R.id.btnSignInBottom);

        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> animateButtonAndExecute(v, this::navigateToLogin));
        }
        if (btnSignInBottom != null) {
            btnSignInBottom.setOnClickListener(v -> animateButtonAndExecute(v, this::navigateToLogin));
        }

        // Portal Selector Hub links (Preserves access to MainActivity for Student vs Admin chooser)
        View btnPortalHubTop = findViewById(R.id.btnPortalHubTop);
        View tvPortalHubBottom = findViewById(R.id.tvPortalHubBottom);

        if (btnPortalHubTop != null) {
            btnPortalHubTop.setOnClickListener(v -> navigateToMain());
        }
        if (tvPortalHubBottom != null) {
            tvPortalHubBottom.setOnClickListener(v -> navigateToMain());
        }

        // Restrained, professional entrance choreography
        playEntranceAnimations();
    }

    /**
     * Touch feedback animation providing tactile response before navigation.
     */
    private void animateButtonAndExecute(View v, Runnable action) {
        v.animate()
            .scaleX(0.96f)
            .scaleY(0.96f)
            .setDuration(100)
            .withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                action.run();
            })
            .start();
    }

    /**
     * Subtle, non-blocking entrance animation for first-screen hero elements.
     */
    private void playEntranceAnimations() {
        View header = findViewById(R.id.headerContainer);
        View heroIntro = findViewById(R.id.heroIntroContainer);
        View heroArt = findViewById(R.id.heroArtContainer);
        View primaryActions = findViewById(R.id.primaryActionsContainer);

        if (header != null) {
            header.setAlpha(0f);
            header.setTranslationY(-20f);
            header.animate().alpha(1f).translationY(0f).setDuration(600).start();
        }

        if (heroIntro != null) {
            heroIntro.setAlpha(0f);
            heroIntro.setTranslationY(30f);
            heroIntro.animate().alpha(1f).translationY(0f).setDuration(700).setStartDelay(150).start();
        }

        if (heroArt != null) {
            heroArt.setAlpha(0f);
            heroArt.setScaleX(0.92f);
            heroArt.setScaleY(0.92f);
            heroArt.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800).setStartDelay(250).start();
        }

        if (primaryActions != null) {
            primaryActions.setAlpha(0f);
            primaryActions.setTranslationY(20f);
            primaryActions.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(400).start();
        }
    }

    /**
     * Launches the student registration / onboarding flow.
     */
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Launches the student login flow.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, StudentLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Launches the MainActivity portal hub (Student vs Admin portal selection).
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Redirects unverified student to email verification portal.
     */
    private void navigateToVerification(String username) {
        Intent intent = new Intent(this, EmailVerificationActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("EMAIL", username);
        startActivity(intent);
        finish();
    }

    /**
     * Redirects authenticated user to their respective dashboard.
     */
    private void navigateToDashboard(boolean isAdmin) {
        Intent intent = new Intent(this, isAdmin ? AdminDashboardActivity.class : DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
