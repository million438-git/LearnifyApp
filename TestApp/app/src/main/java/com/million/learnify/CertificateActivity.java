package com.million.learnify;

import android.content.ContentValues;
import android.content.Intent;
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

    private TextView tvStudentName, tvSubjectMastered, tvCertificateScore, tvCertificateDate;
    private View certificateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);

        initViews();
        setupToolbar();
        loadCertificateData();

        findViewById(R.id.btnShare).setOnClickListener(v -> shareCertificate());
    }

    private void initViews() {
        tvStudentName = findViewById(R.id.tvStudentName);
        tvSubjectMastered = findViewById(R.id.tvSubjectMastered);
        tvCertificateScore = findViewById(R.id.tvCertificateScore);
        tvCertificateDate = findViewById(R.id.tvCertificateDate);
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
        String studentName = getSharedPreferences("UserSession", MODE_PRIVATE).getString("full_name", "Student");
        String subject = getIntent().getStringExtra("SUBJECT_NAME");
        String score = getIntent().getStringExtra("EXAM_SCORE");
        String date = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date());

        tvStudentName.setText(studentName);
        tvSubjectMastered.setText(subject != null ? subject.toUpperCase() : "GENERAL STUDIES");
        tvCertificateScore.setText(score != null ? score : "100%");
        tvCertificateDate.setText(date);
    }

    private void shareCertificate() {
        Bitmap bitmap = createBitmapFromView(certificateView);
        if (bitmap != null) {
            saveAndShare(bitmap);
        }
    }

    private Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void saveAndShare(Bitmap bitmap) {
        String fileName = "AceGrade_Certificate_" + System.currentTimeMillis() + ".png";
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(imageUri);
                
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                if (fos != null) fos.close();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                startActivity(Intent.createChooser(shareIntent, "Share Certificate"));
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
                startActivity(Intent.createChooser(shareIntent, "Share Certificate"));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing certificate", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
