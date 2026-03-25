package com.million.learnify;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

/**
 * DashboardService handles the storage and retrieval of DashboardData.
 * Implements the "Load instantly, sync in background" flow.
 */
public class DashboardService {
    private static final String PREF_NAME = "DashboardCache";
    private static final String KEY_DATA = "dashboard_data";
    private final SharedPreferences preferences;
    private final Gson gson;

    public DashboardService(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Retrieves cached dashboard data for instant display.
     */
    public DashboardData getCachedData() {
        String json = preferences.getString(KEY_DATA, null);
        if (json == null) {
            return new DashboardData(); // Return empty model with defaults
        }
        return gson.fromJson(json, DashboardData.class);
    }

    /**
     * Updates the local cache with fresh data from the server/logic.
     */
    public void cacheData(DashboardData data) {
        data.setLastSync(System.currentTimeMillis());
        String json = gson.toJson(data);
        preferences.edit().putString(KEY_DATA, json).apply();
    }

    /**
     * Simulate background sync with server.
     */
    public void syncDashboard(SyncCallback callback) {
        // In a real app, this would be an API call
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate network delay
                
                DashboardData freshData = new DashboardData();
                freshData.setProgressSummary("Keep it up! You've completed 75% of your weekly goals.");
                freshData.setTodayClasses(3);
                freshData.setPendingHomework(2);
                freshData.getSubjectProgressList().add(new DashboardData.SubjectProgress("Math", 80));
                freshData.getSubjectProgressList().add(new DashboardData.SubjectProgress("English", 60));
                
                cacheData(freshData);
                callback.onSyncComplete(freshData);
            } catch (InterruptedException e) {
                callback.onSyncFailed(e.getMessage());
            }
        }).start();
    }

    public interface SyncCallback {
        void onSyncComplete(DashboardData data);
        void onSyncFailed(String error);
    }
}
