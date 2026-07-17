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
                            String pharmacyName = doc.getString("pharmacyName");
                            String riderId = doc.getString("riderId");

                            TextView tvSummary = findViewById(R.id.tvOrderSummary);
                            if (tvSummary != null) {
                                String riderText = (riderId != null && !riderId.trim().isEmpty()) ? "1 rider" : "No rider assigned";
                                String pharmacyText = (pharmacyName != null) ? pharmacyName : "Pharmacy";
                                tvSummary.setText(pharmacyText + " · " + riderText);
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