package com.million.learnify;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class PhysicsSimulationActivity extends AppCompatActivity {

    private int experimentId;
    private WebView webViewSim;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physics_simulation);

        experimentId = getIntent().getIntExtra("experiment_id", 1);

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
            case 1: fileName = "projectile_motion.html"; break;
            case 2: fileName = "pendulum.html"; break;
            case 3: fileName = "spring-mass_system.html"; break;
            case 4: fileName = "newtons_cradle.html"; break;
            case 5: fileName = "inclined_plane.html"; break;
            case 6: fileName = "atwood_machine.html"; break;
            case 7: fileName = "rocket_thrust.html"; break;
            case 8: fileName = "orbital_motion.html"; break;
            case 9: fileName = "conservation_of_energy.html"; break;
            case 10: fileName = "collisions_1D_and 2D.html"; break;
            case 11: fileName = "wave_on_a_spring.html"; break;
            case 12: fileName = "sound_waves.html"; break;
            case 13: fileName = "doppler_effect.html"; break;
            case 14: fileName = "interference_double_slit.html"; break;
            case 15: fileName = "resonance_tube.html"; break;
            case 16: fileName = "fourier_synthesis.html"; break;
            case 17: fileName = "ohms_law.html"; break;
            case 18: fileName = "rc_circuit.html"; break;
            case 19: fileName = "magnatic_field_of_a_wire.html"; break;
            case 20: fileName = "electric_field_lines.html"; break;
            case 21: fileName = "lorentz_force.html"; break;
            case 22: fileName = "faradays_law.html"; break;
            case 23: fileName = "ac_circuits.html"; break;
            case 24: fileName = "motor-generator.html"; break;
            case 25: fileName = "snells_law.html"; break;
            case 26: fileName = "lens-ray_diagram.html"; break;
            case 27: fileName = "mirror-ray_diagram.html"; break;
            case 28: fileName = "prism-dispersion.html"; break;
            case 29: fileName = "diffraction_grating.html"; break;
            case 30: fileName = "telescope-simulator.html"; break;
            case 31: fileName = "ideal-gas_law.html"; break;
            case 32: fileName = "heat-engine_or_carnot.html"; break;
            case 33: fileName = "specific-heat_capacity.html"; break;
            case 34: fileName = "thermal-expansion.html"; break;
            case 35: fileName = "phase-changes.html"; break;
            case 36: fileName = "photoelectric-effect.html"; break;
            case 37: fileName = "bohr-model-hydrogen.html"; break;
            case 38: fileName = "radioactive-decay.html"; break;
            case 39: fileName = "nuclear-fusion.html"; break;
            case 40: fileName = "relativity.html"; break;
            default: fileName = "projectile_motion.html"; break;
        }
        webViewSim.loadUrl("file:///android_asset/InteractiveLab/Physics Lab/" + fileName);
    }
}
