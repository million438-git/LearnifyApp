package com.million.learnify;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AdminIssueCertificateActivity extends AppCompatActivity {

    private String targetUsername, targetFullName;
    private TextInputEditText etCertTitle, etPresentationText, etStudentName, etBodyText, etIssueDate, etStudentId, etCertificateId, etDirectorName, etDirectorRole;
    private AutoCompleteTextView actvCourseName;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_issue_certificate);

        dbHelper = new DatabaseHelper(this);
        
        targetUsername = getIntent().getStringExtra("TARGET_USERNAME");
        targetFullName = getIntent().getStringExtra("TARGET_NAME");

        if (targetUsername == null) {
            Toast.makeText(this, "Error: No student selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
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

        // Set default values
        etStudentName.setText(targetFullName);
        etStudentId.setText("ID-" + (int)(Math.random() * 90000 + 10000));
        
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        etIssueDate.setText(today);
        
        String randomCertId = "LNYF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        etCertificateId.setText(randomCertId);

        String[] subjects = {"Mathematics 101", "Advanced Physics", "Biology Essentials", "Chemistry Mastery", "Social Studies", "General Excellence"};
        actvCourseName.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects));

        findViewById(R.id.btnCancelIssue).setOnClickListener(v -> finish());
        
        MaterialButton btnSend = findViewById(R.id.btnConfirmIssue);
        btnSend.setOnClickListener(v -> handleIssue());
    }

    private void handleIssue() {
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

        // Save to Database
        boolean success = dbHelper.saveFullCertificate(targetUsername, title, presentation, studName, body, course, date, studId, certId, director, role);
        
        if (success) {
            // Log action and notify
            dbHelper.addNotification(targetUsername, "Professional Certificate Issued! 🏆",
                "Your official certificate for " + course + " is now available in your portal.", "CERTIFICATE");
            dbHelper.logAdminAction("admin", "Issued Professional Certificate", "Sent to " + targetUsername);

            Toast.makeText(this, "✅ Certificate issued to " + studName, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving certificate", Toast.LENGTH_SHORT).show();
        }
    }
}
