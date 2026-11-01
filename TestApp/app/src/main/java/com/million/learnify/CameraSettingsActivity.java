package com.million.learnify;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * CameraSettingsActivity - Manages CameraX, OCR, and Science Studio preferences.
 * Configures:
 * - Camera hardware runtime permissions
 * - Math formula & diagram recognition
 * - Viewfinder grid alignment
 * - Image quality / capture resolution
 * - Auto-detection and auto-saving to Lab Notebook
 */
public class CameraSettingsActivity extends BaseActivity {

    public static final String PREFS_NAME = "CameraSettings";
    public static final String KEY_AUTO_DETECT = "auto_detect";
    public static final String KEY_GRID = "grid_lines";
    public static final String KEY_SAVE_HISTORY = "save_history";
    public static final String KEY_FORMULA_DETECTION = "formula_detection";
    public static final String KEY_DIAGRAM_DETECTION = "diagram_detection";
    public static final String KEY_IMAGE_QUALITY = "image_quality";
    public static final String KEY_AUTO_SAVE = "auto_save_scans";
    public static final String KEY_SUBJECT_SPECIFIC = "subject_specific_detection";

    private static final int REQ_CAMERA_PERMISSION = 2001;

    private SharedPreferences prefs;

    private MaterialCardView cardCameraPermission;
    private ImageView ivCameraPermissionIcon;
    private TextView tvCameraPermissionTitle, tvCameraPermissionStatus;
    private MaterialButton btnGrantCameraPermission;

    private MaterialSwitch switchFormulaDetection, switchDiagramDetection, switchAutoDetect,
            switchSubjectSpecific, switchGrid, switchSaveHistory, switchAutoSave;
    private LinearLayout llImageQuality;
    private TextView tvImageQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupToolbar();
        initViews();
        checkCameraPermissionStatus();
        loadSettingsToUI();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        cardCameraPermission = findViewById(R.id.cardCameraPermission);
        ivCameraPermissionIcon = findViewById(R.id.ivCameraPermissionIcon);
        tvCameraPermissionTitle = findViewById(R.id.tvCameraPermissionTitle);
        tvCameraPermissionStatus = findViewById(R.id.tvCameraPermissionStatus);
        btnGrantCameraPermission = findViewById(R.id.btnGrantCameraPermission);

        switchFormulaDetection = findViewById(R.id.switchFormulaDetection);
        switchDiagramDetection = findViewById(R.id.switchDiagramDetection);
        switchAutoDetect = findViewById(R.id.switchAutoDetect);
        switchSubjectSpecific = findViewById(R.id.switchSubjectSpecific);
        switchGrid = findViewById(R.id.switchGrid);
        switchSaveHistory = findViewById(R.id.switchSaveHistory);
        switchAutoSave = findViewById(R.id.switchAutoSave);

