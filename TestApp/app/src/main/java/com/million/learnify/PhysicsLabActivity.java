package com.million.learnify;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhysicsLabActivity extends AppCompatActivity {

    private RecyclerView rvExperiments;
    private ExperimentAdapter adapter;
    private List<Experiment> fullList;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physics_lab);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Virtual Physics Lab");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Set Subject Specific Colors and Logo
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        LinearLayout headerContainer = findViewById(R.id.llHeaderContainer);
        ImageView ivLogo = findViewById(R.id.ivSubjectLogo);
        
        int physicsColor = Color.parseColor("#0284C7"); // Blue 600
        appBarLayout.setBackgroundColor(physicsColor);
        headerContainer.setBackgroundColor(physicsColor);
        ivLogo.setImageResource(R.drawable.ic_lab);
        ivLogo.setColorFilter(physicsColor);

        etSearch = findViewById(R.id.etSearch);
        rvExperiments = findViewById(R.id.rvExperiments);
        rvExperiments.setLayoutManager(new LinearLayoutManager(this));
        
        fullList = getExperiments();
        adapter = new ExperimentAdapter(new ArrayList<>(fullList));
        rvExperiments.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        List<Experiment> filtered = new ArrayList<>();
        for (Experiment e : fullList) {
            if (e.name.toLowerCase().contains(query.toLowerCase()) || 
                e.category.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(e);
            }
        }
        adapter.updateList(filtered);
    }

    private List<Experiment> getExperiments() {
        List<Experiment> list = new ArrayList<>();
        // 1. Mechanics
        list.add(new Experiment("Projectile Motion", "Mechanics", "Kinematics, trajectory", 1, "🚀"));
        list.add(new Experiment("Pendulum", "Mechanics", "Simple harmonic motion", 2, "⏱️"));
        list.add(new Experiment("Spring-Mass System", "Mechanics", "Hooke's law, oscillation", 3, "🌀"));
        list.add(new Experiment("Newton's Cradle", "Mechanics", "Conservation of momentum", 4, "🎱"));
        list.add(new Experiment("Inclined Plane", "Mechanics", "Friction, forces", 5, "📐"));
        list.add(new Experiment("Atwood Machine", "Mechanics", "Tension, acceleration", 6, "🏗️"));
        list.add(new Experiment("Rocket Thrust", "Mechanics", "Newton's third law", 7, "🚀"));
        list.add(new Experiment("Orbital Motion", "Mechanics", "Gravity, centripetal force", 8, "🪐"));
        list.add(new Experiment("Conservation of Energy", "Mechanics", "Roller coaster", 9, "🎢"));
        list.add(new Experiment("Collisions (1D/2D)", "Mechanics", "Elastic/inelastic", 10, "💥"));

        // 2. Waves
        list.add(new Experiment("Wave on a String", "Waves & Oscillations", "Transverse waves", 11, "🌊"));
        list.add(new Experiment("Sound Waves", "Waves & Oscillations", "Longitudinal waves", 12, "🔊"));
        list.add(new Experiment("Doppler Effect", "Waves & Oscillations", "Frequency shift", 13, "🏎️"));
        list.add(new Experiment("Interference (Double Slit)", "Waves & Oscillations", "Wave interference", 14, "🌈"));
        list.add(new Experiment("Resonance Tube", "Waves & Oscillations", "Sound resonance", 15, "🎺"));
        list.add(new Experiment("Fourier Synthesis", "Waves & Oscillations", "Harmonics", 16, "🎹"));

        // 3. E&M
        list.add(new Experiment("Ohm's Law", "Electricity & Magnetism", "Simple circuits", 17, "💡"));
        list.add(new Experiment("RC Circuit", "Electricity & Magnetism", "Capacitor charging", 18, "🔋"));
        list.add(new Experiment("Magnetic Field of a Wire", "Electricity & Magnetism", "Biot-Savart law", 19, "🧲"));
        list.add(new Experiment("Electric Field Lines", "Electricity & Magnetism", "Coulomb's law", 20, "⚡"));
        list.add(new Experiment("Lorentz Force", "Electricity & Magnetism", "Charged particle", 21, "⚛️"));
        list.add(new Experiment("Faraday's Law", "Electricity & Magnetism", "Induction", 22, "🌀"));
        list.add(new Experiment("AC Circuits", "Electricity & Magnetism", "Impedance, resonance", 23, "🔌"));
        list.add(new Experiment("Motor/Generator", "Electricity & Magnetism", "Electromechanics", 24, "⚙️"));

        // 4. Optics
        list.add(new Experiment("Snell's Law", "Optics", "Refraction", 25, "🔦"));
        list.add(new Experiment("Lens Ray Diagram", "Optics", "Thin lens equation", 26, "🔍"));
        list.add(new Experiment("Mirror Ray Diagram", "Optics", "Concave/convex mirrors", 27, "🪞"));
        list.add(new Experiment("Prism Dispersion", "Optics", "Spectrum", 28, "💎"));
        list.add(new Experiment("Diffraction Grating", "Optics", "Interference pattern", 29, "💿"));
        list.add(new Experiment("Telescope Simulator", "Optics", "Magnification", 30, "🔭"));

        // 5. Thermo
        list.add(new Experiment("Ideal Gas Law", "Thermodynamics", "P-V-T relations", 31, "🎈"));
        list.add(new Experiment("Heat Engine (Carnot)", "Thermodynamics", "Efficiency", 32, "🚂"));
        list.add(new Experiment("Specific Heat Capacity", "Thermodynamics", "Calorimetry", 33, "🌡️"));
        list.add(new Experiment("Thermal Expansion", "Thermodynamics", "Linear expansion", 34, "📏"));
        list.add(new Experiment("Phase Changes", "Thermodynamics", "Latent heat", 35, "🧊"));

        // 6. Modern Physics
        list.add(new Experiment("Photoelectric Effect", "Modern Physics", "Photon energy", 36, "✨"));
        list.add(new Experiment("Bohr Model (Hydrogen)", "Modern Physics", "Energy levels", 37, "⚛️"));
        list.add(new Experiment("Radioactive Decay", "Modern Physics", "Half-life", 38, "☢️"));
        list.add(new Experiment("Nuclear Fusion", "Modern Physics", "Mass-energy", 39, "☀️"));
        list.add(new Experiment("Relativity", "Modern Physics", "Time dilation", 40, "⌛"));

        return list;
    }

    private static class Experiment {
        String name, category, concept, icon;
        int id;
        Experiment(String n, String cat, String con, int i, String ic) {
            name = n; category = cat; concept = con; id = i; icon = ic;
        }
    }

    private class ExperimentAdapter extends RecyclerView.Adapter<ExperimentAdapter.ViewHolder> {
        private List<Experiment> experiments;
        ExperimentAdapter(List<Experiment> list) { this.experiments = list; }

        void updateList(List<Experiment> newList) {
            this.experiments = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_physics_experiment, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Experiment exp = experiments.get(position);
            holder.tvName.setText(exp.name);
            holder.tvCategory.setText(String.format(Locale.US, "%s • %s", exp.category, exp.concept));
            holder.tvIcon.setText(exp.icon);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PhysicsLabActivity.this, PhysicsSimulationActivity.class);
                intent.putExtra("experiment_id", exp.id);
                intent.putExtra("experiment_name", exp.name);
                intent.putExtra("concept", exp.concept);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return experiments.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCategory, tvIcon;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvExpName);
                tvCategory = v.findViewById(R.id.tvExpCategory);
                tvIcon = v.findViewById(R.id.tvExpIcon);
            }
        }
    }
}
