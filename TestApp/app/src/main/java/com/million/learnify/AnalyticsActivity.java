package com.million.learnify;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import java.util.Calendar;

/**
 * AnalyticsActivity - Visual Progress Tracking for Students.
 * Enhanced to track Spaced Repetition Mastery and Emotional Well-being.
 */
public class AnalyticsActivity extends BaseActivity {

    private LinearLayout llStatsContainer;
    private TextView tvTotalXP, tvCurrentStreak, tvCompletionRate;
    private TextView tvMasteryCount, tvMoodStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enforceRole("STUDENT");
        setContentView(R.layout.activity_analytics);

        setupToolbar();
        initViews();
        loadAnalyticsData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.analyticsToolbar);
        setupPortalToolbar(toolbar, "Learning Analytics");
    }

    private void initViews() {
        llStatsContainer = findViewById(R.id.llStatsContainer);
        tvTotalXP = findViewById(R.id.tvTotalXPAnalytics);
        tvCurrentStreak = findViewById(R.id.tvStreakAnalytics);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvMasteryCount = findViewById(R.id.tvMasteryCount);
        tvMoodStatus = findViewById(R.id.tvMoodStatus);
    }

    private void loadAnalyticsData() {
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = session.getString("username", "");

        // 1. Load Challenge Stats
        Cursor cursor = repository.getLocalDb().getChallengeStats(username);
        if (cursor != null && cursor.moveToFirst()) {
            int xp = cursor.getInt(cursor.getColumnIndexOrThrow("total_xp"));
            int streak = cursor.getInt(cursor.getColumnIndexOrThrow("streak"));
            tvTotalXP.setText(xp + " XP");
            tvCurrentStreak.setText(streak + " Days");
            cursor.close();
        }

        // 2. Load Task Completion Stats
        int totalTasks = repository.getLocalDb().getTableCount("tasks", null, null);
        int completedTasks = repository.getLocalDb().getTableCount("tasks", "status='Completed'", null);
        
        if (totalTasks > 0) {
            int rate = (completedTasks * 100) / totalTasks;
            tvCompletionRate.setText(rate + "% Tasks Done");
        } else {
            tvCompletionRate.setText("0% Tasks Done");
        }

        // 3. Load Flashcard Mastery (SM-2 data)
        int masteredCards = repository.getLocalDb().getTableCount("flashcards", "interval > 10", null);
        tvMasteryCount.setText(masteredCards + " Concepts Mastered");

        // 4. Load Recent Mood
        Cursor moodCursor = repository.getLocalDb().getReadableDatabase().rawQuery(
                "SELECT mood FROM mood_logs WHERE username=? ORDER BY timestamp DESC LIMIT 1", new String[]{username});
        if (moodCursor != null && moodCursor.moveToFirst()) {
            tvMoodStatus.setText("Recent Mood: " + moodCursor.getString(0));
            moodCursor.close();
        } else {
            tvMoodStatus.setText("Mood: Not logged yet");
        }
        
        // 5. Render a Simple Visual "Activity Bar"
        renderActivityBar(completedTasks, totalTasks);
    }

    private void renderActivityBar(int done, int total) {
        if (total == 0) return;
        View progressView = findViewById(R.id.viewProgressFill);
        if (progressView != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progressView.getLayoutParams();
            params.weight = (float) done / total;
            progressView.setLayoutParams(params);
        }
    }
}
