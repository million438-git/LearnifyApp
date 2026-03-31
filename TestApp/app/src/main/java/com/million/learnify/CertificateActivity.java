package com.million.learnify;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CertificateActivity extends AppCompatActivity {

    private TextView tvStudentName, tvSubjectMastered, tvCertificateDate, tvStudentID, tvCertificateID;
    private TextView tvCertTitle, tvPresentationText, tvBodyText, tvDirectorName, tvDirectorRole;
    private View certificateView;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);

        dbHelper = new DatabaseHelper(this);
        currentUsername = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", "");

        initViews();
        setupToolbar();
        loadCertificateData();

        findViewById(R.id.btnShare).setOnClickListener(v -> shareCertificate());
    }

    private void initViews() {
        tvCertTitle = findViewById(R.id.tvCertTitle);
        tvPresentationText = findViewById(R.id.tvPresentationText);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvBodyText = findViewById(R.id.tvBodyText);
        tvSubjectMastered = findViewById(R.id.tvSubjectMastered);
        tvCertificateDate = findViewById(R.id.tvCertificateDate);
        tvStudentID = findViewById(R.id.tvStudentID);
        tvCertificateID = findViewById(R.id.tvCertificateID);
        tvDirectorName = findViewById(R.id.tvDirectorName);
        tvDirectorRole = findViewById(R.id.tvDirectorRole);
        certificateView = findViewById(R.id.certificateView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.certificateToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCertificateData() {
        // First, check if there is a FULLY CUSTOM certificate for this user
        Cursor cursor = dbHelper.getCustomCertificate(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            tvCertTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow("cert_title")));
            tvPresentationText.setText(cursor.getString(cursor.getColumnIndexOrThrow("presentation_text")));
            tvStudentName.setText(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
            tvBodyText.setText(cursor.getString(cursor.getColumnIndexOrThrow("body_text")));
            tvSubjectMastered.setText(cursor.getString(cursor.getColumnIndexOrThrow("course_name")));
            tvCertificateDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("issue_date")));
            tvStudentID.setText(cursor.getString(cursor.getColumnIndexOrThrow("student_id")));
            tvCertificateID.setText(cursor.getString(cursor.getColumnIndexOrThrow("certificate_id")));
            tvDirectorName.setText(cursor.getString(cursor.getColumnIndexOrThrow("director_name")));
            tvDirectorRole.setText(cursor.getString(cursor.getColumnIndexOrThrow("director_role")));
            cursor.close();
            return;
        }

        // Fallback to standard data if no custom certificate issued yet
        String studentFullName = getSharedPreferences("UserSession", MODE_PRIVATE).getString("full_name", "MILLION TESHOME");
        tvStudentName.setText(studentFullName.toUpperCase());
        tvStudentID.setText("ID-" + (int)(Math.random() * 90000 + 10000));
        tvCertificateID.setText("LNYF-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tvCertificateDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
    }

    private void shareCertificate() {
        certificateView.post(() -> {
            Bitmap bitmap = createBitmapFromView(certificateView);
            if (bitmap != null) {
                saveAndShare(bitmap);
            }
        });
    }

    private Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void saveAndShare(Bitmap bitmap) {
        String fileName = "Learnify_Official_Certificate_" + System.currentTimeMillis() + ".png";
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (imageUri != null) {
                    fos = getContentResolver().openOutputStream(imageUri);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    if (fos != null) fos.close();

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/png");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Share Professional Certificate"));
                }
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                java.io.File image = new java.io.File(imagesDir, fileName);
                fos = new java.io.FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();

                Uri uri = Uri.fromFile(image);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(shareIntent, "Share Professional Certificate"));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
