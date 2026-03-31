package com.million.learnify;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminUserManagementActivity extends AppCompatActivity {

    private LinearLayout userListContainer;
    private DatabaseHelper dbHelper;
    private TextInputEditText etSearchUser;
    private TextView tvUserCount;
    private ChipGroup chipGroupStatus;
    private Chip chipCourseFilter;
    private List<User> allUsers = new ArrayList<>();
    private Map<String, Set<String>> userEnrollments = new HashMap<>();
    
    private String currentStatusFilter = "All";
    private String currentCourseFilter = "All";
    private String currentUserTypeFilter = "STUDENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        dbHelper = new DatabaseHelper(this);
        setupToolbar();
        initViews();
        loadAllUsers();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.userMgmtToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Administration");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        userListContainer = findViewById(R.id.userListContainer);
        etSearchUser = findViewById(R.id.etSearchUser);
        tvUserCount = findViewById(R.id.tvUserCount);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        chipCourseFilter = findViewById(R.id.chipCourseFilter);
        MaterialButton btnExportData = findViewById(R.id.btnExportData);

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterUsers(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipActive) currentStatusFilter = "Active";
            else if (checkedId == R.id.chipBlocked) currentStatusFilter = "Blocked";
            else currentStatusFilter = "All";
            filterUsers();
        });

        chipCourseFilter.setOnClickListener(v -> showCourseFilterDialog());

        btnExportData.setOnClickListener(v -> {
            StringBuilder csv = new StringBuilder("Name,Email,ID,Status,Type,Joined\n");
            for (User u : allUsers) {
                csv.append(u.name).append(",").append(u.email).append(",").append(u.id)
                   .append(",").append(u.status).append(",").append(currentUserTypeFilter)
                   .append(",").append(u.createdAt).append("\n");
            }
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, csv.toString());
            sendIntent.setType("text/csv");
            startActivity(Intent.createChooser(sendIntent, "Export User List"));
        });

        tvUserCount.setOnClickListener(v -> {
            String[] types = {"STUDENT", "INSTRUCTOR", "ADMIN"};
            new MaterialAlertDialogBuilder(this)
                .setTitle("Select User Category")
                .setItems(types, (d, which) -> {
                    currentUserTypeFilter = types[which];
                    loadAllUsers();
                }).show();
        });
    }

    private void showCourseFilterDialog() {
        Set<String> courses = new HashSet<>();
        courses.add("All");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT course_name FROM enrollments", null);
        while (c.moveToNext()) courses.add(c.getString(0));
        c.close();

        String[] courseArray = courses.toArray(new String[0]);
        new MaterialAlertDialogBuilder(this)
            .setTitle("Filter by Enrollment")
            .setItems(courseArray, (d, which) -> {
                currentCourseFilter = courseArray[which];
                chipCourseFilter.setText("Course: " + currentCourseFilter);
                filterUsers();
            }).show();
    }

    private void loadAllUsers() {
        allUsers.clear();
        userEnrollments.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.rawQuery("SELECT full_name, unique_id, grade_or_role, email, profile_image, status, username, created_at FROM users WHERE user_type=?", new String[]{currentUserTypeFilter});
        if (cursor.moveToFirst()) {
            do {
                allUsers.add(new User(
                    cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5), 
                    cursor.getString(6), cursor.getString(7)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (currentUserTypeFilter.equals("STUDENT")) {
            Cursor enrCursor = db.rawQuery("SELECT username, course_name FROM enrollments", null);
            while (enrCursor.moveToNext()) {
                String uname = enrCursor.getString(0);
                String cname = enrCursor.getString(1);
                if (!userEnrollments.containsKey(uname)) userEnrollments.put(uname, new HashSet<>());
                userEnrollments.get(uname).add(cname);
            }
            enrCursor.close();
        }
        filterUsers();
    }

    private void filterUsers() {
        String searchText = etSearchUser.getText().toString().toLowerCase();
        List<User> filtered = new ArrayList<>();
        
        for (User u : allUsers) {
            boolean matchesSearch = u.name.toLowerCase().contains(searchText) || 
                                   u.email.toLowerCase().contains(searchText) ||
                                   u.id.toLowerCase().contains(searchText);
            
            boolean matchesStatus = currentStatusFilter.equals("All") || u.status.equalsIgnoreCase(currentStatusFilter);
            
            boolean matchesCourse = true;
            if (!currentCourseFilter.equals("All")) {
                Set<String> enrolled = userEnrollments.get(u.username);
                matchesCourse = enrolled != null && enrolled.contains(currentCourseFilter);
            }

            if (matchesSearch && matchesStatus && matchesCourse) {
                filtered.add(u);
            }
        }
        displayUsers(filtered);
    }

    private void displayUsers(List<User> list) {
        userListContainer.removeAllViews();
        tvUserCount.setText("Managing " + currentUserTypeFilter + "s: " + list.size() + " (Tap to Change)");
        for (User u : list) {
            addUserRow(u);
        }
    }

    private void addUserRow(User u) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_admin_user, userListContainer, false);
        ((TextView) row.findViewById(R.id.tvUserName)).setText(u.name);
        ((TextView) row.findViewById(R.id.tvUserRole)).setText(u.id + " | " + u.grade);
        TextView tvStatus = row.findViewById(R.id.tvUserStatus);
        
        if (u.status.equalsIgnoreCase("Active")) {
            tvStatus.setText("🟢 ACTIVE");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStatus.setText("🔴 BLOCKED");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        ImageView ivAvatar = row.findViewById(R.id.ivUserAvatar);
        if (u.img != null && !u.img.isEmpty()) {
            try { ivAvatar.setImageURI(Uri.parse(u.img)); } catch (Exception e) {}
        }

        row.setOnClickListener(v -> showUserProfile(u));
        row.findViewById(R.id.btnEditUser).setOnClickListener(v -> showQuickActions(u));
        userListContainer.addView(row);
    }

    private void showUserProfile(User u) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_student_profile_admin, null);
        
        TextView tvName = dialogView.findViewById(R.id.tvProfileName);
        TextView tvDetails = dialogView.findViewById(R.id.tvProfileDetails);
        LinearLayout enrollmentsContainer = dialogView.findViewById(R.id.enrollmentsContainer);
        LinearLayout examsContainer = dialogView.findViewById(R.id.examsContainer);
        LinearLayout paymentsContainer = dialogView.findViewById(R.id.paymentsContainer);
        
        tvName.setText(u.name);
        tvDetails.setText("Email: " + u.email + "\nUsername: " + u.username + "\nJoined: " + u.createdAt);

        // Enrollments
        Cursor enc = dbHelper.getStudentEnrollments(u.username);
        while (enc.moveToNext()) {
            TextView tv = new TextView(this);
            tv.setText("📖 " + enc.getString(enc.getColumnIndexOrThrow("course_name")));
            tv.setTextColor(getResources().getColor(R.color.primary_indigo));
            tv.setPadding(0, 4, 0, 4);
            enrollmentsContainer.addView(tv);
        }
        if (enrollmentsContainer.getChildCount() == 0) enrollmentsContainer.addView(createEmptyView("No course enrollments."));
        enc.close();

        // Exams/Progress (Showing Certificates)
        Cursor examc = dbHelper.getStudentExamResults(u.username);
        while (examc.moveToNext()) {
            TextView tv = new TextView(this);
            String sub = examc.getString(examc.getColumnIndexOrThrow("subject"));
            String score = examc.getString(examc.getColumnIndexOrThrow("score"));
            String date = examc.getString(examc.getColumnIndexOrThrow("date"));
            tv.setText("🏆 " + sub + ": " + score + " (Certificate Issued: " + date + ")");
            tv.setPadding(0, 4, 0, 4);
            examsContainer.addView(tv);
        }
        if (examsContainer.getChildCount() == 0) examsContainer.addView(createEmptyView("No exam/progress records."));
        examc.close();

        // Payments
        Cursor payc = dbHelper.getStudentPayments(u.username);
        while (payc.moveToNext()) {
            TextView tv = new TextView(this);
            tv.setText("💰 " + payc.getString(payc.getColumnIndexOrThrow("amount")) + " ETB [" + payc.getString(payc.getColumnIndexOrThrow("status")) + "]");
            tv.setPadding(0, 4, 0, 4);
            paymentsContainer.addView(tv);
        }
        if (paymentsContainer.getChildCount() == 0) paymentsContainer.addView(createEmptyView("No payment history."));
        payc.close();

        new MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("QUICK ACTIONS", (d, w) -> showQuickActions(u))
            .setNegativeButton("CLOSE", null)
            .show();
    }

    private TextView createEmptyView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        return tv;
    }

    private void showQuickActions(User u) {
        List<String> actionsList = new ArrayList<>();
        actionsList.add(u.status.equalsIgnoreCase("Active") ? "Suspend Account" : "Activate Account");
        
        if (currentUserTypeFilter.equals("STUDENT")) {
            actionsList.add("Issue Individual Certificate");
            actionsList.add("Send Personal Info/File");
            actionsList.add("Manual Enrollment");
            actionsList.add("Unenroll from Course");
        }
        
        actionsList.add("Reset Password (123456)");
        actionsList.add("Impersonate Student (Support)");
        actionsList.add("Delete Account Permanently");

        String[] actions = actionsList.toArray(new String[0]);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Admin Control Panel: " + u.name)
            .setItems(actions, (dialog, which) -> {
                String action = actions[which];
                if (action.contains("Suspend") || action.contains("Activate")) toggleUserStatus(u);
                else if (action.contains("Issue Individual Certificate")) openCertificateIssuer(u);
                else if (action.contains("Send Personal Info/File")) sendPersonalInfo(u);
                else if (action.contains("Manual Enrollment")) showEnrollDialog(u);
                else if (action.contains("Unenroll")) showUnenrollDialog(u);
                else if (action.contains("Reset Password")) resetPassword(u);
                else if (action.contains("Impersonate")) impersonateUser(u);
                else if (action.contains("Delete")) deleteUser(u);
            })
            .show();
    }

    private void openCertificateIssuer(User u) {
        Intent intent = new Intent(this, AdminIssueCertificateActivity.class);
        intent.putExtra("TARGET_USERNAME", u.username);
        intent.putExtra("TARGET_NAME", u.name);
        startActivity(intent);
    }

    private void sendPersonalInfo(User u) {
        // Individualized data sending logic
        new MaterialAlertDialogBuilder(this)
            .setTitle("Send Info to " + u.name)
            .setMessage("Select file or enter information to broadcast to this specific student port.")
            .setPositiveButton("SELECT FILE", (d, w) -> {
                Toast.makeText(this, "Opening Secure File Manager for " + u.name, Toast.LENGTH_SHORT).show();
                // Logic to upload a specific file for this user
                dbHelper.addNotification(u.username, "New File Received", "Admin has sent you a private document. Check your downloads.", "FILE");
            })
            .setNeutralButton("SEND MESSAGE", (d, w) -> {
                Toast.makeText(this, "Message sent to " + u.name, Toast.LENGTH_SHORT).show();
                dbHelper.addNotification(u.username, "Message from Admin", "Important update regarding your account status.", "INFO");
            })
            .show();
    }

    private void toggleUserStatus(User u) {
        String newStatus = u.status.equalsIgnoreCase("Active") ? "Blocked" : "Active";
        dbHelper.updateUserStatus(u.username, newStatus);
        loadAllUsers();
        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }

    private void showEnrollDialog(User u) {
        String[] courses = {"Grade 9 Full Pack", "Grade 10 Full Pack", "Grade 11 Full Pack", "Grade 12 Full Pack", "Advanced STEM", "Ministry Exam Master"};
        new MaterialAlertDialogBuilder(this)
            .setTitle("Enroll " + u.name)
            .setItems(courses, (d, w) -> {
                dbHelper.enrollStudent(u.username, courses[w]);
                Toast.makeText(this, "Success: Student enrolled.", Toast.LENGTH_SHORT).show();
                loadAllUsers();
            }).show();
    }

    private void showUnenrollDialog(User u) {
        List<String> userCourses = new ArrayList<>();
        Cursor c = dbHelper.getStudentEnrollments(u.username);
        while (c.moveToNext()) userCourses.add(c.getString(c.getColumnIndexOrThrow("course_name")));
        c.close();
        
        if (userCourses.isEmpty()) {
            Toast.makeText(this, "No active enrollments found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] courseArray = userCourses.toArray(new String[0]);
        new MaterialAlertDialogBuilder(this)
            .setTitle("Unenroll Student")
            .setItems(courseArray, (d, w) -> {
                dbHelper.unenrollStudent(u.username, courseArray[w]);
                Toast.makeText(this, "Enrollment removed successfully.", Toast.LENGTH_SHORT).show();
                loadAllUsers();
            }).show();
    }

    private void resetPassword(User u) {
        dbHelper.updatePassword(u.username, "123456");
        Toast.makeText(this, "Password reset to default (123456)", Toast.LENGTH_SHORT).show();
    }

    private void impersonateUser(User u) {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
            .putString("username", u.username)
            .putString("full_name", u.name)
            .putString("user_type", "STUDENT")
            .putString("grade", u.grade)
            .putBoolean("is_impersonating", true)
            .apply();
        
        Toast.makeText(this, "Switching to Student Dashboard...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void deleteUser(User u) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure? This will wipe all " + u.name + "\'s records.")
            .setPositiveButton("DELETE", (d, w) -> {
                dbHelper.getWritableDatabase().delete("users", "username=?", new String[]{u.username});
                loadAllUsers();
                Toast.makeText(this, "User deleted.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }

    private static class User {
        String name, id, grade, email, img, status, username, createdAt;
        User(String n, String i, String g, String e, String im, String s, String un, String ca) {
            name = n; id = i; grade = g; email = e; img = im; status = s; username = un; createdAt = ca;
        }
    }
}
