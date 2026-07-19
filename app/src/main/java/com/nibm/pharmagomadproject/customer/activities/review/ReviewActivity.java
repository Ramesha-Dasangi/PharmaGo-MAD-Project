package com.nibm.pharmagomadproject.customer.activities.review;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    // Star ratings
    private int pharmacyRating = 0;
    private int riderRating    = 0;

    // Pharmacy stars
    private TextView starP1, starP2, starP3, starP4, starP5;

    // Rider stars
    private TextView starR1, starR2, starR3, starR4, starR5;

    // Comments
    private TextInputEditText etPharmacyComment, etRiderComment;

    // Firebase
    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;

    // Order data
    private String orderId    = "";
    private String pharmacyId = "";
    private String riderId    = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // Get data from intent
        orderId    = getIntent().getStringExtra("orderId")    != null ? getIntent().getStringExtra("orderId")    : "PG-00234";
        pharmacyId = getIntent().getStringExtra("pharmacyId") != null ? getIntent().getStringExtra("pharmacyId") : "";
        riderId    = getIntent().getStringExtra("riderId")    != null ? getIntent().getStringExtra("riderId")    : "";

        // Bind views
        starP1 = findViewById(R.id.starP1);
        starP2 = findViewById(R.id.starP2);
        starP3 = findViewById(R.id.starP3);
        starP4 = findViewById(R.id.starP4);
        starP5 = findViewById(R.id.starP5);

        starR1 = findViewById(R.id.starR1);
        starR2 = findViewById(R.id.starR2);
        starR3 = findViewById(R.id.starR3);
        starR4 = findViewById(R.id.starR4);
        starR5 = findViewById(R.id.starR5);

        etPharmacyComment = findViewById(R.id.etPharmacyComment);
        etRiderComment    = findViewById(R.id.etRiderComment);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Star click listeners
        TextView[] pStars = { starP1, starP2, starP3, starP4, starP5 };
        TextView[] rStars = { starR1, starR2, starR3, starR4, starR5 };

        for (int i = 0; i < pStars.length; i++) {
            final int rating = i + 1;
            pStars[i].setOnClickListener(v -> {
                pharmacyRating = rating;
                updateStars(pStars, rating);
            });
        }

        for (int i = 0; i < rStars.length; i++) {
            final int rating = i + 1;
            rStars[i].setOnClickListener(v -> {
                riderRating = rating;
                updateStars(rStars, rating);
            });
        }

        // Submit
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitReview);
        btnSubmit.setOnClickListener(v -> submitReview());

        // Skip
        MaterialButton btnSkip = findViewById(R.id.btnSkipReview);
        btnSkip.setOnClickListener(v -> goToHome());
    }

    // Fill stars up to selected rating
    private void updateStars(TextView[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            stars[i].setText(i < rating ? "★" : "☆");
        }
    }

    // Submit review to Firestore
    private void submitReview() {
        if (pharmacyRating == 0) {
            Toast.makeText(this, "Please rate the pharmacy", Toast.LENGTH_SHORT).show();
            return;
        }
        if (riderRating == 0) {
            Toast.makeText(this, "Please rate the delivery rider", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser() != null
                ? mAuth.getCurrentUser().getUid() : "";

        String pharmacyComment = etPharmacyComment.getText() != null
                ? etPharmacyComment.getText().toString().trim() : "";
        String riderComment    = etRiderComment.getText()    != null
                ? etRiderComment.getText().toString().trim()    : "";

        // Pharmacy review document
        Map<String, Object> pharmacyReview = new HashMap<>();
        pharmacyReview.put("orderId",    orderId);
        pharmacyReview.put("customerId", uid);
        pharmacyReview.put("pharmacyId", pharmacyId);
        pharmacyReview.put("rating",     pharmacyRating);
        pharmacyReview.put("comment",    pharmacyComment);
        pharmacyReview.put("type",       "pharmacy");
        pharmacyReview.put("createdAt",  System.currentTimeMillis());

        // Rider review document
        Map<String, Object> riderReview = new HashMap<>();
        riderReview.put("orderId",    orderId);
        riderReview.put("customerId", uid);
        riderReview.put("riderId",    riderId);
        riderReview.put("rating",     riderRating);
        riderReview.put("comment",    riderComment);
        riderReview.put("type",       "rider");
        riderReview.put("createdAt",  System.currentTimeMillis());

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitReview);
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        // Save both reviews
        db.collection("reviews")
                .document(orderId + "_pharmacy")
                .set(pharmacyReview)
                .addOnSuccessListener(aVoid ->
                        db.collection("reviews")
                                .document(orderId + "_rider")
                                .set(riderReview)
                                .addOnSuccessListener(aVoid2 -> {
                                    // Also update order's average rating in Firestore
                                    db.collection("orders").document(orderId)
                                            .update(
                                                    "pharmacyRating", pharmacyRating,
                                                    "riderRating",    riderRating,
                                                    "reviewDone",     true
                                            );

                                    Toast.makeText(this,
                                            "⭐ Thank you for your review!",
                                            Toast.LENGTH_SHORT).show();
                                    goToHome();
                                })
                                .addOnFailureListener(e -> {
                                    btnSubmit.setEnabled(true);
                                    btnSubmit.setText("Submit review");
                                    Toast.makeText(this,
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit review");
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
