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

public class ChemistryLabActivity extends AppCompatActivity {

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
            getSupportActionBar().setTitle("Virtual Chemistry Lab");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Set Subject Specific Colors and Logo
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        LinearLayout headerContainer = findViewById(R.id.llHeaderContainer);
        ImageView ivLogo = findViewById(R.id.ivSubjectLogo);
        
        int chemistryColor = Color.parseColor("#9333EA"); // Purple 600
        appBarLayout.setBackgroundColor(chemistryColor);
        headerContainer.setBackgroundColor(chemistryColor);
        ivLogo.setImageResource(R.drawable.ic_lab);
        ivLogo.setColorFilter(chemistryColor);

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
        
        // --- 1. Beginner Level (Foundation Labs) ---
        list.add(new Experiment("Laboratory Safety & Equipment Lab", "Beginner", "Learn lab tools & safety rules", 301, "CHM"));
        list.add(new Experiment("Physical vs Chemical Change Lab", "Beginner", "Observe different changes", 302, "CHM"));
        list.add(new Experiment("States of Matter Lab", "Beginner", "Solid, liquid, gas behavior", 303, "CHM"));
        list.add(new Experiment("Density Experiment Lab", "Beginner", "Measure density of liquids/solids", 304, "CHM"));
        list.add(new Experiment("Solubility Lab", "Beginner", "Dissolve substances in water", 305, "CHM"));
        list.add(new Experiment("Mixtures and Separation Lab", "Beginner", "Filtration, evaporation", 306, "CHM"));
        list.add(new Experiment("pH Testing Lab (Basic)", "Beginner", "Use indicators", 307, "CHM"));
        list.add(new Experiment("Acids and Bases Intro Lab", "Beginner", "Identify acids/bases", 308, "CHM"));
        list.add(new Experiment("Simple Chemical Reactions Lab", "Beginner", "Observe reactions", 309, "CHM"));
        list.add(new Experiment("Temperature Change Lab", "Beginner", "Endothermic vs exothermic", 310, "CHM"));

        // --- 2. Intermediate Level (Grade 10–11) ---
        list.add(new Experiment("pH Scale Investigation Lab", "Intermediate", "Measure exact pH values", 311, "CHM"));
        list.add(new Experiment("Titration Lab (Acid-Base)", "Intermediate", "Determine concentration", 312, "CHM"));
        list.add(new Experiment("Reaction Rate Lab", "Intermediate", "Effect of temperature/concentration", 313, "CHM"));
        list.add(new Experiment("Catalysts Lab", "Intermediate", "Study catalyst effect", 314, "CHM"));
        list.add(new Experiment("Gas Collection Lab", "Intermediate", "Collect oxygen or CO₂", 315, "CHM"));
        list.add(new Experiment("Law of Conservation of Mass Lab", "Intermediate", "Mass before/after reaction", 316, "CHM"));
        list.add(new Experiment("Electrolysis Lab", "Intermediate", "Break water into H₂ and O₂", 317, "CHM"));
        list.add(new Experiment("Periodic Table Trends Lab", "Intermediate", "Study atomic size/reactivity", 318, "CHM"));
        list.add(new Experiment("Metal Reactivity Lab", "Intermediate", "Compare metals with acid", 319, "CHM"));
        list.add(new Experiment("Ionic vs Covalent Compounds Lab", "Intermediate", "Identify compound types", 320, "CHM"));
        list.add(new Experiment("Solutions Concentration Lab", "Intermediate", "Prepare solutions", 321, "CHM"));
        list.add(new Experiment("Precipitation Reaction Lab", "Intermediate", "Form solid from solution", 322, "CHM"));
        list.add(new Experiment("Heat of Reaction Lab", "Intermediate", "Measure energy change", 323, "CHM"));
        list.add(new Experiment("Water Hardness Lab", "Intermediate", "Test mineral content", 324, "CHM"));

        // --- 3. Advanced Level (Grade 11–12 / Pre-University) ---
        list.add(new Experiment("Advanced Titration (Redox)", "Advanced", "Oxidation-reduction reactions", 325, "CHM"));
        list.add(new Experiment("Chemical Equilibrium Lab", "Advanced", "Reversible reactions", 326, "CHM"));
        list.add(new Experiment("Le Chatelier’s Principle Lab", "Advanced", "Stress effect on equilibrium", 327, "CHM"));
        list.add(new Experiment("Buffer Solution Lab", "Advanced", "Maintain pH", 328, "CHM"));
        list.add(new Experiment("Organic Chemistry Lab (Hydrocarbons)", "Advanced", "Study alkanes/alkenes", 329, "CHM"));
        list.add(new Experiment("Alcohol Reactions Lab", "Advanced", "Study ethanol properties", 330, "CHM"));
        list.add(new Experiment("Polymerization Lab", "Advanced", "Create polymers (e.g., slime)", 331, "CHM"));
        list.add(new Experiment("Chromatography Lab", "Advanced", "Separate mixtures", 332, "CHM"));
        list.add(new Experiment("Spectroscopy (Simulation)", "Advanced", "Analyze substances", 333, "CHM"));
        list.add(new Experiment("Electrochemical Cells Lab", "Advanced", "Build battery (voltaic cell)", 334, "CHM"));
        list.add(new Experiment("Corrosion Lab", "Advanced", "Study rusting", 335, "CHM"));
        list.add(new Experiment("Thermochemistry Lab", "Advanced", "Energy changes in reactions", 336, "CHM"));
        list.add(new Experiment("Kinetics Lab (Advanced)", "Advanced", "Reaction rate equations", 337, "CHM"));
        list.add(new Experiment("Environmental Chemistry Lab", "Advanced", "Water/air testing", 338, "CHM"));
        list.add(new Experiment("Pharmaceutical Chemistry Lab", "Advanced", "Drug composition basics", 339, "CHM"));
        list.add(new Experiment("Nanotechnology Intro Lab", "Advanced", "Study nanoparticles", 340, "CHM"));

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
            holder.tvIcon.setText(String.format(Locale.US, "#%d", position + 1));
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ChemistryLabActivity.this, ChemistrySimulationActivity.class);
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
