package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SubjectActivity - Premium Academic Library & Courses Experience.
 * Features:
 * - Grade Selection (Grades 9-12) with instant dynamic catalog updating.
 * - Dynamic subject curriculum stats (real units & lessons from assets).
 * - Real persistent student progress tracking via DatabaseHelper.
 * - Subject brand design system with distinct color identities.
 * - Integrated virtual lab shortcuts for STEM subjects.
 * - Full accessibility and TalkBack navigation support.
 */
public class SubjectActivity extends BaseActivity {

    private static final String TAG = "SubjectActivity";
    private static final String PREF_SELECTED_GRADE = "learnify_selected_course_grade";

    private String currentUsername;
    private String selectedGrade = "10";
    private LinearLayout subjectContainer;
    private TextView tvSubjectEmptyState;
    private TextView tvHeroCurriculumBadge;
    private TextView tvSubjectCountBadge;
    private TextView tvGradeOverallProgressText;
    private LinearProgressIndicator gradeOverallProgressBar;

    private TextView chipGrade9, chipGrade10, chipGrade11, chipGrade12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security: Student or Admin portal access
        enforceRole("STUDENT");

        setContentView(R.layout.activity_subject);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        // Restore user's grade or enrolled grade
        String enrolledGrade = pref.getString("grade", "10");
        SharedPreferences coursePref = getSharedPreferences(PREF_SELECTED_GRADE, MODE_PRIVATE);
        selectedGrade = getIntent().getStringExtra("GRADE");
        if (selectedGrade == null || selectedGrade.trim().isEmpty()) {
            selectedGrade = coursePref.getString("grade", enrolledGrade);
        }
        normalizeSelectedGrade();

