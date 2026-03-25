package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminLoginActivity - With normal system transitions.
 * Extends BaseActivity for consistent appearance and global settings.
 */
public class AdminLoginActivity extends BaseActivity {

    private static final String TAG = "AdminLoginActivity";
    private TextInputEditText etAdminUser, etAdminPassword;
    private MaterialButton btnAdminLogin;
    private CircularProgressIndicator adminLoginProgress;
    private TextView tvAdminRegisterLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_admin_login);
            dbHelper = new DatabaseHelper(this);
            initViews();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "UI Load Error", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        etAdminUser = findViewById(R.id.etAdminUser);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        adminLoginProgress = findViewById(R.id.adminLoginProgress);
        tvAdminRegisterLink = findViewById(R.id.tvAdminRegisterLink);

        if (btnAdminLogin != null) {
            btnAdminLogin.setOnClickListener(v -> {
                handleLogin();
            });
        }

        if (tvAdminRegisterLink != null) {
            tvAdminRegisterLink.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminRegisterActivity.class));
            });
        }
    }

    private void handleLogin() {
        String user = etAdminUser != null ? etAdminUser.getText().toString().trim() : "";
        String pass = etAdminPassword != null ? etAdminPassword.getText().toString().trim() : "";

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter admin credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dbHelper.checkUser(user, pass, "ADMIN")) {
                onLoginSuccess(user);
            } else {
                setLoading(false);
                Toast.makeText(this, "Unauthorized admin access attempt blocked.", Toast.LENGTH_SHORT).show();
            }
        }, 800);
    }

    private void setLoading(boolean loading) {
        if (btnAdminLogin != null) {
            btnAdminLogin.setText(loading ? "" : "SECURE LOGIN");
            btnAdminLogin.setEnabled(!loading);
        }
        if (adminLoginProgress != null) {
            adminLoginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void onLoginSuccess(String user) {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
            .putString("username", user)
            .putString("full_name", "Administrator")
            .putString("user_type", "ADMIN")
            .putBoolean("isLoggedIn", true)
            .apply();

        Toast.makeText(this, "Admin access granted. Welcome!", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
