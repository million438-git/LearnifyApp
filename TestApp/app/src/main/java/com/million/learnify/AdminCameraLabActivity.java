package com.million.learnify;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminCameraLabActivity - Governance Hub for Camera Lab.
 * Implements Section 11 requirements for full platform control.
 */
public class AdminCameraLabActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private MaterialSwitch switchGlobalCamera;
    private CheckBox cbMath, cbPhysics, cbChemistry;
    private TextInputEditText etMaxSteps;
    private TextView tvTotalScans, tvSuccessRate;
    private RadioGroup rgProcessMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_camera_lab);

        dbHelper = new DatabaseHelper(this);
        setupToolbar();
        initViews();
        loadSettings();
        loadAnalytics();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.adminCameraToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        switchGlobalCamera = findViewById(R.id.switchGlobalCamera);
        cbMath = findViewById(R.id.cbMath);
        cbPhysics = findViewById(R.id.cbPhysics);
        cbChemistry = findViewById(R.id.cbChemistry);
        etMaxSteps = findViewById(R.id.etMaxSteps);
        tvTotalScans = findViewById(R.id.tvTotalScans);
        tvSuccessRate = findViewById(R.id.tvSuccessRate);
        rgProcessMode = findViewById(R.id.rgProcessMode);

        findViewById(R.id.btnSaveAdminCamera).setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        switchGlobalCamera.setChecked(dbHelper.getSetting("camera_lab_enabled", "1").equals("1"));
        cbMath.setChecked(dbHelper.getSetting("lab_math_enabled", "1").equals("1"));
        cbPhysics.setChecked(dbHelper.getSetting("lab_physics_enabled", "1").equals("1"));
        cbChemistry.setChecked(dbHelper.getSetting("lab_chem_enabled", "1").equals("1"));
        etMaxSteps.setText(dbHelper.getSetting("lab_max_steps", "10"));
        
        String mode = dbHelper.getSetting("lab_process_mode", "CLIENT");
        if (mode.equals("SERVER")) rgProcessMode.check(R.id.rbServer);
        else if (mode.equals("HYBRID")) rgProcessMode.check(R.id.rbHybrid);
        else rgProcessMode.check(R.id.rbClient);
    }

    private void loadAnalytics() {
        Cursor c = dbHelper.getLabUsageAnalytics();
        int total = 0;
        if (c != null) {
            while (c.moveToNext()) {
                total += c.getInt(1);
            }
            c.close();
        }
        tvTotalScans.setText("Total Lab Scans: " + total);
        tvSuccessRate.setText("Recognition Success Rate: 96.4%"); // Mock data for demo
    }

    private void saveSettings() {
        dbHelper.saveSetting("camera_lab_enabled", switchGlobalCamera.isChecked() ? "1" : "0");
        dbHelper.saveSetting("lab_math_enabled", cbMath.isChecked() ? "1" : "0");
        dbHelper.saveSetting("lab_physics_enabled", cbPhysics.isChecked() ? "1" : "0");
        dbHelper.saveSetting("lab_chem_enabled", cbChemistry.isChecked() ? "1" : "0");
        dbHelper.saveSetting("lab_max_steps", etMaxSteps.getText().toString());

        int checkedId = rgProcessMode.getCheckedRadioButtonId();
        String mode = "CLIENT";
        if (checkedId == R.id.rbServer) mode = "SERVER";
        else if (checkedId == R.id.rbHybrid) mode = "HYBRID";
        dbHelper.saveSetting("lab_process_mode", mode);

        dbHelper.logAdminAction("admin", "CAMERA_LAB_UPDATE", "Updated Camera Lab global policies.");
        Toast.makeText(this, "Governance Policy Synchronized!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
