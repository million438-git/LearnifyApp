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
    private static final int DATABASE_VERSION = 33; // Incremented version

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        seedAllData(db);
    }

    private void createTables(SQLiteDatabase db) {
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
                "email_notifications INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE IF NOT EXISTS materials (mat_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, type TEXT, subject TEXT, grade TEXT, file_path TEXT, difficulty TEXT DEFAULT 'Medium', timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks (task_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, subject TEXT, grade TEXT, due_date TEXT, due_time TEXT, priority TEXT, status TEXT DEFAULT 'Pending')");
        db.execSQL("CREATE TABLE IF NOT EXISTS schedule (id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, teacher TEXT, room TEXT, time TEXT, duration TEXT, day TEXT, grade TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS announcements (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, message TEXT, date TEXT, type TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "transaction_id TEXT, " +
                "amount TEXT, " +
                "screenshot_uri TEXT, " + 
                "status TEXT DEFAULT 'Pending', " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS system_settings (id INTEGER PRIMARY KEY, account_details TEXT)");
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
    }

    private void seedAllData(SQLiteDatabase db) {
        ContentValues setting = new ContentValues();
        setting.put("id", 1);
        setting.put("account_details", "🏦 CBE: 1000591245407 (Million Teshome)\n📱 Telebirr: 0902399206 (Million Teshome)");
        db.insertWithOnConflict("system_settings", null, setting, SQLiteDatabase.CONFLICT_IGNORE);

        ContentValues v = new ContentValues();
        v.put("full_name", "System Admin");
        v.put("username", "admin");
        v.put("password", "admin123");
        v.put("user_type", "ADMIN");
        v.put("grade_or_role", "Super Admin");
        db.insertWithOnConflict("users", null, v, SQLiteDatabase.CONFLICT_IGNORE);

        seedDailyQuestions(db);
    }

    private void seedDailyQuestions(SQLiteDatabase db) {
        String[][] questions = {
            {"What is the unit of force?", "Joule", "Watt", "Newton", "Pascal", "Newton", "12", "Physics", "Easy", "Newton is the standard unit of force."},
            {"The speed of light in vacuum is approximately:", "3 x 10^8 m/s", "3 x 10^6 m/s", "150,000 km/s", "340 m/s", "3 x 10^8 m/s", "12", "Physics", "Easy", "Light travels at ~300,000 km/s in vacuum."},
            {"Which law states that F = ma?", "Newton's 1st Law", "Newton's 2nd Law", "Ohm's Law", "Hooke's Law", "Newton's 2nd Law", "12", "Physics", "Easy", "Force equals mass times acceleration."},
            {"What is the unit of electrical resistance?", "Volt", "Ampere", "Ohm", "Coulomb", "Ohm", "12", "Physics", "Medium", "Ohm (Ω) is the unit of resistance."},
            {"Which lens is used to correct nearsightedness?", "Convex", "Concave", "Cylindrical", "Bifocal", "Concave", "11", "Physics", "Medium", "Concave lenses diverge light to help focus on distant objects."},
            {"Which organelle is the powerhouse of the cell?", "Nucleus", "Ribosome", "Mitochondria", "Golgi Body", "Mitochondria", "12", "Biology", "Easy", "Mitochondria produce ATP via respiration."},
            {"How many chambers are in a human heart?", "2", "3", "4", "5", "4", "12", "Biology", "Easy", "Humans have two atria and two ventricles."},
            {"What is the derivative of x^2?", "x", "2x", "x/2", "2", "2x", "12", "Mathematics", "Easy", "Power rule: n*x^(n-1)."},
            {"If 2x + 5 = 15, then x equals:", "5", "10", "15", "20", "5", "12", "Mathematics", "Easy", "2x = 10, so x = 5."},
            {"What is the pH of pure water?", "0", "5", "7", "14", "7", "12", "Chemistry", "Easy", "Water is neutral (pH 7)."}
        };

        for (String[] q : questions) {
            ContentValues cv = new ContentValues();
            cv.put("question", q[0]); cv.put("opt_a", q[1]); cv.put("opt_b", q[2]); cv.put("opt_c", q[3]); cv.put("opt_d", q[4]); cv.put("answer", q[5]); cv.put("grade", q[6]); cv.put("subject", q[7]); cv.put("difficulty", q[8]); cv.put("explanation", q[9]);
            db.insert("daily_questions", null, cv);
        }
    }

    public void addNotification(String username, String title, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("title", title);
        cv.put("message", message);
        cv.put("type", type);
        db.insert("user_notifications", null, cv);
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
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM user_notifications WHERE username=? ORDER BY timestamp DESC, id DESC", new String[]{username});
    }

    public void markNotificationsAsRead(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_read", 1);
        db.update("user_notifications", cv, "username=?", new String[]{username});
    }

    public void markNotificationAsRead(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_read", 1);
        db.update("user_notifications", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteNotification(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("user_notifications", "id=?", new String[]{String.valueOf(id)});
    }

    public void clearAllNotifications(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("user_notifications", "username=?", new String[]{username});
    }

    public boolean insertDailyQuestion(String q, String a, String b, String c, String d, String ans, String grade, String sub, String diff, String expl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("question", q); cv.put("opt_a", a); cv.put("opt_b", b); cv.put("opt_c", c); cv.put("opt_d", d); cv.put("answer", ans); cv.put("grade", grade); cv.put("subject", sub); cv.put("difficulty", diff); cv.put("explanation", expl);
        return db.insert("daily_questions", null, cv) != -1;
    }

    public boolean insertMaterial(String title, String content, String type, String subject, String grade, String path, String difficulty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", title); v.put("content", content); v.put("type", type); v.put("subject", subject); v.put("grade", grade); v.put("file_path", path); v.put("difficulty", difficulty);
        return db.insert("materials", null, v) != -1;
    }

    public List<MaterialModel> getMaterialsForGrade(String grade) {
        List<MaterialModel> list = new ArrayList<>();
        try {
            Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM materials WHERE grade = ? ORDER BY mat_id DESC", new String[]{grade});
            if (c.moveToFirst()) {
                do {
                    MaterialModel m = new MaterialModel();
                    m.setId(c.getInt(0)); m.setTitle(c.getString(1)); m.setContent(c.getString(2)); m.setType(c.getString(3)); m.setSubject(c.getString(4)); m.setGrade(c.getString(5)); m.setFilePath(c.getString(6));
                    list.add(m);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean checkUser(String user, String pass, String type) {
        try {
            Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM users WHERE (username=? OR email=?) AND password=? AND user_type=? AND status='Active'", new String[]{user, user, pass, type});
            boolean exists = c.getCount() > 0; c.close(); return exists;
        } catch (Exception e) { return false; }
    }

    public boolean isEmailRegistered(String email) {
        try {
            Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM users WHERE email=? COLLATE NOCASE", new String[]{email});
            boolean exists = c.getCount() > 0; c.close(); return exists;
        } catch (Exception e) { return false; }
    }

    public String getUserProfileImage(String username) {
        try {
            Cursor c = this.getReadableDatabase().rawQuery("SELECT profile_image FROM users WHERE username = ?", new String[]{username});
            if (c.moveToFirst()) { String img = c.getString(0); c.close(); return img; }
            c.close();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String getUserGrade(String username) {
        try {
            Cursor c = this.getReadableDatabase().rawQuery("SELECT grade_or_role FROM users WHERE username = ? AND user_type='STUDENT'", new String[]{username});
            if (c.moveToFirst()) { String g = c.getString(0); c.close(); return g; }
            c.close();
        } catch (Exception e) { e.printStackTrace(); }
        return "10";
    }

    public boolean registerUser(String name, String uid, String gradeRole, String user, String email, String pass, String imageUri, String type) {
        ContentValues v = new ContentValues();
        v.put("full_name", name); v.put("unique_id", uid); v.put("grade_or_role", gradeRole); v.put("username", user); v.put("email", email); v.put("password", pass); v.put("profile_image", imageUri); v.put("user_type", type);
        return this.getWritableDatabase().insert("users", null, v) != -1;
    }

    public Cursor getAllPayments() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM payments ORDER BY id DESC", null);
    }

    public void updatePaymentStatus(int id, String status) {
        ContentValues v = new ContentValues(); v.put("status", status);
        this.getWritableDatabase().update("payments", v, "id = ?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllSupportTickets() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM support_tickets ORDER BY id DESC", null);
    }

    public void updateTicketReply(int id, String reply) {
        ContentValues cv = new ContentValues();
        cv.put("admin_reply", reply);
        cv.put("status", "Resolved");
        this.getWritableDatabase().update("support_tickets", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public boolean updatePassword(String username, String newPassword) {
        ContentValues v = new ContentValues();
        v.put("password", newPassword);
        return this.getWritableDatabase().update("users", v, "username=?", new String[]{username}) > 0;
    }

    public boolean updatePasswordByEmail(String email, String newPassword) {
        ContentValues v = new ContentValues();
        v.put("password", newPassword);
        return this.getWritableDatabase().update("users", v, "email=? COLLATE NOCASE", new String[]{email}) > 0;
    }

    public List<AnnouncementModel> getAnnouncements() {
        List<AnnouncementModel> list = new ArrayList<>();
        try {
            Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM announcements ORDER BY id DESC", null);
            if (c.moveToFirst()) {
                do {
                    AnnouncementModel a = new AnnouncementModel();
                    a.setTitle(c.getString(1)); a.setMessage(c.getString(2)); a.setDate(c.getString(3)); a.setType(c.getString(4));
                    list.add(a);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean insertTask(String title, String desc, String sub, String grade, String date, String time, String priority) {
        ContentValues v = new ContentValues();
        v.put("title", title); v.put("description", desc); v.put("subject", sub); v.put("grade", grade); v.put("due_date", date); v.put("due_time", time); v.put("priority", priority); v.put("status", "Pending");
        return this.getWritableDatabase().insert("tasks", null, v) != -1;
    }

    public List<TaskModel> getTasksForGrade(String grade) {
        List<TaskModel> list = new ArrayList<>();
        try {
            Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM tasks WHERE grade = ? ORDER BY task_id DESC", new String[]{grade});
            if (c.moveToFirst()) {
                do {
                    TaskModel t = new TaskModel();
                    t.setId(c.getInt(0)); t.setTitle(c.getString(1)); t.setDescription(c.getString(2)); t.setSubject(c.getString(3)); t.setGrade(c.getString(4)); t.setDueDate(c.getString(5)); t.setDueTime(c.getString(6)); t.setPriority(c.getString(7)); t.setStatus(c.getString(8));
                    list.add(t);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void updateTaskStatus(int taskId, String status) {
        ContentValues v = new ContentValues(); v.put("status", status);
        this.getWritableDatabase().update("tasks", v, "task_id = ?", new String[]{String.valueOf(taskId)});
    }

    public void deleteTask(int taskId) {
        this.getWritableDatabase().delete("tasks", "task_id = ?", new String[]{String.valueOf(taskId)});
    }

    public boolean insertClassCustom(String sub, String tea, String room, String time, String dur, String day, String grade) {
        ContentValues v = new ContentValues();
        v.put("subject", sub); v.put("teacher", tea); v.put("room", room); v.put("time", time); v.put("duration", dur); v.put("day", day); v.put("grade", grade);
        return this.getWritableDatabase().insert("schedule", null, v) != -1;
    }

    public List<ScheduleModel> getScheduleForGrade(String grade, String day) {
        List<ScheduleModel> list = new ArrayList<>();
        try {
            Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM schedule WHERE grade = ? AND day = ?", new String[]{grade, day});
            if (c.moveToFirst()) {
                do {
                    ScheduleModel s = new ScheduleModel();
                    s.setSubject(c.getString(1)); s.setTeacher(c.getString(2)); s.setRoom(c.getString(3)); s.setTime(c.getString(4)); s.setDuration(c.getString(5));
                    list.add(s);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public Cursor getChallengeStats(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM challenge_stats WHERE username=?", new String[]{username});
    }

    public void updateChallengeStats(String username, int score, int newStreak, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = getChallengeStats(username);
        ContentValues cv = new ContentValues();
        if (cursor.moveToFirst()) {
            int currentTotal = cursor.getInt(cursor.getColumnIndexOrThrow("total_score"));
            int maxStreak = cursor.getInt(cursor.getColumnIndexOrThrow("max_streak"));
            cv.put("total_score", currentTotal + score);
            cv.put("current_streak", newStreak);
            if (newStreak > maxStreak) cv.put("max_streak", newStreak);
            cv.put("last_challenge_date", date);
            db.update("challenge_stats", cv, "username=?", new String[]{username});
        } else {
            cv.put("username", username); cv.put("total_score", score); cv.put("current_streak", newStreak); cv.put("max_streak", newStreak); cv.put("last_challenge_date", date);
            db.insert("challenge_stats", null, cv);
        }
        cursor.close();
    }

    public Cursor getDailyQuestions(String grade) {
        String numericGrade = grade.replaceAll("[^0-9]", "");
        String query = "SELECT * FROM daily_questions " +
                       "ORDER BY (CASE WHEN grade = ? THEN 0 WHEN grade = ? THEN 1 " +
                       "WHEN grade = 'General' THEN 2 ELSE 3 END), RANDOM() LIMIT 5";
        return this.getReadableDatabase().rawQuery(query, new String[]{grade, numericGrade});
    }

    public Cursor getUserDetails(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
    }

    public boolean updateUserProfileFull(String username, String fullName, String email, String phone, String university, String year, String major, int xpGoal, String subjects, String visibility, int showLeaderboard, int emailNotifications, String imageUri) {
        ContentValues v = new ContentValues();
        v.put("full_name", fullName);
        v.put("email", email);
        v.put("phone", phone);
        v.put("university", university);
        v.put("year", year);
        v.put("major", major);
        v.put("xp_goal", xpGoal);
        v.put("subjects", subjects);
        v.put("visibility", visibility);
        v.put("show_leaderboard", showLeaderboard);
        v.put("email_notifications", emailNotifications);
        if (imageUri != null && !imageUri.isEmpty()) {
            v.put("profile_image", imageUri);
        }
        return this.getWritableDatabase().update("users", v, "username = ?", new String[]{username}) > 0;
    }

    public boolean updateUserProfile(String username, String fullName, String email, String grade, String imageUri) {
        ContentValues v = new ContentValues();
        v.put("full_name", fullName);
        v.put("email", email);
        v.put("grade_or_role", grade);
        if (imageUri != null && !imageUri.isEmpty()) {
            v.put("profile_image", imageUri);
        }
        return this.getWritableDatabase().update("users", v, "username = ?", new String[]{username}) > 0;
    }

    public Cursor getLeaderboard() {
        return this.getReadableDatabase().rawQuery("SELECT username, total_score, max_streak FROM challenge_stats ORDER BY total_score DESC LIMIT 10", null);
    }

    public void saveStudyNote(String user, String sub, String chap, String top, String note, String highlights, int bookmarked, int important) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", user);
        cv.put("subject", sub);
        cv.put("chapter", chap);
        cv.put("topic", top);
        cv.put("personal_note", note);
        cv.put("highlight_data", highlights);
        cv.put("is_bookmarked", bookmarked);
        cv.put("is_important", important);
        db.insertWithOnConflict("study_notes", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Cursor getStudyNote(String user, String sub, String chap, String top) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM study_notes WHERE username=? AND subject=? AND chapter=? AND topic=?", new String[]{user, sub, chap, top});
    }

    public boolean submitPayment(String user, String txId, String amount, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", user);
        cv.put("transaction_id", txId);
        cv.put("amount", amount);
        cv.put("screenshot_uri", uri);
        cv.put("status", "Pending");
        return db.insert("payments", null, cv) != -1;
    }

    public String getAdminAccountDetails() {
        Cursor c = this.getReadableDatabase().rawQuery("SELECT account_details FROM system_settings WHERE id=1", null);
        if (c.moveToFirst()) { String d = c.getString(0); c.close(); return d; }
        c.close(); return "Contact Admin: 0902399206";
    }

    public boolean checkPaymentStatus(String user) {
        Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM payments WHERE username=? AND status='Approved'", new String[]{user});
        boolean isPaid = c.getCount() > 0; c.close(); return isPaid;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 33) {
            db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN university TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN year TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN major TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN xp_goal INTEGER DEFAULT 500");
            db.execSQL("ALTER TABLE users ADD COLUMN subjects TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN visibility TEXT DEFAULT 'Public'");
            db.execSQL("ALTER TABLE users ADD COLUMN show_leaderboard INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE users ADD COLUMN email_notifications INTEGER DEFAULT 1");
        }
        createTables(db);
    }
}
