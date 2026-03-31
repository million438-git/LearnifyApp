package com.million.learnify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AceGradeProfessional.db";
    private static final int DATABASE_VERSION = 44; // Updated for Full Professional Certificate Schema

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        seedAllData(db);
    }

    private void createTables(SQLiteDatabase db) {
        // Core Tables
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT, " +
                "unique_id TEXT, " + 
                "grade_or_role TEXT, " + 
                "username TEXT, " +
                "email TEXT, " +
                "password TEXT, " +
                "profile_image TEXT, " +
                "user_type TEXT, " + 
                "status TEXT DEFAULT 'Active', " +
                "phone TEXT, " +
                "university TEXT, " +
                "year TEXT, " +
                "major TEXT, " +
                "xp_goal INTEGER DEFAULT 500, " +
                "subjects TEXT, " +
                "visibility TEXT DEFAULT 'Public', " +
                "show_leaderboard INTEGER DEFAULT 1, " +
                "email_notifications INTEGER DEFAULT 1, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS enrollments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "course_name TEXT, " +
                "enrolled_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "status TEXT DEFAULT 'Active')");

        db.execSQL("CREATE TABLE IF NOT EXISTS exam_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "subject TEXT, " +
                "score TEXT, " +
                "total_questions INTEGER, " +
                "correct_answers INTEGER, " +
                "date DATETIME DEFAULT CURRENT_TIMESTAMP)");

        // Table for fully editable professional certificates (Twin of the requested image)
        db.execSQL("CREATE TABLE IF NOT EXISTS custom_certificates (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "cert_title TEXT, " +
                "presentation_text TEXT, " +
                "student_name TEXT, " +
                "body_text TEXT, " +
                "course_name TEXT, " +
                "issue_date TEXT, " +
                "student_id TEXT, " +
                "certificate_id TEXT, " +
                "director_name TEXT, " +
                "director_role TEXT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS materials (" +
                "mat_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "content TEXT, " +
                "type TEXT, " +
                "subject TEXT, " +
                "grade TEXT, " +
                "file_path TEXT, " +
                "difficulty TEXT DEFAULT 'Medium', " +
                "usage_count INTEGER DEFAULT 0, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS lessons (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "course_id INTEGER, " +
                "section_name TEXT, " +
                "title TEXT, " +
                "lesson_type TEXT, " +
                "content_body TEXT, " +
                "file_attachment TEXT, " +
                "duration TEXT, " +
                "is_drip INTEGER DEFAULT 0, " +
                "order_index INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS tasks (task_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, subject TEXT, grade TEXT, due_date TEXT, due_time TEXT, priority TEXT, status TEXT DEFAULT 'Pending')");
        db.execSQL("CREATE TABLE IF NOT EXISTS schedule (id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, teacher TEXT, room TEXT, time TEXT, duration TEXT, day TEXT, grade TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS announcements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "message TEXT, " +
                "date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "type TEXT, " +
                "target_audience TEXT DEFAULT 'All')");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "transaction_id TEXT, " +
                "amount TEXT, " +
                "screenshot_uri TEXT, " + 
                "status TEXT DEFAULT 'Pending', " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS daily_questions (id INTEGER PRIMARY KEY AUTOINCREMENT, question TEXT, opt_a TEXT, opt_b TEXT, opt_c TEXT, opt_d TEXT, answer TEXT, grade TEXT, subject TEXT, difficulty TEXT DEFAULT 'Medium', explanation TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS challenge_stats (username TEXT PRIMARY KEY, total_score INTEGER DEFAULT 0, current_streak INTEGER DEFAULT 0, max_streak INTEGER DEFAULT 0, last_challenge_date TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS study_notes (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, subject TEXT, chapter TEXT, topic TEXT, personal_note TEXT, highlight_data TEXT, is_bookmarked INTEGER DEFAULT 0, is_important INTEGER DEFAULT 0, UNIQUE(username, subject, chapter, topic))");

        db.execSQL("CREATE TABLE IF NOT EXISTS support_tickets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "title TEXT, " +
                "category TEXT, " +
                "description TEXT, " +
                "status TEXT DEFAULT 'Open', " +
                "admin_reply TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS user_notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "title TEXT, " +
                "message TEXT, " +
                "type TEXT, " +
                "is_read INTEGER DEFAULT 0, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS admin_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "admin_username TEXT, " +
                "action TEXT, " +
                "details TEXT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS lab_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "mode TEXT, " +
                "problem TEXT, " +
                "solution TEXT, " +
                "is_assignment_help INTEGER DEFAULT 0, " +
                "course_context TEXT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS lab_progress (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "problem_type TEXT, " +
                "attempt_count INTEGER DEFAULT 1, " +
                "last_attempt DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(username, problem_type))");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS system_settings (id INTEGER PRIMARY KEY, account_details TEXT)");
    }

    private void seedAllData(SQLiteDatabase db) {
        ContentValues v = new ContentValues();
        v.put("full_name", "System Admin");
        v.put("username", "admin");
        v.put("password", "admin123");
        v.put("user_type", "ADMIN");
        v.put("grade_or_role", "Super Admin");
        db.insertWithOnConflict("users", null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void saveSetting(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        db.insertWithOnConflict("settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT value FROM settings WHERE key=?", new String[]{key});
        String value = defaultValue;
        if (c.moveToFirst()) value = c.getString(0);
        c.close();
        return value;
    }

    public void saveLabResult(String user, String mode, String prob, String sol, boolean isAssignment, String course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", user);
        cv.put("mode", mode);
        cv.put("problem", prob);
        cv.put("solution", sol);
        cv.put("is_assignment_help", isAssignment ? 1 : 0);
        cv.put("course_context", course);
        db.insert("lab_history", null, cv);
        updateLabProgress(user, mode);
    }

    private void updateLabProgress(String user, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO lab_progress (username, problem_type, attempt_count) " +
                "VALUES (?, ?, 1) " +
                "ON CONFLICT(username, problem_type) DO UPDATE SET " +
                "attempt_count = attempt_count + 1, last_attempt = CURRENT_TIMESTAMP", new Object[]{user, type});
    }

    public Cursor getLabHistory(String user) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM lab_history WHERE username=? ORDER BY timestamp DESC", new String[]{user});
    }

    public Cursor getLabStats(String user) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM lab_progress WHERE username=? ORDER BY attempt_count DESC", new String[]{user});
    }

    public Cursor getLabUsageAnalytics() {
        return this.getReadableDatabase().rawQuery("SELECT mode, COUNT(*) as count FROM lab_history GROUP BY mode", null);
    }

    public boolean isCameraLabEnabled() {
        return getSetting("camera_lab_enabled", "1").equals("1");
    }

    public boolean checkUser(String input, String password, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE (username=? OR email=?) AND password=? AND user_type=?", new String[]{input, input, password, type});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean registerUser(String name, String uid, String role, String user, String email, String pass, String img, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("full_name", name); v.put("unique_id", uid); v.put("grade_or_role", role); v.put("username", user); v.put("email", email); v.put("password", pass); v.put("profile_image", img); v.put("user_type", type);
        return db.insert("users", null, v) != -1;
    }

    public Cursor getUserDetails(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM users WHERE username=?", new String[]{username});
    }

    public String getUserGrade(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT grade_or_role FROM users WHERE username=?", new String[]{username});
        String grade = "10";
        if (c.moveToFirst()) grade = c.getString(0);
        c.close();
        return grade;
    }

    public String getUserProfileImage(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT profile_image FROM users WHERE username=?", new String[]{username});
        String img = "";
        if (c.moveToFirst()) img = c.getString(0);
        c.close();
        return img;
    }

    public boolean updateUserProfileFull(String username, String name, String email, String phone, String uni, String year, String major, int xp, String subjects, String visibility, int leaderboard, int notify, String img) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("full_name", name); cv.put("email", email); cv.put("phone", phone); cv.put("university", uni); cv.put("year", year); cv.put("major", major); cv.put("xp_goal", xp); cv.put("subjects", subjects); cv.put("visibility", visibility); cv.put("show_leaderboard", leaderboard); cv.put("email_notifications", notify); cv.put("profile_image", img);
        return db.update("users", cv, "username=?", new String[]{username}) > 0;
    }

    public void updateUserStatus(String username, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("users", cv, "username=?", new String[]{username});
    }

    public void updatePassword(String username, String pass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", pass);
        db.update("users", cv, "username=?", new String[]{username});
    }

    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE email=?", new String[]{email});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public boolean updatePasswordByEmail(String email, String pass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", pass);
        return db.update("users", cv, "email=?", new String[]{email}) > 0;
    }

    public Cursor getLeaderboard() {
        return this.getReadableDatabase().rawQuery("SELECT username, total_score FROM challenge_stats ORDER BY total_score DESC LIMIT 50", null);
    }

    public boolean insertMaterial(String title, String content, String type, String subject, String grade, String filePath, String difficulty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("type", type);
        cv.put("subject", subject);
        cv.put("grade", grade);
        cv.put("file_path", filePath);
        cv.put("difficulty", difficulty);
        return db.insert("materials", null, cv) != -1;
    }

    public Cursor getAllMaterials() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM materials ORDER BY timestamp DESC", null);
    }

    public boolean addAnnouncement(String title, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("message", message);
        cv.put("type", type);
        cv.put("target_audience", "All");
        return db.insert("announcements", null, cv) != -1;
    }

    public boolean insertAnnouncement(String title, String message, String type, String audience) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("message", message);
        cv.put("type", type);
        cv.put("target_audience", audience);
        return db.insert("announcements", null, cv) != -1;
    }

    public List<AnnouncementModel> getAnnouncements() {
        List<AnnouncementModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM announcements ORDER BY date DESC", null);
        if (c.moveToFirst()) {
            do {
                AnnouncementModel m = new AnnouncementModel();
                m.setId(c.getInt(0));
                m.setTitle(c.getString(1));
                m.setMessage(c.getString(2));
                m.setDate(c.getString(3));
                m.setType(c.getString(4));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean addNotification(String username, String title, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("title", title);
        cv.put("message", message);
        cv.put("type", type);
        return db.insert("user_notifications", null, cv) != -1;
    }

    public int getUnreadNotificationCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM user_notifications WHERE username=? AND is_read=0", new String[]{username});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public Cursor getUserNotifications(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM user_notifications WHERE username=? OR username='all' ORDER BY timestamp DESC", new String[]{username});
    }

    public void markNotificationsAsRead(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE user_notifications SET is_read=1 WHERE username=? OR username='all'", new String[]{username});
    }

    public void clearAllNotifications(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("user_notifications", "username=? OR username='all'", new String[]{username});
    }

    public void deleteNotification(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("user_notifications", "id=?", new String[]{String.valueOf(id)});
    }

    public boolean submitPayment(String username, String txId, String amount, String screenshot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("transaction_id", txId);
        cv.put("amount", amount);
        cv.put("screenshot_uri", screenshot);
        return db.insert("payments", null, cv) != -1;
    }

    public Cursor getAllPayments() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM payments ORDER BY timestamp DESC", null);
    }

    public void updatePaymentStatus(int id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("payments", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor getStudentPayments(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM payments WHERE username=?", new String[]{username});
    }

    public boolean hasPremiumAccess(String username, String grade) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM payments WHERE username=? AND status='Approved'", new String[]{username});
        boolean has = c.getCount() > 0;
        c.close();
        return has;
    }

    public String getAdminAccountDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT account_details FROM system_settings WHERE id=1", null);
        String details = "CBE: 1000123456789 (Learnify Pro)";
        if (c.moveToFirst()) details = c.getString(0);
        c.close();
        return details;
    }

    public Cursor getAllSupportTickets() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM support_tickets ORDER BY created_at DESC", null);
    }

    public void updateTicketReply(int id, String reply) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("admin_reply", reply);
        cv.put("status", "Answered");
        db.update("support_tickets", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void logAdminAction(String admin, String action, String details) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("admin_username", admin);
        cv.put("action", action);
        cv.put("details", details);
        db.insert("admin_logs", null, cv);
    }

    public Cursor getStudentEnrollments(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM enrollments WHERE username=?", new String[]{username});
    }

    public void enrollStudent(String username, String course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("course_name", course);
        db.insert("enrollments", null, cv);
    }

    public void unenrollStudent(String username, String course) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("enrollments", "username=? AND course_name=?", new String[]{username, course});
    }

    public Cursor getStudentExamResults(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM exam_results WHERE username=?", new String[]{username});
    }

    public void saveExamResult(String username, String subject, String score, int total, int correct) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("subject", subject);
        cv.put("score", score);
        cv.put("total_questions", total);
        cv.put("correct_answers", correct);
        db.insert("exam_results", null, cv);
    }

    public boolean saveFullCertificate(String user, String title, String presentation, String studName, String body, String course, String date, String studId, String certId, String director, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", user);
        cv.put("cert_title", title);
        cv.put("presentation_text", presentation);
        cv.put("student_name", studName);
        cv.put("body_text", body);
        cv.put("course_name", course);
        cv.put("issue_date", date);
        cv.put("student_id", studId);
        cv.put("certificate_id", certId);
        cv.put("director_name", director);
        cv.put("director_role", role);
        return db.insert("custom_certificates", null, cv) != -1;
    }

    public Cursor getCustomCertificate(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM custom_certificates WHERE username=? ORDER BY timestamp DESC LIMIT 1", new String[]{username});
    }

    public boolean insertDailyQuestion(String q, String a, String b, String c, String d, String ans, String grade, String sub, String diff, String expl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("question", q); cv.put("opt_a", a); cv.put("opt_b", b); cv.put("opt_c", c); cv.put("opt_d", d); cv.put("answer", ans); cv.put("grade", grade); cv.put("subject", sub); cv.put("difficulty", diff); cv.put("explanation", expl);
        return db.insert("daily_questions", null, cv) != -1;
    }

    public Cursor getDailyQuestions(String grade) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM daily_questions WHERE grade=? ORDER BY RANDOM() LIMIT 5", new String[]{grade});
    }

    public Cursor getChallengeStats(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM challenge_stats WHERE username=?", new String[]{username});
    }

    public void updateChallengeStats(String username, int score, int streak, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO challenge_stats (username, total_score, current_streak, last_challenge_date) VALUES (?, ?, ?, ?) ON CONFLICT(username) DO UPDATE SET total_score = ?, current_streak = ?, last_challenge_date = ?", new Object[]{username, score, streak, date, score, streak, date});
    }

    public void insertClassCustom(String sub, String teacher, String room, String time, String dur, String day, String grade) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("subject", sub); cv.put("teacher", teacher); cv.put("room", room); cv.put("time", time); cv.put("duration", dur); cv.put("day", day); cv.put("grade", grade);
        db.insert("schedule", null, cv);
    }

    public List<ScheduleModel> getScheduleForGrade(String grade, String day) {
        List<ScheduleModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM schedule WHERE grade=? AND day=?", new String[]{grade, day});
        if (c.moveToFirst()) {
            do {
                ScheduleModel m = new ScheduleModel();
                m.setId(c.getInt(0));
                m.setSubject(c.getString(1));
                m.setTeacher(c.getString(2));
                m.setRoom(c.getString(3));
                m.setTime(c.getString(4));
                m.setDuration(c.getString(5));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean insertDailyQuestionFull(String q, String a, String b, String c, String d, String ans, String grade, String sub, String diff, String expl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("question", q); cv.put("opt_a", a); cv.put("opt_b", b); cv.put("opt_c", c); cv.put("opt_d", d); cv.put("answer", ans); cv.put("grade", grade); cv.put("subject", sub); cv.put("difficulty", diff); cv.put("explanation", expl);
        return db.insert("daily_questions", null, cv) != -1;
    }

    public boolean insertTask(String title, String desc, String sub, String grade, String date, String time, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title); cv.put("description", desc); cv.put("subject", sub); cv.put("grade", grade); cv.put("due_date", date); cv.put("due_time", time); cv.put("priority", priority);
        return db.insert("tasks", null, cv) != -1;
    }

    public List<TaskModel> getTasksForGrade(String grade) {
        List<TaskModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tasks WHERE grade=?", new String[]{grade});
        if (c.moveToFirst()) {
            do {
                TaskModel m = new TaskModel();
                m.setId(c.getInt(0));
                m.setTitle(c.getString(1));
                m.setDescription(c.getString(2));
                m.setSubject(c.getString(3));
                m.setGrade(c.getString(4));
                m.setDueDate(c.getString(5));
                m.setDueTime(c.getString(6));
                m.setPriority(c.getString(7));
                m.setStatus(c.getString(8));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public void updateTaskStatus(int id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("tasks", cv, "task_id=?", new String[]{String.valueOf(id)});
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tasks", "task_id=?", new String[]{String.valueOf(id)});
    }

    public Cursor getStudyNote(String user, String sub, String chapter, String topic) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM study_notes WHERE username=? AND subject=? AND chapter=? AND topic=?", new String[]{user, sub, chapter, topic});
    }

    public void saveStudyNote(String user, String sub, String chapter, String topic, String note, String highlight, int bookmark, int important) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", user); cv.put("subject", sub); cv.put("chapter", chapter); cv.put("topic", topic); cv.put("personal_note", note); cv.put("highlight_data", highlight); cv.put("is_bookmarked", bookmark); cv.put("is_important", important);
        db.insertWithOnConflict("study_notes", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 44) {
            db.execSQL("DROP TABLE IF EXISTS custom_certificates");
            createTables(db);
        }
    }
}
