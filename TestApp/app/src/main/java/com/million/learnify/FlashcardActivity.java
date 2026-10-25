package com.million.learnify;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * FlashcardActivity - Spaced Repetition Mastery Tool.
 * Implements a simplified SM-2 algorithm for effective memorization.
 */
public class FlashcardActivity extends BaseActivity {

    private TextView tvQuestion, tvAnswer, tvSubjectLabel, tvProgress;
    private MaterialCardView cardFlashcard;
    private MaterialButton btnShowAnswer, btnEasy, btnGood, btnHard, btnAgain;
    
    private List<FlashcardModel> reviewList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isAnswerVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        initViews();
        loadFlashcards();
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tvFlashcardQuestion);
        tvAnswer = findViewById(R.id.tvFlashcardAnswer);
        tvSubjectLabel = findViewById(R.id.tvFlashcardSubject);
        tvProgress = findViewById(R.id.tvFlashcardProgress);
        cardFlashcard = findViewById(R.id.cardFlashcard);
        
        btnShowAnswer = findViewById(R.id.btnShowAnswer);
        btnEasy = findViewById(R.id.btnEasy);
        btnGood = findViewById(R.id.btnGood);
        btnHard = findViewById(R.id.btnHard);
        btnAgain = findViewById(R.id.btnAgain);

        btnShowAnswer.setOnClickListener(v -> showAnswer());
        
        btnAgain.setOnClickListener(v -> processResponse(0));
        btnHard.setOnClickListener(v -> processResponse(2));
        btnGood.setOnClickListener(v -> processResponse(4));
        btnEasy.setOnClickListener(v -> processResponse(5));
    }

    private void loadFlashcards() {
        String user = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", "Student");
        Cursor cursor = repository.getLocalDb().getFlashcardsForReview(user);
        
        reviewList.clear();
        if (cursor.moveToFirst()) {
            do {
                FlashcardModel fm = new FlashcardModel();
                fm.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                fm.question = cursor.getString(cursor.getColumnIndexOrThrow("question"));
                fm.answer = cursor.getString(cursor.getColumnIndexOrThrow("answer"));
                fm.subject = cursor.getString(cursor.getColumnIndexOrThrow("subject"));
                fm.interval = cursor.getInt(cursor.getColumnIndexOrThrow("interval"));
                fm.repetitions = cursor.getInt(cursor.getColumnIndexOrThrow("repetitions"));
                fm.easeFactor = cursor.getFloat(cursor.getColumnIndexOrThrow("ease_factor"));
                reviewList.add(fm);
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (reviewList.isEmpty()) {
            tvQuestion.setText("All caught up!\nNo flashcards for review today.");
            tvAnswer.setVisibility(View.GONE);
            btnShowAnswer.setVisibility(View.GONE);
            setRatingButtonsVisibility(View.GONE);
        } else {
            showFlashcard(0);
        }
    }

    private void showFlashcard(int index) {
        currentIndex = index;
        isAnswerVisible = false;
        FlashcardModel fm = reviewList.get(index);
        tvQuestion.setText(fm.question);
        tvAnswer.setText(fm.answer);
        tvAnswer.setVisibility(View.GONE);
        tvSubjectLabel.setText(fm.subject);
        tvProgress.setText((index + 1) + " / " + reviewList.size());
        
        btnShowAnswer.setVisibility(View.VISIBLE);
        setRatingButtonsVisibility(View.GONE);
    }

    private void showAnswer() {
        isAnswerVisible = true;
        tvAnswer.setVisibility(View.VISIBLE);
        btnShowAnswer.setVisibility(View.GONE);
        setRatingButtonsVisibility(View.VISIBLE);
    }

    private void setRatingButtonsVisibility(int visibility) {
        findViewById(R.id.layoutRatings).setVisibility(visibility);
    }

    private void processResponse(int quality) {
        FlashcardModel fm = reviewList.get(currentIndex);
        
        // SM-2 Algorithm simplified
        int newInterval;
        int newRepetitions;
        float newEaseFactor = (float) Math.max(1.3, fm.easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));

        if (quality < 3) {
            newRepetitions = 0;
            newInterval = 1;
        } else {
            newRepetitions = fm.repetitions + 1;
            if (newRepetitions == 1) {
                newInterval = 1;
            } else if (newRepetitions == 2) {
                newInterval = 6;
            } else {
                newInterval = Math.round(fm.interval * newEaseFactor);
            }
        }

        long nextReview = System.currentTimeMillis() + (long) newInterval * 24 * 60 * 60 * 1000;
        repository.getLocalDb().updateFlashcardSM2(fm.id, newInterval, newRepetitions, newEaseFactor, nextReview);

        if (currentIndex < reviewList.size() - 1) {
            showFlashcard(currentIndex + 1);
        } else {
            Toast.makeText(this, "Session Complete! Great work.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private static class FlashcardModel {
        int id, interval, repetitions;
        float easeFactor;
        String question, answer, subject;
    }
}
