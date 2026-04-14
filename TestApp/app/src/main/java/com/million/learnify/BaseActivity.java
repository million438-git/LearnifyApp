package com.million.learnify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * BaseActivity - Professional Foundation for all Activities.
 * Features: Mandatory Email Verification Enforcement, Role-Based Access Control (RBAC), 
 * Theme Management, and Session Recovery.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected SecurePreferencesHelper securePrefs;
    protected StudentSettings currentSettings;
    protected DataRepository repository;

    @Override
    protected void attachBaseContext(Context newBase) {
        SettingsService settingsService = new SettingsService(newBase);
        String lang = settingsService.loadSettings().getLanguage();
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 1. Initialize Core Components with safety
        try {
            securePrefs = new SecurePreferencesHelper(this);
            repository = DataRepository.getInstance(this);
            
            // 2. Load and Apply Global Settings
            SettingsService settingsService = new SettingsService(this);
            currentSettings = settingsService.loadSettings();
            
            // Apply theme BEFORE super.onCreate to prevent flickering/crashes
            if (currentSettings != null) {
                applyTheme(currentSettings.getThemeMode());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize base components", e);
        }
        
        super.onCreate(savedInstanceState);
        
        // 3. Professional Session Healing
        if (securePrefs != null) {
            recoverSecureSession();
        }
    }

    /**
     * Standardizes the toolbar with a visible white back arrow [ ← Title ].
     */
    protected void setupPortalToolbar(Toolbar toolbar, String title) {
        if (toolbar == null) return;
        
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);
        
        // Safety check for drawable resource
        try {
            Drawable backArrow = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_revert);
            if (backArrow != null) {
                backArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                toolbar.setNavigationIcon(backArrow);
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation icon failed", e);
        }
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    protected void setupDrawerButton(final DrawerLayout drawerLayout) {
        View btnOpenDrawer = findViewById(R.id.btnOpenDrawer);
        if (btnOpenDrawer != null && drawerLayout != null) {
            btnOpenDrawer.setOnClickListener(v -> {
                try {
                    drawerLayout.openDrawer(GravityCompat.START);
                } catch (Exception e) {
                    Log.e(TAG, "Drawer open failed", e);
                }
            });
        }
    }

    /**
     * STRICT SECURITY: Enforces both Role and Email Verification.
     * Any activity calling this will automatically block unverified students.
     */
    protected void enforceRole(String requiredRole) {
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentRole = session.getString("user_type", "");
        String username = session.getString("username", "");
        
        // 1. Check Role Authorization
        if (!currentRole.equals(requiredRole)) {
            Log.w(TAG, "Access Denied: Required " + requiredRole + " but found " + currentRole);
            Toast.makeText(this, "⚠️ Access Denied: Unauthorized Portal.", Toast.LENGTH_LONG).show();
            kickToWelcome();
            return;
        }

        // 2. Check Mandatory Email Verification for Students
        if ("STUDENT".equals(currentRole)) {
            if (username.isEmpty() || !repository.isUserVerified(username)) {
                Log.w(TAG, "Security Protocol: Verification Wall triggered for " + username);
                
                // If username is empty, we can't verify, so log out
                if (username.isEmpty()) {
                    logoutInternal();
                    return;
                }

                // Redirect to Verification Activity
                Intent intent = new Intent(this, EmailVerificationActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("EMAIL", username);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    private void kickToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logoutInternal() {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
        if (securePrefs != null) securePrefs.clearSession();
        kickToWelcome();
    }

    private void applyTheme(String theme) {
        try {
            int mode;
            if ("dark".equals(theme)) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else if ("light".equals(theme)) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            
            if (AppCompatDelegate.getDefaultNightMode() != mode) {
                AppCompatDelegate.setDefaultNightMode(mode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme", e);
        }
    }

    private void recoverSecureSession() {
        try {
            SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
            boolean isLoggedIn = session.getBoolean("isLoggedIn", false);
            
            if (!isLoggedIn && securePrefs != null && securePrefs.getBoolean("isLoggedIn", false)) {
                String savedUser = securePrefs.getString("username", "");
                String savedName = securePrefs.getString("full_name", "");
                String userType = securePrefs.getString("user_type", "STUDENT");

                if (!savedUser.isEmpty()) {
                    session.edit()
                        .putString("username", savedUser)
                        .putString("full_name", savedName)
                        .putString("user_type", userType)
                        .putBoolean("isLoggedIn", true)
                        .apply();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Session recovery failed", e);
        }
    }
}
