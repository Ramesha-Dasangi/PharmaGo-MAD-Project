package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import com.nibm.pharmagomadproject.R;

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
            });
        }
    }

    private void updateStatusUI(String status) {
        if (status == null) return;

        // Update step indicators based on status
        // (These IDs should match your activity_order_tracking.xml)
        try {
            android.widget.TextView tvStatus = findViewById(R.id.tvCurrentStatus);
            if (tvStatus != null) {
                switch (status) {
                    case "pending":
                        tvStatus.setText("Order confirmed");
                        break;
                    case "processing":
                        tvStatus.setText("Pharmacy is preparing your order");
                        break;
                    case "picked_up":
                        tvStatus.setText("Rider picked up your order");
                        break;
                    case "out_for_delivery":
                        tvStatus.setText("Out for delivery");
                        break;
                    case "delivered":
                        tvStatus.setText("Delivered ✓");
                        break;
                    default:
                        tvStatus.setText(status);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) statusListener.remove();
    }
}
