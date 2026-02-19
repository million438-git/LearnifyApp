package com.example.testapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

/**
 * AdminUserManagementActivity - Provides detailed oversight of all registered students.
 * Organizes students by their specific grades (9-12) for professional management.
 */
public class AdminUserManagementActivity extends AppCompatActivity {

    private LinearLayout userListContainer;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        dbHelper = new DatabaseHelper(this);
        setupToolbar();
        initViews();
        loadAllDetailedUsers();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.userMgmtToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Student Enrollment Registry");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        userListContainer = findViewById(R.id.userListContainer);
        MaterialButton btnAddUser = findViewById(R.id.btnAddUser);
        btnAddUser.setOnClickListener(v -> Toast.makeText(this, "Opening Enrollment Portal...", Toast.LENGTH_SHORT).show());
    }

    private void loadAllDetailedUsers() {
        userListContainer.removeAllViews();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Fetch detailed information for all students and admins
        Cursor cursor = db.rawQuery("SELECT full_name, unique_id, grade_or_role, email, profile_image, status, user_type FROM users ORDER BY grade_or_role ASC, full_name ASC", null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String uid = cursor.getString(1);
                String gradeRole = cursor.getString(2);
                String email = cursor.getString(3);
                String imgUri = cursor.getString(4);
                String status = cursor.getString(5);
                String type = cursor.getString(6);

                addUserDetailRow(name, uid, gradeRole, email, imgUri, status, type);
            } while (cursor.moveToNext());
        } else {
            showEmptyState();
        }
        cursor.close();
    }

    private void addUserDetailRow(String name, String uid, String grade, String email, String imgUri, String status, String type) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_admin_user, userListContainer, false);
        
        TextView tvName = row.findViewById(R.id.tvUserName);
        TextView tvRole = row.findViewById(R.id.tvUserRole);
        TextView tvStatus = row.findViewById(R.id.tvUserStatus);
        ImageView ivAvatar = row.findViewById(R.id.ivUserAvatar);

        // Professional Detailed Display
        tvName.setText(name);
        String detailedInfo = "ID: " + uid + " | " + (type.equals("STUDENT") ? "Grade " + grade : grade);
        tvRole.setText(detailedInfo + "\n" + email);
        
        tvStatus.setText(status.equals("Active") ? "🟢 ACTIVE" : "🔴 RESTRICTED");

        if (imgUri != null && !imgUri.isEmpty()) {
            try {
                ivAvatar.setImageURI(Uri.parse(imgUri));
                ivAvatar.clearColorFilter();
            } catch (Exception e) {
                ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
        }

        row.setOnClickListener(v -> Toast.makeText(this, "Profile: " + name + " (" + email + ")", Toast.LENGTH_LONG).show());
        userListContainer.addView(row);
    }

    private void showEmptyState() {
        TextView tv = new TextView(this);
        tv.setText("No students registered in the database.");
        tv.setPadding(0, 100, 0, 0);
        tv.setGravity(android.view.Gravity.CENTER);
        userListContainer.addView(tv);
    }
}
