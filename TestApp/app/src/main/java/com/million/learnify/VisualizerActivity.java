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
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class VisualizerActivity extends AppCompatActivity {

    private WebView webView;
    private TextView tvSimTitle, tvLabInstructions;
    private LinearLayout simButtonsContainer;
    private TabLayout subjectTabs;
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
        tvLabInstructions = findViewById(R.id.tvLabInstructions);
        simButtonsContainer = findViewById(R.id.simButtonsContainer);
        subjectTabs = findViewById(R.id.subjectTabs);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setAllowFileAccess(true);
        webView.setWebViewClient(new WebViewClient());

        subjectTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateSimList(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Load Physics by default
        updateSimList(0);
    }

    private void updateSimList(int position) {
        simButtonsContainer.removeAllViews();
        List<SimInfo> sims = new ArrayList<>();

        switch (position) {
            case 0: // Physics - ONLY FULL LAB BUTTON
                addFullLabButton("🚀 FULL PHYSICS LAB (40+)", R.color.blue_600, PhysicsLabActivity.class);
                break;
            case 1: // Chemistry - ONLY FULL LAB BUTTON
                addFullLabButton("⚗️ FULL CHEMISTRY LAB (40+)", R.color.purple_600, ChemistryLabActivity.class);
                break;
            case 2: // Biology - ONLY FULL LAB BUTTON
                addFullLabButton("🧬 FULL BIOLOGY LAB (35+)", android.R.color.holo_green_dark, BiologyLabActivity.class);
                break;
            case 3: // Mathematics
                sims.add(new SimInfo("3D Geometry 📐", "file:///android_asset/InteractiveLab/Mathematics Lab/geometry.html"));
                sims.add(new SimInfo("Trigonometry 📐", "file:///android_asset/InteractiveLab/Mathematics Lab/trig.html"));
                sims.add(new SimInfo("Graph Plotter 📊", "file:///android_asset/InteractiveLab/Mathematics Lab/graphs.html"));
                break;
            case 4: // English
                sims.add(new SimInfo("Grammar Lab 📖", "file:///android_asset/InteractiveLab/English Lab/grammar.html"));
                sims.add(new SimInfo("Vocabulary 🔡", "file:///android_asset/InteractiveLab/English Lab/vocabulary.html"));
                break;
            case 5: // Around You
                sims.add(new SimInfo("Smart City 🏙️", "file:///android_asset/InteractiveLab/Around-You Lab/city.html"));
                sims.add(new SimInfo("Eco House 🏠", "file:///android_asset/InteractiveLab/Around-You Lab/house.html"));
                break;
            case 6: // What If?
                sims.add(new SimInfo("No Gravity 🌌", "file:///android_asset/InteractiveLab/What-If Lab/no_gravity.html"));
                sims.add(new SimInfo("Infinite Energy ⚡", "file:///android_asset/InteractiveLab/What-If Lab/infinite_energy.html"));
                break;
            case 7: // Real Data
                sims.add(new SimInfo("Live Weather 🌦️", "file:///android_asset/InteractiveLab/Real-Data Lab/weather.html"));
                break;
            case 8: // Micro-Animations
                sims.add(new SimInfo("Atomic Vibrations ⚛️", "file:///android_asset/InteractiveLab/Micro-Animations Lab/atoms.html"));
                break;
            case 9: // Real-World Errors
                sims.add(new SimInfo("Bridge Collapse 🌉", "file:///android_asset/InteractiveLab/Real-World-Error Lab/bridge.html"));
                sims.add(new SimInfo("Circuit Short 💥", "file:///android_asset/InteractiveLab/Real-World-Error Lab/circuit_error.html"));
                break;
        }

        for (SimInfo sim : sims) {
            MaterialButton btn = (MaterialButton) LayoutInflater.from(this).inflate(R.layout.item_sim_button, simButtonsContainer, false);
            btn.setText(sim.title);
            btn.setOnClickListener(v -> loadSimulation(sim.title, sim.url));
            simButtonsContainer.addView(btn);
        }
        
        if (position <= 2) {
            // For core subjects, we've only added a full lab button, so we clear the view
            tvSimTitle.setText("Interactive Lab");
            webView.loadUrl("about:blank");
            tvLabInstructions.setText("Select 'FULL LAB' to access the complete interactive environment.");
        } else if (!sims.isEmpty()) {
            loadSimulation(sims.get(0).title, sims.get(0).url);
        } else {
            tvSimTitle.setText("Coming Soon!");
            webView.loadUrl("about:blank");
            tvLabInstructions.setText("We are currently preparing interactive simulations for this category. Stay tuned!");
        }
    }

    private void addFullLabButton(String text, int colorRes, Class<?> activityClass) {
        MaterialButton btnFullLab = (MaterialButton) LayoutInflater.from(this).inflate(R.layout.item_sim_button, simButtonsContainer, false);
        btnFullLab.setText(text);
        btnFullLab.setTextColor(getResources().getColor(colorRes));
        btnFullLab.setStrokeColorResource(colorRes);
        btnFullLab.setOnClickListener(v -> startActivity(new Intent(this, activityClass)));
        simButtonsContainer.addView(btnFullLab);
    }

    private void loadSimulation(String title, String url) {
        tvSimTitle.setText(title);
        currentSimUrl = url;
        webView.loadUrl(url);
        updateInstructions(title);
    }

    private void updateInstructions(String title) {
        String msg = "Take control of the " + title + " simulation. Adjust the parameters and observe the real-time changes in the environment.";
        if (title.contains("Physics")) msg += " Focus on how forces and energy interact.";
        else if (title.contains("Chemistry")) msg += " Observe chemical changes and reaction dynamics.";
        else if (title.contains("Biology")) msg += " Explore the intricate structures and processes of life.";
        tvLabInstructions.setText(msg);
    }

    private static class SimInfo {
        String title, url;
        SimInfo(String t, String u) { title = t; url = u; }
    }
}
