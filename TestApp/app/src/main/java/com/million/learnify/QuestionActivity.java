package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.Arrays;
import java.util.List;

/**
 * QuestionActivity - Dynamic HTML Practice & Assessment Engine.
 * Features:
 * - High-compatibility asset path matching (UNIT-EXAM, Exam, Questions).
 * - Automatic graceful fallback to ExamActivity if chapter-specific HTML is absent.
 * - Inherits from BaseActivity for role-based security.
 */
public class QuestionActivity extends BaseActivity {

    private WebView wvQuestionContent;
    private View scrollLegacyQuestions;
    private TextView tvSubjectHeader, tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
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
        if (subjectName == null) subjectName = "Mathematics";

        String chapterName = getIntent().getStringExtra("CHAPTER_NAME");
        if (chapterName == null) chapterName = "UNIT-1";

        String topicName = getIntent().getStringExtra("TOPIC_NAME");
        String grade = getIntent().getStringExtra("GRADE");
        if (grade == null || grade.trim().isEmpty()) {
            grade = getSharedPreferences("UserSession", MODE_PRIVATE).getString("grade", "10");
        }
        grade = grade.replace("Grade-", "").replace("Grade", "").replace("-", "").trim();

        if (tvSubjectHeader != null) tvSubjectHeader.setText(subjectName + " Practice");
        if (tvProgress != null) tvProgress.setText("Grade " + grade + " • " + chapterName);

        String url = resolveAssessmentUrl(subjectName, chapterName, topicName, grade);

        if (url != null) {
            wvQuestionContent.setVisibility(View.VISIBLE);
            wvQuestionContent.getSettings().setJavaScriptEnabled(true);
            wvQuestionContent.getSettings().setDomStorageEnabled(true);
            wvQuestionContent.getSettings().setAllowFileAccess(true);
            wvQuestionContent.loadUrl(url);
        } else {
            // Graceful fallback to dynamic exam assessment engine
            Toast.makeText(this, "Launching practice quiz for " + subjectName, Toast.LENGTH_SHORT).show();
            Intent examIntent = new Intent(this, ExamActivity.class);
            examIntent.putExtra("SUBJECT_NAME", subjectName);
            startActivity(examIntent);
            finish();
        }
    }

    private String resolveAssessmentUrl(String subject, String chapter, String topic, String grade) {
        // Priority candidate names for assessments
        List<String> candidates = Arrays.asList(
                topic != null ? topic + "_questions" : null,
                topic != null && topic.toLowerCase().contains("exam") ? topic : null,
                topic != null && topic.toLowerCase().contains("quiz") ? topic : null,
                "UNIT-EXAM",
                "UNIT EXAM",
                "Unit Exam",
                "Exam",
                "Exam-1",
                "Exam Questions " + chapter,
                "questions",
                "FINAL-EXAM",
                "FINAL EXAM"
        );

        for (String candidate : candidates) {
            if (candidate == null) continue;
            String found = StudyContentProvider.getHtmlUrl(this, subject, chapter, candidate, grade);
            if (found != null) return found;
        }

        // Check if any topic in this chapter is an exam
        List<String> chapterTopics = StudyContentProvider.getTopics(this, subject, chapter, grade);
        for (String t : chapterTopics) {
            String lower = t.toLowerCase(java.util.Locale.US);
            if (lower.contains("exam") || lower.contains("quiz") || lower.contains("question")) {
                String found = StudyContentProvider.getHtmlUrl(this, subject, chapter, t, grade);
                if (found != null) return found;
            }
        }

        return null;
    }
}
