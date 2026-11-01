package com.million.learnify;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.CombinedVibration;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * AppFeedbackManager - Centralized Audio & Haptic Feedback Controller.
 * Ensures student settings (sound effects, haptic feedback, animations, reduced motion)
 * are respected across every screen and interaction in Learnify.
 */
public final class AppFeedbackManager {

    private static volatile AppFeedbackManager instance;
    private final Context appContext;
    private final SettingsService settingsService;
    private final AudioManager audioManager;
    private final Vibrator vibrator;

    private AppFeedbackManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.settingsService = new SettingsService(this.appContext);
        this.audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            this.vibrator = vibratorManager != null ? vibratorManager.getDefaultVibrator() : null;
        } else {
            this.vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public static AppFeedbackManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AppFeedbackManager.class) {
                if (instance == null) {
                    instance = new AppFeedbackManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Triggers a standard click haptic and audio feedback if enabled by the user.
     */
    public void performTap(View view) {
        StudentSettings settings = settingsService.loadSettings();
        if (settings == null) return;

        // Audio
        if (settings.isSoundEffects() && audioManager != null) {
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        }

        // Haptic
        if (settings.isHapticFeedback()) {
            if (view != null) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } else if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(15);
                }
            }
        }
    }

    /**
     * Triggers a success feedback for completed lessons, quiz submissions, or saved settings.
     */
    public void performSuccess(View view) {
        StudentSettings settings = settingsService.loadSettings();
        if (settings == null) return;

        if (settings.isSoundEffects() && audioManager != null) {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }

        if (settings.isHapticFeedback()) {
            if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            } else if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(40);
                }
            }
        }
    }

    /**
     * Triggers error / warning tactile feedback for invalid inputs or failed validations.
     */
    public void performError(View view) {
        StudentSettings settings = settingsService.loadSettings();
        if (settings == null) return;

        if (settings.isHapticFeedback()) {
            if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT);
            } else if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 50, 40, 50}, -1));
                } else {
                    vibrator.vibrate(new long[]{0, 50, 40, 50}, -1);
                }
            }
        }
    }

    /**
     * Whether celebration animations (confetti, badges, score celebrations) should be rendered.
     */
    public boolean shouldAnimateCelebrations() {
        StudentSettings settings = settingsService.loadSettings();
        if (settings == null) return true;
        return settings.isAnimatedCelebrations() && !settings.isReduceMotion();
    }

    /**
     * Whether media auto-downloads are allowed given the current network connection.
     */
    public boolean canAutoDownloadMedia(boolean isWifiConnected, boolean isCellularConnected) {
        StudentSettings settings = settingsService.loadSettings();
        if (settings == null) return true;

        String policy = settings.getAutoDownloadMedia();
        if ("Never".equalsIgnoreCase(policy)) {
            return false;
        }
        if ("Wi-Fi only".equalsIgnoreCase(policy)) {
            return isWifiConnected;
        }
        // "Wi-Fi and Cellular"
        return isWifiConnected || isCellularConnected;
    }
}

