package com.million.learnify;

import android.content.Context;
import android.database.Cursor;
import org.json.JSONObject;
import java.util.List;

/**
 * DataRepository - The Single Source of Truth for the application.
 * This class implements the Repository Pattern, abstracting the data source 
 * (Local SQLite vs Cloud API). 
 * 
 * SECURITY HARDENING: By using this repository, we can implement centralized 
 * validation and sync logic without touching UI code.
 */
public class DataRepository {
    private static DataRepository instance;
    private final DatabaseHelper dbHelper;
    private final Context context;
    
    private DataRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new DatabaseHelper(this.context);
    }

    public static synchronized DataRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DataRepository(context);
        }
        return instance;
    }

    // --- 1. USER AUTH & PROFILE (Common) ---

    public boolean login(String user, String pass, String type) {
        return dbHelper.checkUser(user, pass, type);
    }

    public boolean isEmailRegistered(String email) {
        return dbHelper.isEmailRegistered(email);
    }

    public boolean isEmailTaken(String email) {
        return dbHelper.isEmailRegistered(email);
    }

    boolean register(String name, String uid, String role, String user, String email, String pass, String img, String type) {
        return dbHelper.registerUser(name, uid, role, user, email, pass, img, type, null);
    }

    boolean register(String name, String uid, String role, String user, String email, String pass, String img, String type, String phone) {
        return dbHelper.registerUser(name, uid, role, user, email, pass, img, type, phone);
    }

    public boolean isUsernameTaken(String username) {
        return dbHelper.isUsernameTaken(username);
    }

    public Cursor getUserDetails(String identifier) {
        return dbHelper.getUserDetails(identifier);
    }

    public String getUserProfileImage(String identifier) {
        return dbHelper.getUserProfileImage(identifier);
    }
    
    public boolean isUserVerified(String identifier) {
        return dbHelper.isUserVerified(identifier);
    }
    
    public void setVerified(String identifier, boolean verified) {
        dbHelper.setVerified(identifier, verified);
    }

    public boolean updateProfile(String username, String name, String email, String phone, String uni, String year, String major, int xp, String subjects, String vis, int showLead, int notify, String img) {
        return dbHelper.updateUserProfileFull(username, name, email, phone, uni, year, major, xp, subjects, vis, showLead, notify, img);
    }

    public boolean updateUserProfileImage(String identifier, String imageUri) {
        return dbHelper.updateUserProfileImage(identifier, imageUri);
    }

    public boolean deleteUserAccount(String identifier) {
        return dbHelper.deleteUserAccount(identifier);
    }

    public JSONObject exportUserData(String identifier) {
        return dbHelper.exportUserData(identifier);
    }

    public boolean updatePassword(String identifier, String newPassword) {
        return dbHelper.updatePassword(identifier, newPassword);
    }

    public boolean verifyUserPassword(String identifier, String candidatePassword) {
        return dbHelper.verifyUserPassword(identifier, candidatePassword);
    }

    // --- 2. STUDENT PORTAL DATA ---

    public List<TaskModel> getStudentTasks(String grade) {
        return dbHelper.getTasksForGrade(grade);
    }

    public void updateTaskStatus(int taskId, String status) {
        dbHelper.updateTaskStatus(taskId, status);
    }

    public List<AnnouncementModel> getAnnouncements() {
        return dbHelper.getAnnouncements();
    }

    public List<ScheduleModel> getSchedule(String grade, String day) {
        return dbHelper.getScheduleForGrade(grade, day);
    }

    public Cursor getNotifications(String username) {
        return dbHelper.getUserNotifications(username);
    }

    public int getUnreadNotificationCount(String username) {
        return dbHelper.getUnreadNotificationCount(username);
    }

    public void markNotificationsRead(String username) {
        dbHelper.markNotificationsAsRead(username);
    }

    public void addNotification(String username, String title, String message, String type) {
        dbHelper.addNotification(username, title, message, type);
    }

    public Cursor getLeaderboard() {
        return dbHelper.getLeaderboard();
    }

    public Cursor getStudentEnrollments(String username) {
        return dbHelper.getStudentEnrollments(username);
    }

    public Cursor getStudentExamResults(String username) {
        return dbHelper.getStudentExamResults(username);
    }

    public Cursor getStudentPayments(String username) {
        return dbHelper.getStudentPayments(username);
    }

    // --- 3. ADMIN CONTROL PORTAL (Client-Server Ready) ---

    public void adminBlockUser(String username) {
        dbHelper.updateUserStatus(username, "Blocked");
        logSystemAction("Admin", "BLOCK_USER", "Blocked student: " + username);
    }

    public void adminUnblockUser(String username) {
        dbHelper.updateUserStatus(username, "Active");
        logSystemAction("Admin", "UNBLOCK_USER", "Unblocked student: " + username);
    }

    public void adminPostAnnouncement(String title, String msg, String type, String target) {
        dbHelper.insertAnnouncement(title, msg, type, target);
    }

    public Cursor getAllUsersByType(String type) {
        return dbHelper.getReadableDatabase().rawQuery(
            "SELECT full_name, unique_id, grade_or_role, email, profile_image, status, username, created_at FROM users WHERE user_type=?", 
            new String[]{type}
        );
    }

    public Cursor getAllEnrollments() {
        return dbHelper.getReadableDatabase().rawQuery("SELECT username, course_name FROM enrollments", null);
    }

    public Cursor getDistinctEnrolledCourses() {
        return dbHelper.getReadableDatabase().rawQuery("SELECT DISTINCT course_name FROM enrollments", null);
    }

    public void enrollStudent(String username, String course) {
        dbHelper.enrollStudent(username, course);
    }

    public void unenrollStudent(String username, String course) {
        dbHelper.unenrollStudent(username, course);
    }

    public void deleteUserPermanently(String username) {
        dbHelper.getWritableDatabase().delete("users", "username=?", new String[]{username});
        logSystemAction("Admin", "DELETE_USER", "Deleted user: " + username);
    }

    public void adminUpdatePayment(int paymentId, String status) {
        dbHelper.updatePaymentStatus(paymentId, status);
    }

    public Cursor getAllPayments() {
        return dbHelper.getAllPayments();
    }

    public boolean insertMaterial(String title, String content, String type, String subject, String grade, String filePath, String difficulty) {
        return dbHelper.insertMaterial(title, content, type, subject, grade, filePath, difficulty);
    }

    public Cursor getAllMaterials() {
        return dbHelper.getAllMaterials();
    }

    // --- 4. ANALYTICS & SYSTEM ---

    public void logSystemAction(String user, String action, String details) {
        dbHelper.logAdminAction(user, action, details);
    }

    public String getSystemSetting(String key, String defaultValue) {
        return dbHelper.getSetting(key, defaultValue);
    }

    public void saveSystemSetting(String key, String value) {
        dbHelper.saveSetting(key, value);
    }

    // --- HELPER ---
    public DatabaseHelper getLocalDb() {
        return dbHelper;
    }
}
