package com.million.learnify;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * AdminIssueCertificateActivity - Professional academic credential issuance system.
 */
public class AdminIssueCertificateActivity extends BaseActivity {

    private String targetUsername, targetFullName;
    private TextInputEditText etCertTitle, etPresentationText, etStudentName, etBodyText, etIssueDate, etStudentId, etCertificateId, etDirectorName, etDirectorRole;
    private AutoCompleteTextView actvCourseName, actvSelectStudent;
    private TextInputLayout tilSelectStudent;
    private DatabaseHelper dbHelper;
    private List<StudentItem> studentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_issue_certificate);

        dbHelper = repository.getLocalDb();
        
        targetUsername = getIntent().getStringExtra("TARGET_USERNAME");
        targetFullName = getIntent().getStringExtra("TARGET_NAME");

        Toolbar toolbar = findViewById(R.id.issueCertToolbar);
        setupPortalToolbar(toolbar, "Issue Certificate");

        initViews();
        loadStudents();

        if (targetUsername != null) {
            tilSelectStudent.setVisibility(View.GONE);
            etStudentName.setText(targetFullName);
            
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT unique_id FROM users WHERE username=?", new String[]{targetUsername});
            if (c.moveToFirst()) {
                etStudentId.setText(c.getString(0));
            }
            c.close();
            
            updateCourseDropdown(targetUsername);
        }
    }

    private void initViews() {
        tilSelectStudent = findViewById(R.id.tilSelectStudent);
        actvSelectStudent = findViewById(R.id.actvSelectStudent);
        etCertTitle = findViewById(R.id.etCertTitle);
        etPresentationText = findViewById(R.id.etPresentationText);
        etStudentName = findViewById(R.id.etStudentName);
        etBodyText = findViewById(R.id.etBodyText);
        actvCourseName = findViewById(R.id.actvCourseName);
        etIssueDate = findViewById(R.id.etIssueDate);
        etStudentId = findViewById(R.id.etStudentId);
        etCertificateId = findViewById(R.id.etCertificateId);
        etDirectorName = findViewById(R.id.etDirectorName);
        etDirectorRole = findViewById(R.id.etDirectorRole);

        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        etIssueDate.setText(today);
        
        String randomCertId = "LNYF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        etCertificateId.setText(randomCertId);

        updateCourseDropdown(null);

        findViewById(R.id.btnCancelIssue).setOnClickListener(v -> finish());
        
        MaterialButton btnSend = findViewById(R.id.btnConfirmIssue);
        btnSend.setOnClickListener(v -> handleIssue());
    }

    private void loadStudents() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT DISTINCT u.username, u.full_name, u.unique_id " +
                       "FROM users u " +
                       "INNER JOIN payments p ON u.username = p.username " +
                       "WHERE u.user_type='STUDENT' AND u.status='Active' AND p.status='Approved'";
        
        Cursor cursor = db.rawQuery(query, null);
        List<String> displayList = new ArrayList<>();
        studentList.clear();
        
        if (cursor.moveToFirst()) {
            do {
                String uname = cursor.getString(0);
                String fname = cursor.getString(1);
                String uid = cursor.getString(2);
                studentList.add(new StudentItem(uname, fname, uid));
                displayList.add(fname + " (" + uname + ")");
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, displayList);
        actvSelectStudent.setAdapter(adapter);

        actvSelectStudent.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            for (StudentItem s : studentList) {
                if (selected.contains("(" + s.username + ")")) {
                    targetUsername = s.username;
                    etStudentName.setText(s.fullName);
                    etStudentId.setText(s.uniqueId);
                    updateCourseDropdown(s.username);
                    break;
                }
            }
        });
    }

    private void updateCourseDropdown(String username) {
        List<String> courses = new ArrayList<>();
        if (username != null) {
            Cursor c = dbHelper.getStudentEnrollments(username);
            if (c.moveToFirst()) {
                do {
                    courses.add(c.getString(c.getColumnIndexOrThrow("course_name")));
                } while (c.moveToNext());
            }
            c.close();
        }

        if (courses.isEmpty()) {
            courses.addAll(Arrays.asList("Mathematics 101", "Advanced Physics", "Biology Essentials", "Chemistry Mastery", "Social Studies", "General Excellence"));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, courses);
        actvCourseName.setAdapter(adapter);
    }

    private void handleIssue() {
        if (targetUsername == null) {
            Toast.makeText(this, "Please select a student from the dropdown", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etCertTitle.getText().toString().trim();
        String presentation = etPresentationText.getText().toString().trim();
        String studName = etStudentName.getText().toString().trim();
        String body = etBodyText.getText().toString().trim();
        String course = actvCourseName.getText().toString().trim();
        String date = etIssueDate.getText().toString().trim();
        String studId = etStudentId.getText().toString().trim();
        String certId = etCertificateId.getText().toString().trim();
        String director = etDirectorName.getText().toString().trim();
        String role = etDirectorRole.getText().toString().trim();

        if (studName.isEmpty() || course.isEmpty()) {
            Toast.makeText(this, "Student Name and Course are required", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.saveFullCertificate(targetUsername, title, presentation, studName, body, course, date, studId, certId, director, role);
        
        if (success) {
            dbHelper.addNotification(targetUsername, "Professional Certificate Issued! 🏆",
                "Your official certificate for " + course + " is now available in your portal.", "CERTIFICATE");
            dbHelper.logAdminAction("admin", "Issued Professional Certificate", "Sent to " + targetUsername);

            Toast.makeText(this, "✅ Certificate issued successfully!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving certificate to database", Toast.LENGTH_SHORT).show();
        }
    }

    private static class StudentItem {
        String username, fullName, uniqueId;
        StudentItem(String u, String f, String id) { username = u; fullName = f; uniqueId = id; }
    }
}
