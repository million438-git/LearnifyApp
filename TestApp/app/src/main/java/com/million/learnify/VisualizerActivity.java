package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class VisualizerActivity extends AppCompatActivity {

    private WebView webView;
    private TextView tvSimTitle;
    private LinearLayout simButtonsContainer;
    private TabLayout subjectTabs;
    private ChipGroup viewAngleGroup;
    private String currentSimUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Interactive Lab & Diagrams");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.webViewVisualizer);
        tvSimTitle = findViewById(R.id.tvSimTitle);
        simButtonsContainer = findViewById(R.id.simButtonsContainer);
        subjectTabs = findViewById(R.id.subjectTabs);
        viewAngleGroup = findViewById(R.id.viewAngleGroup);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient());

        subjectTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateSimList(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewAngleGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                String viewType = "system";
                if (id == R.id.chipParticle) viewType = "particle";
                else if (id == R.id.chipEquation) viewType = "equation";
                else if (id == R.id.chipRealLife) viewType = "real";
                
                webView.evaluateJavascript("if(typeof switchView === 'function') switchView('" + viewType + "');", null);
            }
        });

        // Load Physics by default
        updateSimList(0);
    }

    private void updateSimList(int position) {
        simButtonsContainer.removeAllViews();
        List<SimInfo> sims = new ArrayList<>();

        if (position == 0) { // Physics
            MaterialButton btnFullLab = (MaterialButton) LayoutInflater.from(this).inflate(R.layout.item_sim_button, simButtonsContainer, false);
            btnFullLab.setText("🚀 FULL PHYSICS LAB (40+)");
            btnFullLab.setTextColor(getResources().getColor(R.color.blue_600));
            btnFullLab.setStrokeColorResource(R.color.blue_600);
            btnFullLab.setOnClickListener(v -> startActivity(new Intent(this, PhysicsLabActivity.class)));
            simButtonsContainer.addView(btnFullLab);
        } else if (position == 1) { // Chemistry
            MaterialButton btnFullLab = (MaterialButton) LayoutInflater.from(this).inflate(R.layout.item_sim_button, simButtonsContainer, false);
            btnFullLab.setText("⚗️ FULL CHEMISTRY LAB (40+)");
            btnFullLab.setTextColor(getResources().getColor(R.color.purple_600));
            btnFullLab.setStrokeColorResource(R.color.purple_600);
            btnFullLab.setOnClickListener(v -> startActivity(new Intent(this, ChemistryLabActivity.class)));
            simButtonsContainer.addView(btnFullLab);
        } else if (position == 2) { // Biology
            MaterialButton btnFullLab = (MaterialButton) LayoutInflater.from(this).inflate(R.layout.item_sim_button, simButtonsContainer, false);
            btnFullLab.setText("🧬 FULL BIOLOGY LAB (35+)");
            btnFullLab.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnFullLab.setStrokeColorResource(android.R.color.holo_green_dark);
            btnFullLab.setOnClickListener(v -> startActivity(new Intent(this, BiologyLabActivity.class)));
            simButtonsContainer.addView(btnFullLab);
        }

        switch (position) {
            case 0: // Physics
                sims.add(new SimInfo("Pressure Lab 📍", "file:///android_asset/sims/pressure_lab.html"));
                sims.add(new SimInfo("Waves Lab 🌊", "file:///android_asset/sims/waves_lab.html"));
                sims.add(new SimInfo("Thermal Conductivity 🔥", "file:///android_asset/sims/heat_conduction_practical.html"));
                sims.add(new SimInfo("Newton's Laws ⚖️", "file:///android_asset/sims/force_laws.html"));
                sims.add(new SimInfo("Energy Transfer 🏀", "file:///android_asset/sims/energy_transfer.html"));
                sims.add(new SimInfo("Transformer Lab ⚡", "file:///android_asset/sims/diagram_transformer.html"));
                sims.add(new SimInfo("Electric Field ⚡", "file:///android_asset/sims/electric_field.html"));
                sims.add(new SimInfo("Circuit Builder 💡", "file:///android_asset/sims/circuit.html"));
                sims.add(new SimInfo("Motion Sim 🚗", "file:///android_asset/sims/motion.html"));
                break;
            case 1: // Chemistry
                sims.add(new SimInfo("Bohr Atom Model ⚛️", "file:///android_asset/sims/diagram_atom.html"));
                sims.add(new SimInfo("Periodic Table ⚛️", "file:///android_asset/sims/periodic.html"));
                sims.add(new SimInfo("PH Scale 🧪", "file:///android_asset/sims/ph_scale.html"));
                break;
            case 2: // Biology
                sims.add(new SimInfo("Human Eye Lab 👁️", "file:///android_asset/sims/diagram_eye.html"));
                sims.add(new SimInfo("Heart Beat ❤️", "file:///android_asset/sims/heart.html"));
                sims.add(new SimInfo("Cell Structure 🧬", "file:///android_asset/sims/cell.html"));
                break;
            case 3: // Math
                sims.add(new SimInfo("3D Geometry 📐", "file:///android_asset/sims/diagram_geometry.html"));
                sims.add(new SimInfo("Graph Plotter 📊", "file:///android_asset/sims/graph.html"));
                break;
            case 4: // English
                sims.add(new SimInfo("Tense Diagram 📖", "file:///android_asset/sims/diagram_tense.html"));
                sims.add(new SimInfo("Word Scramble 🔡", "file:///android_asset/sims/word_game.html"));
                break;
            case 5: // Around You
                sims.add(new SimInfo("Fridge (Thermo) ❄️", "file:///android_asset/sims/around_fridge.html"));
                sims.add(new SimInfo("Phone (Circuits) 📱", "file:///android_asset/sims/around_phone.html"));
                sims.add(new SimInfo("Wi-Fi (Waves) 📡", "file:///android_asset/sims/around_wifi.html"));
                sims.add(new SimInfo("Lightning (Static) ⚡", "file:///android_asset/sims/around_lightning.html"));
                break;
            case 6: // What If?
                sims.add(new SimInfo("Gravity Explorer 🪐", "file:///android_asset/sims/what_if_gravity.html"));
                sims.add(new SimInfo("Resistance Lab 🔋", "file:///android_asset/sims/what_if_resistance.html"));
                sims.add(new SimInfo("Heat Explorer 🌡️", "file:///android_asset/sims/what_if_temp.html"));
                break;
            case 7: // Real Data
                sims.add(new SimInfo("Cooling Curve 📉", "file:///android_asset/sims/data_cooling.html"));
                sims.add(new SimInfo("Motion Analysis 📊", "file:///android_asset/sims/data_motion.html"));
                sims.add(new SimInfo("Energy Usage ⚡", "file:///android_asset/sims/data_power.html"));
                break;
            case 8: // Micro-Animations
                sims.add(new SimInfo("Electron Flow ⚡", "file:///android_asset/sims/micro_electron.html"));
                sims.add(new SimInfo("Heat Conduction 🔥", "file:///android_asset/sims/micro_heat.html"));
                sims.add(new SimInfo("Wave Propagation 🌊", "file:///android_asset/sims/micro_wave.html"));
                break;
            case 9: // Real-World Errors
                sims.add(new SimInfo("Circuit Overheat ⚠️", "file:///android_asset/sims/error_circuit.html"));
                sims.add(new SimInfo("Brake Failure ⚠️", "file:///android_asset/sims/error_brakes.html"));
                sims.add(new SimInfo("Structural Failure ⚠️", "file:///android_asset/sims/error_material.html"));
                break;
        }

        for (SimInfo sim : sims) {
            MaterialButton btn = (MaterialButton) LayoutInflater.from(this).inflate(R.layout.item_sim_button, simButtonsContainer, false);
            btn.setText(sim.title);
            btn.setOnClickListener(v -> loadSimulation(sim.title, sim.url));
            simButtonsContainer.addView(btn);
        }
        
        if (!sims.isEmpty()) {
            loadSimulation(sims.get(0).title, sims.get(0).url);
        }
    }

    private void loadSimulation(String title, String url) {
        tvSimTitle.setText(title);
        currentSimUrl = url;
        webView.loadUrl(url);
    }

    private static class SimInfo {
        String title, url;
        SimInfo(String t, String u) { title = t; url = u; }
    }
}
