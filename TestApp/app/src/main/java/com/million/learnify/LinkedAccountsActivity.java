package com.million.learnify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LinkedAccountsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LinkedAccountsPrefs";
    private static final String KEY_GOOGLE = "google_linked";
    private static final String KEY_APPLE = "apple_linked";
    private static final String KEY_MICROSOFT = "microsoft_linked";

    private boolean isGoogleConnected = true;
    private boolean isAppleConnected = false;
    private boolean isMicrosoftConnected = false;

    private TextView tvGoogleStatus, tvAppleStatus, tvMicrosoftStatus;
    private LinearLayout llGoogle, llApple, llMicrosoft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_accounts);

        loadPreferences();
        setupToolbar();
        initViews();
        updateUI();
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isGoogleConnected = prefs.getBoolean(KEY_GOOGLE, true);
        isAppleConnected = prefs.getBoolean(KEY_APPLE, false);
        isMicrosoftConnected = prefs.getBoolean(KEY_MICROSOFT, false);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Linked Accounts");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        tvGoogleStatus = findViewById(R.id.tvGoogleStatus);
        tvAppleStatus = findViewById(R.id.tvAppleStatus);
        tvMicrosoftStatus = findViewById(R.id.tvMicrosoftStatus);

        llGoogle = findViewById(R.id.llGoogle);
        llApple = findViewById(R.id.llApple);
        llMicrosoft = findViewById(R.id.llMicrosoft);

        llGoogle.setOnClickListener(v -> handleAccountClick("Google", isGoogleConnected));
        llApple.setOnClickListener(v -> handleAccountClick("Apple", isAppleConnected));
        llMicrosoft.setOnClickListener(v -> handleAccountClick("Microsoft", isMicrosoftConnected));

        View btnLinkNewAccount = findViewById(R.id.btnLinkNewAccount);
        if (btnLinkNewAccount != null) {
            btnLinkNewAccount.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                    .setTitle("Link New Provider")
                    .setMessage("Additional OAuth authentication providers (GitHub, Discord, and University SSO) are slated for Learnify Enterprise.")
                    .setPositiveButton("Understood", null)
                    .show());
        }
    }

    private void handleAccountClick(String provider, boolean isConnected) {
        if (isConnected) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Unlink " + provider)
                    .setMessage("Unlink this account? You will no longer be able to sign in with " + provider + ".")
                    .setPositiveButton("Unlink", (dialog, which) -> {
                        toggleConnection(provider, false);
                        Toast.makeText(this, provider + " unlinked successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Toast.makeText(this, "Connecting to " + provider + "...", Toast.LENGTH_SHORT).show();
            toggleConnection(provider, true);
        }
    }

    private void toggleConnection(String provider, boolean connected) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        switch (provider) {
            case "Google":
                isGoogleConnected = connected;
                editor.putBoolean(KEY_GOOGLE, connected);
                break;
            case "Apple":
                isAppleConnected = connected;
                editor.putBoolean(KEY_APPLE, connected);
                break;
            case "Microsoft":
                isMicrosoftConnected = connected;
                editor.putBoolean(KEY_MICROSOFT, connected);
                break;
        }
        editor.apply();
        updateUI();
    }

    private void updateUI() {
        updateStatusBadge(tvGoogleStatus, isGoogleConnected);
        updateStatusBadge(tvAppleStatus, isAppleConnected);
        updateStatusBadge(tvMicrosoftStatus, isMicrosoftConnected);
    }

    private void updateStatusBadge(TextView tv, boolean isConnected) {
        if (tv == null) return;
        if (isConnected) {
            tv.setText("Connected");
            tv.setTextColor(ContextCompat.getColor(this, R.color.emerald_primary));
        } else {
            tv.setText("Not Connected");
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_slate));
        }
    }
}
