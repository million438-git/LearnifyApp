package com.million.learnify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "AceGradeProfessional.db";
    private static final int DATABASE_VERSION = 58;
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        // seedAllData(db); // REMOVED FOR PRODUCTION SECURITY: Do not seed hardcoded admin accounts
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
        db.execSQL("CREATE TABLE IF NOT EXISTS lesson_progress (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, subject TEXT, grade TEXT, chapter TEXT, topic TEXT, is_completed INTEGER DEFAULT 0, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, UNIQUE(username, subject, grade, chapter, topic))");
        db.execSQL("CREATE TABLE IF NOT EXISTS lab_notebook (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "investigation_id TEXT, " +
                "title TEXT, " +
                "subject TEXT, " +
                "grade TEXT, " +
                "curriculum_ref TEXT, " +
                "hypothesis TEXT, " +
                "observations TEXT, " +
                "diagram_structures TEXT, " +
                "simulation_inputs TEXT, " +
                "simulation_results TEXT, " +
                "conclusion TEXT, " +
                "quiz_score INTEGER DEFAULT 0, " +
                "current_stage INTEGER DEFAULT 1, " +
                "is_completed INTEGER DEFAULT 0, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(username, investigation_id))");
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

        // NEW: AI Chat History for persistent student conversations
        db.execSQL("CREATE TABLE IF NOT EXISTS ai_chat_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "message TEXT, " +
                "is_user INTEGER, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    private void seedAllData(SQLiteDatabase db) {
        ContentValues v = new ContentValues();
        v.put("full_name", "System Admin");
        // Default hardcoded admin account removed for security
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
        if (user == null || pass == null || type == null) {
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, password FROM users WHERE (username=? OR email=?) AND user_type=?", new String[]{user, user, type});
        if (c == null) {
            return false;
        }

        int userId = -1;
        String storedPassword = null;
        if (c.moveToFirst()) {
            userId = c.getInt(0);
            storedPassword = c.getString(1);
        }
        c.close();

        if (storedPassword == null || userId == -1) {
            return false;
        }

        boolean isValid = PasswordUtils.verifyPassword(pass, storedPassword);
        if (!isValid) {
            return false;
        }

        // Controlled lazy migration: upgrade legacy plaintext records upon verified login
        if (PasswordUtils.needsUpgrade(storedPassword)) {
            try {
                String upgradedHash = PasswordUtils.hashPassword(pass);
                SQLiteDatabase writeDb = getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("password", upgradedHash);
                writeDb.update("users", cv, "id=?", new String[]{String.valueOf(userId)});
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Failed to migrate legacy password hash", e);
            }
        }

        return true;
    }

    public boolean registerUser(String name, String uid, String role, String user, String email, String pass, String img, String type) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("full_name", name); v.put("unique_id", uid); v.put("grade_or_role", role);
        v.put("username", user); v.put("email", email);
        v.put("password", PasswordUtils.hashPassword(pass));
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

    public boolean updateUserProfileImage(String identifier, String imageUri) {
        if (identifier == null) return false;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("profile_image", imageUri != null ? imageUri : "");
            return db.update("users", cv, "username=? OR email=?", new String[]{identifier, identifier}) > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to update profile image for " + identifier, e);
            return false;
        }
    }

    public boolean deleteUserAccount(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Resolve canonical username
            String username = identifier.trim();
            Cursor c = db.rawQuery("SELECT username FROM users WHERE username=? OR email=?", new String[]{identifier, identifier});
            if (c != null) {
                if (c.moveToFirst()) {
                    username = c.getString(0);
                }
                c.close();
            }

            String[] userArg = new String[]{username};
            db.delete("challenge_stats", "username=?", userArg);
            db.delete("user_notifications", "username=?", userArg);
            db.delete("lab_results", "username=?", userArg);
            db.delete("enrollments", "username=?", userArg);
            db.delete("payments", "username=?", userArg);
            db.delete("exam_results", "username=?", userArg);
            db.delete("certificates", "username=?", userArg);
            db.delete("study_notes", "username=?", userArg);
            db.delete("flashcards", "username=?", userArg);
            db.delete("mood_logs", "username=?", userArg);
            db.delete("support_tickets", "username=?", userArg);

            int deletedUsers = db.delete("users", "username=?", userArg);

            db.setTransactionSuccessful();
            return deletedUsers > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to cascade delete user account: " + identifier, e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public JSONObject exportUserData(String identifier) {
        JSONObject root = new JSONObject();
        if (identifier == null || identifier.trim().isEmpty()) {
            return root;
        }
        SQLiteDatabase db = getReadableDatabase();
        try {
            String username = identifier.trim();
            JSONObject profileObj = new JSONObject();
            Cursor c = db.rawQuery("SELECT full_name, username, email, phone, university, year, major, xp_goal, subjects, visibility, created_at, user_type FROM users WHERE username=? OR email=?", new String[]{identifier, identifier});
            if (c != null) {
                if (c.moveToFirst()) {
                    username = c.getString(c.getColumnIndexOrThrow("username"));
                    profileObj.put("full_name", c.getString(c.getColumnIndexOrThrow("full_name")));
                    profileObj.put("username", username);
                    profileObj.put("email", c.getString(c.getColumnIndexOrThrow("email")));
                    profileObj.put("phone", c.getString(c.getColumnIndexOrThrow("phone")));
                    profileObj.put("university", c.getString(c.getColumnIndexOrThrow("university")));
                    profileObj.put("year", c.getString(c.getColumnIndexOrThrow("year")));
                    profileObj.put("major", c.getString(c.getColumnIndexOrThrow("major")));
                    profileObj.put("xp_goal", c.getInt(c.getColumnIndexOrThrow("xp_goal")));
                    profileObj.put("subjects", c.getString(c.getColumnIndexOrThrow("subjects")));
                    profileObj.put("visibility", c.getString(c.getColumnIndexOrThrow("visibility")));
                    profileObj.put("user_type", c.getString(c.getColumnIndexOrThrow("user_type")));
                    profileObj.put("created_at", c.getString(c.getColumnIndexOrThrow("created_at")));
                }
                c.close();
            }
            root.put("app", "Learnify");
            root.put("export_version", "2.8");
            root.put("exported_at", java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
            root.put("profile", profileObj);

            // Challenge stats
            JSONObject challengeObj = new JSONObject();
            Cursor cStats = db.rawQuery("SELECT total_xp, streak, last_completion_date FROM challenge_stats WHERE username=?", new String[]{username});
            if (cStats != null) {
                if (cStats.moveToFirst()) {
                    challengeObj.put("total_xp", cStats.getInt(0));
                    challengeObj.put("streak", cStats.getInt(1));
                    challengeObj.put("last_completion_date", cStats.getString(2));
                }
                cStats.close();
            }
            root.put("learning_stats", challengeObj);

            // Enrollments
            JSONArray enrollmentsArray = new JSONArray();
            Cursor cEnr = db.rawQuery("SELECT course_name FROM enrollments WHERE username=?", new String[]{username});
            if (cEnr != null) {
                while (cEnr.moveToNext()) {
                    enrollmentsArray.put(cEnr.getString(0));
                }
                cEnr.close();
            }
            root.put("enrollments", enrollmentsArray);

            // Exam results
            JSONArray examArray = new JSONArray();
            Cursor cExam = db.rawQuery("SELECT subject, score_text, total_questions, correct_answers, timestamp FROM exam_results WHERE username=?", new String[]{username});
            if (cExam != null) {
                while (cExam.moveToNext()) {
                    JSONObject examItem = new JSONObject();
                    examItem.put("subject", cExam.getString(0));
                    examItem.put("score", cExam.getString(1));
                    examItem.put("total_questions", cExam.getInt(2));
                    examItem.put("correct_answers", cExam.getInt(3));
                    examItem.put("date", cExam.getString(4));
                    examArray.put(examItem);
                }
                cExam.close();
            }
            root.put("exam_results", examArray);

            // Certificates
            JSONArray certArray = new JSONArray();
            Cursor cCert = db.rawQuery("SELECT title, course, date, certificate_id FROM certificates WHERE username=?", new String[]{username});
            if (cCert != null) {
                while (cCert.moveToNext()) {
                    JSONObject certItem = new JSONObject();
                    certItem.put("title", cCert.getString(0));
                    certItem.put("course", cCert.getString(1));
                    certItem.put("date", cCert.getString(2));
                    certItem.put("certificate_id", cCert.getString(3));
                    certArray.put(certItem);
                }
                cCert.close();
            }
            root.put("certificates", certArray);

            // Summary counts
            JSONObject activitySummary = new JSONObject();
            Cursor cNotes = db.rawQuery("SELECT COUNT(*) FROM study_notes WHERE username=?", new String[]{username});
            if (cNotes != null) {
                if (cNotes.moveToFirst()) activitySummary.put("total_study_notes", cNotes.getInt(0));
                cNotes.close();
            }
            Cursor cCards = db.rawQuery("SELECT COUNT(*) FROM flashcards WHERE username=?", new String[]{username});
            if (cCards != null) {
                if (cCards.moveToFirst()) activitySummary.put("total_flashcards", cCards.getInt(0));
                cCards.close();
            }
            Cursor cMood = db.rawQuery("SELECT COUNT(*) FROM mood_logs WHERE username=?", new String[]{username});
            if (cMood != null) {
                if (cMood.moveToFirst()) activitySummary.put("total_mood_logs", cMood.getInt(0));
                cMood.close();
            }
            root.put("activity_summary", activitySummary);

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error exporting user data for " + identifier, e);
        }
        return root;
    }

    public boolean updatePassword(String identifier, String newPassword) {
        if (identifier == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("password", PasswordUtils.hashPassword(newPassword));
            return db.update("users", cv, "username=? OR email=?", new String[]{identifier, identifier}) > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to update password for " + identifier, e);
            return false;
        }
    }

    public boolean verifyUserPassword(String identifier, String candidatePassword) {
        if (identifier == null || candidatePassword == null || candidatePassword.isEmpty()) {
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT id, password FROM users WHERE username=? OR email=?", new String[]{identifier, identifier});
            if (c == null || !c.moveToFirst()) {
                return false;
            }
            int userId = c.getInt(0);
            String storedPassword = c.getString(1);
            if (storedPassword == null) {
                return false;
            }
            boolean isValid = PasswordUtils.verifyPassword(candidatePassword, storedPassword);
            if (!isValid) {
                return false;
            }
            if (PasswordUtils.needsUpgrade(storedPassword)) {
                try {
                    String upgradedHash = PasswordUtils.hashPassword(candidatePassword);
                    SQLiteDatabase writeDb = getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    cv.put("password", upgradedHash);
                    writeDb.update("users", cv, "id=?", new String[]{String.valueOf(userId)});
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Failed to migrate legacy password during verification", e);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error verifying password for " + identifier, e);
            return false;
        } finally {
            if (c != null) {
                c.close();
            }
        }
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
        cv.put("password", PasswordUtils.hashPassword(newPass));
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
        return getReadableDatabase().rawQuery("SELECT * FROM payments WHERE username=? ORDER BY id DESC", new String[]{username});
    }

    public Cursor getAllPayments() {
        return getReadableDatabase().rawQuery("SELECT * FROM payments ORDER BY timestamp DESC", null);
    }

    public void updatePaymentStatus(int id, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        db.update("payments", cv, "id=?", new String[]{String.valueOf(id)});

        try {
            Cursor c = db.rawQuery("SELECT username, amount, transaction_id FROM payments WHERE id=?", new String[]{String.valueOf(id)});
            if (c != null && c.moveToFirst()) {
                String u = c.getString(0);
                String amt = c.getString(1);
                String tx = c.getString(2);
                c.close();
                if ("Approved".equalsIgnoreCase(status)) {
                    addNotification(u, "Payment Approved", "Your payment for " + amt + " (TxID: " + tx + ") was verified. Premium learning access is now unlocked.", "payment");
                } else if ("Rejected".equalsIgnoreCase(status)) {
                    addNotification(u, "Payment Verification Notice", "Your payment proof for " + amt + " (TxID: " + tx + ") could not be verified. Please check details and re-submit.", "payment");
                }
            } else if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Failed to dispatch payment notification", e);
        }
    }

    public boolean submitPayment(String username, String txId, String amount, String img) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", username); v.put("transaction_id", txId); v.put("amount", amount); v.put("image_path", img);
        long rowId = db.insert("payments", null, v);
        if (rowId != -1) {
            try {
                addNotification(username, "Payment Submitted ⏳", "Your payment proof for " + amount + " (TxID: " + txId + ") is under review by admin.", "payment");
            } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    public String getAdminAccountDetails() {
        return "CBE: 1000591245407\nTelebirr: 0902939206\nBeneficiary: Million Teshome Gudeto";
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
        String sql = "SELECT id, username, " +
                     "title, title AS cert_title, " +
                     "presentation, presentation AS presentation_text, " +
                     "student_name, " +
                     "body, body AS body_text, " +
                     "course, course AS course_name, " +
                     "date, date AS issue_date, " +
                     "student_id, certificate_id, " +
                     "director, director AS director_name, " +
                     "role, role AS director_role, " +
                     "timestamp " +
                     "FROM certificates WHERE username=? ORDER BY id DESC LIMIT 1";
        return getReadableDatabase().rawQuery(sql, new String[]{username});
    }

    public Cursor getAllCertificates() {
        return getReadableDatabase().rawQuery("SELECT * FROM certificates ORDER BY id DESC", null);
    }

    public Cursor getCertificateById(String certId) {
        if (certId == null) return null;
        return getReadableDatabase().rawQuery("SELECT * FROM certificates WHERE certificate_id=?", new String[]{certId.trim()});
    }

    public boolean hasCertificateForCourse(String username, String course) {
        if (username == null || course == null) return false;
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery("SELECT id FROM certificates WHERE username=? AND course=? COLLATE NOCASE", new String[]{username.trim(), course.trim()});
            return (c != null && c.getCount() > 0);
        } catch (Exception e) {
            return false;
        } finally {
            if (c != null) c.close();
        }
    }

    public boolean isCertificateIdTaken(String certId) {
        if (certId == null) return false;
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery("SELECT id FROM certificates WHERE certificate_id=?", new String[]{certId.trim()});
            return (c != null && c.getCount() > 0);
        } catch (Exception e) {
            return false;
        } finally {
            if (c != null) c.close();
        }
    }

    public int getCertificateCount() {
        return getTableCount("certificates", null, null);
    }

    public int getEligibleStudentCount() {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                "SELECT COUNT(DISTINCT u.username) FROM users u " +
                "INNER JOIN payments p ON u.username = p.username " +
                "WHERE u.user_type='STUDENT' AND u.status='Active' AND p.status='Approved'", null);
            if (c != null && c.moveToFirst()) return c.getInt(0);
        } catch (Exception ignored) {
        } finally {
            if (c != null) c.close();
        }
        return 0;
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

    public boolean deleteLabResult(int id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            return db.delete("lab_results", "id=?", new String[]{String.valueOf(id)}) > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to delete lab result " + id, e);
            return false;
        }
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

    public long getTrialDurationMillis() {
        return 3L * 24L * 60L * 60L * 1000L; // 3 Days in milliseconds
    }

    public boolean isTrialActive(String username) {
        return getTrialRemainingMillis(username) > 0;
    }

    public long getTrialRemainingMillis(String username) {
        if (username == null || username.trim().isEmpty() || "admin".equalsIgnoreCase(username)) {
            return 0;
        }
        long createdAtMillis = 0;
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery("SELECT created_at FROM users WHERE username=?", new String[]{username});
            if (c != null && c.moveToFirst()) {
                String createdStr = c.getString(0);
                if (createdStr != null && !createdStr.trim().isEmpty()) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    java.util.Date date = sdf.parse(createdStr);
                    if (date != null) {
                        createdAtMillis = date.getTime();
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (c != null) c.close();
        }

        if (createdAtMillis <= 0) {
            return 0;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - createdAtMillis;
        long duration = getTrialDurationMillis();
        if (elapsed >= 0 && elapsed < duration) {
            return duration - elapsed;
        }
        return 0;
    }

    public boolean hasPremiumAccess(String user, String grade) {
        return hasPremiumAccess(user, grade, null);
    }

    public boolean hasPremiumAccess(String user, String grade, String subjectName) {
        if (user == null || user.trim().isEmpty()) return false;

        // 1. 3-Day Free Trial check: grants full access to test all subjects
        if (isTrialActive(user)) {
            return true;
        }

        // 2. Check approved payments
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery("SELECT amount FROM payments WHERE username=? AND status='Approved' ORDER BY id DESC", new String[]{user});
            if (c != null && c.moveToFirst()) {
                do {
                    String amount = c.getString(0);
                    if (amount == null) amount = "";
                    String amtUpper = amount.toUpperCase().trim();

                    // Plan B: 100 ETB grants full lifetime access to all subjects
                    if (amtUpper.contains("100") || amtUpper.contains("FULL") || amtUpper.contains("PREMIUM")) {
                        return true;
                    }

                    // Plan A: 50 ETB grants access to Mathematics and English only
                    if (amtUpper.contains("50") || amtUpper.contains("BASIC") || amtUpper.contains("STANDARD")) {
                        if (subjectName == null || subjectName.trim().isEmpty()) {
                            return true; // General access check
                        }
                        String sLower = subjectName.toLowerCase().trim();
                        if (sLower.contains("math") || sLower.contains("english")) {
                            return true;
                        }
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "hasPremiumAccess query error", e);
        } finally {
            if (c != null) c.close();
        }

        return false;
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

    // --- AI Chat History Methods ---

    public void saveChatMessage(String user, String message, boolean isUser) {
        if (user == null || message == null || message.trim().isEmpty()) return;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("username", user);
            cv.put("message", message);
            cv.put("is_user", isUser ? 1 : 0);
            db.insert("ai_chat_history", null, cv);
        } catch (Exception e) {
            Log.e(TAG, "Error saving chat message", e);
        }
    }

    public List<ChatMessage> getChatHistory(String user) {
        List<ChatMessage> list = new ArrayList<>();
        if (user == null) return list;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery("SELECT message, is_user FROM ai_chat_history WHERE username = ? ORDER BY id ASC LIMIT 100", new String[]{user});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String msg = cursor.getString(0);
                    boolean isUser = cursor.getInt(1) == 1;
                    list.add(new ChatMessage(msg, isUser));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching chat history", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public void clearChatHistory(String user) {
        if (user == null) return;
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete("ai_chat_history", "username = ?", new String[]{user});
        } catch (Exception e) {
            Log.e(TAG, "Error clearing chat history", e);
        }
    }

    public void markLessonCompleted(String username, String subject, String grade, String chapter, String topic, boolean completed) {
        if (username == null || subject == null || chapter == null || topic == null) return;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("username", username);
            cv.put("subject", subject);
            cv.put("grade", grade != null ? grade : "10");
            cv.put("chapter", chapter);
            cv.put("topic", topic);
            cv.put("is_completed", completed ? 1 : 0);
            db.insertWithOnConflict("lesson_progress", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e(TAG, "Error marking lesson completed", e);
        }
    }

    public boolean isLessonCompleted(String username, String subject, String grade, String chapter, String topic) {
        if (username == null || subject == null || chapter == null || topic == null) return false;
        Cursor c = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            c = db.rawQuery(
                    "SELECT is_completed FROM lesson_progress WHERE username=? AND subject=? AND grade=? AND chapter=? AND topic=?",
                    new String[]{username, subject, grade != null ? grade : "10", chapter, topic});
            if (c != null && c.moveToFirst()) {
                return c.getInt(0) == 1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking lesson completion", e);
        } finally {
            if (c != null) c.close();
        }
        return false;
    }

    public int getCompletedLessonsCount(String username, String subject, String grade) {
        if (username == null || subject == null) return 0;
        Cursor c = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            c = db.rawQuery(
                    "SELECT COUNT(*) FROM lesson_progress WHERE username=? AND subject=? AND grade=? AND is_completed=1",
                    new String[]{username, subject, grade != null ? grade : "10"});
            if (c != null && c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting completed count", e);
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    public int getCompletedLessonsCountInChapter(String username, String subject, String grade, String chapter) {
        if (username == null || subject == null || chapter == null) return 0;
        Cursor c = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            c = db.rawQuery(
                    "SELECT COUNT(*) FROM lesson_progress WHERE username=? AND subject=? AND grade=? AND chapter=? AND is_completed=1",
                    new String[]{username, subject, grade != null ? grade : "10", chapter});
            if (c != null && c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting chapter completed count", e);
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    
    public boolean saveNotebookEntry(String username, String investigationId, String title,
                                     String subject, String grade, String curriculumRef,
                                     String hypothesis, String observations, String diagramStructures,
                                     String simulationInputs, String simulationResults, String conclusion,
                                     int quizScore, int currentStage, boolean isCompleted) {
        if (username == null || investigationId == null) return false;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("username", username);
            cv.put("investigation_id", investigationId);
            cv.put("title", title != null ? title : "Investigation");
            cv.put("subject", subject != null ? subject : "Science");
            cv.put("grade", grade != null ? grade : "9");
            cv.put("curriculum_ref", curriculumRef != null ? curriculumRef : "");
            cv.put("hypothesis", hypothesis != null ? hypothesis : "");
            cv.put("observations", observations != null ? observations : "");
            cv.put("diagram_structures", diagramStructures != null ? diagramStructures : "");
            cv.put("simulation_inputs", simulationInputs != null ? simulationInputs : "");
            cv.put("simulation_results", simulationResults != null ? simulationResults : "");
            cv.put("conclusion", conclusion != null ? conclusion : "");
            cv.put("quiz_score", quizScore);
            cv.put("current_stage", currentStage);
            cv.put("is_completed", isCompleted ? 1 : 0);
            cv.put("updated_at", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
            long id = db.insertWithOnConflict("lab_notebook", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            return id != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error saving lab notebook entry", e);
            return false;
        }
    }

    public static class NotebookEntry {
        public int id;
        public String username;
        public String investigationId;
        public String title;
        public String subject;
        public String grade;
        public String curriculumRef;
        public String hypothesis;
        public String observations;
        public String diagramStructures;
        public String simulationInputs;
        public String simulationResults;
        public String conclusion;
        public int quizScore;
        public int currentStage;
        public boolean isCompleted;
        public String createdAt;
        public String updatedAt;

        public static NotebookEntry fromCursor(Cursor c) {
            if (c == null) return null;
            NotebookEntry entry = new NotebookEntry();
            entry.id = c.getInt(c.getColumnIndexOrThrow("id"));
            entry.username = c.getString(c.getColumnIndexOrThrow("username"));
            entry.investigationId = c.getString(c.getColumnIndexOrThrow("investigation_id"));
            entry.title = c.getString(c.getColumnIndexOrThrow("title"));
            entry.subject = c.getString(c.getColumnIndexOrThrow("subject"));
            entry.grade = c.getString(c.getColumnIndexOrThrow("grade"));
            entry.curriculumRef = c.getString(c.getColumnIndexOrThrow("curriculum_ref"));
            entry.hypothesis = c.getString(c.getColumnIndexOrThrow("hypothesis"));
            entry.observations = c.getString(c.getColumnIndexOrThrow("observations"));
            entry.diagramStructures = c.getString(c.getColumnIndexOrThrow("diagram_structures"));
            entry.simulationInputs = c.getString(c.getColumnIndexOrThrow("simulation_inputs"));
            entry.simulationResults = c.getString(c.getColumnIndexOrThrow("simulation_results"));
            entry.conclusion = c.getString(c.getColumnIndexOrThrow("conclusion"));
            entry.quizScore = c.getInt(c.getColumnIndexOrThrow("quiz_score"));
            entry.currentStage = c.getInt(c.getColumnIndexOrThrow("current_stage"));
            entry.isCompleted = c.getInt(c.getColumnIndexOrThrow("is_completed")) == 1;
            entry.createdAt = c.getString(c.getColumnIndexOrThrow("created_at"));
            entry.updatedAt = c.getString(c.getColumnIndexOrThrow("updated_at"));
            return entry;
        }
    }

    public boolean saveNotebookEntry(String username, String investigationId, String title,
                                     String subject, int grade, String curriculumRef,
                                     String hypothesis, String observations, String diagramStructures,
                                     String simulationInputs, String simulationResults, String conclusion,
                                     int quizScore, int currentStage, boolean isCompleted) {
        return saveNotebookEntry(username, investigationId, title, subject, String.valueOf(grade),
                curriculumRef, hypothesis, observations, diagramStructures, simulationInputs,
                simulationResults, conclusion, quizScore, currentStage, isCompleted);
    }

    public NotebookEntry getNotebookEntry(String username, String investigationId) {
        Cursor c = getNotebookEntryCursor(username, investigationId);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return NotebookEntry.fromCursor(c);
                }
            } finally {
                c.close();
            }
        }
        return null;
    }

    public Cursor getNotebookEntryCursor(String username, String investigationId) {
        if (username == null || investigationId == null) return null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            return db.rawQuery(
                    "SELECT * FROM lab_notebook WHERE username=? AND investigation_id=?",
                    new String[]{username, investigationId});
        } catch (Exception e) {
            Log.e(TAG, "Error querying notebook entry", e);
            return null;
        }
    }

    public java.util.List<NotebookEntry> getNotebookEntries(String username) {
        java.util.List<NotebookEntry> list = new java.util.ArrayList<>();
        Cursor c = getNotebookEntriesCursor(username);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    list.add(NotebookEntry.fromCursor(c));
                }
            } finally {
                c.close();
            }
        }
        return list;
    }

    public Cursor getNotebookEntriesCursor(String username) {
        if (username == null) return null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            return db.rawQuery(
                    "SELECT * FROM lab_notebook WHERE username=? ORDER BY updated_at DESC",
                    new String[]{username});
        } catch (Exception e) {
            Log.e(TAG, "Error querying notebook entries", e);
            return null;
        }
    }

    public boolean deleteNotebookEntry(String username, String investigationId) {
        if (username == null || investigationId == null) return false;
        try {
            SQLiteDatabase db = getWritableDatabase();
            int rows = db.delete("lab_notebook", "username=? AND investigation_id=?", new String[]{username, investigationId});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting notebook entry", e);
            return false;
        }
    }

    public int getCompletedInvestigationsCount(String username) {
        if (username == null) return 0;
        Cursor c = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            c = db.rawQuery(
                    "SELECT COUNT(*) FROM lab_notebook WHERE username=? AND is_completed=1",
                    new String[]{username});
            if (c != null && c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error counting completed investigations", e);
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 57) {
            createTables(db);
        } else if (oldVersion < DATABASE_VERSION) {
            createTables(db);
        } else {
            db.execSQL("DROP TABLE IF EXISTS daily_questions");
            createTables(db);
            seedDailyQuestionsFromCSV(db);
        }
    }
}
