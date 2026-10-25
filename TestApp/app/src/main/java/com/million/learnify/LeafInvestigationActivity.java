package com.million.learnify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import java.util.Locale;

public class LeafInvestigationActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername;
    private static final String INVESTIGATION_ID = "bio_grade9_leaf_01";

    private int currentStage = 1;
    private TextToSpeech tts;
    private boolean isTtsReady = false;

    // Stage Views
    private LinearLayout layoutStage1, layoutStage2, layoutStage3, layoutStage4, layoutStage5, layoutStage6, layoutStage7;
    private TextView tabStage1, tabStage2, tabStage3, tabStage4, tabStage5, tabStage6, tabStage7;

    // Stage 1 & 2
    private EditText etHypothesis;

    // Stage 3
    private ImageView ivSpecimenPreview;
    private CheckBox chkTraitBlade, chkTraitVenation, chkTraitCuticle;
    private EditText etObservations;

    // Stage 4 (Explore Anatomy)
    private TextView btnLayerCuticle, btnLayerPalisade, btnLayerSpongy, btnLayerVein, btnLayerStomata;
    private TextView tvTissueName, tvTissueRole, tvTissueDescription, tvTissueSignificance;

    // Stage 5 (Simulation)
    private Slider sliderLight, sliderCo2, sliderTemp;
    private TextView tvLightVal, tvCo2Val, tvTempVal;
    private TextView tvRatePhoto, tvRateTranspiration, tvRateBubbles, tvLimitingFactor;

    // Stage 6 (Quiz)
    private RadioGroup rgQ1, rgQ2, rgQ3;
    private LinearLayout llQuizFeedback;
    private TextView tvQuizScoreDisplay, tvQuizFeedbackExplanation;
    private int calculatedQuizScore = 100;

    // Stage 7 (Reflect)
    private EditText etConclusion;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (ivSpecimenPreview != null) {
                        ivSpecimenPreview.setImageURI(selectedUri);
                        ivSpecimenPreview.setImageTintList(null);
                        Toast.makeText(this, "Specimen image loaded.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaf_investigation);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Guest");

        initViews();
        setupToolbar();
        initTts();
        setupStageNavigation();
        setupStage2HypothesisTemplates();
        setupStage3SpecimenActions();
        setupStage4AnatomyExplorer();
        setupStage5Simulation();
        setupStage6Quiz();
        setupStage7NotebookSave();

        restorePriorProgress();
    }

    private void initViews() {
        layoutStage1 = findViewById(R.id.layoutStage1);
        layoutStage2 = findViewById(R.id.layoutStage2);
        layoutStage3 = findViewById(R.id.layoutStage3);
        layoutStage4 = findViewById(R.id.layoutStage4);
        layoutStage5 = findViewById(R.id.layoutStage5);
        layoutStage6 = findViewById(R.id.layoutStage6);
        layoutStage7 = findViewById(R.id.layoutStage7);

        tabStage1 = findViewById(R.id.tabStage1);
        tabStage2 = findViewById(R.id.tabStage2);
        tabStage3 = findViewById(R.id.tabStage3);
        tabStage4 = findViewById(R.id.tabStage4);
        tabStage5 = findViewById(R.id.tabStage5);
        tabStage6 = findViewById(R.id.tabStage6);
        tabStage7 = findViewById(R.id.tabStage7);

        etHypothesis = findViewById(R.id.etHypothesis);
        ivSpecimenPreview = findViewById(R.id.ivSpecimenPreview);
        chkTraitBlade = findViewById(R.id.chkTraitBlade);
        chkTraitVenation = findViewById(R.id.chkTraitVenation);
        chkTraitCuticle = findViewById(R.id.chkTraitCuticle);
        etObservations = findViewById(R.id.etObservations);

        btnLayerCuticle = findViewById(R.id.btnLayerCuticle);
        btnLayerPalisade = findViewById(R.id.btnLayerPalisade);
        btnLayerSpongy = findViewById(R.id.btnLayerSpongy);
        btnLayerVein = findViewById(R.id.btnLayerVein);
        btnLayerStomata = findViewById(R.id.btnLayerStomata);
        tvTissueName = findViewById(R.id.tvTissueName);
        tvTissueRole = findViewById(R.id.tvTissueRole);
        tvTissueDescription = findViewById(R.id.tvTissueDescription);
        tvTissueSignificance = findViewById(R.id.tvTissueSignificance);

        sliderLight = findViewById(R.id.sliderLight);
        sliderCo2 = findViewById(R.id.sliderCo2);
        sliderTemp = findViewById(R.id.sliderTemp);
        tvLightVal = findViewById(R.id.tvLightVal);
        tvCo2Val = findViewById(R.id.tvCo2Val);
        tvTempVal = findViewById(R.id.tvTempVal);
        tvRatePhoto = findViewById(R.id.tvRatePhoto);
        tvRateTranspiration = findViewById(R.id.tvRateTranspiration);
        tvRateBubbles = findViewById(R.id.tvRateBubbles);
        tvLimitingFactor = findViewById(R.id.tvLimitingFactor);

        rgQ1 = findViewById(R.id.rgQ1);
        rgQ2 = findViewById(R.id.rgQ2);
        rgQ3 = findViewById(R.id.rgQ3);
        llQuizFeedback = findViewById(R.id.llQuizFeedback);
        tvQuizScoreDisplay = findViewById(R.id.tvQuizScoreDisplay);
        tvQuizFeedbackExplanation = findViewById(R.id.tvQuizFeedbackExplanation);

        etConclusion = findViewById(R.id.etConclusion);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarLeaf);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        View btnVoice = findViewById(R.id.btnLeafVoiceGuide);
        if (btnVoice != null) {
            btnVoice.setOnClickListener(v -> speakCurrentStage());
        }

        View btnNotebook = findViewById(R.id.btnLeafNotebook);
        if (btnNotebook != null) {
            btnNotebook.setOnClickListener(v -> startActivity(new Intent(this, LabNotebookActivity.class)));
        }
    }

    private void initTts() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(Locale.US);
                isTtsReady = (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void speakCurrentStage() {
        if (!isTtsReady || tts == null) {
            Toast.makeText(this, "Voice engine initializing...", Toast.LENGTH_SHORT).show();
            return;
        }

        String msg;
        switch (currentStage) {
            case 1:
                msg = "Stage 1: Prepare. Review the inquiry question on leaf adaptations, verify your equipment checklist, and confirm lab safety.";
                break;
            case 2:
                msg = "Stage 2: Predict. Formulate your formal scientific hypothesis regarding leaf stomata distribution and photosynthesis rates.";
                break;
            case 3:
                msg = "Stage 3: Observe. Inspect your specimen with the camera or gallery, check off morphological traits, and record your observations.";
                break;
            case 4:
                msg = "Stage 4: Explore Anatomy. Tap the tissue layers to examine the cuticle, palisade cells, spongy mesophyll, vascular veins, and stomata.";
                break;
            case 5:
                msg = "Stage 5: Investigate. Manipulate light intensity, carbon dioxide, and temperature to calculate real-time photosynthesis and transpiration rates.";
                break;
            case 6:
                msg = "Stage 6: Review. Answer the concept mastery assessment to test your understanding of leaf tissue functions.";
                break;
            case 7:
            default:
                msg = "Stage 7: Reflect and Submit. Synthesize your findings, write your final conclusion, and save your report to the Student Lab Notebook.";
                break;
        }
        tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "STAGE_" + currentStage);
        Toast.makeText(this, "Playing Stage Audio Guide...", Toast.LENGTH_SHORT).show();
    }

    private void setupStageNavigation() {
        tabStage1.setOnClickListener(v -> showStage(1));
        tabStage2.setOnClickListener(v -> showStage(2));
        tabStage3.setOnClickListener(v -> showStage(3));
        tabStage4.setOnClickListener(v -> showStage(4));
        tabStage5.setOnClickListener(v -> showStage(5));
        tabStage6.setOnClickListener(v -> showStage(6));
        tabStage7.setOnClickListener(v -> showStage(7));

        // Inter-stage buttons
        findViewById(R.id.btnProceedToPredict).setOnClickListener(v -> showStage(2));
        findViewById(R.id.btnBackToPrepare).setOnClickListener(v -> showStage(1));
        findViewById(R.id.btnProceedToObserve).setOnClickListener(v -> showStage(3));
        findViewById(R.id.btnBackToPredict).setOnClickListener(v -> showStage(2));
        findViewById(R.id.btnProceedToExplore).setOnClickListener(v -> showStage(4));
        findViewById(R.id.btnBackToObserve).setOnClickListener(v -> showStage(3));
        findViewById(R.id.btnProceedToInvestigate).setOnClickListener(v -> showStage(5));
        findViewById(R.id.btnBackToExplore).setOnClickListener(v -> showStage(4));
        findViewById(R.id.btnProceedToReview).setOnClickListener(v -> showStage(6));
        findViewById(R.id.btnBackToInvestigate).setOnClickListener(v -> showStage(5));
        findViewById(R.id.btnProceedToReflect).setOnClickListener(v -> {
            gradeQuiz();
            showStage(7);
        });
        findViewById(R.id.btnBackToReview).setOnClickListener(v -> showStage(6));
    }

    private void showStage(int stage) {
        currentStage = stage;

        layoutStage1.setVisibility(stage == 1 ? View.VISIBLE : View.GONE);
        layoutStage2.setVisibility(stage == 2 ? View.VISIBLE : View.GONE);
        layoutStage3.setVisibility(stage == 3 ? View.VISIBLE : View.GONE);
        layoutStage4.setVisibility(stage == 4 ? View.VISIBLE : View.GONE);
        layoutStage5.setVisibility(stage == 5 ? View.VISIBLE : View.GONE);
        layoutStage6.setVisibility(stage == 6 ? View.VISIBLE : View.GONE);
        layoutStage7.setVisibility(stage == 7 ? View.VISIBLE : View.GONE);

        updateTabStyle(tabStage1, stage == 1);
        updateTabStyle(tabStage2, stage == 2);
        updateTabStyle(tabStage3, stage == 3);
        updateTabStyle(tabStage4, stage == 4);
        updateTabStyle(tabStage5, stage == 5);
        updateTabStyle(tabStage6, stage == 6);
        updateTabStyle(tabStage7, stage == 7);
    }

    private void updateTabStyle(TextView tab, boolean isActive) {
        if (tab == null) return;
        if (isActive) {
            tab.setBackgroundResource(R.drawable.bg_science_stage_active);
            tab.setTextColor(0xFF071426);
        } else {
            tab.setBackgroundResource(R.drawable.bg_science_stage_inactive);
            tab.setTextColor(0xFFB9C9DC);
        }
    }

    private void setupStage2HypothesisTemplates() {
        MaterialButton btnT1 = findViewById(R.id.btnHypoTemplate1);
        if (btnT1 != null) {
            btnT1.setOnClickListener(v -> etHypothesis.setText("Stomata density is significantly higher on the lower epidermis to reduce solar evaporation while allowing sufficient CO2 intake for photosynthesis."));
        }
        MaterialButton btnT2 = findViewById(R.id.btnHypoTemplate2);
        if (btnT2 != null) {
            btnT2.setOnClickListener(v -> etHypothesis.setText("Photosynthesis rate will increase linearly with light intensity until carbon dioxide availability or temperature becomes the limiting factor."));
        }
    }

    private void setupStage3SpecimenActions() {
        View btnCam = findViewById(R.id.btnObserveCamera);
        if (btnCam != null) {
            btnCam.setOnClickListener(v -> {
                Intent intent = new Intent(this, CameraLabActivity.class);
                intent.putExtra("COURSE_NAME", "Biology");
                startActivity(intent);
            });
        }

        View btnGallery = findViewById(R.id.btnObserveGallery);
        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(pickIntent);
            });
        }
    }

    private void setupStage4AnatomyExplorer() {
        btnLayerCuticle.setOnClickListener(v -> selectTissue(0));
        btnLayerPalisade.setOnClickListener(v -> selectTissue(1));
        btnLayerSpongy.setOnClickListener(v -> selectTissue(2));
        btnLayerVein.setOnClickListener(v -> selectTissue(3));
        btnLayerStomata.setOnClickListener(v -> selectTissue(4));

        selectTissue(0);
    }

    private void selectTissue(int index) {
        updateTabStyle(btnLayerCuticle, index == 0);
        updateTabStyle(btnLayerPalisade, index == 1);
        updateTabStyle(btnLayerSpongy, index == 2);
        updateTabStyle(btnLayerVein, index == 3);
        updateTabStyle(btnLayerStomata, index == 4);

        switch (index) {
            case 0:
                tvTissueName.setText("Upper Cuticle & Epidermis");
                tvTissueRole.setText("PRIMARY ROLE: Water Barrier & Light Transmission");
                tvTissueDescription.setText("A transparent cellular layer coated in a lipid/wax layer called cutin. It prevents desiccation and pathogen invasion while remaining optically transparent for sunlight to penetrate into the palisade cells beneath.");
                tvTissueSignificance.setText("Adaptation: Xerophytes (dry-climate plants) feature an extra-thick waxy cuticle to survive prolonged drought.");
                break;
            case 1:
                tvTissueName.setText("Palisade Mesophyll");
                tvTissueRole.setText("PRIMARY ROLE: Primary Photosynthesis Site (~80%)");
                tvTissueDescription.setText("Densely packed, vertically elongated cylindrical cells positioned directly beneath the upper epidermis. Packed with high concentrations of chloroplasts to intercept maximum photons of sunlight.");
                tvTissueSignificance.setText("Adaptation: In sun leaves, palisade mesophyll can form multiple stacked layers compared to shade leaves.");
                break;
            case 2:
                tvTissueName.setText("Spongy Mesophyll");
                tvTissueRole.setText("PRIMARY ROLE: Gas Exchange & Internal CO2 Diffusion");
                tvTissueDescription.setText("Loosely organized, irregularly shaped cells interspersed with large intercellular air spaces. Facilitates rapid gaseous diffusion of CO2 towards photosynthetic cells and O2 outwards.");
                tvTissueSignificance.setText("Adaptation: High internal surface area to volume ratio maximizes gas dissolution at cell membrane boundaries.");
                break;
            case 3:
                tvTissueName.setText("Vascular Bundle (Vein: Xylem & Phloem)");
                tvTissueRole.setText("PRIMARY ROLE: Fluid Transport & Structural Support");
                tvTissueDescription.setText("Xylem vessels transport water and dissolved inorganic ions upward from roots. Phloem sieve-tube elements transport sucrose and amino acids produced by photosynthesis to growing roots, shoots, and fruit.");
                tvTissueSignificance.setText("Adaptation: Lignified xylem walls prevent vessel collapse under negative tension produced by transpiration.");
                break;
            case 4:
            default:
                tvTissueName.setText("Lower Epidermis & Stomata / Guard Cells");
                tvTissueRole.setText("PRIMARY ROLE: Regulated Gas Exchange & Transpiration Control");
                tvTissueDescription.setText("Microscopic turgor-controlled pores flanked by pairs of guard cells. Opening allows essential CO2 uptake for photosynthesis but incurs water vapor loss (transpiration). Mainly situated on the lower surface to prevent thermal drying.");
                tvTissueSignificance.setText("Adaptation: Guard cells accumulate potassium ions (K+) to absorb water and swell, opening the stomatal aperture during daylight.");
                break;
        }
    }

    private void setupStage5Simulation() {
        Slider.OnChangeListener listener = (slider, value, fromUser) -> updateSimulationOutputs();
        sliderLight.addOnChangeListener(listener);
        sliderCo2.addOnChangeListener(listener);
        sliderTemp.addOnChangeListener(listener);

        updateSimulationOutputs();

        // Launch fullscreen biology lab simulation
        View btnFullSim = findViewById(R.id.btnLaunchInteractiveSim);
        if (btnFullSim != null) {
            btnFullSim.setOnClickListener(v -> {
                Intent intent = new Intent(this, BiologySimulationActivity.class);
                intent.putExtra("EXPERIMENT_ID", 205); // Photosynthesis Basics
                intent.putExtra("EXPERIMENT_NAME", "Photosynthesis Basics Lab");
                startActivity(intent);
            });
        }
    }

    private void updateSimulationOutputs() {
        float light = sliderLight.getValue();
        float co2 = sliderCo2.getValue();
        float temp = sliderTemp.getValue();

        tvLightVal.setText(String.format(Locale.US, "%.0f %%", light));
        tvCo2Val.setText(String.format(Locale.US, "%.0f ppm", co2));
        tvTempVal.setText(String.format(Locale.US, "%.0f °C", temp));

        // Biological kinetics calculation
        double lightFactor = light / (light + 22.0);
        double co2Factor = co2 / (co2 + 320.0);
        double tempFactor = Math.exp(-0.5 * Math.pow((temp - 28.0) / 8.5, 2.0));

        double ratePhoto = 100.0 * lightFactor * co2Factor * tempFactor * 1.5;
        ratePhoto = Math.max(0.0, Math.min(100.0, ratePhoto));

        double rateTransp = (light * 0.035 + (temp - 10.0) * 0.15) * (1.0 - (co2Factor * 0.25));
        rateTransp = Math.max(0.1, rateTransp);

        int bubbles = (int) Math.round(ratePhoto * 0.55);

        String limiting;
        if (temp > 38.0) {
            limiting = "Heat Stress / Enzyme Denaturation";
        } else if (temp < 15.0) {
            limiting = "Low Temperature (Kinetics)";
        } else if (lightFactor < co2Factor) {
            limiting = "Light Intensity (Photons)";
        } else {
            limiting = "CO2 Concentration (ppm)";
        }

        tvRatePhoto.setText(String.format(Locale.US, "%.0f %%", ratePhoto));
        tvRateTranspiration.setText(String.format(Locale.US, "%.1f mL/h", rateTransp));
        tvRateBubbles.setText(String.format(Locale.US, "%d bpm", bubbles));
        tvLimitingFactor.setText(limiting);
    }

    private void setupStage6Quiz() {
        RadioGroup.OnCheckedChangeListener quizListener = (group, checkedId) -> gradeQuiz();
        rgQ1.setOnCheckedChangeListener(quizListener);
        rgQ2.setOnCheckedChangeListener(quizListener);
        rgQ3.setOnCheckedChangeListener(quizListener);
    }

    private void gradeQuiz() {
        int correctCount = 0;
        int answeredCount = 0;

        // Q1: B (Palisade mesophyll) is correct
        int q1 = rgQ1.getCheckedRadioButtonId();
        if (q1 != -1) {
            answeredCount++;
            if (q1 == R.id.rbQ1B) correctCount++;
        }

        // Q2: A (Minimizes water evaporation) is correct
        int q2 = rgQ2.getCheckedRadioButtonId();
        if (q2 != -1) {
            answeredCount++;
            if (q2 == R.id.rbQ2A) correctCount++;
        }

        // Q3: B (Xylem) is correct
        int q3 = rgQ3.getCheckedRadioButtonId();
        if (q3 != -1) {
            answeredCount++;
            if (q3 == R.id.rbQ3B) correctCount++;
        }

        if (answeredCount > 0) {
            calculatedQuizScore = (int) Math.round((correctCount / 3.0) * 100);
            llQuizFeedback.setVisibility(View.VISIBLE);
            tvQuizScoreDisplay.setText(String.format(Locale.US, "Score: %d%% (%d/3 Correct)", calculatedQuizScore, correctCount));
            
            if (correctCount == 3) {
                tvQuizScoreDisplay.setTextColor(0xFF34D399); // Emerald
                tvQuizFeedbackExplanation.setText("Outstanding! You demonstrated complete mastery of palisade mesophyll chloroplast density, lower epidermal stomata function, and xylem water transport.");
            } else {
                tvQuizScoreDisplay.setTextColor(0xFFFBBF24); // Amber
                tvQuizFeedbackExplanation.setText("Good effort! Remember: Palisade cells capture the most light, lower stomata reduce water loss from heat, and Xylem transports water up from roots.");
            }
        }
    }

    private void setupStage7NotebookSave() {
        findViewById(R.id.btnSaveToNotebook).setOnClickListener(v -> saveNotebookReport());
    }

    private void saveNotebookReport() {
        String hypothesis = etHypothesis.getText().toString().trim();
        if (hypothesis.isEmpty()) {
            hypothesis = "Stomata density is higher on lower epidermis to control transpiration under light.";
        }

        StringBuilder observations = new StringBuilder();
        if (chkTraitBlade.isChecked()) observations.append("Broad flattened blade. ");
        if (chkTraitVenation.isChecked()) observations.append("Reticulate venation with midrib. ");
        if (chkTraitCuticle.isChecked()) observations.append("Glossy waxy cuticle. ");
        String obsNote = etObservations.getText().toString().trim();
        if (!obsNote.isEmpty()) observations.append("Notes: ").append(obsNote);

        String simInputs = String.format(Locale.US, "Light: %.0f%%, CO2: %.0f ppm, Temp: %.0f°C",
                sliderLight.getValue(), sliderCo2.getValue(), sliderTemp.getValue());
        String simResults = String.format(Locale.US, "Photosynthesis Rate: %s, Transpiration: %s, O2 Bubbles: %s, Limiting Factor: %s",
                tvRatePhoto.getText(), tvRateTranspiration.getText(), tvRateBubbles.getText(), tvLimitingFactor.getText());

        String conclusion = etConclusion.getText().toString().trim();
        if (conclusion.isEmpty()) {
            conclusion = "The experiment confirmed that internal leaf anatomy—specifically palisade mesophyll and lower stomata—efficiently maximizes photosynthetic light capture while regulating water loss through transpiration.";
            etConclusion.setText(conclusion);
        }

        boolean saved = dbHelper.saveNotebookEntry(
                currentUsername,
                INVESTIGATION_ID,
                "Plant Leaf Investigation: Structure & Photosynthesis",
                "Biology",
                9,
                "Grade 9 Biology • Module 1 (Plant Anatomy)",
                hypothesis,
                observations.toString(),
                "Cuticle, Palisade Mesophyll, Spongy Mesophyll, Vascular Bundle, Stomata",
                simInputs,
                simResults,
                conclusion,
                calculatedQuizScore,
                7,
                true
        );

        if (saved) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Investigation Saved!")
                    .setMessage("Your complete Grade 9 Leaf Investigation report, simulation data, and quiz score (" + calculatedQuizScore + "%) have been recorded in your personal Student Lab Notebook.")
                    .setPositiveButton("OPEN NOTEBOOK", (d, w) -> {
                        Intent intent = new Intent(this, LabNotebookActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNeutralButton("RETURN TO STUDIO", (d, w) -> finish())
                    .setNegativeButton("STAY HERE", null)
                    .show();
        } else {
            Toast.makeText(this, "Failed to save notebook entry. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void restorePriorProgress() {
        DatabaseHelper.NotebookEntry entry = dbHelper.getNotebookEntry(currentUsername, INVESTIGATION_ID);
        if (entry != null) {
            if (entry.hypothesis != null && !entry.hypothesis.isEmpty()) {
                etHypothesis.setText(entry.hypothesis);
            }
            if (entry.observations != null && !entry.observations.isEmpty()) {
                etObservations.setText(entry.observations);
            }
            if (entry.conclusion != null && !entry.conclusion.isEmpty()) {
                etConclusion.setText(entry.conclusion);
            }
            calculatedQuizScore = entry.quizScore;
            if (entry.currentStage > 1) {
                showStage(entry.currentStage);
            }
            Toast.makeText(this, "Prior investigation progress restored.", Toast.LENGTH_SHORT).show();
        }
    }
}

