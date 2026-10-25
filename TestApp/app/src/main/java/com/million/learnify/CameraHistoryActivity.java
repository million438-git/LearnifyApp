package com.million.learnify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class CameraHistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername;
    private RecyclerView rvCameraHistory;
    private View llEmptyHistory;
    private HistoryAdapter adapter;
    private final List<ScanItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_history);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Guest");

        setupToolbar();
        initViews();
        loadHistory();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Lab & Scan History");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        rvCameraHistory = findViewById(R.id.rvCameraHistory);
        llEmptyHistory = findViewById(R.id.llEmptyHistory);
        rvCameraHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(items);
        rvCameraHistory.setAdapter(adapter);

        View btnOpenScanner = findViewById(R.id.btnEmptyOpenScanner);
        if (btnOpenScanner != null) {
            btnOpenScanner.setOnClickListener(v -> {
                startActivity(new Intent(this, CameraLabActivity.class));
                finish();
            });
        }
    }

    private void loadHistory() {
        items.clear();
        Cursor cursor = dbHelper.getLabHistory(currentUsername);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("problem_type"));
                String problem = cursor.getString(cursor.getColumnIndexOrThrow("problem_text"));
                String solution = cursor.getString(cursor.getColumnIndexOrThrow("solution_text"));
                String courseContext = "";
                int courseIdx = cursor.getColumnIndex("course_context");
                if (courseIdx != -1) {
                    courseContext = cursor.getString(courseIdx);
                }
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

                items.add(new ScanItem(id, type, problem, solution, courseContext, timestamp));
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (items.isEmpty()) {
            rvCameraHistory.setVisibility(View.GONE);
            llEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            rvCameraHistory.setVisibility(View.VISIBLE);
            llEmptyHistory.setVisibility(View.GONE);
        }
    }

    private void showScanDetailDialog(ScanItem item) {
        StringBuilder details = new StringBuilder();
        details.append("PROBLEM:\n").append(item.problem).append("\n\n");
        details.append("STEP-BY-STEP SOLUTION:\n").append(item.solution);
        if (item.courseContext != null && !item.courseContext.isEmpty()) {
            details.append("\n\nContext: ").append(item.courseContext);
        }
        details.append("\nScanned: ").append(item.date);

        new MaterialAlertDialogBuilder(this)
                .setTitle(item.type + " Analysis Result")
                .setMessage(details.toString())
                .setPositiveButton("COPY", (d, w) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Lab Solution", item.solution);
                    cm.setPrimaryClip(clip);
                    Toast.makeText(this, "Solution copied to clipboard.", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("SHARE", (d, w) -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Learnify Solution - " + item.type);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Learnify Problem & Solution:\n\n" + details);
                    startActivity(Intent.createChooser(shareIntent, "Share Solution"));
                })
                .setNegativeButton("DELETE", (d, w) -> confirmDelete(item))
                .show();
    }

    private void confirmDelete(ScanItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Scan Record")
                .setMessage("Are you sure you want to remove this scan from your history?")
                .setPositiveButton("DELETE", (d, w) -> {
                    boolean success = dbHelper.deleteLabResult(item.id);
                    if (success) {
                        Toast.makeText(this, "Scan removed.", Toast.LENGTH_SHORT).show();
                        loadHistory();
                    } else {
                        Toast.makeText(this, "Could not delete record.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    public static class ScanItem {
        public final int id;
        public final String type;
        public final String problem;
        public final String solution;
        public final String courseContext;
        public final String date;

        public ScanItem(int id, String type, String problem, String solution, String courseContext, String date) {
            this.id = id;
            this.type = (type != null && !type.trim().isEmpty()) ? type : "LAB";
            this.problem = problem != null ? problem : "";
            this.solution = solution != null ? solution : "";
            this.courseContext = courseContext != null ? courseContext : "";
            this.date = date != null ? date : "";
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final List<ScanItem> list;

        HistoryAdapter(List<ScanItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScanItem item = list.get(position);
            holder.tvType.setText(item.type);
            holder.tvResult.setText(item.problem.isEmpty() ? item.solution : item.problem);
            holder.tvDate.setText(item.date);

            holder.itemView.setOnClickListener(v -> showScanDetailDialog(item));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvResult, tvDate;

            ViewHolder(View v) {
                super(v);
                tvType = v.findViewById(R.id.tvScanType);
                tvResult = v.findViewById(R.id.tvScanResult);
                tvDate = v.findViewById(R.id.tvScanDate);
            }
        }
    }
}
