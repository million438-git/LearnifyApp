package com.million.learnify;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminContentManagementActivity - Handles uploading diverse educational materials.
 * Refined for perfect dropdown functionality and high-tier nature visuals.
 */
public class AdminContentManagementActivity extends AppCompatActivity {

    private TextInputEditText etMaterialTitle, etMaterialDescription;
    private AutoCompleteTextView actvSubject, actvGrade, actvType, actvDifficulty;
    private MaterialButton btnSelectFile, btnUploadConfirm;
    private TextView tvSelectedFileName;
    private DatabaseHelper dbHelper;
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
                        tvSelectedFileName.setText("📁 Ready: " + getFileName(uri));
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_content_management);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
        setupDropdowns();

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            filePickerLauncher.launch(intent);
        });

        btnUploadConfirm.setOnClickListener(v -> handleUpload());
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
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.contentMgmtToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // 1. MATERIAL TYPE DROPDOWN
        String[] types = {"PDF Document", "Video Lesson", "Study Note", "Image Reference"};
        actvType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types));
        actvType.setOnClickListener(v -> actvType.showDropDown());

        // 2. SUBJECT DROPDOWN
        String[] subjects = {"Mathematics", "English", "Physics", "Biology", "Chemistry", "History", "Geography"};
        actvSubject.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects));
        actvSubject.setOnClickListener(v -> actvSubject.showDropDown());

        // 3. GRADE DROPDOWN
        String[] grades = {"9", "10", "11", "12"};
        actvGrade.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, grades));
        actvGrade.setOnClickListener(v -> actvGrade.showDropDown());

        // 4. DIFFICULTY DROPDOWN
        String[] levels = {"Easy", "Medium", "Hard"};
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

        if (title.isEmpty() || grade.isEmpty() || selectedFilePath.isEmpty() || type.isEmpty() || difficulty.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields and attach a file", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.insertMaterial(title, desc, type, subject, grade, selectedFilePath, difficulty);
        if (success) {
            Toast.makeText(this, "🚀 Published Successfully to Students!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Publishing failed. Check data.", Toast.LENGTH_SHORT).show();
        }
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
