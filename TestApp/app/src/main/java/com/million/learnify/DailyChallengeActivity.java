package com.million.learnify;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyChallengeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername, userGrade;
    
    // UI Elements - Top Bar
    private TextView tvTotalXP, tvStreakVal, tvTimerText;
    private CircularProgressIndicator timerProgress;
    
    // UI Elements - Content
    private LinearLayout stepMapContainer;
    private TextView tvQuestionNum, tvQuestionContent, tvExplanation, tvFeedbackTitle;
    private GridLayout optionsGrid;
    private MaterialCardView feedbackCard;
    
    // UI Elements - Bottom Strip
    private TextView tvHintCount, tvCombo, tvBonusTracker;
    private CardView bottomUtilityStrip;

    private List<DailyQuestion> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int totalXP = 0;
    private int currentStreak = 0;
    private int comboCount = 0;
    private int hintsRemaining = 3;
    private boolean[] questionResults; // null = unvisited, true = correct, false = incorrect
    
    private CountDownTimer questionTimer;
    private static final long QUESTION_TIME_LIMIT = 30000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_challenge);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");
        userGrade = dbHelper.getUserGrade(currentUsername);

        initViews();
        loadInitialStats();
        loadQuestions();
    }

    private void initViews() {
        // Top Bar
        tvTotalXP = findViewById(R.id.tvTotalXP);
        tvStreakVal = findViewById(R.id.tvStreakVal);
        tvTimerText = findViewById(R.id.tvTimerText);
        timerProgress = findViewById(R.id.timerProgress);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Content
        stepMapContainer = findViewById(R.id.stepMapContainer);
        tvQuestionNum = findViewById(R.id.tvQuestionNum);
        tvQuestionContent = findViewById(R.id.tvQuestionContent);
        optionsGrid = findViewById(R.id.optionsGrid);
        feedbackCard = findViewById(R.id.feedbackCard);
        tvExplanation = findViewById(R.id.tvExplanation);
        tvFeedbackTitle = findViewById(R.id.tvFeedbackTitle);
        
        // Bottom
        tvHintCount = findViewById(R.id.tvHintCount);
        tvCombo = findViewById(R.id.tvCombo);
        tvBonusTracker = findViewById(R.id.tvBonusTracker);
        bottomUtilityStrip = findViewById(R.id.bottomUtilityStrip);
        
        findViewById(R.id.btnHint).setOnClickListener(v -> useHint());
        findViewById(R.id.btnAvatar).setOnClickListener(v -> Toast.makeText(this, "Profile Quick Menu", Toast.LENGTH_SHORT).show());
    }

    private void loadInitialStats() {
        Cursor cursor = dbHelper.getChallengeStats(currentUsername);
        if (cursor.moveToFirst()) {
            currentStreak = cursor.getInt(cursor.getColumnIndexOrThrow("current_streak"));
            totalXP = cursor.getInt(cursor.getColumnIndexOrThrow("total_score"));
            tvStreakVal.setText("🔥 " + currentStreak);
            tvTotalXP.setText(String.valueOf(totalXP));
        }
        cursor.close();
    }

    private void loadQuestions() {
        Cursor cursor = dbHelper.getDailyQuestions(userGrade);
        while (cursor.moveToNext()) {
            questionList.add(new DailyQuestion(
                cursor.getString(cursor.getColumnIndexOrThrow("question")),
                cursor.getString(cursor.getColumnIndexOrThrow("opt_a")),
                cursor.getString(cursor.getColumnIndexOrThrow("opt_b")),
                cursor.getString(cursor.getColumnIndexOrThrow("opt_c")),
                cursor.getString(cursor.getColumnIndexOrThrow("opt_d")),
                cursor.getString(cursor.getColumnIndexOrThrow("answer"))
            ));
        }
        cursor.close();

        if (questionList.isEmpty()) {
            // Fallback questions if DB is empty
            addDummyQuestions();
        }
        
        questionResults = new boolean[questionList.size()];
        setupStepMap();
        displayQuestion(0);
    }

    private void addDummyQuestions() {
        questionList.add(new DailyQuestion("What is the powerhouse of the cell?", "Nucleus", "Ribosome", "Mitochondria", "Vacuole", "Mitochondria"));
        questionList.add(new DailyQuestion("Which planet is known as the Red Planet?", "Venus", "Mars", "Jupiter", "Saturn", "Mars"));
        questionList.add(new DailyQuestion("What is the chemical symbol for Gold?", "Ag", "Fe", "Au", "Pb", "Au"));
        questionList.add(new DailyQuestion("Who wrote 'Romeo and Juliet'?", "Charles Dickens", "William Shakespeare", "Mark Twain", "Jane Austen", "William Shakespeare"));
        questionList.add(new DailyQuestion("What is the largest ocean on Earth?", "Atlantic", "Indian", "Arctic", "Pacific", "Pacific"));
    }

    private void setupStepMap() {
        stepMapContainer.removeAllViews();
        for (int i = 0; i < questionList.size(); i++) {
            ImageView stepIcon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36));
            params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
            stepIcon.setLayoutParams(params);
            stepIcon.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            stepIcon.setImageResource(R.drawable.step_circle_default);
            
            final int index = i;
            stepIcon.setOnClickListener(v -> {
                if (index < currentQuestionIndex || (index == currentQuestionIndex && questionResults[index])) {
                    jumpToQuestion(index);
                }
            });
            
            stepMapContainer.addView(stepIcon);
        }
        updateStepMapUI();
    }

    private void updateStepMapUI() {
        for (int i = 0; i < stepMapContainer.getChildCount(); i++) {
            ImageView icon = (ImageView) stepMapContainer.getChildAt(i);
            if (i == currentQuestionIndex) {
                icon.setImageResource(R.drawable.step_circle_active);
                // Pulse animation for current
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 1.0f, 1.2f, 1.0f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 1.0f, 1.2f, 1.0f);
                scaleX.setDuration(1000);
                scaleY.setDuration(1000);
                scaleX.setRepeatCount(ValueAnimator.INFINITE);
                scaleY.setRepeatCount(ValueAnimator.INFINITE);
                scaleX.start();
                scaleY.start();
            } else if (i < currentQuestionIndex) {
                icon.setImageResource(questionResults[i] ? R.drawable.step_circle_correct : R.drawable.step_circle_incorrect);
                icon.clearAnimation();
                icon.setScaleX(1.0f);
                icon.setScaleY(1.0f);
            } else {
                icon.setImageResource(R.drawable.step_circle_default);
                icon.clearAnimation();
            }
        }
    }

    private void displayQuestion(int index) {
        currentQuestionIndex = index;
        DailyQuestion q = questionList.get(index);
        
        tvQuestionNum.setText("QUESTION " + (index + 1) + " OF " + questionList.size());
        tvQuestionContent.setText(q.question);
        feedbackCard.setVisibility(View.GONE);
        
        setupOptions(q);
        updateStepMapUI();
        startTimer();
        
        tvBonusTracker.setText("Next question -> +50 XP");
    }

    private void setupOptions(DailyQuestion q) {
        optionsGrid.removeAllViews();
        String[] options = {q.optA, q.optB, q.optC, q.optD};
        
        for (String opt : options) {
            MaterialCardView card = (MaterialCardView) LayoutInflater.from(this).inflate(R.layout.item_challenge_option, optionsGrid, false);
            TextView text = card.findViewById(R.id.tvOptionText);
            text.setText(opt);
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            card.setLayoutParams(params);
            
            card.setOnClickListener(v -> handleAnswerSelection(card, opt, q.answer));
            optionsGrid.addView(card);
        }
    }

    private void handleAnswerSelection(MaterialCardView selectedCard, String selectedText, String correctText) {
        stopTimer();
        boolean isCorrect = selectedText.equals(correctText);
        questionResults[currentQuestionIndex] = isCorrect;
        
        disableAllOptions();
        
        if (isCorrect) {
            comboCount++;
            int xpGained = 50 + (comboCount * 10);
            totalXP += xpGained;
            tvTotalXP.setText(String.valueOf(totalXP));
            
            selectedCard.setCardBackgroundColor(Color.parseColor("#065F46")); // Emerald 900
            selectedCard.setStrokeColor(Color.parseColor("#10B981")); // Emerald 500
            
            // Celebration
            if (comboCount >= 2) {
                tvCombo.setVisibility(View.VISIBLE);
                tvCombo.setText(comboCount + "x combo!");
            }
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (currentQuestionIndex < questionList.size() - 1) {
                    displayQuestion(currentQuestionIndex + 1);
                } else {
                    finishChallenge();
                }
            }, 1200);
            
        } else {
            comboCount = 0;
            tvCombo.setVisibility(View.INVISIBLE);
            
            selectedCard.setCardBackgroundColor(Color.parseColor("#7F1D1D")); // Red 900
            selectedCard.setStrokeColor(Color.parseColor("#EF4444")); // Red 500
            
            // Shake animation
            ObjectAnimator shake = ObjectAnimator.ofFloat(selectedCard, "translationX", 0, 10, -10, 10, -10, 5, -5, 0);
            shake.setDuration(500);
            shake.setInterpolator(new CycleInterpolator(1));
            shake.start();
            
            // Show correct answer explanation
            showFeedback(correctText);
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (currentQuestionIndex < questionList.size() - 1) {
                    displayQuestion(currentQuestionIndex + 1);
                } else {
                    finishChallenge();
                }
            }, 3000);
        }
    }

    private void showFeedback(String correctText) {
        feedbackCard.setVisibility(View.VISIBLE);
        tvFeedbackTitle.setText("Not quite!");
        tvFeedbackTitle.setTextColor(Color.parseColor("#EF4444"));
        tvExplanation.setText("The correct answer is: " + correctText);
        feedbackCard.setStrokeColor(Color.parseColor("#EF4444"));
    }

    private void disableAllOptions() {
        for (int i = 0; i < optionsGrid.getChildCount(); i++) {
            optionsGrid.getChildAt(i).setEnabled(false);
        }
    }

    private void startTimer() {
        if (questionTimer != null) questionTimer.cancel();
        
        timerProgress.setIndicatorColor(Color.parseColor("#10B981"));
        questionTimer = new CountDownTimer(QUESTION_TIME_LIMIT, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (millisUntilFinished * 100 / QUESTION_TIME_LIMIT);
                timerProgress.setProgress(progress);
                tvTimerText.setText(String.valueOf(millisUntilFinished / 1000));
                
                if (progress < 25) {
                    timerProgress.setIndicatorColor(Color.parseColor("#EF4444"));
                    tvTimerText.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvTimerText.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void onFinish() {
                handleAnswerSelection(new MaterialCardView(DailyChallengeActivity.this), "", questionList.get(currentQuestionIndex).answer);
            }
        }.start();
    }

    private void stopTimer() {
        if (questionTimer != null) questionTimer.cancel();
    }

    private void useHint() {
        if (hintsRemaining > 0) {
            hintsRemaining--;
            tvHintCount.setText("Hints: " + hintsRemaining);
            Toast.makeText(this, "Hint: Look for the most logical answer!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No hints left!", Toast.LENGTH_SHORT).show();
        }
    }

    private void jumpToQuestion(int index) {
        displayQuestion(index);
    }

    private void finishChallenge() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int finalStreak = currentStreak + 1;
        
        dbHelper.updateChallengeStats(currentUsername, totalXP, finalStreak, today);

        new AlertDialog.Builder(this)
                .setTitle("Challenge Complete! 🏆")
                .setMessage("Final Score: " + totalXP + " XP\nStreak: " + finalStreak + " Days!")
                .setPositiveButton("Awesome", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private static class DailyQuestion {
        String question, optA, optB, optC, optD, answer;
        DailyQuestion(String q, String a, String b, String c, String d, String ans) {
            question = q; optA = a; optB = b; optC = c; optD = d; answer = ans;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
