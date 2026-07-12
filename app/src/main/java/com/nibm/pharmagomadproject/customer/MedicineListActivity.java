package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MedicineListActivity extends AppCompatActivity {

    public static final String EXTRA_MODE  = "mode";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_QUERY = "query";

    // Views
    private DrawerLayout drawerLayout;
    private LinearLayout medicineListContainer, typeChipsContainer, pharmacyCheckboxContainer,
            activeFiltersRow;
    private EditText            etSearch;
    private ImageView           btnClearSearch;
    private TextView            tvScreenTitle, tvActiveFilters;
    private RadioGroup          rgPriceSort;
    private TextInputEditText   etMinPrice, etMaxPrice;

    // Filter state
    private String mode          = "search";
    private String query         = "";
    private String activeType    = "All";   // type chip
    private String priceSortMode = "none";  // "none" / "low_high" / "high_low"
    private boolean showPriceBars = true;
    private int filterMinPrice   = 0;
    private int filterMaxPrice   = Integer.MAX_VALUE;

    private final Set<String>  hiddenPharmacies = new HashSet<>();
    private final List<Medicine> allMedicines   = new ArrayList<>();

    // Type chips
    private final String[] TYPE_CHIPS = {
            "All", "OTC", "Prescription",
            "Pain relief", "Antibiotic", "Supplement",
            "Gastric", "Diabetes", "Blood pressure", "First Aid"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_list);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Bind views
        drawerLayout              = findViewById(R.id.drawerLayout);
        medicineListContainer     = findViewById(R.id.medicineListContainer);
        typeChipsContainer        = findViewById(R.id.typeChipsContainer);
        pharmacyCheckboxContainer = findViewById(R.id.pharmacyCheckboxContainer);
        activeFiltersRow          = findViewById(R.id.activeFiltersRow);
        etSearch                  = findViewById(R.id.etSearch);
        btnClearSearch            = findViewById(R.id.btnClearSearch);
        tvScreenTitle             = findViewById(R.id.tvScreenTitle);
        tvActiveFilters           = findViewById(R.id.tvActiveFilters);
        rgPriceSort               = findViewById(R.id.rgPriceSort);
        etMinPrice                = findViewById(R.id.etMinPrice);
        etMaxPrice                = findViewById(R.id.etMaxPrice);

        // Intent extras
        mode  = getIntent().getStringExtra(EXTRA_MODE)  != null ? getIntent()
                .getStringExtra(EXTRA_MODE)  : "search";
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        query = getIntent().getStringExtra(EXTRA_QUERY) != null ? getIntent()
                .getStringExtra(EXTRA_QUERY) : "";
        if (title != null) tvScreenTitle.setText(title);

        loadSampleData();

        // Top bar buttons
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Filter icon - open right drawer
        findViewById(R.id.btnOpenFilter).setOnClickListener(v ->
                drawerLayout.openDrawer(Gravity.END));

        // Close drawer button
        findViewById(R.id.btnCloseFilter).setOnClickListener(v ->
                drawerLayout.closeDrawer(Gravity.END));

        // Clear all filters
        findViewById(R.id.btnClearFilters).setOnClickListener(v -> {
            activeType    = "All";
            priceSortMode = "none";
            hiddenPharmacies.clear();
            filterMinPrice = 0;
            filterMaxPrice = Integer.MAX_VALUE;
            rgPriceSort.check(R.id.rbSortNone);
            if (etMinPrice != null) etMinPrice.setText("");
            if (etMaxPrice != null) etMaxPrice.setText("");
            buildTypeChips();
            buildPharmacyCheckboxes();
            refreshList();
        });

        // Apply filter button in drawer
        findViewById(R.id.btnApplyFilter).setOnClickListener(v -> {
            // Read price sort
            int checkedId = rgPriceSort.getCheckedRadioButtonId();
            if (checkedId == R.id.rbSortLowHigh)  priceSortMode = "low_high";
            else if (checkedId == R.id.rbSortHighLow) priceSortMode = "high_low";
            else priceSortMode = "none";

            // Read price range
            try {
                String minStr = etMinPrice.getText() != null ? etMinPrice.getText().toString() : "";
                String maxStr = etMaxPrice.getText() != null ? etMaxPrice.getText().toString() : "";
                filterMinPrice = TextUtils.isEmpty(minStr) ? 0 : Integer.parseInt(minStr);
                filterMaxPrice = TextUtils.isEmpty(maxStr) ?
                        Integer.MAX_VALUE : Integer.parseInt(maxStr);
            } catch (NumberFormatException ignored) {
                filterMinPrice = 0;
                filterMaxPrice = Integer.MAX_VALUE;
            }

            drawerLayout.closeDrawer(Gravity.END);
            refreshList();
        });

        // Search bar
        if ("search".equals(mode)) {
            etSearch.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
        } else {
            etSearch.setVisibility(View.GONE);
            btnClearSearch.setVisibility(View.GONE);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                refreshList();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { refreshList(); return true; }
            return false;
        });

        // Build UI
        buildTypeChips();
        buildPharmacyCheckboxes();
        refreshList();
    }

    // TYPE CHIPS (horizontal row)
    private void buildTypeChips() {
        typeChipsContainer.removeAllViews();

        // Left padding spacer
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(dp(4), 1));
        typeChipsContainer.addView(spacer);

        for (String chipLabel : TYPE_CHIPS) {
            TextView chip = new TextView(this);
            chip.setText(chipLabel);
            chip.setTextSize(12);
            chip.setPadding(dp(14), dp(7), dp(14), dp(7));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(7));
            chip.setLayoutParams(lp);
            chip.setClickable(true);
            chip.setFocusable(true);

            boolean selected = chipLabel.equals(activeType);
            chip.setBackgroundResource(selected
                    ? R.drawable.bg_chip_selected
                    : R.drawable.bg_chip_unselected);
            chip.setTextColor(getResources().getColor(
                    selected ? R.color.pg_primary : R.color.pg_sub, null));
            chip.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);

            chip.setOnClickListener(v -> {
                activeType = chipLabel;
                buildTypeChips();
                refreshList();
            });

            typeChipsContainer.addView(chip);
        }
    }

    // PHARMACY CHECKBOXES (in sidebar)
    private void buildPharmacyCheckboxes() {
        pharmacyCheckboxContainer.removeAllViews();

        // Get unique pharmacies from data
        List<String> pharmacies = new ArrayList<>();
        for (Medicine m : allMedicines) {
            if (!pharmacies.contains(m.pharmacy)) pharmacies.add(m.pharmacy);
        }

        for (String pharmacy : pharmacies) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(4);
            row.setLayoutParams(rowLp);
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackground(getDrawable(android.R.color.transparent));

            CheckBox cb = new CheckBox(this);
            cb.setChecked(!hiddenPharmacies.contains(pharmacy));
            cb.setButtonTintList(
                    android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.pg_primary, null)));
            cb.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView tvPharmacy = new TextView(this);
            tvPharmacy.setText(pharmacy);
            tvPharmacy.setTextColor(getResources().getColor(R.color.pg_text, null));
            tvPharmacy.setTextSize(13);
            tvPharmacy.setPadding(dp(8), dp(10), 0, dp(10));
            LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvPharmacy.setLayoutParams(tvLp);

            // Click row or checkbox — toggle
            View.OnClickListener toggleListener = v -> {
                if (hiddenPharmacies.contains(pharmacy)) {
                    hiddenPharmacies.remove(pharmacy);
                    cb.setChecked(true);
                } else {
                    hiddenPharmacies.add(pharmacy);
                    cb.setChecked(false);
                }
            };
            row.setOnClickListener(toggleListener);
            cb.setOnClickListener(toggleListener);

            row.addView(cb);
            row.addView(tvPharmacy);
            pharmacyCheckboxContainer.addView(row);
        }
    }

    // FILTER + RENDER
    private void refreshList() {
        medicineListContainer.removeAllViews();

        String q = "search".equals(mode)
                ? (etSearch.getText() != null ? etSearch.getText()
                .toString().toLowerCase().trim() : "") : "";

        List<Medicine> filtered = new ArrayList<>();

        for (Medicine m : allMedicines) {
            // Hidden pharmacy
            if (hiddenPharmacies.contains(m.pharmacy)) continue;

            // Mode filter
            boolean modeMatch = true;
            if ("category".equals(mode)) {
                modeMatch = m.categoryKey.equalsIgnoreCase(query);
            } else if ("pharmacy".equals(mode)) {
                modeMatch = m.pharmacy.equalsIgnoreCase(query);
            }

            // Search text
            boolean searchMatch = q.isEmpty()
                    || m.name.toLowerCase().contains(q)
                    || m.category.toLowerCase().contains(q)
                    || m.pharmacy.toLowerCase().contains(q);

            // Type chip
            boolean typeMatch = "All".equals(activeType)
                    || m.type.equalsIgnoreCase(activeType)
                    || m.category.equalsIgnoreCase(activeType);

            // Price range
            boolean priceMatch = m.price >= filterMinPrice && m.price <= filterMaxPrice;

            if (modeMatch && searchMatch && typeMatch && priceMatch) {
                filtered.add(m);
            }
        }

        // Sort
        if ("low_high".equals(priceSortMode)) {
            filtered.sort((a, b) -> a.price - b.price);
        } else if ("high_low".equals(priceSortMode)) {
            filtered.sort((a, b) -> b.price - a.price);
        }

        // Active filters summary
        updateActiveFiltersBadge();

        if (filtered.isEmpty()) {
            tvScreenTitle.setText("No medicines found");
            TextView empty = new TextView(this);
            empty.setText("Try adjusting your filters");
            empty.setTextColor(getResources().getColor(R.color.pg_sub, null));
            empty.setTextSize(13);
            empty.setPadding(0, dp(64), 0, 0);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            medicineListContainer.addView(empty);
            return;
        }

        tvScreenTitle.setText(filtered.size() + " result" +
                (filtered.size() == 1 ? "" : "s") + " found");

        // Price comparison: group by medicine name, collect prices
        Map<String, List<Medicine>> priceMap = new HashMap<>();
        for (Medicine m : filtered) {
            if (!priceMap.containsKey(m.name)) priceMap.put(m.name, new ArrayList<>());
            priceMap.get(m.name).add(m);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Medicine m : filtered) {
            View card = inflater.inflate(R.layout.item_medicine_card, medicineListContainer,
                    false);
            ((TextView) card.findViewById(R.id.tvMedicineName)).setText(m.name);
            ((TextView) card.findViewById(R.id.tvMedicineCategory))
                    .setText(m.category + " · " + m.type);

            // Price + best price indicator
            List<Medicine> sameNameList = priceMap.get(m.name);
            int minPrice = m.price;
            if (sameNameList != null) {
                for (Medicine sm : sameNameList) {
                    if (sm.price < minPrice) minPrice = sm.price;
                }
            }
            TextView tvPrice = card.findViewById(R.id.tvMedicinePrice);
            tvPrice.setText("Rs. " + m.price);
            if (sameNameList != null && sameNameList.size() > 1 && m.price == minPrice) {
                // Best price — show green badge
                tvPrice.setTextColor(getResources().getColor(R.color.pg_primary, null));
                tvPrice.setText("Rs. " + m.price + " ✓ Best");
            } else {
                tvPrice.setTextColor(getResources().getColor(R.color.pg_text, null));
            }

            // Pharmacy name below price
            TextView tvCategory = card.findViewById(R.id.tvMedicineCategory);
            tvCategory.setText(m.category + " · " + m.type + "\n" + m.pharmacy);

            // Card click → MedicineDetailsActivity
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, MedicineDetailsActivity.class);
                intent.putExtra("medicine_name",     m.name);
                intent.putExtra("medicine_price",    m.price);
                intent.putExtra("medicine_type",     m.type);
                intent.putExtra("medicine_category", m.category);
                intent.putExtra("medicine_pharmacy", m.pharmacy);
                startActivity(intent);
            });

            // Add to cart
            card.findViewById(R.id.btnAddToCartCard).setOnClickListener(v ->
                    Toast.makeText(this, m.name + " added to cart!",
                            Toast.LENGTH_SHORT).show());

            medicineListContainer.addView(card);
        }
    }

    private void updateActiveFiltersBadge() {
        List<String> active = new ArrayList<>();
        if (!"All".equals(activeType))         active.add(activeType);
        if (!"none".equals(priceSortMode))     active.add("Price sorted");
        if (!hiddenPharmacies.isEmpty())        active.add(hiddenPharmacies.size() +
                " pharmacies hidden");
        if (filterMinPrice > 0)                 active.add("Min Rs." + filterMinPrice);
        if (filterMaxPrice < Integer.MAX_VALUE) active.add("Max Rs." + filterMaxPrice);

        if (active.isEmpty()) {
            activeFiltersRow.setVisibility(View.GONE);
        } else {
            activeFiltersRow.setVisibility(View.VISIBLE);
            tvActiveFilters.setText("Active: " + TextUtils.join(", ", active));
        }
    }

    private void loadSampleData() {
        allMedicines.add(new Medicine("Paracetamol 500mg",  "Pain relief",
                "OTC",          40,  "otc",      "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Paracetamol 500mg",  "Pain relief",
                "OTC",          38,  "otc",      "City Pharma"));
        allMedicines.add(new Medicine("Panadol Extra",       "Pain relief",
                "OTC",          55,  "otc",      "City Pharma"));
        allMedicines.add(new Medicine("Amoxicillin 500mg",   "Antibiotic",
                "Prescription", 85,  "rx",       "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Amoxicillin 500mg",   "Antibiotic",
                "Prescription", 90,  "rx",       "City Pharma"));
        allMedicines.add(new Medicine("Azithromycin 500mg",  "Antibiotic",
                "Prescription", 220, "rx",       "City Pharma"));
        allMedicines.add(new Medicine("Omeprazole 20mg",     "Gastric",
                "Prescription", 75,  "rx",       "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Omeprazole 20mg",     "Gastric",
                "Prescription", 80,  "rx",       "City Pharma"));
        allMedicines.add(new Medicine("Metformin 500mg",     "Diabetes",
                "Prescription", 35,  "chronic",  "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Atorvastatin 10mg",   "Diabetes",
                "Prescription", 120, "chronic",  "City Pharma"));
        allMedicines.add(new Medicine("Amlodipine 5mg",      "Blood pressure",
                "Prescription", 95,  "chronic",  "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Vitamin C 1000mg",    "Supplement",
                "OTC",          120, "vitamins", "City Pharma"));
        allMedicines.add(new Medicine("Vitamin C 1000mg",    "Supplement",
                "OTC",          115, "vitamins", "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Vitamin D3 1000IU",   "Supplement",
                "OTC",          180, "vitamins", "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Multivitamin Daily",  "Supplement",
                "OTC",          650, "vitamins", "City Pharma"));
        allMedicines.add(new Medicine("Band-Aid Pack",       "First Aid",
                "OTC",          250, "firstaid", "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Dettol Antiseptic",   "First Aid",
                "OTC",          320, "firstaid", "City Pharma"));
        allMedicines.add(new Medicine("Eye Drops Refresh",   "Eye care",
                "OTC",          450, "eyecare",  "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Ibuprofen 400mg",     "Pain relief",
                "OTC",          65,  "otc",      "City Pharma"));
        allMedicines.add(new Medicine("Antacid Tablets",     "Gastric",
                "OTC",          95,  "otc",      "MediCare Pharmacy"));
        allMedicines.add(new Medicine("Cetirizine 10mg",     "Antibiotic",
                "OTC",          38,  "otc",      "City Pharma"));
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    static class Medicine {
        String name, category, type, categoryKey, pharmacy;
        int price;
        Medicine(String n, String c, String t, int p, String ck, String ph) {
            name=n; category=c; type=t; price=p; categoryKey=ck; pharmacy=ph;
        }
    }
}