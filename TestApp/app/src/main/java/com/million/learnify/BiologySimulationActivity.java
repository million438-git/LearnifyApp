package com.million.learnify;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Locale;

public class BiologySimulationActivity extends AppCompatActivity {

    private int experimentId;
    private String experimentName;
    private WebView webViewSim;
    private LinearLayout controlPanel;
    private TextView tvSimDescription, tvChallenge, tvExpandTheory;
    private TabLayout tabLayout;
    private boolean isTheoryExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biology_simulation);

        experimentId = getIntent().getIntExtra("experiment_id", 201);
        experimentName = getIntent().getStringExtra("experiment_name");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(experimentName);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        setupWebView();
        setupTabs();
        setupSimulation("experiments");
    }

    private void initViews() {
        webViewSim = findViewById(R.id.webViewSim);
        controlPanel = findViewById(R.id.controlPanel);
        tvSimDescription = findViewById(R.id.tvSimDescription);
        tvChallenge = findViewById(R.id.tvChallenge);
        tvExpandTheory = findViewById(R.id.tvExpandTheory);
        tabLayout = findViewById(R.id.tabLayout);

        tvExpandTheory.setOnClickListener(v -> {
            isTheoryExpanded = !isTheoryExpanded;
            tvSimDescription.setMaxLines(isTheoryExpanded ? Integer.MAX_VALUE : 3);
            tvExpandTheory.setText(isTheoryExpanded ? "Collapse" : "Expand");
        });
        tvSimDescription.setMaxLines(3);
    }

    private void setupWebView() {
        WebSettings webSettings = webViewSim.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webViewSim.setWebViewClient(new WebViewClient());
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mode = "experiments";
                if (tab.getPosition() == 1) mode = "diagrams";
                else if (tab.getPosition() == 2) mode = "3d";
                setupSimulation(mode);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSimulation(String mode) {
        controlPanel.removeAllViews();
        String filePrefix = mode.equals("3d") ? "3d_" : (mode.equals("diagrams") ? "diag_" : "");
        
        switch (experimentId) {
            // --- LOW LEVEL (201-210) ---
            case 201: 
                loadBioSim(filePrefix + "microscope.html", "Microscope Intro: Learn parts and how to focus. Using light to magnify small organisms.", "Focus on the onion cell until you see the nucleus clearly.");
                showMicroscopeControls(); break;
            case 202: 
                loadBioSim(filePrefix + "cell.html", "Cell Observation: Animal cells lack walls and chloroplasts. Plant cells are rigid.", "Identify three differences between the plant and animal cell models.");
                showCellControls(mode); break;
            case 203: 
                loadBioSim(filePrefix + "osmosis.html", "Osmosis: Diffusion of water through a semi-permeable membrane from high to low concentration.", "Observe what happens to a cell in 10% vs 0% salt solution.");
                showSliderControls("Solute Level", 0, 100, 20, "setConc"); break;
            case 204: 
                loadBioSim(filePrefix + "food_test.html", "Food Test: Chemical indicators change color in presence of starch (Iodine) or glucose (Benedict's).", "Test all samples. Which one contains the highest concentration of protein?");
                showActionControls("Add Indicator", "addReagent"); break;
            case 205: 
                loadBioSim(filePrefix + "photosynthesis_basic.html", "Photosynthesis: Plants convert light, water, and CO2 into chemical energy (glucose).", "Cover the middle of the leaf and explain why it doesn't turn blue with iodine.");
                showActionControls("Apply Iodine", "interact"); break;
            case 206: 
                loadBioSim(filePrefix + "respiration.html", "Respiration: All living things release energy from glucose. CO2 is a major byproduct.", "Monitor the CO2 levels in the flask as seeds germinate.");
                showSliderControls("Seed Activity", 0, 10, 5, "setRate"); break;
            case 207: 
                loadBioSim(filePrefix + "enzyme_basic.html", "Enzymes: Biological catalysts that speed up reactions without being consumed.", "Time how long it takes for starch to disappear at 37°C.");
                showActionControls("Start Reaction", "interact"); break;
            case 208: 
                loadBioSim(filePrefix + "human_body.html", "Body Systems: Organs work together in systems (Digestive, Circulatory, etc.) to maintain life.", "Identify and label the organs of the digestive system.");
                showRotationControls(mode); break;
            case 209: 
                loadBioSim(filePrefix + "plant_structure.html", "Plant Anatomy: Roots absorb, stems support, and leaves produce food.", "Dissect the flower to see the reproductive organs.");
                showDefaultControls(); break;
            case 210: 
                loadBioSim(filePrefix + "classification.html", "Classification: Organizing organisms into groups based on shared characteristics.", "Group these 10 organisms into their correct biological kingdoms.");
                showActionControls("Check Groups", "interact"); break;

            // --- INTERMEDIATE (211-224) ---
            case 211: 
                loadBioSim(filePrefix + "enzymes.html", "Enzymes (Advanced): Temperature and pH significantly impact enzyme shape and function.", "Find the 'Optimum pH' for this specific enzyme by graphing activity.");
                showEnzymeAdvancedControls(); break;
            case 212: 
                loadBioSim(filePrefix + "photosynthesis_rate.html", "Photosynthesis Rate: Factors like light intensity and CO2 availability change the rate of O2 production.", "Count the bubbles per minute at 10cm vs 50cm light distance.");
                showSliderControls("Light Intensity", 0, 100, 50, "setLight"); break;
            case 213: 
                loadBioSim(filePrefix + "respiration_rate.html", "Respiration Rate: Metabolism speed can be measured by oxygen consumption or CO2 production.", "Compare the respiration rate of yeast with and without sugar.");
                showStepControls(); break;
            case 214: 
                loadBioSim(filePrefix + "genetics.html", "Genetics: Inheritance follows predictable patterns discovered by Gregor Mendel.", "Use the Punnett square to predict the ratio of tall vs short plants.");
                showGeneticsControls(); break;
            case 215: 
                loadBioSim(filePrefix + "dna_extraction.html", "DNA Extraction: Breaking cell and nuclear membranes to release genetic material.", "Which step in the protocol precipitates the DNA strands?");
                showStepControls(); break;
            case 216: 
                loadBioSim(filePrefix + "mitosis.html", "Mitosis: Cell division for growth and repair. Results in two identical cells.", "Find a cell in Metaphase. What is happening to the chromosomes?");
                showStepControls(); break;
            case 217: 
                loadBioSim(filePrefix + "meiosis.html", "Meiosis: Production of haploid gametes. Increases genetic variation through crossing over.", "Observe Prophase I. Why is this stage important for evolution?");
                showStepControls(); break;
            case 218: 
                loadBioSim(filePrefix + "blood_test.html", "Blood Groups: Based on antigens on red blood cells. Crucial for safe transfusions.", "Test the unknown sample. Is it Type A, B, AB, or O?");
                showActionControls("Add Anti-Serum", "interact"); break;
            case 219: 
                loadBioSim(filePrefix + "digestive_system.html", "Digestion: Mechanical and chemical breakdown of food for absorption.", "Follow a starch molecule from the mouth to the small intestine.");
                showRotationControls(mode); break;
            case 220: 
                loadBioSim(filePrefix + "nervous_reaction.html", "Nervous System: Rapid coordination using electrical impulses and neurotransmitters.", "Test your reaction time. What is the average for a human?");
                showActionControls("Start Test", "interact"); break;
            case 221: 
                loadBioSim(filePrefix + "food_web.html", "Ecosystems: Complex webs of energy transfer between producers and consumers.", "Predict the impact of removing the primary consumer from the web.");
                showActionControls("Simulate Impact", "interact"); break;
            case 222: 
                loadBioSim(filePrefix + "population.html", "Populations: Growth is limited by resources (carrying capacity).", "Graph the population growth of bacteria over 24 hours.");
                showSliderControls("Resource Level", 0, 100, 50, "setResources"); break;
            case 223: 
                loadBioSim(filePrefix + "plant_transport.html", "Xylem & Phloem: Specialized tissues for transporting water and nutrients.", "Watch how dye moves through the celery stem over time.");
                showDefaultControls(); break;
            case 224: 
                loadBioSim(filePrefix + "transpiration.html", "Transpiration: Loss of water vapor from leaves, driving the 'transpiration pull'.", "Measure water loss at 10% vs 90% humidity.");
                showSliderControls("Humidity", 0, 100, 40, "setHumid"); break;

            // --- ADVANCED (225-237) ---
            case 225: 
                loadBioSim(filePrefix + "enzyme_kinetics.html", "Kinetics: Studying the rate of enzyme-catalyzed reactions (Michaelis-Menten).", "Determine the Vmax and Km for the provided enzyme-substrate pair.");
                showEnzymeAdvancedControls(); break;
            case 226: 
                loadBioSim(filePrefix + "dna_rep.html", "DNA Replication: Semi-conservative process ensuring genetic continuity.", "Identify the leading and lagging strands in the replication fork.");
                showStepControls(); break;
            case 227: 
                loadBioSim(filePrefix + "protein_synth.html", "Protein Synthesis: Decoding DNA into mRNA (Transcription) and mRNA into Protein (Translation).", "Synthesize a protein using the given DNA template.");
                showStepControls(); break;
            case 228: 
                loadBioSim(filePrefix + "biotech.html", "Biotechnology: Manipulating organisms or their components for human benefit.", "Insert the insulin gene into the bacterial plasmid.");
                showStepControls(); break;
            case 229: 
                loadBioSim(filePrefix + "gel.html", "Gel Electrophoresis: Separating DNA fragments by size using an electric field.", "Analyze the DNA fingerprint to solve the mystery.");
                showActionControls("Apply Voltage", "interact"); break;
            case 230: 
                loadBioSim(filePrefix + "immunology.html", "Immunology: Specific immune response using antibodies to target antigens.", "Simulate the primary vs secondary immune response.");
                showActionControls("Inject Pathogen", "interact"); break;
            case 231: 
                loadBioSim(filePrefix + "evolution.html", "Evolution: Change in allele frequencies in a population over time.", "Introduce a selection pressure and watch the population evolve.");
                showSliderControls("Selection Pressure", 0, 100, 20, "setPressure"); break;
            case 232: 
                loadBioSim(filePrefix + "ecology_field.html", "Field Study: Measuring biodiversity using quadrats and transects.", "Estimate the population of dandelions in the virtual field.");
                showDefaultControls(); break;
            case 233: 
                loadBioSim(filePrefix + "antibiotic.html", "Antibiotic Resistance: Evolution of bacteria that can survive antibiotic treatment.", "Test different antibiotics on the bacterial culture.");
                showActionControls("Apply Antibiotic", "interact"); break;
            case 234: 
                loadBioSim(filePrefix + "hormone.html", "Hormones: Chemical messengers regulating metabolism, growth, and homeostasis.", "Observe the blood glucose feedback loop using insulin and glucagon.");
                showActionControls("Trigger Stimulus", "interact"); break;
            case 235: 
                loadBioSim(filePrefix + "heart.html", "Cardiovascular: The heart and vessels working to deliver oxygen.", "Measure the change in heart rate during a virtual 5k run.");
                showHeartControls(); break;
            case 236: 
                loadBioSim(filePrefix + "lung.html", "Lung Capacity: Measuring tidal volume and vital capacity.", "How does exercise impact your lung capacity measurements?");
                showActionControls("Take Breath", "interact"); break;
            case 237: 
                loadBioSim(filePrefix + "bioinformatics.html", "Bioinformatics: Using computing to analyze biological data (DNA/Proteins).", "Align these two DNA sequences to find similarities.");
                showDefaultControls(); break;

            default:
                loadBioSim("placeholder_bio.html", "Interactive Biology Lab for " + experimentName + " is loading.", "Review the fundamental concepts of " + getIntent().getStringExtra("concept") + ".");
                showDefaultControls(); break;
        }
    }

    private void loadBioSim(String fileName, String description, String challenge) {
        webViewSim.loadUrl("file:///android_asset/sims/" + fileName);
        tvSimDescription.setText(description);
        tvChallenge.setText(challenge);
    }

    private void showCellControls(String mode) {
        View v = getLayoutInflater().inflate(R.layout.layout_pendulum_controls, controlPanel, false);
        SeekBar sb = v.findViewById(R.id.sbLength);
        TextView tv = v.findViewById(R.id.tvLengthVal);
        sb.setMax(100); sb.setProgress(50);
        if (mode.equals("3d")) {
            tv.setText("Rotation");
            sb.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { webViewSim.evaluateJavascript("setRotation(" + p + ")", null); } });
        } else {
            tv.setText("Zoom: 1x");
            sb.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { webViewSim.evaluateJavascript("setZoom(" + (0.5f + p/100f*1.5f) + ")", null); } });
        }
        ((MaterialButton)v.findViewById(R.id.btnStart)).setVisibility(View.GONE);
        ((MaterialButton)v.findViewById(R.id.btnReset)).setText("Switch Type");
        v.findViewById(R.id.btnReset).setOnClickListener(view -> webViewSim.evaluateJavascript("toggleType()", null));
        controlPanel.addView(v);
    }

    private void showMicroscopeControls() {
        View v = getLayoutInflater().inflate(R.layout.layout_projectile_controls, controlPanel, false);
        ((TextView)v.findViewById(R.id.tvAngleVal)).setText("Fine Focus");
        ((TextView)v.findViewById(R.id.tvSpeedVal)).setText("Magnification");
        SeekBar sbF = v.findViewById(R.id.sbAngle), sbM = v.findViewById(R.id.sbSpeed);
        sbF.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { webViewSim.evaluateJavascript("setFocus(" + p + ")", null); } });
        sbM.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { webViewSim.evaluateJavascript("setMag(" + p + ")", null); } });
        v.findViewById(R.id.btnLaunch).setVisibility(View.GONE);
        controlPanel.addView(v);
    }

    private void showStepControls() {
        View v = getLayoutInflater().inflate(R.layout.layout_pendulum_controls, controlPanel, false);
        v.findViewById(R.id.sbLength).setVisibility(View.GONE);
        v.findViewById(R.id.tvLengthVal).setVisibility(View.GONE);
        ((MaterialButton)v.findViewById(R.id.btnStart)).setText("Next Step");
        ((MaterialButton)v.findViewById(R.id.btnReset)).setText("Reset");
        v.findViewById(R.id.btnStart).setOnClickListener(view -> webViewSim.evaluateJavascript("nextStep()", null));
        v.findViewById(R.id.btnReset).setOnClickListener(view -> webViewSim.evaluateJavascript("resetSim()", null));
        controlPanel.addView(v);
    }

    private void showSliderControls(String label, int min, int max, int progress, String jsFunc) {
        View v = getLayoutInflater().inflate(R.layout.layout_pendulum_controls, controlPanel, false);
        SeekBar sb = v.findViewById(R.id.sbLength);
        TextView tv = v.findViewById(R.id.tvLengthVal);
        sb.setMax(max); sb.setProgress(progress);
        tv.setText(label + ": " + progress);
        sb.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                tv.setText(label + ": " + p);
                webViewSim.evaluateJavascript(jsFunc + "(" + p + ")", null);
            }
        });
        v.findViewById(R.id.btnStart).setVisibility(View.GONE);
        controlPanel.addView(v);
    }

    private void showRotationControls(String mode) {
        if (mode.equals("3d")) {
            showSliderControls("Rotate X-Axis", 0, 360, 0, "rotateModel");
        } else {
            showSliderControls("Scale", 10, 200, 100, "setScale");
        }
    }

    private void showActionControls(String btnText, String jsFunc) {
        MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        btn.setText(btnText);
        btn.setOnClickListener(v -> webViewSim.evaluateJavascript(jsFunc + "()", null));
        controlPanel.addView(btn);
    }

    private void showHeartControls() {
        View v = getLayoutInflater().inflate(R.layout.layout_wave_controls, controlPanel, false);
        SeekBar sb = v.findViewById(R.id.sbFrequency);
        TextView tv = v.findViewById(R.id.tvFreqVal);
        sb.setMax(200); sb.setProgress(70); tv.setText("Rate: 70 BPM");
        sb.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                tv.setText("Rate: " + p + " BPM");
                webViewSim.evaluateJavascript("setBPM(" + p + ")", null);
            }
        });
        controlPanel.addView(v);
    }

    private void showGeneticsControls() {
        View v = getLayoutInflater().inflate(R.layout.layout_atwood_controls, controlPanel, false);
        ((MaterialButton)v.findViewById(R.id.btnStart)).setText("Cross-Breed");
        v.findViewById(R.id.btnStart).setOnClickListener(view -> webViewSim.evaluateJavascript("calculate()", null));
        controlPanel.addView(v);
    }

    private void showEnzymeAdvancedControls() {
        View v = getLayoutInflater().inflate(R.layout.layout_projectile_controls, controlPanel, false);
        ((TextView)v.findViewById(R.id.tvAngleVal)).setText("Temperature (°C)");
        ((TextView)v.findViewById(R.id.tvSpeedVal)).setText("pH Level");
        SeekBar sbT = v.findViewById(R.id.sbAngle), sbP = v.findViewById(R.id.sbSpeed);
        sbT.setMax(100); sbT.setProgress(37);
        sbP.setMax(14); sbP.setProgress(7);
        sbT.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { webViewSim.evaluateJavascript("setTemp(" + p + ")", null); } });
        sbP.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { webViewSim.evaluateJavascript("setPH(" + p + ")", null); } });
        v.findViewById(R.id.btnLaunch).setVisibility(View.GONE);
        controlPanel.addView(v);
    }

    private void showDefaultControls() {
        MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        btn.setText("Reset Lab Visualization");
        btn.setOnClickListener(v -> webViewSim.loadUrl(webViewSim.getUrl()));
        controlPanel.addView(btn);
    }

    private static abstract class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
