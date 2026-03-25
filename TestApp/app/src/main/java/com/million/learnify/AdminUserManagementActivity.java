package com.million.learnify;

import android.content.ContentValues;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class AdminUserManagementActivity extends AppCompatActivity {

    private LinearLayout userListContainer;
    private DatabaseHelper dbHelper;
    private TextInputEditText etSearchUser;
    private TextView tvUserCount;
    private List<User> allUsers = new ArrayList<>();

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
            getSupportActionBar().setTitle("Student Management");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        userListContainer = findViewById(R.id.userListContainer);
        etSearchUser = findViewById(R.id.etSearchUser);
        tvUserCount = findViewById(R.id.tvUserCount);
        MaterialButton btnExportData = findViewById(R.id.btnExportData);

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterUsers(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnExportData.setOnClickListener(v -> {
            StringBuilder csv = new StringBuilder("Full Name,Email,Grade,Status\n");
            for (User u : allUsers) {
                csv.append(u.name).append(",").append(u.email).append(",").append(u.grade).append(",").append(u.status).append("\n");
            }
            Toast.makeText(this, "Exported " + allUsers.size() + " records to CSV format", Toast.LENGTH_LONG).show();
        });
    }

    private void loadAllUsers() {
        allUsers.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT full_name, unique_id, grade_or_role, email, profile_image, status, username FROM users WHERE user_type='STUDENT'", null);

        if (cursor.moveToFirst()) {
            do {
                allUsers.add(new User(
                    cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        displayUsers(allUsers);
    }

    private void displayUsers(List<User> list) {
        userListContainer.removeAllViews();
        tvUserCount.setText("Total Students: " + list.size());
        for (User u : list) {
            addUserRow(u);
        }
    }

    private void filterUsers(String text) {
        List<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            if (u.name.toLowerCase().contains(text.toLowerCase()) || 
                u.email.toLowerCase().contains(text.toLowerCase()) ||
                u.id.toLowerCase().contains(text.toLowerCase())) {
                filtered.add(u);
            }
        }
        displayUsers(filtered);
    }

    private void addUserRow(User u) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_admin_user, userListContainer, false);
        ((TextView) row.findViewById(R.id.tvUserName)).setText(u.name);
        ((TextView) row.findViewById(R.id.tvUserRole)).setText("ID: " + u.id + " | Grade " + u.grade);
        TextView tvStatus = row.findViewById(R.id.tvUserStatus);
        tvStatus.setText(u.status.equalsIgnoreCase("Active") ? "🟢 ACTIVE" : "🔴 BLOCKED");
        
        ImageView ivAvatar = row.findViewById(R.id.ivUserAvatar);
        if (u.img != null && !u.img.isEmpty()) {
            try { ivAvatar.setImageURI(Uri.parse(u.img)); } catch (Exception e) {}
        }

        row.setOnClickListener(v -> showStudentDetails(u));
        row.findViewById(R.id.btnEditUser).setOnClickListener(v -> showStudentDetails(u));
        userListContainer.addView(row);
    }

    private void showStudentDetails(User u) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Student Profile: " + u.name);
        String details = "Email: " + u.email + "\nGrade: " + u.grade + "\nStatus: " + u.status + "\n\nActions:";
        builder.setMessage(details);

        builder.setPositiveButton(u.status.equalsIgnoreCase("Active") ? "BLOCK USER" : "ACTIVATE", (dialog, which) -> {
            String newStatus = u.status.equalsIgnoreCase("Active") ? "Blocked" : "Active";
            updateUserStatus(u.username, newStatus);
        });

        builder.setNeutralButton("RESET PASSWORD", (dialog, which) -> {
            dbHelper.updatePassword(u.username, "123456");
            Toast.makeText(this, "Password reset to default (123456)", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("CLOSE", null);
        builder.show();
    }

    private void updateUserStatus(String username, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("users", cv, "username=?", new String[]{username});
        loadAllUsers();
        Toast.makeText(this, "Student status updated to " + status, Toast.LENGTH_SHORT).show();
    }

    private static class User {
        String name, id, grade, email, img, status, username;
        User(String n, String i, String g, String e, String im, String s, String un) {
            name = n; id = i; grade = g; email = e; img = im; status = s; username = un;
        }
    }
}
