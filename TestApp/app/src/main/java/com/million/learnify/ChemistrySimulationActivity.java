package com.million.learnify;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class ChemistrySimulationActivity extends AppCompatActivity {

    private int experimentId;
    private WebView webViewSim;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chemistry_simulation);

        experimentId = getIntent().getIntExtra("experiment_id", 301);

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
            case 301: fileName = "laboratory_safety-and-equipment-lab.html"; break;
            case 302: fileName = "physical-and-chemical-change-lab.html"; break;
            case 303: fileName = "states-of-matter-lab.html"; break;
            case 304: fileName = "density-experiment-lab.html"; break;
            case 305: fileName = "solubility-lab.html"; break;
            case 306: fileName = "mixtures-and-separation-lab.html"; break;
            case 307: fileName = "ph-testing-lab.html"; break;
            case 308: fileName = "acids-and-bases-intro-lab.html"; break;
            case 309: fileName = "simple-chemical-reactions-lab.html"; break;
            case 310: fileName = "temperature-change-lab.html"; break;
            case 311: fileName = "ph-scale-investigations-lab.html"; break;
            case 312: fileName = "titration-lab.html"; break;
            case 313: fileName = "reaction-rate-lab.html"; break;
            case 314: fileName = "catalysts-lab.html"; break;
            case 315: fileName = "gas-collection-lab.html"; break;
            case 316: fileName = "law-of-conservation-of-mass-lab.html"; break;
            case 317: fileName = "electrolysis-lab.html"; break;
            case 318: fileName = "periodic-table-trends-lab.html"; break;
            case 319: fileName = "metal-reactivity-lab.html"; break;
            case 320: fileName = "ionic-vs-covalent-compounds-lab.html"; break;
            case 321: fileName = "solutions-concentration-lab.html"; break;
            case 322: fileName = "precipitation-reaction-lab.html"; break;
            case 323: fileName = "heat-of-reaction-lab.html"; break;
            case 324: fileName = "water-hardness-lab.html"; break;
            case 325: fileName = "advanced-titration-redox.html"; break;
            case 326: fileName = "chemical-equilibrium-lab.html"; break;
            case 327: fileName = "le-chatelier-principle-lab.html"; break;
            case 328: fileName = "buffer-solutions-lab.html"; break;
            case 329: fileName = "organic-chemistry-lab.html"; break;
            case 330: fileName = "alcohol-reaction-lab.html"; break;
            case 331: fileName = "polymerization-lab.html"; break;
            case 332: fileName = "chromatography-lab.html"; break;
            case 333: fileName = "spectroscopy-simulation-lab.html"; break;
            case 334: fileName = "electrochemical-cells-lab.html"; break;
            case 335: fileName = "corrosion-lab.html"; break;
            case 336: fileName = "thermochemistry-lab.html"; break;
            case 337: fileName = "kinetics-lab.html"; break;
            case 338: fileName = "environmental-chemistry-lab.html"; break;
            case 339: fileName = "pharmaceutical-chemistry-lab.html"; break;
            case 340: fileName = "nanotechnology-intro-lab.html"; break;
            default: fileName = "titration-lab.html"; break;
        }
        webViewSim.loadUrl("file:///android_asset/InteractiveLab/Chemistry Lab/" + fileName);
    }
}
