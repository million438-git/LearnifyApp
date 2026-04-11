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
import java.util.Random;

/**
 * NotificationReceiver - Professional Security & Alert Engine.
 * Handles high-priority system alerts and simulates real-world email arrival.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "LEARNIFY_SECURITY_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("type");
        if (type == null) type = "generic";

        String taskTitle = intent.getStringExtra("taskTitle");
        String extraData = intent.getStringExtra("extraData"); 
        
        dispatchProfessionalNotification(context, type, taskTitle, extraData);
    }

    private void dispatchProfessionalNotification(Context context, String type, String titleExtra, String extraData) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Learnify Security", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Account security and verification alerts");
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }

        String title = "Learnify Alert";
        String message = "Security update available for your account.";
        Intent targetIntent = new Intent(context, DashboardActivity.class);
        
        // Notification Actions for Professionalism
        NotificationCompat.Action action = null;

        if ("email_verification".equals(type)) {
            title = "📧 Confirm Your Learnify Registration";
            message = "Welcome! Tap 'Verify Now' to activate your student dashboard and start learning.";
            
            // Professional Deep Link for 1-click verification
            Uri verifyUri = Uri.parse("learnify://verify-email?token=" + extraData);
            targetIntent = new Intent(Intent.ACTION_VIEW, verifyUri);
            
            PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 1002, targetIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            action = new NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_send, "VERIFY NOW", actionPendingIntent).build();
            
        } else if ("welcome".equals(type)) {
            title = "🎓 Account Created Successfully";
            message = "Your identifier has been secured. Please complete the email verification process sent to your inbox.";
            targetIntent = new Intent(context, EmailVerificationActivity.class);
        } else if ("homework".equals(type)) {
            title = "📝 Study Task Reminder";
            message = titleExtra != null ? "Upcoming: " + titleExtra : "You have pending tasks to complete.";
        }

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), targetIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(mainPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setColor(context.getResources().getColor(R.color.primary_indigo));

        if (action != null) {
            builder.addAction(action);
        }

        // Notify with a fixed ID for verification to avoid multiple alerts
        int notifyId = "email_verification".equals(type) ? 1001 : (int)System.currentTimeMillis();
        notificationManager.notify(notifyId, builder.build());
        
        // Broadcast to update in-app UI badges
        Intent updateIntent = new Intent("com.million.learnify.UPDATE_NOTIFICATION_UI");
        updateIntent.setPackage(context.getPackageName());
        context.sendBroadcast(updateIntent);
    }
}
