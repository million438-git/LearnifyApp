package com.million.learnify;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.util.Locale;

/**
 * MainActivity - Portal Selection with Normal Transition Styles.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved locale
        SettingsService settingsService = new SettingsService(this);
        applyLocale(settingsService.loadSettings().getLanguage());

        setContentView(R.layout.activity_main);
        
        // Entrance animation for the root layout (fade)
        View root = findViewById(android.R.id.content);
        root.setAlpha(0f);
        root.animate().alpha(1f).setDuration(400).start();
        
        initPortalSelection();
    }

    private void applyLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void initPortalSelection() {
        MaterialCardView cardStudent = findViewById(R.id.cardStudentPortal);
        MaterialCardView cardAdmin = findViewById(R.id.cardAdminPortal);

        // Staggered animation for cards
        if (cardStudent != null) {
            cardStudent.setVisibility(View.INVISIBLE);
            cardStudent.postDelayed(() -> {
                cardStudent.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
                cardStudent.startAnimation(anim);
            }, 100);
            
            cardStudent.setOnClickListener(v -> {
                navigateToStudentLogin();
            });
        }

        if (cardAdmin != null) {
            cardAdmin.setVisibility(View.INVISIBLE);
            cardAdmin.postDelayed(() -> {
                cardAdmin.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
                cardAdmin.startAnimation(anim);
            }, 200);

            cardAdmin.setOnClickListener(v -> {
                navigateToAdminLogin();
            });
        }
    }

    private void navigateToStudentLogin() {
        try {
            Intent intent = new Intent(MainActivity.this, StudentLoginActivity.class);
            startActivity(intent);
            // Using system default "normal" transition
        } catch (Exception e) {
            Toast.makeText(this, "Error opening Student Portal", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAdminLogin() {
        try {
            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(intent);
            // Using system default "normal" transition
        } catch (Exception e) {
            Toast.makeText(this, "Error opening Admin Portal", Toast.LENGTH_SHORT).show();
        }
    }
}
