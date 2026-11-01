package com.million.learnify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BootReceiver - Restores scheduled reminders upon device reboot.
 * Respects student push preferences: if push notifications are disabled,
 * no alarms are registered.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {

            Log.d(TAG, "Device rebooted. Inspecting student notification preferences...");

            // Ensure notification channels exist after system reboot
            NotificationHelper.createNotificationChannels(context);

            SettingsService settingsService = SettingsService.getInstance(context);
            StudentSettings settings = settingsService != null ? settingsService.loadSettings() : null;

            if (settings == null) {
                Log.w(TAG, "No saved student settings found on boot. Skipping reminder restoration.");
                return;
            }

            if (!settings.isPushEnabled()) {
                Log.d(TAG, "Push notifications are disabled. Alarms cleared and will not be scheduled.");
                NotificationHelper.cancelAllReminders(context);
                return;
            }

            // 1. Restore User-Defined Study Reminder
            if (settings.isStudyReminder() && settings.getReminderTime() != null) {
                NotificationHelper.scheduleStudyReminder(context, settings.getReminderTime());
                Log.d(TAG, "Restored study reminder for " + settings.getReminderTime());
            }

            // 2. Restore Daily Motivation & Streak Reminders
            if (settings.isNotifyStreakAlerts() || settings.isMotivationEnabled()) {
                NotificationHelper.scheduleDailyMotivation(context);
                NotificationHelper.scheduleLoginReminder(context);
                Log.d(TAG, "Restored daily motivation and streak alerts.");
            }

            Log.d(TAG, "Notification schedule restoration complete.");
        }
    }
}
