package com.million.learnify;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * NoteDetailActivity - World-Class Educational Study & Interactive Reader.
 * Features:
 * - Responsive HTML curriculum rendering with custom typography and CSS styling.
 * - Live lesson completion toggle linked to DatabaseHelper.
 * - Quick action links to Practice Quizzes and Virtual STEM Labs.
 * - Previous and Next lesson sequential navigation.
 * - Docked Accessible Audio Lesson Reader with speed controls.
 * - Personal study note taking, bookmarking, and AI voice assistance.
 */
public class NoteDetailActivity extends BaseActivity implements VoiceStudyManager.VoiceCallback {

    private static final String TAG = "NoteDetailActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private TextView tvNoteTitle, tvTopicSubtitle, tvNoteContent;
    private WebView wvNoteContent;
    private View scrollTextContent;
    private ImageView ivBookmark, ivFullscreen, ivMyNotes, ivVoiceAssistant;
    private ImageView ivImportant, ivHighlight, ivRecordAudio, ivHandwrite;
    private DrawingView drawingView;
    private MaterialButton btnReadAloud, btnPractice, btnMarkComplete, btnInteractiveLab;
    private MaterialButton btnPrevLesson, btnNextLesson;
    private FloatingActionButton fabExitFullscreen;
    private AppBarLayout appBarLayout;
    private View bottomDockCard;
    private LinearProgressIndicator noteProgressBar;

    // Accessible Audio Reading Bar
    private View readingBarView;
    private AccessibleReadingController readingController;
    private String lessonSpeechContent = "";

    private VoiceStudyManager voiceStudyManager;
    private boolean isSpeaking = false;
    private boolean isFullscreen = false;
    private boolean isHandwritingMode = false;

    private DatabaseHelper dbHelper;
    private OfflineAiEngine offlineAiEngine;
    private String currentUsername, subject, chapter, topic, grade;
    private boolean isBookmarked = false;
    private boolean isImportant = false;
    private boolean isCompleted = false;
    private String personalNote = "";
    private String highlightData = "";

    private ArrayList<String> topicList;
    private int currentTopicIndex = -1;
    private final Gson gson = new Gson();

    // Audio Reflection
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private boolean isRecording = false;
    private String audioFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        enforceRole("STUDENT");

        setContentView(R.layout.activity_note_detail);

        dbHelper = repository.getLocalDb();
        offlineAiEngine = new OfflineAiEngine(this);
        voiceStudyManager = new VoiceStudyManager(this, this);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        subject = getIntent().getStringExtra("SUBJECT_NAME");
        if (subject == null || subject.trim().isEmpty()) subject = "Mathematics";

        chapter = getIntent().getStringExtra("CHAPTER_NAME");
        if (chapter == null || chapter.trim().isEmpty()) chapter = "UNIT-1";

        topic = getIntent().getStringExtra("TOPIC_NAME");
        if (topic == null || topic.trim().isEmpty()) topic = "Overview";

        grade = getIntent().getStringExtra("GRADE");
        if (grade == null || grade.trim().isEmpty()) {
            grade = pref.getString("grade", "10");
        }
        grade = grade.replace("Grade-", "").replace("Grade", "").replace("-", "").trim();

        topicList = getIntent().getStringArrayListExtra("TOPIC_LIST");
        currentTopicIndex = getIntent().getIntExtra("TOPIC_INDEX", -1);

        File cacheDir = getExternalCacheDir();
        String cachePath = cacheDir != null ? cacheDir.getAbsolutePath() : getCacheDir().getAbsolutePath();
        audioFileName = cachePath + "/note_audio_" + topic.hashCode() + ".3gp";

        initViews();
        setupToolbar();
        loadLessonContent();
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

