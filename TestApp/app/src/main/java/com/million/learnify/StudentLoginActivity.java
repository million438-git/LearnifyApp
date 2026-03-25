package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * StudentLoginActivity - With normal system transitions.
 */
public class StudentLoginActivity extends BaseActivity {

    private TextInputEditText etStudentUser, etStudentPassword;
    private MaterialButton btnStudentLogin;
    private CircularProgressIndicator loginProgress;
    private TextView tvRegisterRedirect, tvForgotPassword;
    private ImageView loginProfileIcon;
    private CheckBox cbRememberMe;
    private DatabaseHelper dbHelper;
    private SharedPreferences loginPrefs;
    private static final String PREFS_NAME = "LoginPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_student_login);
            dbHelper = new DatabaseHelper(this);
            loginPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            initViews();
            loadRememberedData();
        } catch (Exception e) {
            Log.e("StudentLogin", "Initialization Error", e);
            Toast.makeText(this, "System Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        etStudentUser = findViewById(R.id.etStudentUser);
        etStudentPassword = findViewById(R.id.etStudentPassword);
        btnStudentLogin = findViewById(R.id.btnStudentLogin);
        loginProgress = findViewById(R.id.loginProgress);
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        loginProfileIcon = findViewById(R.id.loginProfileIcon);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        if (etStudentUser != null) {
            etStudentUser.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateProfileIcon(s.toString().trim());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnStudentLogin != null) {
            btnStudentLogin.setOnClickListener(v -> {
                handleLogin();
            });
        }
        
        if (tvRegisterRedirect != null) tvRegisterRedirect.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        if (tvForgotPassword != null) tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void loadRememberedData() {
        if (loginPrefs != null && etStudentUser != null && etStudentPassword != null) {
            boolean isRemembered = loginPrefs.getBoolean("remember", false);
            if (isRemembered) {
                etStudentUser.setText(loginPrefs.getString("username", ""));
                etStudentPassword.setText(loginPrefs.getString("password", ""));
                cbRememberMe.setChecked(true);
                updateProfileIcon(etStudentUser.getText().toString().trim());
            }
        }
    }

    private void updateProfileIcon(String username) {
        if (loginProfileIcon == null || username.isEmpty()) return;
        try {
            String imageUriStr = dbHelper.getUserProfileImage(username);
            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                loginProfileIcon.setImageURI(Uri.parse(imageUriStr));
            } else {
                loginProfileIcon.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } catch (Exception e) {
            loginProfileIcon.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    private void handleLogin() {
        String input = etStudentUser != null ? etStudentUser.getText().toString().trim() : "";
        String password = etStudentPassword != null ? etStudentPassword.getText().toString().trim() : "";

        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dbHelper.checkUser(input, password, "STUDENT")) {
                onLoginSuccess(input, password);
            } else {
                setLoading(false);
                Toast.makeText(this, "Invalid student credentials", Toast.LENGTH_SHORT).show();
            }
        }, 800);
    }

    private void setLoading(boolean loading) {
        if (btnStudentLogin != null) {
            btnStudentLogin.setText(loading ? "" : "Sign In Now");
            btnStudentLogin.setEnabled(!loading);
        }
        if (loginProgress != null) {
            loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void onLoginSuccess(String input, String password) {
        String fullName = "AceGrade Student";
        String username = input;
        String grade = "10";
        
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor userCursor = db.rawQuery("SELECT username, full_name, grade_or_role FROM users WHERE (username=? OR email=?) AND password=? AND user_type='STUDENT'", new String[]{input, input, password})) {
            
            if (userCursor != null && userCursor.moveToFirst()) {
                username = userCursor.getString(0);
                fullName = userCursor.getString(1);
                grade = userCursor.getString(2);
            }
        } catch (Exception e) {
            Log.e("StudentLogin", "DB Session Query Failed", e);
        }

        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
            .putString("username", username != null ? username : input)
            .putString("full_name", fullName != null ? fullName : "Student")
            .putString("grade", grade != null ? grade : "10")
            .putString("user_type", "STUDENT")
            .putBoolean("isLoggedIn", true)
            .apply();

        if (cbRememberMe != null && cbRememberMe.isChecked()) {
            loginPrefs.edit()
                .putString("username", input)
                .putString("password", password)
                .putBoolean("remember", true)
                .apply();
        } else {
            loginPrefs.edit().clear().apply();
        }

        Toast.makeText(this, "Welcome to Learnify!", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(StudentLoginActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // System default transition
        finish();
    }
}
