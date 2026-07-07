package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MedicineListActivity extends AppCompatActivity {

    public static final String EXTRA_MODE  = "mode";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_QUERY = "query";

    private LinearLayout medicineListContainer;
    private LinearLayout filterChipsContainer;
    private EditText etSearch;
    private ImageView btnClearSearch;
    private TextView tvScreenTitle;

    private String mode         = "search";
    private String query        = "";
    private String activeFilter = "All";

    // Set of hidden pharmacies — customer dislike pharmacies
    private final Set<String> hiddenPharmacies = new HashSet<>();

    final List<Medicine> allMedicines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_list);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        medicineListContainer = findViewById(R.id.medicineListContainer);
        filterChipsContainer  = findViewById(R.id.filterChipsContainer);
        etSearch              = findViewById(R.id.etSearch);
        btnClearSearch        = findViewById(R.id.btnClearSearch);
        tvScreenTitle         = findViewById(R.id.tvScreenTitle);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        mode  = getIntent().getStringExtra(EXTRA_MODE)  != null ? getIntent().getStringExtra(EXTRA_MODE)  : "search";
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        query = getIntent().getStringExtra(EXTRA_QUERY) != null ? getIntent().getStringExtra(EXTRA_QUERY) : "";

        if (title != null) tvScreenTitle.setText(title);

        loadSampleData();
        buildFilterChips();

        if ("search".equals(mode)) {
            etSearch.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
            filterAndRender("");
        } else {
            etSearch.setVisibility(View.GONE);
            btnClearSearch.setVisibility(View.GONE);
            filterAndRender(query);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterAndRender(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterAndRender(etSearch.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void buildFilterChips() {
        filterChipsContainer.removeAllViews();

        // --- Row 1: Type + Price filters ---
        String[] typeFilters = {"All", "OTC", "Prescription", "Price: Low→High", "Price: High→Low"};
        addScrollableChipRow(typeFilters, activeFilter, filter -> {
            activeFilter = filter;
            buildFilterChips();
            filterAndRender("search".equals(mode) ? etSearch.getText().toString() : query);
        });

        // --- Label ---
        TextView label = new TextView(this);
        label.setText("Hide pharmacy:");
        label.setTextSize(11);
        label.setTextColor(getResources().getColor(R.color.pg_sub, null));
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.topMargin  = dp(8);
        llp.bottomMargin = dp(4);
        label.setLayoutParams(llp);
        filterChipsContainer.addView(label);

        // --- Row 2: Pharmacy hide/show toggles ---
        List<String> pharmacies = new ArrayList<>();
        for (Medicine m : allMedicines) {
            if (!pharmacies.contains(m.pharmacy)) pharmacies.add(m.pharmacy);
        }

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.HORIZONTAL);

        for (String pharmacy : pharmacies) {
            boolean isHidden = hiddenPharmacies.contains(pharmacy);

            TextView chip = new TextView(this);
            // Show eye icon + pharmacy name
            chip.setText((isHidden ? "🚫 " : "✅ ") + pharmacy);
            chip.setTextSize(11);
            chip.setPadding(dp(12), dp(6), dp(12), dp(6));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(6));
            chip.setLayoutParams(lp);
            chip.setClickable(true);
            chip.setFocusable(true);

            // Hidden = red chip, Visible = green chip
            if (isHidden) {
                chip.setBackgroundResource(R.drawable.bg_tag_red);
                chip.setTextColor(0xFF991B1B);
            } else {
                chip.setBackgroundResource(R.drawable.bg_tag_green);
                chip.setTextColor(0xFF065F46);
            }

            chip.setOnClickListener(v -> {
                if (hiddenPharmacies.contains(pharmacy)) {
                    // Unhide — customer wants to see again
                    hiddenPharmacies.remove(pharmacy);
                    Toast.makeText(this, pharmacy + " shown", Toast.LENGTH_SHORT).show();
                } else {
                    // Hide — customer doesn't want this pharmacy
                    hiddenPharmacies.add(pharmacy);
                    Toast.makeText(this, pharmacy + " hidden", Toast.LENGTH_SHORT).show();
                }
                buildFilterChips();
                filterAndRender("search".equals(mode) ? etSearch.getText().toString() : query);
            });

            inner.addView(chip);
        }

        hsv.addView(inner);
        filterChipsContainer.addView(hsv);
    }

    private void addScrollableChipRow(String[] labels, String activeValue, ChipClickListener listener) {
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams hsvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hsvLp.bottomMargin = dp(4);
        hsv.setLayoutParams(hsvLp);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.HORIZONTAL);

        for (String label : labels) {
            TextView chip = new TextView(this);
            chip.setText(label);
            chip.setTextSize(11);
            chip.setPadding(dp(14), dp(6), dp(14), dp(6));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(6));
            chip.setLayoutParams(lp);
            chip.setClickable(true);
            chip.setFocusable(true);

            boolean selected = label.equals(activeValue);
            chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            chip.setTextColor(getResources().getColor(
                    selected ? R.color.pg_primary : R.color.pg_sub, null));
            chip.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
            chip.setOnClickListener(v -> listener.onClick(label));
            inner.addView(chip);
        }

        hsv.addView(inner);
        filterChipsContainer.addView(hsv);
    }

    interface ChipClickListener {
        void onClick(String label);
    }

    private void filterAndRender(String searchText) {
        medicineListContainer.removeAllViews();

        String q = "search".equals(mode) ? searchText.toLowerCase().trim() : "";
        List<Medicine> filtered = new ArrayList<>();

        for (Medicine m : allMedicines) {
            //  Hidden pharmacy check — skip hidden ones
            if (hiddenPharmacies.contains(m.pharmacy)) continue;

            // Mode filter
            boolean modeMatch = true;
            if ("category".equals(mode)) {
                modeMatch = m.categoryKey != null &&
                        m.categoryKey.trim().equalsIgnoreCase(query.trim());
            } else if ("pharmacy".equals(mode)) {
                modeMatch = m.pharmacy != null &&
                        m.pharmacy.trim().equalsIgnoreCase(query.trim());
            }

            // Search text filter
            boolean searchMatch = q.isEmpty()
                    || m.name.toLowerCase().contains(q)
                    || m.category.toLowerCase().contains(q)
                    || m.pharmacy.toLowerCase().contains(q);

            // Type chip filter
            boolean chipMatch = true;
            if ("OTC".equals(activeFilter)) {
                chipMatch = "OTC".equals(m.type);
            } else if ("Prescription".equals(activeFilter)) {
                chipMatch = "Prescription".equals(m.type);
            }

            if (modeMatch && searchMatch && chipMatch) filtered.add(m);
        }

        // Sort by price
        if (activeFilter.contains("Low→High")) {
            filtered.sort((a, b) -> a.price - b.price);
        } else if (activeFilter.contains("High→Low")) {
            filtered.sort((a, b) -> b.price - a.price);
        }

        if (filtered.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No medicines found");
            empty.setTextColor(getResources().getColor(R.color.pg_sub, null));
            empty.setTextSize(13);
            empty.setPadding(0, dp(64), 0, 0);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            medicineListContainer.addView(empty);
            return;
        }

        tvScreenTitle.setText(filtered.size() + " result" + (filtered.size() == 1 ? "" : "s") + " found");

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Medicine m : filtered) {
            View card = inflater.inflate(R.layout.item_medicine_card, medicineListContainer, false);
            ((TextView) card.findViewById(R.id.tvMedicineName)).setText(m.name);
            ((TextView) card.findViewById(R.id.tvMedicineCategory))
                    .setText(m.category + " · " + m.type + " · " + m.pharmacy);
            ((TextView) card.findViewById(R.id.tvMedicinePrice)).setText("Rs. " + m.price);

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, MedicineDetailsActivity.class);
                intent.putExtra("medicine_name",     m.name);
                intent.putExtra("medicine_price",    m.price);
                intent.putExtra("medicine_type",     m.type);
                intent.putExtra("medicine_category", m.category);
                intent.putExtra("medicine_pharmacy", m.pharmacy);
                startActivity(intent);
            });

            card.findViewById(R.id.btnAddToCartCard).setOnClickListener(v ->
                    Toast.makeText(this, m.name + " added to cart!", Toast.LENGTH_SHORT).show());

            medicineListContainer.addView(card);
        }
    }

    private void loadSampleData() {
        allMedicines.add(new Medicine("Paracetamol 500mg",   "Pain relief",   "OTC",          40,  "otc",      "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Panadol Extra",        "Pain relief",   "OTC",          55,  "otc",      "City Pharma"));
        allMedicines.add(new Medicine("Amoxicillin 500mg",    "Antibiotic",    "Prescription", 85,  "rx",       "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Azithromycin 500mg",   "Antibiotic",    "Prescription", 220, "rx",       "City Pharma"));
        allMedicines.add(new Medicine("Omeprazole 20mg",      "Gastric",       "Prescription", 75,  "rx",       "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Metformin 500mg",      "Diabetes",      "Prescription", 35,  "chronic",  "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Atorvastatin 10mg",    "Cholesterol",   "Prescription", 120, "chronic",  "City Pharma"));
        allMedicines.add(new Medicine("Amlodipine 5mg",       "Blood pressure","Prescription", 95,  "chronic",  "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Vitamin C 1000mg",     "Supplement",    "OTC",          120, "vitamins", "City Pharma"));
        allMedicines.add(new Medicine("Vitamin D3 1000IU",    "Supplement",    "OTC",          180, "vitamins", "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Multivitamin Daily",   "Supplement",    "OTC",          650, "vitamins", "City Pharma"));
        allMedicines.add(new Medicine("Band-Aid Pack",        "First Aid",     "OTC",          250, "firstaid", "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Dettol Antiseptic",    "First Aid",     "OTC",          320, "firstaid", "City Pharma"));
        allMedicines.add(new Medicine("Eye Drops Refresh",    "Eye care",      "OTC",          450, "eyecare",  "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Colgate Sensitive",    "Dental",        "OTC",          380, "dental",   "City Pharma"));
        allMedicines.add(new Medicine("Baby Gripe Water",     "Baby care",     "OTC",          290, "baby",     "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Ibuprofen 400mg",      "Pain relief",   "OTC",          65,  "otc",      "City Pharma"));
        allMedicines.add(new Medicine("Antacid Tablets",      "Gastric",       "OTC",          95,  "otc",      "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Cetirizine 10mg",      "Allergy",       "OTC",          38,  "otc",      "City Pharma"));
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    static class Medicine {
        String name, category, type, categoryKey, pharmacy;
        int price;
        Medicine(String name, String category, String type, int price,
                 String categoryKey, String pharmacy) {
            this.name = name; this.category = category; this.type = type;
            this.price = price; this.categoryKey = categoryKey; this.pharmacy = pharmacy;
        }
    }
}