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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineDetailsActivity;
import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineListActivity;
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

        if (distance == null) distance = "0.5 km away";
        if (rating == null)   rating = "⭐ 4.5";
        if (hours == null)    hours = "8:00 AM - 10:00 PM";

        ((TextView) findViewById(R.id.tvPharmacyName)).setText(pharmacyName);
        ((TextView) findViewById(R.id.tvPharmacyFullName)).setText(pharmacyName);
        ((TextView) findViewById(R.id.tvDistance)).setText(distance);
        ((TextView) findViewById(R.id.tvRating)).setText(rating);
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
            db.collection("pharmacies").document(pharmacyId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            String addr = doc.getString("address");
                            String phone = doc.getString("phone");
                            Double ratingVal = doc.getDouble("rating");
                            
                            if (name != null) {
                                pharmacyName = name;
                                ((TextView) findViewById(R.id.tvPharmacyName)).setText(name);
                                ((TextView) findViewById(R.id.tvPharmacyFullName)).setText(name);
                            }
                            if (addr != null) {
                                ((TextView) findViewById(R.id.tvAddress)).setText(addr);
                            }
                            if (phone != null) {
                                TextView tvPh = findViewById(R.id.tvPhone);
                                if (tvPh != null) {
                                    tvPh.setText(phone);
                                    tvPh.setOnClickListener(v -> {
                                        try {
                                            Intent dial = new Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + phone.trim()));
                                            startActivity(dial);
                                        } catch (Exception ignored) {}
                                    });
                                }
                            }
                            if (ratingVal != null) {
                                ((TextView) findViewById(R.id.tvRating)).setText("⭐ " + String.format("%.1f", ratingVal));
                            }
                        }
                    });
        }

        loadMedicines();
        buildFilterChips();
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





    private void buildFilterChips(){


        String[] filters = {
                "All",
                "OTC",
                "Prescription",
                "Price Low",
                "Price High"
        };


        filterChipsContainer.removeAllViews();



        for(String filter:filters){


            TextView chip = new TextView(this);

            chip.setText(filter);

            chip.setTextSize(12);

            chip.setPadding(
                    dp(15),
                    dp(7),
                    dp(15),
                    dp(7)
            );



            applyChipStyle(
                    chip,
                    filter.equals(activeFilter)
            );



            chip.setOnClickListener(v -> {

                activeFilter = filter;

                buildFilterChips();

                renderMedicines();

            });



            filterChipsContainer.addView(chip);

        }

    }





    private void applyChipStyle(TextView chip, boolean selected){


        if(selected){

            chip.setBackgroundResource(
                    R.drawable.bg_chip_selected
            );


            chip.setTextColor(
                    getColor(R.color.pg_primary)
            );


            chip.setTypeface(
                    null,
                    Typeface.BOLD
            );


        }
        else{


            chip.setBackgroundResource(
                    R.drawable.bg_chip_unselected
            );


            chip.setTextColor(
                    getColor(R.color.pg_sub)
            );


        }


    }





    private void renderMedicines(){


        medicineListContainer.removeAllViews();



        List<Medicine> list =
                new ArrayList<>();



        for(Medicine m:allMedicines){


            if(activeFilter.equals("All") || activeFilter.startsWith("Price")){

                list.add(m);

            }


            else if(activeFilter.equals("OTC")
                    &&
                    "OTC".equalsIgnoreCase(m.getType())){


                list.add(m);

            }


            else if(activeFilter.equals("Prescription")
                    &&
                    "Prescription".equalsIgnoreCase(m.getType())){


                list.add(m);

            }


        }





        if(activeFilter.equals("Price Low")){


            list.sort((a,b)->Double.compare(
                    a.getPrice(),
                    b.getPrice()
            ));

        }



        if(activeFilter.equals("Price High")){


            list.sort((a,b)->Double.compare(
                    b.getPrice(),
                    a.getPrice()
            ));


        }




        tvMedicineCount.setText(
                list.size()+" medicines available"
        );




        LayoutInflater inflater =
                LayoutInflater.from(this);



        for(Medicine m:list){



            View card =
                    inflater.inflate(
                            R.layout.item_medicine_card,
                            medicineListContainer,
                            false
                    );



            ((TextView)card.findViewById(
                    R.id.tvMedicineName))
                    .setText(
                            m.getMedicineName()
                    );



            ((TextView)card.findViewById(
                    R.id.tvMedicineCategory))
                    .setText(
                            m.getCategory()+" • "+m.getType()
                    );



            ((TextView)card.findViewById(
                    R.id.tvMedicinePrice))
                    .setText(
                            "Rs. "+m.getPrice()
                    );




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
                        CartActivity.addToCart(new Cart(
                                m.getMedicineId(),
                                m.getMedicineName(),
                                m.getBrandName(),
                                m.getPharmacyId(),
                                m.getPharmacy(),
                                m.getPrice(),
                                1
                        ));
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