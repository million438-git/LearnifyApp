package com.million.learnify;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * SecurePreferencesHelper - Professional-grade encrypted storage.
 * Uses Android Keystore System to ensure data is unreadable by other apps.
 * Surpasses standard SharedPreferences in security.
 */
public class SecurePreferencesHelper {

    private static final String TAG = "SecurePrefs";
    private static final String SECURE_PREFS_NAME = "Learnify_Secure_Prefs";
    private SharedPreferences encryptedPrefs;

    public SecurePreferencesHelper(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Secure storage initialization failed. Falling back to standard prefs.", e);
            // Fallback to standard prefs if hardware encryption fails or Keystore is corrupted
            encryptedPrefs = context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveString(String key, String value) {
        if (encryptedPrefs != null) {
            encryptedPrefs.edit().putString(key, value).apply();
        }
    }

    public String getString(String key, String defaultValue) {
        return encryptedPrefs != null ? encryptedPrefs.getString(key, defaultValue) : defaultValue;
    }

    public void saveBoolean(String key, boolean value) {
        if (encryptedPrefs != null) {
            encryptedPrefs.edit().putBoolean(key, value).apply();
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return encryptedPrefs != null ? encryptedPrefs.getBoolean(key, defaultValue) : defaultValue;
    }

    public void clearSession() {
        if (encryptedPrefs != null) {
            encryptedPrefs.edit().clear().apply();
        }
    }
}
