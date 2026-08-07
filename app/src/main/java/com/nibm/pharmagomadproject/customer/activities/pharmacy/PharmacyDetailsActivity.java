package com.nibm.pharmagomadproject.customer.activities.pharmacy;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineDetailsActivity;
import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineListActivity;
import com.nibm.pharmagomadproject.customer.activities.medicine.PrescriptionUploadActivity;
import com.nibm.pharmagomadproject.customer.activities.order.CartActivity;
import com.nibm.pharmagomadproject.customer.models.Cart;
import com.nibm.pharmagomadproject.customer.models.Medicine;

import java.util.ArrayList;
import java.util.List;

public class PharmacyDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PHARMACY_NAME = "pharmacy_name";
    public static final String EXTRA_PHARMACY_DISTANCE = "pharmacy_distance";
    public static final String EXTRA_PHARMACY_RATING = "pharmacy_rating";
    public static final String EXTRA_PHARMACY_HOURS = "pharmacy_hours";

    private LinearLayout medicineListContainer;
    private LinearLayout filterChipsContainer;
    private TextView tvMedicineCount;

    private String pharmacyId = "";
    private String ownerId = "";
    private String pharmacyName = "Pharmacy";
    private String activeFilter = "All";

    private final List<Medicine> allMedicines = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pharmacy_details);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        medicineListContainer = findViewById(R.id.medicineListContainer);
        filterChipsContainer  = findViewById(R.id.filterChipsContainer);
        tvMedicineCount       = findViewById(R.id.tvMedicineCount);

        pharmacyId   = getIntent().getStringExtra("pharmacyId");
        ownerId      = getIntent().getStringExtra("ownerId");
        pharmacyName = getIntent().getStringExtra("name");
        if (pharmacyName == null) {
            pharmacyName = getIntent().getStringExtra(EXTRA_PHARMACY_NAME);
        }
        if (pharmacyName == null) {
            pharmacyName = "Pharmacy";
        }

        String distance = getIntent().getStringExtra(EXTRA_PHARMACY_DISTANCE);
        String rating   = getIntent().getStringExtra(EXTRA_PHARMACY_RATING);
        String hours    = getIntent().getStringExtra(EXTRA_PHARMACY_HOURS);

        if (distance == null) distance = "—";
        if (hours == null)    hours = "8:00 AM - 10:00 PM";

        ((TextView) findViewById(R.id.tvPharmacyName)).setText(pharmacyName);
        ((TextView) findViewById(R.id.tvPharmacyFullName)).setText(pharmacyName);
        ((TextView) findViewById(R.id.tvDistance)).setText(distance);
        ((TextView) findViewById(R.id.tvRating)).setText("⭐ —");
        ((TextView) findViewById(R.id.tvHours)).setText(hours);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent intent = new Intent(this, MedicineListActivity.class);
            intent.putExtra(MedicineListActivity.EXTRA_MODE, "pharmacy");
            intent.putExtra(MedicineListActivity.EXTRA_TITLE, pharmacyName);
            intent.putExtra(MedicineListActivity.EXTRA_QUERY, (ownerId != null && !ownerId.isEmpty()) ? ownerId : pharmacyName);
            startActivity(intent);
        });

        // Load real pharmacy details from Firestore to replace placeholders
        if (pharmacyId != null && !pharmacyId.isEmpty()) {
            loadPharmacyRatingById(pharmacyId);
        } else if (ownerId != null && !ownerId.isEmpty()) {
            // Fallback: resolve pharmacy document via ownerId
            db.collection("pharmacies").whereEqualTo("ownerId", ownerId).limit(1).get()
                    .addOnSuccessListener(snap -> {
                        if (snap != null && !snap.isEmpty()) {
                            loadPharmacyRatingById(snap.getDocuments().get(0).getId());
                        }
                    });
        }

        loadMedicines();
        buildFilterChips();
    }

    private void loadPharmacyRatingById(String docId) {
        db.collection("pharmacies").document(docId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String name = doc.getString("name");
                    String addr = doc.getString("address");
                    String phone = doc.getString("phone");
                    Double ratingVal = doc.getDouble("rating");

                    if (name != null) {
                        pharmacyName = name;
                        TextView tvN = findViewById(R.id.tvPharmacyName);
                        TextView tvF = findViewById(R.id.tvPharmacyFullName);
                        if (tvN != null) tvN.setText(name);
                        if (tvF != null) tvF.setText(name);
                    }
                    if (addr != null) {
                        TextView tvAddr = findViewById(R.id.tvAddress);
                        if (tvAddr != null) tvAddr.setText(addr);
                    }
                    if (phone != null) {
                        TextView tvPh = findViewById(R.id.tvPhone);
                        if (tvPh != null) {
                            tvPh.setText(phone);
                            tvPh.setOnClickListener(v -> {
                                try {
                                    startActivity(new Intent(Intent.ACTION_DIAL,
                                            android.net.Uri.parse("tel:" + phone.trim())));
                                } catch (Exception ignored) {}
                            });
                        }
                    }
                    TextView tvR = findViewById(R.id.tvRating);
                    if (tvR != null) {
                        if (ratingVal != null && ratingVal > 0) {
                            Long countVal = doc.getLong("ratingCount");
                            String countStr = (countVal != null && countVal > 0) ? " (" + countVal + " reviews)" : "";
                            tvR.setText("⭐ " + String.format(java.util.Locale.getDefault(), "%.1f", ratingVal) + countStr);
                        } else {
                            tvR.setText("⭐ —");
                        }
                    }
                });
    }

    private void loadMedicines() {
        String queryId = (ownerId != null && !ownerId.isEmpty()) ? ownerId : pharmacyId;
        if (queryId == null || queryId.isEmpty()) return;

        db.collection("medicines")
                .whereEqualTo("pharmacyId", queryId)
                .get()
                .addOnSuccessListener(query -> {
                    allMedicines.clear();
                    for (DocumentSnapshot doc : query) {
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
                        m.setPharmacyId(doc.getString("pharmacyId"));
                        m.setImageUrl(doc.getString("imageUrl"));
                        allMedicines.add(m);
                    }
                    renderMedicines();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load medicines: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }





    private void buildFilterChips() {
        String[] filters = {
                "All", "Price: Low to High", "Price: High to Low", "OTC", "Prescription", "Rx",
                "First Aid", "Vitamins", "Chronic", "Baby", "Eye Care", "Dental"
        };

        filterChipsContainer.removeAllViews();

        for (String filter : filters) {
            TextView chip = new TextView(this);
            chip.setText(filter);
            chip.setTextSize(12);
            chip.setPadding(dp(15), dp(7), dp(15), dp(7));

            applyChipStyle(chip, filter.equals(activeFilter));

            chip.setOnClickListener(v -> {
                activeFilter = filter;
                buildFilterChips();
                renderMedicines();
            });

            filterChipsContainer.addView(chip);
        }
    }

    private void applyChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(getColor(R.color.pg_primary));
            chip.setTypeface(null, Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(getColor(R.color.pg_sub));
            chip.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void renderMedicines() {
        medicineListContainer.removeAllViews();
        List<Medicine> list = new ArrayList<>();

        for (Medicine m : allMedicines) {
            if ("All".equalsIgnoreCase(activeFilter) || activeFilter.startsWith("Price")) {
                list.add(m);
            } else if ((m.getType() != null && m.getType().equalsIgnoreCase(activeFilter))
                    || (m.getCategory() != null && m.getCategory().equalsIgnoreCase(activeFilter))
                    || (m.getType() != null && activeFilter.replace(" ", "").equalsIgnoreCase(m.getType().replace(" ", "")))
                    || (m.getCategory() != null && activeFilter.replace(" ", "").equalsIgnoreCase(m.getCategory().replace(" ", "")))) {
                list.add(m);
            }
        }

        if (activeFilter.contains("Low to High") || activeFilter.equals("Price Low")) {
            list.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        } else if (activeFilter.contains("High to Low") || activeFilter.equals("Price High")) {
            list.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        }




        tvMedicineCount.setText(
                list.size()+" medicines available"
        );




        LayoutInflater inflater =
                LayoutInflater.from(this);



        for(Medicine m:list){
            View card = inflater.inflate(R.layout.item_medicine_card, medicineListContainer, false);

            TextView tvName = card.findViewById(R.id.tvMedicineName);
            if (tvName != null) tvName.setText(m.getMedicineName());

            TextView tvCat = card.findViewById(R.id.tvMedicineCategory);
            if (tvCat != null) {
                String catStr = m.getCategory() != null ? m.getCategory() : "";
                String typeStr = m.getType() != null ? m.getType() : "";
                tvCat.setText(catStr.isEmpty() ? typeStr : catStr + " • " + typeStr);
            }

            TextView tvPrice = card.findViewById(R.id.tvMedicinePrice);
            if (tvPrice != null) tvPrice.setText("Rs. " + (int) m.getPrice());

            TextView tvRxBadge = card.findViewById(R.id.tvRxBadge);
            if (tvRxBadge != null) {
                boolean isRx = "Prescription".equalsIgnoreCase(m.getType())
                        || "Rx".equalsIgnoreCase(m.getType())
                        || "Prescription".equalsIgnoreCase(m.getCategory())
                        || "Rx".equalsIgnoreCase(m.getCategory());
                tvRxBadge.setVisibility(isRx ? View.VISIBLE : View.GONE);
            }

            ImageView ivImage = card.findViewById(R.id.ivMedicineImage);
            ImageView ivIcon  = card.findViewById(R.id.ivMedicineIcon);
            String imgUrl = m.getImageUrl();
            if (ivImage != null && ivIcon != null) {
                if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                    ivIcon.setVisibility(View.GONE);
                    ivImage.setVisibility(View.VISIBLE);
                    com.bumptech.glide.Glide.with(this)
                            .load(imgUrl.trim())
                            .placeholder(R.drawable.ic_pill)
                            .error(R.drawable.ic_pill)
                            .centerCrop()
                            .into(ivImage);
                } else {
                    ivImage.setVisibility(View.GONE);
                    ivIcon.setVisibility(View.VISIBLE);
                }
            }

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, MedicineDetailsActivity.class);
                intent.putExtra("medicine_id",       m.getMedicineId());
                intent.putExtra("brand_name",        m.getBrandName());
                intent.putExtra("pharmacy_id",       m.getPharmacyId());
                intent.putExtra("medicine_name",     m.getMedicineName());
                intent.putExtra("medicine_price",    (int) m.getPrice());
                intent.putExtra("medicine_type",     m.getType());
                intent.putExtra("medicine_category", m.getCategory());
                intent.putExtra("medicine_pharmacy", pharmacyName);
                intent.putExtra("medicine_image",    m.getImageUrl());
                startActivity(intent);
            });

            card.findViewById(R.id.btnAddToCartCard)
                    .setOnClickListener(v -> {
                        Cart cartItem = new Cart(
                                m.getMedicineId(),
                                m.getMedicineName(),
                                m.getBrandName(),
                                m.getPharmacyId(),
                                m.getPharmacy(),
                                m.getPrice(),
                                1
                        );
                        cartItem.setMedicineType(m.getType());
                        CartActivity.addToCart(cartItem);
                        Toast.makeText(
                                this,
                                m.getMedicineName() + " added to cart",
                                Toast.LENGTH_SHORT
                        ).show();
                    });



            medicineListContainer.addView(card);


        }


    }





    private int dp(int value){

        return (int)(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );

    }


}