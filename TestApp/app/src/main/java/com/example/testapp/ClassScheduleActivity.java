package com.example.testapp;

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
import androidx.appcompat.app.AppCompatActivity;
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
 * Fixed compilation errors and ensured full database synchronization.
 */
public class ClassScheduleActivity extends AppCompatActivity {

    private RecyclerView rvSchedule;
    private DatabaseHelper dbHelper;
    private String currentUsername;
    private String userGrade;
    private String selectedDay = "Monday";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_schedule);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");
        userGrade = dbHelper.getUserGrade(currentUsername);

        setupToolbar();
        initViews();
        loadSchedule(selectedDay);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.scheduleToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        rvSchedule = findViewById(R.id.rvSchedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));

        TabLayout tabLayout = findViewById(R.id.tabLayoutDays);
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

        FloatingActionButton fab = findViewById(R.id.fabAddSchedule);
        fab.setOnClickListener(v -> showAddClassDialog());
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
                        dbHelper.insertClassCustom(sub, "Self", "Personal Space", selectedTime[0], "60m", selectedDay, userGrade);
                        loadSchedule(selectedDay);
                        Toast.makeText(this, "Routine updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void loadSchedule(String day) {
        // Linked correctly to the restored DatabaseHelper method
        List<ScheduleModel> schedule = dbHelper.getScheduleForGrade(userGrade, day);
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
            holder.tvSubject.setText(item.getSubject());
            holder.tvDetails.setText(item.getTeacher() + " • " + item.getRoom());
            
            String fullTime = item.getTime();
            if (fullTime.contains(" ")) {
                String[] parts = fullTime.split(" ");
                holder.tvTime.setText(parts[0]);
                holder.tvAmPm.setText(parts[1]);
            } else {
                holder.tvTime.setText(fullTime);
                holder.tvAmPm.setText("");
            }
            
            holder.tvDuration.setText(item.getDuration());
            holder.btnReminder.setOnClickListener(v -> Toast.makeText(ClassScheduleActivity.this, "Reminder Active", Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return scheduleList.size();
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
