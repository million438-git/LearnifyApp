package com.million.learnify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HelpSupportActivity extends AppCompatActivity {

    private static final String DEVELOPER_EMAIL = "millionteshome439@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        setupToolbar();
        initSupportFeatures();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.helpToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Help & Support");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initSupportFeatures() {
        // FAQ Clicks
        findViewById(R.id.btnFaqAccount).setOnClickListener(v -> showHelpDialog("Account & Login", 
            "• How to create account: Tap Register on login screen.\n" +
            "• Forgot password: Use 'Forgot Password' link to reset via email.\n" +
            "• Change Profile: Go to Settings -> Edit Profile.\n" +
            "• Without account: Limited content is available offline."));

        findViewById(R.id.btnFaqContent).setOnClickListener(v -> showHelpDialog("Learning & Content", 
            "• Access: Subjects are in Academic Library.\n" +
            "• Locked lessons: Premium subjects require activation.\n" +
            "• Scores: Calculated based on quiz accuracy.\n" +
            "• Average Grade: Your total performance across all subjects."));

        findViewById(R.id.btnFaqUsage).setOnClickListener(v -> showHelpDialog("App Usage", 
            "• Dark Mode: Enable in Appearance Settings.\n" +
            "• Offline Use: Use the 'Pack' button in the library.\n" +
            "• Slow speed: Clear cache or ensure enough RAM is free."));

        findViewById(R.id.btnFaqPayment).setOnClickListener(v -> showHelpDialog("Payments", 
            "• Subscriptions: Use the Unlock Premium button.\n" +
            "• Methods: We support Telebirr and CBE.\n" +
            "• Security: Payments are verified by admins manually for maximum safety."));

        // Support & Feedback
        findViewById(R.id.btnContactSupport).setOnClickListener(v -> showContactOptions());
        findViewById(R.id.btnReportProblem).setOnClickListener(v -> showReportProblemDialog());
        findViewById(R.id.btnFeedback).setOnClickListener(v -> showFeedbackDialog());

        // Guides
        findViewById(R.id.btnHowToUse).setOnClickListener(v -> showHelpDialog("User Guide", 
            "1. Register and select your grade.\n" +
            "2. Explore subjects in the Library.\n" +
            "3. Use Voice Study for hands-free learning.\n" +
            "4. Challenge yourself daily to keep your streak!"));

        findViewById(R.id.btnTroubleshooting).setOnClickListener(v -> showHelpDialog("Troubleshooting", 
            "• App not opening: Restart your phone.\n" +
            "• Login issues: Check your internet or reset password.\n" +
            "• Lessons missing: Sync your Knowledge Packs in Library."));

        // Safety & Accessibility
        findViewById(R.id.btnPrivacySafety).setOnClickListener(v -> showHelpDialog("Privacy & Safety", 
            "• Data Protection: Your study history is private.\n" +
            "• Password Safety: Never share your login details.\n" +
            "• Environment: Safe, ad-free learning for all ages."));

        findViewById(R.id.btnAccessibility).setOnClickListener(v -> showHelpDialog("Accessibility", 
            "• Languages: Change in Settings.\n" +
            "• Font Size: Choose Small, Medium, or Large.\n" +
            "• Voice: Humanized audio reading available for all lessons."));

        findViewById(R.id.btnParentTeacherHelp).setOnClickListener(v -> showHelpDialog("Parents & Teachers", 
            "• Parents: Monitor progress via the Performance Dashboard.\n" +
            "• Teachers: Upload materials through the Admin Portal.\n" +
            "• Support: Contact us for group school licenses."));
    }

    private void showHelpDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show();
    }

    private void showContactOptions() {
        String options[] = {"Email Developer (" + DEVELOPER_EMAIL + ")", "In-App Chat (Mon-Fri)", "Emergency Phone Support"};
        new AlertDialog.Builder(this)
                .setTitle("Contact Support")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openEmailApp();
                    else if (which == 1) startActivity(new Intent(this, AIChatActivity.class));
                    else Toast.makeText(this, "Calling 0902399206...", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void openEmailApp() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{DEVELOPER_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, "AceGrade Support Request");
        try {
            startActivity(Intent.createChooser(intent, "Send Email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email apps installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showReportProblemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Report a Problem");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("Problem Title (e.g. Lesson Crash)");
        layout.addView(etTitle);

        final Spinner spinner = new Spinner(this);
        String[] categories = {"App Crash", "Login Issue", "Content Error", "Audio Not Working", "Other"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        layout.addView(spinner);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Detailed Description...");
        etDesc.setMinLines(3);
        layout.addView(etDesc);

        builder.setView(layout);
        builder.setPositiveButton("SUBMIT", (dialog, which) -> {
            Toast.makeText(this, "Problem Reported to Developer! We will fix it soon.", Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void showFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Feedback & Suggestions");
        final EditText input = new EditText(this);
        input.setHint("How can we make AceGrade better for you?");
        input.setPadding(50, 40, 50, 10);
        builder.setView(input);
        builder.setPositiveButton("SEND", (dialog, which) -> {
            Toast.makeText(this, "Thank you for your suggestion!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }
}
