package com.million.learnify;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * NoteDetailActivity - World-Class Professional Study Interface.
 * Enhanced with: Inclusive Learning Mode, Accessible Audio Lesson Reader,
 * Note-taking (Text, Audio, Handwriting), and AI Interaction.
 */
public class NoteDetailActivity extends BaseActivity implements VoiceStudyManager.VoiceCallback {

    private static final String TAG = "NoteDetailActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    
    private TextView tvNoteTitle, tvTopicSubtitle, tvNoteContent;
    private WebView wvNoteContent;
    private View scrollTextContent;
    private ImageView ivBookmark, ivFullscreen, ivImportant, ivHighlight, ivMyNotes, ivVoiceAssistant, ivRecordAudio, ivHandwrite;
    private DrawingView drawingView;
    private MaterialButton btnReadAloud, btnPractice;
    private FloatingActionButton fabExitFullscreen;
    private AppBarLayout appBarLayout;
    private BottomAppBar bottomAppBar;
    private LinearProgressIndicator noteProgressBar;
    
    // Inclusive Audio Reading Bar
    private View readingBarView;
    private AccessibleReadingController readingController;
    private String lessonSpeechContent = "";

    private VoiceStudyManager voiceStudyManager;
    private boolean isSpeaking = false;
    private boolean isFullscreen = false;
    private boolean isHandwritingMode = false;

    private DatabaseHelper dbHelper;
    private OfflineAiEngine offlineAiEngine;
    private String currentUsername, subject, chapter, topic;
    private boolean isBookmarked = false;
    private boolean isImportant = false;
    private String personalNote = "";
    private String highlightData = ""; 

    private ArrayList<String> topicList;
    private int currentTopicIndex = -1;
    private final Gson gson = new Gson();

    // Audio Recording
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private boolean isRecording = false;
    private String audioFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        dbHelper = new DatabaseHelper(this);
        offlineAiEngine = new OfflineAiEngine(this);
        voiceStudyManager = new VoiceStudyManager(this, this);
        
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        subject = getIntent().getStringExtra("SUBJECT_NAME");
        chapter = getIntent().getStringExtra("CHAPTER_NAME");
        topic = getIntent().getStringExtra("TOPIC_NAME");
        topicList = getIntent().getStringArrayListExtra("TOPIC_LIST");
        currentTopicIndex = getIntent().getIntExtra("TOPIC_INDEX", -1);

        File cacheDir = getExternalCacheDir();
        String cachePath = cacheDir != null ? cacheDir.getAbsolutePath() : getCacheDir().getAbsolutePath();
        audioFileName = cachePath + "/note_audio_" + (topic != null ? topic.hashCode() : 0) + ".3gp";