        ivBookmark = findViewById(R.id.ivBookmark);
        ivMyNotes = findViewById(R.id.ivMyNotes);
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant);
        ivFullscreen = findViewById(R.id.ivFullscreen);

        // Compatibility views
        ivImportant = findViewById(R.id.ivImportant);
        ivHighlight = findViewById(R.id.ivHighlight);
        ivRecordAudio = findViewById(R.id.ivRecordAudio);
        ivHandwrite = findViewById(R.id.ivHandwrite);

        btnReadAloud = findViewById(R.id.btnReadAloud);
        btnPractice = findViewById(R.id.btnPractice);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);
        btnInteractiveLab = findViewById(R.id.btnInteractiveLab);
        btnPrevLesson = findViewById(R.id.btnPrevLesson);
        btnNextLesson = findViewById(R.id.btnNextLesson);

        fabExitFullscreen = findViewById(R.id.fabExitFullscreen);
        appBarLayout = findViewById(R.id.appBarLayout);
        bottomDockCard = findViewById(R.id.bottomDockCard);
        noteProgressBar = findViewById(R.id.noteProgressBar);

        readingBarView = findViewById(R.id.accessibleReadingBar);
        if (readingBarView != null) {
            readingController = new AccessibleReadingController(this, readingBarView, getAccessibilitySpeaker());
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.noteToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        AccessibilityUtils.setAsHeading(tvNoteTitle);
        AccessibilityUtils.setAccessibilityLabel(ivVoiceAssistant, "Open AI Voice Study Assistant", "Button");
        AccessibilityUtils.setAccessibilityLabel(ivMyNotes, "Open Personal Study Notes", "Button");
        AccessibilityUtils.setAccessibilityLabel(ivBookmark, "Save Lesson to Library", "Button");
        AccessibilityUtils.setAccessibilityLabel(ivFullscreen, "Toggle Fullscreen Reader", "Button");
        AccessibilityUtils.setAccessibilityLabel(btnReadAloud, "Read Lesson Aloud with Accessible Speaker", "Button");
        AccessibilityUtils.setAccessibilityLabel(btnPractice, "Start Practice Quiz for this lesson", "Button");
        AccessibilityUtils.setAccessibilityLabel(btnMarkComplete, "Toggle Lesson Completed Status", "Button");
    }

    private void loadLessonContent() {
        tvNoteTitle.setText(cleanTopicName(topic));
        tvTopicSubtitle.setText(subject + " • Grade " + grade + " • " + formatUnitName(chapter));

        // Load HTML file from assets
        String htmlUrl = StudyContentProvider.getHtmlUrl(this, subject, chapter, topic, grade);

        if (htmlUrl != null) {
            wvNoteContent.setVisibility(View.VISIBLE);
            scrollTextContent.setVisibility(View.GONE);

            WebSettings settings = wvNoteContent.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setAllowFileAccess(true);

            wvNoteContent.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    injectCustomReaderStyles(view);
                }
            });

            wvNoteContent.loadUrl(htmlUrl);

            // Fetch raw HTML for TTS
            String rawHtml = StudyContentProvider.getHtmlContent(this, subject, chapter, topic, grade);
            if (rawHtml != null && !rawHtml.trim().isEmpty()) {
                lessonSpeechContent = cleanHtmlForSpeech(rawHtml);
            } else {
                lessonSpeechContent = cleanTopicName(topic) + ". In " + subject + " " + formatUnitName(chapter);
            }
        } else {
            wvNoteContent.setVisibility(View.GONE);
            scrollTextContent.setVisibility(View.VISIBLE);
            String placeholder = "Academic lesson notes for " + cleanTopicName(topic) + " in " + subject + ".";
            tvNoteContent.setText(placeholder);
            lessonSpeechContent = placeholder;
        }

        if (readingController != null) {
            readingController.setContent(lessonSpeechContent);
        }

        if (noteProgressBar != null) {
            noteProgressBar.setProgress(100);
        }

        // Configure STEM Virtual Lab button
        configureLabButton();

        // Update Prev / Next button states
        updateNavigationButtons();

        // Check if lesson was already completed
        isCompleted = dbHelper.isLessonCompleted(currentUsername, subject, grade, chapter, topic);
        updateCompletedButtonUI();

        // Auto-read if student requested it
        boolean autoStart = getIntent().getBooleanExtra("AUTO_START_VOICE", false)
                || getInclusiveModeManager().isAutoReadContentEnabled();
        if (autoStart) {
            triggerReadAloud();
        }
    }

    private void injectCustomReaderStyles(WebView view) {
        String css = "javascript:(function() {" +
                "var style = document.createElement('style');" +
                "style.type = 'text/css';" +
                "style.innerHTML = '" +
                "body { font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif !important; " +
                "line-height: 1.65 !important; font-size: 16px !important; color: #14243A !important; " +
                "padding: 16px 16px 80px 16px !important; margin: 0 auto !important; max-width: 720px !important; } " +
                "h1, h2, h3 { color: #071B38 !important; font-weight: 700 !important; } " +
                "img { max-width: 100% !important; height: auto !important; border-radius: 12px !important; margin: 12px 0 !important; } " +
                "table { width: 100% !important; border-collapse: collapse !important; margin: 16px 0 !important; } " +
                "th, td { border: 1px solid #E2EAF5 !important; padding: 10px !important; } " +
                "th { background-color: #E7F0FF !important; color: #071B38 !important; } " +
                "pre, code { background: #F1F5F9 !important; border-radius: 6px !important; padding: 4px 8px !important; font-size: 14px !important; } " +
                "blockquote { border-left: 4px solid #1769FF !important; margin: 12px 0 !important; padding-left: 14px !important; color: #64748B !important; } " +
                "';" +
                "document.getElementsByTagName('head')[0].appendChild(style);" +
                "})()";
        view.loadUrl(css);
    }

    private String cleanHtmlForSpeech(String html) {
        if (html == null) return "";
        // Strip HTML tags and entities
        String text = html.replaceAll("(?s)<style.*?</style>", " ")
                          .replaceAll("(?s)<script.*?</script>", " ")
                          .replaceAll("<[^>]*>", " ")
                          .replaceAll("&nbsp;", " ")
                          .replaceAll("&amp;", " and ")
                          .replaceAll("&lt;", " less than ")
                          .replaceAll("&gt;", " greater than ")
                          .replaceAll("\\s+", " ")
                          .trim();
        try {
            if (Python.isStarted()) {
                Python py = Python.getInstance();
                PyObject brain = py.getModule("voice_brain");
                return brain.callAttr("clean_textbook", text).toString();
            }
        } catch (Exception ignored) {}
        return text;
    }

    private void configureLabButton() {
        if (btnInteractiveLab == null) return;
        String lower = subject.toLowerCase(Locale.US);
        if (lower.contains("physics")) {
            btnInteractiveLab.setVisibility(View.VISIBLE);
            btnInteractiveLab.setText("Physics Lab");
            btnInteractiveLab.setOnClickListener(v -> startActivity(new Intent(this, PhysicsLabActivity.class)));
        } else if (lower.contains("chemistry")) {
            btnInteractiveLab.setVisibility(View.VISIBLE);
            btnInteractiveLab.setText("Chemistry Lab");
            btnInteractiveLab.setOnClickListener(v -> startActivity(new Intent(this, ChemistryLabActivity.class)));
        } else if (lower.contains("biology")) {
            btnInteractiveLab.setVisibility(View.VISIBLE);
            btnInteractiveLab.setText("Biology Lab");
            btnInteractiveLab.setOnClickListener(v -> startActivity(new Intent(this, BiologyLabActivity.class)));
        } else {
            btnInteractiveLab.setVisibility(View.GONE);
        }
    }

    private void updateNavigationButtons() {
        if (topicList == null || topicList.isEmpty()) {
            if (btnPrevLesson != null) btnPrevLesson.setEnabled(false);
            if (btnNextLesson != null) btnNextLesson.setEnabled(false);
            return;
        }

        if (btnPrevLesson != null) {
            boolean hasPrev = currentTopicIndex > 0;
            btnPrevLesson.setEnabled(hasPrev);
            btnPrevLesson.setAlpha(hasPrev ? 1.0f : 0.4f);
        }

        if (btnNextLesson != null) {
            boolean hasNext = currentTopicIndex >= 0 && currentTopicIndex < topicList.size() - 1;
            btnNextLesson.setEnabled(hasNext);
            btnNextLesson.setAlpha(hasNext ? 1.0f : 0.4f);
        }
    }

    private void updateCompletedButtonUI() {
        if (btnMarkComplete == null) return;
        if (isCompleted) {
            btnMarkComplete.setText("Completed");
            btnMarkComplete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.course_success)));
            btnMarkComplete.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_verified_check));
        } else {
            btnMarkComplete.setText("Mark Complete");
            btnMarkComplete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.course_blue)));
            btnMarkComplete.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_verified_check));
        }
    }

    private void setupActions() {
        if (btnMarkComplete != null) {
            btnMarkComplete.setOnClickListener(v -> {
                isCompleted = !isCompleted;
                dbHelper.markLessonCompleted(currentUsername, subject, grade, chapter, topic, isCompleted);
                updateCompletedButtonUI();
                String message = isCompleted ? "Lesson marked as completed!" : "Lesson marked as incomplete";
                AccessibilityUtils.announceForAccessibility(btnMarkComplete, message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        }

        if (btnPractice != null) {
            btnPractice.setOnClickListener(v -> openPracticeQuiz());
        }

        if (btnPrevLesson != null) {
            btnPrevLesson.setOnClickListener(v -> navigateToLesson(currentTopicIndex - 1));
        }

        if (btnNextLesson != null) {
            btnNextLesson.setOnClickListener(v -> navigateToLesson(currentTopicIndex + 1));
        }

        if (btnReadAloud != null) {
            btnReadAloud.setOnClickListener(v -> triggerReadAloud());
        }

        if (ivBookmark != null) {
            ivBookmark.setOnClickListener(v -> {
                isBookmarked = !isBookmarked;
                updatePersonalizationUI();
                savePersonalization();
                String msg = isBookmarked ? "Lesson saved to library" : "Removed from library";
                AccessibilityUtils.announceForAccessibility(v, msg);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        if (ivMyNotes != null) {
            ivMyNotes.setOnClickListener(v -> showPersonalNotesDialog());
        }

        if (ivVoiceAssistant != null) {
            ivVoiceAssistant.setOnClickListener(v -> voiceStudyManager.startListening());
        }

        if (ivFullscreen != null) {
            ivFullscreen.setOnClickListener(v -> toggleFullscreen(true));
        }

        if (fabExitFullscreen != null) {
            fabExitFullscreen.setOnClickListener(v -> toggleFullscreen(false));
        }
    }

    private void navigateToLesson(int newIndex) {
        if (topicList == null || newIndex < 0 || newIndex >= topicList.size()) return;
        currentTopicIndex = newIndex;
        topic = topicList.get(currentTopicIndex);

        loadLessonContent();
        loadUserPersonalization();

        AccessibilityUtils.announceForAccessibility(tvNoteTitle, "Loaded lesson: " + cleanTopicName(topic));
    }

    private void openPracticeQuiz() {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra("SUBJECT_NAME", subject);
        intent.putExtra("GRADE", grade);
        intent.putExtra("CHAPTER_NAME", chapter);
        intent.putExtra("TOPIC_NAME", topic);
        startActivity(intent);
    }

    private void toggleFullscreen(boolean fullscreen) {
        isFullscreen = fullscreen;
        if (appBarLayout != null) {
            appBarLayout.setVisibility(fullscreen ? View.GONE : View.VISIBLE);
        }
        if (bottomDockCard != null) {
            bottomDockCard.setVisibility(fullscreen ? View.GONE : View.VISIBLE);
        }
        if (fabExitFullscreen != null) {
            fabExitFullscreen.setVisibility(fullscreen ? View.VISIBLE : View.GONE);
        }
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
            ivBookmark.setImageResource(isBookmarked ? android.R.drawable.btn_star_big_on : R.drawable.ic_certificate);
            AccessibilityUtils.setAccessibilityLabel(ivBookmark, isBookmarked ? "Lesson Saved in Library" : "Bookmark Lesson", "Button");
        }
    }

    private void savePersonalization() {
        dbHelper.saveStudyNote(currentUsername, subject, chapter, topic, personalNote, highlightData, isBookmarked ? 1 : 0, isImportant ? 1 : 0);
    }

    private void showPersonalNotesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Personal Study Notes");
        final EditText input = new EditText(this);
        input.setText(personalNote);
        input.setHint("Write key takeaways, questions, or formulas here...");
        input.setContentDescription("Personal study note input field");
        builder.setView(input);
        builder.setPositiveButton("SAVE", (dialog, which) -> {
            personalNote = input.getText().toString();
            savePersonalization();
            AccessibilityUtils.announceForAccessibility(this.getWindow().getDecorView(), "Study notes updated");
            Toast.makeText(this, "Notes saved!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void triggerReadAloud() {
        if (readingBarView != null) {
            readingBarView.setVisibility(View.VISIBLE);
            if (readingController != null) {
                if (lessonSpeechContent == null || lessonSpeechContent.isEmpty()) {
                    lessonSpeechContent = cleanTopicName(topic);
                }
                readingController.setContent(lessonSpeechContent);
                getAccessibilitySpeaker().speakContent(lessonSpeechContent);
                AccessibilityUtils.announceForAccessibility(readingBarView, "Starting lesson reading");
            }
        } else {
            getAccessibilitySpeaker().speakContent(lessonSpeechContent);
        }
    }

    private String cleanTopicName(String name) {
        if (name == null) return "Lesson";
        String clean = name;
        if (clean.contains("/")) clean = clean.substring(clean.lastIndexOf('/') + 1);
        if (clean.toLowerCase(Locale.US).endsWith(".html")) {
            clean = clean.substring(0, clean.length() - 5);
        }
        clean = clean.replace("-", " ").replace("_", " ").trim();
        return clean;
    }

    private String formatUnitName(String unit) {
        if (unit == null) return "Unit";
        String clean = unit.replace("_", " ").replace("-", " ").trim();
        if (clean.toUpperCase(Locale.US).startsWith("UNIT")) {
            return "Unit " + clean.substring(4).trim();
        }
        return clean;
    }

    @Override
    public void onCommandRecognized(String text) {
        runOnUiThread(() -> Toast.makeText(this, "Voice heard: " + text, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActionRequired(String action, String page, String extra) {
        runOnUiThread(() -> {
            if ("read_aloud".equals(action) || "resume".equals(action) || "repeat".equals(action)) {
                triggerReadAloud();
            } else if ("stop_voice".equals(action) || "pause".equals(action)) {
                if (getAccessibilitySpeaker() != null) {
                    getAccessibilitySpeaker().stop();
                }
            } else if ("next".equals(action)) {
                navigateToLesson(currentTopicIndex + 1);
            } else if ("previous".equals(action)) {
                navigateToLesson(currentTopicIndex - 1);
            } else if ("navigate".equals(action) && page != null) {
                try {
                    Class<?> targetClass = Class.forName("com.million.learnify." + page);
                    Intent intent = new Intent(this, targetClass);
                    if (extra != null) intent.putExtra("SUBJECT_NAME", extra);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Voice navigation failed", e);
                }
            } else if ("answer".equals(action) && extra != null) {
                Toast.makeText(this, extra, Toast.LENGTH_LONG).show();
            } else if ("chat".equals(action) && extra != null) {
                Toast.makeText(this, extra, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSpeechFinished() {
        isSpeaking = false;
    }

    @Override
    protected void onDestroy() {
        if (recorder != null) recorder.release();
        if (player != null) player.release();
        if (readingController != null) {
            getAccessibilitySpeaker().stop();
        }
        super.onDestroy();
    }
}
