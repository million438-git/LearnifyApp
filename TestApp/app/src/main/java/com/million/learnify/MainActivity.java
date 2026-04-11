package com.million.learnify;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.util.Locale;

/**
 * MainActivity - Professional Portal.
 * Hides Admin Access behind a Developer Secret Trigger on the Welcome Title.
 */
public class MainActivity extends AppCompatActivity {

    private int adminTriggerCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsService settingsService = new SettingsService(this);
        applyLocale(settingsService.loadSettings().getLanguage());

        setContentView(R.layout.activity_main);
        
        initPortalSelection();
        setupSecretAdminTrigger();
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
        // We HIDE the Admin Card from the UI completely for production
        View cardAdmin = findViewById(R.id.cardAdminPortal);
        if (cardAdmin != null) cardAdmin.setVisibility(View.GONE);

        if (cardStudent != null) {
            cardStudent.setOnClickListener(v -> navigateToStudentLogin());
        }
    }

    /**
     * SECRET TRIGGER: Tap the "EduPortal" title 7 times 
     * to unlock the Admin Login. Only you (the developer) knows this.
     */
    private void setupSecretAdminTrigger() {
        TextView tvTitle = findViewById(R.id.welcomeTitle);
        if (tvTitle != null) {
            tvTitle.setOnClickListener(v -> {
                adminTriggerCount++;
                if (adminTriggerCount == 7) {
                    Toast.makeText(this, "Developer Mode: Admin Portal Unlocked", Toast.LENGTH_SHORT).show();
                    navigateToAdminLogin();
                    adminTriggerCount = 0;
                }
            });
        }
    }

    private void navigateToStudentLogin() {
        startActivity(new Intent(this, StudentLoginActivity.class));
    }

    private void navigateToAdminLogin() {
        startActivity(new Intent(this, AdminLoginActivity.class));
    }
}
