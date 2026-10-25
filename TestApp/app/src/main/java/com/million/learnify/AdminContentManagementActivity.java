package com.million.learnify;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminContentManagementActivity - Advanced Hub for Central Asset Management.
 * Refactored to use DataRepository for Client-Server readiness.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class AdminContentManagementActivity extends BaseActivity {

    private TextInputEditText etMaterialTitle, etMaterialDescription;
    private TextInputEditText etAnnTitle, etAnnMessage;
    private AutoCompleteTextView actvSubject, actvGrade, actvType, actvDifficulty;
    private MaterialButton btnSelectFile, btnUploadConfirm, btnSendAnnouncement;
    private TextView tvSelectedFileName;
    private LinearLayout llRecentContent;
    private String selectedFilePath = "";

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) { e.printStackTrace(); }
                        selectedFilePath = uri.toString();
                        tvSelectedFileName.setText("Media Asset: " + getFileName(uri));
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_content_management);

        initViews();
        setupToolbar();
        setupDropdowns();
        refreshRecentContent();
    }

    private void initViews() {
        etMaterialTitle = findViewById(R.id.etMaterialTitle);
        etMaterialDescription = findViewById(R.id.etMaterialDescription);
        actvSubject = findViewById(R.id.actvSubject);
        actvGrade = findViewById(R.id.actvGrade);
        actvType = findViewById(R.id.actvType);
        actvDifficulty = findViewById(R.id.actvDifficulty);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnUploadConfirm = findViewById(R.id.btnUploadConfirm);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        llRecentContent = findViewById(R.id.llRecentContent);

        etAnnTitle = findViewById(R.id.etAnnTitle);
        etAnnMessage = findViewById(R.id.etAnnMessage);
        btnSendAnnouncement = findViewById(R.id.btnSendAnnouncement);

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            filePickerLauncher.launch(intent);
        });

        btnUploadConfirm.setOnClickListener(v -> handleUpload());
        btnSendAnnouncement.setOnClickListener(v -> handleSendAnnouncement());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.contentMgmtToolbar);
        setupPortalToolbar(toolbar, "Content & Media Hub");
    }

    private void setupDropdowns() {
        String[] types = {"PDF Document", "Video Lesson", "Study Note", "Image Reference", "SCORM Package"};
        actvType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types));
        actvType.setOnClickListener(v -> actvType.showDropDown());

        String[] subjects = {"Mathematics", "English", "Physics", "Biology", "Chemistry", "History", "Geography", "ICT"};
        actvSubject.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects));
        actvSubject.setOnClickListener(v -> actvSubject.showDropDown());

        String[] grades = {"9", "10", "11", "12", "University"};
        actvGrade.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, grades));
        actvGrade.setOnClickListener(v -> actvGrade.showDropDown());

        String[] levels = {"Beginner", "Intermediate", "Advanced", "Professional"};
        actvDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels));
        actvDifficulty.setOnClickListener(v -> actvDifficulty.showDropDown());
    }

    private void handleUpload() {
        String title = etMaterialTitle.getText().toString().trim();
        String desc = etMaterialDescription.getText().toString().trim();
        String subject = actvSubject.getText().toString().trim();
        String grade = actvGrade.getText().toString().trim();
        String type = actvType.getText().toString().trim();
        String difficulty = actvDifficulty.getText().toString().trim();

        if (title.isEmpty() || grade.isEmpty() || selectedFilePath.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Validation Failed: Required fields missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use repository for insertion
        boolean success = repository.insertMaterial(title, desc, type, subject, grade, selectedFilePath, difficulty);
        
        if (success) {
            String announcementMsg = "New " + type + " published: " + title + " (" + subject + " - Grade " + grade + ")";
            repository.adminPostAnnouncement("New Content Alert", announcementMsg, "COURSE_UPDATE", grade);
            repository.addNotification("all", "New Material: " + title, desc, "CONTENT");

            // Trigger real-time system notification
            NotificationHelper.showInstantNotification(this, "CONTENT", "New Material: " + title, desc);

            Toast.makeText(this, "Knowledge Broadcast Successful.", Toast.LENGTH_LONG).show();
            clearMaterialForm();
            refreshRecentContent();
        } else {
            Toast.makeText(this, "Publishing Error: Database integrity fault.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSendAnnouncement() {
        String title = etAnnTitle.getText().toString().trim();
        String message = etAnnMessage.getText().toString().trim();

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in title and message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use repository for announcement broadcast
        repository.adminPostAnnouncement(title, message, "SYSTEM_ALERT", "All");
        repository.addNotification("all", title, message, "ANNOUNCEMENT");
        
        // Trigger real-time system notification
        NotificationHelper.showInstantNotification(this, "ANNOUNCEMENT", title, message);

        Toast.makeText(this, "System Broadcast Dispatched.", Toast.LENGTH_LONG).show();
        etAnnTitle.setText("");
        etAnnMessage.setText("");
    }

    private void clearMaterialForm() {
        etMaterialTitle.setText("");
        etMaterialDescription.setText("");
        tvSelectedFileName.setText("No resource attached");
        selectedFilePath = "";
        actvSubject.setText("", false);
        actvGrade.setText("", false);
        actvType.setText("", false);
        actvDifficulty.setText("", false);
    }

    private void refreshRecentContent() {
        if (llRecentContent == null) return;
        llRecentContent.removeAllViews();
        try (Cursor cursor = repository.getAllMaterials()) {
            int count = 0;
            while (cursor.moveToNext() && count < 5) {
                View v = LayoutInflater.from(this).inflate(R.layout.item_material, llRecentContent, false);
                TextView title = v.findViewById(R.id.tvMaterialTitle);
                TextView desc = v.findViewById(R.id.tvMaterialDescription);
                TextView subject = v.findViewById(R.id.tvMaterialSubject);
                TextView type = v.findViewById(R.id.tvMaterialType);
                
                title.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                desc.setText(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                subject.setText(cursor.getString(cursor.getColumnIndexOrThrow("subject")));
                type.setText(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                
                title.setTextColor(ContextCompat.getColor(this, R.color.admin_text_primary));
                desc.setTextColor(ContextCompat.getColor(this, R.color.admin_text_secondary));

                final String filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
                View btnView = v.findViewById(R.id.btnViewMaterial);
                if (btnView != null) {
                    btnView.setOnClickListener(clickV -> {
                        if (filePath != null && !filePath.isEmpty()) {
                            try {
                                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                                viewIntent.setDataAndType(Uri.parse(filePath), "*/*");
                                viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(viewIntent, "Open Resource"));
                            } catch (Exception ex) {
                                Toast.makeText(this, "Opening file: " + filePath, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "No file attachment found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                llRecentContent.addView(v);
                count++;
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }
}
