package com.million.learnify;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
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
    private FrameLayout flSubjectLogoContainer;
    private TextView tvSubjectLogoEmoji;
    private View visualizerRoot;

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

        visualizerRoot = findViewById(R.id.visualizerRoot);
        webView = findViewById(R.id.webViewVisualizer);
        tvSimTitle = findViewById(R.id.tvSimTitle);
        tvLabInstructions = findViewById(R.id.tvLabInstructions);
        simButtonsContainer = findViewById(R.id.simButtonsContainer);
        subjectTabs = findViewById(R.id.subjectTabs);
        flSubjectLogoContainer = findViewById(R.id.flSubjectLogoContainer);

        // Remove any existing views and replace with a large TextView for the subject emoji
        flSubjectLogoContainer.removeAllViews();
        tvSubjectLogoEmoji = new TextView(this);
        tvSubjectLogoEmoji.setTextSize(120);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.CENTER;
        tvSubjectLogoEmoji.setLayoutParams(params);
        tvSubjectLogoEmoji.setAlpha(0.8f);
        flSubjectLogoContainer.addView(tvSubjectLogoEmoji);

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
        
        // Default visibility: Show logo, hide WebView
        flSubjectLogoContainer.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);

        int startColor, centerColor, endColor;
        String subjectEmoji = "📖";

        switch (position) {
            case 0: // Physics - Deep Sky Mix
                startColor = Color.parseColor("#E0F2FE");
                centerColor = Color.parseColor("#BAE6FD");
                endColor = Color.parseColor("#7DD3FC");
                subjectEmoji = "⚛️";
                addFullLabButton("🚀 FULL PHYSICS LAB (40+)", R.color.blue_600, PhysicsLabActivity.class);
                break;
            case 1: // Chemistry - Lavender Mist Mix
                startColor = Color.parseColor("#F3E8FF");
                centerColor = Color.parseColor("#E9D5FF");
                endColor = Color.parseColor("#D8B4FE");
                subjectEmoji = "🧪";
                addFullLabButton("⚗️ FULL CHEMISTRY LAB (40+)", R.color.purple_600, ChemistryLabActivity.class);
                break;
            case 2: // Biology - Emerald Forest Mix
                startColor = Color.parseColor("#DCFCE7");
                centerColor = Color.parseColor("#BBF7D0");
                endColor = Color.parseColor("#86EFAC");
                subjectEmoji = "🧬";
                addFullLabButton("🧬 FULL BIOLOGY LAB (35+)", android.R.color.holo_green_dark, BiologyLabActivity.class);
                break;
            case 3: // Mathematics - Golden Amber Mix
                startColor = Color.parseColor("#FEF3C7");
                centerColor = Color.parseColor("#FDE68A");
                endColor = Color.parseColor("#FCD34D");
                subjectEmoji = "📐";
                sims.add(new SimInfo("3D Geometry 📐", "file:///android_asset/InteractiveLab/Mathematics Lab/geometry.html"));
                sims.add(new SimInfo("Trigonometry 📐", "file:///android_asset/InteractiveLab/Mathematics Lab/trig.html"));
                sims.add(new SimInfo("Graph Plotter 📊", "file:///android_asset/InteractiveLab/Mathematics Lab/graphs.html"));
                break;
            case 4: // English - Sunset Glow Mix
                startColor = Color.parseColor("#FFEDD5");
                centerColor = Color.parseColor("#FED7AA");
                endColor = Color.parseColor("#FDBA8C");
                subjectEmoji = "📖";
                sims.add(new SimInfo("Grammar Lab 📖", "file:///android_asset/InteractiveLab/English Lab/grammar.html"));
                sims.add(new SimInfo("Vocabulary 🔡", "file:///android_asset/InteractiveLab/English Lab/vocabulary.html"));
                break;
            case 5: // Around You - Urban Slate Mix
                startColor = Color.parseColor("#F1F5F9");
                centerColor = Color.parseColor("#E2E8F0");
                endColor = Color.parseColor("#CBD5E1");
                subjectEmoji = "🏙️";
                sims.add(new SimInfo("Smart City 🏙️", "file:///android_asset/InteractiveLab/Around-You Lab/city.html"));
                sims.add(new SimInfo("Eco House 🏠", "file:///android_asset/InteractiveLab/Around-You Lab/house.html"));
                break;
            case 6: // What If? - Cosmic Fuchsia Mix
                startColor = Color.parseColor("#FAE8FF");
                centerColor = Color.parseColor("#F5D0FE");
                endColor = Color.parseColor("#F0ABFC");
                subjectEmoji = "🤔";
                sims.add(new SimInfo("No Gravity 🌌", "file:///android_asset/InteractiveLab/What-If Lab/no_gravity.html"));
                sims.add(new SimInfo("Infinite Energy ⚡", "file:///android_asset/InteractiveLab/What-If Lab/infinite_energy.html"));
                break;
            case 7: // Real Data - Crystal Emerald Mix
                startColor = Color.parseColor("#ECFDF5");
                centerColor = Color.parseColor("#D1FAE5");
                endColor = Color.parseColor("#A7F3D0");
                subjectEmoji = "📊";
                sims.add(new SimInfo("Live Weather 🌦️", "file:///android_asset/InteractiveLab/Real-Data Lab/weather.html"));
                break;
            case 8: // Micro-Animations - Rose Petal Mix
                startColor = Color.parseColor("#FFF1F2");
                centerColor = Color.parseColor("#FFE4E6");
                endColor = Color.parseColor("#FECDD3");
                subjectEmoji = "🎥";
                sims.add(new SimInfo("Atomic Vibrations ⚛️", "file:///android_asset/InteractiveLab/Micro-Animations Lab/atoms.html"));
                break;
            case 9: // Real-World Errors - Warning Crimson Mix
                startColor = Color.parseColor("#FEF2F2");
                centerColor = Color.parseColor("#FEE2E2");
                endColor = Color.parseColor("#FECACA");
                subjectEmoji = "⚠️";
                sims.add(new SimInfo("Bridge Collapse 🌉", "file:///android_asset/InteractiveLab/Real-World-Error Lab/bridge.html"));
                sims.add(new SimInfo("Circuit Short 💥", "file:///android_asset/InteractiveLab/Real-World-Error Lab/circuit_error.html"));
                break;
            default:
                startColor = Color.parseColor("#FFFFFF");
                centerColor = Color.parseColor("#F8FAFC");
                endColor = Color.parseColor("#F1F5F9");
        }

        // Create and apply a vibrant mixing gradient background to the entire page
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[] {startColor, centerColor, endColor}
        );
        gradient.setCornerRadius(0f);
        visualizerRoot.setBackground(gradient);
        
        // Ensure the central logo matches the beautiful mix
        flSubjectLogoContainer.setBackground(null); 
        tvSubjectLogoEmoji.setText(subjectEmoji);

        for (SimInfo sim : sims) {
            MaterialButton btn = (MaterialButton) LayoutInflater.from(this).inflate(R.layout.item_sim_button, simButtonsContainer, false);
            btn.setText(sim.title);
            btn.setOnClickListener(v -> loadSimulation(sim.title, sim.url));
            simButtonsContainer.addView(btn);
        }
        
        if (position <= 2) {
            tvSimTitle.setText("Interactive Lab");
            webView.loadUrl("about:blank");
            tvLabInstructions.setText("Select 'FULL LAB' to access the complete interactive environment.");
        } else if (!sims.isEmpty()) {
            tvSimTitle.setText("Select a Simulation");
            tvLabInstructions.setText("Choose a simulation from the list above to begin.");
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
        flSubjectLogoContainer.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
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
