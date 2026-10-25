package com.million.learnify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.materialswitch.MaterialSwitch;

public class CameraSettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "CameraSettings";
    public static final String KEY_AUTO_DETECT = "auto_detect";
    public static final String KEY_GRID = "grid_lines";
    public static final String KEY_SAVE_HISTORY = "save_history";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupToolbar();
        setupSwitches();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Camera Settings");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupSwitches() {
        MaterialSwitch switchAutoDetect = findViewById(R.id.switchAutoDetect);
        MaterialSwitch switchGrid = findViewById(R.id.switchGrid);
        MaterialSwitch switchSaveHistory = findViewById(R.id.switchSaveHistory);

        if (switchAutoDetect != null) {
            switchAutoDetect.setChecked(prefs.getBoolean(KEY_AUTO_DETECT, true));
            switchAutoDetect.setOnCheckedChangeListener((v, isChecked) -> {
                prefs.edit().putBoolean(KEY_AUTO_DETECT, isChecked).apply();
                Toast.makeText(this, "Auto-detect " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            });
        }

        if (switchGrid != null) {
            switchGrid.setChecked(prefs.getBoolean(KEY_GRID, false));
            switchGrid.setOnCheckedChangeListener((v, isChecked) -> {
                prefs.edit().putBoolean(KEY_GRID, isChecked).apply();
                Toast.makeText(this, "Grid lines " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            });
        }

        if (switchSaveHistory != null) {
            switchSaveHistory.setChecked(prefs.getBoolean(KEY_SAVE_HISTORY, true));
            switchSaveHistory.setOnCheckedChangeListener((v, isChecked) -> {
                prefs.edit().putBoolean(KEY_SAVE_HISTORY, isChecked).apply();
                Toast.makeText(this, "History saving " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
