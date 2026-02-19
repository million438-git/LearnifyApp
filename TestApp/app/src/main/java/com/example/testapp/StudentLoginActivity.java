package com.example.testapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class StudentLoginActivity extends AppCompatActivity {

    private TextInputEditText etStudentUser, etStudentPassword;
    private MaterialButton btnStudentLogin;
    private TextView tvRegisterRedirect, tvForgotPassword;
    private ImageView loginProfileIcon;
    private CheckBox cbRememberMe;
    private DatabaseHelper dbHelper;
    private SharedPreferences loginPrefs;
    private static final String PREFS_NAME = "LoginPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        applyAppTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        dbHelper = new DatabaseHelper(this);
        loginPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        initViews();
        loadRememberedData();
    }

    private void applyAppTheme() {
        SettingsService settingsService = new SettingsService(this);
        StudentSettings settings = settingsService.loadSettings();
        if ("dark".equals(settings.getThemeMode())) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void initViews() {
        etStudentUser = findViewById(R.id.etStudentUser);
        etStudentPassword = findViewById(R.id.etStudentPassword);
        btnStudentLogin = findViewById(R.id.btnStudentLogin);
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        loginProfileIcon = findViewById(R.id.loginProfileIcon);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        // Dynamic Profile Image Loading
        etStudentUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProfileIcon(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnStudentLogin.setOnClickListener(v -> handleLogin());

        tvRegisterRedirect.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void loadRememberedData() {
        boolean isRemembered = loginPrefs.getBoolean("remember", false);
        if (isRemembered) {
            etStudentUser.setText(loginPrefs.getString("username", ""));
            etStudentPassword.setText(loginPrefs.getString("password", ""));
            cbRememberMe.setChecked(true);
            updateProfileIcon(etStudentUser.getText().toString().trim());
        }
    }

    private void updateProfileIcon(String username) {
        if (username.isEmpty()) {
            loginProfileIcon.setImageResource(android.R.drawable.ic_menu_camera);
            return;
        }

        String imageUriStr = dbHelper.getUserProfileImage(username);
        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            try {
                loginProfileIcon.setImageURI(Uri.parse(imageUriStr));
            } catch (Exception e) {
                loginProfileIcon.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            loginProfileIcon.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    private void handleLogin() {
        String username = etStudentUser.getText().toString().trim();
        String password = etStudentPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use 'STUDENT' type for verification
        if (dbHelper.checkUser(username, password, "STUDENT")) {
            // Handle Remember Me logic
            SharedPreferences.Editor editor = loginPrefs.edit();
            if (cbRememberMe.isChecked()) {
                editor.putBoolean("remember", true);
                editor.putString("username", username);
                editor.putString("password", password);
            } else {
                editor.clear();
            }
            editor.apply();

            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            pref.edit().putString("username", username).putBoolean("isLoggedIn", true).apply();

            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid student credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
