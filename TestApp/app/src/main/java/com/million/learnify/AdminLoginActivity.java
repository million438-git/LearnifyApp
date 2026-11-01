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
            if (repository != null) {
                repository.ensureAdminAccount();
            }

            firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);
            android.content.SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
            FirebaseUser currentUser = firebaseAuth != null ? firebaseAuth.getCurrentUser() : null;
            String provider = session.getString("login_provider", "");
            boolean isLocal = "Local".equalsIgnoreCase(provider);

            if (session.getBoolean("isLoggedIn", false)
                    && "ADMIN".equals(session.getString("user_type", ""))
                    && (isLocal || (currentUser != null && currentUser.isEmailVerified()))) {
                startActivity(new Intent(this, AdminDashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                return;
            }

            setContentView(R.layout.activity_admin_login);
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

        // 1. DIRECT LOCAL AUTHENTICATION VIA SQLITE (PBKDF2 Cryptographic Verification)
        if (repository != null && repository.login(user, pass, "ADMIN")) {
            setLoading(false);
            promptOrCompleteAdminLogin(user);
            return;
        }

        // Check if this account is actually registered as another role (e.g. Student)
        if (repository != null) {
            try (Cursor cursor = repository.getUserDetails(user)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String existingRole = cursor.getString(cursor.getColumnIndexOrThrow("user_type"));
                    if (!"ADMIN".equalsIgnoreCase(existingRole)) {
                        setLoading(false);
                        Toast.makeText(this, "This account is registered as a " + existingRole + ". Please use the Student login portal.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }

        // For default admin accounts, NEVER attempt Firebase (avoids OPERATION_NOT_ALLOWED error)
        boolean isDefaultAdmin = "admin".equalsIgnoreCase(user) || "admin@learnify.com".equalsIgnoreCase(user);
        if (isDefaultAdmin || user.toLowerCase().endsWith("@learnify.com")) {
            setLoading(false);
            Toast.makeText(this, "Incorrect administrator credentials. Please check your username and password.", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. OPTIONAL FIREBASE CLOUD AUTHENTICATION (for custom cloud administrators)
        firebaseAuth = FirebaseAuthSupport.getConfiguredAuth(this);
        if (user.contains("@") && firebaseAuth != null) {
            setLoading(true);
            firebaseAuth.signInWithEmailAndPassword(user, pass).addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    verifyAdminClaim(task.getResult().getUser(), "Password");
                } else if (repository != null && repository.login(user, pass, "ADMIN")) {
                    setLoading(false);
                    onLocalAdminLoginSuccess(user);
                } else {
                    setLoading(false);
                    Exception ex = task.getException();
                    String code = (ex instanceof com.google.firebase.auth.FirebaseAuthException)
                            ? ((com.google.firebase.auth.FirebaseAuthException) ex).getErrorCode()
                            : null;
                    if ("ERROR_OPERATION_NOT_ALLOWED".equalsIgnoreCase(code)
                            || "operation-not-allowed".equalsIgnoreCase(code)) {
                        Toast.makeText(this, "Invalid administrator credentials. Please check your username and password.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, FirebaseAuthSupport.messageFor(ex), Toast.LENGTH_LONG).show();
                    }
                }
            });
            return;
        }

        // 4. FALLBACK: Invalid credentials
        setLoading(false);
        Toast.makeText(this, "Invalid administrator credentials. Please check your username and password.", Toast.LENGTH_LONG).show();
    }

    private void promptOrCompleteAdminLogin(String identifier) {
        String adminUsername = identifier;
        try (Cursor cursor = repository.getUserDetails(identifier)) {
            if (cursor != null && cursor.moveToFirst()) {
                adminUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            }
        } catch (Exception ignored) {}

        boolean is2fa = false;
        String secret = null;
        String recoveryCodes = null;
        if (repository != null && repository.getLocalDb() != null) {
            DatabaseHelper dbHelper = repository.getLocalDb();
            if (dbHelper.isTotpEnabled(adminUsername)) {
                is2fa = true;
                secret = dbHelper.getTotpSecret(adminUsername);
                recoveryCodes = dbHelper.getRecoveryCodes(adminUsername);
            }
        }
        if (!is2fa && securePrefs != null && securePrefs.isTwoFactorEnabled()) {
            is2fa = true;
            secret = securePrefs.getTwoFactorSecret();
        }

        if (is2fa && secret != null && !secret.isEmpty()) {
            showAdminTotpDialog(adminUsername, secret, recoveryCodes);
        } else {
            onLocalAdminLoginSuccess(adminUsername);
        }
    }

    private void showAdminTotpDialog(String adminUsername, String secret, String recoveryCodes) {
        android.widget.EditText etCode = new android.widget.EditText(this);
        etCode.setHint("000000");
        etCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCode.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        etCode.setTextSize(22f);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Two-Factor Authentication")
                .setMessage("Enter the 6-digit verification code from your authenticator app (or recovery backup code) to access the Administrator Portal.")
                .setView(etCode)
                .setPositiveButton("Verify & Sign In", (dialog, which) -> {
                    String code = etCode.getText().toString().trim();
                    boolean valid = TotpManager.verifyCode(secret, code);
                    if (!valid && recoveryCodes != null && recoveryCodes.contains(code) && code.length() >= 8) {
                        valid = true;
                    }
                    if (valid) {
                        onLocalAdminLoginSuccess(adminUsername);
                    } else {
                        Toast.makeText(this, "Invalid 2FA verification code. Access denied.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void onLocalAdminLoginSuccess(String identifier) {
        String adminUsername = identifier;
        String adminFullName = "Administrator";
        String adminEmail = identifier.contains("@") ? identifier : "admin@learnify.com";

        try (Cursor cursor = repository.getUserDetails(identifier)) {
            if (cursor != null && cursor.moveToFirst()) {
                adminUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                adminFullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                adminEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching admin details for local login", e);
        }

        securePrefs.saveString("username", adminUsername);
        securePrefs.saveString("full_name", adminFullName);
        securePrefs.saveString("user_type", "ADMIN");
        securePrefs.saveString("firebase_uid", "local_admin_" + adminUsername);
        securePrefs.saveString("login_provider", "Local");
        securePrefs.saveBoolean("isLoggedIn", true);

        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                .putString("username", adminUsername)
                .putString("full_name", adminFullName)
                .putString("user_type", "ADMIN")
                .putString("firebase_uid", "local_admin_" + adminUsername)
                .putString("login_provider", "Local")
                .putBoolean("isLoggedIn", true)
                .apply();

        // Record active device session & security event audit
        String sessionId = "sess_admin_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        String deviceInfo = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + " (API " + android.os.Build.VERSION.SDK_INT + ")";
        if (repository != null && repository.getLocalDb() != null) {
            repository.getLocalDb().registerSession(sessionId, adminUsername, deviceInfo, "Local Admin Console", true);
            repository.getLocalDb().recordSecurityEvent(adminUsername, "ADMIN_LOGIN_SUCCESS", "Authenticated via Admin Portal", deviceInfo);
        }

        repository.logSystemAction(adminUsername, "LOGIN_SUCCESS_ADMIN", "Admin logged in successfully");

        Toast.makeText(this, "Admin access granted.", Toast.LENGTH_SHORT).show();

        setLoading(false);
        startActivity(new Intent(this, AdminDashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
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
