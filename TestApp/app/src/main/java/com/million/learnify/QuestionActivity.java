package com.million.learnify;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;

/**
 * QuestionActivity - Dynamic HTML Practice Engine.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class QuestionActivity extends BaseActivity {

    private WebView wvQuestionContent;
    private View scrollLegacyQuestions;
    private TextView tvSubjectHeader, tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Student users to enter this portal.
        enforceRole("STUDENT");
        
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

        if (tvSubjectHeader != null) tvSubjectHeader.setText(subjectName);
        if (tvProgress != null) tvProgress.setText("Practice: " + chapterName);

        String url = StudyContentProvider.getHtmlUrl(this, subjectName, chapterName, topicName + "_questions");
        
        if (url == null) {
            url = StudyContentProvider.getHtmlUrl(this, subjectName, chapterName, "questions");
            if (url == null && "Physics".equalsIgnoreCase(subjectName)) {
                url = StudyContentProvider.getHtmlUrl(this, subjectName, chapterName, "Physics And Human Society");
            }
        }
        
        if (url == null && topicName != null && topicName.toLowerCase().contains("question")) {
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
