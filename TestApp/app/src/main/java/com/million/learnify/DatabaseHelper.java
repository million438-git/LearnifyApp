package com.million.learnify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AceGradeProfessional.db";
    private static final int DATABASE_VERSION = 57; 
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        seedAllData(db);
        seedDailyQuestionsFromCSV(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT, " +
                "unique_id TEXT, " + 
                "grade_or_role TEXT, " + 
                "username TEXT UNIQUE, " +
                "email TEXT, " +
                "password TEXT, " +
                "profile_image TEXT, " +
                "user_type TEXT, " + 
                "status TEXT DEFAULT 'Active', " +
                "is_verified INTEGER DEFAULT 0, " +
                "verification_token TEXT, " +
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

        db.execSQL("CREATE TABLE IF NOT EXISTS daily_questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT, " +
                "option_a TEXT, " +
                "option_b TEXT, " +
                "option_c TEXT, " +
                "option_d TEXT, " +
                "correct_option TEXT, " +
                "grade TEXT, " +
                "category TEXT, " +
                "difficulty TEXT, " +
                "explanation TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS challenge_stats (" +
                "username TEXT PRIMARY KEY, " +
                "total_xp INTEGER DEFAULT 0, " +
                "streak INTEGER DEFAULT 0, " +
                "last_completion_date TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS support_tickets (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, title TEXT, category TEXT, description TEXT, status TEXT DEFAULT 'Open', reply TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS user_notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, title TEXT, message TEXT, type TEXT, is_read INTEGER DEFAULT 0, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS lab_results (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, problem_type TEXT, problem_text TEXT, solution_text TEXT, is_assignment INTEGER, course_context TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS materials (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, type TEXT, subject TEXT, grade TEXT, file_path TEXT, difficulty TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS announcements (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, message TEXT, type TEXT, target_grade TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS admin_logs (id INTEGER PRIMARY KEY AUTOINCREMENT, admin_username TEXT, action TEXT, details TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS enrollments (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, course_name TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS payments (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, transaction_id TEXT, amount TEXT, image_path TEXT, status TEXT DEFAULT 'Pending', timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS exam_results (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, subject TEXT, score_text TEXT, total_questions INTEGER, correct_answers INTEGER, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS certificates (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, title TEXT, presentation TEXT, student_name TEXT, body TEXT, course TEXT, date TEXT, student_id TEXT, certificate_id TEXT, director TEXT, role TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS study_notes (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, subject TEXT, chapter TEXT, topic TEXT, content TEXT, highlights TEXT, is_bookmarked INTEGER, is_important INTEGER, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, subject TEXT, grade TEXT, date TEXT, time TEXT, priority TEXT, status TEXT DEFAULT 'Pending', timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS class_schedule (id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, instructor TEXT, location TEXT, time TEXT, duration TEXT, day TEXT, grade TEXT)");

        // NEW: Flashcards for Spaced Repetition
        db.execSQL("CREATE TABLE IF NOT EXISTS flashcards (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "question TEXT, " +
                "answer TEXT, " +
                "subject TEXT, " +
                "interval INTEGER DEFAULT 0, " +
                "repetitions INTEGER DEFAULT 0, " +
                "ease_factor REAL DEFAULT 2.5, " +
                "next_review_date TEXT)");

        // NEW: Mood Tracking
        db.execSQL("CREATE TABLE IF NOT EXISTS mood_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "mood TEXT, " +
                "note TEXT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    private void seedAllData(SQLiteDatabase db) {
        ContentValues v = new ContentValues();
        v.put("full_name", "System Admin");
        v.put("username", "admin");
        v.put("password", "admin123");
        v.put("user_type", "ADMIN");
        v.put("is_verified", 1);
        db.insertWithOnConflict("users", null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void seedDailyQuestionsFromCSV(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            InputStream is = context.getAssets().open("daily_questions.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("addQ(")) {
                    int start = line.indexOf("(") + 1;
                    int end = line.lastIndexOf(")");
                    if (start <= 0 || end <= start) continue;
                    String params = line.substring(start, end);
                    String[] parts = params.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (parts.length >= 11) {
                        ContentValues v = new ContentValues();
                        v.put("question", stripQuotes(parts[1]));
                        v.put("option_a", stripQuotes(parts[2]));
                        v.put("option_b", stripQuotes(parts[3]));
                        v.put("option_c", stripQuotes(parts[4]));
                        v.put("option_d", stripQuotes(parts[5]));
                        v.put("correct_option", stripQuotes(parts[6]));
                        v.put("grade", stripQuotes(parts[7]));
                        v.put("category", stripQuotes(parts[8]));
                        v.put("difficulty", stripQuotes(parts[9]));
                        v.put("explanation", stripQuotes(parts[10]));
                        db.insert("daily_questions", null, v);
                    }
                }
            }
            db.setTransactionSuccessful();
            Log.d("DB_SEED", "Seeded questions from CSV successfully.");
        } catch (Exception e) {
            Log.e("DB_SEED", "Critical error seeding questions: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    private String stripQuotes(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"");
    }

    // --- Core User Methods ---

    public boolean checkUser(String user, String pass, String type) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE (username=? OR email=?) AND password=? AND user_type=?", new String[]{user, user, pass, type});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public boolean registerUser(String name, String uid, String role, String user, String email, String pass, String img, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("full_name", name); v.put("unique_id", uid); v.put("grade_or_role", role);
        v.put("username", user); v.put("email", email); v.put("password", pass);
        v.put("profile_image", img); v.put("user_type", type);
        return db.insert("users", null, v) != -1;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM users WHERE username=?", new String[]{username});
        boolean taken = c.getCount() > 0;
        c.close();
        return taken;
    }

    public Cursor getUserDetails(String identifier) {
        return getReadableDatabase().rawQuery("SELECT * FROM users WHERE username=? OR email=?", new String[]{identifier, identifier});
    }

    public String getUserProfileImage(String identifier) {
        Cursor c = getUserDetails(identifier);
        String img = null;
        if (c.moveToFirst()) img = c.getString(c.getColumnIndexOrThrow("profile_image"));
        c.close();
        return img;
    }

    public boolean isUserVerified(String identifier) {
        Cursor c = getUserDetails(identifier);
        boolean verified = false;
        if (c.moveToFirst()) verified = c.getInt(c.getColumnIndexOrThrow("is_verified")) == 1;
        c.close();
        return verified;
    }

    public void setVerified(String identifier, boolean verified) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_verified", verified ? 1 : 0);
        db.update("users", cv, "username=? OR email=?", new String[]{identifier, identifier});
    }

    public boolean updateUserProfileFull(String username, String name, String email, String phone, String uni, String year, String major, int xp, String subjects, String vis, int showLead, int notify, String img) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("full_name", name); cv.put("email", email); cv.put("phone", phone);
        cv.put("university", uni); cv.put("year", year); cv.put("major", major);
        cv.put("xp_goal", xp); cv.put("subjects", subjects); cv.put("visibility", vis);
        cv.put("show_leaderboard", showLead); cv.put("email_notifications", notify);
        cv.put("profile_image", img);
        return db.update("users", cv, "username=?", new String[]{username}) > 0;
    }

    public void updatePassword(String identifier, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        db.update("users", cv, "username=? OR email=?", new String[]{identifier, identifier});
    }

    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM users WHERE email=?", new String[]{email});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public boolean updatePasswordByEmail(String email, String newPass) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPass);
        return db.update("users", cv, "email=?", new String[]{email}) > 0;
    }

    public void updateVerificationToken(String username, String token) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("verification_token", token);
        db.update("users", cv, "username=? OR email=?", new String[]{username, username});
    }

    public boolean verifyToken(String identifier, String token) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM users WHERE (username=? OR email=?) AND verification_token=?", new String[]{identifier, identifier, token});
        boolean valid = c.getCount() > 0;
        c.close();
        return valid;
    }

    public void updateUserStatus(String username, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("users", cv, "username=?", new String[]{username});
    }

    // --- Task Methods ---

    public List<TaskModel> getTasksForGrade(String grade) {
        List<TaskModel> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM tasks WHERE grade=? ORDER BY id DESC", new String[]{grade});
        if (c.moveToFirst()) {
            do {
                TaskModel task = new TaskModel();
                task.setId(c.getInt(c.getColumnIndexOrThrow("id")));
                task.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
                task.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
                task.setSubject(c.getString(c.getColumnIndexOrThrow("subject")));
                task.setDueDate(c.getString(c.getColumnIndexOrThrow("date")));
                task.setDueTime(c.getString(c.getColumnIndexOrThrow("time")));
                task.setPriority(c.getString(c.getColumnIndexOrThrow("priority")));
                task.setStatus(c.getString(c.getColumnIndexOrThrow("status")));
                list.add(task);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public void updateTaskStatus(int id, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("tasks", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public boolean insertTask(String title, String desc, String subject, String grade, String date, String time, String priority) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", title); v.put("description", desc); v.put("subject", subject);
        v.put("grade", grade); v.put("date", date); v.put("time", time); v.put("priority", priority);
        return db.insert("tasks", null, v) != -1;
    }

    public void deleteTask(int id) {
        getWritableDatabase().delete("tasks", "id=?", new String[]{String.valueOf(id)});
    }

    // --- Announcement Methods ---

    public List<AnnouncementModel> getAnnouncements() {
        List<AnnouncementModel> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM announcements ORDER BY timestamp DESC", null);
        if (c.moveToFirst()) {
            do {
                AnnouncementModel ann = new AnnouncementModel();
                ann.setId(c.getInt(c.getColumnIndexOrThrow("id")));
                ann.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
                ann.setMessage(c.getString(c.getColumnIndexOrThrow("message")));
                ann.setType(c.getString(c.getColumnIndexOrThrow("type")));
                ann.setDate(c.getString(c.getColumnIndexOrThrow("timestamp")));
                list.add(ann);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public void insertAnnouncement(String title, String msg, String type, String target) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", title); v.put("message", msg); v.put("type", type); v.put("target_grade", target);
        db.insert("announcements", null, v);
    }

    public void addAnnouncement(String title, String msg, String type) {
        insertAnnouncement(title, msg, type, "All");
    }

    // --- Notification Methods ---

    public Cursor getUserNotifications(String username) {
        return getReadableDatabase().rawQuery("SELECT * FROM user_notifications WHERE username=? ORDER BY timestamp DESC", new String[]{username});
    }

    public int getUnreadNotificationCount(String username) {
        return getTableCount("user_notifications", "username=? AND is_read=0", new String[]{username});
    }

    public void markNotificationsAsRead(String username) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_read", 1);
        db.update("user_notifications", cv, "username=?", new String[]{username});
    }

    public void addNotification(String username, String title, String message, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", username); v.put("title", title); v.put("message", message); v.put("type", type);
        db.insert("user_notifications", null, v);
    }

    public void clearAllNotifications(String username) {
        getWritableDatabase().delete("user_notifications", "username=?", new String[]{username});
    }

    public void deleteNotification(int id) {
        getWritableDatabase().delete("user_notifications", "id=?", new String[]{String.valueOf(id)});
    }

    // --- Analytics & Settings ---

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("settings", new String[]{"value"}, "key=?", new String[]{key}, null, null, null);
        String val = defaultValue;
        if (c != null && c.moveToFirst()) {
            val = c.getString(0);
            c.close();
        }
        return val;
    }

    public void saveSetting(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("key", key); cv.put("value", value);
        db.insertWithOnConflict("settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean isCameraLabEnabled() {
        return getSetting("camera_lab_enabled", "1").equals("1");
    }

    public void logAdminAction(String admin, String action, String details) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("admin_username", admin); cv.put("action", action); cv.put("details", details);
        db.insert("admin_logs", null, cv);
    }

    public Cursor getLabUsageAnalytics() {
        return getReadableDatabase().rawQuery("SELECT problem_type, COUNT(*) FROM lab_results GROUP BY problem_type", null);
    }

    // --- Additional Methods for Build Support ---

    public Cursor getLeaderboard() {
        return getReadableDatabase().rawQuery("SELECT username, total_xp, streak FROM challenge_stats ORDER BY total_xp DESC LIMIT 50", null);
    }

    public Cursor getStudentEnrollments(String username) {
        return getReadableDatabase().rawQuery("SELECT * FROM enrollments WHERE username=?", new String[]{username});
    }

    public void enrollStudent(String username, String course) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", username); v.put("course_name", course);
        db.insert("enrollments", null, v);
    }

    public void unenrollStudent(String username, String course) {
        getWritableDatabase().delete("enrollments", "username=? AND course_name=?", new String[]{username, course});
    }

    public Cursor getStudentExamResults(String username) {
        return getReadableDatabase().rawQuery("SELECT * FROM exam_results WHERE username=?", new String[]{username});
    }

    public void saveExamResult(String username, String subject, String scoreText, int total, int correct) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", username); v.put("subject", subject); v.put("score_text", scoreText);
        v.put("total_questions", total); v.put("correct_answers", correct);
        db.insert("exam_results", null, v);
    }

    public Cursor getStudentPayments(String username) {
        return getReadableDatabase().rawQuery("SELECT * FROM payments WHERE username=?", new String[]{username});
    }

    public Cursor getAllPayments() {
        return getReadableDatabase().rawQuery("SELECT * FROM payments ORDER BY timestamp DESC", null);
    }

    public void updatePaymentStatus(int id, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("payments", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public boolean submitPayment(String username, String txId, String amount, String img) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", username); v.put("transaction_id", txId); v.put("amount", amount); v.put("image_path", img);
        return db.insert("payments", null, v) != -1;
    }

    public String getAdminAccountDetails() {
        return "Bank: AceBank\nAccount Name: Learnify Inc.\nAccount No: 1234567890\nRef: [Username]";
    }

    public boolean insertMaterial(String title, String content, String type, String subject, String grade, String filePath, String difficulty) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", title); v.put("content", content); v.put("type", type);
        v.put("subject", subject); v.put("grade", grade); v.put("file_path", filePath); v.put("difficulty", difficulty);
        return db.insert("materials", null, v) != -1;
    }

    public Cursor getAllMaterials() {
        return getReadableDatabase().rawQuery("SELECT * FROM materials ORDER BY timestamp DESC", null);
    }

    public boolean saveFullCertificate(String user, String title, String pres, String sName, String body, String course, String date, String sId, String cId, String dir, String role) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", user); v.put("title", title); v.put("presentation", pres);
        v.put("student_name", sName); v.put("body", body); v.put("course", course);
        v.put("date", date); v.put("student_id", sId); v.put("certificate_id", cId);
        v.put("director", dir); v.put("role", role);
        return db.insert("certificates", null, v) != -1;
    }

    public Cursor getCustomCertificate(String username) {
        return getReadableDatabase().rawQuery("SELECT * FROM certificates WHERE username=? ORDER BY timestamp DESC LIMIT 1", new String[]{username});
    }

    public boolean insertDailyQuestionFull(String q, String a, String b, String c, String d, String ans, String g, String cat, String diff, String expl) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("question", q); v.put("option_a", a); v.put("option_b", b); v.put("option_c", c); v.put("option_d", d);
        v.put("correct_option", ans); v.put("grade", g); v.put("category", cat); v.put("difficulty", diff); v.put("explanation", expl);
        return db.insert("daily_questions", null, v) != -1;
    }

    public void saveLabResult(String user, String type, String text, String sol, boolean isAsgn, String ctx) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", user); v.put("problem_type", type); v.put("problem_text", text);
        v.put("solution_text", sol); v.put("is_assignment", isAsgn ? 1 : 0); v.put("course_context", ctx);
        db.insert("lab_results", null, v);
    }

    public Cursor getLabHistory(String username) {
        return getReadableDatabase().rawQuery("SELECT * FROM lab_results WHERE username=? ORDER BY timestamp DESC", new String[]{username});
    }

    public Cursor getLabStats(String username) {
        return getReadableDatabase().rawQuery("SELECT problem_type, COUNT(*) FROM lab_results WHERE username=? GROUP BY problem_type", new String[]{username});
    }

    public void insertClassCustom(String sub, String ins, String loc, String time, String dur, String day, String grade) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("subject", sub); v.put("instructor", ins); v.put("location", loc);
        v.put("time", time); v.put("duration", dur); v.put("day", day); v.put("grade", grade);
        db.insert("class_schedule", null, v);
    }

    public List<ScheduleModel> getScheduleForGrade(String grade, String day) {
        List<ScheduleModel> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM class_schedule WHERE grade=? AND day=?", new String[]{grade, day});
        if (c.moveToFirst()) {
            do {
                ScheduleModel sm = new ScheduleModel();
                sm.setSubject(c.getString(c.getColumnIndexOrThrow("subject")));
                sm.setInstructor(c.getString(c.getColumnIndexOrThrow("instructor")));
                sm.setStartTime(c.getString(c.getColumnIndexOrThrow("time")));
                sm.setDuration(c.getString(c.getColumnIndexOrThrow("duration")));
                sm.setLocation(c.getString(c.getColumnIndexOrThrow("location")));
                list.add(sm);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public Cursor getStudyNote(String user, String sub, String chap, String top) {
        return getReadableDatabase().rawQuery("SELECT * FROM study_notes WHERE username=? AND subject=? AND chapter=? AND topic=?", new String[]{user, sub, chap, top});
    }

    public void saveStudyNote(String user, String sub, String chap, String top, String content, String highlights, int bookmarked, int important) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", user); v.put("subject", sub); v.put("chapter", chap); v.put("topic", top);
        v.put("content", content); v.put("highlights", highlights);
        v.put("is_bookmarked", bookmarked); v.put("is_important", important);
        db.insertWithOnConflict("study_notes", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean hasPremiumAccess(String user, String grade) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM payments WHERE username=? AND status='Approved'", new String[]{user});
        boolean premium = c.getCount() > 0;
        c.close();
        return premium;
    }

    public int getTableCount(String table, String where, String[] args) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + table;
        if (where != null) query += " WHERE " + where;
        Cursor c = db.rawQuery(query, args);
        int count = 0; if (c.moveToFirst()) count = c.getInt(0);
        c.close(); return count;
    }

    public boolean insertSupportTicket(String user, String title, String cat, String desc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", user); v.put("title", title); v.put("category", cat); v.put("description", desc);
        return db.insert("support_tickets", null, v) != -1;
    }

    public Cursor getAllSupportTickets() {
        return getReadableDatabase().rawQuery("SELECT * FROM support_tickets ORDER BY timestamp DESC", null);
    }

    public void updateTicketReply(int id, String reply) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("reply", reply); cv.put("status", "Closed");
        db.update("support_tickets", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor getChallengeStats(String username) {
        return getReadableDatabase().rawQuery("SELECT * FROM challenge_stats WHERE username=?", new String[]{username});
    }

    public void updateChallengeStats(String user, int xp, int streak, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", user); cv.put("total_xp", xp); cv.put("streak", streak); cv.put("last_completion_date", date);
        db.insertWithOnConflict("challenge_stats", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getUserGrade(String identifier) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT grade_or_role FROM users WHERE username=? OR email=?", new String[]{identifier, identifier});
        String grade = "12"; if (c.moveToFirst()) grade = c.getString(0);
        c.close(); return grade;
    }

    // --- Flashcard Methods (SM-2 Algorithm) ---

    public boolean insertFlashcard(String user, String q, String a, String sub) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", user); v.put("question", q); v.put("answer", a); v.put("subject", sub);
        v.put("next_review_date", String.valueOf(System.currentTimeMillis()));
        return db.insert("flashcards", null, v) != -1;
    }

    public Cursor getFlashcardsForReview(String user) {
        String now = String.valueOf(System.currentTimeMillis());
        return getReadableDatabase().rawQuery("SELECT * FROM flashcards WHERE username=? AND next_review_date <= ? ORDER BY id ASC", new String[]{user, now});
    }

    public void updateFlashcardSM2(int id, int interval, int repetitions, float easeFactor, long nextReview) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("interval", interval);
        cv.put("repetitions", repetitions);
        cv.put("ease_factor", easeFactor);
        cv.put("next_review_date", String.valueOf(nextReview));
        db.update("flashcards", cv, "id=?", new String[]{String.valueOf(id)});
    }

    // --- Mood Log Methods ---

    public void logMood(String user, String mood, String note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", user); cv.put("mood", mood); cv.put("note", note);
        db.insert("mood_logs", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 57) {
            createTables(db);
        } else {
            db.execSQL("DROP TABLE IF EXISTS daily_questions");
            createTables(db);
            seedDailyQuestionsFromCSV(db);
        }
    }
}
