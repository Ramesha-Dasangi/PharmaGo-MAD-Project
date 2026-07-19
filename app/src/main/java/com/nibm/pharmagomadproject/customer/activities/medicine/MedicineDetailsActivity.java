package com.nibm.pharmagomadproject.customer.activities.medicine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.order.CartActivity;
import com.nibm.pharmagomadproject.customer.models.Cart;

import java.util.ArrayList;
import java.util.List;

public class MedicineDetailsActivity extends AppCompatActivity {

    // Sample price comparison data per medicine — in real app: Firestore query
    static class PharmacyPrice {
        String name; int price; boolean isBest;
        PharmacyPrice(String name, int price, boolean isBest) {
            this.name = name; this.price = price; this.isBest = isBest;
        }
    }

    private String id, name, brand, phId, pharmacy, type, category;
    private int price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_details);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        id       = getIntent().getStringExtra("medicine_id");
        name     = getIntent().getStringExtra("medicine_name");
        brand    = getIntent().getStringExtra("brand_name");
        phId     = getIntent().getStringExtra("pharmacy_id");
        pharmacy = getIntent().getStringExtra("medicine_pharmacy");
        price    = getIntent().getIntExtra("medicine_price", 0);
        type     = getIntent().getStringExtra("medicine_type");
        category = getIntent().getStringExtra("medicine_category");
        pharmacy = getIntent().getStringExtra("medicine_pharmacy");

        // Set medicine name
        safeSetText(R.id.tvMedicineName, name != null ? name : "Medicine");
        safeSetText(R.id.tvManufacturer, (category != null ? category : "") +
                (type != null ? " · " + type : ""));

        // Load image if available
        ImageView imgMed = findViewById(R.id.imgMedicine);
        String image    = getIntent().getStringExtra("medicine_image");
        if (imgMed != null && image != null && !image.trim().isEmpty()) {
            imgMed.setPadding(0, 0, 0, 0);
            imgMed.setImageTintList(null); // Clear tint
            com.bumptech.glide.Glide.with(this)
                .load(image.trim())
                .placeholder(R.drawable.ic_pill)
                .into(imgMed);
        }

        // ── Price comparison bars ──
        loadPriceComparisons(name, price, pharmacy);

        // Back
        safeClick(R.id.btnBack, v -> finish());

        // Cart icon
        safeClick(R.id.btnCart, v -> startActivity(new Intent(this, CartActivity.class)));

        // Add to cart
        safeClick(R.id.btnAddToCart, v -> {
            String medName = name != null ? name : "Medicine";
            CartActivity.addToCart(new Cart(
                    id != null ? id : "temp_id",
                    medName,
                    brand != null ? brand : "",
                    phId != null ? phId : "",
                    pharmacy != null ? pharmacy : "",
                    price,
                    1
            ));
            Toast.makeText(this, medName + " added to cart!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CartActivity.class));
        });

        // Order now
        safeClick(R.id.btnOrderNow, v -> {
            if ("Prescription".equals(type) || "Rx".equals(type)) {
                Intent uploadIntent = new Intent(this, PrescriptionUploadActivity.class);
                uploadIntent.putExtra("medicine_id", id);
                uploadIntent.putExtra("medicine_name", name);
                uploadIntent.putExtra("brand_name", brand);
                uploadIntent.putExtra("pharmacy_id", phId);
                uploadIntent.putExtra("pharmacy_name", pharmacy);
                uploadIntent.putExtra("medicine_price", price);
                startActivity(uploadIntent);
            } else {
                String medName = name != null ? name : "Medicine";
                CartActivity.addToCart(new Cart(
                        id != null ? id : "temp_id",
                        medName,
                        brand != null ? brand : "",
                        phId != null ? phId : "",
                        pharmacy != null ? pharmacy : "",
                        price,
                        1
                ));
                startActivity(new Intent(this, CartActivity.class));
            }
        });

        // Fetch description dynamically from Firestore
        if (id != null && !id.isEmpty()) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("medicines")
                    .document(id)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String desc = doc.getString("description");
                            if (desc != null && !desc.trim().isEmpty()) {
                                TextView tvDesc = findViewById(R.id.tvDescription);
                                CardView cardDesc = findViewById(R.id.cardDescription);
                                if (tvDesc != null) tvDesc.setText(desc);
                                if (cardDesc != null) cardDesc.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    private void loadPriceComparisons(String medicineName, int basePrice, String sourcePharmacy) {
        if (medicineName == null || medicineName.isEmpty()) return;

        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("medicines")
                .whereEqualTo("medicineName", medicineName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PharmacyPrice> pricesList = new ArrayList<>();
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("pharmacies")
                            .whereEqualTo("status", "approved")
                            .get()
                            .addOnSuccessListener(pharmacySnaps -> {
                                java.util.Map<String, String> namesMap = new java.util.HashMap<>();
                                for (com.google.firebase.firestore.DocumentSnapshot doc : pharmacySnaps) {
                                    String ownerId = doc.getString("ownerId");
                                    String nameVal = doc.getString("name");
                                    if (ownerId != null && nameVal != null) {
                                        namesMap.put(ownerId, nameVal);
                                    }
                                }

                                int bestPrice = Integer.MAX_VALUE;
                                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                                    double p = doc.getDouble("price") != null ? doc.getDouble("price") : 0;
                                    int priceVal = (int) p;
                                    if (priceVal < bestPrice) bestPrice = priceVal;
                                }

                                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                                    String phId = doc.getString("pharmacyId");
                                    String phName = phId != null ? namesMap.get(phId) : null;
                                    if (phName == null) phName = doc.getString("pharmacy");
                                    if (phName == null) phName = "Pharmacy";

                                    double p = doc.getDouble("price") != null ? doc.getDouble("price") : 0;
                                    int priceVal = (int) p;
                                    pricesList.add(new PharmacyPrice(phName, priceVal, priceVal == bestPrice));
                                }

                                // Sort: best price first
                                pricesList.sort((a, b) -> Integer.compare(a.price, b.price));

                                // Bind comparisons
                                bindPriceComparison(pricesList);
                            })
                            .addOnFailureListener(e -> {
                                List<PharmacyPrice> list = new ArrayList<>();
                                list.add(new PharmacyPrice(sourcePharmacy != null ? sourcePharmacy : "Pharmacy", basePrice, true));
                                bindPriceComparison(list);
                            });
                })
                .addOnFailureListener(e -> {
                    List<PharmacyPrice> list = new ArrayList<>();
                    list.add(new PharmacyPrice(sourcePharmacy != null ? sourcePharmacy : "Pharmacy", basePrice, true));
                    bindPriceComparison(list);
                });
    }

    private void bindPriceComparison(List<PharmacyPrice> prices) {
        if (prices.isEmpty()) return;

        int maxPrice = 0;
        for (PharmacyPrice p : prices) if (p.price > maxPrice) maxPrice = p.price;

        // Pharmacy card IDs
        int[] cardIds   = { R.id.pharmacyMedicare,   R.id.pharmacyCityPharma,  R.id.pharmacyHealthPlus };
        int[] nameIds   = { R.id.tvPharmacy1Name,    R.id.tvPharmacy2Name,     R.id.tvPharmacy3Name    };
        int[] priceIds  = { R.id.tvPharmacy1Price,   R.id.tvPharmacy2Price,    R.id.tvPharmacy3Price   };

        int primaryColor = getResources().getColor(R.color.pg_primary, null);
        int grayColor    = 0xFFB4B2A9;
        int greenLight   = getResources().getColor(R.color.pg_primary_light, null);
        int normalCard   = getResources().getColor(R.color.pg_card, null);

        for (int i = 0; i < cardIds.length; i++) {
            CardView card = findViewById(cardIds[i]);
            if (card == null) continue;

            if (i < prices.size()) {
                card.setVisibility(View.VISIBLE);
                PharmacyPrice pp = prices.get(i);

                TextView tvName  = findViewById(nameIds[i]);
                TextView tvPrice = findViewById(priceIds[i]);

                if (tvName != null) tvName.setText(pp.name);
                if (tvPrice != null) {
                    tvPrice.setText("Rs. " + pp.price);

                    // Highlight best price card
                    if (pp.isBest) {
                        card.setCardBackgroundColor(greenLight);
                        tvPrice.setTextColor(primaryColor);
                        tvPrice.setTypeface(null, android.graphics.Typeface.BOLD);
                    } else {
                        card.setCardBackgroundColor(normalCard);
                        tvPrice.setTextColor(grayColor);
                        tvPrice.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                }

                updatePriceBar(card, pp.price, maxPrice, pp.isBest ? primaryColor : grayColor);

                // Card click = select
                card.setClickable(true);
                card.setFocusable(true);
                card.setOnClickListener(v -> {
                    for (int j = 0; j < cardIds.length; j++) {
                        CardView c = findViewById(cardIds[j]);
                        if (c != null) {
                            c.setCardBackgroundColor(j < prices.size() && prices.get(j).isBest
                                    ? greenLight : normalCard);
                        }
                    }
                    card.setCardBackgroundColor(greenLight);
                });
            } else {
                card.setVisibility(View.GONE);
            }
        }
    }

    private void updatePriceBar(CardView card, int price, int maxPrice, int fillColor) {
        // Find the bar FrameLayout inside the card if present
        try {
            // bar is the last View in card's LinearLayout
            LinearLayout ll = (LinearLayout) card.getChildAt(0);
            if (ll == null) return;
            for (int i = 0; i < ll.getChildCount(); i++) {
                View child = ll.getChildAt(i);
                if (child instanceof FrameLayout) {
                    FrameLayout frame = (FrameLayout) child;
                    if (frame.getChildCount() >= 2) {
                        View fill = frame.getChildAt(1);
                        float pct = maxPrice > 0 ? (float) price / maxPrice : 1f;
                        // Set width as fraction using post
                        fill.post(() -> {
                            int parentW = frame.getWidth();
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    (int) (parentW * pct), LinearLayout.LayoutParams.MATCH_PARENT);
                            fill.setLayoutParams(lp);
                            fill.setBackgroundColor(fillColor);
                        });
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void safeSetText(int id, String text) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(text);
    }

    private void safeClick(int id, View.OnClickListener l) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(l);
    }
}