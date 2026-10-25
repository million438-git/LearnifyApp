package com.million.learnify;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * AccessibleReadingController - Binds a view_accessible_reading_bar layout
 * to an InclusiveContentSpeaker, providing accessible TalkBack announcements,
 * section navigation, speed adjustments, and play/pause controls.
 */
public class AccessibleReadingController implements InclusiveContentSpeaker.SpeakerListener {

    private final Context context;
    private final InclusiveContentSpeaker speaker;
    private final View rootLayout;

    private TextView tvStatus;
    private MaterialButton btnPlayPause;
    private MaterialButton btnSpeed;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnStop;

    private String currentContentToRead;
    private float currentSpeed = InclusiveModeManager.RATE_NORMAL;

    public AccessibleReadingController(@NonNull Context context,
                                       @NonNull View rootLayout,
                                       @NonNull InclusiveContentSpeaker speaker) {
        this.context = context;
        this.rootLayout = rootLayout;
        this.speaker = speaker;
        this.speaker.setListener(this);
        this.currentSpeed = InclusiveModeManager.getInstance(context).getSpeechRate();
        initViews();
    }

    private void initViews() {
        tvStatus = rootLayout.findViewById(R.id.tvReadingStatus);
        btnPlayPause = rootLayout.findViewById(R.id.btnReadPlayPause);
        btnSpeed = rootLayout.findViewById(R.id.btnReadSpeed);
        btnPrevious = rootLayout.findViewById(R.id.btnReadPrevious);
        btnNext = rootLayout.findViewById(R.id.btnReadNext);
        btnStop = rootLayout.findViewById(R.id.btnReadStop);

        updateSpeedDisplay();

        if (btnPlayPause != null) {
            btnPlayPause.setOnClickListener(v -> {
                if (speaker.isPlaying()) {
                    speaker.pause();
                } else if (speaker.getCurrentState() == InclusiveContentSpeaker.State.PAUSED) {
                    speaker.resume();
                } else {
                    if (currentContentToRead != null) {
                        speaker.speakContent(currentContentToRead);
                    }
                }
            });
        }

        if (btnPrevious != null) {
            btnPrevious.setOnClickListener(v -> speaker.previousSection());
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> speaker.nextSection());
        }

        if (btnStop != null) {
            btnStop.setOnClickListener(v -> speaker.stop());
        }

        if (btnSpeed != null) {
            btnSpeed.setOnClickListener(v -> cycleSpeed());
        }
    }

    public void setContent(@Nullable String rawContent) {
        this.currentContentToRead = rawContent;
    }

    public void setVisibility(int visibility) {
        rootLayout.setVisibility(visibility);
    }

    private void cycleSpeed() {
        if (currentSpeed <= 0.8f) {
            currentSpeed = InclusiveModeManager.RATE_NORMAL;
        } else if (currentSpeed <= 1.1f) {
            currentSpeed = InclusiveModeManager.RATE_FAST;
        } else if (currentSpeed <= 1.35f) {
            currentSpeed = InclusiveModeManager.RATE_VERY_FAST;
        } else {
            currentSpeed = InclusiveModeManager.RATE_SLOW;
        }

        speaker.setSpeechRate(currentSpeed);
        updateSpeedDisplay();
        AccessibilityUtils.announceForAccessibility(btnSpeed, "Speech rate changed to " + btnSpeed.getText());
    }

    private void updateSpeedDisplay() {
        if (btnSpeed == null) return;
        String text = String.format(Locale.US, "%.2fx", currentSpeed);
        btnSpeed.setText(text);
        btnSpeed.setContentDescription("Speech speed: " + text);
    }

    @Override
    public void onStateChanged(InclusiveContentSpeaker.State state, int currentSectionIndex, int totalSections) {
        rootLayout.post(() -> {
            if (state == InclusiveContentSpeaker.State.PLAYING) {
                if (btnPlayPause != null) {
                    btnPlayPause.setText("PAUSE");
                    btnPlayPause.setContentDescription("Pause reading");
                }
                if (tvStatus != null) {
                    tvStatus.setText(String.format(Locale.US, "Reading section %d of %d", currentSectionIndex + 1, totalSections));
                }
            } else if (state == InclusiveContentSpeaker.State.PAUSED) {
                if (btnPlayPause != null) {
                    btnPlayPause.setText("RESUME");
                    btnPlayPause.setContentDescription("Resume reading");
                }
                if (tvStatus != null) {
                    tvStatus.setText(String.format(Locale.US, "Paused at section %d of %d", currentSectionIndex + 1, totalSections));
                }
            } else {
                if (btnPlayPause != null) {
                    btnPlayPause.setText("READ ALOUD");
                    btnPlayPause.setContentDescription("Read lesson aloud");
                }
                if (tvStatus != null) {
                    tvStatus.setText("Audio Lesson Reader");
                }
            }
        });
    }

    @Override
    public void onSectionStarted(int sectionIndex, String sectionText) {
        rootLayout.post(() -> {
            if (tvStatus != null) {
                tvStatus.setText(String.format(Locale.US, "Reading section %d of %d", sectionIndex + 1, speaker.getTotalSections()));
            }
        });
    }

    @Override
    public void onSpeechCompleted() {
        rootLayout.post(() -> {
            if (tvStatus != null) {
                tvStatus.setText("Lesson reading finished");
            }
            if (btnPlayPause != null) {
                btnPlayPause.setText("READ ALOUD");
                btnPlayPause.setContentDescription("Read lesson aloud again");
            }
            AccessibilityUtils.announceForAccessibility(rootLayout, "Lesson reading complete");
        });
    }

    @Override
    public void onError(String errorMessage) {
        rootLayout.post(() -> {
            if (tvStatus != null) {
                tvStatus.setText("Voice: " + errorMessage);
            }
            if (btnPlayPause != null) {
                btnPlayPause.setText("RETRY");
            }
            AccessibilityUtils.announceForAccessibility(rootLayout, errorMessage);
        });
    }
}
