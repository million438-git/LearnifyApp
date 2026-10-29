
package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

/**
 * StudentLoginActivity
 *
 * Handles student authentication through Firebase,
 * legacy local-account migration, email verification, and session management.
 *
 * Existing Learnify authentication flows are preserved.
 */
public class StudentLoginActivity extends BaseActivity {

    private static final String TAG = "StudentLogin";

    private EditText etStudentUser;
    private EditText etStudentPassword;
    private MaterialButton btnStudentLogin;
    private CircularProgressIndicator loginProgress;
    private TextView tvRegisterRedirect;
    private View btnLinkedinLogin;
    private CheckBox cbRememberMe;

    private SharedPreferences loginPrefs;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth.
        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);

        // Preserve the existing verified student-session redirect.
        SharedPreferences session =
                getSharedPreferences("UserSession", MODE_PRIVATE);

        FirebaseUser currentUser = firebaseAuth != null
                ? firebaseAuth.getCurrentUser()
                : null;

        if (session.getBoolean("isLoggedIn", false)
                && "STUDENT".equals(session.getString("user_type", ""))
                && currentUser != null
                && currentUser.isEmailVerified()) {
            navigateToDashboard();
            return;
        }

        // Preserve maintenance mode.
        if ("1".equals(repository.getSystemSetting("maintenance_mode", "0"))) {
            setContentView(R.layout.layout_maintenance);
            return;
        }

        try {
            setContentView(R.layout.activity_student_login);

            loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

            initViews();
            loadRememberedData();

        } catch (Exception e) {
            Log.e(TAG, "Student login initialization failed", e);
            Toast.makeText(
                    this,
                    "Unable to initialize the login screen. Please reopen Learnify.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    private void initViews() {
        etStudentUser = findViewById(R.id.etStudentUser);
        etStudentPassword = findViewById(R.id.etStudentPassword);
        btnStudentLogin = findViewById(R.id.btnStudentLogin);
        loginProgress = findViewById(R.id.loginProgress);
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect);
        btnLinkedinLogin = findViewById(R.id.btnLinkedinLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        if (etStudentUser == null
                || etStudentPassword == null
                || btnStudentLogin == null
                || loginProgress == null
                || tvRegisterRedirect == null
                || cbRememberMe == null) {
            throw new IllegalStateException(
                    "A required student login view is missing from the layout."
            );
        }

        btnStudentLogin.setOnClickListener(
                v -> handleTraditionalLogin()
        );

        if (btnLinkedinLogin != null) {
            btnLinkedinLogin.setOnClickListener(
                    v -> startLinkedInAuth()
            );
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvRegisterRedirect.setOnClickListener(v -> {
            if ("1".equals(
                    repository.getSystemSetting("allow_registration", "1"))) {
                startActivity(
                        new Intent(this, RegisterActivity.class)
                );
            } else {
                Toast.makeText(
                        this,
                        "Registrations are currently closed.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        View forgotPassword = findViewById(R.id.tvForgotPassword);
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v ->
                    startActivity(
                            new Intent(this, ForgotPasswordActivity.class)
                    )
            );
        }
    }


    private void startLinkedInAuth() {
        setLoading(false);
        Toast.makeText(
                this,
                "LinkedIn student sign-in is currently unavailable.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void finishFirebaseStudentSignIn(
            FirebaseUser firebaseUser,
            String provider,
            String name,
            String username,
            String studentId,
            String grade,
            String profileImage) {

        if (firebaseUser == null) {
            setLoading(false);
            Log.e(TAG, "Cannot finish sign-in: FirebaseUser is null.");
            Toast.makeText(
                    this,
                    "Authentication did not return a valid user.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        FirebaseSessionManager.StudentProfile profile;

        try {
            profile = FirebaseSessionManager.ensureStudentProfile(
                    this,
                    firebaseUser,
                    name,
                    username,
                    studentId,
                    grade,
                    profileImage
            );

        } catch (IllegalStateException e) {
            setLoading(false);
            Log.e(TAG, "Could not prepare student profile.", e);

            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (!FirebaseAuthPolicy.hasVerifiedEmail(
                firebaseUser.isEmailVerified())) {

            firebaseUser.sendEmailVerification()
                    .addOnCompleteListener(this, verificationTask -> {
                        setLoading(false);

                        if (!verificationTask.isSuccessful()) {
                            Exception exception =
                                    verificationTask.getException();

                            Log.e(TAG,
                                    "Could not send email verification.",
                                    exception);

                            Toast.makeText(
                                    this,
                                    FirebaseAuthSupport.messageFor(exception),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        Toast.makeText(
                                this,
                                "Verify your email using the link sent by Firebase.",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent intent = new Intent(
                                this,
                                EmailVerificationActivity.class
                        );

                        intent.putExtra("EMAIL", firebaseUser.getEmail());
                        intent.putExtra("USERNAME", profile.username);
                        intent.putExtra("PROVIDER", provider);

                        startActivity(intent);
                    });

            return;
        }

        FirebaseSessionManager.saveStudentSession(
                this,
                firebaseUser,
                profile,
                provider
        );

        if (cbRememberMe != null && cbRememberMe.isChecked()) {
            loginPrefs.edit()
                    .putString("username", profile.username)
                    .putBoolean("remember", true)
                    .apply();
        }

        setLoading(false);
        navigateToDashboard();
    }

    private void handleTraditionalLogin() {
        String user = etStudentUser.getText().toString().trim();
        String pass = etStudentPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please enter your identifier and password",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);

        if (firebaseAuth == null) {
            Log.e(TAG, "FirebaseAuth unavailable for password sign-in.");

            Toast.makeText(
                    this,
                    "Firebase is not configured for this app. Contact Learnify support.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String email = findStudentEmail(user);

        if (email == null && user.contains("@")) {
            email = user;
        }

        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Sign in with the email address linked to your account.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        setLoading(true);
        String signInEmail = email;

        firebaseAuth.signInWithEmailAndPassword(signInEmail, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()
                            && task.getResult() != null
                            && task.getResult().getUser() != null) {

                        finishFirebaseStudentSignIn(
                                task.getResult().getUser(),
                                "Password",
                                "Student",
                                null,
                                null,
                                null,
                                ""
                        );

                    } else if (repository.login(user, pass, "STUDENT")) {
                        migrateLocalStudentAccount(
                                user,
                                pass,
                                signInEmail
                        );

                    } else {
                        setLoading(false);

                        Exception exception = task.getException();
                        Log.e(TAG, "Firebase password sign-in failed.",
                                exception);

                        Toast.makeText(
                                this,
                                FirebaseAuthSupport.messageFor(exception),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private String findStudentEmail(String identifier) {
        try (Cursor cursor = repository.getUserDetails(identifier)) {
            if (cursor != null
                    && cursor.moveToFirst()
                    && "STUDENT".equalsIgnoreCase(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow("user_type")
                            ))) {

                return cursor.getString(
                        cursor.getColumnIndexOrThrow("email")
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "Could not look up student email.", e);
        }

        return null;
    }

    private void migrateLocalStudentAccount(
            String identifier,
            String password,
            String email) {

        try (Cursor cursor = repository.getUserDetails(identifier)) {
            if (cursor == null
                    || !cursor.moveToFirst()
                    || !"STUDENT".equalsIgnoreCase(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow("user_type")
                            ))
                    || "Blocked".equalsIgnoreCase(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow("status")
                            ))) {

                setLoading(false);

                Toast.makeText(
                        this,
                        "This account cannot be migrated. Contact Learnify support.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("full_name")
            );
            String username = cursor.getString(
                    cursor.getColumnIndexOrThrow("username")
            );
            String grade = cursor.getString(
                    cursor.getColumnIndexOrThrow("grade_or_role")
            );
            String image = cursor.getString(
                    cursor.getColumnIndexOrThrow("profile_image")
            );

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, createTask -> {
                        if (createTask.isSuccessful()
                                && createTask.getResult() != null
                                && createTask.getResult().getUser() != null) {

                            finishFirebaseStudentSignIn(
                                    createTask.getResult().getUser(),
                                    "Password",
                                    name,
                                    username,
                                    null,
                                    grade,
                                    image
                            );

                        } else {
                            setLoading(false);

                            Exception exception = createTask.getException();
                            Log.e(TAG,
                                    "Legacy account migration failed.",
                                    exception);

                            Toast.makeText(
                                    this,
                                    FirebaseAuthSupport.messageFor(exception),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });

        } catch (Exception e) {
            setLoading(false);
            Log.e(TAG, "Could not migrate the local student account.", e);

            Toast.makeText(
                    this,
                    "Unable to migrate this account. Please contact Learnify support.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        if (btnStudentLogin != null) {
            btnStudentLogin.setEnabled(!loading);
            btnStudentLogin.setText(loading ? "" : "Sign In");
        }

        if (loginProgress != null) {
            loginProgress.setVisibility(
                    loading ? View.VISIBLE : View.GONE
            );
        }

    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadRememberedData() {
        if (loginPrefs != null
                && loginPrefs.getBoolean("remember", false)) {

            etStudentUser.setText(
                    loginPrefs.getString("username", "")
            );

            cbRememberMe.setChecked(true);
        }
    }
}
