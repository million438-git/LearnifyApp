package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TopicListActivity - Displays curriculum lessons for a selected unit.
 * Features:
 * - Unit header with breadcrumb and lesson count.
 * - Continuous audio listen action ("Listen All").
 * - Unit Practice Quiz and STEM Virtual Lab quick launchers.
 * - Dynamic completion indicators linked to DatabaseHelper.
 * - Clean lesson navigation to NoteDetailActivity.
 * - Comprehensive accessibility and TalkBack labels.
 */
public class TopicListActivity extends BaseActivity {

    private String subject = "Mathematics";
    private String grade = "10";
    private String chapter = "UNIT-1";
    private String currentUsername;

    private List<String> topics = new ArrayList<>();
    private TextView tvUnitBreadcrumb, tvUnitHeader, tvTopicCount, tvLessonsCountPill, tvTopicEmptyState;
    private MaterialButton btnListenUnit, btnUnitQuiz, btnUnitLab;
    private LinearLayout topicContainer;

    private int accentColor;
    private int lightColor;
    private Class<?> labActivityClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        enforceRole("STUDENT");

        setContentView(R.layout.activity_topic_list);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        String s = getIntent().getStringExtra("SUBJECT_NAME");
        if (s != null && !s.trim().isEmpty()) subject = s;

        String g = getIntent().getStringExtra("GRADE");
        if (g != null && !g.trim().isEmpty()) grade = g;
        else grade = pref.getString("grade", "10");
        grade = grade.replace("Grade-", "").replace("Grade", "").replace("-", "").trim();

        String c = getIntent().getStringExtra("CHAPTER_NAME");
        if (c != null && !c.trim().isEmpty()) chapter = c;

