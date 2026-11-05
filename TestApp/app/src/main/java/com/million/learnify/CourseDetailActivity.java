package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

/**
 * CourseDetailActivity - Rich Academic Course Hub & Study Center.
 * Fully interactive with lessons navigation, bookmarking, study tools, and instructor support.
 */
public class CourseDetailActivity extends BaseActivity {

    private String subject = "Physics";
    private String grade = "10";
    private String currentUsername;

    private TextView tvCourseSubjectLabel, tvCourseTitle, tvCourseDescription, tvProgressPercent, tvInstructorNameLarge;
    private LinearProgressIndicator courseProgress;
    private MaterialButton btnContinue, btnBookmark, btnContactInstructor;
    private ExtendedFloatingActionButton fabTools;
    private TabLayout tabLayout;

    private View cardLessons, cardAssignmentCount, cardCredits, cardLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enforceRole("STUDENT");

        setContentView(R.layout.activity_course_detail);

        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = session.getString("username", "Student");

        String intentSubject = getIntent().getStringExtra("SUBJECT_NAME");
        if (intentSubject != null && !intentSubject.trim().isEmpty()) {
            subject = intentSubject.trim();
        }

        String intentGrade = getIntent().getStringExtra("GRADE");
        if (intentGrade != null && !intentGrade.trim().isEmpty()) {
            grade = intentGrade.replace("Grade-", "").replace("Grade", "").replace("-", "").trim();
        } else {
            grade = session.getString("grade", "10").replace("Grade-", "").replace("Grade", "").replace("-", "").trim();
        }

        setupToolbar();
        initViews();
        bindCourseData();
        setupActions();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        tvCourseSubjectLabel = findViewById(R.id.tvCourseSubjectLabel);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvCourseDescription = findViewById(R.id.tvCourseDescription);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvInstructorNameLarge = findViewById(R.id.tvInstructorNameLarge);
        courseProgress = findViewById(R.id.courseProgress);

        btnContinue = findViewById(R.id.btnContinue);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnContactInstructor = findViewById(R.id.btnContactInstructor);
        fabTools = findViewById(R.id.fabTools);
        tabLayout = findViewById(R.id.tabLayout);

