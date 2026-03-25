package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * NoteDetailActivity - World-Class Professional Study Interface.
 * Guaranteed 100% consistency for ALL subjects.
 * Updated: Integrated full Voice Action support (Summarize, Read Aloud, Stop).
 */
public class NoteDetailActivity extends AppCompatActivity implements VoiceStudyManager.VoiceCallback {

    private static final String TAG = "NoteDetailActivity";
    private TextView tvNoteTitle, tvTopicSubtitle, tvNoteContent;
    private WebView wvNoteContent;
    private View scrollTextContent;
    private ImageView ivBookmark, ivFullscreen, ivImportant, ivHighlight, ivMyNotes, ivVoiceAssistant;
    private MaterialButton btnReadAloud, btnPractice;
    private FloatingActionButton fabExitFullscreen;
    private AppBarLayout appBarLayout;
    private BottomAppBar bottomAppBar;
    private LinearProgressIndicator noteProgressBar;
    
    private VoiceStudyManager voiceStudyManager;
    private boolean isSpeaking = false;
    private boolean isFullscreen = false;

    private DatabaseHelper dbHelper;
    private OfflineAiEngine offlineAiEngine;
    private String currentUsername, subject, chapter, topic;
    private boolean isBookmarked = false;
    private boolean isImportant = false;
    private String personalNote = "";
    private String highlightData = ""; 

    private ArrayList<String> topicList;
    private int currentTopicIndex = -1;
    private boolean isContinuousMode = false;
    private final Gson gson = new Gson();

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
        boolean autoStart = getIntent().getBooleanExtra("AUTO_START_VOICE", false);

        initViews();
        setupToolbar();
        loadNoteData();
        loadUserPersonalization();
        setupActions();
        enableZoom();

