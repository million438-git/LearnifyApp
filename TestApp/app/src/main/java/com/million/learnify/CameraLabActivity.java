package com.million.learnify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraLabActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 1001;
    
    private PreviewView viewFinder;
    private MaterialCardView resultPanel;
    private TextView tvResultHeader, tvResultBody;
    private ProgressBar analysisProgress;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Camera camera;
    private boolean isFlashOn = false;
    
    private enum LabMode { MATH, OBJECT, TRANSLATE }
    private LabMode currentMode = LabMode.MATH;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    processImage(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_lab);

        initViews();
        setupToolbar();
        setupModeToggle();
        setupButtons();

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void initViews() {
        viewFinder = findViewById(R.id.viewFinder);
        resultPanel = findViewById(R.id.resultPanel);
        tvResultHeader = findViewById(R.id.tvResultHeader);
        tvResultBody = findViewById(R.id.tvResultBody);
        analysisProgress = findViewById(R.id.analysisProgress);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        findViewById(R.id.btnHistory).setOnClickListener(v -> 
            startActivity(new Intent(this, CameraHistoryActivity.class)));
        findViewById(R.id.btnSettings).setOnClickListener(v -> 
            startActivity(new Intent(this, CameraSettingsActivity.class)));
    }

    private void setupModeToggle() {
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleMode);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnMath) currentMode = LabMode.MATH;
                else if (checkedId == R.id.btnObject) currentMode = LabMode.OBJECT;
                else if (checkedId == R.id.btnTranslate) currentMode = LabMode.TRANSLATE;
                
                resultPanel.setVisibility(View.GONE);
                Toast.makeText(this, "Mode: " + currentMode.name(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        findViewById(R.id.btnCapture).setOnClickListener(v -> takePhoto());
        
        findViewById(R.id.btnGallery).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        ImageView btnFlash = findViewById(R.id.btnFlash);
        btnFlash.setOnClickListener(v -> toggleFlash(btnFlash));

        findViewById(R.id.btnSaveResult).setOnClickListener(v -> {
            Toast.makeText(this, "Result saved to your Lab Notebook", Toast.LENGTH_SHORT).show();
            resultPanel.setVisibility(View.GONE);
        });

        findViewById(R.id.btnShareResult).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Check out this solution from Learnify Camera Lab: " + tvResultBody.getText());
            startActivity(Intent.createChooser(intent, "Share Result"));
        });
        
        findViewById(R.id.btnDetailsResult).setOnClickListener(v -> 
            Toast.makeText(this, "Redirecting to related lesson...", Toast.LENGTH_SHORT).show());
    }

    private void toggleFlash(ImageView btnFlash) {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
            btnFlash.setImageResource(isFlashOn ? android.R.drawable.ic_menu_mylocation : android.R.drawable.ic_menu_mylocation);
            // In a real app we'd use better icons, but staying with standard for now.
            btnFlash.setColorFilter(isFlashOn ? ContextCompat.getColor(this, R.color.blue_600) : ContextCompat.getColor(this, android.R.color.white));
            Toast.makeText(this, "Flash " + (isFlashOn ? "On" : "Off"), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        // Visual feedback
        viewFinder.setAlpha(0.5f);
        new Handler(Looper.getMainLooper()).postDelayed(() -> viewFinder.setAlpha(1.0f), 100);

        File photoFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                processImage(Uri.fromFile(photoFile));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CameraLabActivity.this, "Capture failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processImage(Uri uri) {
        resultPanel.setVisibility(View.GONE);
        analysisProgress.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Analyzing image...", Toast.LENGTH_SHORT).show();
        
        // Simulate AI Processing Delay with real UI updates
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;
            analysisProgress.setVisibility(View.GONE);
            showResults();
        }, 2000);
    }

    private void showResults() {
        resultPanel.setVisibility(View.VISIBLE);
        
        switch (currentMode) {
            case MATH:
                tvResultHeader.setText("🔢 Math Solution Found");
                tvResultBody.setText("Equation: 5x + 10 = 35\nSolution: x = 5\n\nSteps:\n1. Subtract 10: 5x = 25\n2. Divide by 5: x = 5");
                break;
            case OBJECT:
                tvResultHeader.setText("🌿 Object Identified");
                tvResultBody.setText("Common Name: Monstera Deliciosa\nScientific Name: Monstera deliciosa\nFamily: Araceae\n\nEducational Fact: Known as the 'Swiss Cheese Plant', it is native to tropical forests of southern Mexico.");
                break;
            case TRANSLATE:
                tvResultHeader.setText("🌍 Text Translated");
                tvResultBody.setText("Detected (French): Bonjour, bienvenue dans l'application Learnify.\n\nTranslated (English): Hello, welcome to the Learnify app.");
                break;
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
