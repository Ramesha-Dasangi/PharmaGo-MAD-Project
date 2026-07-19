package com.nibm.pharmagomadproject.customer.activities.order;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.report.ReportIssueActivity;
import com.nibm.pharmagomadproject.customer.activities.review.ReviewActivity;

public class OrderTrackingActivity extends AppCompatActivity {

    private static final String RIDER_PHONE = "0771234567";

    private FirebaseFirestore    db;
    private ListenerRegistration statusListener;
    private MaterialButton       btnReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_tracking);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        String orderId    = getIntent().getStringExtra("orderId")    != null ? getIntent()
                .getStringExtra("orderId")    : "";
        String pharmacyId = getIntent().getStringExtra("pharmacyId") != null ? getIntent()
                .getStringExtra("pharmacyId") : "";
        String riderId    = getIntent().getStringExtra("riderId")    != null ? getIntent()
                .getStringExtra("riderId")    : "";

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Call rider
        ImageView btnCall = findViewById(R.id.btnCallRider);
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + RIDER_PHONE));
                startActivity(call);
            });
        }

        // Report issue
        MaterialButton btnReport = findViewById(R.id.btnReportIssue);
        if (btnReport != null) {
            btnReport.setOnClickListener(v ->
                    startActivity(new Intent(this, ReportIssueActivity.class)));
        }

        // Review button (hidden until delivered)
        btnReview = findViewById(R.id.btnLeaveReview);
        if (btnReview != null) {
            btnReview.setVisibility(View.GONE);
            btnReview.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReviewActivity.class);
                intent.putExtra("orderId",    orderId);
                intent.putExtra("pharmacyId", pharmacyId);
                intent.putExtra("riderId",    riderId);
                startActivity(intent);
            });
        }

        // Cancel Order button
        MaterialButton btnCancelOrder = findViewById(R.id.btnCancelOrder);
        if (btnCancelOrder != null && !orderId.isEmpty()) {
            btnCancelOrder.setOnClickListener(v -> {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Cancel Order?")
                        .setMessage("Are you sure you want to cancel this order? This action cannot be undone.")
                        .setPositiveButton("Yes, cancel", (dialog, which) ->
                                db.collection("orders").document(orderId)
                                        .update("status", "cancelled")
                                        .addOnSuccessListener(aVoid ->
                                                android.widget.Toast.makeText(this, "Order cancelled successfully", android.widget.Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e ->
                                                android.widget.Toast.makeText(this, "Failed: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show()))
                        .setNegativeButton("Keep order", null)
                        .show();
            });
        }

        // Firestore real-time status listener
        if (!orderId.isEmpty()) {
            DocumentReference orderRef = db.collection("orders").document(orderId);
            statusListener = orderRef.addSnapshotListener((doc, error) -> {
                if (error != null || doc == null) return;

                String status = doc.getString("status");
                updateStatusUI(status);

                // Show review button when delivered
                if ("delivered".equals(status) && btnReview != null) {
                    btnReview.setVisibility(View.VISIBLE);
                }

                // Show cancel button if the order is still eligible to be cancelled
                if (btnCancelOrder != null) {
                    if ("pending".equalsIgnoreCase(status) || "processing".equalsIgnoreCase(status) || "assigned".equalsIgnoreCase(status)) {
                        btnCancelOrder.setVisibility(View.VISIBLE);
                    } else {
                        btnCancelOrder.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void updateStatusUI(String status) {
        if (status == null) return;

        try {
            android.widget.TextView tvStatus = findViewById(R.id.tvCurrentStatus);
            if (tvStatus != null) {
                switch (status) {
                    case "pending":
                        tvStatus.setText("Order confirmed");
                        setStepsProgress(1);
                        break;
                    case "processing":
                        tvStatus.setText("Pharmacy is preparing your order");
                        setStepsProgress(2);
                        break;
                    case "assigned":
                        tvStatus.setText("Rider is assigned to your order");
                        setStepsProgress(2);
                        break;
                    case "picked_up":
                        tvStatus.setText("Rider picked up your order");
                        setStepsProgress(3);
                        break;
                    case "out_for_delivery":
                        tvStatus.setText("Out for delivery");
                        setStepsProgress(4);
                        break;
                    case "delivered":
                        tvStatus.setText("Delivered ✓");
                        setStepsProgress(5);
                        break;
                    case "cancelled":
                        tvStatus.setText("Order cancelled ❌");
                        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                        setStepsProgress(0); // Optional: you can hide or gray out the steps if it's cancelled
                        break;
                    default:
                        tvStatus.setText(status);
                }
            }
        } catch (Exception ignored) {}
    }

    private void setStepsProgress(int activeStep) {
        int[] indicatorIds = {
                R.id.indicatorStep1,
                R.id.indicatorStep2,
                R.id.indicatorStep3,
                R.id.indicatorStep4,
                R.id.indicatorStep5
        };
        int[] iconIds = {
                R.id.iconStep1,
                R.id.iconStep2,
                R.id.iconStep3,
                R.id.iconStep4,
                R.id.iconStep5
        };

        for (int i = 0; i < 5; i++) {
            View indicator = findViewById(indicatorIds[i]);
            ImageView icon = findViewById(iconIds[i]);
            if (indicator == null) continue;

            int stepNum = i + 1;
            if (activeStep == 5 || stepNum < activeStep) {
                // Done step
                indicator.setBackgroundResource(R.drawable.bg_step_done);
                if (icon != null) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.pg_primary, null)));
                }
            } else if (stepNum == activeStep) {
                // Active step
                indicator.setBackgroundResource(R.drawable.bg_step_active);
                if (icon != null) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
                }
            } else {
                // Pending step
                indicator.setBackgroundResource(R.drawable.bg_step_pending);
                if (icon != null) {
                    icon.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) statusListener.remove();
    }
}
