package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;

public class TopicListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_list);

        String subject = getIntent().getStringExtra("SUBJECT_NAME");
        String chapter = getIntent().getStringExtra("CHAPTER_NAME");
        
        setupToolbar(chapter);
        loadTopics(subject, chapter);
    }

    private void setupToolbar(String chapter) {
        Toolbar toolbar = findViewById(R.id.topicToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(chapter);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadTopics(String subject, String chapter) {
        LinearLayout container = findViewById(R.id.topicContainer);
        container.removeAllViews();
        
        // Fixed: Added 'subject' argument to match StudyContentProvider.getTopics signature
        List<String> topics = StudyContentProvider.getTopics(this, subject, chapter);
        
        for (String topic : topics) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_topic, container, false);
            TextView tvName = itemView.findViewById(R.id.tvTopicName);
            tvName.setText(topic);
            
            itemView.setOnClickListener(v -> {
                // Step 3: Select Topic -> Read Notes
                Intent intent = new Intent(this, NoteDetailActivity.class);
                intent.putExtra("SUBJECT_NAME", subject);
                intent.putExtra("CHAPTER_NAME", chapter);
                intent.putExtra("TOPIC_NAME", topic);
                startActivity(intent);
            });
            container.addView(itemView);
        }
    }
}
