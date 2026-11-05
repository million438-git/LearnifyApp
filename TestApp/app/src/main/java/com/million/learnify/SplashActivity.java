package com.million.learnify;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * SplashActivity - Premium Academic Luminary Launch Screen.
 * Provides a clean, modern, lightweight entrance showcasing the Learnify brand emblem,
 * typography, and tagline, before smoothly transitioning to WelcomeActivity.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 1100L;

    private Handler handler;
    private Runnable transitionRunnable;
    private boolean hasTransitioned = false;

    private ImageView ivLogo;
    private TextView tvTitle;
    private TextView tvTagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install AndroidX SplashScreen API before super.onCreate
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Edge-to-edge transparent system bars with light icons on dark canvas
        configureSystemBars();

        setContentView(R.layout.activity_splash);

        initViews();
        playEntranceAnimation();

        // Schedule smooth transition to WelcomeActivity
        handler = new Handler(Looper.getMainLooper());
        transitionRunnable = this::proceedToWelcome;
        handler.postDelayed(transitionRunnable, SPLASH_DURATION_MS);

        // Tap-to-skip listener for instant navigation
        View rootView = findViewById(R.id.rootSplash);
        if (rootView != null) {
            rootView.setOnClickListener(v -> proceedToWelcome());
        }
    }

    private void configureSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false); // White status-bar icons on dark canvas
            controller.setAppearanceLightNavigationBars(false); // White navigation buttons
        }
    }

    private void initViews() {
        ivLogo = findViewById(R.id.ivSplashLogo);
        tvTitle = findViewById(R.id.tvSplashTitle);
        tvTagline = findViewById(R.id.tvSplashTagline);
    }

    private void playEntranceAnimation() {
        if (ivLogo == null || tvTitle == null || tvTagline == null) {
            return;
        }

        // Initial states
        ivLogo.setAlpha(0f);
        ivLogo.setScaleX(0.92f);
        ivLogo.setScaleY(0.92f);

        tvTitle.setAlpha(0f);
        tvTitle.setTranslationY(dpToPx(12));

        tvTagline.setAlpha(0f);

        // Logo entrance: smooth fade and gentle scale up
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(ivLogo, View.ALPHA, 0f, 1f);
        logoAlpha.setDuration(450);

        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(ivLogo, View.SCALE_X, 0.92f, 1.0f);
        logoScaleX.setDuration(500);

        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(ivLogo, View.SCALE_Y, 0.92f, 1.0f);
        logoScaleY.setDuration(500);

        // Title entrance: gentle rise and fade
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(tvTitle, View.ALPHA, 0f, 1f);
        titleAlpha.setDuration(400);
        titleAlpha.setStartDelay(150);

        ObjectAnimator titleTransY = ObjectAnimator.ofFloat(tvTitle, View.TRANSLATION_Y, dpToPx(12), 0f);
        titleTransY.setDuration(400);
        titleTransY.setStartDelay(150);

        // Tagline entrance: soft fade
        ObjectAnimator tagAlpha = ObjectAnimator.ofFloat(tvTagline, View.ALPHA, 0f, 1f);
        tagAlpha.setDuration(350);
        tagAlpha.setStartDelay(300);

        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new DecelerateInterpolator());
        set.playTogether(logoAlpha, logoScaleX, logoScaleY, titleAlpha, titleTransY, tagAlpha);
        set.start();
    }

    private synchronized void proceedToWelcome() {
        if (hasTransitioned || isFinishing() || isDestroyed()) {
            return;
        }
        hasTransitioned = true;

        if (handler != null && transitionRunnable != null) {
            handler.removeCallbacks(transitionRunnable);
        }

        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && transitionRunnable != null) {
            handler.removeCallbacks(transitionRunnable);
        }
    }
}

