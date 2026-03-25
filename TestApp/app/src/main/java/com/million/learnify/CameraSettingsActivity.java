package com.million.learnify;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class CameraSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_settings);

        setupToolbar();
        setupSwitches();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Camera Settings");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSwitches() {
        MaterialSwitch switchAutoDetect = findViewById(R.id.switchAutoDetect);
        MaterialSwitch switchGrid = findViewById(R.id.switchGrid);
        MaterialSwitch switchSaveHistory = findViewById(R.id.switchSaveHistory);

        switchAutoDetect.setOnCheckedChangeListener((v, isChecked) -> 
            Toast.makeText(this, "Auto-detect " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show());
            
        switchGrid.setOnCheckedChangeListener((v, isChecked) -> 
            Toast.makeText(this, "Grid lines " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show());

        switchSaveHistory.setOnCheckedChangeListener((v, isChecked) -> 
            Toast.makeText(this, "History saving " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show());
    }
}
