package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * EmailVerificationActivity - Professional Verification Center.
 * Completes activation after Firebase confirms the email verification link.
 */
public class EmailVerificationActivity extends BaseActivity {

    private TextView tvVerificationMsg;
    private String targetEmail, targetUsername;
    private String loginProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        initViews();
        
        // Identifiers are display context only; Firebase is authoritative for verification.
        targetEmail = getIntent().getStringExtra("EMAIL");
        targetUsername = getIntent().getStringExtra("USERNAME");
        loginProvider = getIntent().getStringExtra("PROVIDER");
        if (loginProvider == null) loginProvider = "Password";
        
        if (targetEmail != null) {
            String msg = getString(R.string.verify_email_msg, targetEmail);
            tvVerificationMsg.setText(msg);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        targetEmail = intent.getStringExtra("EMAIL");
        targetUsername = intent.getStringExtra("USERNAME");
        loginProvider = intent.getStringExtra("PROVIDER");
    }

    private void initViews() {
        MaterialButton btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        TextView tvResendCode = findViewById(R.id.tvResendCode);
        tvVerificationMsg = findViewById(R.id.tvVerificationMsg);

        btnVerifyOtp.setText("I verified my email");
        btnVerifyOtp.setOnClickListener(v -> reloadAndCheckVerification());

        tvResendCode.setOnClickListener(v -> {
            FirebaseUser user = getCurrentVerificationUser();
            if (user != null) {
                user.sendEmailVerification().addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.verification_resent, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, FirebaseAuthSupport.messageFor(task.getException()), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadAndCheckVerification();
    }

    private FirebaseUser getCurrentVerificationUser() {
        FirebaseAuth auth = FirebaseAuthSupport.getConfiguredAuth(this);
        FirebaseUser user = auth != null ? auth.getCurrentUser() : null;
        if (user == null || user.getEmail() == null
                || (targetEmail != null && !targetEmail.equalsIgnoreCase(user.getEmail()))) {
            Toast.makeText(this, "The Firebase sign-in session is unavailable. Sign in again.", Toast.LENGTH_LONG).show();
            return null;
        }
        return user;
    }

    private void reloadAndCheckVerification() {
        FirebaseUser user = getCurrentVerificationUser();
        if (user == null) return;

        user.reload().addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, FirebaseAuthSupport.messageFor(task.getException()), Toast.LENGTH_LONG).show();
            } else if (user.isEmailVerified()) {
                completeVerification(user);
            } else {
                Toast.makeText(this, "Email is not verified yet. Open the Firebase link, then try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void completeVerification(FirebaseUser user) {
        try {
            FirebaseSessionManager.StudentProfile profile = FirebaseSessionManager.ensureStudentProfile(
                    this, user, user.getDisplayName(), targetUsername, null, null, "");
            FirebaseSessionManager.saveStudentSession(this, user, profile, loginProvider);
        } catch (IllegalStateException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
