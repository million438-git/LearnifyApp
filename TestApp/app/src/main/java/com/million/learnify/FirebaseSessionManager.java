package com.million.learnify;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

final class FirebaseSessionManager {

    private FirebaseSessionManager() {
    }

    static StudentProfile ensureStudentProfile(
            Context context,
            FirebaseUser user,
            String preferredName,
            String preferredUsername,
            String studentId,
            String grade,
            String profileImage) {
        return ensureStudentProfile(context, user, preferredName, preferredUsername, studentId, grade, profileImage, null);
    }

    static StudentProfile ensureStudentProfile(
            Context context,
            FirebaseUser user,
            String preferredName,
            String preferredUsername,
            String studentId,
            String grade,
            String profileImage,
            String phone) {
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            throw new IllegalStateException("The authenticated account has no email address.");
        }

        String email = user.getEmail().trim();
        DataRepository repository = DataRepository.getInstance(context);
        String username = null;
        String name = null;
        String savedGrade = null;
        String status = null;
        String type = null;

        try (Cursor cursor = repository.getUserDetails(email)) {
            if (cursor != null && cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                savedGrade = cursor.getString(cursor.getColumnIndexOrThrow("grade_or_role"));
                status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                type = cursor.getString(cursor.getColumnIndexOrThrow("user_type"));
            }
        }

        if (username == null) {
            username = !TextUtils.isEmpty(preferredUsername) ? preferredUsername.trim() : email;
            if (repository.isUsernameTaken(username)) {
                username = user.getUid();
            }
            name = !TextUtils.isEmpty(preferredName) ? preferredName.trim() : email;
            savedGrade = !TextUtils.isEmpty(grade) ? grade.trim() : "10";
            String savedStudentId = !TextUtils.isEmpty(studentId)
                    ? studentId.trim()
                    : "ST-" + UUID.randomUUID().toString().substring(0, 8);
            String savedImage = profileImage != null ? profileImage : "";
            if (!repository.register(
                    name,
                    savedStudentId,
                    savedGrade,
                    username,
                    email,
                    PasswordUtils.generateRandomPlaceholder(),
                    savedImage,
                    "STUDENT",
                    phone)) {
                throw new IllegalStateException("Learnify could not save the local student profile.");
            }
            type = "STUDENT";
            status = "Active";
        }

        if (!"STUDENT".equalsIgnoreCase(type)) {
            throw new IllegalStateException("This Firebase account is not registered as a student.");
        }
        if ("Blocked".equalsIgnoreCase(status)) {
            throw new IllegalStateException("This account is suspended. Contact Learnify support.");
        }

        if (user.isEmailVerified()) {
            repository.setVerified(username, true);
        }
        return new StudentProfile(
                username,
                !TextUtils.isEmpty(name) ? name : email,
                !TextUtils.isEmpty(savedGrade) ? savedGrade : "10");
    }

    static void saveStudentSession(
            Context context,
            FirebaseUser user,
            StudentProfile profile,
            String provider) {
        if (user == null || !FirebaseAuthPolicy.hasVerifiedEmail(user.isEmailVerified())) {
            throw new IllegalStateException("A verified Firebase account is required.");
        }

        SecurePreferencesHelper securePreferences = new SecurePreferencesHelper(context);
        securePreferences.saveString("username", profile.username);
        securePreferences.saveString("full_name", profile.name);
        securePreferences.saveString("user_type", "STUDENT");
        securePreferences.saveString("grade", profile.grade);
        securePreferences.saveString("firebase_uid", user.getUid());
        securePreferences.saveString("login_provider", provider);
        securePreferences.saveBoolean("isLoggedIn", true);

        SharedPreferences session = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        session.edit()
                .putString("username", profile.username)
                .putString("full_name", profile.name)
                .putString("user_type", "STUDENT")
                .putString("grade", profile.grade)
                .putString("firebase_uid", user.getUid())
                .putString("login_provider", provider)
                .putBoolean("isLoggedIn", true)
                .apply();

        NotificationHelper.scheduleDailyMotivation(context);
        NotificationHelper.scheduleLoginReminder(context);
        StudentSettings settings = new SettingsService(context).loadSettings();
        if (settings != null && settings.getReminderTime() != null) {
            NotificationHelper.scheduleStudyReminder(context, settings.getReminderTime());
        }
    }

    static void signOut(Context context, FirebaseAuth auth) {
        if (auth != null) {
            auth.signOut();
        }
        Context appContext = context.getApplicationContext();
        appContext.getSharedPreferences("UserSession", Context.MODE_PRIVATE).edit().clear().apply();
        new SecurePreferencesHelper(appContext).clearSession();
    }

    static final class StudentProfile {
        final String username;
        final String name;
        final String grade;

        StudentProfile(String username, String name, String grade) {
            this.username = username;
            this.name = name;
            this.grade = grade;
        }
    }
}
