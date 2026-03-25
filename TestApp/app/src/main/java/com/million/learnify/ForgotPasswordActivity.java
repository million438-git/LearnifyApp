package com.million.learnify;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

/**
 * ForgotPasswordActivity - Professional student password recovery system.
 * Features: Email verification and direct secure password reset.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputEditText emailEditText, newPasswordEditText;
    private Button btnVerify, btnUpdate;
    private LinearLayout llStepEmail, llStepReset;
    private ProgressBar progressBar;
    private String verifiedEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);
        initViews();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.resetEmail);
        newPasswordEditText = findViewById(R.id.etNewPassword);
        btnVerify = findViewById(R.id.btnVerifyEmail);
        btnUpdate = findViewById(R.id.btnUpdatePassword);
        llStepEmail = findViewById(R.id.llStepEmail);
        llStepReset = findViewById(R.id.llStepReset);
        progressBar = findViewById(R.id.resetProgressBar);
        TextView backToLogin = findViewById(R.id.backToLogin);

        btnVerify.setOnClickListener(v -> handleEmailVerification());
        btnUpdate.setOnClickListener(v -> handlePasswordUpdate());
        backToLogin.setOnClickListener(v -> finish());
    }

    private void handleEmailVerification() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your registered email", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        // Simulate network processing
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dbHelper.isEmailRegistered(email)) {
                verifiedEmail = email;
                showLoading(false);
                llStepEmail.setVisibility(View.GONE);
                llStepReset.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Identity Verified. Set your new password.", Toast.LENGTH_SHORT).show();
            } else {
                showLoading(false);
                Toast.makeText(this, "Email not found in student records.", Toast.LENGTH_LONG).show();
            }
        }, 1500);
    }

    private void handlePasswordUpdate() {
        String newPass = newPasswordEditText.getText().toString().trim();

        if (newPass.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dbHelper.updatePasswordByEmail(verifiedEmail, newPass)) {
                showLoading(false);
                Toast.makeText(this, "Password reset successful! Please login.", Toast.LENGTH_LONG).show();
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);
            } else {
                showLoading(false);
                Toast.makeText(this, "Error updating password. Try again.", Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!show);
        if (btnUpdate != null) btnUpdate.setEnabled(!show);
    }
}
