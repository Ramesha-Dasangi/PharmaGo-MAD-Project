package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

public class RiderDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvNewOrderId, tvNewOrderDetails;
    private TextView tvActiveOrderId, tvActiveOrderDetails;
    private TextView tvRiderName;
    private TextView tvNewAssignmentLabel, tvInProgressLabel;
    private TextView tvActiveOrdersCount, tvDeliveredCount;
    private ConstraintLayout cardNewAssignment, cardInProgress;
    private Button btnAcceptAssignment;

    private String currentNewOrderId = null;
    private String currentActiveOrderId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_dashboard);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvNewOrderId = findViewById(R.id.tvNewOrderId);
        tvNewOrderDetails = findViewById(R.id.tvNewOrderDetails);
        tvActiveOrderId = findViewById(R.id.tvActiveOrderId);
        tvActiveOrderDetails = findViewById(R.id.tvActiveOrderDetails);
        tvRiderName = findViewById(R.id.tvRiderName);
        cardNewAssignment = findViewById(R.id.cardNewAssignment);
        cardInProgress = findViewById(R.id.cardInProgress);
        btnAcceptAssignment = findViewById(R.id.btnAcceptAssignment);
        tvNewAssignmentLabel = findViewById(R.id.tvNewAssignmentLabel);
        tvInProgressLabel = findViewById(R.id.tvInProgressLabel);
        tvActiveOrdersCount = findViewById(R.id.tvActiveOrdersCount);
        tvDeliveredCount = findViewById(R.id.tvDeliveredCount);

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists() && doc.getString("name") != null) {
                    if (tvRiderName != null) {
                        tvRiderName.setText(doc.getString("name"));
                    }
                }
            });
        }

        // Hide cards and labels initially until we fetch data
        if (cardNewAssignment != null) cardNewAssignment.setVisibility(View.GONE);
        if (cardInProgress != null) cardInProgress.setVisibility(View.GONE);
        if (tvNewAssignmentLabel != null) tvNewAssignmentLabel.setVisibility(View.GONE);
        if (tvInProgressLabel != null) tvInProgressLabel.setVisibility(View.GONE);

        if (btnAcceptAssignment != null) {
            btnAcceptAssignment.setOnClickListener(v -> {
                Intent intent = new Intent(RiderDashboardActivity.this, AssignmentDetailsActivity.class);
                if (currentNewOrderId != null) {
                    intent.putExtra("orderId", currentNewOrderId);
                }
                startActivity(intent);
            });
        }
        
        if (cardInProgress != null) {
            cardInProgress.setOnClickListener(v -> {
                Intent intent = new Intent(RiderDashboardActivity.this, DeliveryProgressActivity.class);
                if (currentActiveOrderId != null) {
                    intent.putExtra("orderId", currentActiveOrderId);
                }
                startActivity(intent);
            });
        }
        
        View navMap = findViewById(R.id.navMap);
        if (navMap != null) {
            navMap.setOnClickListener(v ->
                    startActivity(new Intent(RiderDashboardActivity.this, LiveMapActivity.class)));
        }

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) {
            navHistory.setOnClickListener(v ->
                    startActivity(new Intent(RiderDashboardActivity.this, DeliveryHistoryActivity.class)));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(RiderDashboardActivity.this, RiderProfileActivity.class)));
        }

        fetchOrders();
    }

    private void fetchOrders() {
        String currentUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            boolean hasNew = false;
            boolean hasActive = false;
            int activeCount = 0;
            int deliveredCount = 0;

            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                String status = doc.getString("status");
                String riderId = doc.getString("riderId");
                if (status == null) continue;

                boolean isMyOrder = currentUid != null && currentUid.equals(riderId);

                // Count active orders for this rider (assigned + in-transit)
                if (isMyOrder && (status.equalsIgnoreCase("assigned") || status.equalsIgnoreCase("picked_up") || status.equalsIgnoreCase("out_for_delivery"))) {
                    activeCount++;
                }

                // Count delivered orders for this rider
                if (isMyOrder && status.equalsIgnoreCase("delivered")) {
                    deliveredCount++;
                }

                // Show a new assignment card (orders assigned to this rider but not yet picked up)
                if (!hasNew && isMyOrder && status.equalsIgnoreCase("assigned")) {
                    if (cardNewAssignment != null) cardNewAssignment.setVisibility(View.VISIBLE);
                    if (tvNewAssignmentLabel != null) tvNewAssignmentLabel.setVisibility(View.VISIBLE);
                    if (tvNewOrderId != null) tvNewOrderId.setText("Order #" + doc.getId().substring(0, Math.min(6, doc.getId().length())).toUpperCase());
                    
                    if (tvNewOrderDetails != null) {
                        java.util.List<java.util.Map<String, Object>> items = (java.util.List<java.util.Map<String, Object>>) doc.get("items");
                        int stops = (items != null) ? items.size() : 1;
                        tvNewOrderDetails.setText(stops + " items · 1 customer drop-off");
                    }
                    
                    currentNewOrderId = doc.getId();
                    hasNew = true;
                } else if (!hasActive && isMyOrder && (status.equalsIgnoreCase("picked_up") || status.equalsIgnoreCase("out_for_delivery"))) {
                    if (cardInProgress != null) cardInProgress.setVisibility(View.VISIBLE);
                    if (tvInProgressLabel != null) tvInProgressLabel.setVisibility(View.VISIBLE);
                    if (tvActiveOrderId != null) tvActiveOrderId.setText("Order #" + doc.getId().substring(0, Math.min(6, doc.getId().length())).toUpperCase());
                    
                    if (tvActiveOrderDetails != null) {
                        String displayStatus = status.substring(0, 1).toUpperCase() + status.substring(1).replace("_", " ");
                        tvActiveOrderDetails.setText(displayStatus);
                    }
                    
                    currentActiveOrderId = doc.getId();
                    hasActive = true;
                }
            }

            // Update summary cards with real counts
            if (tvActiveOrdersCount != null) tvActiveOrdersCount.setText(String.valueOf(activeCount));
            if (tvDeliveredCount != null) tvDeliveredCount.setText(String.valueOf(deliveredCount));


        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
