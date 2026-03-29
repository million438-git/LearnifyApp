package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

/**
 * AdminDashboardActivity - Cyberpunk Styled Dashboard with Hamburger Menu.
 * Fully active and connected to system infrastructure.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // 1. Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 2. Initialize Drawer and Toggle
        drawerLayout = findViewById(R.id.adminDrawerLayout);
        
        // This connects the 3-line hamburger icon to the drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        
        // Color the hamburger icon to match theme
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.neon_cyan));
        
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupCyberMenu();
        setupHeaderActions();
    }

    private void setupCyberMenu() {
        // High-tech Menu Item Listeners (Manual layout approach as requested)
        findViewById(R.id.sideDashboard).setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing Cyber Dashboard...", Toast.LENGTH_SHORT).show();
            closeDrawer();
        });
        
        findViewById(R.id.sideUsers).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUserManagementActivity.class));
            closeDrawer();
        });
            
        findViewById(R.id.sideContent).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminContentManagementActivity.class));
            closeDrawer();
        });
            
        findViewById(R.id.sideReports).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminReportsActivity.class));
            closeDrawer();
        });
            
        findViewById(R.id.sideSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminSystemSettingsActivity.class));
            closeDrawer();
        });

        findViewById(R.id.btnLogout).setOnClickListener(this::onLogoutClick);
    }

    private void setupHeaderActions() {
        View profileImg = findViewById(R.id.adminProfileImg);
        if (profileImg != null) {
            profileImg.setOnClickListener(v -> 
                Toast.makeText(this, "Opening Admin Neural Profile...", Toast.LENGTH_SHORT).show());
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void onLogoutClick(View v) {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
