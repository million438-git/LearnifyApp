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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

/**
 * AdminDashboardActivity - Professional Command Center.
 * Updated to display real-time statistics and fully active navigation.
 */
public class AdminDashboardActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private String currentUsername;
    private ImageView adminProfileImg, ivAdminNotification;
    
    // Stats TextViews
    private TextView tvTotalStudents, tvActiveStudents, tvTotalTeachers, tvTotalLabs, tvCompletedLabs, tvPendingLabs, tvSystemHealth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_dashboard);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "admin");

        initViews();
        setupPortalToolbar(findViewById(R.id.adminToolbar), "Admin Dashboard");
        setupDrawerButton(drawerLayout);
        
        setupNavigation();
        setupDashboardActions();
        loadProfileData();
        updateDashboardStats();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.adminDrawerLayout);
        adminProfileImg = findViewById(R.id.adminProfileImg);
        ivAdminNotification = findViewById(R.id.ivAdminNotification);
        
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvActiveStudents = findViewById(R.id.tvActiveStudents);
        tvTotalTeachers = findViewById(R.id.tvTotalTeachers);
        tvTotalLabs = findViewById(R.id.tvTotalLabs);
        tvCompletedLabs = findViewById(R.id.tvCompletedLabs);
        tvPendingLabs = findViewById(R.id.tvPendingLabs);
        tvSystemHealth = findViewById(R.id.tvSystemHealth);

        adminProfileImg.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminProfileActivity.class));
        });

        if (ivAdminNotification != null) {
            ivAdminNotification.setOnClickListener(v -> {
                startActivity(new Intent(AdminDashboardActivity.this, NotificationListActivity.class));
            });
        }
    }

    private void updateDashboardStats() {
        // Fetching Real Data from Repository
        int totalStudents = repository.getLocalDb().getTableCount("users", "user_type=?", new String[]{"STUDENT"});
        int totalTeachers = repository.getLocalDb().getTableCount("users", "user_type=?", new String[]{"TEACHER"});
        int activeStudents = repository.getLocalDb().getTableCount("users", "user_type=? AND status=?", new String[]{"STUDENT", "Active"});
        int totalLabs = repository.getLocalDb().getTableCount("materials", "type=?", new String[]{"LAB"});
        int completedLabs = repository.getLocalDb().getTableCount("lab_results", null, null);
        int pendingTasks = repository.getLocalDb().getTableCount("tasks", "status=?", new String[]{"Pending"});

        // Updating UI with Live Counts
        if (tvTotalStudents != null) tvTotalStudents.setText(totalStudents + " Active Records");
        if (tvActiveStudents != null) tvActiveStudents.setText(activeStudents + " Online Now");
        if (tvTotalTeachers != null) tvTotalTeachers.setText(totalTeachers + " Staff Members");
        if (tvTotalLabs != null) tvTotalLabs.setText(totalLabs + " Virtual Labs");
        if (tvCompletedLabs != null) tvCompletedLabs.setText(completedLabs + " Submissions");
        if (tvPendingLabs != null) tvPendingLabs.setText(pendingTasks + " Action Required");
        
        if (tvSystemHealth != null) {
            tvSystemHealth.setText("All Systems Operational");
            tvSystemHealth.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private void loadProfileData() {
        String imageUriStr = repository.getUserProfileImage(currentUsername);
        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            try {
                adminProfileImg.setImageURI(Uri.parse(imageUriStr));
                adminProfileImg.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                adminProfileImg.setImageResource(R.drawable.ic_user_person);
            }
        }

        NavigationView navigationView = findViewById(R.id.adminNavView);
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            ImageView ivUserHeader = headerView.findViewById(R.id.ivUserHeader);
            TextView tvUserNameHeader = headerView.findViewById(R.id.tvUserNameHeader);
            TextView tvUserEmailHeader = headerView.findViewById(R.id.tvUserEmailHeader);

            Cursor cursor = repository.getUserDetails(currentUsername);
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
            Toast.makeText(this, "Refreshing Dashboard...", Toast.LENGTH_SHORT).show();
            updateDashboardStats();
            loadProfileData();
        } else if (id == R.id.admin_nav_users) {
            startActivity(new Intent(this, AdminUserManagementActivity.class));
        } else if (id == R.id.admin_nav_issue_cert) {
            startActivity(new Intent(this, AdminIssueCertificateActivity.class));
        } else if (id == R.id.admin_nav_content) {
            startActivity(new Intent(this, AdminContentManagementActivity.class));
        } else if (id == R.id.admin_nav_camera_lab) {
            startActivity(new Intent(this, AdminCameraLabActivity.class));
        } else if (id == R.id.admin_nav_reports) {
            startActivity(new Intent(this, AdminReportsActivity.class));
        } else if (id == R.id.admin_nav_settings) {
            startActivity(new Intent(this, AdminSystemSettingsActivity.class));
        } else if (id == R.id.admin_nav_add_question) {
            startActivity(new Intent(this, AdminQuestionActivity.class));
        } else if (id == R.id.admin_nav_logout) {
            onLogoutClick(null);
        }
    }

    private void setupDashboardActions() {
        findViewById(R.id.cardTotalStudents).setOnClickListener(v -> startActivity(new Intent(this, AdminUserManagementActivity.class)));
        findViewById(R.id.cardActiveStudents).setOnClickListener(v -> startActivity(new Intent(this, AdminUserManagementActivity.class)));
        findViewById(R.id.cardTeachers).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminUserManagementActivity.class);
            intent.putExtra("FILTER_ROLE", "TEACHER");
            startActivity(intent);
        });
        findViewById(R.id.cardTotalLabs).setOnClickListener(v -> startActivity(new Intent(this, AdminContentManagementActivity.class)));
        findViewById(R.id.cardCompletedLabs).setOnClickListener(v -> startActivity(new Intent(this, AdminReportsActivity.class)));
        findViewById(R.id.cardPendingLabs).setOnClickListener(v -> startActivity(new Intent(this, AdminContentManagementActivity.class)));
        findViewById(R.id.cardSystemHealth).setOnClickListener(v -> startActivity(new Intent(this, AdminSystemSettingsActivity.class)));

        View cardIssueCert = findViewById(R.id.cardIssueCert);
        if (cardIssueCert != null) {
            cardIssueCert.setOnClickListener(v -> startActivity(new Intent(this, AdminIssueCertificateActivity.class)));
        }

        findViewById(R.id.btnIssueCertificate).setOnClickListener(v -> startActivity(new Intent(this, AdminIssueCertificateActivity.class)));
        findViewById(R.id.btnCreateLab).setOnClickListener(v -> startActivity(new Intent(this, AdminQuestionActivity.class)));
        findViewById(R.id.btnAddUser).setOnClickListener(v -> startActivity(new Intent(this, AdminRegisterActivity.class)));
        findViewById(R.id.btnUpload).setOnClickListener(v -> startActivity(new Intent(this, AdminContentManagementActivity.class)));
        findViewById(R.id.btnSimulate).setOnClickListener(v -> startActivity(new Intent(this, PhysicsSimulationActivity.class)));
        findViewById(R.id.btnReports).setOnClickListener(v -> startActivity(new Intent(this, AdminReportsActivity.class)));
        findViewById(R.id.btnSettings).setOnClickListener(v -> startActivity(new Intent(this, AdminSystemSettingsActivity.class)));
    }

    public void onLogoutClick(View v) {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
        securePrefs.clearSession();
        repository.logSystemAction(currentUsername, "LOGOUT_ADMIN", "Admin logged out");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        updateDashboardStats();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
