package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

/**
 * ChapterListActivity - Displays curriculum units for a selected subject and grade.
 * Features:
 * - Dynamic subject header with grade pill and overall progress.
 * - Real unit & lesson counts discovered from educational assets.
 * - Real live completion tracking per unit from DatabaseHelper.
 * - Virtual Science Lab shortcut for STEM subjects.
 * - Comprehensive accessibility and TalkBack support.
 */
public class ChapterListActivity extends BaseActivity {

    private String subject = "Mathematics";
    private String grade = "10";
    private String currentUsername;

    private TextView tvCourseNameHeader, tvCourseDetails, tvCourseGradeBadge;
    private TextView tvSubjectOverallProgressText, tvUnitsCountPill, tvUnitsEmptyState;
    private LinearProgressIndicator subjectOverallProgressBar;
    private MaterialButton btnHeroLabAction;
    private LinearLayout chapterContainer;

    private int accentColor;
    private int lightColor;
    private Class<?> labActivityClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        enforceRole("STUDENT");

        setContentView(R.layout.activity_chapter_list);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        String intentSubject = getIntent().getStringExtra("SUBJECT_NAME");
        if (intentSubject != null && !intentSubject.trim().isEmpty()) {
            subject = intentSubject;
        }

        String intentGrade = getIntent().getStringExtra("GRADE");
        if (intentGrade != null && !intentGrade.trim().isEmpty()) {
            grade = intentGrade;
        } else {
            grade = pref.getString("grade", "10");
        }
        grade = grade.replace("Grade-", "").replace("Grade", "").replace("-", "").trim();

        resolveSubjectColors();
        initViews();
        setupToolbar();
        loadChapters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChapters();
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
        tvCourseNameHeader = findViewById(R.id.tvCourseNameHeader);
        tvCourseDetails = findViewById(R.id.tvCourseDetails);
        tvCourseGradeBadge = findViewById(R.id.tvCourseGradeBadge);
        tvSubjectOverallProgressText = findViewById(R.id.tvSubjectOverallProgressText);
        subjectOverallProgressBar = findViewById(R.id.subjectOverallProgressBar);
        btnHeroLabAction = findViewById(R.id.btnHeroLabAction);
        tvUnitsCountPill = findViewById(R.id.tvUnitsCountPill);
        tvUnitsEmptyState = findViewById(R.id.tvUnitsEmptyState);
        chapterContainer = findViewById(R.id.chapterContainer);

        if (tvCourseGradeBadge != null) {
            tvCourseGradeBadge.setText("Grade " + grade);
        }

