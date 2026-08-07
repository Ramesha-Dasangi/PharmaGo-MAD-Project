package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyPrescriptionActivity extends AppCompatActivity {

    private TextView txtOrder;
    private TextView txtPatient;
    private TextView txtMedicine;

    private ImageView imgPrescription;
    private TextView txtFileName;

    private CheckBox chkSignature;
    private CheckBox chkMedicine;
    private CheckBox chkExpiry;

    private Button btnUploadPrescription;
    private Button btnReject;
    private Button btnApprove;

    private FirebaseFirestore db;
    private String currentOrderId;
    private String customerId;
    private String customerName;
    private String presUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_prescription);

        db = FirebaseFirestore.getInstance();

        txtOrder = findViewById(R.id.txtOrder);
        txtPatient = findViewById(R.id.txtPatient);
        txtMedicine = findViewById(R.id.txtMedicine);

        imgPrescription = findViewById(R.id.imgPrescription);
        txtFileName = findViewById(R.id.txtFileName);

        chkSignature = findViewById(R.id.chkSignature);
        chkMedicine = findViewById(R.id.chkMedicine);
        chkExpiry = findViewById(R.id.chkExpiry);

        btnUploadPrescription = findViewById(R.id.btnUploadPrescription);
        btnReject = findViewById(R.id.btnReject);
        btnApprove = findViewById(R.id.btnApprove);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Intent Extras
        currentOrderId = getIntent().getStringExtra("orderId");
        customerName   = getIntent().getStringExtra("customerName");
        customerId     = getIntent().getStringExtra("customerId");

        if (currentOrderId != null) {
            txtOrder.setText("Order : #" + currentOrderId);
        }
        if (customerName != null && !customerName.isEmpty()) {
            txtPatient.setText("Patient : " + customerName);
        }

        // Fetch real order data from Firestore
        loadOrderDetails();

        btnReject.setOnClickListener(v -> handleRejectAction());
        btnApprove.setOnClickListener(v -> handleApproveAction());
    }

    private void loadOrderDetails() {
        if (currentOrderId == null) return;

        String pharmUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        db.collection("orders").document(currentOrderId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Fetch customer details if name not passed
                    if (customerId == null || customerId.isEmpty()) {
                        customerId = doc.getString("customerId");
                    }
                    if ((customerName == null || customerName.isEmpty()) && customerId != null) {
                        db.collection("users").document(customerId).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String name = userDoc.getString("name");
                                        if (name != null) txtPatient.setText("Patient : " + name);
                                    }
                                });
                    }

                    // Populate Medicines
                    List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                    if (items != null && !items.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Map<String, Object> item : items) {
                            String itemPharmId = item.get("pharmacyId") != null ? item.get("pharmacyId").toString() : "";
                            if (!pharmUid.isEmpty() && !pharmUid.equals(itemPharmId)) continue;

                            if (sb.length() > 0) sb.append("\n");
                            sb.append(item.get("medicineName"))
                              .append(" x").append(item.get("quantity"));
                        }
                        txtMedicine.setText("Medicine : " + (sb.length() > 0 ? sb.toString() : "Prescription Order"));
                    } else {
                        txtMedicine.setText("Medicine : Prescription Item");
                    }

                    // Check if already responded by this pharmacy
                    List<?> confirmedPharmacies = (List<?>) doc.get("confirmedPharmacies");
                    List<?> rejectedPharmacies  = (List<?>) doc.get("rejectedPharmacies");
                    boolean alreadyConfirmed = confirmedPharmacies != null && confirmedPharmacies.contains(pharmUid);
                    boolean alreadyRejected  = rejectedPharmacies  != null && rejectedPharmacies.contains(pharmUid);

                    if (alreadyConfirmed) {
                        if (btnApprove != null) {
                            btnApprove.setVisibility(View.VISIBLE);
                            btnApprove.setText("PRESCRIPTION APPROVED ✓");
                            btnApprove.setEnabled(false);
                        }
                        if (btnReject != null) btnReject.setVisibility(View.GONE);
                        if (chkSignature != null) chkSignature.setChecked(true);
                        if (chkMedicine != null) chkMedicine.setChecked(true);
                        if (chkExpiry != null) chkExpiry.setChecked(true);
                    } else if (alreadyRejected) {
                        if (btnApprove != null) btnApprove.setVisibility(View.GONE);
                        if (btnReject != null) {
                            btnReject.setVisibility(View.VISIBLE);
                            btnReject.setText("PRESCRIPTION REJECTED ✗");
                            btnReject.setEnabled(false);
                        }
                    }

                    // Populate Prescription Image
                    presUrl = doc.getString("prescriptionUrl");
                    if (presUrl != null && !presUrl.isEmpty()) {
                        imgPrescription.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(presUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .into(imgPrescription);

                        txtFileName.setText("Prescription Attached — tap image to view full screen");

                        View.OnClickListener viewPresListener = v -> {
                            Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(presUrl));
                            startActivity(viewIntent);
                        };

                        imgPrescription.setOnClickListener(viewPresListener);

                        if (btnUploadPrescription != null) {
                            btnUploadPrescription.setVisibility(View.VISIBLE);
                            btnUploadPrescription.setText("VIEW PRESCRIPTION");
                            btnUploadPrescription.setOnClickListener(viewPresListener);
                        }
                    } else {
                        txtFileName.setText("No Prescription attached to this order");
                        if (btnUploadPrescription != null) {
                            btnUploadPrescription.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleApproveAction() {
        if (!chkSignature.isChecked()) {
            Toast.makeText(this, "Doctor signature missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!chkMedicine.isChecked()) {
            Toast.makeText(this, "Medicine name not matched", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!chkExpiry.isChecked()) {
            Toast.makeText(this, "Prescription expired", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentOrderId == null) return;
        btnApprove.setEnabled(false);

        String pharmUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        db.collection("orders").document(currentOrderId).get().addOnSuccessListener(orderDoc -> {
            if (!orderDoc.exists()) {
                btnApprove.setEnabled(true);
                return;
            }

            List<?> pharmIds = (List<?>) orderDoc.get("pharmacyIds");
            List<?> confirmedPharmacies = (List<?>) orderDoc.get("confirmedPharmacies");
            List<String> updatedConfirmed = new ArrayList<>();
            if (confirmedPharmacies != null) {
                for (Object o : confirmedPharmacies) if (o != null) updatedConfirmed.add(o.toString());
            }
            if (!pharmUid.isEmpty() && !updatedConfirmed.contains(pharmUid)) {
                updatedConfirmed.add(pharmUid);
            }

            String nextStatus = "approved_pending_payment";

            db.collection("orders").document(currentOrderId)
                    .update("status", nextStatus,
                            "confirmedPharmacies", com.google.firebase.firestore.FieldValue.arrayUnion(pharmUid))
                    .addOnSuccessListener(unused -> {
                        if (btnApprove != null) {
                            btnApprove.setText("PRESCRIPTION APPROVED ✓");
                            btnApprove.setEnabled(false);
                        }
                        if (btnReject != null) btnReject.setVisibility(View.GONE);

                        if (customerId != null && !customerId.isEmpty()) {
                            Map<String, Object> notif = new HashMap<>();
                            notif.put("userId", customerId);
                            notif.put("title", "Prescription Approved 💊");
                            notif.put("message", "Order " + currentOrderId + " prescription approved. Tap to pay now.");
                            notif.put("type", "prescription_approved");
                            notif.put("referenceId", currentOrderId);
                            notif.put("isRead", false);
                            notif.put("createdAt", System.currentTimeMillis());
                            com.nibm.pharmagomadproject.customer.CustomerNotificationHelper.sendNotification(customerId, notif);
                        }
                        Toast.makeText(this, "Prescription Approved Successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnApprove.setEnabled(true);
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> btnApprove.setEnabled(true));
    }

    private void handleRejectAction() {
        if (currentOrderId == null) return;
        btnReject.setEnabled(false);

        String pharmUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        db.collection("orders").document(currentOrderId).get().addOnSuccessListener(orderDoc -> {
            if (!orderDoc.exists()) {
                btnReject.setEnabled(true);
                return;
            }

            List<?> pharmIds = (List<?>) orderDoc.get("pharmacyIds");
            List<?> rejectedPharmacies = (List<?>) orderDoc.get("rejectedPharmacies");
            List<String> updatedRejected = new ArrayList<>();
            if (rejectedPharmacies != null) {
                for (Object o : rejectedPharmacies) if (o != null) updatedRejected.add(o.toString());
            }
            if (!pharmUid.isEmpty() && !updatedRejected.contains(pharmUid)) {
                updatedRejected.add(pharmUid);
            }

            int totalPharmCount = pharmIds != null && !pharmIds.isEmpty() ? pharmIds.size() : 1;
            boolean allRejected = updatedRejected.size() >= totalPharmCount;
            String nextStatus = allRejected ? "rejected" : "partially_rejected";

            db.collection("orders").document(currentOrderId)
                    .update("status", nextStatus,
                            "rejectedPharmacies", com.google.firebase.firestore.FieldValue.arrayUnion(pharmUid))
                    .addOnSuccessListener(unused -> {
                        if (btnReject != null) {
                            btnReject.setText("PRESCRIPTION REJECTED ✗");
                            btnReject.setEnabled(false);
                        }
                        if (btnApprove != null) btnApprove.setVisibility(View.GONE);

                        if (customerId != null && !customerId.isEmpty()) {
                            Map<String, Object> notif = new HashMap<>();
                            notif.put("userId", customerId);
                            notif.put("title", "Prescription Rejected ⚠️");
                            notif.put("message", "Prescription for Order " + currentOrderId + " was rejected.");
                            notif.put("type", nextStatus);
                            notif.put("referenceId", currentOrderId);
                            notif.put("isRead", false);
                            notif.put("createdAt", System.currentTimeMillis());
                            com.nibm.pharmagomadproject.customer.CustomerNotificationHelper.sendNotification(customerId, notif);

                            String paymentMethod = orderDoc.getString("paymentMethod");
                            if ("card".equalsIgnoreCase(paymentMethod)) {
                                Map<String, Object> refundNotif = new HashMap<>();
                                refundNotif.put("userId", customerId);
                                refundNotif.put("title", "Refund Initiated 💳");
                                refundNotif.put("message", "Your order " + currentOrderId + " was rejected. A refund will be processed to your card within 2 working days.");
                                refundNotif.put("type", "refund");
                                refundNotif.put("referenceId", currentOrderId);
                                refundNotif.put("isRead", false);
                                refundNotif.put("createdAt", System.currentTimeMillis());
                                com.nibm.pharmagomadproject.customer.CustomerNotificationHelper.sendNotification(customerId, refundNotif);
                            }
                        }
                        Toast.makeText(this, "Prescription Rejected.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnReject.setEnabled(true);
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> btnReject.setEnabled(true));
    }
}