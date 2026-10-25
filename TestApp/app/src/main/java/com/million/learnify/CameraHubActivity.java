package com.million.learnify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Locale;

public class CameraHubActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername;
    private LinearLayout llNotebookContainer;
    private View cardEmptyNotebook;
    private LinearLayout llRecentScansContainer;
    private View cardEmptyScans;
    private TextToSpeech tts;
    private boolean isTtsReady = false;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    Intent intent = new Intent(this, CameraLabActivity.class);
                    intent.putExtra("IMAGE_URI", selectedImageUri.toString());
                    startActivity(intent);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_hub);

        dbHelper = new DatabaseHelper(this);

        // Admin Governance Check
        if (!dbHelper.isCameraLabEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Access Restricted")
                    .setMessage("Camera Lab & AI Science Studio are currently disabled by administrator.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (d, w) -> finish())
                    .show();
            return;
        }

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Guest");

        initTts();
        setupToolbar();
        initActions();
    }

    private void initTts() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(Locale.US);
                isTtsReady = (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotebookEntries();
        loadRecentScans();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHub);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("AI Science Studio");
                getSupportActionBar().setSubtitle("Discover. Experiment. Understand.");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        View btnAudio = findViewById(R.id.btnHubAudioGuide);
        if (btnAudio != null) {
            btnAudio.setOnClickListener(v -> speakOverview());
        }

        View btnNotebook = findViewById(R.id.btnHubNotebook);
        if (btnNotebook != null) {
            btnNotebook.setOnClickListener(v -> startActivity(new Intent(this, LabNotebookActivity.class)));
        }

        View btnHistory = findViewById(R.id.btnHubHistory);
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> startActivity(new Intent(this, CameraHistoryActivity.class)));
        }

        View btnSettings = findViewById(R.id.btnHubSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> startActivity(new Intent(this, CameraSettingsActivity.class)));
        }
    }

    private void speakOverview() {
        if (!isTtsReady || tts == null) {
            Toast.makeText(this, "Speech engine initializing, please try again in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = "Welcome to Learnify AI Science Studio: Discover, Experiment, and Understand. "
                + "You can scan specimens and math formulas with your camera, "
                + "explore over 117 virtual interactive science simulations in Biology, Physics, and Chemistry, "
                + "or launch the Grade 9 Leaf Investigation to discover plant anatomy and photosynthesis.";
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "SCIENCE_STUDIO_OVERVIEW");
        Toast.makeText(this, "Playing Audio Guide...", Toast.LENGTH_SHORT).show();
    }

    private void initActions() {
        llNotebookContainer = findViewById(R.id.llNotebookContainer);
        cardEmptyNotebook = findViewById(R.id.cardEmptyNotebook);
        llRecentScansContainer = findViewById(R.id.llRecentScansContainer);
        cardEmptyScans = findViewById(R.id.cardEmptyScans);

        // Hero CTA Buttons
        View btnOpenScanner = findViewById(R.id.btnOpenScanner);
        if (btnOpenScanner != null) {
            btnOpenScanner.setOnClickListener(v -> startActivity(new Intent(this, CameraLabActivity.class)));
        }

        View btnExploreExperiments = findViewById(R.id.btnExploreExperiments);
        if (btnExploreExperiments != null) {
            btnExploreExperiments.setOnClickListener(v -> startActivity(new Intent(this, ExperimentCatalogueActivity.class)));
        }

        // Featured Guided Investigation: Grade 9 Leaf Investigation
        View.OnClickListener openLeafInvestigation = v -> {
            Intent intent = new Intent(this, LeafInvestigationActivity.class);
            startActivity(intent);
        };
        View cardLeaf = findViewById(R.id.cardLeafInvestigation);
        if (cardLeaf != null) cardLeaf.setOnClickListener(openLeafInvestigation);
        View btnStartLeaf = findViewById(R.id.btnStartLeafInvestigation);
        if (btnStartLeaf != null) btnStartLeaf.setOnClickListener(openLeafInvestigation);

        // Discovery Laboratories
        View cardBio = findViewById(R.id.cardSubjectBiology);
        if (cardBio != null) {
            cardBio.setOnClickListener(v -> startActivity(new Intent(this, BiologyLabActivity.class)));
        }

        View cardPhy = findViewById(R.id.cardSubjectPhysics);
        if (cardPhy != null) {
            cardPhy.setOnClickListener(v -> startActivity(new Intent(this, PhysicsLabActivity.class)));
        }

        View cardChm = findViewById(R.id.cardSubjectChemistry);
        if (cardChm != null) {
            cardChm.setOnClickListener(v -> startActivity(new Intent(this, ChemistryLabActivity.class)));
        }

        View cardAll = findViewById(R.id.cardAllSimulations);
        if (cardAll != null) {
            cardAll.setOnClickListener(v -> startActivity(new Intent(this, ExperimentCatalogueActivity.class)));
        }

        // Scanner Tools
        View cardScan = findViewById(R.id.cardScanPage);
        if (cardScan != null) {
            cardScan.setOnClickListener(v -> startActivity(new Intent(this, CameraLabActivity.class)));
        }

        View cardImport = findViewById(R.id.cardImportPhoto);
        if (cardImport != null) {
            cardImport.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(intent);
            });
        }

        // View All Buttons
        View btnViewNotebook = findViewById(R.id.btnViewAllNotebook);
        if (btnViewNotebook != null) {
            btnViewNotebook.setOnClickListener(v -> startActivity(new Intent(this, LabNotebookActivity.class)));
        }

        View btnViewAll = findViewById(R.id.btnViewAllScans);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> startActivity(new Intent(this, CameraHistoryActivity.class)));
        }
    }

    private void loadNotebookEntries() {
        if (llNotebookContainer == null) return;
        llNotebookContainer.removeAllViews();

        List<DatabaseHelper.NotebookEntry> entries = dbHelper.getNotebookEntries(currentUsername);

        if (entries == null || entries.isEmpty()) {
            if (cardEmptyNotebook != null) cardEmptyNotebook.setVisibility(View.VISIBLE);
            return;
        }

        if (cardEmptyNotebook != null) cardEmptyNotebook.setVisibility(View.GONE);

        // Display up to top 2 recent entries
        int count = Math.min(entries.size(), 2);
        for (int i = 0; i < count; i++) {
            DatabaseHelper.NotebookEntry entry = entries.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_science_notebook_entry, llNotebookContainer, false);

            TextView tvSubject = itemView.findViewById(R.id.tvEntrySubject);
            TextView tvStatus = itemView.findViewById(R.id.tvEntryStatus);
            TextView tvDate = itemView.findViewById(R.id.tvEntryDate);
            TextView tvTitle = itemView.findViewById(R.id.tvEntryTitle);
            TextView tvHypothesis = itemView.findViewById(R.id.tvEntryHypothesis);
            TextView tvScore = itemView.findViewById(R.id.tvEntryScore);

            if (tvSubject != null) tvSubject.setText(entry.subject != null ? entry.subject.toUpperCase() : "SCIENCE");
            if (tvStatus != null) {
                if (entry.isCompleted) {
                    tvStatus.setText("Completed ✓");
                    tvStatus.setTextColor(0xFF34D399); // Emerald
                } else {
                    tvStatus.setText("Stage " + entry.currentStage + " • In Progress");
                    tvStatus.setTextColor(0xFF38BDF8); // Cyan
                }
            }
            if (tvDate != null) tvDate.setText(entry.updatedAt != null ? entry.updatedAt : "");
            if (tvTitle != null) tvTitle.setText(entry.title);
            if (tvHypothesis != null) {
                String hyp = entry.hypothesis != null && !entry.hypothesis.trim().isEmpty() 
                        ? "Hypothesis: " + entry.hypothesis 
                        : "Observations: " + (entry.observations != null ? entry.observations : "In progress");
                tvHypothesis.setText(hyp);
            }
            if (tvScore != null) {
                tvScore.setText("Quiz Score: " + entry.quizScore + "%");
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, LabNotebookActivity.class);
                intent.putExtra("OPEN_ENTRY_ID", entry.investigationId);
                startActivity(intent);
            });

            llNotebookContainer.addView(itemView);
        }
    }

    private void loadRecentScans() {
        if (llRecentScansContainer == null) return;
        llRecentScansContainer.removeAllViews();

        Cursor cursor = dbHelper.getLabHistory(currentUsername);
        int count = 0;

        if (cursor != null) {
            while (cursor.moveToNext() && count < 2) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("problem_type"));
                String problem = cursor.getString(cursor.getColumnIndexOrThrow("problem_text"));
                String solution = cursor.getString(cursor.getColumnIndexOrThrow("solution_text"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_camera_history, llRecentScansContainer, false);
                TextView tvType = itemView.findViewById(R.id.tvScanType);
                TextView tvResult = itemView.findViewById(R.id.tvScanResult);
                TextView tvDate = itemView.findViewById(R.id.tvScanDate);

                if (tvType != null) tvType.setText(type != null ? type : "LAB");
                if (tvResult != null) tvResult.setText(problem != null && !problem.isEmpty() ? problem : solution);
                if (tvDate != null) tvDate.setText(timestamp != null ? timestamp : "");

                final String finalProb = problem;
                final String finalSol = solution;
                final String finalType = type;
                final String finalTime = timestamp;

                itemView.setOnClickListener(v -> showDetailDialog(finalType, finalProb, finalSol, finalTime));

                llRecentScansContainer.addView(itemView);
                count++;
            }
            cursor.close();
        }

        if (count == 0) {
            if (cardEmptyScans != null) cardEmptyScans.setVisibility(View.VISIBLE);
        } else {
            if (cardEmptyScans != null) cardEmptyScans.setVisibility(View.GONE);
        }
    }

    private void showDetailDialog(String type, String problem, String solution, String timestamp) {
        StringBuilder details = new StringBuilder();
        details.append("PROBLEM:\n").append(problem).append("\n\n");
        details.append("STEP-BY-STEP SOLUTION:\n").append(solution).append("\n\n");
        details.append("Scanned: ").append(timestamp);

        new MaterialAlertDialogBuilder(this)
                .setTitle(type + " Solution")
                .setMessage(details.toString())
                .setPositiveButton("COPY", (d, w) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Lab Solution", solution);
                    cm.setPrimaryClip(clip);
                    Toast.makeText(this, "Solution copied.", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("SHARE", (d, w) -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Learnify Solution - " + type);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Learnify Solution:\n\n" + details);
                    startActivity(Intent.createChooser(shareIntent, "Share Solution"));
                })
                .setNegativeButton("CLOSE", null)
                .show();
    }
}
