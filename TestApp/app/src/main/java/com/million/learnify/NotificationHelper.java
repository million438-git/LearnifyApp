package com.million.learnify;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * NotificationHelper - Production-grade scheduling, multi-channel notification engine.
 * Supports:
 * - 6 Dedicated Android O+ Notification Channels with distinct priorities and badges
 * - Exact and inexact alarm scheduling with permission-aware fallbacks
 * - Time-zone-aware daily motivation (GMT+3) and study schedule management
 * - Exam, assignment, achievement, XP milestone, streak, and social notification dispatch
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    // Dedicated Notification Channels
    public static final String CHANNEL_STUDY_REMINDERS = "learnify_study_reminders";
    public static final String CHANNEL_EXAMS_ASSIGNMENTS = "learnify_exams_assignments";
    public static final String CHANNEL_ACHIEVEMENTS_XP = "learnify_achievements_xp";
    public static final String CHANNEL_SOCIAL_COMMUNITY = "learnify_social_community";
    public static final String CHANNEL_ANNOUNCEMENTS = "learnify_announcements";
    public static final String CHANNEL_SECURITY = "LEARNIFY_SECURITY_CHANNEL";

    public static final int REQ_STUDY_REMINDER = 101;
    public static final int REQ_DAILY_MOTIVATION = 500;
    public static final int REQ_LOGIN_REMINDER = 600;
    public static final int REQ_STREAK_ALERT = 700;

    /**
     * Initializes all application notification channels for Android 8.0 (API 26) and above.
     */
    public static void createNotificationChannels(Context context) {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        // 1. Study Reminders
        NotificationChannel studyChannel = new NotificationChannel(
                CHANNEL_STUDY_REMINDERS,
                "Study Reminders",
                NotificationManager.IMPORTANCE_HIGH);
        studyChannel.setDescription("Daily study schedules and session reminders");
        studyChannel.setShowBadge(true);
        studyChannel.enableVibration(true);
        manager.createNotificationChannel(studyChannel);

        // 2. Exams & Assignments
        NotificationChannel examsChannel = new NotificationChannel(
                CHANNEL_EXAMS_ASSIGNMENTS,
                "Exams & Assignments",
                NotificationManager.IMPORTANCE_HIGH);
        examsChannel.setDescription("Upcoming national exam dates, unit tests, and homework deadlines");
        examsChannel.setShowBadge(true);
        examsChannel.enableVibration(true);
        manager.createNotificationChannel(examsChannel);

        // 3. Achievements & XP Milestones
        NotificationChannel achievementsChannel = new NotificationChannel(
                CHANNEL_ACHIEVEMENTS_XP,
                "Achievements & XP Milestones",
                NotificationManager.IMPORTANCE_DEFAULT);
        achievementsChannel.setDescription("Academic badges, study streaks, and level progression");
        achievementsChannel.setShowBadge(true);
        manager.createNotificationChannel(achievementsChannel);

        // 4. Social & Study Partners
        NotificationChannel socialChannel = new NotificationChannel(
                CHANNEL_SOCIAL_COMMUNITY,
                "Social & Study Partners",
                NotificationManager.IMPORTANCE_DEFAULT);
        socialChannel.setDescription("Study partner requests, peer activity, and leaderboard updates");
        socialChannel.setShowBadge(false);
        manager.createNotificationChannel(socialChannel);

        // 5. School & Institutional Announcements
        NotificationChannel announcementsChannel = new NotificationChannel(
                CHANNEL_ANNOUNCEMENTS,
                "Institutional Announcements",
                NotificationManager.IMPORTANCE_HIGH);
        announcementsChannel.setDescription("Important academic announcements from schools and administrators");
        announcementsChannel.setShowBadge(true);
        manager.createNotificationChannel(announcementsChannel);

        // 6. Security & System Verification
        NotificationChannel securityChannel = new NotificationChannel(
                CHANNEL_SECURITY,
                "Security & Verification",
                NotificationManager.IMPORTANCE_HIGH);
        securityChannel.setDescription("Critical 2FA codes, suspicious login alerts, and account safety warnings");
        securityChannel.setShowBadge(true);
        securityChannel.enableVibration(true);
        manager.createNotificationChannel(securityChannel);

        Log.d(TAG, "All 6 Learnify notification channels successfully registered.");
    }

    /**
     * Schedules a study reminder.
     * Handles both "HH:mm" (24h) and "hh:mm AM/PM" (12h) formats.
     */
    public static void scheduleStudyReminder(Context context, String time) {
        if (context == null || time == null || time.trim().isEmpty()) return;

        int hour, minute;
        try {
            if (time.toUpperCase().contains("AM") || time.toUpperCase().contains("PM")) {
                SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.US);
                Date date = sdf12.parse(time);
                Calendar cal = Calendar.getInstance();
                if (date != null) cal.setTime(date);
                hour = cal.get(Calendar.HOUR_OF_DAY);
                minute = cal.get(Calendar.MINUTE);
            } else {
                String[] parts = time.split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
            intent.putExtra("type", "study");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQ_STUDY_REMINDER, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
                Log.d(TAG, "Study reminder scheduled for: " + calendar.getTime());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling study reminder", e);
        }
    }

    /**
     * Schedules daily motivation at 2:00 AM Ethiopian Time (GMT+3).
     */
    public static void scheduleDailyMotivation(Context context) {
        if (context == null) return;

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"));
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        intent.putExtra("type", "motivation");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQ_DAILY_MOTIVATION, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
            Log.d(TAG, "Daily motivation scheduled for 2:00 AM Ethiopian Time");
        }
    }

    /**
     * Schedules a login reminder for 24 hours from now.
     */
    public static void scheduleLoginReminder(Context context) {
        if (context == null) return;
        long triggerAt = System.currentTimeMillis() + (24 * 3600 * 1000L);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        intent.putExtra("type", "login_reminder");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQ_LOGIN_REMINDER, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    /**
     * Schedules an exam preparation alert.
     */
    public static void scheduleExamReminder(Context context, int examId, String examTitle, String date, String time) {
        if (context == null || examTitle == null) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date examDate = sdf.parse(date + " " + time);
            if (examDate == null || examDate.before(new Date())) return;

            // Trigger 24 hours before exam, or 1 hour before if less than 24 hours remain
            long examTimeMillis = examDate.getTime();
            long triggerAt = examTimeMillis - (24 * 3600 * 1000L);
            if (triggerAt < System.currentTimeMillis()) {
                triggerAt = Math.max(System.currentTimeMillis() + 5000L, examTimeMillis - (60 * 60 * 1000L));
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
            intent.putExtra("type", "exam");
            intent.putExtra("taskTitle", examTitle);
            intent.putExtra("extraData", "Exam scheduled on " + date + " at " + time);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 3000 + examId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                Log.d(TAG, "Exam reminder scheduled for " + examTitle + " at " + new Date(triggerAt));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling exam reminder", e);
        }
    }

    /**
     * Schedules a task / assignment reminder.
     */
    public static void scheduleTaskNotification(Context context, int taskId, String title, String date, String time) {
        if (context == null || title == null) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date dueDate = sdf.parse(date + " " + time);

            if (dueDate == null || dueDate.before(new Date())) return;

            // Remind 1 hour before
            long triggerAt = dueDate.getTime() - (60 * 60 * 1000L);
            if (triggerAt < System.currentTimeMillis()) triggerAt = System.currentTimeMillis() + 5000L;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
            intent.putExtra("type", "homework");
            intent.putExtra("taskTitle", title);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2000 + taskId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling task notification", e);
        }
    }

    /**
     * Schedules a learning streak preservation notification.
     */
    public static void scheduleStreakReminder(Context context, int currentStreak) {
        if (context == null) return;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 19); // 7:00 PM reminder
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.DATE, 1);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        intent.putExtra("type", "streak");
        intent.putExtra("taskTitle", "🔥 Keep your " + currentStreak + "-day streak alive!");
        intent.putExtra("extraData", "Complete one quick practice lesson today to maintain your momentum.");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQ_STREAK_ALERT, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        }
    }

    public static void showInstantNotification(Context context, String type, String title, String message) {
        if (context == null) return;
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        intent.putExtra("type", type);
        intent.putExtra("taskTitle", title);
        intent.putExtra("extraData", message);
        context.sendBroadcast(intent);
    }

    public static void cancelStudyReminder(Context context) {
        cancelNotification(context, REQ_STUDY_REMINDER);
    }

    public static void cancelDailyMotivation(Context context) {
        cancelNotification(context, REQ_DAILY_MOTIVATION);
    }

    public static void cancelStreakReminder(Context context) {
        cancelNotification(context, REQ_STREAK_ALERT);
    }

    public static void cancelNotification(Context context, int requestCode) {
        if (context == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * Cancels all scheduled periodic reminders.
     */
    public static void cancelAllReminders(Context context) {
        cancelStudyReminder(context);
        cancelDailyMotivation(context);
        cancelNotification(context, REQ_LOGIN_REMINDER);
        cancelStreakReminder(context);
    }
}
