package com.example.testapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

/**
 * DashboardActivity - Professional Student Portal.
 * Fixed: Added global theme preservation.
 */
public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvWelcomeTitle;
    private LinearLayout llMaterialsContainer;
    private BottomNavigationView bottomNavigation;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView ivSmallProfile, ivNotifications;
    private String currentUsername;
    private int lastKnownCount = 0;
    private Handler notificationHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyAppTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "");

        if (currentUsername.isEmpty()) {
            logout();
            return;
        }

        initViews();
        setupToolbarAndDrawer();
        setupNavigation();
        loadUserProfileData();
        
        syncAdminMaterials();
        startRealTimeNotifications();
    }

    private void applyAppTheme() {
        SettingsService settingsService = new SettingsService(this);
        StudentSettings settings = settingsService.loadSettings();
        if ("dark".equals(settings.getThemeMode())) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncAdminMaterials();
        checkNewNotifications(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationHandler.removeCallbacksAndMessages(null);
    }

    private void initViews() {
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        llMaterialsContainer = findViewById(R.id.llMaterialsContainer);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ivSmallProfile = findViewById(R.id.ivSmallProfile);
        ivNotifications = findViewById(R.id.ivNotifications);
        
        tvWelcomeTitle.setText("Welcome Back " + currentUsername + "! 👋 ");

        // 1. COURSES 🔥
        setupOverviewCard(R.id.cardCourses, "🔥", "12", "Courses", SubjectActivity.class);
        
        // 2. TASKS 📝
        setupOverviewCard(R.id.cardAssignments, "📝", "05", "Tasks", TasksActivity.class);
        
        // 3. GPA / ENROLL 📊 (GATEWAY TO PAYMENT)
        setupOverviewCard(R.id.cardGPA, "📊", "3.8", "Enroll", PaymentActivity.class);
        
        // 4. CLASS 🗓
        setupOverviewCard(R.id.cardToday, "🗓", "04", "Class", ClassScheduleActivity.class);
        
        // 🔐 THE PREMIUM ACCESS BUTTON (Direct Link)
        View premiumCard = findViewById(R.id.cardPremium);
        if (premiumCard != null) {
            premiumCard.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, PaymentActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupOverviewCard(int id, String emoji, String value, String label, Class<?> targetActivity) {
        View card = findViewById(id);
        if (card != null) {
            TextView tvIcon = card.findViewById(R.id.tvCardIcon);
            TextView tvValue = card.findViewById(R.id.tvCardValue);
            TextView tvLabel = card.findViewById(R.id.tvCardLabel);
            
            if (tvIcon != null) tvIcon.setText(emoji);
            if (tvValue != null) tvValue.setText(value);
            if (tvLabel != null) tvLabel.setText(label);
            
            card.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, targetActivity);
                startActivity(intent);
            });
        }
    }

    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        bottomNavigation.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });
        findViewById(R.id.cardAI).setOnClickListener(v -> startActivity(new Intent(this, AIChatActivity.class)));
        ivSmallProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        
        ivNotifications.setOnClickListener(v -> {
            syncAdminMaterials();
            ivNotifications.setColorFilter(Color.WHITE);
            Toast.makeText(this, "Content updated.", Toast.LENGTH_SHORT).show();
        });
    }

    private void startRealTimeNotifications() {
        notificationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNewNotifications(true);
                notificationHandler.postDelayed(this, 15000);
            }
        }, 5000);
    }

    private void checkNewNotifications(boolean animate) {
        String grade = dbHelper.getUserGrade(currentUsername);
        List<MaterialModel> materials = dbHelper.getMaterialsForGrade(grade);
        List<TaskModel> tasks = dbHelper.getTasksForGrade(grade);
        int currentCount = materials.size() + tasks.size();
        
        if (currentCount > lastKnownCount && lastKnownCount != 0) {
            ivNotifications.setColorFilter(Color.parseColor("#F472B6"));
            if (animate) Toast.makeText(this, "🔔 New material received!", Toast.LENGTH_LONG).show();
            syncAdminMaterials();
        }
        lastKnownCount = currentCount;
    }

    private void loadUserProfileData() {
        Cursor cursor = dbHelper.getUserDetails(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(0);
            String email = cursor.getString(1);
            String imageUri = cursor.getString(2);

            View headerView = navigationView.getHeaderView(0);
            TextView tvName = headerView.findViewById(R.id.tvUserNameHeader);
            TextView tvEmail = headerView.findViewById(R.id.tvUserEmailHeader);
            ImageView ivHeader = headerView.findViewById(R.id.ivUserHeader);

            if (tvName != null) tvName.setText(fullName);
            if (tvEmail != null) tvEmail.setText(email);
            
            if (imageUri != null && !imageUri.isEmpty()) {
                Uri uri = Uri.parse(imageUri);
                if (ivHeader != null) ivHeader.setImageURI(uri);
                if (ivSmallProfile != null) ivSmallProfile.setImageURI(uri);
            }
            cursor.close();
        }
    }

    private void syncAdminMaterials() {
        if (llMaterialsContainer != null) {
            llMaterialsContainer.removeAllViews();
            String userGrade = dbHelper.getUserGrade(currentUsername);
            List<MaterialModel> materials = dbHelper.getMaterialsForGrade(userGrade);
            
            if (materials.isEmpty()) {
                addEmptyMaterialsState();
            } else {
                for (MaterialModel m : materials) {
                    addMaterialCard(m);
                }
            }
        }
    }

    private void addMaterialCard(MaterialModel m) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_material, llMaterialsContainer, false);
        ((TextView) card.findViewById(R.id.tvMaterialSubject)).setText(m.getSubject().toUpperCase());
        
        String typeIcon = "📄 "; 
        if (m.getType().toLowerCase().contains("video")) typeIcon = "🎬 ";
        else if (m.getType().toLowerCase().contains("image")) typeIcon = "🖼 ";
        
        ((TextView) card.findViewById(R.id.tvMaterialType)).setText(typeIcon + m.getType());
        ((TextView) card.findViewById(R.id.tvMaterialTitle)).setText(m.getTitle());
        ((TextView) card.findViewById(R.id.tvMaterialDescription)).setText(m.getContent());

        card.setOnClickListener(v -> {
            try {
                Uri contentUri = Uri.parse(m.getFilePath());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(contentUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Opening resource...", Toast.LENGTH_SHORT).show();
            }
        });
        llMaterialsContainer.addView(card);
    }

    private void addEmptyMaterialsState() {
        TextView tv = new TextView(this);
        tv.setText("📭 No materials for Grade " + dbHelper.getUserGrade(currentUsername));
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setPadding(0, 100, 0, 100);
        tv.setTextColor(Color.GRAY);
        llMaterialsContainer.addView(tv);
    }

    private void handleNavigation(int id) {
        if (id == R.id.nav_dashboard || id == R.id.menu_home) return;
        
        if (id == R.id.nav_courses || id == R.id.menu_courses) {
            startActivity(new Intent(this, SubjectActivity.class));
        } else if (id == R.id.nav_assignments || id == R.id.menu_assignments) {
            startActivity(new Intent(this, TasksActivity.class));
        } else if (id == R.id.nav_grades) {
            startActivity(new Intent(this, PaymentActivity.class));
        } else if (id == R.id.nav_timetable || id == R.id.cardToday) {
            startActivity(new Intent(this, ClassScheduleActivity.class));
        } else if (id == R.id.nav_attendance) {
            startActivity(new Intent(this, AttendanceActivity.class));
        } else if (id == R.id.nav_resources) {
            syncAdminMaterials();
            Toast.makeText(this, "Resources synchronized.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_messages || id == R.id.menu_messages) {
            startActivity(new Intent(this, AIChatActivity.class));
        } else if (id == R.id.nav_settings || id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }
    }

    private void logout() {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