        if (autoStart) {
            isContinuousMode = true;
            // Short delay to ensure TTS and WebView are ready
            new android.os.Handler().postDelayed(this::triggerReadAloud, 1500);
        }
    }

    private void initViews() {
        tvNoteTitle = findViewById(R.id.tvNoteTitle);
        tvTopicSubtitle = findViewById(R.id.tvTopicSubtitle);
        tvNoteContent = findViewById(R.id.tvNoteContent);
        wvNoteContent = findViewById(R.id.wvNoteContent);
        scrollTextContent = findViewById(R.id.scrollTextContent);
        
        ivHighlight = findViewById(R.id.ivHighlight);
        ivMyNotes = findViewById(R.id.ivMyNotes);
        ivImportant = findViewById(R.id.ivImportant);
        ivFullscreen = findViewById(R.id.ivFullscreen);
        ivBookmark = findViewById(R.id.ivBookmark);
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant);
        
        btnReadAloud = findViewById(R.id.btnReadAloud);
        btnPractice = findViewById(R.id.btnPractice);
        
        fabExitFullscreen = findViewById(R.id.fabExitFullscreen);
        appBarLayout = findViewById(R.id.appBarLayout);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        noteProgressBar = findViewById(R.id.noteProgressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.noteToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
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
                @Override
                public void onPageFinished(WebView view, String url) {
                    applyHighlights();
                }
            });
            wvNoteContent.loadUrl(htmlUrl);
        } else {
            wvNoteContent.setVisibility(View.GONE);
            scrollTextContent.setVisibility(View.VISIBLE);
            tvNoteContent.setText("Preparing high-quality academic content for " + cleanTopicName(topic) + ". Tap Voice Study to begin listening.");
        }
        noteProgressBar.setProgress(100);
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
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
        }
    }

    private void loadUserPersonalization() {
        Cursor cursor = dbHelper.getStudyNote(currentUsername, subject, chapter, topic);
        if (cursor != null && cursor.moveToFirst()) {
            personalNote = cursor.getString(cursor.getColumnIndexOrThrow("personal_note"));
            highlightData = cursor.getString(cursor.getColumnIndexOrThrow("highlight_data"));
            isBookmarked = cursor.getInt(cursor.getColumnIndexOrThrow("is_bookmarked")) == 1;
            isImportant = cursor.getInt(cursor.getColumnIndexOrThrow("is_important")) == 1;
            cursor.close();
        }
        updatePersonalizationUI();
    }

    private void updatePersonalizationUI() {
        if (ivBookmark != null) ivBookmark.setImageResource(isBookmarked ? android.R.drawable.btn_star_big_on : android.R.drawable.ic_menu_save);
        if (ivImportant != null) {
            ivImportant.setImageResource(isImportant ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
            if (isImportant) ivImportant.setColorFilter(Color.parseColor("#F59E0B")); 
            else ivImportant.clearColorFilter();
        }
    }

    private void savePersonalization() {
        dbHelper.saveStudyNote(currentUsername, subject, chapter, topic, personalNote, highlightData, isBookmarked ? 1 : 0, isImportant ? 1 : 0);
    }

    private void setupActions() {
        if (ivHighlight != null) ivHighlight.setOnClickListener(v -> performHighlight());
        if (ivMyNotes != null) ivMyNotes.setOnClickListener(v -> showPersonalNotesDialog());
        if (ivImportant != null) ivImportant.setOnClickListener(v -> {
            isImportant = !isImportant;
            updatePersonalizationUI();
            savePersonalization();
            Toast.makeText(this, isImportant ? "Priority: Essential ⭐" : "Priority Cleared", Toast.LENGTH_SHORT).show();
        });
        if (ivBookmark != null) ivBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            updatePersonalizationUI();
            savePersonalization();
            Toast.makeText(this, isBookmarked ? "Lesson Saved to Library 🔖" : "Removed from Library", Toast.LENGTH_SHORT).show();
        });
        if (ivFullscreen != null) ivFullscreen.setOnClickListener(v -> toggleFullscreen(true));
        if (fabExitFullscreen != null) fabExitFullscreen.setOnClickListener(v -> toggleFullscreen(false));
        if (btnPractice != null) btnPractice.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuestionActivity.class);
            intent.putExtra("SUBJECT_NAME", subject);
            intent.putExtra("CHAPTER_NAME", chapter);
            intent.putExtra("TOPIC_NAME", topic);
            startActivity(intent);
        });
        if (btnReadAloud != null) btnReadAloud.setOnClickListener(v -> {
            if (isSpeaking) { isContinuousMode = false; stopSpeaking(); } 
            else triggerReadAloud();
        });
        if (ivVoiceAssistant != null) {
            ivVoiceAssistant.setOnClickListener(v -> voiceStudyManager.startListening());
        }
    }

    private void performHighlight() {
        if (wvNoteContent.getVisibility() == View.VISIBLE) {
            String js = "(function() { " +
                        "  var selection = window.getSelection();" +
                        "  if (selection.rangeCount > 0) {" +
                        "    var range = selection.getRangeAt(0);" +
                        "    var text = selection.toString();" +
                        "    var mark = document.createElement('mark');" +
                        "    mark.style.backgroundColor = 'yellow';" +
                        "    mark.style.color = 'black';" +
                        "    try { range.surroundContents(mark); } catch(e) { console.log(e); }" +
                        "    return text;" + 
                        "  } return null; " +
                        "})();";
            
            wvNoteContent.evaluateJavascript(js, value -> {
                String text = (value == null || value.equals("null")) ? "" : gson.fromJson(value, String.class);
                if (text != null && text.length() > 2) {
                    wvNoteContent.evaluateJavascript("document.body.innerHTML;", html -> {
                        highlightData = gson.fromJson(html, String.class);
                        savePersonalization();
                    });
                    showOfflineAiMenu(text);
                } else {
                    Toast.makeText(this, "Select text to highlight!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void applyHighlights() {
        if (highlightData != null && !highlightData.isEmpty() && !highlightData.equals("null")) {
            String escapedHtml = gson.toJson(highlightData);
            String js = "document.body.innerHTML = " + escapedHtml + ";";
            wvNoteContent.evaluateJavascript(js, null);
        }
    }

    private void showOfflineAiMenu(String selectedText) {
        new AlertDialog.Builder(this)
                .setTitle("Academic AI Partner 🤖")
                .setItems(new String[]{"Summarize Locally", "Explain Concept", "Save to Notes"}, (dialog, which) -> {
                    if (which == 0) showAiResultDialog("Summary", offlineAiEngine.summarize(selectedText));
                    else if (which == 1) showAiResultDialog("Explanation", offlineAiEngine.explain(topic, selectedText));
                    else {
                        personalNote += "\n\n[AI Summary]: " + offlineAiEngine.summarize(selectedText);
                        savePersonalization();
                        Toast.makeText(this, "Added to reflections!", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void showAiResultDialog(String title, String result) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(result).setPositiveButton("Got it", null).show();
    }

    private void showPersonalNotesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("My Lesson Reflections");
        final EditText input = new EditText(this);
        input.setHint("Write your personal takeaways here...");
        input.setText(personalNote);
        input.setPadding(40, 40, 40, 40);
        input.setMinLines(5);
        input.setGravity(android.view.Gravity.TOP);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(30, 10, 30, 10);
        container.addView(input);
        builder.setView(container);
        builder.setPositiveButton("SAVE", (dialog, which) -> {
            personalNote = input.getText().toString();
            savePersonalization();
            Toast.makeText(this, "Thoughts preserved!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void triggerReadAloud() {
        if (!voiceStudyManager.isTtsReady()) {
            Toast.makeText(this, "Voice Engine is preparing...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isSpeaking) stopSpeaking();
        else {
            SettingsService settings = new SettingsService(this);
            if (!settings.loadSettings().isTextToSpeech()) {
                Toast.makeText(this, "Enable Voice Study in Settings first!", Toast.LENGTH_LONG).show();
                return;
            }

            if (wvNoteContent.getVisibility() == View.VISIBLE) {
                String script = "(function() { " +
                    "  var output = ''; " +
                    "  var main = document.querySelector('.textbook, main, .container, .master-chapter, .chem-book') || document.body; " +
                    "  var cards = main.querySelectorAll('.q-card, .question-block, .section-card, .question, .card, .note-card'); " +
                    "  if (cards.length > 0) { " +
                    "    cards.forEach(function(c) { " +
                    "      var q = c.querySelector('.q-text, .note-title, h2, h3, strong'); " +
                    "      var ex = c.querySelector('.explanation, .note-content, .answer-box, .answer, p, .highlight-box'); " +
                    "      if (q) output += 'Point: ' + q.innerText.trim() + '. '; " +
                    "      if (ex) output += 'Insight: ' + (ex.innerText || ex.textContent).trim() + '. '; " +
                    "    }); " +
                    "  } else { " +
                    "    output = main.innerText || main.textContent || ''; " +
                    "  } " +
                    "  return output.replace(/\\\\s+/g, ' ').trim(); " +
                    "})();";

                wvNoteContent.evaluateJavascript(script, 
                    value -> {
                        String cleanText = (value == null || value.equals("null")) ? "" : gson.fromJson(value, String.class);
                        if (cleanText == null || cleanText.length() < 10) {
                            speakWithHumanization(tvNoteContent.getText().toString());
                        } else {
                            speakWithHumanization(cleanText);
                        }
                    });
            } else {
                speakWithHumanization(tvNoteContent.getText().toString());
            }
        }
    }

    private void speakWithHumanization(String text) {
        if (text == null || text.trim().length() < 10) {
            text = "Welcome to your study session. Today we are exploring " + cleanTopicName(topic) + ". Let's begin our review.";
        }
        
        String humanizedText = text;
        try {
            if (Python.isStarted()) {
                PyObject pyBrain = Python.getInstance().getModule("voice_brain");
                humanizedText = pyBrain.callAttr("humanize_explanation", cleanTopicName(topic), text).toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Python humanization fallback active.");
        }

        isSpeaking = true;
        runOnUiThread(() -> {
            btnReadAloud.setText("Stop Voice");
            btnReadAloud.setIconResource(android.R.drawable.ic_delete);
        });
        
        voiceStudyManager.speak(humanizedText);
    }

    private void stopSpeaking() {
        voiceStudyManager.stop();
        isSpeaking = false;
        runOnUiThread(() -> {
            btnReadAloud.setText("Voice Study");
            btnReadAloud.setIconResource(android.R.drawable.ic_btn_speak_now);
        });
    }

    @Override public void onCommandRecognized(String text) { Toast.makeText(this, "Assistant heard: " + text, Toast.LENGTH_SHORT).show(); }
    
    @Override public void onActionRequired(String action, String page, String extra) {
        if ("navigate".equals(action)) {
            try {
                Class<?> targetClass = Class.forName("com.million.learnify." + page);
                Intent intent = new Intent(this, targetClass);
                if (extra != null && !extra.isEmpty()) intent.putExtra("SUBJECT_NAME", extra);
                startActivity(intent);
            } catch (Exception e) { Log.e(TAG, "Nav failed: " + page); }
        } else if ("summarize".equals(action)) {
            handleVoiceSummarize();
        } else if ("read_aloud".equals(action)) {
            if (!isSpeaking) triggerReadAloud();
        } else if ("stop_voice".equals(action)) {
            if (isSpeaking) stopSpeaking();
        }
    }

    private void handleVoiceSummarize() {
        if (wvNoteContent.getVisibility() == View.VISIBLE) {
            wvNoteContent.evaluateJavascript("document.body.innerText", value -> {
                String text = (value == null || value.equals("null")) ? "" : gson.fromJson(value, String.class);
                if (text != null && text.length() > 50) {
                    showAiResultDialog("Voice AI Summary", offlineAiEngine.summarize(text));
                } else {
                    Toast.makeText(this, "Material too short to summarize.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            showAiResultDialog("Voice AI Summary", offlineAiEngine.summarize(tvNoteContent.getText().toString()));
        }
    }
    
    @Override public void onSpeechFinished() {
        if (isContinuousMode) runOnUiThread(this::moveToNextTopic);
        else stopSpeaking();
    }

    private void moveToNextTopic() {
        if (topicList != null && currentTopicIndex < topicList.size() - 1) {
            currentTopicIndex++;
            topic = topicList.get(currentTopicIndex);
            loadNoteData();
            loadUserPersonalization();
            Toast.makeText(this, "Continuing Study: " + cleanTopicName(topic), Toast.LENGTH_SHORT).show();
            // Wait for WebView load
            new android.os.Handler().postDelayed(this::triggerReadAloud, 2000);
        } else {
            isContinuousMode = false;
            stopSpeaking();
            Toast.makeText(this, "Unit Pack Complete! 🎓", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleFullscreen(boolean fullscreen) {
        this.isFullscreen = fullscreen;
        if (fullscreen) {
            appBarLayout.setVisibility(View.GONE);
            bottomAppBar.setVisibility(View.GONE);
            fabExitFullscreen.setVisibility(View.VISIBLE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            appBarLayout.setVisibility(View.VISIBLE);
            bottomAppBar.setVisibility(View.VISIBLE);
            fabExitFullscreen.setVisibility(View.GONE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override public void onBackPressed() { if (isFullscreen) toggleFullscreen(false); else super.onBackPressed(); }
    @Override protected void onPause() { if (voiceStudyManager != null && isSpeaking && !isContinuousMode) stopSpeaking(); super.onPause(); }
    @Override protected void onDestroy() { if (voiceStudyManager != null) voiceStudyManager.shutdown(); super.onDestroy(); }
}