        llImageQuality = findViewById(R.id.llImageQuality);
        tvImageQuality = findViewById(R.id.tvImageQuality);
    }

    private void checkCameraPermissionStatus() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            if (cardCameraPermission != null) {
                cardCameraPermission.setCardBackgroundColor(0xFFF0FDF4); // light green
                cardCameraPermission.setStrokeColor(0xFFBBF7D0);
            }
            if (ivCameraPermissionIcon != null) {
                ivCameraPermissionIcon.setImageResource(R.drawable.ic_verified_check);
                ivCameraPermissionIcon.setColorFilter(0xFF16A34A);
            }
            if (tvCameraPermissionTitle != null) tvCameraPermissionTitle.setText("Camera Hardware Ready");
            if (tvCameraPermissionStatus != null) tvCameraPermissionStatus.setText("Camera permission granted for Science Studio.");
            if (btnGrantCameraPermission != null) btnGrantCameraPermission.setVisibility(View.GONE);
        } else {
            if (cardCameraPermission != null) {
                cardCameraPermission.setCardBackgroundColor(0xFFFEF2F2); // light red
                cardCameraPermission.setStrokeColor(0xFFFECACA);
            }
            if (ivCameraPermissionIcon != null) {
                ivCameraPermissionIcon.setImageResource(R.drawable.ic_shield_security);
                ivCameraPermissionIcon.setColorFilter(0xFFDC2626);
            }
            if (tvCameraPermissionTitle != null) tvCameraPermissionTitle.setText("Camera Permission Required");
            if (tvCameraPermissionStatus != null) tvCameraPermissionStatus.setText("Camera access is needed to scan math problems and science diagrams.");
            if (btnGrantCameraPermission != null) {
                btnGrantCameraPermission.setVisibility(View.VISIBLE);
                btnGrantCameraPermission.setOnClickListener(v -> {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
                });
            }
        }
    }

    private void loadSettingsToUI() {
        if (switchFormulaDetection != null) {
            switchFormulaDetection.setChecked(prefs.getBoolean(KEY_FORMULA_DETECTION, true));
        }
        if (switchDiagramDetection != null) {
            switchDiagramDetection.setChecked(prefs.getBoolean(KEY_DIAGRAM_DETECTION, true));
        }
        if (switchAutoDetect != null) {
            switchAutoDetect.setChecked(prefs.getBoolean(KEY_AUTO_DETECT, true));
        }
        if (switchSubjectSpecific != null) {
            switchSubjectSpecific.setChecked(prefs.getBoolean(KEY_SUBJECT_SPECIFIC, true));
        }
        if (switchGrid != null) {
            switchGrid.setChecked(prefs.getBoolean(KEY_GRID, false));
        }
        if (switchSaveHistory != null) {
            switchSaveHistory.setChecked(prefs.getBoolean(KEY_SAVE_HISTORY, true));
        }
        if (switchAutoSave != null) {
            switchAutoSave.setChecked(prefs.getBoolean(KEY_AUTO_SAVE, true));
        }
        updateImageQualityUI();
    }

    private void setupListeners() {
        setupSwitch(switchFormulaDetection, KEY_FORMULA_DETECTION, "Formula OCR");
        setupSwitch(switchDiagramDetection, KEY_DIAGRAM_DETECTION, "Diagram detection");
        setupSwitch(switchAutoDetect, KEY_AUTO_DETECT, "Auto-detect");
        setupSwitch(switchSubjectSpecific, KEY_SUBJECT_SPECIFIC, "Subject filtering");
        setupSwitch(switchGrid, KEY_GRID, "Grid lines");
        setupSwitch(switchSaveHistory, KEY_SAVE_HISTORY, "Scan history");
        setupSwitch(switchAutoSave, KEY_AUTO_SAVE, "Auto-save solutions");

        if (llImageQuality != null) {
            llImageQuality.setOnClickListener(v -> showImageQualityDialog());
        }
    }

    private void setupSwitch(MaterialSwitch sw, String prefKey, String label) {
        if (sw != null) {
            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(buttonView);
                prefs.edit().putBoolean(prefKey, isChecked).apply();
                Toast.makeText(this, label + " " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showImageQualityDialog() {
        String[] options = {
                "High Quality (1080p — Best for fine text & diagrams)",
                "Balanced (720p — Recommended for speed & clarity)",
                "Data Saver (480p — Low bandwidth & low storage)"
        };

        String currentQuality = prefs.getString(KEY_IMAGE_QUALITY, "balanced");
        int checkedItem = "high".equalsIgnoreCase(currentQuality) ? 0 : ("data_saver".equalsIgnoreCase(currentQuality) ? 2 : 1);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Capture Quality")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    String selected = which == 0 ? "high" : (which == 2 ? "data_saver" : "balanced");
                    prefs.edit().putString(KEY_IMAGE_QUALITY, selected).apply();
                    updateImageQualityUI();
                    dialog.dismiss();
                    Toast.makeText(this, "Capture quality updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateImageQualityUI() {
        if (tvImageQuality == null) return;
        String quality = prefs.getString(KEY_IMAGE_QUALITY, "balanced");
        if ("high".equalsIgnoreCase(quality)) {
            tvImageQuality.setText("High (1080p) ▼");
        } else if ("data_saver".equalsIgnoreCase(quality)) {
            tvImageQuality.setText("Data Saver (480p) ▼");
        } else {
            tvImageQuality.setText("Balanced (720p) ▼");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION) {
            checkCameraPermissionStatus();
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission is required for AI Scanner", Toast.LENGTH_LONG).show();
            }
        }
    }
}
