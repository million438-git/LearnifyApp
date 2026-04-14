package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;

/**
 * MainActivity - Fully Active Portal Selection Page.
 * This acts as the central hub for users to navigate between Student and Admin areas.
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);
            
            // 1. Initialize Portal Components
            setupPortals();
            
            // 2. Apply "Active" entrance animations
            runEntranceAnimations();
            
        } catch (Exception e) {
            Log.e(TAG, "Main Page Activation Failed", e);
            // Emergency Fallback to Student Login
            startActivity(new Intent(this, StudentLoginActivity.class));
            finish();
        }
    }

    private void setupPortals() {
        MaterialCardView cardStudent = findViewById(R.id.cardStudentPortal);
        MaterialCardView cardAdmin = findViewById(R.id.cardAdminPortal);

        // Make both portals fully active and visible
        if (cardStudent != null) {
            cardStudent.setVisibility(View.VISIBLE);
            cardStudent.setOnClickListener(v -> animateAndNavigate(v, StudentLoginActivity.class));
        }

        if (cardAdmin != null) {
            cardAdmin.setVisibility(View.VISIBLE);
            cardAdmin.setOnClickListener(v -> animateAndNavigate(v, AdminLoginActivity.class));
        }
    }

    private void animateAndNavigate(View v, Class<?> destination) {
        // Active feedback animation: Scale down then navigate
        v.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(150)
            .withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                try {
                    Intent intent = new Intent(MainActivity.this, destination);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation failed", e);
                }
            }).start();
    }

    private void runEntranceAnimations() {
        View header = findViewById(R.id.headerLayout);
        View cardStudent = findViewById(R.id.cardStudentPortal);
        View cardAdmin = findViewById(R.id.cardAdminPortal);
        View footer = findViewById(R.id.footerText);

        // Synchronized staggered entrance
        if (header != null) {
            header.setAlpha(0f);
            header.setTranslationY(-40f);
            header.animate().alpha(1f).translationY(0f).setDuration(700).start();
        }

        if (cardStudent != null) {
            cardStudent.setAlpha(0f);
            cardStudent.setTranslationY(60f);
            cardStudent.animate().alpha(1f).translationY(0f).setDuration(700).setStartDelay(200).start();
        }

        if (cardAdmin != null) {
            cardAdmin.setAlpha(0f);
            cardAdmin.setTranslationY(60f);
            cardAdmin.animate().alpha(1f).translationY(0f).setDuration(700).setStartDelay(400).start();
        }

        if (footer != null) {
            footer.setAlpha(0f);
            footer.animate().alpha(1f).setDuration(1000).setStartDelay(800).start();
        }
    }
}
