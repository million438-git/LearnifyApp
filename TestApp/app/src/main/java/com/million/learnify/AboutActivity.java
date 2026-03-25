package com.million.learnify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
            getSupportActionBar().setTitle("About Learnify");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initAboutFeatures() {
        TextView tvVersion = findViewById(R.id.tvAppVersion);
        LinearLayout llTerms = findViewById(R.id.llTerms);
        LinearLayout llPrivacy = findViewById(R.id.llPrivacy);
        LinearLayout llRateUs = findViewById(R.id.llRateUs);

        // 1. DYNAMIC VERSION
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersion.setText(versionName);
        } catch (Exception e) {
            tvVersion.setText(BuildConfig.VERSION_NAME);
        }

        // 2. TERMS OF SERVICE
        if (llTerms != null) {
            llTerms.setOnClickListener(v -> openInAppBrowser("Terms of Service", "https://learnify.com/terms"));
        }

        // 3. PRIVACY POLICY
        if (llPrivacy != null) {
            llPrivacy.setOnClickListener(v -> openInAppBrowser("Privacy Policy", "https://learnify.com/privacy"));
        }

        // 4. RATE US (Full Function)
        if (llRateUs != null) {
            llRateUs.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Rate Learnify")
                        .setMessage("If you love using Learnify, would you mind taking a moment to rate it? It won't take more than a minute. Thanks for your support!")
                        .setPositiveButton("Rate Now", (dialog, which) -> {
                            openStoreListing();
                            Toast.makeText(this, "Thank you for your rating!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No Thanks", null)
                        .setNeutralButton("Remind Me Later", null)
                        .show();
            });
        }
    }

    private void openInAppBrowser(String title, String url) {
        Toast.makeText(this, "Opening " + title + "...", Toast.LENGTH_SHORT).show();
        // For real deployment, we use Intent.ACTION_VIEW which opens the system's preferred browser
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No browser found to open link", Toast.LENGTH_SHORT).show();
        }
    }

    private void openStoreListing() {
        String packageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }
}