        cardLessons = findViewById(R.id.cardLessons);
        cardAssignmentCount = findViewById(R.id.cardAssignmentCount);
        cardCredits = findViewById(R.id.cardCredits);
        cardLevel = findViewById(R.id.cardLevel);
    }

    private void bindCourseData() {
        if (tvCourseSubjectLabel != null) {
            tvCourseSubjectLabel.setText(resolveDepartment(subject));
        }

        if (tvCourseTitle != null) {
            tvCourseTitle.setText(String.format("Grade %s – %s", grade, subject));
        }

        int completed = 0;
        int total = 10;
        try {
            List<String> chapters = StudyContentProvider.getChapters(this, grade, subject);
            total = (chapters != null && !chapters.isEmpty()) ? chapters.size() : 8;
            completed = repository.getLocalDb().getCompletedTopicCountForSubject(currentUsername, grade, subject);
        } catch (Exception ignored) {}

        int percent = (total > 0) ? Math.min(100, (int) Math.round(((double) completed / total) * 100)) : 0;
        if (percent == 0 && completed == 0) {
            percent = 25; // Default progress milestone
        }

        if (courseProgress != null) courseProgress.setProgress(percent);
        if (tvProgressPercent != null) tvProgressPercent.setText(percent + "%");

        if (tvCourseDescription != null) {
            tvCourseDescription.setText(String.format(
                    "Welcome to Grade %s %s. This curriculum provides comprehensive mastery through interactive multimedia lessons, hands-on scientific investigations, and personalized AI tutoring.",
                    grade, subject));
        }

        setupDetailCard(cardLessons, String.valueOf(total), "Units");
        setupDetailCard(cardAssignmentCount, "5", "Quizzes");
        setupDetailCard(cardCredits, "4.0", "Credits");
        setupDetailCard(cardLevel, "Grade " + grade, "Level");

        updateBookmarkButtonUI();
    }

    private void setupDetailCard(View cardView, String value, String label) {
        if (cardView == null) return;
        TextView tvVal = cardView.findViewById(R.id.tvDetailValue);
        TextView tvLbl = cardView.findViewById(R.id.tvDetailLabel);
        if (tvVal != null) tvVal.setText(value);
        if (tvLbl != null) tvLbl.setText(label);
    }

    private String resolveDepartment(String subj) {
        String lower = subj.toLowerCase();
        if (lower.contains("physics") || lower.contains("chem") || lower.contains("bio")) {
            return "Department of Natural Sciences";
        } else if (lower.contains("math")) {
            return "Department of Mathematics";
        } else if (lower.contains("english")) {
            return "Department of Languages & Literature";
        } else if (lower.contains("history") || lower.contains("geography") || lower.contains("civic")) {
            return "Department of Social Sciences";
        }
        return "Academic Curriculum Department";
    }

    private void setupActions() {
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChapterListActivity.class);
                intent.putExtra("SUBJECT_NAME", subject);
                intent.putExtra("GRADE", grade);
                startActivity(intent);
            });
        }

        if (btnBookmark != null) {
            btnBookmark.setOnClickListener(v -> toggleBookmark());
        }

        if (btnContactInstructor != null) {
            btnContactInstructor.setOnClickListener(v -> showContactDialog());
        }

        if (fabTools != null) {
            fabTools.setOnClickListener(v -> showStudyToolsDialog());
        }

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    handleTabSelection(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    handleTabSelection(tab.getPosition());
                }
            });
        }
    }

    private boolean isCourseBookmarked() {
        SharedPreferences prefs = getSharedPreferences("CourseBookmarks", MODE_PRIVATE);
        return prefs.getBoolean("bookmark_" + currentUsername + "_" + subject + "_" + grade, false);
    }

    private void toggleBookmark() {
        SharedPreferences prefs = getSharedPreferences("CourseBookmarks", MODE_PRIVATE);
        String key = "bookmark_" + currentUsername + "_" + subject + "_" + grade;
        boolean currentlyBookmarked = prefs.getBoolean(key, false);
        boolean newState = !currentlyBookmarked;
        prefs.edit().putBoolean(key, newState).apply();

        updateBookmarkButtonUI();

        String message = newState ? "★ Course saved to your bookmarks!" : "Course removed from bookmarks.";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateBookmarkButtonUI() {
        if (btnBookmark == null) return;
        boolean bookmarked = isCourseBookmarked();
        if (bookmarked) {
            btnBookmark.setText("BOOKMARKED");
            btnBookmark.setTextColor(getResources().getColor(R.color.admin_green));
        } else {
            btnBookmark.setText("BOOKMARK COURSE");
            btnBookmark.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    private void showContactDialog() {
        String[] options = {
            "Ask Subject AI Tutor",
            "Email Department Instructor",
            "Community Study Group"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Contact " + subject + " Department")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent aiIntent = new Intent(this, AIChatActivity.class);
                        aiIntent.putExtra("PREFILL_PROMPT", "I have a question regarding Grade " + grade + " " + subject + ": ");
                        startActivity(aiIntent);
                    } else if (which == 1) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:support@learnify.com"));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Question about Grade " + grade + " " + subject);
                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send Email"));
                        } catch (Exception e) {
                            Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Study group discussions are active in the Community Forum.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showStudyToolsDialog() {
        String[] tools = {
            "Virtual Science Studio & Labs",
            "Flashcard Mastery Deck",
            "Unit Practice Quiz",
            "AI Homework & Concept Tutor"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Study & Learning Tools")
                .setItems(tools, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            startActivity(new Intent(this, CameraHubActivity.class));
                            break;
                        case 1:
                            startActivity(new Intent(this, FlashcardActivity.class));
                            break;
                        case 2:
                            Intent quizIntent = new Intent(this, ExamActivity.class);
                            quizIntent.putExtra("SUBJECT_NAME", subject);
                            quizIntent.putExtra("GRADE", grade);
                            startActivity(quizIntent);
                            break;
                        case 3:
                            startActivity(new Intent(this, AIChatActivity.class));
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleTabSelection(int position) {
        switch (position) {
            case 0: // Overview
                break;
            case 1: // Lessons
                Intent chaptersIntent = new Intent(this, ChapterListActivity.class);
                chaptersIntent.putExtra("SUBJECT_NAME", subject);
                chaptersIntent.putExtra("GRADE", grade);
                startActivity(chaptersIntent);
                break;
            case 2: // Assignments
                Intent taskIntent = new Intent(this, TasksActivity.class);
                startActivity(taskIntent);
                break;
            case 3: // Discussions
                Intent chatIntent = new Intent(this, AIChatActivity.class);
                chatIntent.putExtra("PREFILL_PROMPT", "Let's discuss Grade " + grade + " " + subject + ": ");
                startActivity(chatIntent);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
