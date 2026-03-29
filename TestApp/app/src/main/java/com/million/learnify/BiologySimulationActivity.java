package com.million.learnify;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class BiologySimulationActivity extends AppCompatActivity {

    private int experimentId;
    private WebView webViewSim;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biology_simulation);

        experimentId = getIntent().getIntExtra("experiment_id", 201);

        webViewSim = findViewById(R.id.webViewSim);
        setupWebView();
        setupSimulation();
    }

    private void setupWebView() {
        WebSettings webSettings = webViewSim.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        
        webViewSim.setWebViewClient(new WebViewClient());
        webViewSim.setBackgroundColor(0x00000000);
    }

    private void setupSimulation() {
        String fileName;
        switch (experimentId) {
            case 201: fileName = "microscope_introduction.html"; break;
            case 202: fileName = "cell_observation.html"; break;
            case 203: fileName = "diffusion_and_osmosis.html"; break;
            case 204: fileName = "food_test_lab.html"; break;
            case 205: fileName = "photosynthesis_basic.html"; break;
            case 206: fileName = "respiration_in_living_organisms.html"; break;
            case 207: fileName = "enzyme_activity.html"; break;
            case 208: fileName = "human_body_systems_intro.html"; break;
            case 209: fileName = "plant_structure_lab.html"; break;
            case 210: fileName = "simple_classification.html"; break;
            case 211: fileName = "enzyme_activity_advanced.html"; break;
            case 212: fileName = "photosynthesis_rate.html"; break;
            case 213: fileName = "respiration_rate.html"; break;
            case 214: fileName = "genetics_simulation.html"; break;
            case 215: fileName = "dna_extraction.html"; break;
            case 216: fileName = "mitosis_observation.html"; break;
            case 217: fileName = "meiosis_simulation.html"; break;
            case 218: fileName = "blood_group_testing.html"; break;
            case 219: fileName = "human_digestive_system.html"; break;
            case 220: fileName = "nervous_system_reaction.html"; break;
            case 221: fileName = "ecosystem_interaction.html"; break;
            case 222: fileName = "population_growth.html"; break;
            case 223: fileName = "water_transport_in_plants.html"; break;
            case 224: fileName = "transpiration_lab.html"; break;
            case 225: fileName = "enzyme_kinetics.html"; break;
            case 226: fileName = "dna_replication_model.html"; break;
            case 227: fileName = "protein_synthesis.html"; break;
            case 228: fileName = "biotechnology_lab.html"; break;
            case 229: fileName = "gel_electrophoresis.html"; break;
            case 230: fileName = "immunology_lab.html"; break;
            case 231: fileName = "evolution_simulation.html"; break;
            case 232: fileName = "ecology_field_study.html"; break;
            case 233: fileName = "antibiotic_resistance.html"; break;
            case 234: fileName = "hormone_action_lab.html"; break;
            case 235: fileName = "cardiovascular_system.html"; break;
            case 236: fileName = "lung_capacity_lab.html"; break;
            case 237: fileName = "bioinformatics_lab.html"; break;
            default: fileName = "cell_observation.html"; break;
        }
        webViewSim.loadUrl("file:///android_asset/InteractiveLab/Biology Lab/" + fileName);
    }
}
