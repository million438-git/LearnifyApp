package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * StudentLoginActivity - Updated with Social Login Functionality.
 */
public class StudentLoginActivity extends BaseActivity {

    private EditText etStudentUser, etStudentPassword;
    private MaterialButton btnStudentLogin;
    private CircularProgressIndicator loginProgress;
    private TextView tvRegisterRedirect, tvForgotPassword;
    private ImageView loginProfileIcon, btnGoogleLogin, btnLinkedinLogin;
    private CheckBox cbRememberMe;
    private DatabaseHelper dbHelper;
    private SharedPreferences loginPrefs;
    private static final String PREFS_NAME = "LoginPrefs";
    
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private String currentPhotoPath;

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
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnLinkedinLogin = findViewById(R.id.btnLinkedinLogin);

        if (loginProfileIcon != null) {
            loginProfileIcon.setOnClickListener(v -> dispatchTakePictureIntent());
        }

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
            btnStudentLogin.setOnClickListener(v -> handleLogin());
        }
        
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> handleSocialLogin("Google"));
        }

        if (btnLinkedinLogin != null) {
            btnLinkedinLogin.setOnClickListener(v -> handleSocialLogin("LinkedIn"));
        }

        if (tvRegisterRedirect != null) tvRegisterRedirect.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        if (tvForgotPassword != null) tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void handleSocialLogin(String provider) {
        setLoading(true);
        Toast.makeText(this, "Connecting to " + provider + "...", Toast.LENGTH_SHORT).show();
        
        // Simulate social login process
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String mockEmail = provider.toLowerCase() + "_user@example.com";
            String mockName = provider + " User";
            
            // Check if user exists or create a placeholder for simulation
            if (!dbHelper.checkUser(mockEmail, "social_pass", "STUDENT")) {
                 // In a real app, we'd sign up here. For simulation, we'll just log in as a guest
                 Log.d("SocialLogin", "New social user: " + mockName);
            }
            
            setLoading(false);
            onSocialLoginSuccess(mockEmail, mockName);
        }, 1500);
    }

    private void onSocialLoginSuccess(String email, String name) {
        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
            .putString("username", email)
            .putString("full_name", name)
            .putString("grade", "10")
            .putString("user_type", "STUDENT")
            .putBoolean("isLoggedIn", true)
            .apply();

        Toast.makeText(this, "Logged in via " + (email.contains("google") ? "Google" : "LinkedIn"), Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(StudentLoginActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("StudentLogin", "Error creating image file", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (loginProfileIcon != null && currentPhotoPath != null) {
                loginProfileIcon.setImageURI(Uri.fromFile(new File(currentPhotoPath)));
            }
        }
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
                loginProfileIcon.setImageResource(R.drawable.ic_real_camera);
            }
        } catch (Exception e) {
            loginProfileIcon.setImageResource(R.drawable.ic_real_camera);
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
            btnStudentLogin.setText(loading ? "" : "Login");
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
        finish();
    }
}
