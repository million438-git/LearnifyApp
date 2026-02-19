package com.example.testapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.IOException;

/**
 * SubjectActivity - Fully Dynamic Course Catalog.
 * Automatically displays subjects found in the assets/subjects folder.
 */
public class SubjectActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername;
    private LinearLayout subjectContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "");

        subjectContainer = findViewById(R.id.subjectContainer);
        setupToolbar();
        loadDynamicSubjects();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Course Catalog");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void loadDynamicSubjects() {
        try {
            String[] subjects = getAssets().list("subjects");
            if (subjects != null) {
                for (String subjectName : subjects) {
                    addSubjectCard(subjectName);
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error loading subjects", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSubjectCard(String subjectName) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_subject_card_vertical, subjectContainer, false);
        
        TextView nameText = card.findViewById(R.id.subjectNameText);
        TextView statusText = card.findViewById(R.id.subjectStatusText);
        TextView emojiText = card.findViewById(R.id.tvSubjectEmoji);

        // Clean name for display (e.g., "physics" -> "Physics")
        String displayName = subjectName.substring(0, 1).toUpperCase() + subjectName.substring(1);
        
        // Special case for mathematics display
        if (subjectName.equalsIgnoreCase("mathematics") || subjectName.equalsIgnoreCase("math") || subjectName.equalsIgnoreCase("maths")) {
            displayName = "Mathematics";
        }
        
        nameText.setText(displayName);
        statusText.setText("Explore " + displayName + " Units");
        
        // Dynamic Emoji based on name
        emojiText.setText(getEmojiForSubject(subjectName));

        card.setOnClickListener(v -> {
            if (dbHelper.checkPaymentStatus(currentUsername)) {
                Intent intent = new Intent(SubjectActivity.this, ChapterListActivity.class);
                intent.putExtra("SUBJECT_NAME", subjectName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "🔐 Premium Access Required.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, PaymentActivity.class));
            }
        });

        subjectContainer.addView(card);
    }

    private String getEmojiForSubject(String name) {
        // Robust matching for both capitalized and lowercase folder names
        String lowerName = name.toLowerCase();
        switch (lowerName) {
            case "mathematics": return "📐";
            case "physics": return "⚛️";
            case "biology": return "🧬";
            case "chemistry": return "🧪";
            case "english": return "📚";
            case "history": return "📜";
            case "geography": return "🌍";
            case "scholastic aptitude": return "🧠";
            default: return "📖";
        }
    }
}
