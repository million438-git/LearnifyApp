package com.million.learnify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * EmailVerificationActivity - Professional Verification Center.
 * Handles both manual OTP entry and secure Deep Link activation.
 */
public class EmailVerificationActivity extends BaseActivity {

    private static final String TAG = "EmailVerification";
    private EditText etOtpCode;
    private TextView tvVerificationMsg;
    private String targetEmail, targetUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        initViews();
        
        // Retrieve identifiers from registration/resend flow
        targetEmail = getIntent().getStringExtra("EMAIL");
        targetUsername = getIntent().getStringExtra("USERNAME");
        
        // Support for incoming deep links (from "Email Inbox")
        handleIntent(getIntent());
        
        if (targetEmail != null) {
            String msg = getString(R.string.verify_email_msg, targetEmail);
            tvVerificationMsg.setText(msg);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "learnify".equals(data.getScheme()) && "verify-email".equals(data.getHost())) {
            String token = data.getQueryParameter("token");
            String email = data.getQueryParameter("email");
            if (token != null && email != null) {
                this.targetEmail = email;
                this.targetUsername = email; // In this system, email can act as identifier
                etOtpCode.setText(token);
                verifyToken(email, token);
            }
        }
    }

    private void initViews() {
        etOtpCode = findViewById(R.id.etOtpCode);
        MaterialButton btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        TextView tvResendCode = findViewById(R.id.tvResendCode);
        tvVerificationMsg = findViewById(R.id.tvVerificationMsg);

        btnVerifyOtp.setOnClickListener(v -> {
            String code = etOtpCode.getText().toString().trim();
            String identifier = (targetUsername != null) ? targetUsername : targetEmail;
            
            if (identifier == null) {
                Toast.makeText(this, "Session expired. Please register again.", Toast.LENGTH_LONG).show();
                return;
            }

            if (code.length() == 6) {
                verifyToken(identifier, code);
            } else {
                Toast.makeText(this, R.string.enter_6_digit, Toast.LENGTH_SHORT).show();
            }
        });

        tvResendCode.setOnClickListener(v -> {
            if (targetEmail != null && (targetUsername != null || targetEmail != null)) {
                String identifier = (targetUsername != null) ? targetUsername : targetEmail;
                new MailService(this).sendVerificationEmail(targetEmail, identifier);
                Toast.makeText(this, R.string.verification_resent, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyToken(String identifier, String token) {
        Log.d(TAG, "Security Protocol: Validating token for " + identifier);
        
        if (repository.getLocalDb().verifyToken(identifier, token)) {
            showSuccessDialog();
        } else {
            Toast.makeText(this, R.string.verification_invalid, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Identity Verified ✅")
                .setMessage("Your account has been successfully activated. You now have full access to the student dashboard.")
                .setPositiveButton("GET STARTED", (dialog, which) -> completeVerification())
                .setCancelable(false)
                .show();
    }

    private void completeVerification() {
        String identifier = (targetUsername != null) ? targetUsername : targetEmail;
        if (identifier != null) {
            repository.getLocalDb().setVerified(identifier, true);
        }
        
        // Persist session locally
        securePrefs.saveBoolean("isLoggedIn", true);
        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                .putBoolean("isLoggedIn", true)
                .apply();

        // Navigate to Dashboard and clear backstack
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
