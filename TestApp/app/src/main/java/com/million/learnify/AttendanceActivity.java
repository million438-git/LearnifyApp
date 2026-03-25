package com.million.learnify;

import android.content.SharedPreferences;
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

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * AttendanceActivity - Professional student attendance tracking system.
 */
public class AttendanceActivity extends AppCompatActivity {

    private RecyclerView rvAttendance;
    private CircularProgressIndicator progressOverall;
    private TextView tvPercentage;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        setupToolbar();
        initViews();
        loadAttendanceData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.attendanceToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Attendance Registry");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        rvAttendance = findViewById(R.id.rvAttendance);
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        progressOverall = findViewById(R.id.progressOverall);
        tvPercentage = findViewById(R.id.tvPercentage);
    }

    private void loadAttendanceData() {
        // High-performance mock data for professional feel
        List<AttendanceItem> list = new ArrayList<>();
        list.add(new AttendanceItem("Mathematics", "95%", "2 Present • 0 Absent"));
        list.add(new AttendanceItem("Physics", "88%", "18 Present • 2 Absent"));
        list.add(new AttendanceItem("Biology", "100%", "20 Present • 0 Absent"));
        list.add(new AttendanceItem("History", "92%", "15 Present • 1 Absent"));

        rvAttendance.setAdapter(new AttendanceAdapter(list));
        
        progressOverall.setProgress(94);
        tvPercentage.setText("94%");
    }

    class AttendanceItem {
        String subject, percentage, details;
        AttendanceItem(String s, String p, String d) {
            this.subject = s; this.percentage = p; this.details = d;
        }
    }

    class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
        private List<AttendanceItem> items;
        AttendanceAdapter(List<AttendanceItem> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AttendanceItem item = items.get(position);
            holder.tvName.setText(item.subject);
            holder.tvDetails.setText(item.details);
            holder.tvStatus.setText(item.percentage);
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetails, tvStatus;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvUserName);
                tvDetails = v.findViewById(R.id.tvUserRole);
                tvStatus = v.findViewById(R.id.tvUserStatus);
            }
        }
    }
}
