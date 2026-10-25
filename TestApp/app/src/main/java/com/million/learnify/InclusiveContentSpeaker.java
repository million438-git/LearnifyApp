package com.million.learnify;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * InclusiveContentSpeaker - Lifecycle-safe, accessible speech controller for
 * Learnify lessons, notes, exams, and AI explanations.
 * Features:
 * - Proper audio focus handling (AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).
 * - Section-by-section paragraph chunking (avoids Android TTS 4000 char limits).
 * - Play, Pause, Resume, Stop, Next Section, Previous Section controls.
 * - Phonetic scientific formula and HTML translation.
 * - Offline voice verification and graceful error reporting.
 */
public class InclusiveContentSpeaker implements TextToSpeech.OnInitListener {

    private static final String TAG = "InclusiveSpeaker";
    private static final String UTTERANCE_PREFIX = "learnify_utterance_";

    public enum State {
        IDLE,
        INITIALIZING,
        PLAYING,
        PAUSED,
        STOPPED,
        ERROR
    }

    public interface SpeakerListener {
        void onStateChanged(State state, int currentSectionIndex, int totalSections);
        void onSectionStarted(int sectionIndex, String sectionText);
        void onSpeechCompleted();
        void onError(String errorMessage);
    }

    private final Context context;
    private final AudioManager audioManager;
    private final InclusiveModeManager modeManager;

    private TextToSpeech tts;
    private boolean isInitialized = false;
    private State currentState = State.IDLE;

    private List<String> currentSections = new ArrayList<>();
    private int currentSectionIndex = 0;
    private SpeakerListener listener;

