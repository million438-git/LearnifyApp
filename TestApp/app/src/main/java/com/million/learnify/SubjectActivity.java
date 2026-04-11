package com.million.learnify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import java.io.IOException;

/**
 * SubjectActivity - Student academic library portal.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class SubjectActivity extends BaseActivity {

    private static final String TAG = "SubjectActivity";
    private String currentUsername, userGrade;
    private LinearLayout subjectContainer;
    private SharedPreferences downloadPrefs;
    private static final String PREF_DOWNLOADS = "DownloadedPacks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Student users to enter this portal.
        enforceRole("STUDENT");
        
        setContentView(R.layout.activity_subject);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "");
        userGrade = pref.getString("grade", "10");
        downloadPrefs = getSharedPreferences(PREF_DOWNLOADS, MODE_PRIVATE);

        subjectContainer = findViewById(R.id.subjectContainer);
        setupToolbar();
        loadDynamicSubjects();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Academic Library");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void loadDynamicSubjects() {
        if (subjectContainer == null) return;
        subjectContainer.removeAllViews();
        
        try {
            String[] subjects = getAssets().list("subjects");
            if (subjects != null) {
                for (String subjectName : subjects) {
                    if (!subjectName.contains(".")) addSubjectCard(subjectName);
                }
            }
        } catch (IOException e) { Log.e(TAG, "Error loading catalog", e); }
    }

    private void addSubjectCard(String subjectName) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_subject_card_vertical, subjectContainer, false);
        TextView nameText = card.findViewById(R.id.subjectNameText);
        TextView statusText = card.findViewById(R.id.subjectStatusText);
        TextView emojiText = card.findViewById(R.id.tvSubjectEmoji);
        MaterialButton btnDownload = card.findViewById(R.id.btnDownloadPack);

        String displayName = subjectName.substring(0, 1).toUpperCase() + subjectName.substring(1);
        nameText.setText(displayName);
        emojiText.setText(getEmojiForSubject(subjectName));

        boolean isDownloaded = downloadPrefs.getBoolean(subjectName, false);
        if (isDownloaded) {
            statusText.setText("✅ Knowledge Pack Active (Offline)");
            if (btnDownload != null) { btnDownload.setText("Synced"); btnDownload.setEnabled(false); }
        } else {
            statusText.setText("Master " + displayName + " lessons offline");
            if (btnDownload != null) {
                btnDownload.setOnClickListener(v -> downloadPack(subjectName, displayName, btnDownload, statusText));
            }
        }

        card.setOnClickListener(v -> {
            if (repository.getLocalDb().hasPremiumAccess(currentUsername, userGrade)) {
                Intent intent = new Intent(SubjectActivity.this, ChapterListActivity.class);
                intent.putExtra("SUBJECT_NAME", subjectName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "🔐 Access Restricted. Requires Premium or Admin Enrollment.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, PaymentActivity.class));
            }
        });

        subjectContainer.addView(card);
    }

    private void downloadPack(String id, String name, MaterialButton btn, TextView status) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Syncing " + name);
        progress.setMessage("Optimizing for Offline Access...");
        progress.setCancelable(false);
        progress.show();

        new Handler().postDelayed(() -> {
            progress.dismiss();
            downloadPrefs.edit().putBoolean(id, true).apply();
            if (btn != null) { btn.setText("Synced"); btn.setEnabled(false); }
            status.setText("✅ Knowledge Pack Active (Offline)");
            Toast.makeText(this, name + " is now full-functional offline!", Toast.LENGTH_LONG).show();
        }, 1500);
    }

    private String getEmojiForSubject(String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("biology")) return "🧬";
        if (lowerName.contains("physics")) return "⚛️";
        if (lowerName.contains("chemistry")) return "🧪";
        if (lowerName.contains("math")) return "📐";
        if (lowerName.contains("english")) return "📚";
        return "📖";
    }
}
