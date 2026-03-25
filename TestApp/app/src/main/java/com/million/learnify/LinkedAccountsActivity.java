package com.million.learnify;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LinkedAccountsActivity extends AppCompatActivity {

    private boolean isGoogleConnected = true;
    private boolean isAppleConnected = false;
    private boolean isMicrosoftConnected = false;

    private TextView tvGoogleStatus, tvAppleStatus, tvMicrosoftStatus;
    private LinearLayout llGoogle, llApple, llMicrosoft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_accounts);

        setupToolbar();
        initViews();
        updateUI();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Linked Accounts");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
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
    }

    private void handleAccountClick(String provider, boolean isConnected) {
        if (isConnected) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Unlink " + provider)
                    .setMessage("Unlink this account? You will no longer be able to sign in with it.")
                    .setPositiveButton("Unlink", (dialog, which) -> {
                        toggleConnection(provider, false);
                        Toast.makeText(this, provider + " unlinked", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Toast.makeText(this, "Connecting to " + provider + "...", Toast.LENGTH_SHORT).show();
            toggleConnection(provider, true);
        }
    }

    private void toggleConnection(String provider, boolean connected) {
        switch (provider) {
            case "Google": isGoogleConnected = connected; break;
            case "Apple": isAppleConnected = connected; break;
            case "Microsoft": isMicrosoftConnected = connected; break;
        }
        updateUI();
    }

    private void updateUI() {
        tvGoogleStatus.setText(isGoogleConnected ? "Connected" : "Not Connected");
        tvAppleStatus.setText(isAppleConnected ? "Connected" : "Not Connected");
        tvMicrosoftStatus.setText(isMicrosoftConnected ? "Connected" : "Not Connected");
    }
}
