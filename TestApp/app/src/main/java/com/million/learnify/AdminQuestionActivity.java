package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.button.MaterialButton;

public class AdminQuestionActivity extends AppCompatActivity {

    private EditText etQuestionTitle, etQuestion, etOptA, etOptB, etOptC, etOptD, etExplanation;
    private AutoCompleteTextView actvType, actvDifficulty, actvCategory;
    private RadioButton rbA, rbB, rbC, rbD;
    private DatabaseHelper dbHelper;
    private String currentUsername;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_question);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "admin");

        initViews();
        setupDropdowns();
        setupDrawerNavigation();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.adminQuestionDrawerLayout);
        etQuestionTitle = findViewById(R.id.etQuestionTitle);
        etQuestion = findViewById(R.id.etQuestion);
        etOptA = findViewById(R.id.etOptA);
        etOptB = findViewById(R.id.etOptB);
        etOptC = findViewById(R.id.etOptC);
        etOptD = findViewById(R.id.etOptD);
        etExplanation = findViewById(R.id.etExplanation_new);
        
        actvType = findViewById(R.id.actvType);
        actvDifficulty = findViewById(R.id.actvDifficulty);
        actvCategory = findViewById(R.id.actvCategory);
        
        rbA = findViewById(R.id.rbA);
        rbB = findViewById(R.id.rbB);
        rbC = findViewById(R.id.rbC);
        rbD = findViewById(R.id.rbD);

        // Hamburger Menu Icon
        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        
        MaterialButton btnPublish = findViewById(R.id.btnPublishQuestion);
        btnPublish.setOnClickListener(v -> handlePublish());

        findViewById(R.id.btnUploadFile).setOnClickListener(v -> 
            Toast.makeText(this, "Opening File Gallery...", Toast.LENGTH_SHORT).show());

        // Radio group logic
        View.OnClickListener rbListener = v -> {
            rbA.setChecked(v.getId() == R.id.rbA);
            rbB.setChecked(v.getId() == R.id.rbB);
            rbC.setChecked(v.getId() == R.id.rbC);
            rbD.setChecked(v.getId() == R.id.rbD);
        };
        rbA.setOnClickListener(rbListener);
        rbB.setOnClickListener(rbListener);
        rbC.setOnClickListener(rbListener);
        rbD.setOnClickListener(rbListener);
    }

    private void setupDrawerNavigation() {
        // Dashboard
        findViewById(R.id.drawerNavDashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
        });

        // Courses / Content
        findViewById(R.id.drawerNavCourses).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminContentManagementActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Add Question (Current Page)
        findViewById(R.id.drawerNavAddQuestion).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // View Questions
        findViewById(R.id.drawerNavViewQuestions).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminContentManagementActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Create Exam
        findViewById(R.id.drawerNavCreateExam).setOnClickListener(v -> {
            Toast.makeText(this, "Exam Builder Module Loading...", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Schedule
        findViewById(R.id.drawerNavSchedule).setOnClickListener(v -> {
            Toast.makeText(this, "Opening Exam Scheduler...", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Students
        findViewById(R.id.drawerNavStudents).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUserManagementActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Staff
        findViewById(R.id.drawerNavStaff).setOnClickListener(v -> {
            Toast.makeText(this, "Accessing Staff Directory...", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private void setupDropdowns() {
        String[] types = {"Multiple Choice", "True/False", "Short Answer"};
        actvType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types));

        String[] levels = {"Beginner", "Intermediate", "Advanced"};
        actvDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels));

        String[] cats = {"Mathematics 101", "Calculus Essentials", "Physics Mechanics", "General Science"};
        actvCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cats));
    }

    private void handlePublish() {
        String title = etQuestionTitle.getText().toString().trim();
        String question = etQuestion.getText().toString().trim();
        String a = etOptA.getText().toString().trim();
        String b = etOptB.getText().toString().trim();
        String c = etOptC.getText().toString().trim();
        String d = etOptD.getText().toString().trim();
        String diff = actvDifficulty.getText().toString().trim();
        String cat = actvCategory.getText().toString().trim();
        String expl = etExplanation.getText().toString().trim();

        if (question.isEmpty() || a.isEmpty() || b.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String correctAns = "";
        if (rbA.isChecked()) correctAns = a;
        else if (rbB.isChecked()) correctAns = b;
        else if (rbC.isChecked()) correctAns = c;
        else if (rbD.isChecked()) correctAns = d;

        if (correctAns.isEmpty()) {
            Toast.makeText(this, "Select a correct answer", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send to Database
        boolean success = dbHelper.insertDailyQuestionFull(question, a, b, c, d, correctAns, "12", cat, diff, expl);
        
        if (success) {
            dbHelper.addAnnouncement("New Exam Question Added: " + title, "Question on " + cat, "EXAM");
            Toast.makeText(this, "✅ Question Saved and Published!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