        initViews();
        setupToolbar();
        loadNoteData();
        loadUserPersonalization();
        setupActions();
        enableZoom();
    }

    private void initViews() {
        tvNoteTitle = findViewById(R.id.tvNoteTitle);
        tvTopicSubtitle = findViewById(R.id.tvTopicSubtitle);
        tvNoteContent = findViewById(R.id.tvNoteContent);
        wvNoteContent = findViewById(R.id.wvNoteContent);
        scrollTextContent = findViewById(R.id.scrollTextContent);
        drawingView = findViewById(R.id.drawingView);
        
        ivHighlight = findViewById(R.id.ivHighlight);
        ivMyNotes = findViewById(R.id.ivMyNotes);
        ivImportant = findViewById(R.id.ivImportant);
        ivFullscreen = findViewById(R.id.ivFullscreen);
        ivBookmark = findViewById(R.id.ivBookmark);
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant);
        ivRecordAudio = findViewById(R.id.ivRecordAudio);
        ivHandwrite = findViewById(R.id.ivHandwrite);
        
        btnReadAloud = findViewById(R.id.btnReadAloud);
        btnPractice = findViewById(R.id.btnPractice);
        
        fabExitFullscreen = findViewById(R.id.fabExitFullscreen);
        appBarLayout = findViewById(R.id.appBarLayout);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        noteProgressBar = findViewById(R.id.noteProgressBar);

        // Accessible Reading Bar
        readingBarView = findViewById(R.id.accessibleReadingBar);
        if (readingBarView != null) {
            readingController = new AccessibleReadingController(this, readingBarView, getAccessibilitySpeaker());
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.noteToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Semantic heading and accessibility roles
        AccessibilityUtils.setAsHeading(tvNoteTitle);
        AccessibilityUtils.setAccessibilityLabel(ivVoiceAssistant, "Open Voice Assistant", "Button");
        AccessibilityUtils.setAccessibilityLabel(ivRecordAudio, "Record Audio Reflection", "Button");
        AccessibilityUtils.setAccessibilityLabel(ivHandwrite, "Toggle Handwriting Canvas", "Button");
        AccessibilityUtils.setAccessibilityLabel(ivHighlight, "Highlight Selected Text", "Button");
        AccessibilityUtils.setAccessibilityLabel(ivMyNotes, "Open Personal Study Notes", "Button");
        AccessibilityUtils.setAccessibilityLabel(ivFullscreen, "Toggle Fullscreen", "Button");
        AccessibilityUtils.setAccessibilityLabel(btnReadAloud, "Read Lesson Aloud", "Button");
        AccessibilityUtils.setAccessibilityLabel(btnPractice, "Start Practice Quiz", "Button");
    }

    private void loadNoteData() {
        if (subject == null) subject = "Mathematics";
        if (chapter == null) chapter = "Module";
        if (topic == null) topic = "Overview";

        tvNoteTitle.setText(chapter);
        tvTopicSubtitle.setText(cleanTopicName(topic));

        String htmlUrl = StudyContentProvider.getHtmlUrl(this, subject, chapter, topic);
        if (htmlUrl != null) {
            wvNoteContent.setVisibility(View.VISIBLE);
            scrollTextContent.setVisibility(View.GONE);
            wvNoteContent.getSettings().setJavaScriptEnabled(true);
            wvNoteContent.getSettings().setDomStorageEnabled(true);
            wvNoteContent.getSettings().setAllowFileAccess(true);
            wvNoteContent.setWebViewClient(new WebViewClient() {
                @Override public void onPageFinished(WebView view, String url) { applyHighlights(); }
            });
            wvNoteContent.loadUrl(htmlUrl);

            // Fetch raw HTML for accessible speech extraction
            String rawHtml = StudyContentProvider.getHtmlContent(this, subject, chapter, topic);
            lessonSpeechContent = (rawHtml != null && !rawHtml.trim().isEmpty()) ? rawHtml : chapter + ". " + cleanTopicName(topic);
        } else {
            wvNoteContent.setVisibility(View.GONE);
            scrollTextContent.setVisibility(View.VISIBLE);
            String placeholder = "Academic content for " + cleanTopicName(topic) + " in " + chapter + ".";
            tvNoteContent.setText(placeholder);
            lessonSpeechContent = placeholder;
        }

        if (readingController != null) {
            readingController.setContent(lessonSpeechContent);
        }

        noteProgressBar.setProgress(100);

        // Auto-read if student enabled auto-read or navigated with auto-voice
        boolean autoStart = getIntent().getBooleanExtra("AUTO_START_VOICE", false)
                || getInclusiveModeManager().isAutoReadContentEnabled();
        if (autoStart) {
            triggerReadAloud();
        }
    }

    private String cleanTopicName(String name) {
        if (name == null) return "";
        if (name.contains("/")) return name.substring(name.lastIndexOf("/") + 1);
        return name;
    }

    private void enableZoom() {
        if (wvNoteContent != null) {
            WebSettings settings = wvNoteContent.getSettings();
            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false); 
        }
    }

    private void loadUserPersonalization() {
        Cursor cursor = dbHelper.getStudyNote(currentUsername, subject, chapter, topic);
        if (cursor != null && cursor.moveToFirst()) {
            personalNote = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            highlightData = cursor.getString(cursor.getColumnIndexOrThrow("highlights"));
            isBookmarked = cursor.getInt(cursor.getColumnIndexOrThrow("is_bookmarked")) == 1;
            isImportant = cursor.getInt(cursor.getColumnIndexOrThrow("is_important")) == 1;
            cursor.close();
        }
        updatePersonalizationUI();
    }

    private void updatePersonalizationUI() {
        if (ivBookmark != null) {
            ivBookmark.setImageResource(isBookmarked ? android.R.drawable.btn_star_big_on : android.R.drawable.ic_menu_save);
            AccessibilityUtils.setAccessibilityLabel(ivBookmark, isBookmarked ? "Lesson Saved in Library" : "Bookmark Lesson", "Button");
        }
        if (ivImportant != null) {
            ivImportant.setImageResource(isImportant ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
            if (isImportant) ivImportant.setColorFilter(Color.parseColor("#F59E0B")); 
            else ivImportant.clearColorFilter();
            AccessibilityUtils.setAccessibilityLabel(ivImportant, isImportant ? "Marked as Essential Priority" : "Mark as Important", "Button");
        }
    }

    private void savePersonalization() {
        dbHelper.saveStudyNote(currentUsername, subject, chapter, topic, personalNote, highlightData, isBookmarked ? 1 : 0, isImportant ? 1 : 0);
    }

    private void setupActions() {
        if (ivHighlight != null) ivHighlight.setOnClickListener(v -> performHighlight());
        if (ivMyNotes != null) ivMyNotes.setOnClickListener(v -> showPersonalNotesDialog());
        if (ivRecordAudio != null) ivRecordAudio.setOnClickListener(v -> handleAudioRecording());
        if (ivHandwrite != null) ivHandwrite.setOnClickListener(v -> toggleHandwriting());
        
        if (ivImportant != null) ivImportant.setOnClickListener(v -> {
            isImportant = !isImportant;
            updatePersonalizationUI();
            savePersonalization();
            String msg = isImportant ? "Priority: Essential" : "Priority Cleared";
            AccessibilityUtils.announceForAccessibility(v, msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
        
        if (ivBookmark != null) ivBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            updatePersonalizationUI();
            savePersonalization();
            String msg = isBookmarked ? "Lesson Saved" : "Removed from Library";
            AccessibilityUtils.announceForAccessibility(v, msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        if (btnReadAloud != null) btnReadAloud.setOnClickListener(v -> triggerReadAloud());
        if (ivVoiceAssistant != null) ivVoiceAssistant.setOnClickListener(v -> voiceStudyManager.startListening());
    }

    private void toggleHandwriting() {
        isHandwritingMode = !isHandwritingMode;
        drawingView.setVisibility(isHandwritingMode ? View.VISIBLE : View.GONE);
        ivHandwrite.setColorFilter(isHandwritingMode ? Color.CYAN : Color.WHITE);
        if (isHandwritingMode) {
            AccessibilityUtils.announceForAccessibility(drawingView, "Handwriting Mode Active. Draw directly on screen.");
            Toast.makeText(this, "Handwriting Mode Active. Draw on screen!", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(this)
                .setTitle("Save Handwriting?")
                .setMessage("Keep your handwritten notes?")
                .setPositiveButton("Keep", null)
                .setNegativeButton("Clear", (d, w) -> drawingView.clearCanvas())
                .show();
        }
    }

    private void handleAudioRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        if (!isRecording) {
            startRecording();
            ivRecordAudio.setColorFilter(Color.RED);
            AccessibilityUtils.announceForAccessibility(ivRecordAudio, "Recording audio reflection started");
            Toast.makeText(this, "Recording audio reflection...", Toast.LENGTH_SHORT).show();
        } else {
            stopRecording();
            ivRecordAudio.clearColorFilter();
            AccessibilityUtils.announceForAccessibility(ivRecordAudio, "Reflection saved. Tap again to play.");
            Toast.makeText(this, "Reflection saved! Tap again to play.", Toast.LENGTH_SHORT).show();
            playAudio();
        }
        isRecording = !isRecording;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(audioFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try { recorder.prepare(); recorder.start(); } catch (IOException e) { Log.e(TAG, "prepare() failed", e); }
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception ignored) {}
            recorder.release();
            recorder = null;
        }
    }

    private void playAudio() {
        player = new MediaPlayer();
        try {
            player.setDataSource(audioFileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "playAudio() failed", e);
        }
    }

    private void performHighlight() {
        if (wvNoteContent.getVisibility() == View.VISIBLE) {
            wvNoteContent.evaluateJavascript("(function(){return window.getSelection().toString();})();", value -> {
                String text = gson.fromJson(value, String.class);
                if (text != null && text.length() > 2) {
                    highlightData += text + "||";
                    savePersonalization();
                    AccessibilityUtils.announceForAccessibility(wvNoteContent, "Highlighted: " + text);
                    Toast.makeText(this, "Highlighted: " + text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void applyHighlights() {}

    private void showPersonalNotesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Personal Study Notes");
        final EditText input = new EditText(this);
        input.setText(personalNote);
        input.setContentDescription("Personal study note input field");
        builder.setView(input);
        builder.setPositiveButton("SAVE", (dialog, which) -> {
            personalNote = input.getText().toString();
            savePersonalization();
            AccessibilityUtils.announceForAccessibility(this.getWindow().getDecorView(), "Study notes updated");
            Toast.makeText(this, "Notes updated!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void triggerReadAloud() {
        if (readingBarView != null) {
            readingBarView.setVisibility(View.VISIBLE);
            if (readingController != null) {
                if (lessonSpeechContent == null || lessonSpeechContent.isEmpty()) {
                    lessonSpeechContent = tvNoteContent.getText().toString();
                    readingController.setContent(lessonSpeechContent);
                }
                getAccessibilitySpeaker().speakContent(lessonSpeechContent);
                AccessibilityUtils.announceForAccessibility(readingBarView, "Starting lesson reading");
            }
        } else {
            getAccessibilitySpeaker().speakContent(lessonSpeechContent);
        }
    }

    @Override public void onCommandRecognized(String text) { Toast.makeText(this, "Heard: " + text, Toast.LENGTH_SHORT).show(); }
    @Override public void onActionRequired(String action, String page, String extra) {}
    @Override public void onSpeechFinished() { isSpeaking = false; }

    @Override protected void onDestroy() {
        if (recorder != null) recorder.release();
        if (player != null) player.release();
        if (readingController != null) {
            getAccessibilitySpeaker().stop();
        }
        super.onDestroy();
    }
}
