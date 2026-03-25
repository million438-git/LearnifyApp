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

import java.util.Locale;

public class PhysicsSimulationActivity extends AppCompatActivity {

    private int experimentId;
    private String experimentName;
    private ProjectileView projectileView;
    private PendulumView pendulumView;
    private SpringMassView springMassView;
    private CollisionView collisionView;
    private InclinedPlaneView inclinedPlaneView;
    private OpticsView opticsView;
    private GasLawView gasLawView;
    private WaveView waveView;
    private BohrModelView bohrModelView;
    private AtwoodView atwoodView;
    private ModernPhysicsView modernPhysicsView;
    private WebView webViewSim;
    private LinearLayout controlPanel;
    private TextView tvSimDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physics_simulation);

        experimentId = getIntent().getIntExtra("experiment_id", 1);
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
        setupSimulation();
    }

    private void initViews() {
        projectileView = findViewById(R.id.projectileView);
        pendulumView = findViewById(R.id.pendulumView);
        springMassView = findViewById(R.id.springMassView);
        collisionView = findViewById(R.id.collisionView);
        inclinedPlaneView = findViewById(R.id.inclinedPlaneView);
        opticsView = findViewById(R.id.opticsView);
        gasLawView = findViewById(R.id.gasLawView);
        waveView = findViewById(R.id.waveView);
        bohrModelView = findViewById(R.id.bohrModelView);
        atwoodView = findViewById(R.id.atwoodView);
        modernPhysicsView = findViewById(R.id.modernPhysicsView);
        webViewSim = findViewById(R.id.webViewSim);
        controlPanel = findViewById(R.id.controlPanel);
        tvSimDescription = findViewById(R.id.tvSimDescription);
    }

    private void setupWebView() {
        WebSettings webSettings = webViewSim.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webViewSim.setWebViewClient(new WebViewClient());
    }

    private void setupSimulation() {
        resetVisibility();
        
        switch (experimentId) {
            // --- 1. Mechanics (1-10) ---
            case 1: showProjectileControls(); break;
            case 2: showPendulumControls(); break;
            case 3: showSpringMassControls(); break;
            case 4: loadWebSim("force_laws.html", "Newton's Cradle: Momentum transfer and energy conservation."); break;
            case 5: showInclinedPlaneControls(); break;
            case 6: showAtwoodControls(); break;
            case 7: showProjectileControls(); tvSimDescription.setText("Rocket Thrust: Newton's 3rd Law application."); break;
            case 8: loadWebSim("what_if_gravity.html", "Orbital Motion: Gravity and centripetal force."); break;
            case 9: loadWebSim("energy_transfer.html", "Conservation of Energy: Potential to Kinetic."); break;
            case 10: showCollisionControls(); break;

            // --- 2. Waves (11-16) ---
            case 11: showWaveControls(); break;
            case 12: showWaveControls(); tvSimDescription.setText("Sound Waves: Longitudinal propagation."); break;
            case 13: showWaveControls(); tvSimDescription.setText("Doppler Effect: Frequency shift."); break;
            case 14: loadWebSim("waves_lab.html", "Double Slit: Interference patterns."); break;
            case 15: loadWebSim("waves_lab.html", "Resonance Tube: Sound resonance."); break;
            case 16: showWaveControls(); tvSimDescription.setText("Fourier Synthesis: Harmonic superposition."); break;

            // --- 3. E&M (17-24) ---
            case 17: loadWebSim("circuit.html", "Ohm's Law: V = IR relationship."); break;
            case 18: loadWebSim("circuit.html", "RC Circuit: Capacitor dynamics."); break;
            case 19: loadWebSim("electric_field.html", "Magnetic Field: Biot-Savart law."); break;
            case 20: loadWebSim("electric_field.html", "Electric Field Lines: Coulomb's law."); break;
            case 21: loadWebSim("electric_field.html", "Lorentz Force: Particle trajectory."); break;
            case 22: loadWebSim("diagram_transformer.html", "Induction: Faraday's law."); break;
            case 23: loadWebSim("circuit.html", "AC Circuits: Impedance and resonance."); break;
            case 24: loadWebSim("diagram_transformer.html", "Motor/Generator: Electromechanics."); break;

            // --- 4. Optics (25-30) ---
            case 25: showOpticsControls(1); break; // Snell
            case 26: showOpticsControls(2); break; // Lens
            case 27: showOpticsControls(2); tvSimDescription.setText("Mirror Ray Diagram: Image formation."); break;
            case 28: showOpticsControls(1); tvSimDescription.setText("Prism Dispersion: Light spectrum."); break;
            case 29: loadWebSim("waves_lab.html", "Diffraction Grating: Interference."); break;
            case 30: showOpticsControls(2); tvSimDescription.setText("Telescope: Magnification optics."); break;

            // --- 5. Thermo (31-35) ---
            case 31: showGasLawControls(); break;
            case 32: loadWebSim("pressure_lab.html", "Heat Engine: Efficiency and cycles."); break;
            case 33: loadWebSim("heat_conduction_practical.html", "Heat Capacity: Calorimetry."); break;
            case 34: loadWebSim("heat_conduction_practical.html", "Thermal Expansion."); break;
            case 35: loadWebSim("pressure_lab.html", "Phase Changes: Latent heat."); break;

            // --- 6. Modern (36-40) ---
            case 36: showModernControls(1); break; // Photoelectric
            case 37: showBohrControls(); break; // Bohr
            case 38: showModernControls(2); break; // Decay
            case 39: showModernControls(1); tvSimDescription.setText("Nuclear Fusion: Energy production."); break;
            case 40: showModernControls(3); break; // Relativity

            default: showPlaceholder(); break;
        }
    }

    private void resetVisibility() {
        projectileView.setVisibility(View.GONE);
        pendulumView.setVisibility(View.GONE);
        springMassView.setVisibility(View.GONE);
        collisionView.setVisibility(View.GONE);
        inclinedPlaneView.setVisibility(View.GONE);
        opticsView.setVisibility(View.GONE);
        gasLawView.setVisibility(View.GONE);
        waveView.setVisibility(View.GONE);
        bohrModelView.setVisibility(View.GONE);
        atwoodView.setVisibility(View.GONE);
        modernPhysicsView.setVisibility(View.GONE);
        webViewSim.setVisibility(View.GONE);
        controlPanel.removeAllViews();
    }

    private void loadWebSim(String fileName, String description) {
        webViewSim.setVisibility(View.VISIBLE);
        webViewSim.loadUrl("file:///android_asset/sims/" + fileName);
        tvSimDescription.setText(description);
    }

    private void showPlaceholder() {
        TextView placeholder = new TextView(this);
        placeholder.setText(String.format(Locale.US, "Sim Lab ID %d is active.", experimentId));
        placeholder.setTextSize(16);
        placeholder.setPadding(32, 32, 32, 32);
        ((LinearLayout)findViewById(R.id.simulationContainer)).addView(placeholder);
    }

    private void showProjectileControls() {
        projectileView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_projectile_controls, controlPanel, false);
        SeekBar sbA = v.findViewById(R.id.sbAngle), sbS = v.findViewById(R.id.sbSpeed);
        TextView tvA = v.findViewById(R.id.tvAngleVal), tvS = v.findViewById(R.id.tvSpeedVal);
        v.findViewById(R.id.btnLaunch).setOnClickListener(view -> projectileView.startSimulation(sbA.getProgress(), sbS.getProgress(), 9.8f, 0.01f, 1.0f));
        v.findViewById(R.id.btnReset).setOnClickListener(view -> projectileView.reset());
        sbA.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvA.setText(p+"°"); projectileView.setAngle(p); }
        });
        sbS.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvS.setText(p+" m/s"); projectileView.setSpeed(p); }
        });
        controlPanel.addView(v);
        tvSimDescription.setText("Projectile Motion: Angle and Velocity impact on range.");
    }

    private void showPendulumControls() {
        pendulumView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_pendulum_controls, controlPanel, false);
        SeekBar sbL = v.findViewById(R.id.sbLength);
        TextView tvL = v.findViewById(R.id.tvLengthVal);
        v.findViewById(R.id.btnStart).setOnClickListener(view -> pendulumView.startSimulation(sbL.getProgress(), 1, 9.8f));
        v.findViewById(R.id.btnReset).setOnClickListener(view -> pendulumView.reset());
        sbL.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvL.setText(p+".0 m"); pendulumView.setLength(p); }
        });
        controlPanel.addView(v);
        tvSimDescription.setText("Pendulum: Period depends on string length.");
    }

    private void showSpringMassControls() {
        springMassView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_spring_mass_controls, controlPanel, false);
        SeekBar sbK = v.findViewById(R.id.sbK), sbM = v.findViewById(R.id.sbMass);
        TextView tvK = v.findViewById(R.id.tvKVal), tvM = v.findViewById(R.id.tvMassVal);
        v.findViewById(R.id.btnStart).setOnClickListener(view -> springMassView.startSimulation(sbK.getProgress(), sbM.getProgress()+0.5f, 0.1f));
        v.findViewById(R.id.btnReset).setOnClickListener(view -> springMassView.reset());
        sbK.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvK.setText(p+" N/m"); } });
        sbM.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvM.setText((p+0.5f)+" kg"); } });
        controlPanel.addView(v);
        tvSimDescription.setText("Spring-Mass: Hooke's Law and SHM.");
    }

    private void showCollisionControls() {
        collisionView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_collision_controls, controlPanel, false);
        SeekBar m1 = v.findViewById(R.id.sbM1), m2 = v.findViewById(R.id.sbM2);
        SeekBar v1 = v.findViewById(R.id.sbV1), v2 = v.findViewById(R.id.sbV2);
        v.findViewById(R.id.btnStart).setOnClickListener(view -> collisionView.startSimulation(m1.getProgress()+1, m2.getProgress()+1, v1.getProgress()-10, v2.getProgress()-10));
        v.findViewById(R.id.btnReset).setOnClickListener(view -> collisionView.reset());
        controlPanel.addView(v);
        tvSimDescription.setText("Collisions: Elastic interaction between masses.");
    }

    private void showInclinedPlaneControls() {
        inclinedPlaneView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_projectile_controls, controlPanel, false);
        SeekBar sbA = v.findViewById(R.id.sbAngle), sbM = v.findViewById(R.id.sbSpeed);
        TextView tvA = v.findViewById(R.id.tvAngleVal), tvM = v.findViewById(R.id.tvSpeedVal);
        sbM.setMax(100); sbM.setProgress(20); tvM.setText("μ: 0.20");
        v.findViewById(R.id.btnLaunch).setOnClickListener(view -> inclinedPlaneView.startSimulation(sbA.getProgress(), 1, sbM.getProgress()/100f));
        v.findViewById(R.id.btnReset).setOnClickListener(view -> inclinedPlaneView.reset());
        sbA.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvA.setText(p+"°"); inclinedPlaneView.setAngle(p); } });
        sbM.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvM.setText(String.format(Locale.US, "μ: %.2f", p/100f)); } });
        controlPanel.addView(v);
        tvSimDescription.setText("Inclined Plane: Friction and Gravity forces.");
    }

    private void showAtwoodControls() {
        atwoodView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_atwood_controls, controlPanel, false);
        SeekBar m1 = v.findViewById(R.id.sbM1), m2 = v.findViewById(R.id.sbM2);
        TextView tv1 = v.findViewById(R.id.tvM1Val), tv2 = v.findViewById(R.id.tvM2Val);
        v.findViewById(R.id.btnStart).setOnClickListener(view -> atwoodView.startSimulation(m1.getProgress()+1, m2.getProgress()+1));
        v.findViewById(R.id.btnReset).setOnClickListener(view -> atwoodView.reset());
        m1.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tv1.setText((p+1)+".0 kg"); } });
        m2.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tv2.setText((p+1)+".0 kg"); } });
        controlPanel.addView(v);
        tvSimDescription.setText("Atwood Machine: Tension in the rope system.");
    }

    private void showWaveControls() {
        waveView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_wave_controls, controlPanel, false);
        SeekBar sbA = v.findViewById(R.id.sbAmplitude), sbF = v.findViewById(R.id.sbFrequency);
        TextView tvA = v.findViewById(R.id.tvAmpVal), tvF = v.findViewById(R.id.tvFreqVal);
        sbA.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvA.setText(p+" px"); waveView.setParams(p, sbF.getProgress()/10f); } });
        sbF.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvF.setText((p/10f)+" Hz"); waveView.setParams(sbA.getProgress(), p/10f); } });
        controlPanel.addView(v);
        tvSimDescription.setText("Wave Motion: Amplitude and Frequency visualization.");
    }

    private void showOpticsControls(int subMode) {
        opticsView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_optics_controls, controlPanel, false);
        SeekBar n = v.findViewById(R.id.sbN2), a = v.findViewById(R.id.sbAngle);
        TextView tvN = v.findViewById(R.id.tvN2Val), tvA = v.findViewById(R.id.tvAngleVal);
        if (subMode == 1) {
            n.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvN.setText(String.format(Locale.US, "%.2f", 1+(p/100f))); opticsView.setSnellParams(1+(p/100f), a.getProgress()); } });
            a.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvA.setText(p+"°"); opticsView.setSnellParams(1+(n.getProgress()/100f), p); } });
            opticsView.setSnellParams(1.5f, 45f);
        } else {
            tvN.setText("f: 150"); tvA.setText("u: 250"); n.setMax(300); n.setProgress(150); a.setMax(500); a.setProgress(250);
            n.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvN.setText("f: "+p); opticsView.setLensParams(p, a.getProgress()); } });
            a.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvA.setText("u: "+p); opticsView.setLensParams(n.getProgress(), p); } });
            opticsView.setLensParams(150, 250);
        }
        controlPanel.addView(v);
        tvSimDescription.setText("Optics: Light ray bending and image formation.");
    }

    private void showGasLawControls() {
        gasLawView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_gas_law_controls, controlPanel, false);
        SeekBar sbV = v.findViewById(R.id.sbVolume), sbT = v.findViewById(R.id.sbTemp);
        TextView tvV = v.findViewById(R.id.tvVolVal), tvT = v.findViewById(R.id.tvTempVal);
        sbV.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvV.setText(p+" L"); gasLawView.setVolume(p); } });
        sbT.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { tvT.setText(p+" K"); gasLawView.setTemperature(p); } });
        controlPanel.addView(v);
        gasLawView.startSimulation(1, 2, 300);
        tvSimDescription.setText("Ideal Gas: Particles reacting to volume and heat.");
    }

    private void showBohrControls() {
        bohrModelView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_pendulum_controls, controlPanel, false);
        SeekBar sbN = v.findViewById(R.id.sbLength);
        TextView tvN = v.findViewById(R.id.tvLengthVal);
        sbN.setMax(4); sbN.setProgress(1); tvN.setText("n=1");
        sbN.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { int n = p>0?p:1; tvN.setText("n="+n); bohrModelView.setN(n); } });
        controlPanel.addView(v);
        bohrModelView.setN(1);
        tvSimDescription.setText("Atomic Shells: Principal quantum levels.");
    }

    private void showModernControls(int subMode) {
        modernPhysicsView.setVisibility(View.VISIBLE);
        View v = getLayoutInflater().inflate(R.layout.layout_wave_controls, controlPanel, false);
        SeekBar sb1 = v.findViewById(R.id.sbAmplitude), sb2 = v.findViewById(R.id.sbFrequency);
        TextView tv1 = v.findViewById(R.id.tvAmpVal), tv2 = v.findViewById(R.id.tvFreqVal);
        
        if (subMode == 1) { // Photoelectric
            tv1.setText("Intensity"); tv2.setText("Frequency");
            sb1.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { modernPhysicsView.setIntensity(p); } });
            sb2.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { modernPhysicsView.setFrequency(p/50f); } });
            modernPhysicsView.startSimulation(1);
        } else if (subMode == 2) { // Decay
            tv1.setText("Initial Nuclei"); tv2.setText("Half-Life");
            modernPhysicsView.startSimulation(2);
        } else { // Relativity
            tv1.setText("Velocity (c)"); tv2.setText("Length");
            sb1.setOnSeekBarChangeListener(new SimpleSeekBarListener() { @Override public void onProgressChanged(SeekBar s, int p, boolean f) { modernPhysicsView.setVelocity(p/100f); } });
            modernPhysicsView.startSimulation(3);
        }
        controlPanel.addView(v);
    }

    private static abstract class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
