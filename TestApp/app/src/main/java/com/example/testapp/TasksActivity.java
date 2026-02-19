package com.example.testapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TasksActivity extends AppCompatActivity {

    private RecyclerView rvTasks;
    private DatabaseHelper dbHelper;
    private String currentUsername, userGrade;
    private TextView tvTotalCount, tvCompletedCount, tvPendingCount, tvOverdueCount;
    private MaterialCardView cardAddTaskForm;
    private TextInputEditText etTaskTitle, etTaskDesc, etTaskDate, etTaskTime;
    private AutoCompleteTextView actvTaskSubject, actvTaskPriority;
    
    private static final String API_KEY = "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";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");
        userGrade = dbHelper.getUserGrade(currentUsername);

        setupToolbar();
        initViews();
        setupFormDropdowns();
        loadTasks();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.tasksToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvOverdueCount = findViewById(R.id.tvOverdueCount);
        
        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        cardAddTaskForm = findViewById(R.id.cardAddTaskForm);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDesc = findViewById(R.id.etTaskDesc);
        etTaskDate = findViewById(R.id.etTaskDate);
        etTaskTime = findViewById(R.id.etTaskTime);
        actvTaskSubject = findViewById(R.id.actvTaskSubject);
        actvTaskPriority = findViewById(R.id.actvTaskPriority);

        findViewById(R.id.btnShowAddTask).setOnClickListener(v -> cardAddTaskForm.setVisibility(View.VISIBLE));
        findViewById(R.id.btnCancelTask).setOnClickListener(v -> cardAddTaskForm.setVisibility(View.GONE));
        findViewById(R.id.btnSaveTask).setOnClickListener(v -> saveTask());
        findViewById(R.id.btnAiTaskAssist).setOnClickListener(v -> getAiHelp());

        etTaskDate.setOnClickListener(v -> showDatePicker());
        etTaskTime.setOnClickListener(v -> showTimePicker());
    }

    private void setupFormDropdowns() {
        String[] subjects = {"Mathematics 📐", "Physics ⚛️", "Chemistry 🧪", "Biology 🧬", "English 📖", "Computer Science 💻", "Personal Study 🏠"};
        actvTaskSubject.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subjects));

        String[] priorities = {"High 🔴", "Medium 🟡", "Low 🟢"};
        actvTaskPriority.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, priorities));
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            etTaskDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            etTaskTime.setText(hourOfDay + ":" + minute);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String desc = etTaskDesc.getText().toString().trim();
        String subject = actvTaskSubject.getText().toString();
        String date = etTaskDate.getText().toString();
        String time = etTaskTime.getText().toString();
        String priority = actvTaskPriority.getText().toString();

        if (title.isEmpty() || subject.isEmpty() || date.isEmpty() || priority.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.insertTask(title, desc, subject, userGrade, date, time, priority);
        if (success) {
            Toast.makeText(this, "📌 Saving task...", Toast.LENGTH_SHORT).show();
            cardAddTaskForm.setVisibility(View.GONE);
            clearForm();
            loadTasks();
        }
    }

    private void clearForm() {
        etTaskTitle.setText("");
        etTaskDesc.setText("");
        etTaskDate.setText("");
        etTaskTime.setText("");
        actvTaskSubject.setText("");
        actvTaskPriority.setText("");
    }

    private void loadTasks() {
        List<TaskModel> tasks = dbHelper.getTasksForGrade(userGrade);
        
        int total = tasks.size();
        int done = 0, pending = 0, overdue = 0;
        
        // Simple logic for summary
        for (TaskModel t : tasks) {
            if ("Completed".equals(t.getStatus())) done++;
            else pending++;
        }

        tvTotalCount.setText(String.valueOf(total));
        tvCompletedCount.setText(String.valueOf(done));
        tvPendingCount.setText(String.valueOf(pending));
        tvOverdueCount.setText(String.valueOf(overdue));

        rvTasks.setAdapter(new TasksAdapter(tasks));
    }

    private void getAiHelp() {
        Toast.makeText(this, "🤖 AI analyzing your schedule...", Toast.LENGTH_SHORT).show();
        // Prompt construction and OpenAI call would go here using API_KEY
    }

    class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {
        private List<TaskModel> taskList;

        TasksAdapter(List<TaskModel> taskList) {
            this.taskList = taskList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TaskModel task = taskList.get(position);
            holder.tvSubject.setText(task.getSubject().toUpperCase());
            holder.tvTitle.setText(task.getTitle());
            holder.tvDesc.setText(task.getDescription());
            holder.tvDue.setText("📅 " + task.getDueDate() + "  ⏰ " + task.getDueTime());
            holder.tvStatus.setText(task.getStatus());
            holder.tvPriority.setText(task.getPriority());

            if ("Completed".equals(task.getStatus())) {
                holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
                holder.btnMarkDone.setVisibility(View.GONE);
            } else {
                holder.tvStatus.setTextColor(Color.parseColor("#3B82F6"));
                holder.btnMarkDone.setVisibility(View.VISIBLE);
            }

            holder.btnMarkDone.setOnClickListener(v -> {
                dbHelper.updateTaskStatus(task.getId(), "Completed");
                loadTasks();
            });

            holder.btnDelete.setOnClickListener(v -> {
                dbHelper.deleteTask(task.getId());
                loadTasks();
            });
        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSubject, tvTitle, tvDesc, tvDue, tvStatus, tvPriority;
            MaterialButton btnMarkDone;
            View btnDelete;

            ViewHolder(View v) {
                super(v);
                tvSubject = v.findViewById(R.id.tvTaskSubject);
                tvTitle = v.findViewById(R.id.tvTaskTitle);
                tvDesc = v.findViewById(R.id.tvTaskDescription);
                tvDue = v.findViewById(R.id.tvTaskDueDate);
                tvStatus = v.findViewById(R.id.tvTaskStatus);
                tvPriority = v.findViewById(R.id.tvTaskPriority);
                btnMarkDone = v.findViewById(R.id.btnMarkDone);
                btnDelete = v.findViewById(R.id.btnDeleteTask); // Ensure this exists in item_task.xml
            }
        }
    }
}
