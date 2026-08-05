package com.nibm.pharmagomadproject.customer.activities.review;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.home.HomeActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;

    private String orderId    = "";
    private String pharmacyId = "";
    private String riderId    = "";

    private int riderRating = 0;
    private TextView starR1, starR2, starR3, starR4, starR5;
    private TextInputEditText etRiderComment;

    // Multi-pharmacy rating structure
    private static class PharmacyRatingItem {
        String pharmacyId;
        String pharmacyName;
        int rating = 0;
        TextView[] stars;
        TextInputEditText etComment;
    }

    private final List<PharmacyRatingItem> pharmacyRatings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        orderId    = getIntent().getStringExtra("orderId")    != null ? getIntent().getStringExtra("orderId")    : "";
        pharmacyId = getIntent().getStringExtra("pharmacyId") != null ? getIntent().getStringExtra("pharmacyId") : "";
        riderId    = getIntent().getStringExtra("riderId")    != null ? getIntent().getStringExtra("riderId")    : "";

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Bind Rider Stars
        starR1 = findViewById(R.id.starR1);
        starR2 = findViewById(R.id.starR2);
        starR3 = findViewById(R.id.starR3);
        starR4 = findViewById(R.id.starR4);
        starR5 = findViewById(R.id.starR5);
        etRiderComment = findViewById(R.id.etRiderComment);

        TextView[] rStars = { starR1, starR2, starR3, starR4, starR5 };
        for (int i = 0; i < rStars.length; i++) {
            final int rating = i + 1;
            rStars[i].setOnClickListener(v -> {
                riderRating = rating;
                updateStars(rStars, rating);
            });
        }

        // Single default pharmacy bind setup fallback
        TextView starP1 = findViewById(R.id.starP1);
        TextView starP2 = findViewById(R.id.starP2);
        TextView starP3 = findViewById(R.id.starP3);
        TextView starP4 = findViewById(R.id.starP4);
        TextView starP5 = findViewById(R.id.starP5);
        TextInputEditText etPharmComment = findViewById(R.id.etPharmacyComment);
        TextView tvPharmLabel = findViewById(R.id.tvPharmacyRateLabel);

        PharmacyRatingItem defaultItem = new PharmacyRatingItem();
        defaultItem.pharmacyId = pharmacyId;
        defaultItem.pharmacyName = "Pharmacy";
        defaultItem.stars = new TextView[]{ starP1, starP2, starP3, starP4, starP5 };
        defaultItem.etComment = etPharmComment;

        if (starP1 != null) {
            for (int i = 0; i < defaultItem.stars.length; i++) {
                final int rating = i + 1;
                defaultItem.stars[i].setOnClickListener(v -> {
                    defaultItem.rating = rating;
                    updateStars(defaultItem.stars, rating);
                });
            }
        }
        pharmacyRatings.add(defaultItem);

        // Submit & Skip
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitReview);
        btnSubmit.setOnClickListener(v -> submitReview());

        MaterialButton btnSkip = findViewById(R.id.btnSkipReview);
        btnSkip.setOnClickListener(v -> goToHome());

        // Load real order details from Firestore
        if (!orderId.isEmpty()) {
            db.collection("orders").document(orderId).get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;

                        // Check if already reviewed
                        Boolean reviewDone = doc.getBoolean("reviewDone");
                        if (Boolean.TRUE.equals(reviewDone)) {
                            switchToViewOnlyMode();
                            return;
                        }

                        TextView tvOrderTitle = findViewById(R.id.tvOrderHeader);
                        if (tvOrderTitle != null) {
                            tvOrderTitle.setText("Order #" + doc.getId().toUpperCase());
                        }

                        String rName = doc.getString("riderName");
                        String rIdDoc = doc.getString("riderId");
                        if (rIdDoc != null && !rIdDoc.isEmpty()) riderId = rIdDoc;

                        View cardRiderView = findViewById(R.id.cardRider);
                        if (riderId == null || riderId.trim().isEmpty()) {
                            if (cardRiderView != null) cardRiderView.setVisibility(View.GONE);
                        } else {
                            if (cardRiderView != null) cardRiderView.setVisibility(View.VISIBLE);
                            if (rName != null && !rName.isEmpty()) {
                                TextView tvRiderLabel = findViewById(R.id.tvRiderRateLabel);
                                if (tvRiderLabel != null) tvRiderLabel.setText("Rate " + rName + " — your rider");
                            } else {
                                db.collection("users").document(riderId).get()
                                        .addOnSuccessListener(uDoc -> {
                                            if (uDoc.exists() && uDoc.getString("name") != null) {
                                                TextView tvRiderLabel = findViewById(R.id.tvRiderRateLabel);
                                                if (tvRiderLabel != null)
                                                    tvRiderLabel.setText("Rate " + uDoc.getString("name") + " — your rider");
                                            }
                                        });
                            }
                        }

                        // Inspect items & confirmedPharmacies list to load only confirmed/approved pharmacies
                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                        List<?> confirmedList = (List<?>) doc.get("confirmedPharmacies");
                        Map<String, String> uniquePharmacies = new java.util.LinkedHashMap<>();

                        if (items != null) {
                            for (Map<String, Object> item : items) {
                                String pId = (String) item.get("pharmacyId");
                                String pName = (String) item.get("pharmacyName");
                                if (pId != null && !pId.isEmpty()) {
                                    // If confirmedPharmacies list exists, only include if in confirmedPharmacies
                                    if (confirmedList != null && !confirmedList.isEmpty()) {
                                        if (confirmedList.contains(pId)) {
                                            uniquePharmacies.put(pId, pName != null ? pName : "Pharmacy");
                                        }
                                    } else {
                                        uniquePharmacies.put(pId, pName != null ? pName : "Pharmacy");
                                    }
                                }
                            }
                        }

                        if (uniquePharmacies.isEmpty()) {
                            String pName = doc.getString("pharmacyName");
                            String pId = doc.getString("pharmacyId");
                            if (pId != null && !pId.isEmpty()) {
                                if (confirmedList == null || confirmedList.isEmpty() || confirmedList.contains(pId)) {
                                    uniquePharmacies.put(pId, pName != null ? pName : "Pharmacy");
                                }
                            }
                        }

                        if (!uniquePharmacies.isEmpty()) {
                            pharmacyRatings.clear();
                            boolean isFirst = true;

                            LinearLayout extraContainer = findViewById(R.id.extraPharmacyContainer);

                            for (Map.Entry<String, String> entry : uniquePharmacies.entrySet()) {
                                String pId = entry.getKey();
                                String pName = entry.getValue();

                                if (isFirst) {
                                    // First pharmacy reuses the XML-defined card
                                    if (tvPharmLabel != null) tvPharmLabel.setText("Rate " + pName);
                                    defaultItem.pharmacyId = pId;
                                    defaultItem.pharmacyName = pName;
                                    pharmacyRatings.add(defaultItem);
                                    isFirst = false;
                                } else {
                                    // Extra pharmacies — build a card programmatically
                                    addExtraPharmacyCard(extraContainer, pId, pName);
                                }
                            }
                        }
                    });
        }
    }

    private void switchToViewOnlyMode() {
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitReview);
        MaterialButton btnSkip   = findViewById(R.id.btnSkipReview);
        if (btnSubmit != null) {
            btnSubmit.setText("Already Reviewed ✓");
            btnSubmit.setEnabled(false);
        }
        if (btnSkip != null) {
            btnSkip.setText("Back to Home");
            btnSkip.setEnabled(true);
            btnSkip.setOnClickListener(v -> goToHome());
        }

        TextView tvOrderHeader = findViewById(R.id.tvOrderHeader);
        if (tvOrderHeader != null) tvOrderHeader.setText("Review Submitted ✓");

        // Disable all input on default rider stars
        if (etRiderComment != null) etRiderComment.setEnabled(false);
        TextView[] rStars = { starR1, starR2, starR3, starR4, starR5 };
        for (TextView st : rStars) if (st != null) st.setClickable(false);

        // Hide the pharmacy card and rider rating card — replace with scrollable list of all reviews
        View cardRider = findViewById(R.id.cardRider);
        View cardPharmacy = findViewById(R.id.cardPharmacy);
        View extraContainer = findViewById(R.id.extraPharmacyContainer);
        if (cardRider != null) cardRider.setVisibility(View.GONE);
        if (cardPharmacy != null) cardPharmacy.setVisibility(View.GONE);
        if (extraContainer != null) extraContainer.setVisibility(View.GONE);

        // Build a scrollable review summary
        LinearLayout rootContainer = findViewById(R.id.reviewRootContainer);
        if (rootContainer == null) return;

        // Add a "Your Reviews" header
        TextView tvHeader = new TextView(this);
        tvHeader.setText("Your Reviews for this Order");
        tvHeader.setTextSize(15);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        tvHeader.setTextColor(getResources().getColor(R.color.pg_text, getTheme()));
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hlp.topMargin = (int)(16 * getResources().getDisplayMetrics().density);
        hlp.bottomMargin = (int)(8 * getResources().getDisplayMetrics().density);
        tvHeader.setLayoutParams(hlp);
        rootContainer.addView(tvHeader);

        // Load all reviews for this order
        if (!orderId.isEmpty()) {
            db.collection("reviews")
                    .whereEqualTo("orderId", orderId)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (snap == null || snap.isEmpty()) {
                            TextView tvNone = new TextView(this);
                            tvNone.setText("No reviews found.");
                            tvNone.setTextColor(0xFF888888);
                            rootContainer.addView(tvNone);
                            return;
                        }

                        for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                            String type        = d.getString("type");
                            Long   rating      = d.getLong("rating");
                            String comment     = d.getString("comment");
                            String custName    = d.getString("customerName");
                            String pharmName   = d.getString("pharmacyName");
                            int starsVal = rating != null ? rating.intValue() : 0;

                            // Card wrapper
                            androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
                            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            cardLp.bottomMargin = (int)(10 * getResources().getDisplayMetrics().density);
                            card.setLayoutParams(cardLp);
                            card.setRadius(12 * getResources().getDisplayMetrics().density);
                            card.setCardElevation(2 * getResources().getDisplayMetrics().density);
                            card.setCardBackgroundColor(getResources().getColor(R.color.pg_card, getTheme()));

                            LinearLayout inner = new LinearLayout(this);
                            inner.setOrientation(LinearLayout.VERTICAL);
                            int pad = (int)(14 * getResources().getDisplayMetrics().density);
                            inner.setPadding(pad, pad, pad, pad);
                            card.addView(inner);

                            // Type label (Pharmacy Name / Rider)
                            TextView tvType = new TextView(this);
                            String label = "rider".equalsIgnoreCase(type) ? "Rider Review"
                                    : (pharmName != null ? pharmName : "Pharmacy Review");
                            tvType.setText(label);
                            tvType.setTextSize(13);
                            tvType.setTypeface(null, android.graphics.Typeface.BOLD);
                            tvType.setTextColor(getResources().getColor(R.color.pg_text, getTheme()));
                            inner.addView(tvType);

                            // Order ID subtitle
                            TextView tvOid = new TextView(this);
                            tvOid.setText("Order #" + orderId.toUpperCase());
                            tvOid.setTextSize(11);
                            tvOid.setTextColor(0xFF9E9E9E);
                            LinearLayout.LayoutParams oidLp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            oidLp.topMargin = 2;
                            tvOid.setLayoutParams(oidLp);
                            inner.addView(tvOid);

                            // Customer name (if present)
                            if (custName != null && !custName.isEmpty()) {
                                TextView tvCust = new TextView(this);
                                tvCust.setText("by " + custName);
                                tvCust.setTextSize(12);
                                tvCust.setTextColor(0xFF757575);
                                inner.addView(tvCust);
                            }

                            // Stars
                            TextView tvStars = new TextView(this);
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < 5; i++) sb.append(i < starsVal ? "★" : "☆");
                            tvStars.setText(sb.toString());
                            tvStars.setTextColor(0xFFFFC107);
                            tvStars.setTextSize(20);
                            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            slp.topMargin = (int)(6 * getResources().getDisplayMetrics().density);
                            tvStars.setLayoutParams(slp);
                            inner.addView(tvStars);

                            // Comment
                            if (comment != null && !comment.isEmpty()) {
                                TextView tvCmt = new TextView(this);
                                tvCmt.setText("\"" + comment + "\"");
                                tvCmt.setTextSize(13);
                                tvCmt.setTextColor(0xFF555555);
                                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                clp.topMargin = (int)(6 * getResources().getDisplayMetrics().density);
                                tvCmt.setLayoutParams(clp);
                                inner.addView(tvCmt);
                            }

                            rootContainer.addView(card);
                        }
                    });
        }
    }

    private void updateStars(TextView[] stars, int rating) {
        if (stars == null) return;
        for (int i = 0; i < stars.length; i++) {
            if (stars[i] != null) {
                stars[i].setText(i < rating ? "★" : "☆");
            }
        }
    }

    private void addExtraPharmacyCard(LinearLayout container, String pId, String pName) {
        if (container == null) return;

        PharmacyRatingItem item = new PharmacyRatingItem();
        item.pharmacyId = pId;
        item.pharmacyName = pName;

        // Build a CardView programmatically
        androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
        card.setLayoutParams(cardLp);
        card.setRadius(12 * getResources().getDisplayMetrics().density);
        card.setCardElevation(2 * getResources().getDisplayMetrics().density);
        card.setCardBackgroundColor(getResources().getColor(R.color.pg_card, getTheme()));

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (14 * getResources().getDisplayMetrics().density);
        inner.setPadding(pad, pad, pad, pad);
        card.addView(inner);

        // Label
        TextView label = new TextView(this);
        label.setText("Rate " + pName);
        label.setTextColor(getResources().getColor(R.color.pg_text, getTheme()));
        label.setTextSize(13);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelLp.bottomMargin = (int) (10 * getResources().getDisplayMetrics().density);
        label.setLayoutParams(labelLp);
        inner.addView(label);

        // Stars row
        LinearLayout starsRow = new LinearLayout(this);
        starsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams starsLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        starsLp.bottomMargin = (int) (10 * getResources().getDisplayMetrics().density);
        starsRow.setLayoutParams(starsLp);

        item.stars = new TextView[5];
        for (int i = 0; i < 5; i++) {
            TextView star = new TextView(this);
            int sz = (int) (36 * getResources().getDisplayMetrics().density);
            star.setLayoutParams(new LinearLayout.LayoutParams(sz, sz));
            star.setGravity(android.view.Gravity.CENTER);
            star.setText("☆");
            star.setTextSize(28);
            star.setTextColor(0xFFFCD34D);
            star.setClickable(true);
            star.setFocusable(true);
            final int rating = i + 1;
            star.setOnClickListener(v -> {
                item.rating = rating;
                updateStars(item.stars, rating);
            });
            starsRow.addView(star);
            item.stars[i] = star;
        }
        inner.addView(starsRow);

        // Comment input
        com.google.android.material.textfield.TextInputEditText et =
                new com.google.android.material.textfield.TextInputEditText(this);
        et.setHint("Share your experience with " + pName + "...");
        et.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        et.setGravity(android.view.Gravity.TOP);
        et.setMinHeight((int) (80 * getResources().getDisplayMetrics().density));
        et.setTextSize(13);
        et.setTextColor(getResources().getColor(R.color.pg_text, getTheme()));
        et.setHintTextColor(getResources().getColor(R.color.pg_sub, getTheme()));
        inner.addView(et);
        item.etComment = et;

        container.addView(card);
        pharmacyRatings.add(item);
    }

    private void submitReview() {
        boolean hasAnyRating = riderRating > 0;
        for (PharmacyRatingItem item : pharmacyRatings) {
            if (item.rating > 0) {
                hasAnyRating = true;
                break;
            }
        }

        if (!hasAnyRating) {
            Toast.makeText(this, "No ratings provided. Skipping review.", Toast.LENGTH_SHORT).show();
            goToHome();
            return;
        }

        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        String riderComment = etRiderComment.getText() != null ? etRiderComment.getText().toString().trim() : "";

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitReview);
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        com.google.firebase.firestore.WriteBatch batch = db.batch();

        // Batch save reviews for pharmacies that were rated
        int primaryRating = 5;
        for (PharmacyRatingItem item : pharmacyRatings) {
            if (item.rating > 0) {
                primaryRating = item.rating;
                String pComment = item.etComment != null && item.etComment.getText() != null
                        ? item.etComment.getText().toString().trim() : "";

                Map<String, Object> pReview = new HashMap<>();
                pReview.put("orderId",      orderId);
                pReview.put("customerId",   uid);
                pReview.put("pharmacyId",   item.pharmacyId);
                pReview.put("pharmacyName", item.pharmacyName);
                pReview.put("rating",       item.rating);
                pReview.put("comment",      pComment);
                pReview.put("type",         "pharmacy");
                pReview.put("createdAt",    System.currentTimeMillis());

                batch.set(db.collection("reviews").document(orderId + "_" + item.pharmacyId), pReview);
            }
        }

        // Save rider review ONLY if rider was rated
        if (riderRating > 0 && riderId != null && !riderId.isEmpty()) {
            Map<String, Object> riderReview = new HashMap<>();
            riderReview.put("orderId",    orderId);
            riderReview.put("customerId", uid);
            riderReview.put("riderId",    riderId);
            riderReview.put("rating",     riderRating);
            riderReview.put("comment",    riderComment);
            riderReview.put("type",       "rider");
            riderReview.put("createdAt",  System.currentTimeMillis());

            batch.set(db.collection("reviews").document(orderId + "_rider"), riderReview);
        }

        // Update order review flag
        batch.update(db.collection("orders").document(orderId),
                "pharmacyRating", primaryRating,
                "riderRating", riderRating > 0 ? riderRating : 5,
                "reviewDone", true
        );

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Update real average rating for each rated pharmacy
                    for (PharmacyRatingItem item : pharmacyRatings) {
                        if (item.rating > 0 && item.pharmacyId != null) {
                            updatePharmacyAverageRating(item.pharmacyId);
                        }
                    }
                    // Update real average rating for rider
                    if (riderRating > 0 && riderId != null && !riderId.isEmpty()) {
                        updateRiderAverageRating(riderId);
                    }
                    Toast.makeText(this, "⭐ Thank you for your review!", Toast.LENGTH_SHORT).show();
                    goToHome();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit review");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Recalculate and persist the average rating for a pharmacy */
    private void updatePharmacyAverageRating(String pId) {
        if (pId == null || pId.isEmpty()) return;
        db.collection("reviews").whereEqualTo("pharmacyId", pId).get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) return;
                    double sum = 0; int count = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                        Long r = d.getLong("rating");
                        if (r != null && r > 0) { sum += r; count++; }
                    }
                    if (count > 0) {
                        double avg = Math.round((sum / count) * 10.0) / 10.0;
                        java.util.Map<String, Object> upd = new HashMap<>();
                        upd.put("rating", avg);
                        upd.put("ratingCount", count);
                        db.collection("users").document(pId).update(upd);
                        db.collection("pharmacies").document(pId).update(upd);
                    }
                });
    }

    /** Recalculate and persist the average rating for a rider */
    private void updateRiderAverageRating(String rId) {
        if (rId == null || rId.isEmpty()) return;
        db.collection("reviews").whereEqualTo("riderId", rId).get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) return;
                    double sum = 0; int count = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                        Long r = d.getLong("rating");
                        if (r != null && r > 0) { sum += r; count++; }
                    }
                    if (count > 0) {
                        double avg = Math.round((sum / count) * 10.0) / 10.0;
                        java.util.Map<String, Object> upd = new HashMap<>();
                        upd.put("rating", avg);
                        upd.put("ratingCount", count);
                        db.collection("users").document(rId).update(upd);
                        db.collection("riders").document(rId).update(upd);
                    }
                });
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
