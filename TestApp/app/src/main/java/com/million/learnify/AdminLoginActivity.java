package com.million.learnify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminLoginActivity - Professional Portal with Secure Persistence.
 * Refactored to use DataRepository for centralized security.
 */
public class AdminLoginActivity extends BaseActivity {

    private static final String TAG = "AdminLoginActivity";
    private TextInputEditText etAdminUser, etAdminPassword;
    private MaterialButton btnAdminLogin;
    private CircularProgressIndicator adminLoginProgress;
    private TextView tvAdminRegisterLink;
    private ImageView ivAdminLoginProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_admin_login);
            initViews();
            loadAdminProfileImage();
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
        ivAdminLoginProfile = findViewById(R.id.ivAdminLoginProfile);

        if (btnAdminLogin != null) {
            btnAdminLogin.setOnClickListener(v -> handleLogin());
        }

        if (tvAdminRegisterLink != null) {
            tvAdminRegisterLink.setOnClickListener(v -> startActivity(new Intent(this, AdminRegisterActivity.class)));
        }
    }

    private void loadAdminProfileImage() {
        if (ivAdminLoginProfile != null) {
            String imageUriStr = repository.getUserProfileImage("admin");
            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                try {
                    ivAdminLoginProfile.setImageURI(Uri.parse(imageUriStr));
                    ivAdminLoginProfile.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    ivAdminLoginProfile.setImageResource(R.drawable.ic_app_logo);
                }
            }
        }
    }

    private void handleLogin() {
        String user = etAdminUser.getText().toString().trim();
        String pass = etAdminPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter admin credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (repository.login(user, pass, "ADMIN")) {
                onLoginSuccess(user);
            } else {
                setLoading(false);
                Toast.makeText(this, "Unauthorized access attempt blocked.", Toast.LENGTH_SHORT).show();
                repository.logSystemAction(user, "FAILED_LOGIN_ADMIN", "Failed login attempt from: " + user);
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
        securePrefs.saveString("username", user);
        securePrefs.saveString("full_name", "Administrator");
        securePrefs.saveString("user_type", "ADMIN");
        securePrefs.saveBoolean("isLoggedIn", true);

        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
            .putString("username", user)
            .putString("full_name", "Administrator")
            .putString("user_type", "ADMIN")
            .putBoolean("isLoggedIn", true)
            .apply();

        repository.logSystemAction(user, "LOGIN_SUCCESS_ADMIN", "Admin logged in successfully");

        Toast.makeText(this, "Admin access granted.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AdminDashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