        if (btnHeroLabAction != null) {
            if (labActivityClass != null) {
                btnHeroLabAction.setVisibility(View.VISIBLE);
                btnHeroLabAction.setOnClickListener(v -> startActivity(new Intent(this, labActivityClass)));
                AccessibilityUtils.setAccessibilityLabel(btnHeroLabAction, "Launch Virtual Lab for " + subject, "Button");
            } else {
                btnHeroLabAction.setVisibility(View.GONE);
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.chapterToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(subject);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void loadChapters() {
        if (chapterContainer == null) return;
        chapterContainer.removeAllViews();

        List<String> chapters = StudyContentProvider.getChapters(this, subject, grade);

        if (chapters.isEmpty()) {
            if (tvUnitsEmptyState != null) tvUnitsEmptyState.setVisibility(View.VISIBLE);
            if (tvUnitsCountPill != null) tvUnitsCountPill.setText("0 Units");
            if (tvCourseNameHeader != null) tvCourseNameHeader.setText(subject);
            if (tvCourseDetails != null) tvCourseDetails.setText("Grade " + grade + " • No units found");
            if (subjectOverallProgressBar != null) subjectOverallProgressBar.setProgress(0);
            if (tvSubjectOverallProgressText != null) tvSubjectOverallProgressText.setText("0% Done");
            return;
        }

        if (tvUnitsEmptyState != null) tvUnitsEmptyState.setVisibility(View.GONE);
        if (tvUnitsCountPill != null) tvUnitsCountPill.setText(chapters.size() + " Units");

        int totalSubjectLessons = 0;
        int completedSubjectLessons = 0;

        for (int i = 0; i < chapters.size(); i++) {
            String chapter = chapters.get(i);
            List<String> topics = StudyContentProvider.getTopics(this, subject, chapter, grade);
            int lessonCount = topics.size();
            totalSubjectLessons += lessonCount;

            int completedInUnit = repository.getLocalDb().getCompletedLessonsCountInChapter(
                    currentUsername, subject, grade, chapter);
            completedSubjectLessons += completedInUnit;

            addChapterCard(i + 1, chapter, lessonCount, completedInUnit);
        }

        // Update Hero Details & Progress
        if (tvCourseNameHeader != null) {
            tvCourseNameHeader.setText(subject);
        }
        if (tvCourseDetails != null) {
            tvCourseDetails.setText("Grade " + grade + " • " + chapters.size() + " Units • "
                    + totalSubjectLessons + " Lessons • Ethiopian National Curriculum");
        }

        if (totalSubjectLessons > 0) {
            int overallPercent = Math.min(100, (completedSubjectLessons * 100) / totalSubjectLessons);
            if (subjectOverallProgressBar != null) {
                subjectOverallProgressBar.setProgress(overallPercent);
                subjectOverallProgressBar.setIndicatorColor(accentColor);
            }
            if (tvSubjectOverallProgressText != null) {
                tvSubjectOverallProgressText.setText(overallPercent + "% (" + completedSubjectLessons + "/" + totalSubjectLessons + ")");
            }
        } else {
            if (subjectOverallProgressBar != null) subjectOverallProgressBar.setProgress(0);
            if (tvSubjectOverallProgressText != null) tvSubjectOverallProgressText.setText("0% Done");
        }
    }

    private void addChapterCard(int unitNumber, String rawChapterName, int lessonCount, int completedCount) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_chapter, chapterContainer, false);

        TextView tvNumber = card.findViewById(R.id.tvChapterNumber);
        TextView tvName = card.findViewById(R.id.tvChapterName);
        TextView tvMeta = card.findViewById(R.id.tvChapterMeta);
        LinearProgressIndicator progressBar = card.findViewById(R.id.chapterProgressBar);
        TextView tvStatus = card.findViewById(R.id.tvChapterProgressStatus);
        ImageView ivChevron = card.findViewById(R.id.ivChapterChevron);

        String numberString = String.format(Locale.US, "%02d", unitNumber);
        if (tvNumber != null) {
            tvNumber.setText(numberString);
            tvNumber.setTextColor(accentColor);
            tvNumber.setBackgroundTintList(ColorStateList.valueOf(lightColor));
        }

        String formattedName = formatUnitTitle(rawChapterName, unitNumber);
        if (tvName != null) {
            tvName.setText(formattedName);
        }

        if (tvMeta != null) {
            tvMeta.setText(lessonCount + " Lessons • Ethiopian Curriculum");
        }

        int percent = lessonCount > 0 ? Math.min(100, (completedCount * 100) / lessonCount) : 0;
        if (progressBar != null) {
            progressBar.setProgress(percent);
            progressBar.setIndicatorColor(accentColor);
        }

        if (tvStatus != null) {
            if (completedCount >= lessonCount && lessonCount > 0) {
                tvStatus.setText("Completed (" + completedCount + "/" + lessonCount + ")");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.course_success));
            } else {
                tvStatus.setText(completedCount + "/" + lessonCount);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.course_text_secondary));
            }
        }

        if (ivChevron != null) {
            ivChevron.setImageTintList(ColorStateList.valueOf(accentColor));
        }

        card.setOnClickListener(v -> {
            Intent intent = new Intent(ChapterListActivity.this, TopicListActivity.class);
            intent.putExtra("SUBJECT_NAME", subject);
            intent.putExtra("GRADE", grade);
            intent.putExtra("CHAPTER_NAME", rawChapterName);
            startActivity(intent);
        });

        AccessibilityUtils.setAccessibilityLabel(card,
                formattedName + ", " + lessonCount + " lessons, " + completedCount + " completed",
                "Button");

        chapterContainer.addView(card);
    }

    private String formatUnitTitle(String rawName, int index) {
        if (rawName == null) return "Unit " + index;
        String clean = rawName.replace("_", " ").replace("-", " ").trim();
        if (clean.toUpperCase(Locale.US).startsWith("UNIT")) {
            return "Unit " + clean.substring(4).trim();
        }
        if (clean.toUpperCase(Locale.US).startsWith("CHAPTER")) {
            return "Chapter " + clean.substring(7).trim();
        }
        return "Unit " + index + ": " + clean;
    }
}