        resolveSubjectColors();
        initViews();
        setupToolbar();
        loadTopics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTopics();
    }

    private void resolveSubjectColors() {
        String lower = subject.toLowerCase(Locale.US);
        if (lower.contains("math")) {
            accentColor = ContextCompat.getColor(this, R.color.course_math_accent);
            lightColor = ContextCompat.getColor(this, R.color.course_math_light);
            labActivityClass = null;
        } else if (lower.contains("physics")) {
            accentColor = ContextCompat.getColor(this, R.color.course_physics_accent);
            lightColor = ContextCompat.getColor(this, R.color.course_physics_light);
            labActivityClass = PhysicsLabActivity.class;
        } else if (lower.contains("chemistry")) {
            accentColor = ContextCompat.getColor(this, R.color.course_chemistry_accent);
            lightColor = ContextCompat.getColor(this, R.color.course_chemistry_light);
            labActivityClass = ChemistryLabActivity.class;
        } else if (lower.contains("biology")) {
            accentColor = ContextCompat.getColor(this, R.color.course_biology_accent);
            lightColor = ContextCompat.getColor(this, R.color.course_biology_light);
            labActivityClass = BiologyLabActivity.class;
        } else if (lower.contains("english")) {
            accentColor = ContextCompat.getColor(this, R.color.course_english_accent);
            lightColor = ContextCompat.getColor(this, R.color.course_english_light);
            labActivityClass = null;
        } else {
            accentColor = ContextCompat.getColor(this, R.color.course_aptitude_accent);
            lightColor = ContextCompat.getColor(this, R.color.course_aptitude_light);
            labActivityClass = null;
        }
    }

    private void initViews() {
        tvUnitBreadcrumb = findViewById(R.id.tvUnitBreadcrumb);
        tvUnitHeader = findViewById(R.id.tvUnitHeader);
        tvTopicCount = findViewById(R.id.tvTopicCount);
        tvLessonsCountPill = findViewById(R.id.tvLessonsCountPill);
        tvTopicEmptyState = findViewById(R.id.tvTopicEmptyState);
        btnListenUnit = findViewById(R.id.btnListenUnit);
        btnUnitQuiz = findViewById(R.id.btnUnitQuiz);
        btnUnitLab = findViewById(R.id.btnUnitLab);
        topicContainer = findViewById(R.id.topicContainer);

        if (tvUnitBreadcrumb != null) {
            tvUnitBreadcrumb.setText(subject + " • Grade " + grade);
        }

        if (tvUnitHeader != null) {
            tvUnitHeader.setText(formatUnitTitle(chapter));
        }

        if (btnListenUnit != null) {
            btnListenUnit.setOnClickListener(v -> startContinuousReading(0));
            AccessibilityUtils.setAccessibilityLabel(btnListenUnit, "Listen to unit audio reading", "Button");
        }

        if (btnUnitQuiz != null) {
            btnUnitQuiz.setOnClickListener(v -> openUnitQuiz());
            AccessibilityUtils.setAccessibilityLabel(btnUnitQuiz, "Start unit practice quiz", "Button");
        }

        if (btnUnitLab != null) {
            if (labActivityClass != null) {
                btnUnitLab.setVisibility(View.VISIBLE);
                btnUnitLab.setOnClickListener(v -> startActivity(new Intent(this, labActivityClass)));
                AccessibilityUtils.setAccessibilityLabel(btnUnitLab, "Open virtual science lab", "Button");
            } else {
                btnUnitLab.setVisibility(View.GONE);
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.topicToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(formatUnitTitle(chapter));
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void loadTopics() {
        if (topicContainer == null) return;
        topicContainer.removeAllViews();

        topics = StudyContentProvider.getTopics(this, subject, chapter, grade);

        if (topics == null || topics.isEmpty()) {
            if (tvTopicEmptyState != null) tvTopicEmptyState.setVisibility(View.VISIBLE);
            if (tvLessonsCountPill != null) tvLessonsCountPill.setText("0 Lessons");
            if (tvTopicCount != null) tvTopicCount.setText("No lesson files found in this unit");
            return;
        }

        if (tvTopicEmptyState != null) tvTopicEmptyState.setVisibility(View.GONE);
        if (tvLessonsCountPill != null) tvLessonsCountPill.setText(topics.size() + " Lessons");
        if (tvTopicCount != null) {
            tvTopicCount.setText(topics.size() + " Lessons • Ethiopian National Curriculum");
        }

        for (int i = 0; i < topics.size(); i++) {
            String topic = topics.get(i);
            boolean isCompleted = repository.getLocalDb().isLessonCompleted(
                    currentUsername, subject, grade, chapter, topic);
            addTopicCard(i + 1, topic, isCompleted, i);
        }
    }

    private void addTopicCard(int orderNumber, String topicPath, boolean isCompleted, int index) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_topic, topicContainer, false);

        TextView tvOrder = card.findViewById(R.id.tvTopicOrder);
        TextView tvName = card.findViewById(R.id.tvTopicName);
        TextView tvMeta = card.findViewById(R.id.tvTopicMeta);
        ImageView ivCheck = card.findViewById(R.id.ivTopicCompletedCheck);
        ImageView ivChevron = card.findViewById(R.id.ivTopicChevron);

        String orderString = String.format(Locale.US, "%02d", orderNumber);
        if (tvOrder != null) {
            tvOrder.setText(orderString);
            tvOrder.setTextColor(accentColor);
            tvOrder.setBackgroundTintList(ColorStateList.valueOf(lightColor));
        }

        String displayName = cleanTopicTitle(topicPath);
        if (tvName != null) {
            tvName.setText(displayName);
        }

        if (tvMeta != null) {
            tvMeta.setText(determineTopicMeta(topicPath));
        }

        if (ivCheck != null) {
            ivCheck.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
        }

        if (ivChevron != null) {
            ivChevron.setImageTintList(ColorStateList.valueOf(accentColor));
        }

        card.setOnClickListener(v -> openLesson(index));

        AccessibilityUtils.setAccessibilityLabel(card,
                displayName + ", " + (isCompleted ? "Completed" : "Not yet completed"),
                "Button");

        topicContainer.addView(card);
    }

    private void openLesson(int index) {
        if (topics == null || index < 0 || index >= topics.size()) return;
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra("SUBJECT_NAME", subject);
        intent.putExtra("GRADE", grade);
        intent.putExtra("CHAPTER_NAME", chapter);
        intent.putExtra("TOPIC_NAME", topics.get(index));
        intent.putStringArrayListExtra("TOPIC_LIST", new ArrayList<>(topics));
        intent.putExtra("TOPIC_INDEX", index);
        startActivity(intent);
    }

    private void startContinuousReading(int startIndex) {
        if (topics == null || topics.isEmpty()) {
            Toast.makeText(this, "No lessons to read in this unit.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra("SUBJECT_NAME", subject);
        intent.putExtra("GRADE", grade);
        intent.putExtra("CHAPTER_NAME", chapter);
        intent.putExtra("TOPIC_NAME", topics.get(startIndex));
        intent.putStringArrayListExtra("TOPIC_LIST", new ArrayList<>(topics));
        intent.putExtra("TOPIC_INDEX", startIndex);
        intent.putExtra("AUTO_START_VOICE", true);
        startActivity(intent);
    }

    private void openUnitQuiz() {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra("SUBJECT_NAME", subject);
        intent.putExtra("GRADE", grade);
        intent.putExtra("CHAPTER_NAME", chapter);
        // Find if any topic has exam in name
        String examTopic = null;
        for (String t : topics) {
            String lower = t.toLowerCase(Locale.US);
            if (lower.contains("exam") || lower.contains("quiz") || lower.contains("question")) {
                examTopic = t;
                break;
            }
        }
        if (examTopic != null) {
            intent.putExtra("TOPIC_NAME", examTopic);
        }
        startActivity(intent);
    }

    private String cleanTopicTitle(String topic) {
        if (topic == null) return "Lesson";
        String clean = topic;
        if (clean.contains("/")) clean = clean.substring(clean.lastIndexOf('/') + 1);
        if (clean.toLowerCase(Locale.US).endsWith(".html")) {
            clean = clean.substring(0, clean.length() - 5);
        }
        clean = clean.replace("-", " ").replace("_", " ").trim();
        return clean;
    }

    private String determineTopicMeta(String topic) {
        String lower = topic.toLowerCase(Locale.US);
        if (lower.contains("exam") || lower.contains("quiz") || lower.contains("test")) {
            return "Assessment • Practice Quiz";
        } else if (lower.contains("index") || lower.contains("overview")) {
            return "Overview • Unit Summary";
        } else if (lower.contains("lab") || lower.contains("experiment")) {
            return "Interactive • Virtual Lab";
        } else {
            return "Reading • ~8 min read";
        }
    }

    private String formatUnitTitle(String rawName) {
        if (rawName == null) return "Unit";
        String clean = rawName.replace("_", " ").replace("-", " ").trim();
        if (clean.toUpperCase(Locale.US).startsWith("UNIT")) {
            return "Unit " + clean.substring(4).trim();
        }
        if (clean.toUpperCase(Locale.US).startsWith("CHAPTER")) {
            return "Chapter " + clean.substring(7).trim();
        }
        return clean;
    }
}
