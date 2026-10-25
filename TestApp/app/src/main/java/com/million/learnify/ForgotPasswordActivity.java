package com.million.learnify;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

/**
 * ForgotPasswordActivity - High-Security Account Recovery Hub.
 * Delegates password recovery to Firebase Auth; local OTPs cannot reset cloud credentials.
 */
public class ForgotPasswordActivity extends BaseActivity {

    private TextInputEditText emailEditText;
    private Button btnVerify, btnUpdate;
    private LinearLayout llStepEmail, llStepReset;
    private ProgressBar progressBar;
    private TextView tvResetInstruction, tvResendCode, tvChangeEmail, backToLogin, forgotSubtitle;
    private ImageView btnBack;

    private String verifiedEmail = "";
    private boolean isProcessing = false;
    private long lastResendTime = 0;
    private static final long RESEND_COOLDOWN_MS = 30000; // 30s cooldown
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);
        initViews();
        if (btnUpdate != null) btnUpdate.setText("RETURN TO SIGN IN");
        setupBackNavigation();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.resetEmail);
        btnVerify = findViewById(R.id.btnVerifyEmail);
        btnUpdate = findViewById(R.id.btnUpdatePassword);

        llStepEmail = findViewById(R.id.llStepEmail);
        llStepReset = findViewById(R.id.llStepReset);

        progressBar = findViewById(R.id.resetProgressBar);
        backToLogin = findViewById(R.id.backToLogin);
        forgotSubtitle = findViewById(R.id.forgotSubtitle);
        tvResetInstruction = findViewById(R.id.tvResetInstruction);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvChangeEmail = findViewById(R.id.tvChangeEmail);
        btnBack = findViewById(R.id.btnBack);

        if (btnVerify != null) {
            btnVerify.setOnClickListener(v -> handleSendInstructions());
        }

        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> handlePasswordReset());
        }

        if (backToLogin != null) {
            backToLogin.setOnClickListener(v -> finish());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> handleBack());
        }

        if (tvResendCode != null) {
            tvResendCode.setOnClickListener(v -> handleResendCode());
        }

        if (tvChangeEmail != null) {
            tvChangeEmail.setOnClickListener(v -> switchToStep1());
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });
    }

    private void handleBack() {
        if (llStepReset != null && llStepReset.getVisibility() == View.VISIBLE) {
            switchToStep1();
        } else {
            finish();
        }
    }

    private void switchToStep1() {
        if (llStepReset != null) llStepReset.setVisibility(View.GONE);
        if (llStepEmail != null) llStepEmail.setVisibility(View.VISIBLE);
    }

    private void handleSendInstructions() {
        if (isProcessing) return;

        String email = (emailEditText != null && emailEditText.getText() != null)
                ? emailEditText.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(email)) {
            if (emailEditText != null) emailEditText.setError("Registered email address is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (emailEditText != null) emailEditText.setError("Please enter a valid email address");
            return;
        }

        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);
        if (firebaseAuth == null) {
            Toast.makeText(this, "Firebase is not configured for this app. Contact Learnify support.", Toast.LENGTH_LONG).show();
            return;
        }
        verifiedEmail = email;
        setLoading(true);
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, task -> {
            setLoading(false);
            if (!task.isSuccessful()) {
                Toast.makeText(this, FirebaseAuthSupport.messageFor(task.getException()), Toast.LENGTH_LONG).show();
                return;
            }
            if (llStepEmail != null) llStepEmail.setVisibility(View.GONE);
            if (llStepReset != null) llStepReset.setVisibility(View.VISIBLE);
            if (tvResetInstruction != null) {
                tvResetInstruction.setText(
                        "If an account exists for " + maskEmail(verifiedEmail)
                                + ", Firebase has sent password-reset instructions. Open the email link, then return to sign in.");
            }
            lastResendTime = System.currentTimeMillis();
            Toast.makeText(this, "Check your email for password-reset instructions.", Toast.LENGTH_SHORT).show();
        });
    }

    private void handlePasswordReset() {
        finish();
    }

    private void handleResendCode() {
        long now = System.currentTimeMillis();
        if (now - lastResendTime < RESEND_COOLDOWN_MS) {
            long remainingSec = (RESEND_COOLDOWN_MS - (now - lastResendTime)) / 1000;
            Toast.makeText(this, "Please wait " + remainingSec + "s before requesting a new code.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(verifiedEmail)) {
            if (firebaseAuth == null) {
                Toast.makeText(this, "Firebase is not configured for this app.", Toast.LENGTH_LONG).show();
                return;
            }
            firebaseAuth.sendPasswordResetEmail(verifiedEmail).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    lastResendTime = System.currentTimeMillis();
                    Toast.makeText(this, "Password-reset email sent.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, FirebaseAuthSupport.messageFor(task.getException()), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setLoading(boolean loading) {
        isProcessing = loading;
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (btnVerify != null) {
            btnVerify.setEnabled(!loading);
            btnVerify.setText(loading ? "" : "SEND RESET INSTRUCTIONS");
        }
    }

    private String maskEmail(String email) {
        if (TextUtils.isEmpty(email) || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return email.charAt(0) + "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
