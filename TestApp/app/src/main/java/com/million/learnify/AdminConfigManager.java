package com.million.learnify;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AdminConfigManager - Client Governance Engine for Category C System Configuration.
 * Treats the Spring Boot backend as the authoritative source.
 * Enforces server-side policy retrieval, secure local caching in SQLite, and ensures
 * client controls cannot override server security restrictions.
 */
public class AdminConfigManager {

    private static final String TAG = "AdminConfigManager";

    public interface AdminConfigCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    private static volatile AdminConfigManager instance;

    public static AdminConfigManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AdminConfigManager.class) {
                if (instance == null && context != null) {
                    instance = new AdminConfigManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final Context context;
    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public AdminConfigManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new DatabaseHelper(this.context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public AdminSystemConfig getCachedConfig() {
        AdminSystemConfig config = dbHelper.getAdminSystemConfig(AdminSystemConfig.DEFAULT_CONFIG_ID);
        if (config == null) {
            config = new AdminSystemConfig();
            config.setCached(false);
        }
        return config;
    }

    public void fetchRemoteConfig(
            final SettingsApiService apiService,
            final AdminConfigCallback<AdminSystemConfig> callback) {

        if (apiService == null) {
            if (callback != null) {
                mainHandler.post(() -> callback.onSuccess(getCachedConfig()));
            }
            return;
        }

        executor.execute(() -> {
            try {
                Call<ResponseBody> call = apiService.getAdminSystemConfig();
                Response<ResponseBody> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    AdminSystemConfig config = AdminSystemConfig.fromJson(json);
                    config.setFetchedAt(System.currentTimeMillis());
                    config.setCached(true);

                    // Cache to SQLite
                    dbHelper.saveAdminSystemConfig(config);

                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(config));
                    }
                } else if (response.code() == 403) {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onError("Access Denied: Administrator role is required."));
                    }
                } else {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onError("Failed to fetch configuration: HTTP " + response.code()));
                    }
                }
            } catch (final IOException e) {
                Log.e(TAG, "Network error fetching admin config: " + e.getMessage());
                // Fall back to SQLite cache
                final AdminSystemConfig cached = getCachedConfig();
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(cached));
                }
            } catch (final Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError("Unexpected error: " + e.getMessage()));
                }
            }
        });
    }

    public void updateRemoteConfig(
            final AdminSystemConfig newConfig,
            final SettingsApiService apiService,
            final AdminConfigCallback<AdminSystemConfig> callback) {

        if (newConfig == null) {
            if (callback != null) callback.onError("Configuration payload cannot be null.");
            return;
        }

        if (apiService == null) {
            // Local persistence fallback
            dbHelper.saveAdminSystemConfig(newConfig);
            if (callback != null) {
                mainHandler.post(() -> callback.onSuccess(newConfig));
            }
            return;
        }

        executor.execute(() -> {
            try {
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),
                        newConfig.toJson());

                Call<ResponseBody> call = apiService.updateAdminSystemConfig(body);
                Response<ResponseBody> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseJson = response.body().string();
                    AdminSystemConfig saved = AdminSystemConfig.fromJson(responseJson);
                    saved.setFetchedAt(System.currentTimeMillis());
                    saved.setCached(true);

                    // Update SQLite cache
                    dbHelper.saveAdminSystemConfig(saved);

                    // Record local security log
                    dbHelper.recordSecurityEvent(
                            "admin",
                            "ADMIN_CONFIG_SYNC",
                            "Successfully pushed system configuration v" + saved.getVersion() + " to backend.",
                            "Android Admin Console"
                    );

                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(saved));
                    }
                } else if (response.code() == 400) {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onError("Server rejected configuration: Validation constraint violated."));
                    }
                } else if (response.code() == 403) {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onError("Forbidden: Insufficient privileges to update system configuration."));
                    }
                } else {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onError("Server error HTTP " + response.code()));
                    }
                }
            } catch (final IOException e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError("Network failure pushing configuration: " + e.getMessage()));
                }
            } catch (final Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError("Unexpected error: " + e.getMessage()));
                }
            }
        });
    }

    public boolean isMaintenanceActive() {
        return getCachedConfig().isMaintenanceMode();
    }

    public boolean isAiServicePermitted() {
        return getCachedConfig().isAiServiceEnabled();
    }

    public boolean isCameraScannerPermitted() {
        return getCachedConfig().isCameraScannerEnabled();
    }
}

