package com.nibm.pharmagomadproject.customer.activities.report;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.pharmacy.ComplaintPharmacyActivity;
import com.nibm.pharmagomadproject.customer.activities.rider.ComplaintRiderActivity;

public class ReportIssueActivity extends AppCompatActivity {

    private String selectedTarget = "pharmacy";
    private MaterialCardView optionPharmacy, optionRider;
    private String orderId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_issue);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        optionPharmacy = findViewById(R.id.optionPharmacy);
        optionRider = findViewById(R.id.optionRider);
        MaterialButton btnContinue = findViewById(R.id.btnContinue);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Extract order ID
        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) orderId = "";

        // Load details dynamically
        if (!orderId.isEmpty()) {
            TextView tvId = findViewById(R.id.tvOrderId);
            if (tvId != null) tvId.setText("Order #" + orderId);

            FirebaseFirestore.getInstance().collection("orders").document(orderId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            java.util.List<?> items = (java.util.List<?>) doc.get("items");
                            java.util.Set<String> pharmNames = new java.util.LinkedHashSet<>();
                            if (items != null) {
                                for (Object itemObj : items) {
                                    if (itemObj instanceof java.util.Map) {
                                        Object pName = ((java.util.Map<?, ?>) itemObj).get("pharmacyName");
                                        if (pName != null && !pName.toString().trim().isEmpty()) {
                                            pharmNames.add(pName.toString().trim());
                                        }
                                    }
                                }
                            }
                            if (pharmNames.isEmpty()) {
                                String singleName = doc.getString("pharmacyName");
                                if (singleName != null && !singleName.trim().isEmpty()) pharmNames.add(singleName.trim());
                            }
                            StringBuilder sb = new StringBuilder();
                            for (String name : pharmNames) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(name);
                            }
                            String pharmacyText = sb.length() > 0 ? sb.toString() : "Pharmacy";
                            String riderId = doc.getString("riderId");

                            TextView tvSummary = findViewById(R.id.tvOrderSummary);

                            if (riderId != null && !riderId.trim().isEmpty()) {
                                // Fetch rider name
                                FirebaseFirestore.getInstance().collection("users")
                                        .document(riderId).get()
                                        .addOnSuccessListener(riderDoc -> {
                                            String riderName = riderDoc.getString("name");
                                            String display = pharmacyText + "  ·  Rider: "
                                                    + (riderName != null ? riderName : "Assigned");
                                            if (tvSummary != null) tvSummary.setText(display);
                                        })
                                        .addOnFailureListener(e -> {
                                            if (tvSummary != null)
                                                tvSummary.setText(pharmacyText + "  ·  Rider assigned");
                                        });
                            } else {
                                if (tvSummary != null)
                                    tvSummary.setText(pharmacyText + "  ·  No rider assigned yet");
                            }
                        }
                    });
        }

        // default selection
        selectTarget("pharmacy");

        optionPharmacy.setOnClickListener(v -> selectTarget("pharmacy"));
        optionRider.setOnClickListener(v -> selectTarget("rider"));

        btnContinue.setOnClickListener(v -> {
            Intent intent;
            if ("pharmacy".equals(selectedTarget)) {
                Log.d("ReportIssue", "Opening ComplaintPharmacyActivity");
                intent = new Intent(ReportIssueActivity.this, ComplaintPharmacyActivity.class);
            } else {
                Log.d("ReportIssue", "Opening ComplaintRiderActivity");
                intent = new Intent(ReportIssueActivity.this, ComplaintRiderActivity.class);
            }
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });
    }

    private void selectTarget(String target) {
        selectedTarget = target;

        int selectedBg = getResources().getColor(R.color.pg_primary_light, null);
        int normalBg = getResources().getColor(R.color.pg_card, null);
        int selectedStroke = getResources().getColor(R.color.pg_primary, null);
        int normalStroke = getResources().getColor(R.color.pg_border, null);

        float density = getResources().getDisplayMetrics().density;

        if ("pharmacy".equals(target)) {
            optionPharmacy.setCardBackgroundColor(selectedBg);
            optionPharmacy.setStrokeColor(selectedStroke);
            optionPharmacy.setStrokeWidth((int) (2 * density));

            optionRider.setCardBackgroundColor(normalBg);
            optionRider.setStrokeColor(normalStroke);
            optionRider.setStrokeWidth((int) (1 * density));
        } else {
            optionRider.setCardBackgroundColor(selectedBg);
            optionRider.setStrokeColor(selectedStroke);
            optionRider.setStrokeWidth((int) (2 * density));

            optionPharmacy.setCardBackgroundColor(normalBg);
            optionPharmacy.setStrokeColor(normalStroke);
            optionPharmacy.setStrokeWidth((int) (1 * density));
        }
    }
}