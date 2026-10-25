package com.million.learnify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * CameraLabActivity - Real-world Smart Learning System.
 * Fully Integrated Advanced Features (Section 10) and Admin Governance (Section 11).
 */
public class CameraLabActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 1001;
    
    private PreviewView viewFinder;
    private View focusOverlay, reviewLayer;
    private ImageView ivCapturedImage;
    private EditText etExtractedText;
    private MaterialCardView resultPanel;
    private TextView tvResultHeader, tvResultBody;
    private ProgressBar analysisProgress;
    private Slider zoomSlider;
    private Chip chipPageCount;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Camera camera;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
    private boolean isFlashOn = false;
    private DatabaseHelper dbHelper;
    private String currentUsername;
    private List<String> detectedVisuals;
    private TextToSpeech tts;
    
    // Bridge Actions: Spaced-Repetition Flashcards & 3D Interactive Lab
    private MaterialButton btnCreateFlashcard;
    private MaterialButton btnOpenVisualizer;
    private boolean isFlashcardSaved = false;
    private boolean isSavingFlashcard = false;

    // 10.4 Multi-page Support
    private final List<String> accumulatedPages = new ArrayList<>();
    private static final int MAX_PAGES = 5;

    // 10.1 Multi-language solver code
    private String currentLangCode = "en";

    // Course Context
    private String currentCourseContext = "General";
    private enum LabMode { MATH, PHYSICS, CHEMISTRY, GEOMETRY }
    private LabMode currentMode = LabMode.MATH;

    private TextRecognizer recognizer;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    showReview(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_lab);

        dbHelper = new DatabaseHelper(this);
        // 11.1 Admin Global Toggle Check
        if (!dbHelper.isCameraLabEnabled()) {
            showDisabledDialog("Camera Lab is currently disabled by administrator.");
            return;
        }

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Guest");

        if (getIntent().hasExtra("COURSE_NAME")) {
            currentCourseContext = getIntent().getStringExtra("COURSE_NAME");
            autoSetModeByCourse(currentCourseContext);
        }

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        
        // 10.2 Accessibility: Voice Guidance
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                speak("Camera Lab active. Point at a problem to begin.");
            }
        });

        initViews();
        setupToolbar();
        setupModeToggle();
        setupButtons();
        setupZoomAndFocus();
        showProgressRecommendations();
        applyCameraSettings();

        if (getIntent().hasExtra("IMAGE_URI")) {
            String uriStr = getIntent().getStringExtra("IMAGE_URI");
            if (uriStr != null && !uriStr.isEmpty()) {
                showReview(Uri.parse(uriStr));
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
        checkPermissionsAndStartCamera();
    }

    private void showDisabledDialog(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Access Restricted")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }

    private void autoSetModeByCourse(String course) {
        String lower = course.toLowerCase();
        if (lower.contains("math") || lower.contains("algebra")) currentMode = LabMode.MATH;
        else if (lower.contains("physics") || lower.contains("motion")) currentMode = LabMode.PHYSICS;
        else if (lower.contains("chem")) currentMode = LabMode.CHEMISTRY;
        else if (lower.contains("geo")) currentMode = LabMode.GEOMETRY;
    }

    private void initViews() {
        viewFinder = findViewById(R.id.viewFinder);
        focusOverlay = findViewById(R.id.focusOverlay);
        reviewLayer = findViewById(R.id.reviewLayer);
        ivCapturedImage = findViewById(R.id.ivCapturedImage);
        etExtractedText = findViewById(R.id.etExtractedText);
        resultPanel = findViewById(R.id.resultPanel);
        tvResultHeader = findViewById(R.id.tvResultHeader);
        tvResultBody = findViewById(R.id.tvResultBody);
        analysisProgress = findViewById(R.id.analysisProgress);
        zoomSlider = findViewById(R.id.zoomSlider);
        chipPageCount = findViewById(R.id.chipPageCount);
        btnCreateFlashcard = findViewById(R.id.btnCreateFlashcard);
        btnOpenVisualizer = findViewById(R.id.btnOpenVisualizer);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // 10.1 Multi-language Selection
        findViewById(R.id.btnLanguage).setOnClickListener(v -> {
            String[] langs = {"English", "Amharic (አማርኛ)"};
            String[] codes = {"en", "am"};
            new AlertDialog.Builder(this)
                    .setTitle("Output Language")
                    .setItems(langs, (d, which) -> {
                        currentLangCode = codes[which];
                        Toast.makeText(this, "Solving in " + langs[which], Toast.LENGTH_SHORT).show();
                    }).show();
        });

        findViewById(R.id.btnHistory).setOnClickListener(v -> 
            startActivity(new Intent(this, CameraHistoryActivity.class)));
        findViewById(R.id.btnSettings).setOnClickListener(v -> 
            startActivity(new Intent(this, CameraSettingsActivity.class)));
    }

    private void setupModeToggle() {
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleMode);
        
        if (currentMode == LabMode.MATH) toggleGroup.check(R.id.btnMath);
        else if (currentMode == LabMode.PHYSICS) toggleGroup.check(R.id.btnPhysics);
        else if (currentMode == LabMode.CHEMISTRY) toggleGroup.check(R.id.btnChemistry);
        else if (currentMode == LabMode.GEOMETRY) toggleGroup.check(R.id.btnGeometry);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                // 11.2 Admin Supported Subjects Check
                boolean allowed = true;
                if (checkedId == R.id.btnMath) {
                    if (dbHelper.getSetting("lab_math_enabled", "1").equals("0")) allowed = false;
                    else currentMode = LabMode.MATH;
                } else if (checkedId == R.id.btnPhysics) {
                    if (dbHelper.getSetting("lab_physics_enabled", "1").equals("0")) allowed = false;
                    else currentMode = LabMode.PHYSICS;
                } else if (checkedId == R.id.btnChemistry) {
                    if (dbHelper.getSetting("lab_chem_enabled", "1").equals("0")) allowed = false;
                    else currentMode = LabMode.CHEMISTRY;
                } else if (checkedId == R.id.btnGeometry) {
                    currentMode = LabMode.GEOMETRY; // Shared with math setting usually
                }

                if (!allowed) {
                    Toast.makeText(this, "This subject is currently restricted by admin.", Toast.LENGTH_SHORT).show();
                    // Revert to a safe mode or show warning
                } else {
                    resultPanel.setVisibility(View.GONE);
                    speak("Mode: " + currentMode.name());
                }
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
        findViewById(R.id.btnSwitchCamera).setOnClickListener(v -> switchCamera());

        findViewById(R.id.btnRetake).setOnClickListener(v -> {
            reviewLayer.setVisibility(View.GONE);
            focusOverlay.setVisibility(View.VISIBLE);
        });

        // 10.4 Multi-page Scanning
        findViewById(R.id.btnAddPage).setOnClickListener(v -> {
            if (accumulatedPages.size() >= MAX_PAGES) {
                Toast.makeText(this, "Max multi-page limit reached (5).", Toast.LENGTH_SHORT).show();
                return;
            }
            String text = etExtractedText.getText().toString().trim();
            if (!text.isEmpty()) {
                accumulatedPages.add(text);
                chipPageCount.setVisibility(View.VISIBLE);
                chipPageCount.setText("Session Pages: " + accumulatedPages.size());
                reviewLayer.setVisibility(View.GONE);
                focusOverlay.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Page captured. Scan next.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            StringBuilder combined = new StringBuilder();
            for (String p : accumulatedPages) combined.append(p).append("\n");
            combined.append(etExtractedText.getText().toString());

            String finalProblem = combined.toString().trim();
            if (finalProblem.isEmpty()) {
                Toast.makeText(this, "Please capture or enter text.", Toast.LENGTH_SHORT).show();
                return;
            }
            reviewLayer.setVisibility(View.GONE);
            solveProblem(finalProblem);
            
            // Clear page buffer after solving
            accumulatedPages.clear();
            chipPageCount.setVisibility(View.GONE);
        });

        // 10.5 External Calculator Integration
        findViewById(R.id.btnDesmos).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.desmos.com/calculator"));
            startActivity(i);
        });

        findViewById(R.id.btnSaveResult).setOnClickListener(v -> {
            String extracted = etExtractedText.getText().toString();
            String resultText = tvResultBody.getText().toString();
            dbHelper.saveLabResult(currentUsername, currentMode.name(), extracted, resultText, false, currentCourseContext);
            String investigationId = "scan_" + System.currentTimeMillis();
            String title = (extracted != null && !extracted.trim().isEmpty())
                    ? (extracted.length() > 30 ? extracted.substring(0, 30) + "..." : extracted)
                    : (currentMode.name() + " Lab Analysis");
            dbHelper.saveNotebookEntry(
                    currentUsername,
                    investigationId,
                    title,
                    currentMode.name(),
                    9,
                    "Camera AI Lab • " + currentMode.name(),
                    "Camera AI Analysis & Symbolic Solver",
                    "Extracted: " + extracted,
                    "N/A",
                    "Mode: " + currentMode.name(),
                    resultText,
                    "Verified via Camera AI Science Studio.",
                    100,
                    7,
                    true
            );
            Toast.makeText(this, "Saved to Lab History & Student Notebook!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnAssignment).setOnClickListener(v -> {
            dbHelper.saveLabResult(currentUsername, currentMode.name(), etExtractedText.getText().toString(), tvResultBody.getText().toString(), true, currentCourseContext);
            Toast.makeText(this, "Added to Assignment Helper!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnShareResult).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Solution Found via Camera Lab:\n\n" + tvResultBody.getText());
            startActivity(Intent.createChooser(intent, "Share Solution"));
        });

        findViewById(R.id.btnCopyResult).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Lab Result", tvResultBody.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnSimilar).setOnClickListener(v -> generateSimilarProblem());
        
        if (btnCreateFlashcard != null) {
            btnCreateFlashcard.setOnClickListener(v -> handleCreateFlashcard());
        }
        if (btnOpenVisualizer != null) {
            btnOpenVisualizer.setOnClickListener(v -> handleOpenVisualizer());
        }

        // Accessibility: Tap to speak result
        tvResultBody.setOnClickListener(v -> speak(tvResultBody.getText().toString()));
    }

    private void generateSimilarProblem() {
        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject solverMod = py.getModule("camera_lab_solver");
                String similar = solverMod.callAttr("generate_similar", etExtractedText.getText().toString(), currentMode.name()).toString();
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Practice Similar Problem")
                            .setMessage(similar)
                            .setPositiveButton("Solve This", (d, w) -> {
                                etExtractedText.setText(similar);
                                solveProblem(similar);
                            })
                            .setNegativeButton("Close", null)
                            .show();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void showProgressRecommendations() {
        Cursor cursor = dbHelper.getLabStats(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String topMode = cursor.getString(cursor.getColumnIndex("problem_type"));
            @SuppressLint("Range") int count = cursor.getInt(cursor.getColumnIndex("attempt_count"));
            cursor.close();

            if (count > 3) {
                String rec = (topMode.equals("MATH")) ? "Strong Math foundation! Ready for Chemistry?" : "Physics Expert! Try some Engineering Math?";
                Toast.makeText(this, "Recommendation: " + rec, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void speak(String text) {
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupZoomAndFocus() {
        zoomSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (camera != null) camera.getCameraControl().setLinearZoom(value);
        });
        viewFinder.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                autoFocus(event.getX(), event.getY());
                return true;
            }
            return true;
        });
    }

    private void autoFocus(float x, float y) {
        if (camera == null) return;
        MeteringPoint point = viewFinder.getMeteringPointFactory().createPoint(x, y);
        camera.getCameraControl().startFocusAndMetering(new FocusMeteringAction.Builder(point).build());
    }

    private void switchCamera() {
        lensFacing = (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
        startCamera();
    }

    private void toggleFlash(ImageView btn) {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
            btn.setImageResource(R.drawable.ic_bolt);
            btn.setColorFilter(isFlashOn ? 0xFFF59E0B : 0xFFFFFFFF);
        }
    }

    private void checkPermissionsAndStartCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build();
                provider.unbindAll();
                camera = provider.bindToLifecycle(this, lensFacing, preview, imageCapture);
            } catch (Exception e) { e.printStackTrace(); }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        
        // PROFESSIONAL FIX: Save to getFilesDir() instead of getExternalCacheDir() 
        // to ensure photos survive "Clear Cache" and are included in backups.
        File storageDir = new File(getFilesDir(), "lab_captures");
        if (!storageDir.exists()) storageDir.mkdirs();
        File file = new File(storageDir, "LAB_" + System.currentTimeMillis() + ".jpg");

        imageCapture.takePicture(new ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override public void onImageSaved(@NonNull ImageCapture.OutputFileResults res) { showReview(Uri.fromFile(file)); }
            @Override public void onError(@NonNull ImageCaptureException e) { Toast.makeText(CameraLabActivity.this, "Capture Error", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void showReview(Uri uri) {
        ivCapturedImage.setImageURI(uri);
        reviewLayer.setVisibility(View.VISIBLE);
        focusOverlay.setVisibility(View.GONE);
        etExtractedText.setText("");
        etExtractedText.setHint("Analyzing visual data...");
        processImagePipeline(uri);
    }

    private void processImagePipeline(Uri uri) {
        new Thread(() -> {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                String base64 = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);

                Python py = Python.getInstance();
                PyObject solverMod = py.getModule("camera_lab_solver");
                
                String processedB64 = solverMod.callAttr("preprocess_image", base64).toString();
                PyObject visualsObj = solverMod.callAttr("detect_visuals", processedB64);
                detectedVisuals = visualsObj.asList().stream().map(Object::toString).collect(Collectors.toList());

                byte[] decoded = Base64.decode(processedB64, Base64.DEFAULT);
                Bitmap processedBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                runOnUiThread(() -> {
                    ivCapturedImage.setImageBitmap(processedBitmap);
                    
                    // 10.3 Augmented Reality Simulation
                    if (detectedVisuals != null && !detectedVisuals.isEmpty()) {
                        Toast.makeText(this, "AR Identify: " + detectedVisuals.get(0), Toast.LENGTH_SHORT).show();
                    }

                    recognizer.process(InputImage.fromBitmap(processedBitmap, 0))
                            .addOnSuccessListener(t -> {
                                String result = t.getText();
                                if (!result.isEmpty()) etExtractedText.setText(result);
                                else etExtractedText.setHint("OCR failed. Enter text manually.");
                            });
                });
            } catch (Exception e) { 
                runOnUiThread(() -> Toast.makeText(this, "Pipeline Fault", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void solveProblem(String text) {
        resultPanel.setVisibility(View.GONE);
        analysisProgress.setVisibility(View.VISIBLE);
        
        isFlashcardSaved = false;
        isSavingFlashcard = false;
        if (btnCreateFlashcard != null) {
            btnCreateFlashcard.setEnabled(true);
            btnCreateFlashcard.setText("CREATE FLASHCARD");
            btnCreateFlashcard.setIconResource(R.drawable.ic_flashcard);
        }

        // 11.3 Admin Solver Parameters
        int maxSteps = Integer.parseInt(dbHelper.getSetting("lab_max_steps", "10"));

        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject solverMod = py.getModule("camera_lab_solver");
                String solution;
                
                // Pass current language and max steps to engine
                switch (currentMode) {
                    case MATH: solution = solverMod.callAttr("solve_math", text, currentLangCode, maxSteps).toString(); break;
                    case PHYSICS: solution = solverMod.callAttr("solve_physics", text, currentLangCode).toString(); break;
                    case CHEMISTRY: solution = solverMod.callAttr("solve_chemistry", text, currentLangCode).toString(); break;
                    case GEOMETRY: solution = solverMod.callAttr("solve_geometry", text, detectedVisuals != null ? detectedVisuals.toArray() : new String[0], currentLangCode).toString(); break;
                    default: solution = "Solver module not configured.";
                }

                // Auto-save scan to lab history if enabled in CameraSettings
                SharedPreferences camPrefs = getSharedPreferences("CameraSettings", MODE_PRIVATE);
                if (camPrefs.getBoolean("save_history", true)) {
                    dbHelper.saveLabResult(currentUsername, currentMode.name(), text, solution, false, currentCourseContext);
                }

                runOnUiThread(() -> {
                    analysisProgress.setVisibility(View.GONE);
                    tvResultHeader.setText(currentMode.name() + " Laboratory Engine");
                    tvResultBody.setText(solution);
                    resultPanel.setVisibility(View.VISIBLE);
                    speak("Analysis complete. Review steps below.");

                    isFlashcardSaved = false;
                    isSavingFlashcard = false;
                    if (btnCreateFlashcard != null) {
                        btnCreateFlashcard.setEnabled(true);
                        btnCreateFlashcard.setText("CREATE FLASHCARD");
                        btnCreateFlashcard.setIconResource(R.drawable.ic_flashcard);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    analysisProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Solver Engine Error", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String resolveAuthenticatedStudentUsername() {
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = session.getString("username", null);
        if (username != null) {
            username = username.trim();
        }
        if (username == null || username.isEmpty() 
                || "Guest".equalsIgnoreCase(username) 
                || "Student".equalsIgnoreCase(username)) {
            try {
                SecurePreferencesHelper secHelper = new SecurePreferencesHelper(this);
                String secUser = secHelper.getString("username", null);
                if (secUser != null) {
                    secUser = secUser.trim();
                    if (!secUser.isEmpty() 
                            && !"Guest".equalsIgnoreCase(secUser) 
                            && !"Student".equalsIgnoreCase(secUser)) {
                        username = secUser;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (username == null || username.isEmpty() 
                || "Guest".equalsIgnoreCase(username) 
                || "Student".equalsIgnoreCase(username)) {
            return null;
        }
        return username;
    }

    public static boolean isGenuineSolution(String question, String solution) {
        if (question == null || solution == null) {
            return false;
        }
        String q = question.trim();
        String s = solution.trim();

        // 1. Question must contain meaningful content (at least one alphanumeric character)
        if (q.isEmpty() || !q.matches(".*[a-zA-Z0-9].*")) {
            return false;
        }

        // 2. Reject basic stubs and common non-answers
        if (s.isEmpty() || s.equalsIgnoreCase("Error") || s.equalsIgnoreCase("NaN") 
                || s.equalsIgnoreCase("null") || s.equals("[]") || s.equals("{}")) {
            return false;
        }

        // 3. Reject solver engine errors and pipeline failure messages
        String sLower = s.toLowerCase(Locale.ROOT);
        if (sLower.contains("mathematical engine error") 
                || s.contains("የሂሳብ ሞተር ስህተት")
                || sLower.contains("solver module not configured")
                || sLower.contains("processing...")
                || sLower.contains("pipeline fault")
                || sLower.contains("solver engine error")
                || sLower.contains("failed to solve")
                || sLower.contains("syntax error")) {
            return false;
        }

        // 4. Must contain substantive solved content:
        boolean hasStructure = s.contains("FINAL ANSWER") 
                || s.contains("SOLUTION STEPS") 
                || s.contains("EXTRACTED PROBLEM")
                || s.contains("የመጨረሻ መልስ")
                || s.contains("Molar Mass")
                || s.contains("Calculation:")
                || s.contains("Solutions:")
                || s.contains("Derivative:")
                || s.contains("Simplified:")
                || s.contains("Result:");

        boolean hasSubstance = s.matches(".*[a-zA-Z0-9].*") && s.length() >= 3;

        return hasStructure || hasSubstance;
    }

    private void handleCreateFlashcard() {
        if (isSavingFlashcard || isFlashcardSaved) {
            return;
        }

        // 1. Resolve student username
        String username = resolveAuthenticatedStudentUsername();
        if (username == null) {
            Toast.makeText(this, "Please log in with a student account to save flashcards.", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Validate question and solution
        String question = etExtractedText != null ? etExtractedText.getText().toString().trim() : "";
        String solution = tvResultBody != null ? tvResultBody.getText().toString().trim() : "";

        if (!isGenuineSolution(question, solution)) {
            Toast.makeText(this, "Cannot create flashcard: No genuine solution available to save.", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Temporarily disable button to prevent repeated clicks while inserting
        isSavingFlashcard = true;
        if (btnCreateFlashcard != null) {
            btnCreateFlashcard.setEnabled(false);
        }

        // 4. Save to database using existing flashcard schema and SM-2 method
        String subject = currentMode.name();
        boolean inserted = dbHelper.insertFlashcard(username, question, solution, subject);

        isSavingFlashcard = false;

        if (inserted) {
            isFlashcardSaved = true;
            if (btnCreateFlashcard != null) {
                btnCreateFlashcard.setEnabled(false);
                btnCreateFlashcard.setText("FLASHCARD SAVED");
                btnCreateFlashcard.setIconResource(R.drawable.ic_verified_check);
            }
            Toast.makeText(this, "Flashcard created! Ready for spaced repetition in " + subject + ".", Toast.LENGTH_SHORT).show();
        } else {
            // Insertion failed: re-enable button so student can retry
            isFlashcardSaved = false;
            if (btnCreateFlashcard != null) {
                btnCreateFlashcard.setEnabled(true);
            }
            Toast.makeText(this, "Failed to save flashcard to database. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleOpenVisualizer() {
        Intent intent = new Intent(this, VisualizerActivity.class);
        intent.putExtra("SUBJECT", currentMode.name());
        intent.putExtra("TARGET_TAB", VisualizerActivity.mapSubjectToTabIndex(currentMode.name()));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyCameraSettings();
    }

    private void applyCameraSettings() {
        SharedPreferences camPrefs = getSharedPreferences(CameraSettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean showGrid = camPrefs.getBoolean(CameraSettingsActivity.KEY_GRID, false);
        View gridView = findViewById(R.id.viewFinderGrid);
        if (gridView != null) {
            gridView.setVisibility(showGrid ? View.VISIBLE : View.GONE);
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        cameraExecutor.shutdown();
    }
}
