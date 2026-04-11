package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import java.util.List;

/**
 * ChapterListActivity - Displays units/chapters for a specific subject.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class ChapterListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Student users to enter this portal.
        enforceRole("STUDENT");
        
        setContentView(R.layout.activity_chapter_list);

        String subject = getIntent().getStringExtra("SUBJECT_NAME");
        if (subject == null) subject = "Mathematics";
        
        setupToolbar(subject);
        loadChapters(subject);
    }

    private void setupToolbar(String subject) {
        Toolbar toolbar = findViewById(R.id.chapterToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(subject);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadChapters(String subject) {
        LinearLayout container = findViewById(R.id.chapterContainer);
        if (container == null) return;
        container.removeAllViews();
        
        List<String> chapters = StudyContentProvider.getChapters(this, subject);
        
        for (String chapter : chapters) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_chapter, container, false);
            TextView tvName = itemView.findViewById(R.id.tvChapterName);
            if (tvName != null) tvName.setText(chapter);
            
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, TopicListActivity.class);
                intent.putExtra("SUBJECT_NAME", subject);
                intent.putExtra("CHAPTER_NAME", chapter);
                startActivity(intent);
            });
            container.addView(itemView);
        }
    }
}
