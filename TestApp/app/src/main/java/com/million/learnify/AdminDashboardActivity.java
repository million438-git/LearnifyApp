package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

/**
 * AdminDashboardActivity - Fully active and functional Command Center.
 * Linked to all internal administration and student management portals.
 * Layout structure matches the Student Dashboard but without Bottom Navigation.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private DatabaseHelper dbHelper;
    private String currentUsername;
    private ImageView adminProfileImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "admin");

        // 1. Initialize Toolbar and Drawer Navigation
        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawerLayout = findViewById(R.id.adminDrawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.neon_cyan));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize Profile Image in Toolbar
        adminProfileImg = findViewById(R.id.adminProfileImg);
        adminProfileImg.setOnClickListener(v -> {
            // Updated to open the specific Admin Profile Page
            startActivity(new Intent(AdminDashboardActivity.this, AdminProfileActivity.class));
        });

        // 2. Activate Navigation
        setupNavigation();

        // 3. Activate Dashboard Actions
        setupDashboardActions();

        // 4. Load Profile Data
        loadProfileData();
    }

    private void loadProfileData() {
        // Load for Toolbar
        String imageUriStr = dbHelper.getUserProfileImage(currentUsername);
        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            try {
                adminProfileImg.setImageURI(Uri.parse(imageUriStr));
                adminProfileImg.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                adminProfileImg.setImageResource(R.drawable.ic_user_person);
            }
        }

        // Load for Navigation Header
        NavigationView navigationView = findViewById(R.id.adminNavView);
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            ImageView ivUserHeader = headerView.findViewById(R.id.ivUserHeader);
            TextView tvUserNameHeader = headerView.findViewById(R.id.tvUserNameHeader);
            TextView tvUserEmailHeader = headerView.findViewById(R.id.tvUserEmailHeader);

            Cursor cursor = dbHelper.getUserDetails(currentUsername);
            if (cursor != null && cursor.moveToFirst()) {
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String headerImgUriStr = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));

                tvUserNameHeader.setText(fullName);
                tvUserEmailHeader.setText(email);

                if (headerImgUriStr != null && !headerImgUriStr.isEmpty()) {
                    try {
                        ivUserHeader.setImageURI(Uri.parse(headerImgUriStr));
                        ivUserHeader.setPadding(0, 0, 0, 0);
                    } catch (Exception e) {
                        ivUserHeader.setImageResource(android.R.drawable.ic_menu_myplaces);
                    }
                }
                cursor.close();
            }

            headerView.setOnClickListener(v -> {
                // Updated to open the specific Admin Profile Page
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfileActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }
    }

    private void setupNavigation() {
        NavigationView navigationView = findViewById(R.id.adminNavView);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                handleNavigation(item.getItemId());
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void handleNavigation(int id) {
        if (id == R.id.admin_nav_dashboard) {
            Toast.makeText(this, "Admin Dashboard Refreshing...", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.admin_nav_users) {
            startActivity(new Intent(this, AdminUserManagementActivity.class));
        } else if (id == R.id.admin_nav_content) {
            startActivity(new Intent(this, AdminContentManagementActivity.class));
        } else if (id == R.id.admin_nav_camera_lab) {
            startActivity(new Intent(this, AdminCameraLabActivity.class));
        } else if (id == R.id.admin_nav_reports) {
            startActivity(new Intent(this, AdminReportsActivity.class));
        } else if (id == R.id.admin_nav_settings) {
            startActivity(new Intent(this, AdminSystemSettingsActivity.class));
        } else if (id == R.id.admin_nav_logout) {
            onLogoutClick(null);
        }
    }

    private void setupDashboardActions() {
        // --- 1. STATS CARDS ---
        findViewById(R.id.cardTotalStudents).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminUserManagementActivity.class)));
            
        findViewById(R.id.cardActiveStudents).setOnClickListener(v -> 
            startActivity(new Intent(this, DashboardActivity.class)));
            
        findViewById(R.id.cardTeachers).setOnClickListener(v ->
            Toast.makeText(this, "Opening Staff Directory...", Toast.LENGTH_SHORT).show());

        findViewById(R.id.cardTotalLabs).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminContentManagementActivity.class)));

        findViewById(R.id.cardCompletedLabs).setOnClickListener(v ->
            startActivity(new Intent(this, AdminReportsActivity.class)));

        findViewById(R.id.cardPendingLabs).setOnClickListener(v ->
            startActivity(new Intent(this, AdminContentManagementActivity.class)));

        findViewById(R.id.cardSystemHealth).setOnClickListener(v ->
            startActivity(new Intent(this, AdminSystemSettingsActivity.class)));

        // --- 2. COMMAND CENTER BUTTONS ---
        findViewById(R.id.btnCreateLab).setOnClickListener(this::onCreateLabClick);
        findViewById(R.id.btnAddUser).setOnClickListener(this::onAddUserClick);
        findViewById(R.id.btnUpload).setOnClickListener(this::onUploadClick);
        findViewById(R.id.btnSimulate).setOnClickListener(this::onSimulateClick);
        findViewById(R.id.btnReports).setOnClickListener(this::onReportsClick);
        findViewById(R.id.btnSettings).setOnClickListener(this::onSettingsClick);

        // --- 3. AI INSIGHTS ---
        findViewById(R.id.panelIntelligence).setOnClickListener(v -> 
            startActivity(new Intent(this, AdminReportsActivity.class)));
    }

    // Command Center Job Implementations
    public void onCreateLabClick(View v) {
        Toast.makeText(this, "Initializing Virtual Lab Portal...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ChemistryLabActivity.class));
    }

    public void onAddUserClick(View v) {
        startActivity(new Intent(this, AdminRegisterActivity.class));
    }

    public void onUploadClick(View v) {
        startActivity(new Intent(this, AdminContentManagementActivity.class));
    }

    public void onSimulateClick(View v) {
        Toast.makeText(this, "Running Global System Simulation...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, PhysicsSimulationActivity.class));
    }

    public void onReportsClick(View v) {
        startActivity(new Intent(this, AdminReportsActivity.class));
    }

    public void onSettingsClick(View v) {
        startActivity(new Intent(this, AdminSystemSettingsActivity.class));
    }

    public void onLogoutClick(View v) {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
