package com.million.learnify;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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

import java.util.List;

/**
 * DashboardActivity - Fixed navigation and inflation crashes.
 * Now extends BaseActivity for consistent Appearance settings.
 */
public class DashboardActivity extends BaseActivity implements VoiceStudyManager.VoiceCallback {

    private static final int MIC_PERMISSION_CODE = 200;
    private DatabaseHelper dbHelper;
    private TextView tvWelcomeTitle, tvNotificationBadgeCount;
    private LinearLayout llMaterialsContainer;
    private DrawerLayout drawerLayout;
    private String currentUsername, userGrade;
    private VoiceStudyManager voiceStudyManager;
    private View rlNotificationContainer;

    private final BroadcastReceiver notificationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotificationUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // BaseActivity handles Theme and Appearance settings
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_dashboard);
        } catch (Exception e) {
            Log.e("Dashboard", "Inflation Error", e);
            // Fallback or alert user
            Toast.makeText(this, "UI Load Error. Please restart.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "");
        userGrade = pref.getString("grade", "10");

        if (currentUsername.isEmpty()) {
            logout();
            return;
        }

        try {
            voiceStudyManager = new VoiceStudyManager(this, this);
        } catch (Exception e) {
            Log.e("Dashboard", "Voice manager init failed", e);
        }

        initViews();
        setupNavigation();
        loadMaterials();
        
        checkMicrophonePermission();
        updateNotificationUI();

        // Register receiver for real-time notification updates
        try {
            IntentFilter filter = new IntentFilter("com.million.learnify.UPDATE_NOTIFICATION_UI");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(notificationUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(notificationUpdateReceiver, filter);
            }
        } catch (Exception e) {
            Log.e("Dashboard", "Receiver registration failed", e);
        }
        
        // Trigger welcome notification on first entry
        sendWelcomeNotification();

        // Initial motivation scheduling
        NotificationHelper.scheduleGenericNotification(this, "motivation", 500, 1);
    }

    private void sendWelcomeNotification() {
        Intent welcomeIntent = new Intent(this, NotificationReceiver.class);
        welcomeIntent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        welcomeIntent.putExtra("type", "welcome");
        sendBroadcast(welcomeIntent);
    }

    private void initViews() {
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        llMaterialsContainer = findViewById(R.id.llMaterialsContainer);
        tvNotificationBadgeCount = findViewById(R.id.tvNotificationBadgeCount);
        rlNotificationContainer = findViewById(R.id.rlNotificationContainer);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        if (tvWelcomeTitle != null) {
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            String fullName = pref.getString("full_name", currentUsername);
            tvWelcomeTitle.setText("Welcome " + fullName + "!");
        }

        if (rlNotificationContainer != null) {
            rlNotificationContainer.setOnClickListener(v -> {
                startActivity(new Intent(this, NotificationListActivity.class));
            });
        }

        // Dashboard Feature Navigation
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
            if (rlNotificationContainer != null) {
                rlNotificationContainer.setBackgroundResource(R.drawable.bg_circle_light);
            }
        } else {
            tvNotificationBadgeCount.setVisibility(View.GONE);
            if (rlNotificationContainer != null) {
                rlNotificationContainer.setBackground(null);
            }
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

    private void loadMaterials() {
        if (llMaterialsContainer == null) return;
        llMaterialsContainer.removeAllViews();
        try {
            List<MaterialModel> materials = dbHelper.getMaterialsForGrade(userGrade);
            if (materials != null) {
                for (MaterialModel m : materials) {
                    View v = LayoutInflater.from(this).inflate(R.layout.item_material, llMaterialsContainer, false);
                    TextView title = v.findViewById(R.id.tvMaterialTitle);
                    if (title != null) title.setText(m.getTitle());
                    llMaterialsContainer.addView(v);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
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
        } else if (id == R.id.nav_announcements) {
            startActivity(new Intent(this, AnnouncementsActivity.class));
        } else if (id == R.id.nav_timetable) {
            startActivity(new Intent(this, ClassScheduleActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }
    }

    private void logout() {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_CODE);
        }
    }

    @Override public void onCommandRecognized(String text) {
        Toast.makeText(this, "I heard: " + text, Toast.LENGTH_SHORT).show();
    }

    @Override public void onActionRequired(String action, String page, String extra) {
        if ("navigate".equals(action)) {
            try {
                Class<?> targetClass = Class.forName("com.million.learnify." + page);
                Intent intent = new Intent(this, targetClass);
                if (extra != null && !extra.isEmpty()) {
                    intent.putExtra("SUBJECT_NAME", extra);
                }
                startActivity(intent);
            } catch (Exception e) {
                Log.e("Dashboard", "Voice Navigation Error: " + page, e);
            }
        } else if ("summarize".equals(action)) {
            Toast.makeText(this, "Summary action triggered via voice.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceStudyManager != null) {
            voiceStudyManager.shutdown();
        }
        try {
            unregisterReceiver(notificationUpdateReceiver);
        } catch (Exception e) {}
    }
}
