package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

/**
 * AdminDashboardActivity - Professional Administrator Control Center.
 * Synchronized for Student Payment Verifications and System Governance.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupToolbarAndDrawer();
        setupNavigation();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.adminDrawerLayout);
        navigationView = findViewById(R.id.adminNavigationView);
    }

    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_nav_dashboard) {
                // Already here
            } else if (id == R.id.admin_nav_users) {
                startActivity(new Intent(this, AdminUserManagementActivity.class));
            } else if (id == R.id.admin_nav_content) {
                startActivity(new Intent(this, AdminContentManagementActivity.class));
            } else if (id == R.id.admin_nav_reports) {
                // FINANCIAL REPORTS -> PAYMENT VERIFICATION
                startActivity(new Intent(this, AdminPaymentVerificationActivity.class));
            } else if (id == R.id.admin_nav_settings) {
                startActivity(new Intent(this, AdminSystemSettingsActivity.class));
            } else if (id == R.id.admin_nav_logout) {
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // 👥 BUTTON 1: MANAGE USERS
        View btnUsers = findViewById(R.id.btnManageUsers);
        if (btnUsers != null) btnUsers.setOnClickListener(v -> 
            startActivity(new Intent(this, AdminUserManagementActivity.class)));
        
        // 📚 BUTTON 2: MANAGE CONTENT
        View btnContent = findViewById(R.id.btnManageContent);
        if (btnContent != null) btnContent.setOnClickListener(v -> 
            startActivity(new Intent(this, AdminContentManagementActivity.class)));
            
        // 💸 BUTTON 3: VERIFY PAYMENTS (Mapped to System Settings)
        View btnPayments = findViewById(R.id.btnSystemSettings);
        if (btnPayments != null) btnPayments.setOnClickListener(v -> 
            startActivity(new Intent(this, AdminPaymentVerificationActivity.class)));
    }

    private void logout() {
        Toast.makeText(this, "Admin Logged Out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, AdminLoginActivity.class);
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
