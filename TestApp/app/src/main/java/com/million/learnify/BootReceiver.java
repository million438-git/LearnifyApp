package com.million.learnify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * BootReceiver - Ensures all notifications are restored after a device reboot.
 * This fixes the drawback where alarms are cleared by the system on shutdown.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device rebooted. Restoring all scheduled notifications...");

            // 1. Restore User-Defined Study Reminder
            SettingsService settingsService = new SettingsService(context);
            StudentSettings settings = settingsService.loadSettings();
            if (settings != null && settings.getReminderTime() != null) {
                NotificationHelper.scheduleStudyReminder(context, settings.getReminderTime());
            }

            // 2. Restore Daily Motivation (2:00 AM)
            NotificationHelper.scheduleDailyMotivation(context);

            // 3. Restore Login Reminder (24h)
            NotificationHelper.scheduleLoginReminder(context);
            
            Log.d(TAG, "All notifications successfully restored.");
        }
    }
}
