package com.million.learnify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SettingsService - Central Architecture Service for Learnify Preferences.
 * Enforces strict ownership separation between LocalDevicePreferences (Category A: SharedPreferences)
 * and StudentAccountPreferences (Category B: SQLite + Cloud Sync).
 * Provides thread-safe asynchronous and synchronous access with error reporting callbacks.
 */
public class SettingsService {

    public interface SettingsCallback<T> {
        void onSuccess(T result);
        void onError(Throwable throwable);
    }

    private static final String PREFS_NAME = "StudentSettings";
    private static final String KEY_ACTIVE_STUDENT = "active_student_username";

    private static volatile SettingsService instance;

    public static SettingsService getInstance(Context context) {
        if (instance == null) {
            synchronized (SettingsService.class) {
                if (instance == null && context != null) {
                    instance = new SettingsService(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final DatabaseHelper dbHelper;
    private final ExecutorService backgroundExecutor;
    private final Handler mainHandler;

    public SettingsService(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.dbHelper = new DatabaseHelper(this.context);
        this.backgroundExecutor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public String getActiveUsername() {
        return sharedPreferences.getString(KEY_ACTIVE_STUDENT, "student");
    }

    public void setActiveUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            sharedPreferences.edit().putString(KEY_ACTIVE_STUDENT, username.trim()).apply();
        }
    }

    // ==========================================
    // 1. LOCAL DEVICE PREFERENCES (Category A)
    // ==========================================

    public LocalDevicePreferences getLocalDevicePreferences() {
        return LocalDevicePreferences.fromPreferences(sharedPreferences);
    }

    public void saveLocalDevicePreferences(LocalDevicePreferences prefs) {
        if (prefs == null) return;
        prefs.saveToPreferences(sharedPreferences);

        // Sync with centralized InclusiveModeManager
        try {
            InclusiveModeManager imm = InclusiveModeManager.getInstance(context);
            imm.setInclusiveModeEnabled(prefs.isInclusiveMode());
            imm.setTextToSpeechEnabled(prefs.isTextToSpeech());
            imm.setSpeechRate(prefs.getSpeechRate());
            imm.setSpeechPitch(prefs.getSpeechPitch());
            imm.setHighContrastEnabled(prefs.isHighContrastMode());
            imm.setReducedMotionEnabled(prefs.isReduceMotion());
            imm.setAutoReadContentEnabled(prefs.isAutoReadContent());
        } catch (Exception ignored) {
        }
    }

    // ==========================================
    // 2. STUDENT ACCOUNT PREFERENCES (Category B)
    // ==========================================

    public StudentAccountPreferences getStudentAccountPreferences(String username) {
        if (username == null || username.trim().isEmpty()) {
            username = getActiveUsername();
        }

        StudentAccountPreferences prefs = dbHelper.getStudentAccountPreferences(username);
        if (prefs == null) {
            // Backward-compatible migration from legacy SharedPreferences on first load
            prefs = migrateLegacyPreferences(username);
            dbHelper.saveStudentAccountPreferences(prefs);
        }
        return prefs;
    }

    public boolean saveStudentAccountPreferences(StudentAccountPreferences prefs) {
        if (prefs == null) return false;
        if (prefs.getUsername() == null || prefs.getUsername().trim().isEmpty()) {
            prefs.setUsername(getActiveUsername());
        }

        prefs.setUpdatedAt(System.currentTimeMillis());
        if (prefs.getSyncStatus() != StudentAccountPreferences.SyncStatus.CONFLICT) {
            prefs.setSyncStatus(StudentAccountPreferences.SyncStatus.PENDING_UPLOAD);
        }

        return dbHelper.saveStudentAccountPreferences(prefs);
    }

    private StudentAccountPreferences migrateLegacyPreferences(String username) {
        StudentAccountPreferences p = new StudentAccountPreferences(username);
        p.setDailyStudyMinutes(sharedPreferences.getInt("daily_study_minutes", 30));
        p.setWeeklyLessonGoal(sharedPreferences.getInt("weekly_lesson_goal", 10));
        p.setPracticeDifficulty(sharedPreferences.getString("practice_difficulty", "Intermediate"));
        p.setExplanationDepth(sharedPreferences.getString("explanation_depth", "Step-by-Step"));
        p.setPreferredLessonPresentation(sharedPreferences.getString("preferred_lesson_presentation", "Interactive & Visual"));
        p.setPersonalizedRecommendations(sharedPreferences.getBoolean("personalized_recommendations", true));

        p.setStudyReminder(sharedPreferences.getBoolean("notifications_enabled", true));
        p.setReminderTime(sharedPreferences.getString("reminder_time", "17:00"));
        p.setQuietHoursEnabled(sharedPreferences.getBoolean("quiet_hours_enabled", false));
        p.setQuietHoursStart(sharedPreferences.getString("quiet_hours_start", "22:00"));
        p.setQuietHoursEnd(sharedPreferences.getString("quiet_hours_end", "07:00"));

        p.setPushEnabled(sharedPreferences.getBoolean("push_enabled", true));
        p.setNotifyDailyChallenge(sharedPreferences.getBoolean("notify_challenge", true));
        p.setNotifyFriendRequests(sharedPreferences.getBoolean("notify_friend_req", true));
        p.setNotifyFriendActivity(sharedPreferences.getBoolean("notify_friend_act", true));
        p.setNotifyAchievements(sharedPreferences.getBoolean("notify_achievements", true));
        p.setNotifyXPMilestones(sharedPreferences.getBoolean("notify_xp", true));
        p.setNotifyStreakAlerts(sharedPreferences.getBoolean("notify_streak", true));
        p.setNotifyAnnouncements(sharedPreferences.getBoolean("notify_announcements", true));

        p.setProfileVisibility(sharedPreferences.getBoolean("profile_visibility", true));
        p.setShareProgress(sharedPreferences.getBoolean("share_progress", true));
        p.setLanguage(sharedPreferences.getString("language", "en"));
        p.setRegion(sharedPreferences.getString("region", "US"));
        p.setDateFormat(sharedPreferences.getString("date_format", "MM/DD/YYYY"));
        return p;
    }

    // ==========================================
    // 3. UNIFIED FACADE API (Backward-Compatible)
    // ==========================================

    public StudentSettings loadSettings() {
        return loadSettings(getActiveUsername());
    }

    public StudentSettings loadSettings(String username) {
        LocalDevicePreferences local = getLocalDevicePreferences();
        StudentAccountPreferences account = getStudentAccountPreferences(username);
        return new StudentSettings(local, account);
    }

    public void saveSettings(StudentSettings settings) {
        saveSettings(settings, getActiveUsername());
    }

    public void saveSettings(StudentSettings settings, String username) {
        if (settings == null) return;
        saveLocalDevicePreferences(settings.getLocalDevicePreferences());

        StudentAccountPreferences account = settings.getStudentAccountPreferences();
        if (username != null && !username.trim().isEmpty()) {
            account.setUsername(username);
        }
        saveStudentAccountPreferences(account);
    }

    // ==========================================
    // 4. ASYNCHRONOUS OPERATIONS & CALLBACKS
    // ==========================================

    public void saveSettingsAsync(final StudentSettings settings, final SettingsCallback<Void> callback) {
        backgroundExecutor.execute(() -> {
            try {
                saveSettings(settings);
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(null));
                }
            } catch (final Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }

    public void loadSettingsAsync(final SettingsCallback<StudentSettings> callback) {
        backgroundExecutor.execute(() -> {
            try {
                final StudentSettings settings = loadSettings();
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(settings));
                }
            } catch (final Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }

    public void saveAccountPreferencesAsync(
            final StudentAccountPreferences prefs,
            final SettingsCallback<Void> callback) {
        backgroundExecutor.execute(() -> {
            try {
                boolean success = saveStudentAccountPreferences(prefs);
                if (success) {
                    if (callback != null) mainHandler.post(() -> callback.onSuccess(null));
                } else {
                    if (callback != null) mainHandler.post(() -> callback.onError(new IllegalStateException("Failed to persist account preferences to SQLite.")));
                }
            } catch (final Exception e) {
                if (callback != null) mainHandler.post(() -> callback.onError(e));
            }
        });
    }
}