    private AudioFocusRequest audioFocusRequest;
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            pause();
        }
    };

    public InclusiveContentSpeaker(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        this.modeManager = InclusiveModeManager.getInstance(this.context);
        initTts();
    }

    public void setListener(@Nullable SpeakerListener listener) {
        this.listener = listener;
    }

    private void initTts() {
        currentState = State.INITIALIZING;
        try {
            tts = new TextToSpeech(context, this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e);
            currentState = State.ERROR;
            if (listener != null) {
                listener.onError("Voice service unavailable on this device.");
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to device default locale
                tts.setLanguage(Locale.getDefault());
            }

            float speechRate = modeManager.getSpeechRate();
            tts.setSpeechRate(speechRate);
            tts.setPitch(1.0f);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                tts.setAudioAttributes(attrs);
            }

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    currentState = State.PLAYING;
                    if (listener != null) {
                        listener.onStateChanged(currentState, currentSectionIndex, currentSections.size());
                    }
                }

                @Override
                public void onDone(String utteranceId) {
                    handleSectionFinished();
                }

                @Override
                public void onError(String utteranceId) {
                    currentState = State.ERROR;
                    abandonAudioFocus();
                    if (listener != null) {
                        listener.onError("Speech playback encountered an error.");
                        listener.onStateChanged(currentState, currentSectionIndex, currentSections.size());
                    }
                }
            });

            isInitialized = true;
            currentState = State.IDLE;
        } else {
            Log.e(TAG, "TextToSpeech onInit failed with status: " + status);
            currentState = State.ERROR;
            if (listener != null) {
                listener.onError("Speech engine initialization failed.");
                listener.onStateChanged(currentState, 0, 0);
            }
        }
    }

    /**
     * Prepares content by converting HTML, markdown, and scientific formulas into spoken sentences,
     * chunking into logical sections, and starts playback.
     */
    public void speakContent(@Nullable String rawContent) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            stop();
            return;
        }

        // Convert HTML / math formulas into phonetically clean speech
        String spokenText = AccessibilityUtils.cleanHtmlForSpeech(rawContent);
        this.currentSections = chunkContentIntoSections(spokenText);
        this.currentSectionIndex = 0;

        if (currentSections.isEmpty()) {
            stop();
            return;
        }

        playSection(currentSectionIndex);
    }

    /**
     * Plays a single short utterance immediately (e.g. state announcements, hints).
     */
    public void speakAnnouncement(@Nullable String text) {
        if (text == null || text.trim().isEmpty()) return;
        if (!isInitialized || tts == null) return;

        requestAudioFocus();
        String spoken = AccessibilityUtils.cleanMathForSpeech(text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_PREFIX + "announcement");
        } else {
            Bundle params = new Bundle();
            tts.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_PREFIX + "announcement");
        }
    }

    public void pause() {
        if (currentState == State.PLAYING) {
            if (tts != null) {
                tts.stop();
            }
            currentState = State.PAUSED;
            abandonAudioFocus();
            if (listener != null) {
                listener.onStateChanged(currentState, currentSectionIndex, currentSections.size());
            }
        }
    }

    public void resume() {
        if (currentState == State.PAUSED && currentSectionIndex < currentSections.size()) {
            playSection(currentSectionIndex);
        }
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
        currentState = State.STOPPED;
        currentSectionIndex = 0;
        abandonAudioFocus();
        if (listener != null) {
            listener.onStateChanged(currentState, currentSectionIndex, currentSections.size());
        }
    }

    public void nextSection() {
        if (currentSectionIndex + 1 < currentSections.size()) {
            currentSectionIndex++;
            playSection(currentSectionIndex);
        } else {
            stop();
        }
    }

    public void previousSection() {
        if (currentSectionIndex > 0) {
            currentSectionIndex--;
            playSection(currentSectionIndex);
        } else {
            playSection(0);
        }
    }

    public void setSpeechRate(float rate) {
        if (tts != null && isInitialized) {
            tts.setSpeechRate(rate);
        }
        modeManager.setSpeechRate(rate);
    }

    public State getCurrentState() {
        return currentState;
    }

    public boolean isPlaying() {
        return currentState == State.PLAYING;
    }

    public int getCurrentSectionIndex() {
        return currentSectionIndex;
    }

    public int getTotalSections() {
        return currentSections.size();
    }

    /**
     * Checks if at least one offline voice is installed and available for English or default locale.
     */
    public boolean hasOfflineVoice() {
        if (tts == null || !isInitialized) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Set<Voice> voices = tts.getVoices();
                if (voices != null) {
                    for (Voice v : voices) {
                        if (!v.isNetworkConnectionRequired()) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error checking offline voices: " + e.getMessage());
            }
        }
        return false;
    }

    private void playSection(int index) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not ready for playback");
            return;
        }

        if (index < 0 || index >= currentSections.size()) {
            stop();
            return;
        }

        requestAudioFocus();
        String text = currentSections.get(index);
        String utteranceId = UTTERANCE_PREFIX + index;

        if (listener != null) {
            listener.onSectionStarted(index, text);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
    }

    private void handleSectionFinished() {
        if (currentSectionIndex + 1 < currentSections.size()) {
            currentSectionIndex++;
            playSection(currentSectionIndex);
        } else {
            currentState = State.IDLE;
            abandonAudioFocus();
            if (listener != null) {
                listener.onSpeechCompleted();
                listener.onStateChanged(currentState, currentSections.size(), currentSections.size());
            }
        }
    }

    private List<String> chunkContentIntoSections(String fullText) {
        if (fullText == null || fullText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> sections = new ArrayList<>();
        // Split primarily by double newlines (paragraphs)
        String[] paragraphs = fullText.split("\\n\\s*\\n");
        for (String p : paragraphs) {
            String trimmed = p.trim();
            if (trimmed.isEmpty()) continue;

            // If a paragraph is longer than 500 characters, break by sentence
            if (trimmed.length() > 500) {
                String[] sentences = trimmed.split("(?<=[.!?])\\s+");
                StringBuilder sb = new StringBuilder();
                for (String s : sentences) {
                    if (sb.length() + s.length() > 500 && sb.length() > 0) {
                        sections.add(sb.toString().trim());
                        sb.setLength(0);
                    }
                    sb.append(s).append(" ");
                }
                if (sb.length() > 0) {
                    sections.add(sb.toString().trim());
                }
            } else {
                sections.add(trimmed);
            }
        }
        return sections;
    }

    private void requestAudioFocus() {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            );
        }
    }

    private void abandonAudioFocus() {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }
        } else {
            audioManager.abandonAudioFocus(focusChangeListener);
        }
    }

    public void shutdown() {
        abandonAudioFocus();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        isInitialized = false;
        currentState = State.IDLE;
    }
}
