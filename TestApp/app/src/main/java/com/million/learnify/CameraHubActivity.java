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

public class CameraHubActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername;
    private LinearLayout llRecentScansContainer;
    private View cardEmptyScans;

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
                    .setMessage("Camera Lab is currently disabled by administrator.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (d, w) -> finish())
                    .show();
            return;
        }

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Guest");

        setupToolbar();
        initActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentScans();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHub);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Camera & Science Lab");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
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

    private void initActions() {
        llRecentScansContainer = findViewById(R.id.llRecentScansContainer);
        cardEmptyScans = findViewById(R.id.cardEmptyScans);

        // Primary Action: Open Camera Scanner
        findViewById(R.id.cardScanPage).setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraLabActivity.class);
            startActivity(intent);
        });

        // Primary Action: Import Photo
        findViewById(R.id.cardImportPhoto).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        // Science Explorer Shortcuts
        findViewById(R.id.cardSubjectMath).setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraLabActivity.class);
            intent.putExtra("COURSE_NAME", "Mathematics");
            startActivity(intent);
        });

        findViewById(R.id.cardSubjectPhysics).setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraLabActivity.class);
            intent.putExtra("COURSE_NAME", "Physics");
            startActivity(intent);
        });

        findViewById(R.id.cardSubjectChemistry).setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraLabActivity.class);
            intent.putExtra("COURSE_NAME", "Chemistry");
            startActivity(intent);
        });

        // Interactive Visualizer Labs Bridge
        findViewById(R.id.cardInteractiveLabs).setOnClickListener(v -> {
            Intent intent = new Intent(this, VisualizerActivity.class);
            startActivity(intent);
        });

        // View All Scans
        View btnViewAll = findViewById(R.id.btnViewAllScans);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> startActivity(new Intent(this, CameraHistoryActivity.class)));
        }
    }

    private void loadRecentScans() {
        if (llRecentScansContainer == null) return;
        llRecentScansContainer.removeAllViews();

        Cursor cursor = dbHelper.getLabHistory(currentUsername);
        int count = 0;

        if (cursor != null) {
            while (cursor.moveToNext() && count < 3) {
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

