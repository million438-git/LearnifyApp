package com.million.learnify;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminLoginActivity - Professional Portal with Secure Persistence.
 * Refactored to use DataRepository for centralized security.
 */
public class AdminLoginActivity extends BaseActivity {

    private static final String TAG = "AdminLoginActivity";
    private TextInputEditText etAdminUser, etAdminPassword;
    private MaterialButton btnAdminLogin;
    private CircularProgressIndicator adminLoginProgress;
    private TextView tvAdminRegisterLink;
    private ImageView ivAdminLoginProfile;
    private ImageView btnAdminLinkedinLogin;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_admin_login);
            firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);
            initViews();
            loadAdminProfileImage();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "UI Load Error", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private void initViews() {
        etAdminUser = findViewById(R.id.etAdminUser);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        adminLoginProgress = findViewById(R.id.adminLoginProgress);
        tvAdminRegisterLink = findViewById(R.id.tvAdminRegisterLink);
        ivAdminLoginProfile = findViewById(R.id.ivAdminLoginProfile);
        btnAdminLinkedinLogin = findViewById(R.id.btnAdminLinkedinLogin);

        if (btnAdminLogin != null) {
            btnAdminLogin.setOnClickListener(v -> handleLogin());
        }

        if (tvAdminRegisterLink != null) {
            tvAdminRegisterLink.setVisibility(View.GONE);
        }

        if (btnAdminLinkedinLogin != null) {
            btnAdminLinkedinLogin.setOnClickListener(v -> startLinkedInAuth());
        }
    }

    private void startLinkedInAuth() {
        setLoading(false);
        Toast.makeText(this, "LinkedIn administrator sign-in is unavailable because secure backend verification is not configured.", Toast.LENGTH_LONG).show();
    }


    private void loadAdminProfileImage() {
        if (ivAdminLoginProfile != null) {
            String imageUriStr = repository.getUserProfileImage("admin");
            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                try {
                    ivAdminLoginProfile.setImageURI(Uri.parse(imageUriStr));
                    ivAdminLoginProfile.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    ivAdminLoginProfile.setImageResource(R.drawable.ic_app_logo);
                }
            }
        }
    }

    private void handleLogin() {
        String user = etAdminUser.getText().toString().trim();
        String pass = etAdminPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter admin credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);
        if (firebaseAuth == null) {
            Toast.makeText(this, "Firebase is not configured for this app. Contact Learnify support.", Toast.LENGTH_LONG).show();
            return;
        }
        String email = findAdminEmail(user);
        if (email == null && user.contains("@")) {
            email = user;
        }
        if (email == null) {
            Toast.makeText(this, "Use the email address provisioned for your administrator account.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                verifyAdminClaim(task.getResult().getUser(), "Password");
            } else {
                setLoading(false);
                Toast.makeText(this, FirebaseAuthSupport.messageFor(task.getException()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String findAdminEmail(String identifier) {
        try (Cursor cursor = repository.getUserDetails(identifier)) {
            if (cursor != null && cursor.moveToFirst()
                    && "ADMIN".equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("user_type")))) {
                return cursor.getString(cursor.getColumnIndexOrThrow("email"));
            }
        }
        return null;
    }

    private void verifyAdminClaim(FirebaseUser user, String provider) {
        if (user == null || !FirebaseAuthPolicy.hasVerifiedEmail(user.isEmailVerified())) {
            FirebaseSessionManager.signOut(this, firebaseAuth);
            setLoading(false);
            Toast.makeText(this, "A verified Firebase email is required for administrator access.", Toast.LENGTH_LONG).show();
            return;
        }
        user.getIdToken(true).addOnCompleteListener(this, task -> {
            GetTokenResult tokenResult = task.isSuccessful() ? task.getResult() : null;
            boolean isProvisionedAdmin = tokenResult != null
                    && FirebaseAuthPolicy.hasAdminAuthorization(
                            user.isEmailVerified(),
                            tokenResult.getClaims().get("admin"));
            if (!isProvisionedAdmin) {
                FirebaseSessionManager.signOut(this, firebaseAuth);
                setLoading(false);
                Toast.makeText(this, "Access denied. Administrator access must be provisioned by Learnify security staff.", Toast.LENGTH_LONG).show();
                return;
            }
            if (!ensureLocalAdminProfile(user)) {
                FirebaseSessionManager.signOut(this, firebaseAuth);
                setLoading(false);
                Toast.makeText(this, "Unable to prepare the local administrator profile. Contact support.", Toast.LENGTH_LONG).show();
                return;
            }
            onLoginSuccess(user, provider);
        });
    }

    private boolean ensureLocalAdminProfile(FirebaseUser user) {
        String email = user.getEmail();
        if (email == null) return false;
        try (Cursor cursor = repository.getUserDetails(email)) {
            if (cursor != null && cursor.moveToFirst()) {
                if (!"ADMIN".equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("user_type")))
                        || "Blocked".equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("status")))) {
                    return false;
                }
                repository.setVerified(email, true);
                return true;
            }
        }
        String name = user.getDisplayName() != null ? user.getDisplayName() : "Administrator";
        boolean registered = repository.register(
                name,
                user.getUid(),
                "Administrator",
                email,
                email,
                PasswordUtils.generateRandomPlaceholder(),
                "",
                "ADMIN");
        if (registered) repository.setVerified(email, true);
        return registered;
    }

    private void setLoading(boolean loading) {
        if (btnAdminLogin != null) {
            btnAdminLogin.setText(loading ? "" : "SECURE LOGIN");
            btnAdminLogin.setEnabled(!loading);
        }
        if (adminLoginProgress != null) {
            adminLoginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void onLoginSuccess(FirebaseUser firebaseUser, String provider) {
        String user = firebaseUser.getEmail();
        securePrefs.saveString("username", user);
        securePrefs.saveString("full_name", "Administrator");
        securePrefs.saveString("user_type", "ADMIN");
        securePrefs.saveString("firebase_uid", firebaseUser.getUid());
        securePrefs.saveString("login_provider", provider);
        securePrefs.saveBoolean("isLoggedIn", true);

        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
            .putString("username", user)
            .putString("full_name", "Administrator")
            .putString("user_type", "ADMIN")
            .putString("firebase_uid", firebaseUser.getUid())
            .putString("login_provider", provider)
            .putBoolean("isLoggedIn", true)
            .apply();

        repository.logSystemAction(user, "LOGIN_SUCCESS_ADMIN", "Admin logged in successfully");

        Toast.makeText(this, "Admin access granted.", Toast.LENGTH_SHORT).show();

        setLoading(false);
        startActivity(new Intent(this, AdminDashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
