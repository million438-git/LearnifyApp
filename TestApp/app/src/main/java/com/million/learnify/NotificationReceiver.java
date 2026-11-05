package com.million.learnify;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

/**
 * NotificationReceiver - Multi-channel alert dispatch and background notification processor.
 * Routes alerts to 6 dedicated Android O+ notification channels.
 * Enforces:
 * - Master push notification switch
 * - Granular per-category switches (Exams, Homework, Streaks, Challenges, Announcements, XP, Achievements, Friends)
 * - Quiet hours time window calculation (handling midnight crossovers)
 * - Android 13+ POST_NOTIFICATIONS runtime permission verification
 * - Deep linking to appropriate Activities (Dashboard, Tasks, Announcements, Email Verification)
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

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

        // Check Android 13+ runtime notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification blocked: POST_NOTIFICATIONS permission not granted.");
                return;
            }
        }

        // Initialize channels if needed
        NotificationHelper.createNotificationChannels(context);

        // Security verification alerts always bypass quiet hours and student category toggles
        boolean isSecurityAlert = "email_verification".equals(type) || "security_alert".equals(type);
        if (!isSecurityAlert) {
            SettingsService settingsService = SettingsService.getInstance(context);
            StudentSettings settings = settingsService != null ? settingsService.loadSettings() : null;
            if (settings != null) {
                if (!settings.isPushEnabled()) {
                    Log.d(TAG, "Notification suppressed: push notifications disabled by user.");
                    return;
                }
                if (settings.isQuietHoursEnabled() && isWithinQuietHours(settings.getQuietHoursStart(), settings.getQuietHoursEnd())) {
                    Log.d(TAG, "Notification suppressed: within quiet hours (" + settings.getQuietHoursStart() + " - " + settings.getQuietHoursEnd() + ")");
                    return;
                }
                if (("homework".equals(type) || "assignment".equals(type)) && !settings.isHomeworkAlerts()) {
                    return;
                }
                if ("exam".equals(type) && !settings.isExamNotifications()) {
                    return;
                }
                if (("login_reminder".equals(type) || "motivation".equals(type) || "streak".equals(type)) && !settings.isNotifyStreakAlerts()) {
                    return;
                }
                if (("challenge".equals(type) || "daily_challenge".equals(type)) && !settings.isNotifyDailyChallenge()) {
                    return;
                }
                if (("ANNOUNCEMENT".equalsIgnoreCase(type) || "CONTENT".equalsIgnoreCase(type) || "announcement".equalsIgnoreCase(type))
                        && !settings.isNotifyAnnouncements()) {
                    return;
                }
                if ("friend_req".equals(type) && !settings.isNotifyFriendRequests()) {
                    return;
                }
                if ("friend_act".equals(type) && !settings.isNotifyFriendActivity()) {
                    return;
                }
                if ("achievement".equals(type) && !settings.isNotifyAchievements()) {
                    return;
                }
                if ("xp".equals(type) && !settings.isNotifyXPMilestones()) {
                    return;
                }
            }
        }

        // Determine destination channel, title, content, and target activity
        String channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS;
        String title = "Learnify Update";
        String message = "A new update is available for your learning journey.";
        Intent targetIntent = new Intent(context, DashboardActivity.class);
        int notifyId = (int) System.currentTimeMillis();

        switch (type) {
            case "email_verification":
                channelId = NotificationHelper.CHANNEL_SECURITY;
                title = "Verification Code: " + (extraData != null ? extraData : "");
                message = "Confirm registration for " + (email != null ? email : "your account") + ". Tap to verify.";
                targetIntent = new Intent(context, EmailVerificationActivity.class);
                targetIntent.putExtra("EMAIL", email);
                targetIntent.putExtra("USERNAME", email);
                targetIntent.putExtra("OTP_TOKEN", extraData);
                notifyId = 1001;
                break;

            case "security_alert":
                channelId = NotificationHelper.CHANNEL_SECURITY;
                title = titleExtra != null ? titleExtra : "Security Alert";
                message = extraData != null ? extraData : "A security event was detected on your account.";
                notifyId = 1002;
                break;

            case "study":
                channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS;
                title = "Daily Study Reminder ⏰";
                message = "Time for your scheduled study session! Tap to jump into your lessons.";
                notifyId = 5002;
                break;

            case "motivation":
                channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS;
                title = "Daily Study Motivation";
                message = "Believe in yourself and all that you are. Today is a great day to master something new.";
                notifyId = 5001;
                break;

            case "login_reminder":
                channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS;
                title = "Study Session Reminder";
                message = "It's been 24 hours since your last session. Keep your streak alive and study now.";
                notifyId = 6001;
                break;

            case "homework":
            case "assignment":
                channelId = NotificationHelper.CHANNEL_EXAMS_ASSIGNMENTS;
                title = "Study Task Reminder";
                message = titleExtra != null ? "Upcoming Deadline: " + titleExtra : "You have pending academic tasks.";
                targetIntent = new Intent(context, TasksActivity.class);
                break;

            case "exam":
                channelId = NotificationHelper.CHANNEL_EXAMS_ASSIGNMENTS;
                title = "Upcoming Exam Alert";
                message = titleExtra != null ? titleExtra : (extraData != null ? extraData : "You have an exam scheduled soon. Review your lesson notes.");
                break;

            case "achievement":
                channelId = NotificationHelper.CHANNEL_ACHIEVEMENTS_XP;
                title = "Achievement Unlocked!";
                message = titleExtra != null ? titleExtra : "You earned a new badge on Learnify!";
                break;

            case "xp":
                channelId = NotificationHelper.CHANNEL_ACHIEVEMENTS_XP;
                title = "XP Milestone Reached!";
                message = titleExtra != null ? titleExtra : "Great progress! You just leveled up your study XP points.";
                break;

            case "streak":
                channelId = NotificationHelper.CHANNEL_ACHIEVEMENTS_XP;
                title = titleExtra != null ? titleExtra : "Learning Streak at Risk!";
                message = extraData != null ? extraData : "Complete a quick lesson today to maintain your consecutive study streak.";
                notifyId = 7001;
                break;

            case "friend_req":
                channelId = NotificationHelper.CHANNEL_SOCIAL_COMMUNITY;
                title = "New Study Partner Request";
                message = titleExtra != null ? titleExtra : "A classmate wants to connect with you on Learnify.";
                break;

            case "friend_act":
                channelId = NotificationHelper.CHANNEL_SOCIAL_COMMUNITY;
                title = "Study Partner Update";
                message = titleExtra != null ? titleExtra : "Your study partner just achieved a new milestone.";
                break;

            case "challenge":
            case "daily_challenge":
                channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS;
                title = "Daily Challenge Ready";
                message = titleExtra != null ? titleExtra : "Today's quick challenge is ready. Test your knowledge now!";
                break;

            case "ANNOUNCEMENT":
            case "CONTENT":
            case "announcement":
                channelId = NotificationHelper.CHANNEL_ANNOUNCEMENTS;
                title = titleExtra != null ? titleExtra : "School Announcement";
                message = extraData != null ? extraData : "Check the announcement board for important academic updates.";
                targetIntent = new Intent(context, AnnouncementsActivity.class);
                break;

            case "welcome":
                channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS;
                title = "Welcome to Learnify";
                message = "We're glad to have you! Explore your dashboard to start your personalized learning journey.";
                notifyId = 8001;
                break;

            default:
                if (titleExtra != null) title = titleExtra;
                if (extraData != null) message = extraData;
                break;
        }

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                targetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        notificationManager.notify(notifyId, builder.build());

        // Notify UI to refresh badges/unread indicators if applicable
        Intent updateIntent = new Intent("com.million.learnify.UPDATE_NOTIFICATION_UI");
        updateIntent.setPackage(context.getPackageName());
        context.sendBroadcast(updateIntent);
    }

    private boolean isWithinQuietHours(String start, String end) {
        if (start == null || end == null || !start.contains(":") || !end.contains(":")) {
            return false;
        }
        try {
            Calendar now = Calendar.getInstance();
            int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

            String[] startParts = start.split(":");
            int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);

            String[] endParts = end.split(":");
            int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);

            if (startMinutes <= endMinutes) {
                return currentMinutes >= startMinutes && currentMinutes <= endMinutes;
            } else {
                // Wraps past midnight (e.g. 22:00 to 07:00)
                return currentMinutes >= startMinutes || currentMinutes <= endMinutes;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
