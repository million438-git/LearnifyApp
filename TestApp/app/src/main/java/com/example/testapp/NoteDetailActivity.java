package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.IOException;

public class NoteDetailActivity extends AppCompatActivity {

    private TextView tvNoteTitle, tvTopicSubtitle, tvNoteContent, tvKeyPoints, tvExamples;
    private WebView wvNoteContent;
    private View scrollTextContent;
    private ImageView ivBookmark;
    private LinearProgressIndicator noteProgressBar;
    private boolean isBookmarked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        initViews();
        setupToolbar();
        loadNoteData();
        setupActions();
    }

    private void initViews() {
        tvNoteTitle = findViewById(R.id.tvNoteTitle);
        tvTopicSubtitle = findViewById(R.id.tvTopicSubtitle);
        tvNoteContent = findViewById(R.id.tvNoteContent);
        tvKeyPoints = findViewById(R.id.tvKeyPoints);
        tvExamples = findViewById(R.id.tvExamples);
        wvNoteContent = findViewById(R.id.wvNoteContent);
        scrollTextContent = findViewById(R.id.scrollTextContent);
        ivBookmark = findViewById(R.id.ivBookmark);
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
        String subject = getIntent().getStringExtra("SUBJECT_NAME");
        String chapter = getIntent().getStringExtra("CHAPTER_NAME");
        String topic = getIntent().getStringExtra("TOPIC_NAME");

        if (subject == null) subject = "Mathematics";
        if (chapter == null) chapter = "Chapter 1";
        if (topic == null) topic = "Overview";

        tvNoteTitle.setText(chapter);
        tvTopicSubtitle.setText(topic);

        // Dynamic flexible path discovery
        String htmlUrl = StudyContentProvider.getHtmlUrl(this, subject, chapter, topic);

        if (htmlUrl != null) {
            wvNoteContent.setVisibility(View.VISIBLE);
            scrollTextContent.setVisibility(View.GONE);
            wvNoteContent.getSettings().setJavaScriptEnabled(true);
            wvNoteContent.loadUrl(htmlUrl);
        } else {
            // Fallback UI for non-HTML or missing files
            wvNoteContent.setVisibility(View.GONE);
            scrollTextContent.setVisibility(View.VISIBLE);
            tvNoteContent.setText("This lesson is being updated. Please check back later.");
            tvKeyPoints.setText("• Content loading soon...");
            tvExamples.setText("No examples available yet.");
        }
        
        noteProgressBar.setProgress(100);
    }

    private void setupActions() {
        ivBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            ivBookmark.setImageResource(isBookmarked ? 
                android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
            Toast.makeText(this, isBookmarked ? "Bookmarked!" : "Removed Bookmark", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnAIHelp).setOnClickListener(v -> {
            Intent intent = new Intent(this, AIChatActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnPractice).setOnClickListener(v -> {
            Intent intent = new Intent(this, QuestionActivity.class);
            intent.putExtra("SUBJECT_NAME", getIntent().getStringExtra("SUBJECT_NAME"));
            intent.putExtra("CHAPTER_NAME", getIntent().getStringExtra("CHAPTER_NAME"));
            intent.putExtra("TOPIC_NAME", getIntent().getStringExtra("TOPIC_NAME"));
            startActivity(intent);
        });
    }
}
