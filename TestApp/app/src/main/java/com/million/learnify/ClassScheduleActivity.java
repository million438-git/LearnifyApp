package com.million.learnify;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * ClassScheduleActivity - Perfected Weekly Routine with Student Flexibility.
 * Refactored to use DataRepository for Client-Server readiness.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class ClassScheduleActivity extends BaseActivity {

    private RecyclerView rvSchedule;
    private String currentUsername;
    private String userGrade;
    private String selectedDay = "Monday";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Student users to enter this portal.
        enforceRole("STUDENT");
        
        setContentView(R.layout.activity_class_schedule);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");
        userGrade = pref.getString("grade", "10");

        setupToolbar();
        initViews();
        loadSchedule(selectedDay);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.scheduleToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        rvSchedule = findViewById(R.id.rvSchedule);
        if (rvSchedule != null) {
            rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        }

        TabLayout tabLayout = findViewById(R.id.tabLayoutDays);
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getText() != null) {
                        selectedDay = tab.getText().toString();
                        loadSchedule(selectedDay);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        FloatingActionButton fab = findViewById(R.id.fabAddSchedule);
        if (fab != null) {
            fab.setOnClickListener(v -> showAddClassDialog());
        }
    }

    private void showAddClassDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final TextInputEditText etSubject = new TextInputEditText(this);
        etSubject.setHint("Subject or Study Task");
        layout.addView(etSubject);

        final TextView tvTimePicker = new TextView(this);
        tvTimePicker.setText("🕒 Select Start Time (Tap here)");
        tvTimePicker.setPadding(0, 40, 0, 20);
        tvTimePicker.setTextColor(Color.parseColor("#22D3EE"));
        layout.addView(tvTimePicker);

        final String[] selectedTime = {"09:00 AM"};
        tvTimePicker.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker = new TimePickerDialog(this, (view, h, m) -> {
                String am_pm = (h < 12) ? "AM" : "PM";
                int displayHour = (h > 12) ? h - 12 : (h == 0 ? 12 : h);
                selectedTime[0] = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, m, am_pm);
                tvTimePicker.setText("🕒 Time Set: " + selectedTime[0]);
            }, hour, minute, false);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add to " + selectedDay)
                .setView(layout)
                .setPositiveButton("SCHEDULE", (dialog, which) -> {
                    String sub = etSubject.getText() != null ? etSubject.getText().toString().trim() : "";
                    if (!sub.isEmpty()) {
                        // Using Repository for insertion (future-proof)
                        repository.getLocalDb().insertClassCustom(sub, "Self", "Personal Space", selectedTime[0], "60m", selectedDay, userGrade);
                        loadSchedule(selectedDay);
                        Toast.makeText(this, "Routine updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void loadSchedule(String day) {
        if (rvSchedule == null) return;
        // Fetch via Repository (Abstraction Layer)
        List<ScheduleModel> schedule = repository.getSchedule(userGrade, day);
        rvSchedule.setAdapter(new ScheduleAdapter(schedule));
    }

    class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
        private List<ScheduleModel> scheduleList;

        ScheduleAdapter(List<ScheduleModel> scheduleList) {
            this.scheduleList = scheduleList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScheduleModel item = scheduleList.get(position);
            if (item == null) return;

            holder.tvSubject.setText(item.getSubject() != null ? item.getSubject() : "Untitled");
            
            String instructor = item.getInstructor() != null ? item.getInstructor() : "Self";
            String location = item.getLocation() != null ? item.getLocation() : "N/A";
            holder.tvDetails.setText(String.format("%s • %s", instructor, location));
            
            String fullTime = item.getStartTime();
            if (fullTime != null && fullTime.trim().contains(" ")) {
                String[] parts = fullTime.trim().split("\\s+");
                if (parts.length >= 2) {
                    holder.tvTime.setText(parts[0]);
                    holder.tvAmPm.setText(parts[1]);
                } else {
                    holder.tvTime.setText(parts[0]);
                    holder.tvAmPm.setText("");
                }
            } else {
                holder.tvTime.setText(fullTime != null ? fullTime : "00:00");
                holder.tvAmPm.setText("");
            }
            
            holder.tvDuration.setText(item.getDuration() != null ? item.getDuration() : "60m");
            
            if (holder.btnReminder != null) {
                holder.btnReminder.setOnClickListener(v -> 
                    Toast.makeText(ClassScheduleActivity.this, "Reminder Active for " + holder.tvSubject.getText(), Toast.LENGTH_SHORT).show()
                );
            }
        }

        @Override
        public int getItemCount() {
            return scheduleList != null ? scheduleList.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSubject, tvDetails, tvTime, tvAmPm, tvDuration;
            ImageButton btnReminder;

            ViewHolder(View v) {
                super(v);
                tvSubject = v.findViewById(R.id.tvClassSubject);
                tvDetails = v.findViewById(R.id.tvClassDetails);
                tvTime = v.findViewById(R.id.tvClassTime);
                tvAmPm = v.findViewById(R.id.tvClassTimeAmPm);
                tvDuration = v.findViewById(R.id.tvClassDuration);
                btnReminder = v.findViewById(R.id.btnSetReminder);
            }
        }
    }
}
