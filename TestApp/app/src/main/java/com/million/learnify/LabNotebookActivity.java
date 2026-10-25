package com.million.learnify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Locale;

public class LabNotebookActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername;

    private TextView tvStatTotal, tvStatCompleted, tvStatAvgScore;
    private LinearLayout llNotebookEntries;
    private View cardEmptyNotebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab_notebook);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Guest");

        setupToolbar();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEntries();

        // Check if opened for a specific entry ID
        String openId = getIntent().getStringExtra("OPEN_ENTRY_ID");
        if (openId != null && !openId.isEmpty()) {
            DatabaseHelper.NotebookEntry entry = dbHelper.getNotebookEntry(currentUsername, openId);
            if (entry != null) {
                showEntryDetailDialog(entry);
            }
            getIntent().removeExtra("OPEN_ENTRY_ID");
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarNotebook);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        View btnNew = findViewById(R.id.btnNewInvestigation);
        if (btnNew != null) {
            btnNew.setOnClickListener(v -> startActivity(new Intent(this, LeafInvestigationActivity.class)));
        }
    }

    private void initViews() {
        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatCompleted = findViewById(R.id.tvStatCompleted);
        tvStatAvgScore = findViewById(R.id.tvStatAvgScore);
        llNotebookEntries = findViewById(R.id.llNotebookEntries);
        cardEmptyNotebook = findViewById(R.id.cardEmptyNotebook);

        View btnEmpty = findViewById(R.id.btnEmptyLaunchLeaf);
        if (btnEmpty != null) {
            btnEmpty.setOnClickListener(v -> startActivity(new Intent(this, LeafInvestigationActivity.class)));
        }
    }

    private void loadEntries() {
        llNotebookEntries.removeAllViews();
        List<DatabaseHelper.NotebookEntry> entries = dbHelper.getNotebookEntries(currentUsername);

        if (entries == null || entries.isEmpty()) {
            tvStatTotal.setText("0");
            tvStatCompleted.setText("0");
            tvStatAvgScore.setText("0%");
            cardEmptyNotebook.setVisibility(View.VISIBLE);
            return;
        }

        cardEmptyNotebook.setVisibility(View.GONE);

        int totalCount = entries.size();
        int completedCount = 0;
        int totalScore = 0;

        for (DatabaseHelper.NotebookEntry entry : entries) {
            if (entry.isCompleted) completedCount++;
            totalScore += entry.quizScore;

            View itemView = LayoutInflater.from(this).inflate(R.layout.item_science_notebook_entry, llNotebookEntries, false);

            TextView tvSubject = itemView.findViewById(R.id.tvEntrySubject);
            TextView tvStatus = itemView.findViewById(R.id.tvEntryStatus);
            TextView tvDate = itemView.findViewById(R.id.tvEntryDate);
            TextView tvTitle = itemView.findViewById(R.id.tvEntryTitle);
            TextView tvHypothesis = itemView.findViewById(R.id.tvEntryHypothesis);
            TextView tvScore = itemView.findViewById(R.id.tvEntryScore);

            if (tvSubject != null) tvSubject.setText(entry.subject != null ? entry.subject.toUpperCase() : "SCIENCE");
            if (tvStatus != null) {
                if (entry.isCompleted) {
                    tvStatus.setText("Stage " + entry.currentStage + " • Completed ✓");
                    tvStatus.setTextColor(0xFF34D399); // Emerald
                } else {
                    tvStatus.setText("Stage " + entry.currentStage + " • In Progress");
                    tvStatus.setTextColor(0xFF38BDF8); // Cyan
                }
            }
            if (tvDate != null) tvDate.setText(entry.updatedAt != null ? entry.updatedAt : "");
            if (tvTitle != null) tvTitle.setText(entry.title);
            if (tvHypothesis != null) {
                String hyp = (entry.hypothesis != null && !entry.hypothesis.trim().isEmpty())
                        ? "Hypothesis: " + entry.hypothesis
                        : "Observations: " + (entry.observations != null ? entry.observations : "In progress");
                tvHypothesis.setText(hyp);
            }
            if (tvScore != null) {
                tvScore.setText("Quiz Score: " + entry.quizScore + "%");
            }

            itemView.setOnClickListener(v -> showEntryDetailDialog(entry));

            llNotebookEntries.addView(itemView);
        }

        tvStatTotal.setText(String.valueOf(totalCount));
        tvStatCompleted.setText(String.valueOf(completedCount));
        int avgScore = totalCount > 0 ? (totalScore / totalCount) : 0;
        tvStatAvgScore.setText(avgScore + "%");
    }

    private void showEntryDetailDialog(DatabaseHelper.NotebookEntry entry) {
        StringBuilder report = new StringBuilder();
        report.append("═════════════════════════════════════\n");
        report.append("LEARNIFY STUDENT LAB NOTEBOOK REPORT\n");
        report.append("═════════════════════════════════════\n\n");
        report.append("INVESTIGATION: ").append(entry.title).append("\n");
        report.append("CURRICULUM: ").append(entry.curriculumRef).append("\n");
        report.append("STATUS: ").append(entry.isCompleted ? "COMPLETED" : "IN PROGRESS (STAGE " + entry.currentStage + ")").append("\n");
        report.append("QUIZ SCORE: ").append(entry.quizScore).append("%\n\n");

        report.append("1. HYPOTHESIS:\n");
        report.append(entry.hypothesis != null && !entry.hypothesis.isEmpty() ? entry.hypothesis : "Not recorded").append("\n\n");

        report.append("2. SPECIMEN OBSERVATIONS:\n");
        report.append(entry.observations != null && !entry.observations.isEmpty() ? entry.observations : "Not recorded").append("\n\n");

        if (entry.diagramStructures != null && !entry.diagramStructures.isEmpty()) {
            report.append("3. ANATOMICAL STRUCTURES INVESTIGATED:\n");
            report.append(entry.diagramStructures).append("\n\n");
        }

        if (entry.simulationInputs != null && !entry.simulationInputs.isEmpty()) {
            report.append("4. SIMULATION CONDITIONS:\n");
            report.append(entry.simulationInputs).append("\n");
            report.append("Results: ").append(entry.simulationResults != null ? entry.simulationResults : "N/A").append("\n\n");
        }

        report.append("5. SCIENTIFIC CONCLUSION:\n");
        report.append(entry.conclusion != null && !entry.conclusion.isEmpty() ? entry.conclusion : "Pending completion").append("\n\n");

        report.append("Last Updated: ").append(entry.updatedAt);

        final String finalReport = report.toString();

        new MaterialAlertDialogBuilder(this)
                .setTitle(entry.title)
                .setMessage(finalReport)
                .setPositiveButton("RESUME LAB", (d, w) -> {
                    Intent intent;
                    if ("bio_grade9_leaf_01".equalsIgnoreCase(entry.investigationId)) {
                        intent = new Intent(this, LeafInvestigationActivity.class);
                    } else {
                        intent = new Intent(this, CameraHubActivity.class);
                    }
                    startActivity(intent);
                })
                .setNeutralButton("COPY REPORT", (d, w) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Lab Report", finalReport);
                    cm.setPrimaryClip(clip);
                    Toast.makeText(this, "Lab report copied to clipboard.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("SHARE", (d, w) -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, entry.title + " - Lab Report");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, finalReport);
                    startActivity(Intent.createChooser(shareIntent, "Share Lab Report"));
                })
                .show();
    }
}

