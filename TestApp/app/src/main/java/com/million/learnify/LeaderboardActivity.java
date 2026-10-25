package com.million.learnify;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        dbHelper = new DatabaseHelper(this);
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));

        setupToolbar();
        loadLeaderboard();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.leaderboardToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadLeaderboard() {
        List<LeaderboardEntry> list = new ArrayList<>();
        Cursor cursor = dbHelper.getLeaderboard();
        while (cursor.moveToNext()) {
            list.add(new LeaderboardEntry(
                cursor.getString(cursor.getColumnIndexOrThrow("username")),
                cursor.getInt(cursor.getColumnIndexOrThrow("total_score")),
                cursor.getInt(cursor.getColumnIndexOrThrow("max_streak"))
            ));
        }
        cursor.close();
        rvLeaderboard.setAdapter(new LeaderboardAdapter(list));
    }

    static class LeaderboardEntry {
        String username;
        int score, streak;
        LeaderboardEntry(String u, int s, int st) {
            username = u; score = s; streak = st;
        }
    }

    class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private List<LeaderboardEntry> list;
        LeaderboardAdapter(List<LeaderboardEntry> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LeaderboardEntry entry = list.get(position);
            String rank = "#" + (position + 1) + " ";
            holder.text1.setText(rank + entry.username);
            holder.text1.setTextColor(Color.WHITE);
            holder.text2.setText("Points: " + entry.score + "  |  Best Streak: " + entry.streak + " days");
            holder.text2.setTextColor(Color.parseColor("#94A3B8"));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
