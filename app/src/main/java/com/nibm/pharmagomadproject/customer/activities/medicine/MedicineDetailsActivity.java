package com.nibm.pharmagomadproject.customer.activities.medicine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.order.CartActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_details);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Read intent extras
        String name     = getIntent().getStringExtra("medicine_name");
        int    price    = getIntent().getIntExtra("medicine_price", 42);
        String type     = getIntent().getStringExtra("medicine_type");
        String category = getIntent().getStringExtra("medicine_category");
        String pharmacy = getIntent().getStringExtra("medicine_pharmacy");

        // Set medicine name
        safeSetText(R.id.tvMedicineName, name != null ? name : "Medicine");
        safeSetText(R.id.tvManufacturer, (category != null ? category : "") +
                (type != null ? " · " + type : ""));

        // ── Price comparison bars ──
        List<PharmacyPrice> prices = getPriceComparisons(name, price, pharmacy);
        bindPriceComparison(prices);

        // Back
        safeClick(R.id.btnBack, v -> finish());

        // Cart icon
        safeClick(R.id.btnCart, v -> startActivity(new Intent(this, CartActivity.class)));

        // Add to cart
        safeClick(R.id.btnAddToCart, v -> {
            String medName = name != null ? name : "Medicine";
            Toast.makeText(this, medName + " added to cart!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CartActivity.class));
        });

        // Order now
        safeClick(R.id.btnOrderNow, v -> {
            if ("Prescription".equals(type) || "Rx".equals(type)) {
                startActivity(new Intent(this, PrescriptionUploadActivity.class));
            } else {
                startActivity(new Intent(this, CartActivity.class));
            }
        });
    }

    private List<PharmacyPrice> getPriceComparisons(String name, int basePrice, String sourcePharmacy) {
        List<PharmacyPrice> list = new ArrayList<>();

        // Generate realistic variation — in real app query Firestore
        int p1 = basePrice;
        int p2 = (int) (basePrice * 1.07);  // 7% more
        int p3 = (int) (basePrice * 1.19);  // 19% more

        String ph1 = sourcePharmacy != null ? sourcePharmacy : "MediCare Pharmacy";
        String ph2 = ph1.equals("City Pharma") ? "MediCare Pharmacy" : "City Pharma";
        String ph3 = "HealthPlus Pharmacy";

        list.add(new PharmacyPrice(ph1, p1, true));   // best price
        list.add(new PharmacyPrice(ph2, p2, false));
        list.add(new PharmacyPrice(ph3, p3, false));

        return list;
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

        for (int i = 0; i < Math.min(prices.size(), cardIds.length); i++) {
            PharmacyPrice pp = prices.get(i);

            CardView card = findViewById(cardIds[i]);
            TextView tvName  = findViewById(nameIds[i]);
            TextView tvPrice = findViewById(priceIds[i]);

            if (card == null || tvName == null || tvPrice == null) continue;

            tvName.setText(pp.name);
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

            // Price bar — find or build inside the card
            // The bar is a FrameLayout with 2 layers: background + fill
            // We look for tvPharmacy1Price's parent and draw below it
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