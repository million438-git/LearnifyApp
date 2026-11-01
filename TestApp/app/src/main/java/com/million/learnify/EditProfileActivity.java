package com.million.learnify;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * EditProfileActivity - Modern, interactive profile management.
 * Fully active with database integration, dynamic curriculum subjects, and haptic feedback.
 */
public class EditProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputEditText etFullName, etUsername, etEmail, etPhone, etUniversity, etYear, etMajor;
    private TextView tvDisplayFullName, tvDisplayUsername, tvXPValue, tvReminderTime, tvCurrentLanguage;
    private TextView tvVisibility, tvLeaderboard, tvNotifications;
    private ImageView ivEditProfilePic;
    private Slider sliderXP;
    private ChipGroup chipGroupSubjects;
    private View btnAddSubject;
    private DatabaseHelper dbHelper;
    private SettingsService settingsService;
    private StudentSettings currentSettings;
    private String currentUsername;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper = new DatabaseHelper(this);
        settingsService = SettingsService.getInstance(this);
        currentSettings = settingsService.loadSettings();

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        setupToolbar();
        initViews();
        loadCurrentData();
        setupInteractions();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.editProfileToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etUniversity = findViewById(R.id.etUniversity);
        etYear = findViewById(R.id.etYear);
        etMajor = findViewById(R.id.etMajor);

        tvDisplayFullName = findViewById(R.id.tvDisplayFullName);
        tvDisplayUsername = findViewById(R.id.tvDisplayUsername);
        tvXPValue = findViewById(R.id.tvXPValue);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
        
        tvVisibility = findViewById(R.id.tvVisibility);
        tvLeaderboard = findViewById(R.id.tvLeaderboard);
        tvNotifications = findViewById(R.id.tvNotifications);

        ivEditProfilePic = findViewById(R.id.ivEditProfilePic);
        sliderXP = findViewById(R.id.sliderXP);
        chipGroupSubjects = findViewById(R.id.chipGroupSubjects);
        btnAddSubject = findViewById(R.id.btnAddSubject);
    }

    private void loadCurrentData() {
        Cursor cursor = dbHelper.getUserDetails(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            etFullName.setText(fullName);
            tvDisplayFullName.setText(fullName);
            
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            etEmail.setText(email);
            
            etUsername.setText(currentUsername);
            tvDisplayUsername.setText("@" + currentUsername);
            
            String imgUriStr = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));
            if (imgUriStr != null && !imgUriStr.isEmpty()) {
                try {
                    ivEditProfilePic.setImageURI(Uri.parse(imgUriStr));
                } catch (Exception e) {
                    ivEditProfilePic.setImageResource(R.drawable.ic_user_person);
                }
            }

            // Load extended fields
            etPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            etUniversity.setText(cursor.getString(cursor.getColumnIndexOrThrow("university")));
            etYear.setText(cursor.getString(cursor.getColumnIndexOrThrow("year")));
            etMajor.setText(cursor.getString(cursor.getColumnIndexOrThrow("major")));
            
            int xpGoal = cursor.getInt(cursor.getColumnIndexOrThrow("xp_goal"));
            sliderXP.setValue(xpGoal > 0 ? xpGoal : 500);
            tvXPValue.setText(((int) sliderXP.getValue()) + " XP");

            tvVisibility.setText(cursor.getString(cursor.getColumnIndexOrThrow("visibility")) + " ▼");
            tvLeaderboard.setText((cursor.getInt(cursor.getColumnIndexOrThrow("show_leaderboard")) == 1 ? "Yes" : "No") + " ▼");
            tvNotifications.setText((cursor.getInt(cursor.getColumnIndexOrThrow("email_notifications")) == 1 ? "On" : "Off") + " ▼");

            String subjectsCsv = cursor.getString(cursor.getColumnIndexOrThrow("subjects"));
            populateSubjectChips(subjectsCsv);

            cursor.close();
        } else {
            populateSubjectChips("Mathematics,Physics");
        }

        tvReminderTime.setText("⏰ " + formatTo12Hour(currentSettings.getReminderTime()));
        tvCurrentLanguage.setText(getLanguageName(currentSettings.getLanguage()));
    }

    private void populateSubjectChips(String subjectsCsv) {
        if (chipGroupSubjects == null) return;

        // Clear dynamic chips, keeping btnAddSubject
        for (int i = chipGroupSubjects.getChildCount() - 1; i >= 0; i--) {
            View child = chipGroupSubjects.getChildAt(i);
            if (child != btnAddSubject) {
                chipGroupSubjects.removeViewAt(i);
            }
        }

        if (subjectsCsv == null || subjectsCsv.trim().isEmpty()) {
            subjectsCsv = "Mathematics,Physics";
        }

        String[] parts = subjectsCsv.split(",");
        for (String subject : parts) {
            String trimmed = subject.trim();
            if (!trimmed.isEmpty()) {
                addSubjectChip(trimmed);
            }
        }
    }

    private void addSubjectChip(String subjectName) {
        if (chipGroupSubjects == null || subjectName == null || subjectName.trim().isEmpty()) return;

        Chip chip = new Chip(this);
        chip.setText(subjectName.trim());
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setClickable(false);
        chip.setChipBackgroundColorResource(R.color.sapphire_subtle);
        chip.setChipStrokeColorResource(R.color.sapphire_primary_light);
        chip.setChipStrokeWidth(getResources().getDisplayMetrics().density);
        chip.setTextColor(ContextCompat.getColor(this, R.color.text_primary_slate));
        chip.setCloseIconTintResource(R.color.text_secondary_slate);

        chip.setOnCloseIconClickListener(v -> {
            chipGroupSubjects.removeView(chip);
            AppFeedbackManager.getInstance(this).performTap(chip);
        });

        int addIndex = -1;
        if (btnAddSubject != null) {
            addIndex = chipGroupSubjects.indexOfChild(btnAddSubject);
        }

        if (addIndex >= 0) {
            chipGroupSubjects.addView(chip, addIndex);
        } else {
            chipGroupSubjects.addView(chip);
        }
    }

    private List<String> getSelectedSubjectsList() {
        List<String> list = new ArrayList<>();
        if (chipGroupSubjects == null) return list;
        for (int i = 0; i < chipGroupSubjects.getChildCount(); i++) {
            View child = chipGroupSubjects.getChildAt(i);
            if (child instanceof Chip && child != btnAddSubject) {
                CharSequence text = ((Chip) child).getText();
                if (text != null && !text.toString().trim().isEmpty()) {
                    list.add(text.toString().trim());
                }
            }
        }
        return list;
    }

    private void showAddSubjectDialog() {
        String[] standardSubjects = {
            "Mathematics", "Physics", "Chemistry", "Biology", 
            "History", "Geography", "English", "Civics", 
            "Economics", "Computer Science", "Information Technology"
        };

        List<String> existing = getSelectedSubjectsList();
        List<String> available = new ArrayList<>();
        for (String s : standardSubjects) {
            if (!existing.contains(s)) {
                available.add(s);
            }
        }

        if (available.isEmpty()) {
            Toast.makeText(this, "All available curriculum subjects are already selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] availableArray = available.toArray(new String[0]);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Curriculum Subject")
                .setItems(availableArray, (dialog, which) -> {
                    String chosen = availableArray[which];
                    addSubjectChip(chosen);
                    AppFeedbackManager.getInstance(this).performTap(chipGroupSubjects);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getLanguageName(String code) {
        if (code == null) return "English";
        switch (code) {
            case "am": return "Amharic (አማርኛ)";
            case "om": return "Afaan Oromoo";
            case "ti": return "Tigrinya (ትግርኛ)";
            case "es": return "Spanish (Español)";
            case "fr": return "French (Français)";
            case "ar": return "Arabic (العربية)";
            default: return "English";
        }
    }

    private String formatTo12Hour(String time24) {
        try {
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String suffix = (hour >= 12) ? "PM" : "AM";
            int hour12 = (hour == 0) ? 12 : (hour > 12 ? hour - 12 : hour);
            return String.format(Locale.US, "%02d:%02d %s", hour12, minute, suffix);
        } catch (Exception e) { return time24; }
    }

    private void setupInteractions() {
        View btnSaveTop = findViewById(R.id.btnSaveTop);
        if (btnSaveTop != null) {
            btnSaveTop.setOnClickListener(v -> handleSave());
        }

        View btnChangePic = findViewById(R.id.btnChangePic);
        if (btnChangePic != null) {
            btnChangePic.setOnClickListener(v -> openGallery());
        }
        
        if (sliderXP != null) {
            sliderXP.addOnChangeListener((slider, value, fromUser) -> {
                tvXPValue.setText(((int) value) + " XP");
            });
        }

        View llReminder = findViewById(R.id.llReminderTime);
        if (llReminder != null) {
            llReminder.setOnClickListener(v -> showTimePicker());
        }

        View llChangeLang = findViewById(R.id.llChangeLanguage);
        if (llChangeLang != null) {
            llChangeLang.setOnClickListener(v -> showLanguagePicker());
        }

        View btnLangTop = findViewById(R.id.btnLanguageTop);
        if (btnLangTop != null) {
            btnLangTop.setOnClickListener(v -> showLanguagePicker());
        }
        
        View llChangePass = findViewById(R.id.llChangePassword);
        if (llChangePass != null) {
            llChangePass.setOnClickListener(v -> {
                startActivity(new Intent(this, PrivacySecurityActivity.class));
            });
        }

        if (tvVisibility != null) {
            tvVisibility.setOnClickListener(v -> showOptionPicker("Profile Visibility", new String[]{"Public", "Private", "Friends Only"}, tvVisibility));
        }

        if (tvLeaderboard != null) {
            tvLeaderboard.setOnClickListener(v -> showOptionPicker("Show on Leaderboard", new String[]{"Yes", "No"}, tvLeaderboard));
        }

        if (tvNotifications != null) {
            tvNotifications.setOnClickListener(v -> showOptionPicker("Email Notifications", new String[]{"On", "Off"}, tvNotifications));
        }

        View btnDeactivate = findViewById(R.id.btnDeactivate);
        if (btnDeactivate != null) {
            btnDeactivate.setOnClickListener(v -> showDeactivationWarning());
        }
        
        if (btnAddSubject != null) {
            btnAddSubject.setOnClickListener(v -> showAddSubjectDialog());
        }
    }

    private void showOptionPicker(String title, String[] options, TextView target) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setItems(options, (dialog, which) -> {
                    target.setText(options[which] + " ▼");
                    AppFeedbackManager.getInstance(this).performTap(target);
                }).show();
    }

    private void showTimePicker() {
        String time = currentSettings.getReminderTime();
        if (time == null || !time.contains(":")) time = "08:00";
        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String selectedTime24 = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
            tvReminderTime.setText("⏰ " + formatTo12Hour(selectedTime24));
            currentSettings.setReminderTime(selectedTime24);
            settingsService.saveSettings(currentSettings);
            AppFeedbackManager.getInstance(this).performSuccess(tvReminderTime);
        }, hour, minute, false).show();
    }

    private void showLanguagePicker() {
        String[] languages = {"English", "Español", "Français", "العربية", "አማርኛ", "Afaan Oromoo", "ትግርኛ"};
        String[] codes = {"en", "es", "fr", "ar", "am", "om", "ti"};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select App Language")
                .setItems(languages, (dialog, which) -> {
                    String chosen = codes[which];
                    currentSettings.setLanguage(chosen);
                    settingsService.saveSettings(currentSettings);
                    LocaleHelper.setLocale(this, chosen);
                    tvCurrentLanguage.setText(getLanguageName(chosen));
                    AppFeedbackManager.getInstance(this).performSuccess(tvCurrentLanguage);
                    Toast.makeText(this, "Language changed to " + languages[which], Toast.LENGTH_SHORT).show();
                    recreate();
                }).show();
    }

    private void showDeactivationWarning() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Danger Zone")
                .setMessage("Are you sure you want to deactivate your account? This action can be reversed by logging in again within 30 days.")
                .setPositiveButton("Deactivate", (d, w) -> {
                    Toast.makeText(this, "Account deactivated. Logging out...", Toast.LENGTH_LONG).show();
                    FirebaseSessionManager.signOut(this, FirebaseAuthSupport.getConfiguredAuth(this));
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ivEditProfilePic.setImageURI(selectedImageUri);
                AppFeedbackManager.getInstance(this).performSuccess(ivEditProfilePic);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void handleSave() {
        View btnSave = findViewById(R.id.btnSaveTop);
        String name = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String university = etUniversity.getText() != null ? etUniversity.getText().toString().trim() : "";
        String year = etYear.getText() != null ? etYear.getText().toString().trim() : "";
        String major = etMajor.getText() != null ? etMajor.getText().toString().trim() : "";
        int xpGoal = (int) sliderXP.getValue();
        
        String visibility = tvVisibility.getText().toString().replace(" ▼", "").trim();
        int showLeaderboard = tvLeaderboard.getText().toString().contains("Yes") ? 1 : 0;
        int emailNotifications = tvNotifications.getText().toString().contains("On") ? 1 : 0;
        
        String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (name.isEmpty()) {
            if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
            etFullName.setError("Name is required");
            etFullName.requestFocus();
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> subjectList = getSelectedSubjectsList();
        String subjectsCsv = subjectList.isEmpty() ? "Mathematics,Physics" : TextUtils.join(",", subjectList);

        boolean success = dbHelper.updateUserProfileFull(currentUsername, name, email, phone, university, year, major, xpGoal, subjectsCsv, visibility, showLeaderboard, emailNotifications, imageUriStr);
        
        if (success) {
            if (btnSave != null) AppFeedbackManager.getInstance(this).performSuccess(btnSave);
            Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (btnSave != null) AppFeedbackManager.getInstance(this).performError(btnSave);
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }
}
