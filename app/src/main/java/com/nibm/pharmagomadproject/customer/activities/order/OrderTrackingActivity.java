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
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        String orderId    = getIntent().getStringExtra("orderId")    != null ? getIntent()
                .getStringExtra("orderId")    : "";
        String pharmacyId = getIntent().getStringExtra("pharmacyId") != null ? getIntent()
                .getStringExtra("pharmacyId") : "";
        String riderId    = getIntent().getStringExtra("riderId")    != null ? getIntent()
                .getStringExtra("riderId")    : "";

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Report issue (hidden until order is delivered)
        MaterialButton btnReport = findViewById(R.id.btnReportIssue);
        if (btnReport != null) {
            btnReport.setVisibility(View.GONE);
            btnReport.setOnClickListener(v -> {
                Intent i = new Intent(this, ReportIssueActivity.class);
                i.putExtra("orderId", orderId);
                startActivity(i);
            });
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

        // Firestore real-time status listener & real data loading
        if (!orderId.isEmpty()) {
            DocumentReference orderRef = db.collection("orders").document(orderId);
            statusListener = orderRef.addSnapshotListener((doc, error) -> {
                if (error != null || doc == null || !doc.exists()) return;

                // Load real Order ID header
                android.widget.TextView tvOrderNum = findViewById(R.id.tvOrderNumber);
                if (tvOrderNum != null) {
                    tvOrderNum.setText("Order #" + doc.getId().substring(0, Math.min(8, doc.getId().length())).toUpperCase());
                }

                // Load order summary (items count + total amount)
                // Load order summary (items count + pharmacies + total amount)
                android.widget.TextView tvOrderSummary = findViewById(R.id.tvOrderSummary);
                if (tvOrderSummary != null) {
                    String pharmName = doc.getString("pharmacyName");
                    Double total = doc.getDouble("total");
                    java.util.List<?> items = (java.util.List<?>) doc.get("items");
                    java.util.List<?> pIds = (java.util.List<?>) doc.get("pharmacyIds");
                    int itemCount = items != null ? items.size() : 0;
                    int pharmCount = pIds != null && !pIds.isEmpty() ? pIds.size() : (pharmName != null && pharmName.contains(",") ? pharmName.split(",").length : 1);

                    String summary = itemCount + " item(s)" + (pharmCount > 1 ? " from " + pharmCount + " pharmacies (" + pharmName + ")" : (pharmName != null ? " from " + pharmName : "")) + " · Rs. " + (total != null ? total.intValue() : 0);
                    tvOrderSummary.setText(summary);
                }

                // Rider Info & Card Visibility ONLY show when rider is assigned
                String rName = doc.getString("riderName");
                String rPhone = doc.getString("riderPhone");
                String rId = doc.getString("riderId");
                boolean hasRider = (rName != null && !rName.trim().isEmpty())
                                || (rPhone != null && !rPhone.trim().isEmpty())
                                || (rId != null && !rId.trim().isEmpty());

                View cardRider = findViewById(R.id.cardRider);
                if (cardRider != null) {
                    cardRider.setVisibility(hasRider ? View.VISIBLE : View.GONE);
                }

                if (hasRider) {
                    android.widget.TextView tvRider = findViewById(R.id.tvRiderName);
                    if (tvRider != null) {
                        tvRider.setText(rName != null ? rName + " — your rider" : "Assigned Rider");
                    }
                    ImageView btnCall = findViewById(R.id.btnCallRider);
                    if (btnCall != null && rPhone != null && !rPhone.isEmpty()) {
                        btnCall.setOnClickListener(v -> {
                            Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + rPhone));
                            startActivity(call);
                        });
                    }
                }

                String status = doc.getString("status");
                updateStatusUI(status);

                // Show report & review buttons ONLY when delivered
                boolean isDelivered = "delivered".equalsIgnoreCase(status);
                if (btnReview != null) btnReview.setVisibility(isDelivered ? View.VISIBLE : View.GONE);
                if (btnReport != null) btnReport.setVisibility(isDelivered ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void updateStatusUI(String status) {
        if (status == null) status = "pending";

        try {
            android.widget.TextView tvStatus = findViewById(R.id.tvCurrentStatus);
            android.widget.TextView tvBadge = findViewById(R.id.tvOrderBadge);

            if (tvBadge != null) {
                tvBadge.setText(status.substring(0, 1).toUpperCase() + status.substring(1).replace("_", " "));
            }

            android.widget.TextView tvStep2Sub = findViewById(R.id.tvStep2Sub);
            android.widget.TextView tvStep3Sub = findViewById(R.id.tvStep3Sub);
            android.widget.TextView tvStep4Sub = findViewById(R.id.tvStep4Sub);
            android.widget.TextView tvStep5Sub = findViewById(R.id.tvStep5Sub);

            switch (status.toLowerCase()) {
                case "pending":
                    if (tvStatus != null) tvStatus.setText("Order Placed — Waiting for Pharmacy Confirmation");
                    if (tvStep2Sub != null) tvStep2Sub.setText("Awaiting confirmation...");
                    if (tvStep3Sub != null) tvStep3Sub.setText("Pending");
                    if (tvStep4Sub != null) tvStep4Sub.setText("Pending");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(1);
                    break;
                case "confirmed":
                case "approved":
                case "processing":
                case "ready":
                    if (tvStatus != null) tvStatus.setText("Pharmacy is preparing your order");
                    if (tvStep2Sub != null) tvStep2Sub.setText("Confirmed & Preparing");
                    if (tvStep3Sub != null) tvStep3Sub.setText("Awaiting rider assignment...");
                    if (tvStep4Sub != null) tvStep4Sub.setText("Pending");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(2);
                    break;
                case "assigned":
                    if (tvStatus != null) tvStatus.setText("Rider assigned — preparing for pickup");
                    if (tvStep2Sub != null) tvStep2Sub.setText("Confirmed & Prepared");
                    if (tvStep3Sub != null) tvStep3Sub.setText("Rider assigned, heading to pharmacy");
                    if (tvStep4Sub != null) tvStep4Sub.setText("Pending");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(2);
                    break;
                case "picked_up":
                    if (tvStatus != null) tvStatus.setText("Rider accepted — picking up your order");
                    if (tvStep2Sub != null) tvStep2Sub.setText("Confirmed & Prepared");
                    if (tvStep3Sub != null) tvStep3Sub.setText("Rider heading to pharmacy");
                    if (tvStep4Sub != null) tvStep4Sub.setText("In transit...");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(3);
                    break;
                case "out_for_delivery":
                    if (tvStatus != null) tvStatus.setText("Out for delivery");
                    if (tvStep2Sub != null) tvStep2Sub.setText("Confirmed & Prepared");
                    if (tvStep3Sub != null) tvStep3Sub.setText("Picked up by Rider");
                    if (tvStep4Sub != null) tvStep4Sub.setText("Out for delivery");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Arriving soon");
                    setStepsProgress(4);
                    break;
                case "delivered":
                    if (tvStatus != null) tvStatus.setText("Delivered ✓");
                    if (tvStep2Sub != null) tvStep2Sub.setText("Confirmed");
                    if (tvStep3Sub != null) tvStep3Sub.setText("Picked up");
                    if (tvStep4Sub != null) tvStep4Sub.setText("Completed");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Delivered successfully");
                    setStepsProgress(5);
                    break;
                default:
                    if (tvStatus != null) tvStatus.setText(status);
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
