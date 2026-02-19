package com.example.testapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setupToolbar();
        initAboutFeatures();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.aboutToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initAboutFeatures() {
        TextView tvAppVersion = findViewById(R.id.tvAppVersion);
        TextView btnCheckUpdates = findViewById(R.id.btnCheckUpdates);
        TextView btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        TextView btnTermsConditions = findViewById(R.id.btnTermsConditions);

        // Display Version (can be dynamically fetched from PackageInfo if needed)
        tvAppVersion.setText("1.0.0 (Stable)");

        // Check for Updates logic
        btnCheckUpdates.setOnClickListener(v -> {
            Toast.makeText(this, "Checking for updates...", Toast.LENGTH_SHORT).show();
            // Simulate a check
            v.postDelayed(() -> 
                Toast.makeText(this, "You are using the latest version!", Toast.LENGTH_SHORT).show(), 
                1500);
        });

        // Privacy Policy link
        btnPrivacyPolicy.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Privacy Policy...", Toast.LENGTH_SHORT).show();
            // In a real app, open a URL:
            // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://acegrade.com/privacy"));
            // startActivity(browserIntent);
        });

        // Terms & Conditions link
        btnTermsConditions.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Terms & Conditions...", Toast.LENGTH_SHORT).show();
            // In a real app, open a URL:
            // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://acegrade.com/terms"));
            // startActivity(browserIntent);
        });
    }
}
