package com.million.learnify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "STUDY_REMINDER_CHANNEL";

    private static final String[] MOTIVATION_MESSAGES = {
        "Rise and shine! Your future is created by what you do today, not tomorrow. 🌟",
        "The expert in anything was once a beginner. Start your study session now! 📚",
        "Success is the sum of small efforts, repeated day in and day out. Let's go! 🚀",
        "Believe you can and you're halfway there. Have a productive morning! ☀️",
        "Don't stop until you're proud. AceGrade is here to help you succeed! 🎓",
        "Your education is a dress rehearsal for a life that is yours to lead. 🌈",
        "Every morning is a new opportunity to become better than you were yesterday. 💪",
        "The secret of getting ahead is getting started. Open your lessons today! 📖"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("type");
        if (type == null) type = "study";

        String taskTitle = intent.getStringExtra("taskTitle");
        showNotification(context, type, taskTitle);
    }

    private void showNotification(Context context, String type, String extra) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "AceGrade Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        String title = "Time to Study! 📚";
        String message = "Focus on your goals. Open AceGrade to continue learning.";

        if ("motivation".equals(type)) {
            title = "Morning Motivation ☀️";
            message = MOTIVATION_MESSAGES[new Random().nextInt(MOTIVATION_MESSAGES.length)];
        } else if ("homework".equals(type)) {
            title = "Homework Reminder 📝";
            message = extra != null ? "Task: " + extra : "Check your pending tasks and finish them on time.";
        } else if ("exam".equals(type)) {
            title = "Exam Alert 🎓";
            message = "Your exams are approaching. Keep revising!";
        } else if ("ai_tips".equals(type)) {
            title = "AI Study Tip 💡";
            message = "Take a 5-minute break to improve your focus!";
        } else if ("welcome".equals(type)) {
            title = "Welcome to Learnify! 🎓";
            message = "We're glad to have you here. Let's start your learning journey!";
        }

        // Save notification to database for in-app display
        SharedPreferences pref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = pref.getString("username", "");
        if (!username.isEmpty()) {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            dbHelper.addNotification(username, title, message, type);
        }

        Intent notificationIntent = new Intent(context, DashboardActivity.class);
        notificationIntent.putExtra("SHOW_NOTIFICATIONS", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        
        // Notify any active Dashboard to update its UI
        Intent updateIntent = new Intent("com.example.testapp.UPDATE_NOTIFICATION_UI");
        context.sendBroadcast(updateIntent);
    }
}
