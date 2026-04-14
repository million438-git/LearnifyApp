package com.million.learnify;

import android.app.AlarmManager;
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

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    /**
     * Schedules a study reminder. 
     * Handles both "HH:mm" (24h) and "hh:mm AM/PM" (12h) formats.
     */
    public static void scheduleStudyReminder(Context context, String time) {
        int hour, minute;
        try {
            if (time.toUpperCase().contains("AM") || time.toUpperCase().contains("PM")) {
                SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.US);
                Date date = sdf12.parse(time);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
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

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 101, intent, 
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
                Log.d(TAG, "Study reminder scheduled for: " + calendar.getTime().toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling study reminder", e);
        }
    }

    /**
     * Schedules daily motivation at 2:00 AM Ethiopian Time (GMT+3).
     */
    public static void scheduleDailyMotivation(Context context) {
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

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 500, intent, 
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
        long triggerAt = System.currentTimeMillis() + (24 * 3600 * 1000L);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        intent.putExtra("type", "login_reminder");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 600, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    public static void showInstantNotification(Context context, String type, String title, String message) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        intent.putExtra("type", type);
        intent.putExtra("taskTitle", title);
        intent.putExtra("extraData", message);
        context.sendBroadcast(intent);
    }

    public static void cancelStudyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 101, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void scheduleTaskNotification(Context context, int taskId, String title, String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date dueDate = sdf.parse(date + " " + time);
            
            if (dueDate == null || dueDate.before(new Date())) return;

            // Remind 1 hour before
            long triggerAt = dueDate.getTime() - (60 * 60 * 1000); 
            if (triggerAt < System.currentTimeMillis()) triggerAt = System.currentTimeMillis() + 5000;

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

    public static void scheduleGenericNotification(Context context, String type, int requestCode, int delayHours) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        intent.putExtra("type", type);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerAt = System.currentTimeMillis() + (delayHours * 3600 * 1000L);

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    public static void cancelNotification(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.million.learnify.STUDY_NOTIFICATION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
