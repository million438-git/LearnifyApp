package com.million.learnify;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DailyChallengeActivity - 5 Questions per Day curated for students.
 * Logic: Questions are selected based on the current date to ensure 
 * all students see the same 5 questions each day.
 */
public class DailyChallengeActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername, userGrade;
    
    // UI Elements
    private TextView tvTotalXP, tvStreakVal, tvTimerText;
    private CircularProgressIndicator timerProgress;
    private LinearLayout stepMapContainer;
    private TextView tvQuestionNum, tvQuestionContent, tvExplanation, tvFeedbackTitle;
    private GridLayout optionsGrid;
    private MaterialCardView feedbackCard;
    private TextView tvHintCount, tvCombo, tvBonusTracker;

    private List<DailyQuestion> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int totalXP = 0;
    private int currentStreak = 0;
    private int comboCount = 0;
    private int hintsRemaining = 3;
    private boolean[] questionResults;
    
    private CountDownTimer questionTimer;
    private static final long QUESTION_TIME_LIMIT = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        enforceRole("STUDENT");
        setContentView(R.layout.activity_daily_challenge);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");
        userGrade = dbHelper.getUserGrade(currentUsername);

        if (isChallengeAlreadyDone()) {
            Toast.makeText(this, "You have already completed today's challenge! Come back tomorrow.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        loadInitialStats();
        loadDailyQuestions();
    }

    private boolean isChallengeAlreadyDone() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor cursor = dbHelper.getChallengeStats(currentUsername);
        boolean done = false;
        if (cursor.moveToFirst()) {
            String lastDate = cursor.getString(cursor.getColumnIndexOrThrow("last_completion_date"));
            if (today.equals(lastDate)) done = true;
        }
        cursor.close();
        return done;
    }

    private void initViews() {
        tvTotalXP = findViewById(R.id.tvTotalXP);
        tvStreakVal = findViewById(R.id.tvStreakVal);
        tvTimerText = findViewById(R.id.tvTimerText);
        timerProgress = findViewById(R.id.timerProgress);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        stepMapContainer = findViewById(R.id.stepMapContainer);
        tvQuestionNum = findViewById(R.id.tvQuestionNum);
        tvQuestionContent = findViewById(R.id.tvQuestionContent);
        optionsGrid = findViewById(R.id.optionsGrid);
        feedbackCard = findViewById(R.id.feedbackCard);
        tvExplanation = findViewById(R.id.tvExplanation);
        tvFeedbackTitle = findViewById(R.id.tvFeedbackTitle);
        
        tvHintCount = findViewById(R.id.tvHintCount);
        tvCombo = findViewById(R.id.tvCombo);
        tvBonusTracker = findViewById(R.id.tvBonusTracker);
        
        findViewById(R.id.btnHint).setOnClickListener(v -> useHint());
    }

    private void loadInitialStats() {
        Cursor cursor = dbHelper.getChallengeStats(currentUsername);
        if (cursor.moveToFirst()) {
            currentStreak = cursor.getInt(cursor.getColumnIndexOrThrow("streak"));
            totalXP = cursor.getInt(cursor.getColumnIndexOrThrow("total_xp"));
            tvStreakVal.setText("🔥 " + currentStreak);
            tvTotalXP.setText(String.valueOf(totalXP));
        }
        cursor.close();
    }

    private void loadDailyQuestions() {
        // Logic: Get current day of year to offset selection
        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        
        // Use the day of the year as a seed to pick 5 questions from the database.
        // This ensures the questions change every 24 hours (day to day).
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Count total questions available
        Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM daily_questions", null);
        int totalQuestions = 0;
        if (countCursor.moveToFirst()) totalQuestions = countCursor.getInt(0);
        countCursor.close();

        if (totalQuestions < 5) {
            Toast.makeText(this, "Daily Challenge questions are being updated...", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Calculate offset based on day of year
        int offset = (dayOfYear * 5) % totalQuestions;
        if (offset + 5 > totalQuestions) offset = 0;

        questionList.clear();
        
        // Fetch 5 questions starting from the offset
        Cursor cursor = db.rawQuery("SELECT * FROM daily_questions LIMIT 5 OFFSET ?", new String[]{String.valueOf(offset)});
        
        while (cursor.moveToNext()) {
            questionList.add(new DailyQuestion(
                cursor.getString(cursor.getColumnIndexOrThrow("question")),
                cursor.getString(cursor.getColumnIndexOrThrow("option_a")),
                cursor.getString(cursor.getColumnIndexOrThrow("option_b")),
                cursor.getString(cursor.getColumnIndexOrThrow("option_c")),
                cursor.getString(cursor.getColumnIndexOrThrow("option_d")),
                cursor.getString(cursor.getColumnIndexOrThrow("correct_option")),
                cursor.getString(cursor.getColumnIndexOrThrow("explanation"))
            ));
        }
        cursor.close();
        
        if (questionList.isEmpty()) {
            Toast.makeText(this, "No questions found for today. Please try again later.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        questionResults = new boolean[questionList.size()];
        setupStepMap();
        displayQuestion(0);
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
            stepMapContainer.addView(stepIcon);
        }
        updateStepMapUI();
    }

    private void updateStepMapUI() {
        for (int i = 0; i < stepMapContainer.getChildCount(); i++) {
            ImageView icon = (ImageView) stepMapContainer.getChildAt(i);
            if (i == currentQuestionIndex) {
                icon.setImageResource(R.drawable.step_circle_active);
            } else if (i < currentQuestionIndex) {
                icon.setImageResource(questionResults[i] ? R.drawable.step_circle_correct : R.drawable.step_circle_incorrect);
            } else {
                icon.setImageResource(R.drawable.step_circle_default);
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
        
        tvBonusTracker.setText("Success -> +50 XP");
    }

    private void setupOptions(DailyQuestion q) {
        optionsGrid.removeAllViews();
        String[] opts = {q.optA, q.optB, q.optC, q.optD};
        String[] letters = {"A", "B", "C", "D"};
        
        for (int i=0; i<4; i++) {
            final String textValue = opts[i];
            final String letter = letters[i];
            
            MaterialCardView card = (MaterialCardView) LayoutInflater.from(this).inflate(R.layout.item_challenge_option, optionsGrid, false);
            TextView text = card.findViewById(R.id.tvOptionText);
            text.setText(letter + ") " + textValue);
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            card.setLayoutParams(params);
            
            card.setOnClickListener(v -> handleAnswerSelection(card, letter, q.answer, q.explanation));
            optionsGrid.addView(card);
        }
    }

    private void handleAnswerSelection(MaterialCardView selectedCard, String selectedLetter, String correctLetter, String explanation) {
        stopTimer();
        boolean isCorrect = selectedLetter.equalsIgnoreCase(correctLetter);
        questionResults[currentQuestionIndex] = isCorrect;
        
        disableAllOptions();
        
        if (isCorrect) {
            comboCount++;
            int xpGained = 50 + (comboCount * 10);
            totalXP += xpGained;
            tvTotalXP.setText(String.valueOf(totalXP));
            selectedCard.setCardBackgroundColor(Color.parseColor("#065F46"));
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (currentQuestionIndex < questionList.size() - 1) displayQuestion(currentQuestionIndex + 1);
                else finishChallenge();
            }, 1000);
            
        } else {
            comboCount = 0;
            selectedCard.setCardBackgroundColor(Color.parseColor("#7F1D1D"));
            showFeedback(correctLetter, explanation);
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (currentQuestionIndex < questionList.size() - 1) displayQuestion(currentQuestionIndex + 1);
                else finishChallenge();
            }, 4000);
        }
    }

    private void showFeedback(String correctLetter, String explanation) {
        feedbackCard.setVisibility(View.VISIBLE);
        tvFeedbackTitle.setText("Review Required");
        tvExplanation.setText("Correct Answer: " + correctLetter + "\n\n" + explanation);
    }

    private void disableAllOptions() {
        for (int i = 0; i < optionsGrid.getChildCount(); i++) optionsGrid.getChildAt(i).setEnabled(false);
    }

    private void startTimer() {
        if (questionTimer != null) questionTimer.cancel();
        questionTimer = new CountDownTimer(QUESTION_TIME_LIMIT, 100) {
            @Override
            public void onTick(long ms) {
                int p = (int) (ms * 100 / QUESTION_TIME_LIMIT);
                timerProgress.setProgress(p);
                tvTimerText.setText(String.valueOf(ms / 1000));
            }
            @Override
            public void onFinish() {
                handleAnswerSelection(new MaterialCardView(DailyChallengeActivity.this), "NONE", questionList.get(currentQuestionIndex).answer, questionList.get(currentQuestionIndex).explanation);
            }
        }.start();
    }

    private void stopTimer() { if (questionTimer != null) questionTimer.cancel(); }

    private void useHint() {
        if (hintsRemaining > 0) {
            hintsRemaining--;
            tvHintCount.setText("Hints: " + hintsRemaining);
            Toast.makeText(this, "Focus on the core concept!", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishChallenge() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dbHelper.updateChallengeStats(currentUsername, totalXP, currentStreak + 1, today);
        new AlertDialog.Builder(this)
                .setTitle("Daily Goal Achieved! 🏆")
                .setMessage("Total XP: " + totalXP + "\nStreak: " + (currentStreak + 1) + " Days")
                .setPositiveButton("Finish", (d, w) -> finish())
                .setCancelable(false).show();
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }

    private static class DailyQuestion {
        String question, optA, optB, optC, optD, answer, explanation;
        DailyQuestion(String q, String a, String b, String c, String d, String ans, String expl) {
            question = q; optA = a; optB = b; optC = c; optD = d; answer = ans; explanation = expl;
        }
    }

    @Override
    protected void onDestroy() { super.onDestroy(); stopTimer(); }
}
