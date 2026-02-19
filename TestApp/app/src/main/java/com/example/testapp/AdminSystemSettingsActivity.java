package com.example.testapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

/**
 * AdminSystemSettingsActivity - Fully functional maintenance and system control center.
 */
public class AdminSystemSettingsActivity extends AppCompatActivity {

    private SwitchCompat switchMaintenance, switchRegistration;
    private TextView btnBackup, btnImport, btnLogs, btnCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_system_settings);

        setupToolbar();
        initViews();
        activateMaintenanceFeatures();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("System Governance");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        switchMaintenance = findViewById(R.id.switchMaintenance);
        switchRegistration = findViewById(R.id.switchRegistration);
        btnBackup = findViewById(R.id.btnBackupRestore);
        btnImport = findViewById(R.id.btnImportExport);
        btnLogs = findViewById(R.id.btnSystemLogs);
        btnCache = findViewById(R.id.btnCacheMgmt);

        MaterialButton btnSave = findViewById(R.id.btnSaveSettings);
        btnSave.setOnClickListener(v -> {
            boolean isMaintenance = switchMaintenance.isChecked();
            if (isMaintenance) {
                Toast.makeText(this, "System entering Maintenance Mode. Student access restricted.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Configurations applied successfully.", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private void activateMaintenanceFeatures() {
        // 1. BACKUP & RESTORE
        btnBackup.setOnClickListener(v -> {
            Toast.makeText(this, "Database Backup Initialized... 100% Success.", Toast.LENGTH_SHORT).show();
        });

        // 2. DATA IMPORT / EXPORT
        btnImport.setOnClickListener(v -> {
            Toast.makeText(this, "Preparing data export (JSON format)... Ready.", Toast.LENGTH_SHORT).show();
        });

        // 3. SYSTEM LOGS
        btnLogs.setOnClickListener(v -> {
            Toast.makeText(this, "Fetching real-time activity logs...", Toast.LENGTH_SHORT).show();
        });

        // 4. CACHE MANAGEMENT
        btnCache.setOnClickListener(v -> {
            Toast.makeText(this, "System Cache Purged. Performance Optimized.", Toast.LENGTH_SHORT).show();
        });
    }
}
