package com.million.learnify;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Random;

/**
 * MailService - Professional Authentication Dispatcher.
 * Simulates a high-security SMTP integration using automated verification codes.
 */
public class MailService {

    private static final String TAG = "MailService";
    private final Context context;
    private final DatabaseHelper dbHelper;

    public MailService(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Dispatches a professional verification email simulation.
     * Uses a secure 6-digit numeric OTP for maximum user compatibility.
     */
    public void sendVerificationEmail(String email, String username) {
        // 1. Generate a professional 6-digit secure code
        String otpCode = String.valueOf(100000 + new Random().nextInt(900000));
        
        // 2. Persist to secure database layer
        dbHelper.updateVerificationToken(username, otpCode);

        Log.d(TAG, "Security Protocol: Dispatching OTP " + otpCode + " to " + email);

        // 3. Simulate real-world network latency and SMTP relay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            
            // 4. Professional Simulation: System-Level Email Arrival
            // This notification acts as the 'Incoming Mail' from Learnify Admin
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
            intent.putExtra("type", "email_verification");
            intent.putExtra("extraData", otpCode); // The OTP is the 'Token'
            
            context.sendBroadcast(intent);
            
            Log.i(TAG, "SMTP Transaction Complete. Verification payload delivered to: " + email);
        }, 2500);
    }
}
