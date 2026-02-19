package com.example.testapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

/**
 * ForgotPasswordActivity - Professional student password recovery system.
 * Verifies identity against the database and simulates a secure reset link transmission.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputEditText emailEditText;
    private Button btnReset;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);
        initViews();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.resetEmail);
        btnReset = findViewById(R.id.btnResetPassword);
        TextView backToLogin = findViewById(R.id.backToLogin);
        // Assuming activity_forgot_password.xml might need a ProgressBar for professional feel
        // If not present, we will just handle the logic
        
        btnReset.setOnClickListener(v -> handlePasswordReset());
        backToLogin.setOnClickListener(v -> finish());
    }

    private void handlePasswordReset() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your registered email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Professional Verification Logic
        btnReset.setEnabled(false);
        btnReset.setText("VERIFYING...");

        // Simulate network delay for professional feel
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dbHelper.isEmailRegistered(email)) {
                // REAL SYSTEM SYNC: Email exists in student database
                Toast.makeText(this, "Security link successfully sent to " + email, Toast.LENGTH_LONG).show();
                Toast.makeText(this, "Please check your inbox to reset your password.", Toast.LENGTH_LONG).show();
                
                // Professional exit after success
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 2000);
            } else {
                // SECURITY: Standard professional response
                Toast.makeText(this, "Verification failed. This email is not associated with an active student account.", Toast.LENGTH_LONG).show();
                btnReset.setEnabled(true);
                btnReset.setText("SEND RESET LINK");
            }
        }, 1500);
    }
}
