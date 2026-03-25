package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminLoginActivity - With Full Login Flow Motion Styles.
 */
public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText etAdminUser, etAdminPassword;
    private MaterialButton btnAdminLogin;
    private CircularProgressIndicator adminLoginProgress;
    private TextView tvAdminRegisterLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        dbHelper = new DatabaseHelper(this);
        initViews();
        
        // Step 4: Login Page Appearance (Staggered fade-up)
        animateEntrance();
    }

    private void initViews() {
        etAdminUser = findViewById(R.id.etAdminUser);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        adminLoginProgress = findViewById(R.id.adminLoginProgress);
        tvAdminRegisterLink = findViewById(R.id.tvAdminRegisterLink);

        if (btnAdminLogin != null) {
            btnAdminLogin.setOnClickListener(v -> {
                // Step 5: Login Button Interaction (Scale down feedback)
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    handleLogin();
                }).start();
            });
        }

        if (tvAdminRegisterLink != null) {
            tvAdminRegisterLink.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminRegisterActivity.class));
            });
        }
    }

    private void animateEntrance() {
        View icon = findViewById(R.id.adminHeaderIcon);
        View title = findViewById(R.id.tvAdminTitle);
        View subtitle = findViewById(R.id.tvAdminSubtitle);
        View card = findViewById(R.id.adminLoginCard);

        long delay = 100;
        fadeUp(icon, delay);
        fadeUp(title, delay += 50);
        fadeUp(subtitle, delay += 50);
        fadeUp(card, delay += 100);
    }

    private void fadeUp(View v, long delay) {
        if (v == null) return;
        v.setAlpha(0f);
        v.setTranslationY(20f);
        v.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(delay)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();
    }

    private void handleLogin() {
        String user = etAdminUser != null ? etAdminUser.getText().toString().trim() : "";
        String pass = etAdminPassword != null ? etAdminPassword.getText().toString().trim() : "";

        if (user.isEmpty() || pass.isEmpty()) {
            showErrorFeedback();
            Toast.makeText(this, "Please enter admin credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Simulate network delay for professional feel
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dbHelper.checkUser(user, pass, "ADMIN")) {
                onLoginSuccess(user);
            } else {
                setLoading(false);
                showErrorFeedback();
                Toast.makeText(this, "Unauthorized admin access attempt blocked.", Toast.LENGTH_SHORT).show();
            }
        }, 1200);
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

    private void showErrorFeedback() {
        // Step 5: Error (Shake animation)
        View card = findViewById(R.id.adminLoginCard);
        if (card != null) {
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            card.startAnimation(shake);
        }
    }

    private void onLoginSuccess(String user) {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
            .putString("username", user)
            .putString("full_name", "Administrator")
            .putString("user_type", "ADMIN")
            .putBoolean("isLoggedIn", true)
            .apply();

        Toast.makeText(this, "Admin access granted. Welcome! ✓", Toast.LENGTH_SHORT).show();
        
        // Step 6: Transition to Home (Cross-fade)
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
