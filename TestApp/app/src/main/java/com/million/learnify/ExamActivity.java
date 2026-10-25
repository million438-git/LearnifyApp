package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ExamActivity - Professional examination and assessment engine.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC) and
 * Learnify Inclusive Learning Mode support.
 */
public class ExamActivity extends BaseActivity {

    private View layoutInfo, layoutExam;
    private TextView tvTimer, tvQuestionNumber, tvQuestionText, tvExplanation;
    private RadioGroup rgOptions;
    private RadioButton rbA, rbB, rbC, rbD;
    private View cvExplanation;
    private Button btnNext, btnPrev, btnMark;

    private List<Question> examQuestions;
    private int currentIdx = 0;
    private int score = 0;
    private CountDownTimer examTimer;
    private final long totalDurationMillis = 15 * 60 * 1000;
    private long timeLeftInMillis = totalDurationMillis; 
    private boolean isSubmitted = false;
    private String currentUsername;
    private String subjectName;

    // Milestone tracking for accessible non-flooding timer alerts
    private boolean announcedHalfTime = false;
    private boolean announced5Min = false;
    private boolean announced1Min = false;
    private boolean announced30Sec = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Student users to enter this portal.
        enforceRole("STUDENT");
        
        setContentView(R.layout.activity_exam);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "anonymous");
        subjectName = getIntent().getStringExtra("SUBJECT_NAME");
        if (subjectName == null) subjectName = "General Studies";

        initViews();
        setupToolbar();
        loadMockQuestions();

        findViewById(R.id.btnStartExam).setOnClickListener(v -> startExam());
    }

    private void initViews() {
        layoutInfo = findViewById(R.id.layoutInfo);
        layoutExam = findViewById(R.id.layoutExam);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        tvExplanation = findViewById(R.id.tvExplanation);
        rgOptions = findViewById(R.id.rgOptions);
        rbA = findViewById(R.id.rbA);
        rbB = findViewById(R.id.rbB);
        rbC = findViewById(R.id.rbC);
        rbD = findViewById(R.id.rbD);
        cvExplanation = findViewById(R.id.cvExplanation);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnMark = findViewById(R.id.btnMark);

        // Accessibility heading roles
        if (tvQuestionNumber != null) {
            AccessibilityUtils.setAsHeading(tvQuestionNumber, true);
        }
        TextView tvExamTitle = findViewById(R.id.tvExamTitleLarge);
        if (tvExamTitle != null) {
            AccessibilityUtils.setAsHeading(tvExamTitle, true);
        }

        // Option selection accessibility listener
        if (rgOptions != null) {
            rgOptions.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == -1) return;
                String optionLetter = "";
                String rawText = "";
                if (checkedId == R.id.rbA) {
                    optionLetter = "A";
                    rawText = rbA.getText().toString();
                } else if (checkedId == R.id.rbB) {
                    optionLetter = "B";
                    rawText = rbB.getText().toString();
                } else if (checkedId == R.id.rbC) {
                    optionLetter = "C";
                    rawText = rbC.getText().toString();
                } else if (checkedId == R.id.rbD) {
                    optionLetter = "D";
                    rawText = rbD.getText().toString();
                }
                String spokenOption = AccessibilityUtils.cleanMathForSpeech(rawText);
                String announcement = "Option " + optionLetter + " selected: " + spokenOption;
                AccessibilityUtils.announceForAccessibility(group, announcement);
                if (InclusiveModeManager.getInstance(this).isInclusiveModeEnabled() &&
                        InclusiveModeManager.getInstance(this).isAutoReadEnabled()) {
                    speakAnnouncement(announcement);
                }
            });
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.examToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(subjectName + " Exam");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadMockQuestions() {
        examQuestions = new ArrayList<>();
        examQuestions.add(new Question(
            "A force of 10N acts on an object of 2kg. What is the acceleration?",
            "2 m/s²", "5 m/s²", "20 m/s²", "0.2 m/s²",
            1, "Using F = ma, a = F/m = 10/2 = 5 m/s²."
        ));
        examQuestions.add(new Question(
            "Which of the following is a unit of power?",
            "Joule", "Newton", "Watt", "Pascal",
            2, "Power is the rate of doing work, measured in Watts (W)."
        ));
    }

    private void startExam() {
        layoutInfo.setVisibility(View.GONE);
        layoutExam.setVisibility(View.VISIBLE);
        displayQuestion();
        startTimer();
    }

    private void startTimer() {
        if (examTimer != null) {
            examTimer.cancel();
        }
        announcedHalfTime = false;
        announced5Min = false;
        announced1Min = false;
        announced30Sec = false;

        examTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isFinishing() || isDestroyed() || isSubmitted) {
                    cancel();
                    return;
                }
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                if (isFinishing() || isDestroyed() || isSubmitted) {
                    return;
                }
                autoSubmit();
            }
        }.start();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        tvTimer.setContentDescription(String.format(Locale.getDefault(), "%d minutes and %d seconds remaining", minutes, seconds));
        
        if (minutes < 1) tvTimer.setTextColor(Color.RED);
        else tvTimer.setTextColor(Color.parseColor("#4CAF50"));

        long secondsRemaining = timeLeftInMillis / 1000;
        if (!announcedHalfTime && timeLeftInMillis <= (totalDurationMillis / 2)) {
            announcedHalfTime = true;
            announceTimerMilestone("Half time remaining: " + minutes + " minutes.");
        } else if (!announced5Min && secondsRemaining <= 300 && secondsRemaining > 290) {
            announced5Min = true;
            announceTimerMilestone("5 minutes remaining.");
        } else if (!announced1Min && secondsRemaining <= 60 && secondsRemaining > 50) {
            announced1Min = true;
            announceTimerMilestone("Warning: 1 minute remaining.");
        } else if (!announced30Sec && secondsRemaining <= 30 && secondsRemaining > 20) {
            announced30Sec = true;
            announceTimerMilestone("Warning: 30 seconds remaining.");
        }
    }

    private void announceTimerMilestone(String message) {
        if (tvTimer != null) {
            AccessibilityUtils.announceAssertive(tvTimer, message);
        }
        if (InclusiveModeManager.getInstance(this).isInclusiveModeEnabled()) {
            speakAnnouncement(message);
        }
    }

    private void displayQuestion() {
        Question q = examQuestions.get(currentIdx);
        String qNumDisplay = "Question " + (currentIdx + 1) + "/" + examQuestions.size();
        String qNumSpoken = "Question " + (currentIdx + 1) + " of " + examQuestions.size();
        tvQuestionNumber.setText(qNumDisplay);
        tvQuestionNumber.setContentDescription(qNumSpoken);

        String spokenQuestion = AccessibilityUtils.cleanMathForSpeech(q.getQuestionText());
        tvQuestionText.setText(q.getQuestionText());
        tvQuestionText.setContentDescription(spokenQuestion);

        String cleanA = AccessibilityUtils.cleanMathForSpeech(q.getOptionA());
        String cleanB = AccessibilityUtils.cleanMathForSpeech(q.getOptionB());
        String cleanC = AccessibilityUtils.cleanMathForSpeech(q.getOptionC());
        String cleanD = AccessibilityUtils.cleanMathForSpeech(q.getOptionD());

        rbA.setText(q.getOptionA());
        rbA.setContentDescription("Option A: " + cleanA);
        rbB.setText(q.getOptionB());
        rbB.setContentDescription("Option B: " + cleanB);
        rbC.setText(q.getOptionC());
        rbC.setContentDescription("Option C: " + cleanC);
        rbD.setText(q.getOptionD());
        rbD.setContentDescription("Option D: " + cleanD);
        
        rgOptions.clearCheck();
        cvExplanation.setVisibility(View.GONE);

        btnPrev.setEnabled(currentIdx > 0);
        boolean isLast = currentIdx == examQuestions.size() - 1;
        btnNext.setText(isLast ? "FINISH" : "Next");
        btnNext.setContentDescription(isLast ? getString(R.string.acc_finish_exam) : getString(R.string.acc_next_question));

        // Accessible announcement of the new question
        String fullAnnouncement = qNumSpoken + ". " + spokenQuestion;
        AccessibilityUtils.announceForAccessibility(tvQuestionText, fullAnnouncement);
        if (InclusiveModeManager.getInstance(this).isInclusiveModeEnabled() &&
                InclusiveModeManager.getInstance(this).isAutoReadEnabled()) {
            speakAnnouncement(fullAnnouncement);
        }

        btnNext.setOnClickListener(v -> {
            checkAnswer();
            if (currentIdx < examQuestions.size() - 1) {
                currentIdx++;
                displayQuestion();
            } else {
                confirmSubmit();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIdx > 0) {
                currentIdx--;
                displayQuestion();
            }
        });
    }

    private void checkAnswer() {
        int selectedId = rgOptions.getCheckedRadioButtonId();
        int answer = -1;
        if (selectedId == R.id.rbA) answer = 0;
        else if (selectedId == R.id.rbB) answer = 1;
        else if (selectedId == R.id.rbC) answer = 2;
        else if (selectedId == R.id.rbD) answer = 3;

        if (answer == examQuestions.get(currentIdx).getCorrectOptionIndex()) {
            score++;
        }
    }

    private void confirmSubmit() {
        new AlertDialog.Builder(this)
                .setTitle("Submit Exam?")
                .setMessage("Are you sure you want to finish the exam?")
                .setPositiveButton("Submit", (dialog, which) -> autoSubmit())
                .setNegativeButton("Keep Working", null)
                .show();
    }

    private void autoSubmit() {
        if (isSubmitted || isFinishing() || isDestroyed()) {
            return;
        }
        isSubmitted = true;

        if (examTimer != null) {
            examTimer.cancel();
            examTimer = null;
        }

        int totalQuestions = examQuestions != null ? examQuestions.size() : 0;
        if (totalQuestions == 0) {
            finish();
            return;
        }

        int finalScorePercent = (int) (((float) score / totalQuestions) * 100);
        String finalScoreStr = finalScorePercent + "%";

        try {
            if (repository != null && repository.getLocalDb() != null) {
                repository.getLocalDb().saveExamResult(currentUsername, subjectName, finalScoreStr, totalQuestions, score);
            }
        } catch (Exception e) {
            android.util.Log.e("ExamActivity", "Failed to save exam result", e);
        }

        String submitAnnouncement = "Exam completed. Your score is " + finalScoreStr + ".";
        AccessibilityUtils.announceAssertive(layoutExam != null ? layoutExam : layoutInfo, submitAnnouncement);

        Intent intent = new Intent(this, CertificateActivity.class);
        intent.putExtra("SUBJECT_NAME", subjectName);
        intent.putExtra("EXAM_SCORE", finalScoreStr);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (layoutExam != null && layoutExam.getVisibility() == View.VISIBLE && !isSubmitted) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit Exam?")
                    .setMessage("Are you sure you want to leave? Your exam progress will not be saved.")
                    .setPositiveButton("Exit", (dialog, which) -> {
                        if (examTimer != null) {
                            examTimer.cancel();
                            examTimer = null;
                        }
                        finish();
                    })
                    .setNegativeButton("Continue Exam", null)
                    .show();
        } else {
            if (examTimer != null) {
                examTimer.cancel();
                examTimer = null;
            }
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (examTimer != null) {
            examTimer.cancel();
            examTimer = null;
        }
    }
}
