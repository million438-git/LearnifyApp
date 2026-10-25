package com.million.learnify;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.util.Locale;

/**
 * NotificationPreferencesActivity - Manages granular notification settings.
 * Persists data using SettingsService.
 */
public class NotificationPreferencesActivity extends AppCompatActivity {

    private MaterialSwitch switchDailyChallenge, switchFriendRequests, switchFriendActivity,
            switchAchievements, switchXPMilestones, switchStreakAlerts,
            switchAnnouncements, switchPushNotifications;
    private LinearLayout llReminderTime;
    private TextView tvReminderTime;
    
    private SettingsService settingsService;
    private StudentSettings currentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsService = new SettingsService(this);
        currentSettings = settingsService.loadSettings();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_preferences);

        setupToolbar();
        initViews();
        loadSettingsToUI();
        setupListeners();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.notificationsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        switchDailyChallenge = findViewById(R.id.switchDailyChallenge);
        switchFriendRequests = findViewById(R.id.switchFriendRequests);
        switchFriendActivity = findViewById(R.id.switchFriendActivity);
        switchAchievements = findViewById(R.id.switchAchievements);
        switchXPMilestones = findViewById(R.id.switchXPMilestones);
        switchStreakAlerts = findViewById(R.id.switchStreakAlerts);
        switchAnnouncements = findViewById(R.id.switchAnnouncements);
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        llReminderTime = findViewById(R.id.llReminderTime);
        tvReminderTime = findViewById(R.id.tvReminderTime);
    }

    private void loadSettingsToUI() {
        if (switchDailyChallenge != null) switchDailyChallenge.setChecked(currentSettings.isNotifyDailyChallenge());
        if (switchFriendRequests != null) switchFriendRequests.setChecked(currentSettings.isNotifyFriendRequests());
        if (switchFriendActivity != null) switchFriendActivity.setChecked(currentSettings.isNotifyFriendActivity());
        if (switchAchievements != null) switchAchievements.setChecked(currentSettings.isNotifyAchievements());
        if (switchXPMilestones != null) switchXPMilestones.setChecked(currentSettings.isNotifyXPMilestones());
        if (switchStreakAlerts != null) switchStreakAlerts.setChecked(currentSettings.isNotifyStreakAlerts());
        if (switchAnnouncements != null) switchAnnouncements.setChecked(currentSettings.isNotifyAnnouncements());
        if (switchPushNotifications != null) switchPushNotifications.setChecked(currentSettings.isPushEnabled());
        
        updateTimeUI();
    }

    private void setupListeners() {
        setupSwitch(switchDailyChallenge, (v, isChecked) -> currentSettings.setNotifyDailyChallenge(isChecked));
        setupSwitch(switchFriendRequests, (v, isChecked) -> currentSettings.setNotifyFriendRequests(isChecked));
        setupSwitch(switchFriendActivity, (v, isChecked) -> currentSettings.setNotifyFriendActivity(isChecked));
        setupSwitch(switchAchievements, (v, isChecked) -> currentSettings.setNotifyAchievements(isChecked));
        setupSwitch(switchXPMilestones, (v, isChecked) -> currentSettings.setNotifyXPMilestones(isChecked));
        setupSwitch(switchStreakAlerts, (v, isChecked) -> currentSettings.setNotifyStreakAlerts(isChecked));
        setupSwitch(switchAnnouncements, (v, isChecked) -> currentSettings.setNotifyAnnouncements(isChecked));
        setupSwitch(switchPushNotifications, (v, isChecked) -> {
            currentSettings.setPushEnabled(isChecked);
            if (isChecked) {
                NotificationHelper.scheduleStudyReminder(this, currentSettings.getReminderTime());
            } else {
                NotificationHelper.cancelStudyReminder(this);
            }
        });

        if (llReminderTime != null) {
            llReminderTime.setOnClickListener(v -> {
                String reminder = currentSettings.getReminderTime();
                if (reminder == null || !reminder.contains(":")) reminder = "17:00";
                String[] parts = reminder.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
                    String newTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
                    currentSettings.setReminderTime(newTime);
                    settingsService.saveSettings(currentSettings);
                    NotificationHelper.scheduleStudyReminder(this, newTime);
                    updateTimeUI();
                    Toast.makeText(this, "Reminder time updated", Toast.LENGTH_SHORT).show();
                }, hour, minute, false).show();
            });
        }
    }

    private void setupSwitch(MaterialSwitch sw, android.widget.CompoundButton.OnCheckedChangeListener listener) {
        if (sw != null) {
            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onCheckedChanged(buttonView, isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }
    }

    private void updateTimeUI() {
        if (tvReminderTime == null) return;
        try {
            String time24 = currentSettings.getReminderTime();
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String suffix = (hour >= 12) ? "PM" : "AM";
            int hour12 = (hour == 0) ? 12 : (hour > 12 ? hour - 12 : hour);
            tvReminderTime.setText(String.format(Locale.US, "⏰ %02d:%02d %s", hour12, minute, suffix));
        } catch (Exception e) {
            tvReminderTime.setText("⏰ " + currentSettings.getReminderTime());
        }
    }
}
