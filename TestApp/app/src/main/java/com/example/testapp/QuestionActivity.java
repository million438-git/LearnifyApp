package com.example.testapp;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;

/**
 * QuestionActivity - Dynamic HTML Practice Engine.
 * Automatically loads interactive questions from the assets folder.
 */
public class QuestionActivity extends AppCompatActivity {

    private WebView wvQuestionContent;
    private View scrollLegacyQuestions;
    private TextView tvSubjectHeader, tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        initViews();
        setupToolbar();
        loadDynamicQuestions();
    }

    private void initViews() {
        wvQuestionContent = findViewById(R.id.wvQuestionContent);
        scrollLegacyQuestions = findViewById(R.id.scrollLegacyQuestions);
        tvSubjectHeader = findViewById(R.id.tvSubjectHeader);
        tvProgress = findViewById(R.id.tvProgress);
        
        // Hide legacy UI components as we are now 100% HTML-based
        if (scrollLegacyQuestions != null) {
            scrollLegacyQuestions.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.questionToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadDynamicQuestions() {
        String subjectName = getIntent().getStringExtra("SUBJECT_NAME");
        String chapterName = getIntent().getStringExtra("CHAPTER_NAME");
        String topicName = getIntent().getStringExtra("TOPIC_NAME");

        tvSubjectHeader.setText(subjectName);
        tvProgress.setText("Practice: " + chapterName);

        // 1. Try to find a question file specific to the topic
        String url = StudyContentProvider.getHtmlUrl(this, subjectName, chapterName, topicName + "_questions");
        
        // 2. Fallback: Try to find a general question file in the unit folder
        if (url == null) {
            // Updated to check for both 'questions' and subject-specific names
            url = StudyContentProvider.getHtmlUrl(this, subjectName, chapterName, "questions");
            if (url == null && subjectName.equalsIgnoreCase("Physics")) {
                url = StudyContentProvider.getHtmlUrl(this, subjectName, chapterName, "Physics And Human Society");
            }
        }
        
        // 3. Fallback: Check if the topic itself was meant to be the question file
        if (url == null && topicName.toLowerCase().contains("question")) {
            url = StudyContentProvider.getHtmlUrl(this, subjectName, chapterName, topicName);
        }

        if (url != null) {
            wvQuestionContent.setVisibility(View.VISIBLE);
            wvQuestionContent.getSettings().setJavaScriptEnabled(true);
            wvQuestionContent.getSettings().setDomStorageEnabled(true);
            wvQuestionContent.loadUrl(url);
        } else {
            Toast.makeText(this, "No interactive questions found for: " + chapterName, Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
