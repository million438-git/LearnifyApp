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

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

/**
 * DashboardActivity - Professional Student Workspace.
 * Features automated Daily Challenge reminders and brainstorm prompts.
 */
public class DashboardActivity extends BaseActivity implements VoiceStudyManager.VoiceCallback {

    private static final int MIC_PERMISSION_CODE = 200;
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
        
        enforceRole("STUDENT");
        
        try {
            setContentView(R.layout.activity_dashboard);
        } catch (Exception e) {
            Log.e("Dashboard", "Inflation Error", e);
            finish();
            return;
        }

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "");
        isImpersonating = pref.getBoolean("is_impersonating", false);

        if (currentUsername.isEmpty()) {
            logout();
            return;
        }

        setupPortalToolbar(findViewById(R.id.toolbar), "Student Dashboard");
        drawerLayout = findViewById(R.id.drawerLayout);
        setupDrawerButton(drawerLayout);

        try {
            voiceStudyManager = new VoiceStudyManager(this, this);
        } catch (Exception e) { e.printStackTrace(); }

        initViews();
        setupNavigation();
        updateHeaderAndWelcome();
        
        checkDailyBrainstormOnce();
        checkMicrophonePermission();
        updateNotificationUI();

        try {
            IntentFilter filter = new IntentFilter("com.million.learnify.UPDATE_NOTIFICATION_UI");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(notificationUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                ContextCompat.registerReceiver(this, notificationUpdateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            }
        } catch (Exception e) { }
    }

    private void checkDailyBrainstormOnce() {
        Calendar cal = Calendar.getInstance();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());
        
        SharedPreferences challengePrefs = getSharedPreferences("ChallengePrefs", MODE_PRIVATE);
        String lastNotifDate = challengePrefs.getString("last_notif_date_" + currentUsername, "");

        if (!today.equals(lastNotifDate)) {
            Cursor cursor = repository.getLocalDb().getChallengeStats(currentUsername);
            boolean doneToday = false;
            if (cursor.moveToFirst()) {
                String lastCompletionDate = cursor.getString(cursor.getColumnIndexOrThrow("last_completion_date"));
                if (today.equals(lastCompletionDate)) doneToday = true;
            }
            cursor.close();

            if (!doneToday) {
                DailyChallengeService.sendDailyReminder(this);
                challengePrefs.edit().putString("last_notif_date_" + currentUsername, today).apply();
            }
        }
    }

    private void updateHeaderAndWelcome() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String fullName = pref.getString("full_name", currentUsername);
        
        if (tvWelcomeTitle != null) {
            String welcome = "Welcome " + fullName + "!";
            if (isImpersonating) welcome += "\n(Admin Mode)";
            tvWelcomeTitle.setText(welcome);
        }

        NavigationView navView = findViewById(R.id.navigationView);
        if (navView != null && navView.getHeaderCount() > 0) {
            View header = navView.getHeaderView(0);
            ImageView ivHeaderProfile = header.findViewById(R.id.ivUserHeader);
            TextView tvHeaderName = header.findViewById(R.id.tvUserNameHeader);
            TextView tvHeaderEmail = header.findViewById(R.id.tvUserEmailHeader);

            Cursor cursor = repository.getUserDetails(currentUsername);
            if (cursor != null && cursor.moveToFirst()) {
                tvHeaderName.setText(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
                tvHeaderEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                String img = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));
                if (ivHeaderProfile != null && img != null && !img.isEmpty()) {
                    try { ivHeaderProfile.setImageURI(Uri.parse(img)); } catch (Exception e) {}
                }
                cursor.close();
            }
        }
    }

    private void initViews() {
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        tvNotificationBadgeCount = findViewById(R.id.tvNotificationBadgeCount);
        rlNotificationContainer = findViewById(R.id.rlNotificationContainer);

        if (rlNotificationContainer != null) {
            rlNotificationContainer.setOnClickListener(v -> startActivity(new Intent(this, NotificationListActivity.class)));
        }

        safeSetClick(R.id.cardFocusTimer, v -> showPomodoroDialog());
        safeSetClick(R.id.cardMoodTracker, v -> showMoodDialog());
        safeSetClick(R.id.cardFlashcards, v -> startActivity(new Intent(this, FlashcardActivity.class)));

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

    private void showPomodoroDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("⏳ Pomodoro Focus Timer");
        builder.setMessage("Ready to start a 25-minute deep study session?");
        builder.setPositiveButton("START SESSION", (d, w) -> {
            Toast.makeText(this, "Focus Timer Started: 25:00", Toast.LENGTH_LONG).show();
            // Start a simple handler-based countdown if needed
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void showMoodDialog() {
        String[] moods = {"Great 🌟", "Good 🙂", "Okay 😐", "Tired 😴", "Stressed 😫"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("🌈 How are you feeling today?");
        builder.setItems(moods, (d, which) -> {
            repository.getLocalDb().logMood(currentUsername, moods[which], "Recorded via Dashboard");
            Toast.makeText(this, "Mood logged! Take care of yourself.", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void safeSetClick(int id, View.OnClickListener listener) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(listener);
    }

    private void updateNotificationUI() {
        if (tvNotificationBadgeCount == null) return;
        int unreadCount = repository.getUnreadNotificationCount(currentUsername);
        if (unreadCount > 0) {
            tvNotificationBadgeCount.setText(String.valueOf(unreadCount));
            tvNotificationBadgeCount.setVisibility(View.VISIBLE);
        } else {
            tvNotificationBadgeCount.setVisibility(View.GONE);
        }
    }

    private void setupNavigation() {
        NavigationView navigationView = findViewById(R.id.navigationView);
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
        } else if (id == R.id.nav_visualizer || id == R.id.menu_visualizer) {
            startActivity(new Intent(this, VisualizerActivity.class));
        } else if (id == R.id.nav_courses || id == R.id.menu_courses) {
            startActivity(new Intent(this, SubjectActivity.class));
        } else if (id == R.id.nav_certificate) {
            startActivity(new Intent(this, CertificateActivity.class));
        } else if (id == R.id.nav_assignments || id == R.id.menu_assignments) {
            startActivity(new Intent(this, TasksActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_settings || id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_timetable) {
            startActivity(new Intent(this, ClassScheduleActivity.class));
        } else if (id == R.id.nav_announcements) {
            startActivity(new Intent(this, AnnouncementsActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }
    }

    private void logout() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        pref.edit().clear().apply();
        securePrefs.clearSession();
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
        checkDailyBrainstormOnce();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(notificationUpdateReceiver); } catch (Exception e) {}
        if (voiceStudyManager != null) voiceStudyManager.shutdown();
    }
}
