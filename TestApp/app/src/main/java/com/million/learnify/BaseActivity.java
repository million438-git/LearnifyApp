package com.million.learnify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * BaseActivity - Professional Foundation for all Activities.
 * Features: Mandatory Email Verification Enforcement, Role-Based Access Control (RBAC), 
 * Theme Management, Inclusive Learning & Accessibility integration, and Session Recovery.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected SecurePreferencesHelper securePrefs;
    protected StudentSettings currentSettings;
    protected DataRepository repository;
    protected InclusiveModeManager inclusiveManager;
    private InclusiveContentSpeaker activitySpeaker;

    @Override
    protected void attachBaseContext(Context newBase) {
        SettingsService settingsService = new SettingsService(newBase);
        String lang = settingsService.loadSettings().getLanguage();
        Context context = LocaleHelper.setLocale(newBase, lang);

        // Scalable font support for students with low vision
        try {
            InclusiveModeManager imm = InclusiveModeManager.getInstance(newBase);
            if (imm.isLargeTextEnabled()) {
                Configuration config = context.getResources().getConfiguration();
                config.fontScale = Math.max(config.fontScale, imm.getFontScale());
                context = context.createConfigurationContext(config);
            }
        } catch (Exception ignored) {}

        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 1. Initialize Core Components with safety
        try {
            securePrefs = new SecurePreferencesHelper(this);
            repository = DataRepository.getInstance(this);
            inclusiveManager = InclusiveModeManager.getInstance(this);
            
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

    public InclusiveModeManager getInclusiveModeManager() {
        if (inclusiveManager == null) {
            inclusiveManager = InclusiveModeManager.getInstance(this);
        }
        return inclusiveManager;
    }

    public synchronized InclusiveContentSpeaker getAccessibilitySpeaker() {
        if (activitySpeaker == null) {
            activitySpeaker = new InclusiveContentSpeaker(this);
        }
        return activitySpeaker;
    }

    public void speakAnnouncement(String text) {
        if (getInclusiveModeManager().isTextToSpeechEnabled() || getInclusiveModeManager().isInclusiveModeEnabled()) {
            getAccessibilitySpeaker().speakAnnouncement(text);
        }
    }

    @Override
    protected void onDestroy() {
        if (activitySpeaker != null) {
            activitySpeaker.shutdown();
            activitySpeaker = null;
        }
        super.onDestroy();
    }

    /**
     * Standardizes the toolbar with a visible white back arrow [ ← Title ] for child screens.
     */
    protected void setupPortalToolbar(Toolbar toolbar, String title) {
        if (toolbar == null) return;
        
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);
        
        // Safety check for drawable resource
        try {
            Drawable backArrow = ContextCompat.getDrawable(this, R.drawable.ic_back_white);
            if (backArrow == null) {
                backArrow = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_revert);
            }
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

    /**
     * Standardizes the root dashboard toolbar with a hamburger icon to toggle the navigation drawer.
     */
    protected void setupRootToolbar(Toolbar toolbar, String title, final DrawerLayout drawerLayout) {
        if (toolbar == null) return;

        toolbar.setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);

        try {
            Drawable menuIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu);
            if (menuIcon != null) {
                menuIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                toolbar.setNavigationIcon(menuIcon);
            }
        } catch (Exception e) {
            Log.e(TAG, "Root toolbar menu icon failed", e);
        }

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        if (drawerLayout != null) {
            toolbar.setNavigationOnClickListener(v -> {
                try {
                    drawerLayout.openDrawer(GravityCompat.START);
                } catch (Exception e) {
                    Log.e(TAG, "Drawer open from toolbar failed", e);
                }
            });
        }
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
     * Validates against encrypted storage and the local database to prevent session tampering.
     * Any activity calling this will automatically block unverified or unauthorized users.
     */
    protected void enforceRole(String requiredRole) {
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentRole = session.getString("user_type", "");
        String username = session.getString("username", "");
        FirebaseAuth auth = FirebaseAuthSupport.getConfiguredAuth(this);
        FirebaseUser firebaseUser = auth != null ? auth.getCurrentUser() : null;
        String storedFirebaseUid = securePrefs != null
                ? securePrefs.getString("firebase_uid", "")
                : "";
        if (firebaseUser == null
                || !firebaseUser.isEmailVerified()
                || !firebaseUser.getUid().equals(storedFirebaseUid)) {
            Toast.makeText(this, "A verified Firebase sign-in is required.", Toast.LENGTH_LONG).show();
            logoutInternal();
            return;
        }

        // 1. Check Role Authorization against Encrypted Preferences (if available)
        String secureRole = (securePrefs != null) ? securePrefs.getString("user_type", "") : "";
        if (!secureRole.isEmpty() && !secureRole.equalsIgnoreCase(requiredRole)) {
            Log.w(TAG, "Access Denied: Encrypted session role mismatch. Required " + requiredRole + " but found " + secureRole);
            Toast.makeText(this, "Access Denied: Unauthorized Portal.", Toast.LENGTH_LONG).show();
            logoutInternal();
            return;
        }

        // 2. Check Role Authorization against UserSession
        if (!currentRole.equalsIgnoreCase(requiredRole) || username.isEmpty()) {
            Log.w(TAG, "Access Denied: Required " + requiredRole + " but found " + currentRole);
            Toast.makeText(this, "Access Denied: Unauthorized Portal.", Toast.LENGTH_LONG).show();
            logoutInternal();
            return;
        }

        // 3. Authoritative verification: cross-check against database record
        if (repository != null) {
            Cursor cursor = repository.getUserDetails(username);
            boolean dbRoleValid = false;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int colIndex = cursor.getColumnIndex("user_type");
                    if (colIndex != -1) {
                        String dbRole = cursor.getString(colIndex);
                        if (requiredRole.equalsIgnoreCase(dbRole)) {
                            dbRoleValid = true;
                        }
                    }
                }
                cursor.close();
            }
            if (!dbRoleValid) {
                Log.w(TAG, "Access Denied: Database record does not match required role for " + username);
                Toast.makeText(this, "Access Denied: Invalid user authorization.", Toast.LENGTH_LONG).show();
                logoutInternal();
                return;
            }
        }

        // 4. Check Mandatory Email Verification for Students
        if ("STUDENT".equalsIgnoreCase(currentRole)) {
            if (!repository.isUserVerified(username)) {
                Log.w(TAG, "Security Protocol: Verification Wall triggered for " + username);

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
        FirebaseSessionManager.signOut(this, FirebaseAuthSupport.getConfiguredAuth(this));
        kickToWelcome();
    }

    private void applyTheme(String theme) {
        try {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
                ThemeManager.applyTheme(this);
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
