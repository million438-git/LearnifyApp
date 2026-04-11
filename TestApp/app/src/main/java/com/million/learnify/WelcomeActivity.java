package com.million.learnify;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.splashscreen.SplashScreen;
import com.google.android.material.button.MaterialButton;

/**
 * WelcomeActivity - Entry point with auto-login redirection.
 */
public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // 1. Professional Auto-Login Redirect
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        if (session.getBoolean("isLoggedIn", false)) {
            String type = session.getString("user_type", "STUDENT");
            if ("ADMIN".equals(type)) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, DashboardActivity.class));
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        View logoCard = findViewById(R.id.welcomeLogoCard);
        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);

        if (logoCard != null) {
            logoCard.setAlpha(0f);
            logoCard.animate().alpha(1f).setDuration(800).start();
        }

        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error starting app", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
