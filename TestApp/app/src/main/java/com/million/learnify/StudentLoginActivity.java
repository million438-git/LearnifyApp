package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

/**
 * StudentLoginActivity - High-Security Authentication Hub.
 * Manages secure login and enforces email verification protocols.
 */
public class StudentLoginActivity extends BaseActivity {

    private static final String TAG = "StudentLogin";
    private EditText etStudentUser, etStudentPassword;
    private MaterialButton btnStudentLogin;
    private CircularProgressIndicator loginProgress;
    private TextView tvRegisterRedirect;
    private View btnGoogleLogin, btnLinkedinLogin;
    private CheckBox cbRememberMe;
    private SharedPreferences loginPrefs;
    
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 1. Auto-Redirect if session is already active
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);
        if (session.getBoolean("isLoggedIn", false)
                && "STUDENT".equals(session.getString("user_type", ""))
                && firebaseAuth != null
                && firebaseAuth.getCurrentUser() != null
                && firebaseAuth.getCurrentUser().isEmailVerified()) {
            navigateToDashboard();
            return;
        }

        // 2. Maintenance Check
        if (repository.getSystemSetting("maintenance_mode", "0").equals("1")) {
            setContentView(R.layout.layout_maintenance);
            return;
        }

        try {
            setContentView(R.layout.activity_student_login);
            loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            
            configureGoogleAuth();
            initViews();
            loadRememberedData();
        } catch (Exception e) {
            Log.e(TAG, "Auth Initialization Error", e);
        }
    }

    private void configureGoogleAuth() {
        int clientIdResource = getResources().getIdentifier(
                "default_web_client_id",
                "string",
                getPackageName());
        if (clientIdResource == 0) {
            mGoogleSignInClient = null;
            return;
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(clientIdResource))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initViews() {
        etStudentUser = findViewById(R.id.etStudentUser);
        etStudentPassword = findViewById(R.id.etStudentPassword);
        btnStudentLogin = findViewById(R.id.btnStudentLogin);
        loginProgress = findViewById(R.id.loginProgress);
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnLinkedinLogin = findViewById(R.id.btnLinkedinLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        btnStudentLogin.setOnClickListener(v -> handleTraditionalLogin());
        btnGoogleLogin.setOnClickListener(v -> startGoogleAuth());
        if (btnLinkedinLogin != null) {
            btnLinkedinLogin.setOnClickListener(v -> startLinkedInAuth());
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        tvRegisterRedirect.setOnClickListener(v -> {
            if (repository.getSystemSetting("allow_registration", "1").equals("1")) {
                startActivity(new Intent(this, RegisterActivity.class));
            } else {
                Toast.makeText(this, "Registrations are currently closed.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> 
            startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void startGoogleAuth() {
        if (mGoogleSignInClient == null || firebaseAuth == null) {
            Toast.makeText(this, "Firebase Google sign-in is not configured for this build.", Toast.LENGTH_LONG).show();
            return;
        }
        setLoading(true);
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_GOOGLE_SIGN_IN);
    }

    private void handleGoogleResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null && firebaseAuth != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, authTask -> {
                    if (authTask.isSuccessful() && authTask.getResult() != null) {
                        finishFirebaseStudentSignIn(
                                authTask.getResult().getUser(),
                                "Google",
                                account.getDisplayName(),
                                null,
                                null,
                                null,
                                "");
                    } else {
                        setLoading(false);
                        Toast.makeText(this, FirebaseAuthSupport.messageFor(authTask.getException()), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                setLoading(false);
                Toast.makeText(this, "Google sign-in did not return a Firebase credential.", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            setLoading(false);
            Toast.makeText(this, "Google sign-in was cancelled or could not be completed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLinkedInAuth() {
        setLoading(false);
        Toast.makeText(this, "LinkedIn student sign-in is currently unavailable.", Toast.LENGTH_SHORT).show();
    }

    private void finishFirebaseStudentSignIn(
            FirebaseUser firebaseUser,
            String provider,
            String name,
            String username,
            String studentId,
            String grade,
            String profileImage) {
        FirebaseSessionManager.StudentProfile profile;
        try {
            profile = FirebaseSessionManager.ensureStudentProfile(
                    this, firebaseUser, name, username, studentId, grade, profileImage);
        } catch (IllegalStateException e) {
            setLoading(false);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (!FirebaseAuthPolicy.hasVerifiedEmail(
                firebaseUser != null && firebaseUser.isEmailVerified())) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(this, verificationTask -> {
                setLoading(false);
                if (!verificationTask.isSuccessful()) {
                    Toast.makeText(this, FirebaseAuthSupport.messageFor(verificationTask.getException()), Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(this, "Verify your email using the link sent by Firebase.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, EmailVerificationActivity.class);
                intent.putExtra("EMAIL", firebaseUser.getEmail());
                intent.putExtra("USERNAME", profile.username);
                intent.putExtra("PROVIDER", provider);
                startActivity(intent);
            });
            return;
        }

        FirebaseSessionManager.saveStudentSession(this, firebaseUser, profile, provider);
        if (cbRememberMe.isChecked()) {
            loginPrefs.edit().putString("username", profile.username).putBoolean("remember", true).apply();
        }
        setLoading(false);
        navigateToDashboard();
    }

    private void handleTraditionalLogin() {
        String user = etStudentUser.getText().toString().trim();
        String pass = etStudentPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter your identifier and password", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);
        if (firebaseAuth == null) {
            Toast.makeText(this, "Firebase is not configured for this app. Contact Learnify support.", Toast.LENGTH_LONG).show();
            return;
        }

        String email = findStudentEmail(user);
        if (email == null && user.contains("@")) {
            email = user;
        }
        if (email == null) {
            Toast.makeText(this, "Sign in with the email address linked to your account.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        String signInEmail = email;
        firebaseAuth.signInWithEmailAndPassword(signInEmail, pass).addOnCompleteListener(this, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                finishFirebaseStudentSignIn(
                        task.getResult().getUser(), "Password", "Student", null, null, null, "");
            } else if (repository.login(user, pass, "STUDENT")) {
                migrateLocalStudentAccount(user, pass, signInEmail);
            } else {
                setLoading(false);
                Toast.makeText(this, FirebaseAuthSupport.messageFor(task.getException()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String findStudentEmail(String identifier) {
        try (Cursor cursor = repository.getUserDetails(identifier)) {
            if (cursor != null && cursor.moveToFirst()
                    && "STUDENT".equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("user_type")))) {
                return cursor.getString(cursor.getColumnIndexOrThrow("email"));
            }
        }
        return null;
    }

    private void migrateLocalStudentAccount(String identifier, String password, String email) {
        try (Cursor cursor = repository.getUserDetails(identifier)) {
            if (cursor == null || !cursor.moveToFirst()
                    || !"STUDENT".equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("user_type")))
                    || "Blocked".equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("status")))) {
                setLoading(false);
                Toast.makeText(this, "This account cannot be migrated. Contact Learnify support.", Toast.LENGTH_LONG).show();
                return;
            }

            String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String grade = cursor.getString(cursor.getColumnIndexOrThrow("grade_or_role"));
            String image = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, createTask -> {
                        if (createTask.isSuccessful() && createTask.getResult() != null) {
                            finishFirebaseStudentSignIn(
                                    createTask.getResult().getUser(),
                                    "Password",
                                    name,
                                    username,
                                    null,
                                    grade,
                                    image);
                        } else {
                            setLoading(false);
                            Toast.makeText(this, FirebaseAuthSupport.messageFor(createTask.getException()), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void navigateToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void setLoading(boolean loading) {
        btnStudentLogin.setEnabled(!loading);
        btnStudentLogin.setText(loading ? "" : "Sign In");
        loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            handleGoogleResult(GoogleSignIn.getSignedInAccountFromIntent(data));
        }
    }

    private void loadRememberedData() {
        if (loginPrefs.getBoolean("remember", false)) {
            etStudentUser.setText(loginPrefs.getString("username", ""));
            cbRememberMe.setChecked(true);
        }
    }
}