        initViews();
        setupToolbar();
        setupGradeChips();
        refreshCurriculumCatalog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh progress when returning from lessons or assessments
        refreshCurriculumCatalog();
    }

    private void normalizeSelectedGrade() {
        if (selectedGrade == null) selectedGrade = "10";
        selectedGrade = selectedGrade.replace("Grade-", "").replace("Grade", "").replace("-", "").trim();
        if (!Arrays.asList("9", "10", "11", "12").contains(selectedGrade)) {
            selectedGrade = "10";
        }
    }

    private void initViews() {
        subjectContainer = findViewById(R.id.subjectContainer);
        tvSubjectEmptyState = findViewById(R.id.tvSubjectEmptyState);
        tvHeroCurriculumBadge = findViewById(R.id.tvHeroCurriculumBadge);
        tvSubjectCountBadge = findViewById(R.id.tvSubjectCountBadge);
        tvGradeOverallProgressText = findViewById(R.id.tvGradeOverallProgressText);
        gradeOverallProgressBar = findViewById(R.id.gradeOverallProgressBar);

        chipGrade9 = findViewById(R.id.chipGrade9);
        chipGrade10 = findViewById(R.id.chipGrade10);
        chipGrade11 = findViewById(R.id.chipGrade11);
        chipGrade12 = findViewById(R.id.chipGrade12);

        View cardVirtualLabs = findViewById(R.id.cardVirtualLabsShortcut);
        if (cardVirtualLabs != null) {
            cardVirtualLabs.setOnClickListener(v -> startActivity(new Intent(this, CameraHubActivity.class)));
            AccessibilityUtils.setAccessibilityLabel(cardVirtualLabs, "Explore Virtual Science Labs", "Button");
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Academic Courses");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupGradeChips() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.chipGrade9) selectedGrade = "9";
            else if (id == R.id.chipGrade10) selectedGrade = "10";
            else if (id == R.id.chipGrade11) selectedGrade = "11";
            else if (id == R.id.chipGrade12) selectedGrade = "12";

            getSharedPreferences(PREF_SELECTED_GRADE, MODE_PRIVATE)
                    .edit()
                    .putString("grade", selectedGrade)
                    .apply();

            updateGradeChipStyles();
            refreshCurriculumCatalog();
            AccessibilityUtils.announceForAccessibility(v, "Switched to Grade " + selectedGrade + " curriculum");
        };

        if (chipGrade9 != null) chipGrade9.setOnClickListener(listener);
        if (chipGrade10 != null) chipGrade10.setOnClickListener(listener);
        if (chipGrade11 != null) chipGrade11.setOnClickListener(listener);
        if (chipGrade12 != null) chipGrade12.setOnClickListener(listener);

        updateGradeChipStyles();
    }

    private void updateGradeChipStyles() {
        setChipStyle(chipGrade9, "9".equals(selectedGrade));
        setChipStyle(chipGrade10, "10".equals(selectedGrade));
        setChipStyle(chipGrade11, "11".equals(selectedGrade));
        setChipStyle(chipGrade12, "12".equals(selectedGrade));
    }

    private void setChipStyle(TextView chip, boolean isSelected) {
        if (chip == null) return;
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_grade_chip_selected);
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundResource(R.drawable.bg_grade_chip_unselected);
            chip.setTextColor(ContextCompat.getColor(this, R.color.course_text_primary));
        }
    }

    private void refreshCurriculumCatalog() {
        if (tvHeroCurriculumBadge != null) {
            tvHeroCurriculumBadge.setText("Grade " + selectedGrade + " Curriculum");
        }

        if (subjectContainer == null) return;
        subjectContainer.removeAllViews();

        List<String> discoveredSubjects = getSubjectListForGrade(selectedGrade);

        if (discoveredSubjects.isEmpty()) {
            if (tvSubjectEmptyState != null) tvSubjectEmptyState.setVisibility(View.VISIBLE);
            if (tvSubjectCountBadge != null) tvSubjectCountBadge.setText("0 Subjects");
            if (gradeOverallProgressBar != null) gradeOverallProgressBar.setProgress(0);
            if (tvGradeOverallProgressText != null) tvGradeOverallProgressText.setText("0% Done");
            return;
        }

        if (tvSubjectEmptyState != null) tvSubjectEmptyState.setVisibility(View.GONE);
        if (tvSubjectCountBadge != null) {
            tvSubjectCountBadge.setText(discoveredSubjects.size() + " Subjects");
        }

        int totalGradeLessons = 0;
        int completedGradeLessons = 0;

        for (String subjectName : discoveredSubjects) {
            SubjectMeta meta = analyzeSubject(subjectName, selectedGrade);
            totalGradeLessons += meta.totalLessons;
            completedGradeLessons += meta.completedLessons;
            addSubjectCard(meta);
        }

        // Update Overall Grade Progress
        if (totalGradeLessons > 0) {
            int gradePercent = Math.min(100, (completedGradeLessons * 100) / totalGradeLessons);
            if (gradeOverallProgressBar != null) gradeOverallProgressBar.setProgress(gradePercent);
            if (tvGradeOverallProgressText != null) {
                tvGradeOverallProgressText.setText(gradePercent + "% (" + completedGradeLessons + "/" + totalGradeLessons + ")");
            }
        } else {
            if (gradeOverallProgressBar != null) gradeOverallProgressBar.setProgress(0);
            if (tvGradeOverallProgressText != null) tvGradeOverallProgressText.setText("0% Done");
        }
    }

    private List<String> getSubjectListForGrade(String grade) {
        List<String> result = new ArrayList<>();
        try {
            String[] subjects = getAssets().list("subjects");
            if (subjects != null) {
                // Canonical subject order
                List<String> preferredOrder = Arrays.asList(
                        "Mathematics", "Physics", "Chemistry", "Biology", "English", "Scholastic Aptitude");
                for (String preferred : preferredOrder) {
                    for (String subject : subjects) {
                        if (subject.equalsIgnoreCase(preferred) && hasContentForGrade(subject, grade)) {
                            result.add(subject);
                            break;
                        }
                    }
                }
                // Any extra subjects not in preferred list
                for (String subject : subjects) {
                    if (!subject.contains(".") && !result.contains(subject) && hasContentForGrade(subject, grade)) {
                        result.add(subject);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error listing subjects from assets", e);
        }
        return result;
    }

    private boolean hasContentForGrade(String subject, String grade) {
        List<String> chapters = StudyContentProvider.getChapters(this, subject, grade);
        return chapters != null && !chapters.isEmpty();
    }

    private SubjectMeta analyzeSubject(String subjectName, String grade) {
        SubjectMeta meta = new SubjectMeta();
        meta.subjectName = subjectName;
        meta.displayName = subjectName.substring(0, 1).toUpperCase() + subjectName.substring(1);
        meta.grade = grade;

        List<String> chapters = StudyContentProvider.getChapters(this, subjectName, grade);
        meta.unitCount = chapters.size();

        int totalLessons = 0;
        for (String chapter : chapters) {
            List<String> topics = StudyContentProvider.getTopics(this, subjectName, chapter, grade);
            totalLessons += topics.size();
        }
        meta.totalLessons = totalLessons;

        // Query completion
        meta.completedLessons = repository.getLocalDb().getCompletedLessonsCount(currentUsername, subjectName, grade);
        meta.progressPercent = meta.totalLessons > 0
                ? Math.min(100, (meta.completedLessons * 100) / meta.totalLessons)
                : 0;

        configureSubjectDesign(meta);
        return meta;
    }

    private void configureSubjectDesign(SubjectMeta meta) {
        String lower = meta.subjectName.toLowerCase(java.util.Locale.US);
        if (lower.contains("math")) {
            meta.accentColor = ContextCompat.getColor(this, R.color.course_math_accent);
            meta.lightColor = ContextCompat.getColor(this, R.color.course_math_light);
            meta.iconRes = R.drawable.ic_subject_math;
            meta.badgeCode = "MTH";
            meta.hasLab = false;
        } else if (lower.contains("physics")) {
            meta.accentColor = ContextCompat.getColor(this, R.color.course_physics_accent);
            meta.lightColor = ContextCompat.getColor(this, R.color.course_physics_light);
            meta.iconRes = R.drawable.ic_subject_physics;
            meta.badgeCode = "PHY";
            meta.hasLab = true;
            meta.labActivityClass = PhysicsLabActivity.class;
        } else if (lower.contains("chemistry")) {
            meta.accentColor = ContextCompat.getColor(this, R.color.course_chemistry_accent);
            meta.lightColor = ContextCompat.getColor(this, R.color.course_chemistry_light);
            meta.iconRes = R.drawable.ic_subject_chemistry;
            meta.badgeCode = "CHM";
            meta.hasLab = true;
            meta.labActivityClass = ChemistryLabActivity.class;
        } else if (lower.contains("biology")) {
            meta.accentColor = ContextCompat.getColor(this, R.color.course_biology_accent);
            meta.lightColor = ContextCompat.getColor(this, R.color.course_biology_light);
            meta.iconRes = R.drawable.ic_subject_biology;
            meta.badgeCode = "BIO";
            meta.hasLab = true;
            meta.labActivityClass = BiologyLabActivity.class;
        } else if (lower.contains("english")) {
            meta.accentColor = ContextCompat.getColor(this, R.color.course_english_accent);
            meta.lightColor = ContextCompat.getColor(this, R.color.course_english_light);
            meta.iconRes = R.drawable.ic_subject_english;
            meta.badgeCode = "ENG";
            meta.hasLab = false;
        } else {
            meta.accentColor = ContextCompat.getColor(this, R.color.course_aptitude_accent);
            meta.lightColor = ContextCompat.getColor(this, R.color.course_aptitude_light);
            meta.iconRes = R.drawable.ic_subject_aptitude;
            meta.badgeCode = "SAT";
            meta.hasLab = false;
        }
    }

    private void addSubjectCard(SubjectMeta meta) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_subject_card_vertical, subjectContainer, false);

        FrameLayout flIconContainer = card.findViewById(R.id.flSubjectIconContainer);
        ImageView ivIcon = card.findViewById(R.id.ivSubjectIcon);
        TextView tvEmoji = card.findViewById(R.id.tvSubjectEmoji);
        TextView nameText = card.findViewById(R.id.subjectNameText);
        TextView statusText = card.findViewById(R.id.subjectStatusText);
        LinearProgressIndicator progressBar = card.findViewById(R.id.subjectProgressBar);
        TextView tvProgressPercent = card.findViewById(R.id.tvSubjectProgressPercent);
        MaterialButton btnViewUnits = card.findViewById(R.id.btnViewUnits);
        MaterialButton btnLab = card.findViewById(R.id.btnLabShortcut);
        ImageView ivLock = card.findViewById(R.id.ivSubjectLock);

        nameText.setText(meta.displayName);

        String detailText = meta.unitCount + " Units • " + meta.totalLessons + " Lessons";
        statusText.setText(detailText);

        // Icon & background colors
        if (flIconContainer != null) {
            flIconContainer.setBackgroundTintList(ColorStateList.valueOf(meta.lightColor));
        }
        if (ivIcon != null) {
            ivIcon.setImageResource(meta.iconRes);
            ivIcon.setImageTintList(ColorStateList.valueOf(meta.accentColor));
            ivIcon.setVisibility(View.VISIBLE);
        }
        if (tvEmoji != null) tvEmoji.setVisibility(View.GONE);

        // Progress
        if (progressBar != null) {
            progressBar.setProgress(meta.progressPercent);
            progressBar.setIndicatorColor(meta.accentColor);
        }
        if (tvProgressPercent != null) {
            tvProgressPercent.setText(meta.progressPercent + "% (" + meta.completedLessons + "/" + meta.totalLessons + ")");
        }

        // Primary action button styling
        if (btnViewUnits != null) {
            btnViewUnits.setBackgroundTintList(ColorStateList.valueOf(meta.accentColor));
            btnViewUnits.setOnClickListener(v -> openSubjectUnits(meta));
        }

        // Lab shortcut button
        if (meta.hasLab && btnLab != null) {
            btnLab.setVisibility(View.VISIBLE);
            btnLab.setTextColor(meta.accentColor);
            btnLab.setIconTint(ColorStateList.valueOf(meta.accentColor));
            btnLab.setOnClickListener(v -> {
                if (meta.labActivityClass != null) {
                    startActivity(new Intent(SubjectActivity.this, meta.labActivityClass));
                }
            });
        } else if (btnLab != null) {
            btnLab.setVisibility(View.GONE);
        }

        // Entire card tap
        card.setOnClickListener(v -> openSubjectUnits(meta));

        // Accessibility
        AccessibilityUtils.setAccessibilityLabel(card,
                meta.displayName + ", Grade " + meta.grade + ", " + meta.unitCount + " Units, "
                        + meta.totalLessons + " Lessons, " + meta.progressPercent + " percent completed",
                "Card");

        subjectContainer.addView(card);
    }

    private void openSubjectUnits(SubjectMeta meta) {
        boolean hasAccess = repository.getLocalDb().hasPremiumAccess(currentUsername, selectedGrade, meta.subjectName);
        if (hasAccess) {
            Intent intent = new Intent(SubjectActivity.this, ChapterListActivity.class);
            intent.putExtra("SUBJECT_NAME", meta.subjectName);
            intent.putExtra("GRADE", selectedGrade);
            startActivity(intent);
        } else {
            if (repository.getLocalDb().hasPremiumAccess(currentUsername, selectedGrade, null)) {
                Toast.makeText(this, "Your active plan covers basic courses. Upgrade to access " + meta.displayName + "!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Access Restricted. Please enroll or upgrade to view " + meta.displayName + ".", Toast.LENGTH_LONG).show();
            }
            startActivity(new Intent(this, PaymentActivity.class));
        }
    }

    private static class SubjectMeta {
        String subjectName;
        String displayName;
        String grade;
        int unitCount;
        int totalLessons;
        int completedLessons;
        int progressPercent;
        int accentColor;
        int lightColor;
        int iconRes;
        String badgeCode;
        boolean hasLab;
        Class<?> labActivityClass;
    }
}
