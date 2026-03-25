package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.List;

public class TopicListActivity extends AppCompatActivity {

    private String subject, chapter;
    private List<String> topics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_list);

        subject = getIntent().getStringExtra("SUBJECT_NAME");
        chapter = getIntent().getStringExtra("CHAPTER_NAME");
        
        setupToolbar();
        loadTopics();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.topicToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(chapter);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Add a "Read All" button to the header
        ImageView ivVoiceAll = new ImageView(this);
        ivVoiceAll.setImageResource(android.R.drawable.ic_btn_speak_now);
        ivVoiceAll.setPadding(16, 16, 16, 16);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.END;
        ivVoiceAll.setLayoutParams(params);
        ivVoiceAll.setOnClickListener(v -> startContinuousReading(0));
        toolbar.addView(ivVoiceAll);
    }

    private void loadTopics() {
        LinearLayout container = findViewById(R.id.topicContainer);
        container.removeAllViews();
        
        topics = StudyContentProvider.getTopics(this, subject, chapter);
        
        for (int i = 0; i < topics.size(); i++) {
            final String topic = topics.get(i);
            final int index = i;
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_topic, container, false);
            TextView tvName = itemView.findViewById(R.id.tvTopicName);
            tvName.setText(topic);
            
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, NoteDetailActivity.class);
                intent.putExtra("SUBJECT_NAME", subject);
                intent.putExtra("CHAPTER_NAME", chapter);
                intent.putExtra("TOPIC_NAME", topic);
                intent.putStringArrayListExtra("TOPIC_LIST", new ArrayList<>(topics));
                intent.putExtra("TOPIC_INDEX", index);
                startActivity(intent);
            });
            container.addView(itemView);
        }
    }

    private void startContinuousReading(int startIndex) {
        if (topics == null || topics.isEmpty()) return;
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra("SUBJECT_NAME", subject);
        intent.putExtra("CHAPTER_NAME", chapter);
        intent.putExtra("TOPIC_NAME", topics.get(startIndex));
        intent.putStringArrayListExtra("TOPIC_LIST", new ArrayList<>(topics));
        intent.putExtra("TOPIC_INDEX", startIndex);
        intent.putExtra("AUTO_START_VOICE", true);
        startActivity(intent);
    }
}
