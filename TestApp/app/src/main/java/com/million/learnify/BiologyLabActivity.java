package com.million.learnify;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BiologyLabActivity extends AppCompatActivity {

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
            getSupportActionBar().setTitle("Interactive Biology Lab");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

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
        
        // --- 🧪 LOW LEVEL (Beginner Labs) ---
        list.add(new Experiment("Microscope Introduction", "Beginner", "Parts & Focusing (Onion Cells)", 201, "🔬"));
        list.add(new Experiment("Cell Observation", "Beginner", "Plant vs Animal Cells", 202, "🧫"));
        list.add(new Experiment("Diffusion and Osmosis", "Beginner", "Water Movement (Potato/Egg)", 203, "💧"));
        list.add(new Experiment("Food Test Lab", "Beginner", "Starch, Protein, Glucose", 204, "🧪"));
        list.add(new Experiment("Photosynthesis Basics", "Beginner", "Leaf Starch Test", 205, "🍃"));
        list.add(new Experiment("Respiration in Living Organisms", "Beginner", "CO2 Production (Seeds)", 206, "🌱"));
        list.add(new Experiment("Enzyme Activity (Simple)", "Beginner", "Saliva & Starch reaction", 207, "⏱️"));
        list.add(new Experiment("Human Body Systems Intro", "Beginner", "Interactive Organ Labeling", 208, "👤"));
        list.add(new Experiment("Plant Structure Lab", "Beginner", "Roots, Stems, Leaves Dissection", 209, "🌿"));
        list.add(new Experiment("Simple Classification", "Beginner", "Grouping Organisms", 210, "📁"));

        // --- 🧬 INTERMEDIATE LEVEL (Grade 10–11) ---
        list.add(new Experiment("Enzyme Activity (Advanced)", "Intermediate", "Temp & pH Effects", 211, "🌡️"));
        list.add(new Experiment("Photosynthesis Rate", "Intermediate", "Oxygen Bubbles (Light Intensity)", 212, "🫧"));
        list.add(new Experiment("Respiration Rate", "Intermediate", "Yeast + Sugar Gas Production", 213, "🍞"));
        list.add(new Experiment("Genetics Simulation", "Intermediate", "Punnett Squares Traits", 214, "🔲"));
        list.add(new Experiment("DNA Extraction", "Intermediate", "Fruit DNA Visualization", 215, "🧬"));
        list.add(new Experiment("Mitosis Observation", "Intermediate", "Cell Division Phases", 216, "➗"));
        list.add(new Experiment("Meiosis Simulation", "Intermediate", "Genetic Variation & Crossing Over", 217, "✖️"));
        list.add(new Experiment("Blood Group Testing", "Intermediate", "Virtual Blood Typing (ABO)", 218, "🩸"));
        list.add(new Experiment("Human Digestive System", "Intermediate", "Food Breakdown Simulation", 219, "🍔"));
        list.add(new Experiment("Nervous System Reaction", "Intermediate", "Reflex & Reaction Time", 220, "⚡"));
        list.add(new Experiment("Ecosystem Interaction", "Intermediate", "Food Web Model", 221, "🕸️"));
        list.add(new Experiment("Population Growth", "Intermediate", "Exponential Growth Graphs", 222, "📈"));
        list.add(new Experiment("Water Transport in Plants", "Intermediate", "Xylem Movement", 223, "🪵"));
        list.add(new Experiment("Transpiration Lab", "Intermediate", "Water Loss (Plastic Bag method)", 224, "☁️"));

        // --- 🧫 ADVANCED LEVEL (Grade 11–12) ---
        list.add(new Experiment("Enzyme Kinetics", "Advanced", "Mathematical Reaction Rates", 225, "📊"));
        list.add(new Experiment("DNA Replication Model", "Advanced", "Copying Simulation", 226, "🖨️"));
        list.add(new Experiment("Protein Synthesis", "Advanced", "Transcription & Translation", 227, "🔗"));
        list.add(new Experiment("Biotechnology Lab", "Advanced", "Genetic Engineering Basics", 228, "✂️"));
        list.add(new Experiment("Gel Electrophoresis", "Advanced", "DNA Fragment Analysis", 229, "🏁"));
        list.add(new Experiment("Immunology Lab", "Advanced", "Antigen-Antibody Reaction", 230, "🛡️"));
        list.add(new Experiment("Evolution Simulation", "Advanced", "Natural Selection Survival", 231, "🦎"));
        list.add(new Experiment("Ecology Field Study", "Advanced", "Biodiversity Collection", 232, "🌍"));
        list.add(new Experiment("Antibiotic Resistance", "Advanced", "Bacterial Growth Comparison", 233, "💊"));
        list.add(new Experiment("Hormone Action Lab", "Advanced", "Endocrine Feedback loops", 234, "🧪"));
        list.add(new Experiment("Cardiovascular System", "Advanced", "Heart Rate & Exercise", 235, "❤️"));
        list.add(new Experiment("Lung Capacity Lab", "Advanced", "Breathing Volume (Balloon)", 236, "🎈"));
        list.add(new Experiment("Bioinformatics Lab", "Advanced", "DNA Sequence Analysis", 237, "💻"));

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
                Intent intent = new Intent(BiologyLabActivity.this, BiologySimulationActivity.class);
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
