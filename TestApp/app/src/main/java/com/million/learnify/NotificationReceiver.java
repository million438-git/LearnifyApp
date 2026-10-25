package com.million.learnify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/**
 * NotificationReceiver - Professional Security & Alert Engine.
 * Handles high-priority system alerts and simulates real-world email arrival.
 * Integrated with deep-linking for instant one-tap account verification.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "LEARNIFY_SECURITY_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("type");
        if (type == null) type = "generic";

        String taskTitle = intent.getStringExtra("taskTitle");
        String extraData = intent.getStringExtra("extraData"); 
        String email = intent.getStringExtra("email");
        
        dispatchProfessionalNotification(context, type, taskTitle, extraData, email);
    }

    private void dispatchProfessionalNotification(Context context, String type, String titleExtra, String extraData, String email) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        // 1. Setup High-Priority Security Channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Learnify Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Critical security, motivation, and system alerts");
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }

        String title = "Learnify Update";
        String message = "A new update is available for your account.";
        Intent targetIntent = new Intent(context, DashboardActivity.class);
        
        NotificationCompat.Action action = null;

        // 2. Specialized Flow: Various Notification Types
        if ("email_verification".equals(type)) {
            title = "Verification Code: " + extraData;
            message = "Confirm registration for " + email + ". Tap to verify.";
            targetIntent = new Intent(context, EmailVerificationActivity.class);
            targetIntent.putExtra("EMAIL", email);
            targetIntent.putExtra("USERNAME", email);
            targetIntent.putExtra("OTP_TOKEN", extraData);
        } else if ("welcome".equals(type)) {
            title = "Welcome to Learnify";
            message = "We're glad to have you. Explore your dashboard to start learning.";
            targetIntent = new Intent(context, DashboardActivity.class);
        } else if ("motivation".equals(type)) {
            title = "Daily Study Motivation";
            message = "Believe in yourself and all that you are. Today is a great day to learn something new.";
            targetIntent = new Intent(context, DashboardActivity.class);
        } else if ("login_reminder".equals(type)) {
            title = "Study Session Reminder";
            message = "It's been 24 hours since your last session. Keep your streak alive and study now.";
            targetIntent = new Intent(context, DashboardActivity.class);
        } else if ("homework".equals(type)) {
            title = "Study Task Reminder";
            message = titleExtra != null ? "Upcoming Deadline: " + titleExtra : "You have pending academic tasks.";
            targetIntent = new Intent(context, TasksActivity.class);
        } else if ("ANNOUNCEMENT".equals(type) || "CONTENT".equals(type)) {
            title = titleExtra != null ? titleExtra : "New Announcement";
            message = extraData != null ? extraData : "Check the bulletin board for new updates.";
            targetIntent = new Intent(context, AnnouncementsActivity.class);
        }

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), targetIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 3. Build the Professional Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(mainPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // Use fixed ID for specific types, or timestamp for unique ones
        int notifyId = (int)System.currentTimeMillis();
        if ("motivation".equals(type)) notifyId = 5001;
        if ("login_reminder".equals(type)) notifyId = 6001;

        notificationManager.notify(notifyId, builder.build());
        
        // 4. Update internal UI state
        Intent updateIntent = new Intent("com.million.learnify.UPDATE_NOTIFICATION_UI");
        updateIntent.setPackage(context.getPackageName());
        context.sendBroadcast(updateIntent);
    }
}
