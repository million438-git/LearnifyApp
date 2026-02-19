package com.example.testapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AceGradeProfessional.db";
    private static final int DATABASE_VERSION = 23; 

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT, " +
                "unique_id TEXT, " + 
                "grade_or_role TEXT, " + 
                "username TEXT, " +
                "email TEXT, " +
                "password TEXT, " +
                "profile_image TEXT, " +
                "user_type TEXT, " + 
                "status TEXT DEFAULT 'Active')");

        db.execSQL("CREATE TABLE materials (mat_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, type TEXT, subject TEXT, grade TEXT, file_path TEXT)");
        db.execSQL("CREATE TABLE tasks (task_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, subject TEXT, grade TEXT, due_date TEXT, due_time TEXT, priority TEXT, status TEXT DEFAULT 'Pending')");
        db.execSQL("CREATE TABLE schedule (id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, teacher TEXT, room TEXT, time TEXT, duration TEXT, day TEXT, grade TEXT)");
        db.execSQL("CREATE TABLE announcements (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, message TEXT, date TEXT, type TEXT)");
        
        db.execSQL("CREATE TABLE payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "transaction_id TEXT, " +
                "amount TEXT, " +
                "screenshot_uri TEXT, " + 
                "status TEXT DEFAULT 'Pending', " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE system_settings (id INTEGER PRIMARY KEY, account_details TEXT)");
        
        seedAllData(db);
    }

    private void seedAllData(SQLiteDatabase db) {
        ContentValues setting = new ContentValues();
        setting.put("id", 1);
        setting.put("account_details", "🏦 CBE: 1000591245407 (Million Teshome)\n📱 Telebirr: 0902399206 (Million Teshome)");
        db.insert("system_settings", null, setting);

        ContentValues v = new ContentValues();
        v.put("full_name", "System Admin");
        v.put("username", "admin");
        v.put("password", "admin123");
        v.put("user_type", "ADMIN");
        v.put("grade_or_role", "Super Admin");
        db.insert("users", null, v);
    }

    // --- TASK LOGIC (FIXED) ---
    public boolean insertTask(String title, String desc, String sub, String grade, String date, String time, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("description", desc);
        v.put("subject", sub);
        v.put("grade", grade);
        v.put("due_date", date);
        v.put("due_time", time);
        v.put("priority", priority);
        v.put("status", "Pending");
        return db.insert("tasks", null, v) != -1;
    }

    public List<TaskModel> getTasksForGrade(String grade) {
        List<TaskModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tasks WHERE grade = ? ORDER BY task_id DESC", new String[]{grade});
        if (c.moveToFirst()) {
            do {
                TaskModel t = new TaskModel();
                t.setId(c.getInt(0)); 
                t.setTitle(c.getString(1)); 
                t.setDescription(c.getString(2));
                t.setSubject(c.getString(3)); 
                t.setGrade(c.getString(4)); 
                t.setDueDate(c.getString(5));
                t.setDueTime(c.getString(6)); 
                t.setPriority(c.getString(7)); 
                t.setStatus(c.getString(8));
                list.add(t);
            } while (c.moveToNext());
        }
        c.close(); 
        return list;
    }

    public void updateTaskStatus(int taskId, String status) {
        ContentValues v = new ContentValues(); 
        v.put("status", status);
        this.getWritableDatabase().update("tasks", v, "task_id = ?", new String[]{String.valueOf(taskId)});
    }

    public void deleteTask(int taskId) {
        this.getWritableDatabase().delete("tasks", "task_id = ?", new String[]{String.valueOf(taskId)});
    }

    // --- RECOVERY & LOGIN LOGIC ---
    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE LOWER(email)=LOWER(?)", new String[]{email});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public boolean checkUser(String user, String pass, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE (username=? OR email=?) AND password=? AND user_type=? AND status='Active'", 
                new String[]{user, user, pass, type});
        boolean exists = c.getCount() > 0; 
        c.close(); 
        return exists;
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        return db.update("users", values, "username = ?", new String[]{username}) > 0;
    }

    public boolean updateUserProfile(String username, String fullName, String email, String grade, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("full_name", fullName);
        v.put("email", email);
        v.put("grade_or_role", grade);
        if (imageUri != null && !imageUri.isEmpty()) {
            v.put("profile_image", imageUri);
        }
        return db.update("users", v, "username = ?", new String[]{username}) > 0;
    }

    public String getUserProfileImage(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT profile_image FROM users WHERE username = ?", new String[]{username});
        if (c.moveToFirst()) { 
            String img = c.getString(0); 
            c.close(); 
            return img; 
        }
        c.close(); 
        return null;
    }

    public Cursor getUserDetails(String username) {
        return this.getReadableDatabase().rawQuery("SELECT full_name, email, profile_image, grade_or_role FROM users WHERE username = ?", new String[]{username});
    }

    public String getUserGrade(String username) {
        Cursor c = this.getReadableDatabase().rawQuery("SELECT grade_or_role FROM users WHERE username = ? AND user_type='STUDENT'", new String[]{username});
        if (c.moveToFirst()) { String g = c.getString(0); c.close(); return g; }
        c.close(); return "10";
    }

    // --- ACADEMIC CONTENT LOGIC ---
    public boolean insertMaterial(String title, String content, String type, String subject, String grade, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", title); 
        v.put("content", content); 
        v.put("type", type);
        v.put("subject", subject); 
        v.put("grade", grade); 
        v.put("file_path", path);
        return db.insert("materials", null, v) != -1;
    }

    public List<MaterialModel> getMaterialsForGrade(String grade) {
        List<MaterialModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM materials WHERE grade = ? ORDER BY mat_id DESC", new String[]{grade});
        if (c.moveToFirst()) {
            do {
                MaterialModel m = new MaterialModel();
                m.setId(c.getInt(0)); m.setTitle(c.getString(1)); m.setContent(c.getString(2));
                m.setType(c.getString(3)); m.setSubject(c.getString(4)); m.setGrade(c.getString(5));
                m.setFilePath(c.getString(6)); list.add(m);
            } while (c.moveToNext());
        }
        c.close(); return list;
    }

    public List<AnnouncementModel> getAnnouncements() {
        List<AnnouncementModel> list = new ArrayList<>();
        Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM announcements ORDER BY id DESC", null);
        if (c.moveToFirst()) {
            do {
                AnnouncementModel a = new AnnouncementModel();
                a.setTitle(c.getString(1)); a.setMessage(c.getString(2)); a.setDate(c.getString(3)); a.setType(c.getString(4));
                list.add(a);
            } while (c.moveToNext());
        }
        c.close(); return list;
    }

    // --- SCHEDULE LOGIC ---
    public boolean insertClassCustom(String sub, String tea, String room, String time, String dur, String day, String grade) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("subject", sub); v.put("teacher", tea); v.put("room", room);
        v.put("time", time); v.put("duration", dur); v.put("day", day); v.put("grade", grade);
        return db.insert("schedule", null, v) != -1;
    }

    public List<ScheduleModel> getScheduleForGrade(String grade, String day) {
        List<ScheduleModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM schedule WHERE grade = ? AND day = ?", new String[]{grade, day});
        if (c.moveToFirst()) {
            do {
                ScheduleModel s = new ScheduleModel();
                s.setSubject(c.getString(1)); s.setTeacher(c.getString(2)); s.setRoom(c.getString(3));
                s.setTime(c.getString(4)); s.setDuration(c.getString(5)); list.add(s);
            } while (c.moveToNext());
        }
        c.close(); return list;
    }

    // --- PAYMENT LOGIC ---
    public boolean submitPayment(String user, String txId, String amount, String imgUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", user);
        v.put("transaction_id", txId);
        v.put("amount", amount);
        v.put("screenshot_uri", imgUri);
        v.put("status", "Pending");
        return db.insert("payments", null, v) != -1;
    }

    public boolean checkPaymentStatus(String user) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM payments WHERE username=? AND status='Approved'", new String[]{user});
        boolean isPaid = c.getCount() > 0;
        c.close(); return isPaid;
    }

    public Cursor getAllPayments() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM payments ORDER BY id DESC", null);
    }

    public void updatePaymentStatus(int id, String status) {
        ContentValues v = new ContentValues(); v.put("status", status);
        this.getWritableDatabase().update("payments", v, "id = ?", new String[]{String.valueOf(id)});
    }

    public String getAdminAccountDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT account_details FROM system_settings WHERE id=1", null);
        if (c.moveToFirst()) { String d = c.getString(0); c.close(); return d; }
        c.close(); return "Contact Admin: 0902399206";
    }

    public boolean registerUser(String name, String uid, String gradeRole, String user, String email, String pass, String imageUri, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("full_name", name); v.put("unique_id", uid); v.put("grade_or_role", gradeRole);
        v.put("username", user); v.put("email", email); v.put("password", pass);
        v.put("profile_image", imageUri); v.put("user_type", type);
        return db.insert("users", null, v) != -1;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS materials");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        db.execSQL("DROP TABLE IF EXISTS schedule");
        db.execSQL("DROP TABLE IF EXISTS announcements");
        db.execSQL("DROP TABLE IF EXISTS payments");
        db.execSQL("DROP TABLE IF EXISTS system_settings");
        onCreate(db);
    }
}
