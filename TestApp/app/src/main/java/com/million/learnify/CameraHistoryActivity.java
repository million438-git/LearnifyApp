package com.million.learnify;

import android.annotation.SuppressLint;
import android.database.Cursor;
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
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class CameraHistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_history);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Guest");

        setupToolbar();
        setupRecyclerView();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lab History");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvCameraHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        List<ScanItem> items = new ArrayList<>();
        Cursor cursor = dbHelper.getLabHistory(currentUsername);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String mode = cursor.getString(cursor.getColumnIndex("mode"));
                @SuppressLint("Range") String problem = cursor.getString(cursor.getColumnIndex("problem"));
                @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
                items.add(new ScanItem(mode, problem, timestamp));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        rv.setAdapter(new HistoryAdapter(items));
    }

    private static class ScanItem {
        String type, result, date;
        ScanItem(String t, String r, String d) { type = t; result = r; date = d; }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final List<ScanItem> items;
        HistoryAdapter(List<ScanItem> items) { this.items = items; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera_history, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScanItem item = items.get(position);
            holder.tvType.setText(item.type);
            holder.tvResult.setText(item.result);
            holder.tvDate.setText(item.date);
        }

        @Override public int getItemCount() { return items.size(); }

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
