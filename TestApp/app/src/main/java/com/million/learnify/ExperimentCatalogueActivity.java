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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExperimentCatalogueActivity extends AppCompatActivity {

    private RecyclerView rvCatalogue;
    private CatalogueAdapter adapter;
    private List<CatalogueExperiment> masterList;
    private List<CatalogueExperiment> filteredList;

    private EditText etSearch;
    private View llEmptyResults;

    // Filter states
    private String selectedSubject = "ALL"; // ALL, BIO, PHY, CHM
    private String selectedLevel = "ALL";   // ALL, BEGINNER, INTERMEDIATE, ADVANCED

    // Filter Chips
    private TextView chipAll, chipBio, chipPhy, chipChm, chipBeginner, chipIntermediate, chipAdvanced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment_catalogue);

        setupToolbar();
        initViews();
        buildMasterCatalogue();
        setupFilters();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarCatalogue);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearchExperiment);
        llEmptyResults = findViewById(R.id.llEmptyResults);
        rvCatalogue = findViewById(R.id.rvCatalogueExperiments);
        rvCatalogue.setLayoutManager(new LinearLayoutManager(this));

        chipAll = findViewById(R.id.chipFilterAll);
        chipBio = findViewById(R.id.chipFilterBio);
        chipPhy = findViewById(R.id.chipFilterPhy);
        chipChm = findViewById(R.id.chipFilterChm);
        chipBeginner = findViewById(R.id.chipFilterBeginner);
        chipIntermediate = findViewById(R.id.chipFilterIntermediate);
        chipAdvanced = findViewById(R.id.chipFilterAdvanced);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> {
            selectedSubject = "ALL";
            selectedLevel = "ALL";
            updateChipStyles();
            applyFilter();
        });

        chipBio.setOnClickListener(v -> {
            selectedSubject = "BIO";
            updateChipStyles();
            applyFilter();
        });

        chipPhy.setOnClickListener(v -> {
            selectedSubject = "PHY";
            updateChipStyles();
            applyFilter();
        });

        chipChm.setOnClickListener(v -> {
            selectedSubject = "CHM";
            updateChipStyles();
            applyFilter();
        });

        chipBeginner.setOnClickListener(v -> {
            selectedLevel = "BEGINNER";
            updateChipStyles();
            applyFilter();
        });

        chipIntermediate.setOnClickListener(v -> {
            selectedLevel = "INTERMEDIATE";
            updateChipStyles();
            applyFilter();
        });

        chipAdvanced.setOnClickListener(v -> {
            selectedLevel = "ADVANCED";
            updateChipStyles();
            applyFilter();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateChipStyles() {
        setChipStyle(chipAll, "ALL".equals(selectedSubject) && "ALL".equals(selectedLevel));
        setChipStyle(chipBio, "BIO".equals(selectedSubject));
        setChipStyle(chipPhy, "PHY".equals(selectedSubject));
        setChipStyle(chipChm, "CHM".equals(selectedSubject));
        setChipStyle(chipBeginner, "BEGINNER".equals(selectedLevel));
        setChipStyle(chipIntermediate, "INTERMEDIATE".equals(selectedLevel));
        setChipStyle(chipAdvanced, "ADVANCED".equals(selectedLevel));
    }

    private void setChipStyle(TextView chip, boolean isActive) {
        if (chip == null) return;
        if (isActive) {
            chip.setBackgroundResource(R.drawable.bg_science_stage_active);
            chip.setTextColor(0xFF071426);
        } else {
            chip.setBackgroundResource(R.drawable.bg_science_stage_inactive);
            chip.setTextColor(0xFFB9C9DC);
        }
    }

    private void applyFilter() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase(Locale.US) : "";
        filteredList.clear();

        for (CatalogueExperiment exp : masterList) {
            // Subject check
            if (!"ALL".equals(selectedSubject) && !exp.subject.equalsIgnoreCase(selectedSubject)) {
                continue;
            }

            // Level check
            if (!"ALL".equals(selectedLevel)) {
                boolean matchLevel = false;
                if ("BEGINNER".equals(selectedLevel) && (exp.category.toLowerCase().contains("beginner") || exp.category.toLowerCase().contains("grade 9") || exp.level.equalsIgnoreCase("Beginner"))) {
                    matchLevel = true;
                } else if ("INTERMEDIATE".equals(selectedLevel) && (exp.category.toLowerCase().contains("intermediate") || exp.category.toLowerCase().contains("grade 10") || exp.level.equalsIgnoreCase("Intermediate"))) {
                    matchLevel = true;
                } else if ("ADVANCED".equals(selectedLevel) && (exp.category.toLowerCase().contains("advanced") || exp.category.toLowerCase().contains("grade 11") || exp.category.toLowerCase().contains("grade 12") || exp.level.equalsIgnoreCase("Advanced"))) {
                    matchLevel = true;
                }
                if (!matchLevel) continue;
            }

            // Search query check
            if (!query.isEmpty()) {
                boolean matchQuery = exp.name.toLowerCase(Locale.US).contains(query)
                        || exp.category.toLowerCase(Locale.US).contains(query)
                        || exp.concept.toLowerCase(Locale.US).contains(query);
                if (!matchQuery) continue;
            }

            filteredList.add(exp);
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            llEmptyResults.setVisibility(View.VISIBLE);
            rvCatalogue.setVisibility(View.GONE);
        } else {
            llEmptyResults.setVisibility(View.GONE);
            rvCatalogue.setVisibility(View.VISIBLE);
        }
    }

    private void buildMasterCatalogue() {
        masterList = new ArrayList<>();

        // 1. Biology Experiments (IDs 201 - 237)
        masterList.add(new CatalogueExperiment(201, "BIO", "Microscope Introduction", "Beginner", "Parts & Focusing (Onion Cells)", "Beginner"));
        masterList.add(new CatalogueExperiment(202, "BIO", "Cell Observation", "Beginner", "Plant vs Animal Cells", "Beginner"));
        masterList.add(new CatalogueExperiment(203, "BIO", "Diffusion and Osmosis", "Beginner", "Water Movement (Potato/Egg)", "Beginner"));
        masterList.add(new CatalogueExperiment(204, "BIO", "Food Test Lab", "Beginner", "Starch, Protein, Glucose", "Beginner"));
        masterList.add(new CatalogueExperiment(205, "BIO", "Photosynthesis Basics", "Beginner", "Leaf Starch Test", "Beginner"));
        masterList.add(new CatalogueExperiment(206, "BIO", "Respiration in Living Organisms", "Beginner", "CO2 Production (Seeds)", "Beginner"));
        masterList.add(new CatalogueExperiment(207, "BIO", "Enzyme Activity (Simple)", "Beginner", "Saliva & Starch reaction", "Beginner"));
        masterList.add(new CatalogueExperiment(208, "BIO", "Human Body Systems Intro", "Beginner", "Interactive Organ Labeling", "Beginner"));
        masterList.add(new CatalogueExperiment(209, "BIO", "Plant Structure Lab", "Beginner", "Roots, Stems, Leaves Dissection", "Beginner"));
        masterList.add(new CatalogueExperiment(210, "BIO", "Simple Classification", "Beginner", "Grouping Organisms", "Beginner"));

        masterList.add(new CatalogueExperiment(211, "BIO", "Enzyme Activity (Advanced)", "Intermediate", "Temp & pH Effects", "Intermediate"));
        masterList.add(new CatalogueExperiment(212, "BIO", "Photosynthesis Rate", "Intermediate", "Oxygen Bubbles (Light Intensity)", "Intermediate"));
        masterList.add(new CatalogueExperiment(213, "BIO", "Respiration Rate", "Intermediate", "Yeast + Sugar Gas Production", "Intermediate"));
        masterList.add(new CatalogueExperiment(214, "BIO", "Genetics Simulation", "Intermediate", "Punnett Squares Traits", "Intermediate"));
        masterList.add(new CatalogueExperiment(215, "BIO", "DNA Extraction", "Intermediate", "Fruit DNA Visualization", "Intermediate"));
        masterList.add(new CatalogueExperiment(216, "BIO", "Mitosis Observation", "Intermediate", "Cell Division Phases", "Intermediate"));
        masterList.add(new CatalogueExperiment(217, "BIO", "Meiosis Simulation", "Intermediate", "Genetic Variation & Crossing Over", "Intermediate"));
        masterList.add(new CatalogueExperiment(218, "BIO", "Blood Group Testing", "Intermediate", "Virtual Blood Typing (ABO)", "Intermediate"));
        masterList.add(new CatalogueExperiment(219, "BIO", "Human Digestive System", "Intermediate", "Food Breakdown Simulation", "Intermediate"));
        masterList.add(new CatalogueExperiment(220, "BIO", "Nervous System Reaction", "Intermediate", "Reflex & Reaction Time", "Intermediate"));
        masterList.add(new CatalogueExperiment(221, "BIO", "Ecosystem Interaction", "Intermediate", "Food Web Model", "Intermediate"));
        masterList.add(new CatalogueExperiment(222, "BIO", "Population Growth", "Intermediate", "Exponential Growth Graphs", "Intermediate"));
        masterList.add(new CatalogueExperiment(223, "BIO", "Water Transport in Plants", "Intermediate", "Xylem Movement", "Intermediate"));
        masterList.add(new CatalogueExperiment(224, "BIO", "Transpiration Lab", "Intermediate", "Water Loss (Plastic Bag method)", "Intermediate"));

        masterList.add(new CatalogueExperiment(225, "BIO", "Enzyme Kinetics", "Advanced", "Mathematical Reaction Rates", "Advanced"));
        masterList.add(new CatalogueExperiment(226, "BIO", "DNA Replication Model", "Advanced", "Copying Simulation", "Advanced"));
        masterList.add(new CatalogueExperiment(227, "BIO", "Protein Synthesis", "Advanced", "Transcription & Translation", "Advanced"));
        masterList.add(new CatalogueExperiment(228, "BIO", "Biotechnology Lab", "Advanced", "Genetic Engineering Basics", "Advanced"));
        masterList.add(new CatalogueExperiment(229, "BIO", "Gel Electrophoresis", "Advanced", "DNA Fragment Analysis", "Advanced"));
        masterList.add(new CatalogueExperiment(230, "BIO", "Immunology Lab", "Advanced", "Antigen-Antibody Reaction", "Advanced"));
        masterList.add(new CatalogueExperiment(231, "BIO", "Evolution Simulation", "Advanced", "Natural Selection Survival", "Advanced"));
        masterList.add(new CatalogueExperiment(232, "BIO", "Ecology Field Study", "Advanced", "Biodiversity Collection", "Advanced"));
        masterList.add(new CatalogueExperiment(233, "BIO", "Antibiotic Resistance", "Advanced", "Bacterial Growth Comparison", "Advanced"));
        masterList.add(new CatalogueExperiment(234, "BIO", "Hormone Action Lab", "Advanced", "Endocrine Feedback loops", "Advanced"));
        masterList.add(new CatalogueExperiment(235, "BIO", "Cardiovascular System", "Advanced", "Heart Rate & Exercise", "Advanced"));
        masterList.add(new CatalogueExperiment(236, "BIO", "Lung Capacity Lab", "Advanced", "Breathing Volume (Balloon)", "Advanced"));
        masterList.add(new CatalogueExperiment(237, "BIO", "Bioinformatics Lab", "Advanced", "DNA Sequence Analysis", "Advanced"));

        // 2. Physics Experiments (IDs 1 - 40)
        masterList.add(new CatalogueExperiment(1, "PHY", "Projectile Motion", "Mechanics", "Kinematics, trajectory", "Intermediate"));
        masterList.add(new CatalogueExperiment(2, "PHY", "Pendulum", "Mechanics", "Simple harmonic motion", "Beginner"));
        masterList.add(new CatalogueExperiment(3, "PHY", "Spring-Mass System", "Mechanics", "Hooke's law, oscillation", "Beginner"));
        masterList.add(new CatalogueExperiment(4, "PHY", "Newton's Cradle", "Mechanics", "Conservation of momentum", "Beginner"));
        masterList.add(new CatalogueExperiment(5, "PHY", "Inclined Plane", "Mechanics", "Friction, forces", "Beginner"));
        masterList.add(new CatalogueExperiment(6, "PHY", "Atwood Machine", "Mechanics", "Tension, acceleration", "Intermediate"));
        masterList.add(new CatalogueExperiment(7, "PHY", "Rocket Thrust", "Mechanics", "Newton's third law", "Intermediate"));
        masterList.add(new CatalogueExperiment(8, "PHY", "Orbital Motion", "Mechanics", "Gravity, centripetal force", "Intermediate"));
        masterList.add(new CatalogueExperiment(9, "PHY", "Conservation of Energy", "Mechanics", "Roller coaster", "Beginner"));
        masterList.add(new CatalogueExperiment(10, "PHY", "Collisions (1D/2D)", "Mechanics", "Elastic/inelastic", "Intermediate"));

        masterList.add(new CatalogueExperiment(11, "PHY", "Wave on a String", "Waves & Oscillations", "Transverse waves", "Beginner"));
        masterList.add(new CatalogueExperiment(12, "PHY", "Sound Waves", "Waves & Oscillations", "Longitudinal waves", "Beginner"));
        masterList.add(new CatalogueExperiment(13, "PHY", "Doppler Effect", "Waves & Oscillations", "Frequency shift", "Intermediate"));
        masterList.add(new CatalogueExperiment(14, "PHY", "Interference (Double Slit)", "Waves & Oscillations", "Wave interference", "Advanced"));
        masterList.add(new CatalogueExperiment(15, "PHY", "Resonance Tube", "Waves & Oscillations", "Sound resonance", "Intermediate"));
        masterList.add(new CatalogueExperiment(16, "PHY", "Fourier Synthesis", "Waves & Oscillations", "Harmonics", "Advanced"));

        masterList.add(new CatalogueExperiment(17, "PHY", "Ohm's Law", "Electricity & Magnetism", "Simple circuits", "Beginner"));
        masterList.add(new CatalogueExperiment(18, "PHY", "RC Circuit", "Electricity & Magnetism", "Capacitor charging", "Intermediate"));
        masterList.add(new CatalogueExperiment(19, "PHY", "Magnetic Field of a Wire", "Electricity & Magnetism", "Biot-Savart law", "Intermediate"));
        masterList.add(new CatalogueExperiment(20, "PHY", "Electric Field Lines", "Electricity & Magnetism", "Coulomb's law", "Intermediate"));
        masterList.add(new CatalogueExperiment(21, "PHY", "Lorentz Force", "Electricity & Magnetism", "Charged particle", "Advanced"));
        masterList.add(new CatalogueExperiment(22, "PHY", "Faraday's Law", "Electricity & Magnetism", "Induction", "Intermediate"));
        masterList.add(new CatalogueExperiment(23, "PHY", "AC Circuits", "Electricity & Magnetism", "Impedance, resonance", "Advanced"));
        masterList.add(new CatalogueExperiment(24, "PHY", "Motor/Generator", "Electricity & Magnetism", "Electromechanics", "Intermediate"));

        masterList.add(new CatalogueExperiment(25, "PHY", "Snell's Law", "Optics", "Refraction", "Beginner"));
        masterList.add(new CatalogueExperiment(26, "PHY", "Lens Ray Diagram", "Optics", "Thin lens equation", "Beginner"));
        masterList.add(new CatalogueExperiment(27, "PHY", "Mirror Ray Diagram", "Optics", "Concave/convex mirrors", "Beginner"));
        masterList.add(new CatalogueExperiment(28, "PHY", "Prism Dispersion", "Optics", "Spectrum", "Beginner"));
        masterList.add(new CatalogueExperiment(29, "PHY", "Diffraction Grating", "Optics", "Interference pattern", "Advanced"));
        masterList.add(new CatalogueExperiment(30, "PHY", "Telescope Simulator", "Optics", "Magnification", "Intermediate"));

        masterList.add(new CatalogueExperiment(31, "PHY", "Ideal Gas Law", "Thermodynamics", "P-V-T relations", "Intermediate"));
        masterList.add(new CatalogueExperiment(32, "PHY", "Heat Engine (Carnot)", "Thermodynamics", "Efficiency", "Advanced"));
        masterList.add(new CatalogueExperiment(33, "PHY", "Specific Heat Capacity", "Thermodynamics", "Calorimetry", "Beginner"));
        masterList.add(new CatalogueExperiment(34, "PHY", "Thermal Expansion", "Thermodynamics", "Linear expansion", "Beginner"));
        masterList.add(new CatalogueExperiment(35, "PHY", "Phase Changes", "Thermodynamics", "Latent heat", "Beginner"));

        masterList.add(new CatalogueExperiment(36, "PHY", "Photoelectric Effect", "Modern Physics", "Photon energy", "Advanced"));
        masterList.add(new CatalogueExperiment(37, "PHY", "Bohr Model (Hydrogen)", "Modern Physics", "Energy levels", "Advanced"));
        masterList.add(new CatalogueExperiment(38, "PHY", "Radioactive Decay", "Modern Physics", "Half-life", "Intermediate"));
        masterList.add(new CatalogueExperiment(39, "PHY", "Nuclear Fusion", "Modern Physics", "Mass-energy", "Advanced"));
        masterList.add(new CatalogueExperiment(40, "PHY", "Relativity", "Modern Physics", "Time dilation", "Advanced"));

        // 3. Chemistry Experiments (IDs 301 - 340)
        masterList.add(new CatalogueExperiment(301, "CHM", "Laboratory Safety & Equipment Lab", "Beginner", "Learn lab tools & safety rules", "Beginner"));
        masterList.add(new CatalogueExperiment(302, "CHM", "Physical vs Chemical Change Lab", "Beginner", "Observe different changes", "Beginner"));
        masterList.add(new CatalogueExperiment(303, "CHM", "States of Matter Lab", "Beginner", "Solid, liquid, gas behavior", "Beginner"));
        masterList.add(new CatalogueExperiment(304, "CHM", "Density Experiment Lab", "Beginner", "Measure density of liquids/solids", "Beginner"));
        masterList.add(new CatalogueExperiment(305, "CHM", "Solubility Lab", "Beginner", "Dissolve substances in water", "Beginner"));
        masterList.add(new CatalogueExperiment(306, "CHM", "Mixtures and Separation Lab", "Beginner", "Filtration, evaporation", "Beginner"));
        masterList.add(new CatalogueExperiment(307, "CHM", "pH Testing Lab (Basic)", "Beginner", "Use indicators", "Beginner"));
        masterList.add(new CatalogueExperiment(308, "CHM", "Acids and Bases Intro Lab", "Beginner", "Identify acids/bases", "Beginner"));
        masterList.add(new CatalogueExperiment(309, "CHM", "Simple Chemical Reactions Lab", "Beginner", "Observe reactions", "Beginner"));
        masterList.add(new CatalogueExperiment(310, "CHM", "Temperature Change Lab", "Beginner", "Endothermic vs exothermic", "Beginner"));

        masterList.add(new CatalogueExperiment(311, "CHM", "pH Scale Investigation Lab", "Intermediate", "Measure exact pH values", "Intermediate"));
        masterList.add(new CatalogueExperiment(312, "CHM", "Titration Lab (Acid-Base)", "Intermediate", "Determine concentration", "Intermediate"));
        masterList.add(new CatalogueExperiment(313, "CHM", "Reaction Rate Lab", "Intermediate", "Effect of temperature/concentration", "Intermediate"));
        masterList.add(new CatalogueExperiment(314, "CHM", "Catalysts Lab", "Intermediate", "Study catalyst effect", "Intermediate"));
        masterList.add(new CatalogueExperiment(315, "CHM", "Gas Collection Lab", "Intermediate", "Collect oxygen or CO₂", "Intermediate"));
        masterList.add(new CatalogueExperiment(316, "CHM", "Law of Conservation of Mass Lab", "Intermediate", "Mass before/after reaction", "Intermediate"));
        masterList.add(new CatalogueExperiment(317, "CHM", "Electrolysis Lab", "Intermediate", "Break water into H₂ and O₂", "Intermediate"));
        masterList.add(new CatalogueExperiment(318, "CHM", "Periodic Table Trends Lab", "Intermediate", "Study atomic size/reactivity", "Intermediate"));
        masterList.add(new CatalogueExperiment(319, "CHM", "Metal Reactivity Lab", "Intermediate", "Compare metals with acid", "Intermediate"));
        masterList.add(new CatalogueExperiment(320, "CHM", "Ionic vs Covalent Compounds Lab", "Intermediate", "Identify compound types", "Intermediate"));
        masterList.add(new CatalogueExperiment(321, "CHM", "Solutions Concentration Lab", "Intermediate", "Prepare solutions", "Intermediate"));
        masterList.add(new CatalogueExperiment(322, "CHM", "Precipitation Reaction Lab", "Intermediate", "Form solid from solution", "Intermediate"));
        masterList.add(new CatalogueExperiment(323, "CHM", "Heat of Reaction Lab", "Intermediate", "Measure energy change", "Intermediate"));
        masterList.add(new CatalogueExperiment(324, "CHM", "Water Hardness Lab", "Intermediate", "Test mineral content", "Intermediate"));

        masterList.add(new CatalogueExperiment(325, "CHM", "Advanced Titration (Redox)", "Advanced", "Oxidation-reduction reactions", "Advanced"));
        masterList.add(new CatalogueExperiment(326, "CHM", "Chemical Equilibrium Lab", "Advanced", "Reversible reactions", "Advanced"));
        masterList.add(new CatalogueExperiment(327, "CHM", "Le Chatelier’s Principle Lab", "Advanced", "Stress effect on equilibrium", "Advanced"));
        masterList.add(new CatalogueExperiment(328, "CHM", "Buffer Solution Lab", "Advanced", "Maintain pH", "Advanced"));
        masterList.add(new CatalogueExperiment(329, "CHM", "Organic Chemistry Lab (Hydrocarbons)", "Advanced", "Study alkanes/alkenes", "Advanced"));
        masterList.add(new CatalogueExperiment(330, "CHM", "Alcohol Reactions Lab", "Advanced", "Study ethanol properties", "Advanced"));
        masterList.add(new CatalogueExperiment(331, "CHM", "Polymerization Lab", "Advanced", "Create polymers (e.g., slime)", "Advanced"));
        masterList.add(new CatalogueExperiment(332, "CHM", "Chromatography Lab", "Advanced", "Separate mixtures", "Advanced"));
        masterList.add(new CatalogueExperiment(333, "CHM", "Spectroscopy (Simulation)", "Advanced", "Analyze substances", "Advanced"));
        masterList.add(new CatalogueExperiment(334, "CHM", "Electrochemical Cells Lab", "Advanced", "Build battery (voltaic cell)", "Advanced"));
        masterList.add(new CatalogueExperiment(335, "CHM", "Corrosion Lab", "Advanced", "Study rusting", "Advanced"));
        masterList.add(new CatalogueExperiment(336, "CHM", "Thermochemistry Lab", "Advanced", "Energy changes in reactions", "Advanced"));
        masterList.add(new CatalogueExperiment(337, "CHM", "Kinetics Lab (Advanced)", "Advanced", "Reaction rate equations", "Advanced"));
        masterList.add(new CatalogueExperiment(338, "CHM", "Environmental Chemistry Lab", "Advanced", "Water/air testing", "Advanced"));
        masterList.add(new CatalogueExperiment(339, "CHM", "Pharmaceutical Chemistry Lab", "Advanced", "Drug composition basics", "Advanced"));
        masterList.add(new CatalogueExperiment(340, "CHM", "Nanotechnology Intro Lab", "Advanced", "Study nanoparticles", "Advanced"));

        filteredList = new ArrayList<>(masterList);
        adapter = new CatalogueAdapter(filteredList);
        rvCatalogue.setAdapter(adapter);
    }

    private static class CatalogueExperiment {
        int id;
        String subject;
        String name;
        String category;
        String concept;
        String level;

        CatalogueExperiment(int id, String subject, String name, String category, String concept, String level) {
            this.id = id;
            this.subject = subject;
            this.name = name;
            this.category = category;
            this.concept = concept;
            this.level = level;
        }
    }

    private class CatalogueAdapter extends RecyclerView.Adapter<CatalogueAdapter.ViewHolder> {
        private final List<CatalogueExperiment> list;

        CatalogueAdapter(List<CatalogueExperiment> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalogue_experiment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CatalogueExperiment exp = list.get(position);

            holder.tvSubject.setText(exp.subject);
            holder.tvId.setText("#" + exp.id);
            holder.tvCategory.setText(String.format(Locale.US, "%s • %s", exp.level, exp.category));
            holder.tvName.setText(exp.name);
            holder.tvConcept.setText(exp.concept);

            // Subject badge colors
            if ("BIO".equalsIgnoreCase(exp.subject)) {
                holder.tvSubject.setTextColor(Color.parseColor("#10B981")); // Green
            } else if ("PHY".equalsIgnoreCase(exp.subject)) {
                holder.tvSubject.setTextColor(Color.parseColor("#38BDF8")); // Cyan
            } else if ("CHM".equalsIgnoreCase(exp.subject)) {
                holder.tvSubject.setTextColor(Color.parseColor("#C084FC")); // Purple
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent;
                if ("BIO".equalsIgnoreCase(exp.subject)) {
                    intent = new Intent(ExperimentCatalogueActivity.this, BiologySimulationActivity.class);
                } else if ("CHM".equalsIgnoreCase(exp.subject)) {
                    intent = new Intent(ExperimentCatalogueActivity.this, ChemistrySimulationActivity.class);
                } else {
                    intent = new Intent(ExperimentCatalogueActivity.this, PhysicsSimulationActivity.class);
                }
                intent.putExtra("experiment_id", exp.id);
                intent.putExtra("experiment_name", exp.name);
                intent.putExtra("concept", exp.concept);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSubject, tvId, tvCategory, tvName, tvConcept;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSubject = itemView.findViewById(R.id.tvSubjectBadge);
                tvId = itemView.findViewById(R.id.tvExpIdBadge);
                tvCategory = itemView.findViewById(R.id.tvExpCategory);
                tvName = itemView.findViewById(R.id.tvExpName);
                tvConcept = itemView.findViewById(R.id.tvExpConcept);
            }
        }
    }
}

