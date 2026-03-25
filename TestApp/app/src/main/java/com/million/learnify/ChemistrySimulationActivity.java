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

public class ChemistrySimulationActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_chemistry_simulation);

        experimentId = getIntent().getIntExtra("experiment_id", 301);
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
        webSettings.setAllowFileAccess(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webViewSim.setWebViewClient(new WebViewClient());
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mode = "experiments";
                if (tab.getPosition() == 1) mode = "data";
                else if (tab.getPosition() == 2) mode = "3d";
                setupSimulation(mode);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSimulation(String mode) {
        controlPanel.removeAllViews();
        // If 3D mode is selected, we load the specialized 3D viewer
        if (mode.equals("3d")) {
            loadChemSim("3d_molecule_viewer.html", "3D Molecular Structure: Examine the atomic arrangement in high detail.", "Rotate and zoom to identify the bond angles.");
            showActionControls("Toggle Atomic Labels", "toggleLabels");
            return;
        }

        String filePrefix = mode.equals("data") ? "data_" : "";
        
        // Map Experiment IDs to specific simulation logic
        if (experimentId >= 301 && experimentId <= 310) {
            handleBeginnerLabs(filePrefix);
        } else if (experimentId >= 311 && experimentId <= 324) {
            handleIntermediateLabs(filePrefix);
        } else {
            handleAdvancedLabs(filePrefix);
        }
    }

    private void handleBeginnerLabs(String prefix) {
        switch (experimentId) {
            case 303: // States of Matter
                loadChemSim(prefix + "states.html", "Kinetic Theory: Observe particle motion in different phases.", "Increase heat to see the transition from Solid to Liquid.");
                showSliderControls("Heat Energy", 0, 100, 20, "setEnergy");
                break;
            case 307: // pH Testing
                loadChemSim(prefix + "ph_scale.html", "pH Indicators: Test common substances using Litmus/Universal indicators.", "Identify the pH of the unknown solution.");
                showSliderControls("Solution Sample", 1, 10, 5, "changeSample");
                break;
            default:
                loadChemSim(prefix + "lab_basic.html", experimentName + ": Foundational chemistry principles and observation.", "Follow the step-by-step procedure to complete the lab.");
                showDefaultControls();
                break;
        }
    }

    private void handleIntermediateLabs(String prefix) {
        switch (experimentId) {
            case 312: // Titration
                loadChemSim(prefix + "titration.html", "Quantitative Analysis: Neutralization of Acid and Base.", "Determine the concentration of HCl using 0.1M NaOH.");
                showTitrationControls();
                break;
            case 313: // Reaction Rate
                loadChemSim(prefix + "rates.html", "Chemical Kinetics: Factors like Temperature and Concentration.", "Measure the time taken for the cross to disappear.");
                showSliderControls("Concentration (M)", 1, 10, 1, "setConc");
                break;
            default:
                loadChemSim(prefix + "lab_intermediate.html", experimentName + ": Application of chemical laws and measurements.", "Record your observations in the data table.");
                showDefaultControls();
                break;
        }
    }

    private void handleAdvancedLabs(String prefix) {
        switch (experimentId) {
            case 329: // Organic
                loadChemSim(prefix + "organic_reactions.html", "Hydrocarbons: Addition and Substitution reactions.", "Synthesize Ethanol from Ethene via hydration.");
                showActionControls("Add Reagent", "addReagent");
                break;
            case 332: // Chromatography
                loadChemSim(prefix + "chromatography.html", "Separation Techniques: Differential solubility in moving phase.", "Calculate the Rf value for each pigment.");
                showActionControls("Start Flow", "startFlow");
                break;
            default:
                loadChemSim(prefix + "lab_advanced.html", experimentName + ": Complex analytical and synthesis procedures.", "Complete the advanced simulation and analyze results.");
                showDefaultControls();
                break;
        }
    }

    private void loadChemSim(String fileName, String description, String challenge) {
        webViewSim.loadUrl("file:///android_asset/sims/" + fileName);
        tvSimDescription.setText(description);
        tvChallenge.setText(challenge);
    }

    private void showTitrationControls() {
        View v = getLayoutInflater().inflate(R.layout.layout_pendulum_controls, controlPanel, false);
        SeekBar sb = v.findViewById(R.id.sbLength);
        TextView tv = v.findViewById(R.id.tvLengthVal);
        sb.setMax(50); sb.setProgress(0);
        tv.setText("Volume: 0 ml");
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                tv.setText("Volume: " + p + " ml");
                webViewSim.evaluateJavascript("addVolume(" + p + ")", null);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        MaterialButton btn = v.findViewById(R.id.btnStart);
        btn.setText("Dropwise");
        btn.setOnClickListener(view -> webViewSim.evaluateJavascript("addDrop()", null));
        v.findViewById(R.id.btnReset).setOnClickListener(view -> {
            sb.setProgress(0);
            webViewSim.evaluateJavascript("resetSim()", null);
        });
        controlPanel.addView(v);
    }

    private void showSliderControls(String label, int min, int max, int progress, String jsFunc) {
        View v = getLayoutInflater().inflate(R.layout.layout_pendulum_controls, controlPanel, false);
        SeekBar sb = v.findViewById(R.id.sbLength);
        TextView tv = v.findViewById(R.id.tvLengthVal);
        sb.setMax(max); sb.setProgress(progress);
        tv.setText(label + ": " + progress);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                tv.setText(label + ": " + p);
                webViewSim.evaluateJavascript(jsFunc + "(" + p + ")", null);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        v.findViewById(R.id.btnStart).setVisibility(View.GONE);
        controlPanel.addView(v);
    }

    private void showActionControls(String btnText, String jsFunc) {
        MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        btn.setText(btnText);
        btn.setOnClickListener(v -> webViewSim.evaluateJavascript(jsFunc + "()", null));
        controlPanel.addView(btn);
    }

    private void showDefaultControls() {
        MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        btn.setText("Reset Experiment");
        btn.setOnClickListener(v -> webViewSim.loadUrl(webViewSim.getUrl()));
        controlPanel.addView(btn);
    }
}
