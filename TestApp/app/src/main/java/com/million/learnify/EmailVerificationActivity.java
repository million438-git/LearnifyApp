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
 * Inherits from BaseActivity for repository access.
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
        
        targetEmail = getIntent().getStringExtra("EMAIL");
        targetUsername = getIntent().getStringExtra("USERNAME");
        
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
            if (token != null) {
                verifyToken(token);
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
            if (code.length() == 6) {
                verifyToken(code);
            } else {
                Toast.makeText(this, R.string.enter_6_digit, Toast.LENGTH_SHORT).show();
            }
        });

        tvResendCode.setOnClickListener(v -> {
            if (targetEmail != null && targetUsername != null) {
                new MailService(this).sendVerificationEmail(targetEmail, targetUsername);
                Toast.makeText(this, R.string.verification_resent, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyToken(String token) {
        Log.d(TAG, "Securing Activation with Token: " + token);
        if (repository.getLocalDb().verifyToken(token)) {
            showSuccessDialog();
        } else {
            Toast.makeText(this, R.string.verification_invalid, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Verification Successful")
                .setMessage("Your email has been confirmed. Welcome to the Learnify community!")
                .setPositiveButton("ENTER DASHBOARD", (dialog, which) -> completeVerification())
                .setCancelable(false)
                .show();
    }

    private void completeVerification() {
        if (targetUsername != null) {
            repository.getLocalDb().setVerified(targetUsername, true);
        }
        
        securePrefs.saveBoolean("isLoggedIn", true);
        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                .putBoolean("isLoggedIn", true)
                .apply();

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
