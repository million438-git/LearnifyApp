package com.million.learnify;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Locale;

/**
 * NotificationPreferencesActivity - Manages granular notification settings.
 * Persists data using SettingsService.
 * Supports quiet hours, homework/assignment alerts, exam notifications,
 * learning streaks, achievements, XP milestones, social requests, and time-zone-aware scheduling.
 */
public class NotificationPreferencesActivity extends BaseActivity {

    private static final int REQ_CODE_POST_NOTIFICATIONS = 1101;

    private MaterialSwitch switchPushNotifications, switchQuietHours,
            switchHomeworkAlerts, switchExamAlerts,
            switchDailyChallenge, switchFriendRequests, switchFriendActivity,
            switchAchievements, switchXPMilestones, switchStreakAlerts, switchAnnouncements;
    private LinearLayout llReminderTime, llQuietHoursRange;
    private TextView tvReminderTime, tvQuietHoursRange;

    private SettingsService settingsService;
    private StudentSettings currentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsService = SettingsService.getInstance(this);
        currentSettings = settingsService.loadSettings();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_preferences);

        // Ensure all 6 notification channels exist
        NotificationHelper.createNotificationChannels(this);

        setupToolbar();
        initViews();
        loadSettingsToUI();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.notificationsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        switchQuietHours = findViewById(R.id.switchQuietHours);
        switchHomeworkAlerts = findViewById(R.id.switchHomeworkAlerts);
        switchExamAlerts = findViewById(R.id.switchExamAlerts);
        switchDailyChallenge = findViewById(R.id.switchDailyChallenge);
        switchFriendRequests = findViewById(R.id.switchFriendRequests);
        switchFriendActivity = findViewById(R.id.switchFriendActivity);
        switchAchievements = findViewById(R.id.switchAchievements);
        switchXPMilestones = findViewById(R.id.switchXPMilestones);
        switchStreakAlerts = findViewById(R.id.switchStreakAlerts);
        switchAnnouncements = findViewById(R.id.switchAnnouncements);

        llReminderTime = findViewById(R.id.llReminderTime);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        llQuietHoursRange = findViewById(R.id.llQuietHoursRange);
        tvQuietHoursRange = findViewById(R.id.tvQuietHoursRange);
    }

    private void loadSettingsToUI() {
        if (switchPushNotifications != null) switchPushNotifications.setChecked(currentSettings.isPushEnabled());
        if (switchQuietHours != null) switchQuietHours.setChecked(currentSettings.isQuietHoursEnabled());
        if (switchHomeworkAlerts != null) switchHomeworkAlerts.setChecked(currentSettings.isHomeworkAlerts());
        if (switchExamAlerts != null) switchExamAlerts.setChecked(currentSettings.isExamNotifications());

        if (switchDailyChallenge != null) switchDailyChallenge.setChecked(currentSettings.isNotifyDailyChallenge());
        if (switchFriendRequests != null) switchFriendRequests.setChecked(currentSettings.isNotifyFriendRequests());
        if (switchFriendActivity != null) switchFriendActivity.setChecked(currentSettings.isNotifyFriendActivity());
        if (switchAchievements != null) switchAchievements.setChecked(currentSettings.isNotifyAchievements());
        if (switchXPMilestones != null) switchXPMilestones.setChecked(currentSettings.isNotifyXPMilestones());
        if (switchStreakAlerts != null) switchStreakAlerts.setChecked(currentSettings.isNotifyStreakAlerts());
        if (switchAnnouncements != null) switchAnnouncements.setChecked(currentSettings.isNotifyAnnouncements());

        updateTimeUI();
        updateQuietHoursUI();
    }

    private void setupListeners() {
        setupSwitch(switchPushNotifications, (v, isChecked) -> {
            currentSettings.setPushEnabled(isChecked);
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_CODE_POST_NOTIFICATIONS);
                        return;
                    }
                }
                NotificationHelper.scheduleStudyReminder(this, currentSettings.getReminderTime());
                if (currentSettings.isNotifyStreakAlerts()) {
                    NotificationHelper.scheduleDailyMotivation(this);
                }
            } else {
                NotificationHelper.cancelAllReminders(this);
            }
        });

        setupSwitch(switchQuietHours, (v, isChecked) -> {
            currentSettings.setQuietHoursEnabled(isChecked);
            Toast.makeText(this, isChecked ? "Quiet hours active: notifications muted during rest" : "Quiet hours disabled", Toast.LENGTH_SHORT).show();
        });

        setupSwitch(switchHomeworkAlerts, (v, isChecked) -> currentSettings.setHomeworkAlerts(isChecked));
        setupSwitch(switchExamAlerts, (v, isChecked) -> currentSettings.setExamNotifications(isChecked));
        setupSwitch(switchDailyChallenge, (v, isChecked) -> currentSettings.setNotifyDailyChallenge(isChecked));
        setupSwitch(switchFriendRequests, (v, isChecked) -> currentSettings.setNotifyFriendRequests(isChecked));
        setupSwitch(switchFriendActivity, (v, isChecked) -> currentSettings.setNotifyFriendActivity(isChecked));
        setupSwitch(switchAchievements, (v, isChecked) -> currentSettings.setNotifyAchievements(isChecked));
        setupSwitch(switchXPMilestones, (v, isChecked) -> currentSettings.setNotifyXPMilestones(isChecked));

        setupSwitch(switchStreakAlerts, (v, isChecked) -> {
            currentSettings.setNotifyStreakAlerts(isChecked);
            if (isChecked && currentSettings.isPushEnabled()) {
                NotificationHelper.scheduleDailyMotivation(this);
            } else {
                NotificationHelper.cancelDailyMotivation(this);
            }
        });

        setupSwitch(switchAnnouncements, (v, isChecked) -> currentSettings.setNotifyAnnouncements(isChecked));

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
                    if (currentSettings.isPushEnabled()) {
                        NotificationHelper.scheduleStudyReminder(this, newTime);
                    }
                    updateTimeUI();
                    AppFeedbackManager.getInstance(this).performSuccess(tvReminderTime);
                    Toast.makeText(this, "Daily study reminder set to " + format12Hour(newTime), Toast.LENGTH_SHORT).show();
                }, hour, minute, false).show();
            });
        }

        if (llQuietHoursRange != null) {
            llQuietHoursRange.setOnClickListener(v -> showQuietHoursTimePicker());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                currentSettings.setPushEnabled(true);
                settingsService.saveSettings(currentSettings);
                NotificationHelper.scheduleStudyReminder(this, currentSettings.getReminderTime());
                Toast.makeText(this, "Notifications enabled successfully", Toast.LENGTH_SHORT).show();
            } else {
                currentSettings.setPushEnabled(false);
                settingsService.saveSettings(currentSettings);
                if (switchPushNotifications != null) {
                    switchPushNotifications.setChecked(false);
                }
                Toast.makeText(this, "Notification permission is required to receive reminders", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showQuietHoursTimePicker() {
        String[] options = {
                "10:00 PM – 07:00 AM (Default Sleep)",
                "11:00 PM – 06:00 AM (Night Owl)",
                "09:00 PM – 06:00 AM (Early Rest)",
                "Custom Time Range..."
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Quiet Hours Period")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        currentSettings.setQuietHoursStart("22:00");
                        currentSettings.setQuietHoursEnd("07:00");
                        saveQuietHours();
                    } else if (which == 1) {
                        currentSettings.setQuietHoursStart("23:00");
                        currentSettings.setQuietHoursEnd("06:00");
                        saveQuietHours();
                    } else if (which == 2) {
                        currentSettings.setQuietHoursStart("21:00");
                        currentSettings.setQuietHoursEnd("06:00");
                        saveQuietHours();
                    } else {
                        pickCustomQuietHoursStart();
                    }
                })
                .show();
    }

    private void pickCustomQuietHoursStart() {
        new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String startTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
            currentSettings.setQuietHoursStart(startTime);
            pickCustomQuietHoursEnd();
        }, 22, 0, false).show();
    }

    private void pickCustomQuietHoursEnd() {
        new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String endTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
            currentSettings.setQuietHoursEnd(endTime);
            saveQuietHours();
        }, 7, 0, false).show();
    }

    private void saveQuietHours() {
        settingsService.saveSettings(currentSettings);
        updateQuietHoursUI();
        AppFeedbackManager.getInstance(this).performSuccess(tvQuietHoursRange);
        Toast.makeText(this, "Quiet hours updated", Toast.LENGTH_SHORT).show();
    }

    private void setupSwitch(MaterialSwitch sw, android.widget.CompoundButton.OnCheckedChangeListener listener) {
        if (sw != null) {
            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                AppFeedbackManager.getInstance(this).performTap(buttonView);
                listener.onCheckedChanged(buttonView, isChecked);
                settingsService.saveSettings(currentSettings);
            });
        }
    }

    private void updateTimeUI() {
        if (tvReminderTime == null) return;
        try {
            String time24 = currentSettings.getReminderTime();
            tvReminderTime.setText("⏰ " + format12Hour(time24));
        } catch (Exception e) {
            tvReminderTime.setText("⏰ " + currentSettings.getReminderTime());
        }
    }

    private void updateQuietHoursUI() {
        if (tvQuietHoursRange == null) return;
        try {
            String start = format12Hour(currentSettings.getQuietHoursStart());
            String end = format12Hour(currentSettings.getQuietHoursEnd());
            tvQuietHoursRange.setText(start + " – " + end + " ▼");
        } catch (Exception e) {
            tvQuietHoursRange.setText(currentSettings.getQuietHoursStart() + " – " + currentSettings.getQuietHoursEnd());
        }
    }

    private String format12Hour(String time24) {
        if (time24 == null || !time24.contains(":")) return time24;
        String[] parts = time24.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        String suffix = (hour >= 12) ? "PM" : "AM";
        int hour12 = (hour == 0) ? 12 : (hour > 12 ? hour - 12 : hour);
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, suffix);
    }
}
