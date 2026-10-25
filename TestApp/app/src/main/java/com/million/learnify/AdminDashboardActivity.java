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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AdminDashboardActivity - Professional Command Center.
 * Updated to display real-time statistics and fully active navigation.
 */
public class AdminDashboardActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private String currentUsername;
    private ImageView adminProfileImg, ivAdminNotification;
    
    // Stats & Hero TextViews
    private TextView tvTotalStudents, tvActiveStudents, tvTotalTeachers, tvTotalLabs, tvCompletedLabs, tvPendingLabs, tvSystemHealth;
    private TextView tvAdminWelcome, tvAdminDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_dashboard);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "admin");

        initViews();
        Toolbar adminToolbar = findViewById(R.id.adminToolbar);
        if (adminToolbar != null) {
            setSupportActionBar(adminToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        View llToolbarBack = findViewById(R.id.llToolbarBack);
        if (llToolbarBack != null) {
            llToolbarBack.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
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
        tvAdminWelcome = findViewById(R.id.tvAdminWelcome);
        tvAdminDate = findViewById(R.id.tvAdminDate);

        adminProfileImg.setOnClickListener(v -> {
            safeStartActivity(new Intent(AdminDashboardActivity.this, AdminProfileActivity.class));
        });

        if (ivAdminNotification != null) {
            ivAdminNotification.setOnClickListener(v -> {
                safeStartActivity(new Intent(AdminDashboardActivity.this, NotificationListActivity.class));
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

        NumberFormat nf = NumberFormat.getIntegerInstance();

        // Updating UI with Live Counts
        if (tvTotalStudents != null) tvTotalStudents.setText(nf.format(totalStudents));
        if (tvActiveStudents != null) tvActiveStudents.setText(nf.format(activeStudents));
        if (tvTotalTeachers != null) tvTotalTeachers.setText(nf.format(totalTeachers));
        if (tvTotalLabs != null) tvTotalLabs.setText(nf.format(totalLabs));
        if (tvCompletedLabs != null) tvCompletedLabs.setText(nf.format(completedLabs));
        if (tvPendingLabs != null) tvPendingLabs.setText(nf.format(pendingTasks));
        
        if (tvSystemHealth != null) {
            tvSystemHealth.setText("All Systems Operational");
            tvSystemHealth.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.admin_green));
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

        String displayName = null;

        NavigationView navigationView = findViewById(R.id.adminNavView);
        View headerView = (navigationView != null) ? navigationView.getHeaderView(0) : null;

        Cursor cursor = repository.getUserDetails(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String headerImgUriStr = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));

            if (fullName != null && !fullName.trim().isEmpty()) {
                displayName = fullName.trim();
            }

            if (headerView != null) {
                TextView tvUserNameHeader = headerView.findViewById(R.id.tvUserNameHeader);
                TextView tvUserEmailHeader = headerView.findViewById(R.id.tvUserEmailHeader);
                ImageView ivUserHeader = headerView.findViewById(R.id.ivUserHeader);

                if (tvUserNameHeader != null) tvUserNameHeader.setText(fullName != null ? fullName : currentUsername);
                if (tvUserEmailHeader != null) tvUserEmailHeader.setText(email);

                if (headerImgUriStr != null && !headerImgUriStr.isEmpty() && ivUserHeader != null) {
                    try {
                        ivUserHeader.setImageURI(Uri.parse(headerImgUriStr));
                        ivUserHeader.setPadding(0, 0, 0, 0);
                    } catch (Exception e) {
                        ivUserHeader.setImageResource(R.drawable.ic_user_person);
                    }
                }
            }
            cursor.close();
        }

        if (headerView != null) {
            headerView.setOnClickListener(v -> {
                safeStartActivity(new Intent(AdminDashboardActivity.this, AdminProfileActivity.class));
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            });
        }

        // Set dynamic welcome text without raw placeholders
        if (displayName == null || displayName.trim().isEmpty()) {
            if (currentUsername != null && !currentUsername.trim().isEmpty() && !currentUsername.equalsIgnoreCase("admin")) {
                displayName = currentUsername.trim();
            } else {
                displayName = "Administrator";
            }
        }

        if (tvAdminWelcome != null) {
            tvAdminWelcome.setText("Welcome back, " + displayName);
        }

        if (tvAdminDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
                tvAdminDate.setText(sdf.format(new Date()));
            } catch (Exception ignored) {}
        }
    }

    private void setupNavigation() {
        NavigationView navigationView = findViewById(R.id.adminNavView);
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.admin_nav_dashboard);
            navigationView.setNavigationItemSelectedListener(item -> {
                handleNavigation(item.getItemId());
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private long lastActionClickTime = 0;
    public static final long DEBOUNCE_INTERVAL_MS = 600;

    public static boolean checkCanPerformAction(long currentTime, long lastActionTime) {
        return (currentTime - lastActionTime) >= DEBOUNCE_INTERVAL_MS;
    }

    boolean canPerformAction() {
        return canPerformAction(System.currentTimeMillis());
    }

    boolean canPerformAction(long currentTime) {
        if (!checkCanPerformAction(currentTime, lastActionClickTime)) {
            return false;
        }
        lastActionClickTime = currentTime;
        return true;
    }

    private void safeStartActivity(Intent intent) {
        if (canPerformAction()) {
            startActivity(intent);
        }
    }

    private void setSafeClickListener(int viewId, Class<?> targetClass) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> safeStartActivity(new Intent(this, targetClass)));
        }
    }

    private void handleNavigation(int id) {
        if (id == R.id.admin_nav_dashboard) {
            Toast.makeText(this, "Refreshing Dashboard...", Toast.LENGTH_SHORT).show();
            updateDashboardStats();
            loadProfileData();
        } else if (id == R.id.admin_nav_users) {
            safeStartActivity(new Intent(this, AdminUserManagementActivity.class));
        } else if (id == R.id.admin_nav_issue_cert) {
            safeStartActivity(new Intent(this, AdminIssueCertificateActivity.class));
        } else if (id == R.id.admin_nav_content) {
            safeStartActivity(new Intent(this, AdminContentManagementActivity.class));
        } else if (id == R.id.admin_nav_camera_lab) {
            safeStartActivity(new Intent(this, AdminCameraLabActivity.class));
        } else if (id == R.id.admin_nav_reports) {
            safeStartActivity(new Intent(this, AdminReportsActivity.class));
        } else if (id == R.id.admin_nav_settings) {
            safeStartActivity(new Intent(this, AdminSystemSettingsActivity.class));
        } else if (id == R.id.admin_nav_add_question) {
            safeStartActivity(new Intent(this, AdminQuestionActivity.class));
        } else if (id == R.id.admin_nav_payment_verification) {
            safeStartActivity(new Intent(this, AdminPaymentVerificationActivity.class));
        } else if (id == R.id.admin_nav_support_management) {
            safeStartActivity(new Intent(this, AdminSupportManagementActivity.class));
        } else if (id == R.id.admin_nav_logout) {
            onLogoutClick(null);
        }
    }

    private void setupDashboardActions() {
        setSafeClickListener(R.id.cardTotalStudents, AdminUserManagementActivity.class);
        setSafeClickListener(R.id.cardActiveStudents, AdminUserManagementActivity.class);
        
        View cardTeachers = findViewById(R.id.cardTeachers);
        if (cardTeachers != null) {
            cardTeachers.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminUserManagementActivity.class);
                intent.putExtra("FILTER_ROLE", "TEACHER");
                safeStartActivity(intent);
            });
        }
        
        setSafeClickListener(R.id.cardTotalLabs, AdminContentManagementActivity.class);
        setSafeClickListener(R.id.cardCompletedLabs, AdminReportsActivity.class);
        setSafeClickListener(R.id.cardPendingLabs, AdminContentManagementActivity.class);
        setSafeClickListener(R.id.cardSystemHealth, AdminSystemSettingsActivity.class);

        // Governance & Compliance Cards
        setSafeClickListener(R.id.cardIssueCert, AdminIssueCertificateActivity.class);
        setSafeClickListener(R.id.cardPaymentVerification, AdminPaymentVerificationActivity.class);
        setSafeClickListener(R.id.cardSupportCenter, AdminSupportManagementActivity.class);

        // Quick Action Grid
        setSafeClickListener(R.id.btnIssueCertificate, AdminIssueCertificateActivity.class);
        setSafeClickListener(R.id.btnCreateLab, AdminQuestionActivity.class);
        setSafeClickListener(R.id.btnAddUser, AdminRegisterActivity.class);
        setSafeClickListener(R.id.btnUpload, AdminContentManagementActivity.class);
        setSafeClickListener(R.id.btnSimulate, PhysicsSimulationActivity.class);
        setSafeClickListener(R.id.btnReports, AdminReportsActivity.class);
        setSafeClickListener(R.id.btnSettings, AdminSystemSettingsActivity.class);
    }

    public void onLogoutClick(View v) {
        FirebaseSessionManager.signOut(this, FirebaseAuthSupport.getConfiguredAuth(this));
        repository.logSystemAction(currentUsername, "LOGOUT_ADMIN", "Admin logged out");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastActionClickTime = 0;
        NavigationView navigationView = findViewById(R.id.adminNavView);
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.admin_nav_dashboard);
        }
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
