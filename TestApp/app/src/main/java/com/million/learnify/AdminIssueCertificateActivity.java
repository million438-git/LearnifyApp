package com.million.learnify;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
 * AdminIssueCertificateActivity - Professional academic credential issuance & registry governance.
 * Features:
 * 1. Role enforcement (ADMIN).
 * 2. Summary KPI cards for issued certificates and eligible candidates.
 * 3. Tab switcher: Issue New, Issued Registry, and Verify Authenticity.
 * 4. Transparent student eligibility inspector (payment, exams, duplicate checks).
 * 5. Pre-issuance review & duplicate issuance warning.
 * 6. Authenticity lookup by Certificate ID against real SQLite database records.
 * 7. Live search & audit list of all issued certificates.
 */
public class AdminIssueCertificateActivity extends BaseActivity {

    private String targetUsername, targetFullName;
    private DatabaseHelper dbHelper;
    private String currentAdminUsername;

    // Preserved Form Views
    private TextInputEditText etCertTitle, etPresentationText, etStudentName, etBodyText, etIssueDate, etStudentId, etCertificateId, etDirectorName, etDirectorRole;
    private AutoCompleteTextView actvCourseName, actvSelectStudent;
    private TextInputLayout tilSelectStudent;
    private MaterialButton btnCancelIssue, btnConfirmIssue;

    // Mode Switcher Tabs
    private MaterialButton btnTabIssue, btnTabRegistry, btnTabVerify;
    private LinearLayout layoutIssueSection, layoutRegistrySection, layoutVerifySection;

    // KPI Metrics
    private TextView tvStatTotalIssued, tvStatEligibleCandidates, tvStatRecentCount;

    // Eligibility Inspector Views
    private MaterialCardView cardEligibilityInspector;
    private TextView tvEligibilityStatusBadge, tvEligibilityPaymentStatus, tvEligibilityExamStatus, tvEligibilityDuplicateWarning;

    // Registry Views
    private TextInputEditText etSearchRegistry;
    private TextView tvRegistryRecordsCount, tvNoRegistryCertificates;
    private LinearLayout registryListContainer;

    // Verification Views
    private TextInputEditText etVerifyCertId;
    private MaterialButton btnRunVerify;
    private MaterialCardView cardVerificationResult;
    private TextView tvVerifyResultTitle, tvVerifyResultStatusBadge, tvVerifyResultDetails;

    // Internal data structures
    private final List<StudentItem> studentList = new ArrayList<>();
    private final List<CertificateRecord> allCertificatesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");

        setContentView(R.layout.activity_admin_issue_certificate);

        dbHelper = repository.getLocalDb();
        currentAdminUsername = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", "admin");

        targetUsername = getIntent().getStringExtra("TARGET_USERNAME");
        targetFullName = getIntent().getStringExtra("TARGET_NAME");

        Toolbar toolbar = findViewById(R.id.issueCertToolbar);
        if (toolbar != null) {
            setupPortalToolbar(toolbar, "Certificate Management");
        }

