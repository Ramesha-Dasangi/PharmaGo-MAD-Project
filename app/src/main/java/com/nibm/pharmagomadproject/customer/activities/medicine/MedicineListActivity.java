package com.nibm.pharmagomadproject.customer.activities.medicine;

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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.order.CartActivity;
import com.nibm.pharmagomadproject.customer.models.Cart;
import com.nibm.pharmagomadproject.customer.models.Medicine;

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
    private androidx.recyclerview.widget.RecyclerView medicineRecyclerView;
    private com.nibm.pharmagomadproject.customer.adapter.MedicineAdapter medicineAdapter;
    private LinearLayout typeChipsContainer, pharmacyCheckboxContainer,
            activeFiltersRow, layoutPharmacyFilter;
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
    private final Map<String, String> pharmacyNameById = new HashMap<>(); // ownerId -> pharmacy display name

    private FirebaseFirestore db;

    // Type chips
    private final String[] TYPE_CHIPS = {
            "All", "OTC", "Prescription", "Rx",
            "First Aid", "Vitamins", "Chronic", "Baby", "Eye Care", "Dental"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_list);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        // Bind views
        drawerLayout              = findViewById(R.id.drawerLayout);
        medicineRecyclerView      = findViewById(R.id.medicineRecyclerView);
        typeChipsContainer        = findViewById(R.id.typeChipsContainer);
        pharmacyCheckboxContainer = findViewById(R.id.pharmacyCheckboxContainer);
        activeFiltersRow          = findViewById(R.id.activeFiltersRow);
        layoutPharmacyFilter      = findViewById(R.id.layoutPharmacyFilter);
        etSearch                  = findViewById(R.id.etSearch);
        btnClearSearch            = findViewById(R.id.btnClearSearch);
        tvScreenTitle             = findViewById(R.id.tvScreenTitle);
        tvActiveFilters           = findViewById(R.id.tvActiveFilters);
        rgPriceSort               = findViewById(R.id.rgPriceSort);
        etMinPrice                = findViewById(R.id.etMinPrice);
        etMaxPrice                = findViewById(R.id.etMaxPrice);

        medicineAdapter = new com.nibm.pharmagomadproject.customer.adapter.MedicineAdapter(this, new ArrayList<>(), new com.nibm.pharmagomadproject.customer.adapter.MedicineAdapter.OnMedicineClickListener() {
            @Override
            public void onCardClick(Medicine medicine) {
                Intent intent = new Intent(MedicineListActivity.this, MedicineDetailsActivity.class);
                intent.putExtra("medicine_id",       medicine.getMedicineId());
                intent.putExtra("brand_name",        medicine.getBrandName());
                intent.putExtra("pharmacy_id",       medicine.getPharmacyId());
                intent.putExtra("medicine_name",     medicine.getMedicineName());
                intent.putExtra("medicine_price",    (int) medicine.getPrice());
                intent.putExtra("medicine_type",     medicine.getType());
                intent.putExtra("medicine_category", medicine.getCategory());
                intent.putExtra("medicine_pharmacy", medicine.getPharmacy());
                intent.putExtra("medicine_image",    medicine.getImageUrl());
                startActivity(intent);
            }

            @Override
            public void onAddToCartClick(Medicine medicine) {
                CartActivity.addToCart(new Cart(
                        medicine.getMedicineId(),
                        medicine.getMedicineName(),
                        medicine.getBrandName(),
                        medicine.getPharmacyId(),
                        medicine.getPharmacy(),
                        medicine.getPrice(),
                        1
                ));
                Toast.makeText(MedicineListActivity.this, medicine.getMedicineName() + " added to cart!", Toast.LENGTH_SHORT).show();
            }
        });

        if (medicineRecyclerView != null) {
            medicineRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            medicineRecyclerView.setAdapter(medicineAdapter);
        }

        // Intent extras
        mode  = getIntent().getStringExtra(EXTRA_MODE)  != null ? getIntent()
                .getStringExtra(EXTRA_MODE)  : "search";
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        query = getIntent().getStringExtra(EXTRA_QUERY) != null ? getIntent()
                .getStringExtra(EXTRA_QUERY) : "";
        if (title != null) tvScreenTitle.setText(title);

        if ("pharmacy".equals(mode)) {
            if (layoutPharmacyFilter != null) {
                layoutPharmacyFilter.setVisibility(View.GONE);
            }
            View btnOpenFilter = findViewById(R.id.btnOpenFilter);
            if (btnOpenFilter != null) btnOpenFilter.setVisibility(View.GONE);
        } else {
            if (layoutPharmacyFilter != null) {
                layoutPharmacyFilter.setVisibility(View.VISIBLE);
            }
            View btnOpenFilter = findViewById(R.id.btnOpenFilter);
            if (btnOpenFilter != null) btnOpenFilter.setVisibility(View.VISIBLE);
        }

        // Pre-select the matching category chip when opened via category shortcut
        if ("category".equals(mode) && !query.isEmpty()) {
            for (String chip : TYPE_CHIPS) {
                String chipClean = chip.replace(" ", "").toLowerCase();
                String queryClean = query.replace(" ", "").toLowerCase();
                if (chipClean.equals(queryClean)) {
                    activeType = chip;
                    break;
                }
            }
        }

        loadMedicinesFromFirestore();

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

        // Search bar — always visible so users can search in any mode
        etSearch.setVisibility(View.VISIBLE);
        if ("search".equals(mode) || "pharmacy".equals(mode)) {
            etSearch.requestFocus();
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
            if (!pharmacies.contains(m.getPharmacy())) pharmacies.add(m.getPharmacy());
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
                refreshList();
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
        // Use search text in ALL modes so users can search within a pharmacy / category too
        String q = etSearch.getText() != null ? etSearch.getText().toString().toLowerCase().trim() : "";

        List<Medicine> filtered = new ArrayList<>();

        for (Medicine m : allMedicines) {
            // Hidden pharmacy
            if (hiddenPharmacies.contains(m.getPharmacy())) continue;

            // Mode filter
            boolean modeMatch = true;
            if ("category".equals(mode)) {
                String catFieldClean = m.getCategory() != null ? m.getCategory().replace(" ", "").toLowerCase() : "";
                String queryClean = query.replace(" ", "").toLowerCase();
                modeMatch = catFieldClean.equals(queryClean);
            } else if ("pharmacy".equals(mode)) {
                modeMatch = (m.getPharmacyId() != null && m.getPharmacyId().equalsIgnoreCase(query))
                        || (m.getPharmacy() != null && m.getPharmacy().equalsIgnoreCase(query));
            }

            // Search text
            boolean searchMatch = q.isEmpty()
                    || m.getMedicineName().toLowerCase().contains(q)
                    || m.getCategory().toLowerCase().contains(q)
                    || m.getPharmacy().toLowerCase().contains(q);

            // Type chip
            boolean typeMatch = "All".equals(activeType)
                    || m.getType().equalsIgnoreCase(activeType)
                    || m.getCategory().equalsIgnoreCase(activeType);

            // Price range
            boolean priceMatch = m.getPrice() >= filterMinPrice && m.getPrice() <= filterMaxPrice;

            if (modeMatch && searchMatch && typeMatch && priceMatch) {
                filtered.add(m);
            }
        }

        // Sort
        if ("low_high".equals(priceSortMode)) {
            filtered.sort((a, b) -> (int) (a.getPrice() - b.getPrice()));
        } else if ("high_low".equals(priceSortMode)) {
            filtered.sort((a, b) -> (int) (b.getPrice() - a.getPrice()));
        }

        // Active filters summary
        updateActiveFiltersBadge();

        if (filtered.isEmpty()) {
            tvScreenTitle.setText("No medicines found");
        } else {
            tvScreenTitle.setText(filtered.size() + " result" +
                    (filtered.size() == 1 ? "" : "s") + " found");
        }

        if (medicineAdapter != null) {
            medicineAdapter.updateList(filtered);
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

    private void loadMedicinesFromFirestore() {
        // Step 1: build a map of approved pharmacy ownerId -> pharmacy display name
        db.collection("pharmacies")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(pharmacySnaps -> {
                    pharmacyNameById.clear();
                    for (DocumentSnapshot doc : pharmacySnaps) {
                        String ownerId = doc.getString("ownerId");
                        String name    = doc.getString("name");
                        if (ownerId != null) {
                            pharmacyNameById.put(ownerId, name != null ? name : "Pharmacy");
                        }
                    }
                    loadMedicineDocs();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load pharmacies: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    loadMedicineDocs();
                });
    }

    private void loadMedicineDocs() {
        db.collection("medicines")
                .get()
                .addOnSuccessListener(query -> {
                    allMedicines.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String pharmacyId = doc.getString("pharmacyId");
                        String pharmacyName = null;
                        if (pharmacyId != null) {
                            pharmacyName = pharmacyNameById.get(pharmacyId);
                        }
                        if (pharmacyName == null) {
                            pharmacyName = doc.getString("pharmacy");
                        }
                        if (pharmacyName == null) {
                            pharmacyName = "Pharmacy";
                        }

                        double price = doc.getDouble("price") != null ? doc.getDouble("price") : 0;
                        Long stockLong = doc.getLong("stock");
                        int stock = stockLong != null ? stockLong.intValue() : 0;

                        Medicine m = new Medicine(
                                doc.getId(),
                                doc.getString("brand"),
                                doc.getString("medicineName"),
                                doc.getString("category"),
                                doc.getString("type"),
                                price,
                                stock,
                                pharmacyName
                        );
                        m.setPharmacyId(pharmacyId);
                        m.setImageUrl(doc.getString("imageUrl"));
                        allMedicines.add(m);
                    }

                    buildTypeChips();
                    buildPharmacyCheckboxes();
                    refreshList();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load medicines: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}