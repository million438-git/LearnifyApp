package com.million.learnify;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * TasksActivity - Student assignment and personal task manager.
 * Refactored to use DataRepository for Client-Server readiness.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class TasksActivity extends BaseActivity {

    private RecyclerView rvTasks;
    private String currentUsername, userGrade;
    private TextView tvTotalCount, tvCompletedCount, tvPendingCount, tvOverdueCount;
    private MaterialCardView cardAddTaskForm;
    private TextInputEditText etTaskTitle, etTaskDesc, etTaskDate, etTaskTime;
    private AutoCompleteTextView actvTaskSubject, actvTaskPriority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Student users to enter this portal.
        enforceRole("STUDENT");
        
        setContentView(R.layout.activity_tasks);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");
        userGrade = pref.getString("grade", "10");

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
            etTaskDate.setText(String.format(Locale.US, "%d-%02d-%02d", year, (month + 1), dayOfMonth));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            etTaskTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute));
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

        // Using Repository for insertion (future-proof)
        boolean success = repository.getLocalDb().insertTask(title, desc, subject, userGrade, date, time, priority);
        if (success) {
            Toast.makeText(this, "📌 Task Saved!", Toast.LENGTH_SHORT).show();
            
            SettingsService settings = new SettingsService(this);
            if (settings.loadSettings().isHomeworkAlerts()) {
                NotificationHelper.scheduleTaskNotification(this, (int)System.currentTimeMillis(), title, date, time);
            }

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
        // Fetch via Repository (Abstraction Layer)
        List<TaskModel> tasks = repository.getStudentTasks(userGrade);
        int total = tasks.size();
        int done = 0, pending = 0, overdue = 0;
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
                // Update via Repository
                repository.updateTaskStatus(task.getId(), "Completed");
                loadTasks();
            });

            holder.btnDelete.setOnClickListener(v -> {
                repository.getLocalDb().deleteTask(task.getId());
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
                btnDelete = v.findViewById(R.id.btnDeleteTask);
            }
        }
    }
}