        initViews();
        setupTabs();
        loadStudents();
        refreshKpiMetrics();
        loadIssuedCertificatesRegistry();

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
            inspectStudentEligibility(targetUsername);
        }
    }

    private void initViews() {
        // Preserved 15 views
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
        btnCancelIssue = findViewById(R.id.btnCancelIssue);
        btnConfirmIssue = findViewById(R.id.btnConfirmIssue);

        // Mode Switcher Tabs
        btnTabIssue = findViewById(R.id.btnTabIssue);
        btnTabRegistry = findViewById(R.id.btnTabRegistry);
        btnTabVerify = findViewById(R.id.btnTabVerify);
        layoutIssueSection = findViewById(R.id.layoutIssueSection);
        layoutRegistrySection = findViewById(R.id.layoutRegistrySection);
        layoutVerifySection = findViewById(R.id.layoutVerifySection);

        // KPI Metrics
        tvStatTotalIssued = findViewById(R.id.tvStatTotalIssued);
        tvStatEligibleCandidates = findViewById(R.id.tvStatEligibleCandidates);
        tvStatRecentCount = findViewById(R.id.tvStatRecentCount);

        // Eligibility Inspector
        cardEligibilityInspector = findViewById(R.id.cardEligibilityInspector);
        tvEligibilityStatusBadge = findViewById(R.id.tvEligibilityStatusBadge);
        tvEligibilityPaymentStatus = findViewById(R.id.tvEligibilityPaymentStatus);
        tvEligibilityExamStatus = findViewById(R.id.tvEligibilityExamStatus);
        tvEligibilityDuplicateWarning = findViewById(R.id.tvEligibilityDuplicateWarning);

        // Registry Views
        etSearchRegistry = findViewById(R.id.etSearchRegistry);
        tvRegistryRecordsCount = findViewById(R.id.tvRegistryRecordsCount);
        tvNoRegistryCertificates = findViewById(R.id.tvNoRegistryCertificates);
        registryListContainer = findViewById(R.id.registryListContainer);

        // Verifier Views
        etVerifyCertId = findViewById(R.id.etVerifyCertId);
        btnRunVerify = findViewById(R.id.btnRunVerify);
        cardVerificationResult = findViewById(R.id.cardVerificationResult);
        tvVerifyResultTitle = findViewById(R.id.tvVerifyResultTitle);
        tvVerifyResultStatusBadge = findViewById(R.id.tvVerifyResultStatusBadge);
        tvVerifyResultDetails = findViewById(R.id.tvVerifyResultDetails);

        // Default Date
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        etIssueDate.setText(today);

        // Default Fresh ID
        generateFreshCertificateId();

        updateCourseDropdown(null);

        btnCancelIssue.setOnClickListener(v -> finish());
        btnConfirmIssue.setOnClickListener(v -> handleIssuePrecheck());

        if (btnRunVerify != null) {
            btnRunVerify.setOnClickListener(v -> runAuthenticityVerification());
        }

        if (etSearchRegistry != null) {
            etSearchRegistry.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterAndRenderRegistry(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    public static boolean isValidCertificateId(String certId) {
        return certId != null && certId.matches("^LNYF-[A-Z0-9]{8}$");
    }

    public static String generateCertificateIdString() {
        return "LNYF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void generateFreshCertificateId() {
        String certId;
        do {
            certId = generateCertificateIdString();
        } while (dbHelper.isCertificateIdTaken(certId));

        if (etCertificateId != null) {
            etCertificateId.setText(certId);
        }
    }

    private void setupTabs() {
        if (btnTabIssue == null || btnTabRegistry == null || btnTabVerify == null) return;

        btnTabIssue.setOnClickListener(v -> switchTab(0));
        btnTabRegistry.setOnClickListener(v -> switchTab(1));
        btnTabVerify.setOnClickListener(v -> switchTab(2));
    }

    private void switchTab(int tabIndex) {
        if (layoutIssueSection == null || layoutRegistrySection == null || layoutVerifySection == null) return;

        layoutIssueSection.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        layoutRegistrySection.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        layoutVerifySection.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);

        int activeBg = ContextCompat.getColor(this, R.color.admin_sapphire_primary);
        int activeText = ContextCompat.getColor(this, R.color.white);
        int inactiveText = ContextCompat.getColor(this, R.color.admin_text_primary);

        // Tab 0: Issue
        btnTabIssue.setBackgroundColor(tabIndex == 0 ? activeBg : 0x00000000);
        btnTabIssue.setTextColor(tabIndex == 0 ? activeText : inactiveText);

        // Tab 1: Registry
        btnTabRegistry.setBackgroundColor(tabIndex == 1 ? activeBg : 0x00000000);
        btnTabRegistry.setTextColor(tabIndex == 1 ? activeText : inactiveText);

        // Tab 2: Verify
        btnTabVerify.setBackgroundColor(tabIndex == 2 ? activeBg : 0x00000000);
        btnTabVerify.setTextColor(tabIndex == 2 ? activeText : inactiveText);

        if (tabIndex == 1) {
            loadIssuedCertificatesRegistry();
        }
    }

    private void refreshKpiMetrics() {
        int totalIssued = dbHelper.getCertificateCount();
        int eligibleStudents = dbHelper.getEligibleStudentCount();

        if (tvStatTotalIssued != null) tvStatTotalIssued.setText(String.valueOf(totalIssued));
        if (tvStatEligibleCandidates != null) tvStatEligibleCandidates.setText(String.valueOf(eligibleStudents));
        if (tvStatRecentCount != null) tvStatRecentCount.setText(String.valueOf(Math.min(totalIssued, 5)));
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
                    inspectStudentEligibility(s.username);
                    break;
                }
            }
        });
    }

    private void updateCourseDropdown(String username) {
        List<String> courses = new ArrayList<>();
        if (username != null) {
            Cursor c = dbHelper.getStudentEnrollments(username);
            if (c != null && c.moveToFirst()) {
                do {
                    courses.add(c.getString(c.getColumnIndexOrThrow("course_name")));
                } while (c.moveToNext());
                c.close();
            }
        }

        if (courses.isEmpty()) {
            courses.addAll(Arrays.asList("Mathematics 101", "Advanced Physics", "Biology Essentials", "Chemistry Mastery", "Social Studies", "General Excellence"));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, courses);
        actvCourseName.setAdapter(adapter);

        actvCourseName.setOnItemClickListener((parent, view, position, id) -> {
            if (targetUsername != null) {
                inspectStudentEligibility(targetUsername);
            }
        });
    }

    private void inspectStudentEligibility(String username) {
        if (cardEligibilityInspector == null || username == null) return;

        // 1. Payment status
        boolean hasApprovedPayment = false;
        String paymentPlan = "No active plan";
        Cursor cPay = null;
        try {
            cPay = dbHelper.getReadableDatabase().rawQuery("SELECT amount, status FROM payments WHERE username=? AND status='Approved' ORDER BY id DESC LIMIT 1", new String[]{username});
            if (cPay != null && cPay.moveToFirst()) {
                hasApprovedPayment = true;
                paymentPlan = cPay.getString(0) + " (Approved)";
            }
        } catch (Exception ignored) {
        } finally {
            if (cPay != null) cPay.close();
        }

        if (tvEligibilityPaymentStatus != null) {
            tvEligibilityPaymentStatus.setText("Payment Plan: " + paymentPlan);
        }

        // 2. Examination records
        String examSummary = "No exam history recorded yet";
        Cursor cExam = null;
        try {
            cExam = dbHelper.getStudentExamResults(username);
            if (cExam != null && cExam.moveToFirst()) {
                int examCount = cExam.getCount();
                String latestSubject = cExam.getString(cExam.getColumnIndexOrThrow("subject"));
                String latestScore = cExam.getString(cExam.getColumnIndexOrThrow("score_text"));
                examSummary = examCount + " exam(s) taken • Latest: " + latestSubject + " (" + latestScore + ")";
            }
        } catch (Exception ignored) {
        } finally {
            if (cExam != null) cExam.close();
        }

        if (tvEligibilityExamStatus != null) {
            tvEligibilityExamStatus.setText("Academic Record: " + examSummary);
        }

        // 3. Duplicate check for currently selected course
        String currentCourse = actvCourseName != null ? actvCourseName.getText().toString().trim() : "";
        boolean isDuplicate = !currentCourse.isEmpty() && dbHelper.hasCertificateForCourse(username, currentCourse);

        if (tvEligibilityDuplicateWarning != null) {
            if (isDuplicate) {
                tvEligibilityDuplicateWarning.setText("Notice: A certificate for '" + currentCourse + "' was already issued to this student.");
                tvEligibilityDuplicateWarning.setTextColor(ContextCompat.getColor(this, R.color.admin_orange));
            } else {
                tvEligibilityDuplicateWarning.setText("Prior Issuance: No duplicate detected for " + (currentCourse.isEmpty() ? "selected course" : "'" + currentCourse + "'"));
                tvEligibilityDuplicateWarning.setTextColor(0xFF059669);
            }
        }

        if (tvEligibilityStatusBadge != null) {
            if (!hasApprovedPayment) {
                tvEligibilityStatusBadge.setText("PAYMENT REQUIRED");
                tvEligibilityStatusBadge.setBackgroundResource(R.drawable.bg_badge_blocked);
                tvEligibilityStatusBadge.setTextColor(0xFFEF4444);
            } else if (isDuplicate) {
                tvEligibilityStatusBadge.setText("ALREADY ISSUED");
                tvEligibilityStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending);
                tvEligibilityStatusBadge.setTextColor(0xFFD97706);
            } else {
                tvEligibilityStatusBadge.setText("ELIGIBLE FOR ISSUANCE");
                tvEligibilityStatusBadge.setBackgroundResource(R.drawable.bg_badge_active);
                tvEligibilityStatusBadge.setTextColor(0xFF059669);
            }
        }
    }

    private void handleIssuePrecheck() {
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            Toast.makeText(this, "Please select a student from the dropdown", Toast.LENGTH_SHORT).show();
            return;
        }

        String studName = etStudentName.getText().toString().trim();
        String course = actvCourseName.getText().toString().trim();
        String certId = etCertificateId.getText().toString().trim();
        String title = etCertTitle.getText().toString().trim();
        String presentation = etPresentationText.getText().toString().trim();
        String body = etBodyText.getText().toString().trim();
        String date = etIssueDate.getText().toString().trim();
        String studId = etStudentId.getText().toString().trim();
        String director = etDirectorName.getText().toString().trim();
        String role = etDirectorRole.getText().toString().trim();

        if (studName.isEmpty() || course.isEmpty()) {
            Toast.makeText(this, "Student Name and Course are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (certId.isEmpty()) {
            generateFreshCertificateId();
            certId = etCertificateId.getText().toString().trim();
        }

        // Duplicate check
        boolean isDuplicate = dbHelper.hasCertificateForCourse(targetUsername, course);
        if (isDuplicate) {
            final String finalCertId = certId;
            new MaterialAlertDialogBuilder(this)
                .setTitle("Duplicate Credential Warning")
                .setMessage("Student '" + studName + "' already has an official certificate for '" + course + "'.\n\nDo you want to proceed and issue an additional certificate?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Proceed Anyway", (dialog, which) -> showFinalReviewDialog(title, presentation, studName, body, course, date, studId, finalCertId, director, role))
                .show();
        } else {
            showFinalReviewDialog(title, presentation, studName, body, course, date, studId, certId, director, role);
        }
    }

    private void showFinalReviewDialog(String title, String presentation, String studName, String body, String course, String date, String studId, String certId, String director, String role) {
        String reviewSummary = "Recipient: " + studName + " (" + targetUsername + ")\n" +
                               "Student ID: " + (studId.isEmpty() ? "N/A" : studId) + "\n" +
                               "Course / Subject: " + course + "\n" +
                               "Certificate Serial ID: " + certId + "\n" +
                               "Issue Date: " + date + "\n" +
                               "Signatory: " + director + " (" + role + ")\n\n" +
                               "This will permanently persist the credential to the Learnify database and notify the student.";

        new MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Certificate Issuance")
            .setMessage(reviewSummary)
            .setNegativeButton("Edit", null)
            .setPositiveButton("ISSUE NOW", (dialog, which) -> executeIssuance(title, presentation, studName, body, course, date, studId, certId, director, role))
            .show();
    }

    private void executeIssuance(String title, String presentation, String studName, String body, String course, String date, String studId, String certId, String director, String role) {
        // Ensure Certificate ID is collision-free
        if (dbHelper.isCertificateIdTaken(certId)) {
            generateFreshCertificateId();
            certId = etCertificateId.getText().toString().trim();
        }

        boolean success = dbHelper.saveFullCertificate(targetUsername, title, presentation, studName, body, course, date, studId, certId, director, role);

        if (success) {
            // Notify student
            dbHelper.addNotification(targetUsername, "Professional Certificate Issued",
                    "Your official certificate for " + course + " is now available in your portal.", "CERTIFICATE");

            // Audit log with authenticated admin username
            dbHelper.logAdminAction(currentAdminUsername, "Issued Professional Certificate", "Course: " + course + " to " + targetUsername + " (ID: " + certId + ")");

            Toast.makeText(this, "Certificate issued successfully.", Toast.LENGTH_LONG).show();

            // Refresh UI
            refreshKpiMetrics();
            loadIssuedCertificatesRegistry();
            generateFreshCertificateId();
            inspectStudentEligibility(targetUsername);

            // Switch to registry tab to show the new record
            switchTab(1);
        } else {
            Toast.makeText(this, "Error saving certificate to database", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadIssuedCertificatesRegistry() {
        allCertificatesList.clear();
        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllCertificates();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String uname = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String pres = cursor.getString(cursor.getColumnIndexOrThrow("presentation"));
                    String sName = cursor.getString(cursor.getColumnIndexOrThrow("student_name"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    String course = cursor.getString(cursor.getColumnIndexOrThrow("course"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    String sId = cursor.getString(cursor.getColumnIndexOrThrow("student_id"));
                    String cId = cursor.getString(cursor.getColumnIndexOrThrow("certificate_id"));
                    String dir = cursor.getString(cursor.getColumnIndexOrThrow("director"));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                    String timestamp = cursor.getColumnIndex("timestamp") != -1 ? cursor.getString(cursor.getColumnIndexOrThrow("timestamp")) : "";

                    allCertificatesList.add(new CertificateRecord(id, uname, title, pres, sName, body, course, date, sId, cId, dir, role, timestamp));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("AdminIssueCert", "Failed to load certificates registry", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        filterAndRenderRegistry(etSearchRegistry != null && etSearchRegistry.getText() != null ? etSearchRegistry.getText().toString() : "");
    }

    private void filterAndRenderRegistry(String query) {
        if (registryListContainer == null) return;
        registryListContainer.removeAllViews();

        String q = query != null ? query.toLowerCase().trim() : "";
        List<CertificateRecord> filtered = new ArrayList<>();
        for (CertificateRecord r : allCertificatesList) {
            if (q.isEmpty() ||
                r.studentName.toLowerCase().contains(q) ||
                r.username.toLowerCase().contains(q) ||
                r.course.toLowerCase().contains(q) ||
                r.certificateId.toLowerCase().contains(q)) {
                filtered.add(r);
            }
        }

        if (tvRegistryRecordsCount != null) {
            tvRegistryRecordsCount.setText(filtered.size() + (filtered.size() == 1 ? " record" : " records"));
        }

        if (tvNoRegistryCertificates != null) {
            if (filtered.isEmpty()) {
                tvNoRegistryCertificates.setVisibility(View.VISIBLE);
                if (!q.isEmpty()) {
                    tvNoRegistryCertificates.setText("No certificates match your search query '" + query + "'.");
                } else {
                    tvNoRegistryCertificates.setText("No certificates have been issued yet.\nEligible candidates can be issued certificates using the 'Issue New' tab.");
                }
            } else {
                tvNoRegistryCertificates.setVisibility(View.GONE);
            }
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (CertificateRecord record : filtered) {
            View itemView = inflater.inflate(R.layout.item_issued_certificate_admin, registryListContainer, false);
            TextView tvName = itemView.findViewById(R.id.tvCertStudentName);
            TextView tvCourse = itemView.findViewById(R.id.tvCertCourse);
            TextView tvId = itemView.findViewById(R.id.tvCertId);
            TextView tvDate = itemView.findViewById(R.id.tvCertDate);
            MaterialButton btnDetails = itemView.findViewById(R.id.btnViewCertDetails);

            if (tvName != null) tvName.setText(record.studentName + " (" + record.username + ")");
            if (tvCourse != null) tvCourse.setText(record.course);
            if (tvId != null) tvId.setText("ID: " + record.certificateId);
            if (tvDate != null) tvDate.setText("Issued: " + record.date);

            if (btnDetails != null) {
                btnDetails.setOnClickListener(v -> showCertificateDetailsDialog(record));
            }

            registryListContainer.addView(itemView);
        }
    }

    private void showCertificateDetailsDialog(CertificateRecord r) {
        String details = "Student Name: " + r.studentName + "\n" +
                         "Username: " + r.username + "\n" +
                         "Student ID: " + (r.studentId.isEmpty() ? "N/A" : r.studentId) + "\n" +
                         "Accredited Course: " + r.course + "\n" +
                         "Certificate Serial ID: " + r.certificateId + "\n" +
                         "Issue Date: " + r.date + "\n" +
                         "Title: " + r.title + "\n" +
                         "Signatory: " + r.director + " (" + r.role + ")\n" +
                         "Database Timestamp: " + (r.timestamp.isEmpty() ? "N/A" : r.timestamp);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Official Certificate Record")
            .setMessage(details)
            .setPositiveButton("Close", null)
            .show();
    }

    private void runAuthenticityVerification() {
        if (etVerifyCertId == null || cardVerificationResult == null) return;

        String queryId = etVerifyCertId.getText() != null ? etVerifyCertId.getText().toString().trim() : "";
        if (queryId.isEmpty()) {
            Toast.makeText(this, "Please enter a Certificate Serial ID to verify", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor c = null;
        try {
            c = dbHelper.getCertificateById(queryId);
            cardVerificationResult.setVisibility(View.VISIBLE);

            if (c != null && c.moveToFirst()) {
                String sName = c.getString(c.getColumnIndexOrThrow("student_name"));
                String uname = c.getString(c.getColumnIndexOrThrow("username"));
                String course = c.getString(c.getColumnIndexOrThrow("course"));
                String date = c.getString(c.getColumnIndexOrThrow("date"));
                String certId = c.getString(c.getColumnIndexOrThrow("certificate_id"));
                String dir = c.getString(c.getColumnIndexOrThrow("director"));
                String timestamp = c.getColumnIndex("timestamp") != -1 ? c.getString(c.getColumnIndexOrThrow("timestamp")) : "";

                if (tvVerifyResultTitle != null) tvVerifyResultTitle.setText("Certificate Authenticated");
                if (tvVerifyResultStatusBadge != null) {
                    tvVerifyResultStatusBadge.setText("AUTHENTIC RECORD");
                    tvVerifyResultStatusBadge.setBackgroundResource(R.drawable.bg_badge_active);
                    tvVerifyResultStatusBadge.setTextColor(0xFF059669);
                }

                if (tvVerifyResultDetails != null) {
                    String d = "Verified in local Learnify registry:\n\n" +
                               "• Serial ID: " + certId + "\n" +
                               "• Recipient: " + sName + " (" + uname + ")\n" +
                               "• Course: " + course + "\n" +
                               "• Date Awarded: " + date + "\n" +
                               "• Issuing Authority: " + dir + "\n" +
                               "• Registry Record Created: " + (timestamp.isEmpty() ? date : timestamp);
                    tvVerifyResultDetails.setText(d);
                    tvVerifyResultDetails.setTextColor(ContextCompat.getColor(this, R.color.admin_text_primary));
                }
            } else {
                if (tvVerifyResultTitle != null) tvVerifyResultTitle.setText("Verification Notice");
                if (tvVerifyResultStatusBadge != null) {
                    tvVerifyResultStatusBadge.setText("NOT FOUND");
                    tvVerifyResultStatusBadge.setBackgroundResource(R.drawable.bg_badge_blocked);
                    tvVerifyResultStatusBadge.setTextColor(0xFFEF4444);
                }

                if (tvVerifyResultDetails != null) {
                    String d = "No certificate matching serial ID '" + queryId + "' exists in the Learnify database.\n\n" +
                               "This credential could not be authenticated. Ensure the serial ID is entered accurately (e.g. LNYF-ABCD1234).";
                    tvVerifyResultDetails.setText(d);
                    tvVerifyResultDetails.setTextColor(0xFFEF4444);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lookup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (c != null) c.close();
        }
    }

    private static class StudentItem {
        String username, fullName, uniqueId;
        StudentItem(String u, String f, String id) { username = u; fullName = f; uniqueId = id; }
    }

    public static class CertificateRecord {
        public final int id;
        public final String username, title, presentation, studentName, body, course, date, studentId, certificateId, director, role, timestamp;
        public CertificateRecord(int id, String u, String t, String p, String sn, String b, String c, String d, String sid, String cid, String dir, String r, String ts) {
            this.id = id; this.username = u; this.title = t; this.presentation = p; this.studentName = sn;
            this.body = b; this.course = c; this.date = d; this.studentId = sid; this.certificateId = cid;
            this.director = dir; this.role = r; this.timestamp = ts;
        }
    }
}
