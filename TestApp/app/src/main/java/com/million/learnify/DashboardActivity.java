package com.million.learnify;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

/**
 * DashboardActivity - Professional Student Workspace.
 * Optimized: Removed Grades, Attendance, and dynamic Resources from dashboard.
 */
public class DashboardActivity extends BaseActivity implements VoiceStudyManager.VoiceCallback {

    private static final int MIC_PERMISSION_CODE = 200;
    private DatabaseHelper dbHelper;
    private TextView tvWelcomeTitle, tvNotificationBadgeCount;
    private DrawerLayout drawerLayout;
    private String currentUsername;
    private VoiceStudyManager voiceStudyManager;
    private View rlNotificationContainer;
    private boolean isImpersonating = false;

    private final BroadcastReceiver notificationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotificationUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_dashboard);
        } catch (Exception e) {
            Log.e("Dashboard", "Inflation Error", e);
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "");
        isImpersonating = pref.getBoolean("is_impersonating", false);

        if (currentUsername.isEmpty()) {
            logout();
            return;
        }

        try {
            voiceStudyManager = new VoiceStudyManager(this, this);
        } catch (Exception e) { e.printStackTrace(); }

        initViews();
        setupNavigation();
        updateHeaderAndWelcome();
        
        checkMicrophonePermission();
        updateNotificationUI();

        if (isImpersonating) {
            showSupportModeWarning();
        }

        try {
            IntentFilter filter = new IntentFilter("com.million.learnify.UPDATE_NOTIFICATION_UI");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(notificationUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                ContextCompat.registerReceiver(this, notificationUpdateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            }
        } catch (Exception e) { }
    }

    private void showSupportModeWarning() {
        Toast.makeText(this, "⚠️ SUPPORT MODE ACTIVE: Logged in as " + currentUsername, Toast.LENGTH_LONG).show();
    }

    private void updateHeaderAndWelcome() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String fullName = pref.getString("full_name", currentUsername);
        
        if (tvWelcomeTitle != null) {
            String welcome = "Welcome " + fullName + "!";
            if (isImpersonating) welcome += "\n(Admin Support Mode)";
            tvWelcomeTitle.setText(welcome);
        }

        NavigationView navView = findViewById(R.id.navigationView);
        if (navView != null && navView.getHeaderCount() > 0) {
            View header = navView.getHeaderView(0);
            ImageView ivHeaderProfile = header.findViewById(R.id.ivUserHeader);
            TextView tvHeaderName = header.findViewById(R.id.tvUserNameHeader);
            TextView tvHeaderEmail = header.findViewById(R.id.tvUserEmailHeader);

            if (isImpersonating) {
                tvHeaderName.setText(fullName);
                tvHeaderEmail.setText("Impersonating Account");
                ivHeaderProfile.setImageResource(android.R.drawable.ic_menu_view);
                navView.getMenu().findItem(R.id.nav_logout).setTitle("Exit Support Mode");
            } else {
                Cursor cursor = dbHelper.getUserDetails(currentUsername);
                if (cursor != null && cursor.moveToFirst()) {
                    tvHeaderName.setText(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
                    tvHeaderEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                    String img = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));
                    if (ivHeaderProfile != null && img != null && !img.isEmpty()) {
                        try { ivHeaderProfile.setImageURI(Uri.parse(img)); } catch (Exception e) {}
                    }
                    cursor.close();
                }
                navView.getMenu().findItem(R.id.nav_logout).setTitle("Logout");
            }
        }
    }

    private void initViews() {
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        tvNotificationBadgeCount = findViewById(R.id.tvNotificationBadgeCount);
        rlNotificationContainer = findViewById(R.id.rlNotificationContainer);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        if (rlNotificationContainer != null) {
            rlNotificationContainer.setOnClickListener(v -> startActivity(new Intent(this, NotificationListActivity.class)));
        }

        safeSetClick(R.id.cardTimetable, v -> startActivity(new Intent(this, ClassScheduleActivity.class)));
        safeSetClick(R.id.cardCourses, v -> startActivity(new Intent(this, SubjectActivity.class)));
        safeSetClick(R.id.cardAssignments, v -> startActivity(new Intent(this, TasksActivity.class)));
        safeSetClick(R.id.cardDailyChallenge, v -> startActivity(new Intent(this, DailyChallengeActivity.class)));
        safeSetClick(R.id.cardCameraLab, v -> startActivity(new Intent(this, CameraLabActivity.class)));
        safeSetClick(R.id.cardVisualizers, v -> startActivity(new Intent(this, VisualizerActivity.class)));
        safeSetClick(R.id.cardAI, v -> startActivity(new Intent(this, AIChatActivity.class)));
        safeSetClick(R.id.cardPremium, v -> startActivity(new Intent(this, PaymentActivity.class)));
        safeSetClick(R.id.cardAnnouncements, v -> startActivity(new Intent(this, AnnouncementsActivity.class)));

        View ivVoice = findViewById(R.id.ivVoiceAssistant);
        if (ivVoice != null) ivVoice.setOnClickListener(v -> {
            if (voiceStudyManager != null) voiceStudyManager.startListening();
        });
    }

    private void safeSetClick(int id, View.OnClickListener listener) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(listener);
    }

    private void updateNotificationUI() {
        if (tvNotificationBadgeCount == null) return;
        int unreadCount = dbHelper.getUnreadNotificationCount(currentUsername);
        if (unreadCount > 0) {
            tvNotificationBadgeCount.setText(String.valueOf(unreadCount));
            tvNotificationBadgeCount.setVisibility(View.VISIBLE);
        } else {
            tvNotificationBadgeCount.setVisibility(View.GONE);
        }
    }

    private void setupNavigation() {
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                handleNavigation(item.getItemId());
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                handleNavigation(item.getItemId());
                return true;
            });
        }
    }

    private void handleNavigation(int id) {
        if (id == R.id.nav_dashboard || id == R.id.menu_home) {
             // Home
        } else if (id == R.id.nav_visualizer || id == R.id.menu_visualizer) {
            startActivity(new Intent(this, VisualizerActivity.class));
        } else if (id == R.id.nav_courses || id == R.id.menu_courses) {
            startActivity(new Intent(this, SubjectActivity.class));
        } else if (id == R.id.nav_assignments || id == R.id.menu_assignments) {
            startActivity(new Intent(this, TasksActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_settings || id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }
    }

    private void logout() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean wasImpersonating = pref.getBoolean("is_impersonating", false);
        pref.edit().clear().apply();
        
        if (wasImpersonating) {
            Toast.makeText(this, "Exited Support Mode", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }

    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_CODE);
        }
    }

    @Override public void onCommandRecognized(String text) {}
    @Override public void onActionRequired(String action, String page, String extra) {
        if ("navigate".equals(action)) {
            try {
                Class<?> targetClass = Class.forName("com.million.learnify." + page);
                Intent intent = new Intent(this, targetClass);
                if (extra != null) intent.putExtra("SUBJECT_NAME", extra);
                startActivity(intent);
            } catch (Exception e) { }
        }
    }

    @Override protected void onResume() {
        super.onResume();
        updateNotificationUI();
        updateHeaderAndWelcome();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(notificationUpdateReceiver); } catch (Exception e) {}
    }
}
