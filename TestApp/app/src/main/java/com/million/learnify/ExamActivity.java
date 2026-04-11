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
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
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
    private long timeLeftInMillis = 15 * 60 * 1000; 
    private String currentUsername;
    private String subjectName;

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
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.examToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(subjectName + " Exam");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
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
        examTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                autoSubmit();
            }
        }.start();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "⏱ %02d:%02d", minutes, seconds));
        if (minutes < 1) tvTimer.setTextColor(Color.RED);
        else tvTimer.setTextColor(Color.parseColor("#4CAF50"));
    }

    private void displayQuestion() {
        Question q = examQuestions.get(currentIdx);
        tvQuestionNumber.setText("Question " + (currentIdx + 1) + "/" + examQuestions.size());
        tvQuestionText.setText(q.getQuestionText());
        rbA.setText(q.getOptionA());
        rbB.setText(q.getOptionB());
        rbC.setText(q.getOptionC());
        rbD.setText(q.getOptionD());
        
        rgOptions.clearCheck();
        cvExplanation.setVisibility(View.GONE);

        btnPrev.setEnabled(currentIdx > 0);
        btnNext.setText(currentIdx == examQuestions.size() - 1 ? "FINISH" : "Next");

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
        if (examTimer != null) examTimer.cancel();
        
        int finalScorePercent = (int) (((float) score / examQuestions.size()) * 100);
        String finalScoreStr = finalScorePercent + "%";
        
        repository.getLocalDb().saveExamResult(currentUsername, subjectName, finalScoreStr, examQuestions.size(), score);

        Intent intent = new Intent(this, CertificateActivity.class);
        intent.putExtra("SUBJECT_NAME", subjectName);
        intent.putExtra("EXAM_SCORE", finalScoreStr);
        startActivity(intent);
        finish();
    }
}
