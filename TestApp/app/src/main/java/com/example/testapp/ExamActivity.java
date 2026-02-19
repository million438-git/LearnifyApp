package com.example.testapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExamActivity extends AppCompatActivity {

    private View layoutInfo, layoutExam;
    private TextView tvTimer, tvQuestionNumber, tvQuestionText, tvExplanation;
    private RadioGroup rgOptions;
    private RadioButton rbA, rbB, rbC, rbD;
    private View cvExplanation;
    private Button btnNext, btnPrev, btnMark;

    private List<Question> examQuestions;
    private int currentIdx = 0;
    private CountDownTimer examTimer;
    private long timeLeftInMillis = 15 * 60 * 1000; // 15 Minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

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
        // Add more questions here...
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
        String time = String.format(Locale.getDefault(), "⏱ %02d:%02d", minutes, seconds);
        tvTimer.setText(time);

        if (minutes < 1) {
            tvTimer.setTextColor(Color.RED); // Last 1 min
        } else if (minutes < 5) {
            tvTimer.setTextColor(Color.parseColor("#FF9800")); // Last 5 mins
        } else {
            tvTimer.setTextColor(Color.parseColor("#4CAF50")); // Normal
        }
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
        Toast.makeText(this, "Exam Submitted Successfully!", Toast.LENGTH_LONG).show();
        finish();
    }
}
