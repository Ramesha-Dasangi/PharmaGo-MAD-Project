package com.nibm.pharmagomadproject.customer.activities.pharmacy;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineDetailsActivity;
import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineListActivity;

import java.util.ArrayList;
import java.util.List;

public class PharmacyDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PHARMACY_NAME     = "pharmacy_name";
    public static final String EXTRA_PHARMACY_DISTANCE = "pharmacy_distance";
    public static final String EXTRA_PHARMACY_RATING   = "pharmacy_rating";
    public static final String EXTRA_PHARMACY_HOURS    = "pharmacy_hours";

    private LinearLayout medicineListContainer;
    private LinearLayout filterChipsContainer;
    private TextView tvMedicineCount;

    private String pharmacyName = "MediCare Pharmacy";
    private String activeFilter = "All";

    private final List<MedicineListActivity.Medicine> allMedicines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pharmacy_details);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        medicineListContainer = findViewById(R.id.medicineListContainer);
        filterChipsContainer  = findViewById(R.id.filterChipsContainer);
        tvMedicineCount       = findViewById(R.id.tvMedicineCount);

        // Get extras
        pharmacyName = getIntent().getStringExtra(EXTRA_PHARMACY_NAME) != null
                ? getIntent().getStringExtra(EXTRA_PHARMACY_NAME) : "Pharmacy";
        String distance = getIntent().getStringExtra(EXTRA_PHARMACY_DISTANCE) != null
                ? getIntent().getStringExtra(EXTRA_PHARMACY_DISTANCE) : "0.3 km away";
        String rating   = getIntent().getStringExtra(EXTRA_PHARMACY_RATING)   != null
                ? getIntent().getStringExtra(EXTRA_PHARMACY_RATING)   : "⭐ 4.8";
        String hours    = getIntent().getStringExtra(EXTRA_PHARMACY_HOURS)    != null
                ? getIntent().getStringExtra(EXTRA_PHARMACY_HOURS)    : "8:00 AM – 10:00 PM";

        // Set pharmacy info
        ((TextView) findViewById(R.id.tvPharmacyName)).setText(pharmacyName);
        ((TextView) findViewById(R.id.tvPharmacyFullName)).setText(pharmacyName);
        ((TextView) findViewById(R.id.tvDistance)).setText(distance);
        ((TextView) findViewById(R.id.tvRating)).setText(rating);
        ((TextView) findViewById(R.id.tvHours)).setText(hours);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Search icon → MedicineListActivity in search mode filtered to this pharmacy
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent intent = new Intent(this, MedicineListActivity.class);
            intent.putExtra(MedicineListActivity.EXTRA_MODE,  "pharmacy");
            intent.putExtra(MedicineListActivity.EXTRA_TITLE, pharmacyName);
            intent.putExtra(MedicineListActivity.EXTRA_QUERY, pharmacyName);
            startActivity(intent);
        });

        loadMedicines();
        buildFilterChips();
        renderMedicines();
    }

    private void loadMedicines() {
        // Medicines belonging to this pharmacy — same sample data as MedicineListActivity
        // In real app: query Firestore where pharmacyId == this pharmacy
        allMedicines.add(new MedicineListActivity.Medicine("Paracetamol 500mg",  "Pain relief",   "OTC",          40,  "otc",      pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Amoxicillin 500mg",  "Antibiotic",    "Prescription", 85,  "rx",       pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Omeprazole 20mg",    "Gastric",       "Prescription", 75,  "rx",       pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Metformin 500mg",    "Diabetes",      "Prescription", 35,  "chronic",  pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Amlodipine 5mg",     "Blood pressure","Prescription", 95,  "chronic",  pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Vitamin D3 1000IU",  "Supplement",    "OTC",          180, "vitamins", pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Band-Aid Pack",       "First Aid",     "OTC",          250, "firstaid", pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Eye Drops Refresh",   "Eye care",      "OTC",          450, "eyecare",  pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Baby Gripe Water",    "Baby care",     "OTC",          290, "baby",     pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Antacid Tablets",     "Gastric",       "OTC",          95,  "otc",      pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Cetirizine 10mg",     "Allergy",       "OTC",          38,  "otc",      pharmacyName));
        allMedicines.add(new MedicineListActivity.Medicine("Ibuprofen 400mg",     "Pain relief",   "OTC",          65,  "otc",      pharmacyName));
    }

    private void buildFilterChips() {
        String[] filters = {"All", "OTC", "Prescription", "Price: Low→High", "Price: High→Low"};
        filterChipsContainer.removeAllViews();

        for (String filter : filters) {
            TextView chip = new TextView(this);
            chip.setText(filter);
            chip.setTextSize(11);
            chip.setPadding(dp(14), dp(6), dp(14), dp(6));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(8);
            chip.setLayoutParams(lp);
            chip.setClickable(true);
            chip.setFocusable(true);

            applyChipStyle(chip, filter.equals(activeFilter));

            chip.setOnClickListener(v -> {
                activeFilter = filter;
                buildFilterChips(); // rebuild to update styles
                renderMedicines();
            });

            filterChipsContainer.addView(chip);
        }
    }

    private void applyChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(getResources().getColor(R.color.pg_primary, null));
            chip.setTypeface(null, Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(getResources().getColor(R.color.pg_sub, null));
            chip.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void renderMedicines() {
        medicineListContainer.removeAllViews();

        List<MedicineListActivity.Medicine> filtered = new ArrayList<>();
        for (MedicineListActivity.Medicine m : allMedicines) {
            if ("All".equals(activeFilter)) {
                filtered.add(m);
            } else if ("OTC".equals(activeFilter) && "OTC".equals(m.type)) {
                filtered.add(m);
            } else if ("Prescription".equals(activeFilter) && "Prescription".equals(m.type)) {
                filtered.add(m);
            } else if (activeFilter.contains("Low→High") || activeFilter.contains("High→Low")) {
                filtered.add(m);
            }
        }

        // Sort by price
        if (activeFilter.contains("Low→High")) {
            filtered.sort((a, b) -> a.price - b.price);
        } else if (activeFilter.contains("High→Low")) {
            filtered.sort((a, b) -> b.price - a.price);
        }

        tvMedicineCount.setText(filtered.size() + " medicines available");

        if (filtered.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No medicines found");
            empty.setTextColor(getResources().getColor(R.color.pg_sub, null));
            empty.setTextSize(13);
            empty.setPadding(0, dp(48), 0, 0);
            empty.setGravity(android.view.Gravity.CENTER);
            medicineListContainer.addView(empty);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (MedicineListActivity.Medicine m : filtered) {
            View card = inflater.inflate(R.layout.item_medicine_card, medicineListContainer, false);
            ((TextView) card.findViewById(R.id.tvMedicineName)).setText(m.name);
            ((TextView) card.findViewById(R.id.tvMedicineCategory))
                    .setText(m.category + " · " + m.type);
            ((TextView) card.findViewById(R.id.tvMedicinePrice)).setText("Rs. " + m.price);

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, MedicineDetailsActivity.class);
                intent.putExtra("medicine_name",     m.name);
                intent.putExtra("medicine_price",    m.price);
                intent.putExtra("medicine_type",     m.type);
                intent.putExtra("medicine_category", m.category);
                intent.putExtra("medicine_pharmacy", pharmacyName);
                startActivity(intent);
            });

            card.findViewById(R.id.btnAddToCartCard).setOnClickListener(v ->
                    Toast.makeText(this, m.name + " added to cart!", Toast.LENGTH_SHORT).show());

            medicineListContainer.addView(card);
        }
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}