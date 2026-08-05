package com.nibm.pharmagomadproject.pharmacyowner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.nibm.pharmagomadproject.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailsActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;

    // ──────────────── Views ────────────────
    private TextView txtOrderId, txtDate, txtCustomer, txtPhone,
                     txtAddress, txtTotal, txtItems, txtStatus;
    private ImageView imgPrescription;
    private TextView txtFileName;
    private Button btnUploadPrescription, btnReject, btnApprove;

    // ──────────────── Firebase ────────────────
    private FirebaseFirestore db;
    private String currentOrderId;
    private String customerId;
    private String currentStatus = "";
    private boolean isPrescriptionOrder = false;

    // ──────────────── Camera / Gallery ────────────────
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {
                            Uri uri = result.getData().getData();
                            imgPrescription.setImageURI(uri);
                            txtFileName.setText("Prescription Selected");
                        }
                    });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                Bitmap bitmap = (Bitmap) extras.get("data");
                                imgPrescription.setImageBitmap(bitmap);
                                txtFileName.setText("Prescription Captured");
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // ── View Binding ──
        txtOrderId   = findViewById(R.id.txtOrderId);
        txtDate      = findViewById(R.id.txtDate);
        txtCustomer  = findViewById(R.id.txtCustomer);
        txtPhone     = findViewById(R.id.txtPhone);
        txtAddress   = findViewById(R.id.txtAddress);
        txtTotal     = findViewById(R.id.txtTotal);
        txtItems     = findViewById(R.id.txtItems);
        imgPrescription  = findViewById(R.id.imgPrescription);
        txtFileName      = findViewById(R.id.txtFileName);
        btnUploadPrescription = findViewById(R.id.btnUploadPrescription);
        btnReject  = findViewById(R.id.btnReject);
        btnApprove = findViewById(R.id.btnApprove);

        if (btnReject != null) {
            btnReject.setOnClickListener(v -> handleRejectAction());
        }

        // Optional status label (may not exist in older layout)
        int resId = getResources().getIdentifier("txtStatus", "id", getPackageName());
        txtStatus = resId != 0 ? findViewById(resId) : null;

        db = FirebaseFirestore.getInstance();

        // ── Intent Extras ──
        currentOrderId = getIntent().getStringExtra("orderId");
        customerId     = getIntent().getStringExtra("customerId");
        String customer = getIntent().getStringExtra("customerName");
        String time     = getIntent().getStringExtra("time");
        String amount   = getIntent().getStringExtra("amount");

        if (currentOrderId != null) txtOrderId.setText(currentOrderId);
        if (time      != null) txtDate.setText(time);
        if (customer  != null) txtCustomer.setText(customer);
        if (amount    != null) txtTotal.setText("Total : " + amount);

        // ── Back ──
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ── Load customer details ──
        if (customerId != null && !customerId.isEmpty()) {
            db.collection("users").document(customerId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String phone   = doc.getString("phone");
                            String address = doc.getString("address");
                            if (phone   != null) txtPhone.setText(phone);
                            if (address != null) txtAddress.setText(address);
                        }
                    });
        } else {
            txtPhone.setText("—");
            txtAddress.setText("—");
        }

        // ── Load full order from Firestore ──
        if (currentOrderId != null) {
            String pharmUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                    ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

            db.collection("orders").document(currentOrderId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            java.util.List<java.util.Map<String, Object>> items =
                                     (java.util.List<java.util.Map<String, Object>>) doc.get("items");

                            // Only show items belonging to this pharmacy owner
                            if (items != null) {
                                StringBuilder desc = new StringBuilder();
                                double myTotal = 0;
                                for (java.util.Map<String, Object> item : items) {
                                    String itemPharmId = item.get("pharmacyId") != null ? item.get("pharmacyId").toString() : "";
                                    if (!pharmUid.isEmpty() && !pharmUid.equals(itemPharmId)) continue;
                                    if (desc.length() > 0) desc.append("\n");
                                    desc.append(item.get("medicineName"))
                                        .append(" x").append(item.get("quantity"))
                                        .append("  —  Rs. ")
                                        .append(String.format("%.0f", ((Number) item.getOrDefault("price", 0)).doubleValue()));
                                    Object price = item.get("price");
                                    Object qty   = item.get("quantity");
                                    if (price instanceof Number && qty instanceof Number) {
                                        myTotal += ((Number) price).doubleValue() * ((Number) qty).doubleValue();
                                    }
                                }
                                if (txtItems != null) {
                                    txtItems.setText(desc.length() > 0 ? desc.toString() : "No items found for your pharmacy");
                                }
                                if (myTotal > 0 && txtTotal != null) {
                                    txtTotal.setText("Your Items Total : Rs. " + (int) myTotal);
                                }
                            }

                            // Show prescription if present
                            String presUrl = doc.getString("prescriptionUrl");
                            if (presUrl != null && !presUrl.isEmpty()) {
                                isPrescriptionOrder = true;
                                View.OnClickListener viewPresListener = pv -> {
                                    android.content.Intent viewIntent = new android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(presUrl));
                                    startActivity(viewIntent);
                                };
                                if (btnUploadPrescription != null) {
                                    btnUploadPrescription.setVisibility(android.view.View.VISIBLE);
                                    btnUploadPrescription.setText("VIEW PRESCRIPTION");
                                    btnUploadPrescription.setOnClickListener(viewPresListener);
                                }
                                if (imgPrescription != null) {
                                    imgPrescription.setVisibility(android.view.View.VISIBLE);
                                    com.bumptech.glide.Glide.with(this)
                                            .load(presUrl)
                                            .placeholder(android.R.drawable.ic_menu_gallery)
                                            .into(imgPrescription);
                                    imgPrescription.setOnClickListener(viewPresListener);
                                }
                                if (txtFileName != null) {
                                    txtFileName.setText("Prescription attached — tap image or button to view");
                                }
                            } else {
                                // Not a prescription order or no prescription attached
                                if (btnUploadPrescription != null) {
                                    btnUploadPrescription.setVisibility(android.view.View.GONE);
                                }
                                if (txtFileName != null) {
                                    txtFileName.setText("No Prescription attached to this order");
                                }
                            }
                            String st = doc.getString("status");
                            if (st != null) {
                                currentStatus = st;
                                updateStatusBadge(currentStatus);
                                refreshActionButtons(currentStatus);
                            }

                            Double total = doc.getDouble("total");
                            if (total != null && txtTotal != null && txtTotal.getText().toString().startsWith("Total")) {
                                txtTotal.setText("Total : Rs. " + total.intValue());
                            }
                        }
                    });
        }


        // Approve — set status to 'approved_pending_payment' for Rx orders, 'processing' if all pharmacies confirmed, or 'partially_approved'
        btnApprove.setOnClickListener(v -> {
            if (currentOrderId == null) return;
            btnApprove.setEnabled(false);

            String pharmUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                    ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : "";

            db.collection("orders").document(currentOrderId).get().addOnSuccessListener(orderDoc -> {
                if (!orderDoc.exists()) {
                    btnApprove.setEnabled(true);
                    return;
                }

                java.util.List<?> pharmIds = (java.util.List<?>) orderDoc.get("pharmacyIds");
                java.util.List<?> confirmedPharmacies = (java.util.List<?>) orderDoc.get("confirmedPharmacies");
                java.util.List<String> updatedConfirmed = new java.util.ArrayList<>();
                if (confirmedPharmacies != null) {
                    for (Object o : confirmedPharmacies) if (o != null) updatedConfirmed.add(o.toString());
                }
                if (!pharmUid.isEmpty() && !updatedConfirmed.contains(pharmUid)) {
                    updatedConfirmed.add(pharmUid);
                }

                int totalPharmCount = pharmIds != null && !pharmIds.isEmpty() ? pharmIds.size() : 1;
                boolean allConfirmed = updatedConfirmed.size() >= totalPharmCount;
                String nextStatus = isPrescriptionOrder ? "approved_pending_payment" : (allConfirmed ? "processing" : "partially_approved");

                db.collection("orders").document(currentOrderId)
                        .update("status", nextStatus,
                                "confirmedPharmacies", com.google.firebase.firestore.FieldValue.arrayUnion(pharmUid))
                        .addOnSuccessListener(unused -> {
                            if (customerId != null && !customerId.isEmpty()) {
                                java.util.Map<String, Object> notif = new java.util.HashMap<>();
                                notif.put("userId", customerId);
                                if (isPrescriptionOrder) {
                                    notif.put("title", "Prescription Approved 💊");
                                    notif.put("message", "Order " + currentOrderId + " prescription approved. Tap to pay now.");
                                    notif.put("type", "prescription_approved");
                                } else if (allConfirmed) {
                                    notif.put("title", "Order Approved 💊");
                                    notif.put("message", "All pharmacies confirmed order " + currentOrderId + ". Now being prepared!");
                                    notif.put("type", "order_processing");
                                } else {
                                    notif.put("title", "Order Update 💊");
                                    notif.put("message", "A pharmacy confirmed items in order " + currentOrderId + ". Waiting for remaining pharmacies.");
                                    notif.put("type", "order_processing");
                                }
                                notif.put("referenceId", currentOrderId);
                                notif.put("isRead", false);
                                notif.put("createdAt", System.currentTimeMillis());
                                db.collection("notifications").add(notif);
                            }
                            String msg = isPrescriptionOrder ? "Order approved — awaiting payment!" : "Order approved!";
                            Toast.makeText(OrderDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            btnApprove.setEnabled(true);
                            Toast.makeText(OrderDetailsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }).addOnFailureListener(e -> btnApprove.setEnabled(true));
        });
    }

    // ═══════════════════════════════════════════════════
    //  REJECT / CANCEL
    // ═══════════════════════════════════════════════════
    private void handleRejectAction() {
        if (currentOrderId == null) return;

        switch (currentStatus.toLowerCase()) {
            case "completed":
            case "rejected":
            case "cancelled":
                finish();
                return;
        }

        btnReject.setEnabled(false);

        String pharmUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        db.collection("orders").document(currentOrderId).get().addOnSuccessListener(orderDoc -> {
            if (!orderDoc.exists()) {
                btnReject.setEnabled(true);
                return;
            }

            java.util.List<?> pharmIds = (java.util.List<?>) orderDoc.get("pharmacyIds");
            java.util.List<?> rejectedPharmacies = (java.util.List<?>) orderDoc.get("rejectedPharmacies");
            java.util.List<String> updatedRejected = new java.util.ArrayList<>();
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
                        sendCustomerNotification(
                                "Order Update ⚠️",
                                allRejected ? "Order " + currentOrderId + " was rejected by all pharmacies."
                                            : "A pharmacy rejected items in Order " + currentOrderId + ". Remaining items are still active.",
                                nextStatus);

                        // Refund notification if order was paid by card
                        String paymentMethod = orderDoc.getString("paymentMethod");
                        if ("card".equalsIgnoreCase(paymentMethod) && customerId != null && !customerId.isEmpty()) {
                            java.util.Map<String, Object> refundNotif = new java.util.HashMap<>();
                            refundNotif.put("userId", customerId);
                            refundNotif.put("title", "Refund Initiated 💳");
                            refundNotif.put("message", "Your order " + currentOrderId + " was rejected. A refund will be processed to your card within 2 working days.");
                            refundNotif.put("type", "refund");
                            refundNotif.put("referenceId", currentOrderId);
                            refundNotif.put("isRead", false);
                            refundNotif.put("createdAt", System.currentTimeMillis());
                            db.collection("notifications").add(refundNotif);
                        }

                        Toast.makeText(this, "Order status updated.", Toast.LENGTH_SHORT).show();
                        currentStatus = nextStatus;
                        refreshActionButtons(currentStatus);
                        updateStatusBadge(currentStatus);
                    })
                    .addOnFailureListener(e -> {
                        btnReject.setEnabled(true);
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> btnReject.setEnabled(true));
    }

    // ═══════════════════════════════════════════════════
    //  Atomic stock deduction via Firestore Transaction
    // ═══════════════════════════════════════════════════
    private void deductStockAndComplete(String toastMsg, String notifTitle, String notifBody) {
        db.collection("orders").document(currentOrderId).get()
                .addOnSuccessListener(orderDoc -> {
                    if (!orderDoc.exists()) {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                        btnApprove.setEnabled(true);
                        return;
                    }

                    List<Map<String, Object>> items =
                            (List<Map<String, Object>>) orderDoc.get("items");

                    if (items == null || items.isEmpty()) {
                        // No items — just mark complete
                        finalizeOrderStatus(toastMsg, notifTitle, notifBody);
                        return;
                    }

                    // Collect medicine IDs and quantities
                    Map<String, Long> deductions = new HashMap<>();
                    for (Map<String, Object> item : items) {
                        Object medIdObj = item.get("medicineId");
                        Object qtyObj   = item.get("quantity");
                        if (medIdObj == null) continue;
                        String medId = medIdObj.toString();
                        long qty = qtyObj != null ? ((Number) qtyObj).longValue() : 1L;
                        deductions.merge(medId, qty, Long::sum);
                    }

                    if (deductions.isEmpty()) {
                        finalizeOrderStatus(toastMsg, notifTitle, notifBody);
                        return;
                    }

                    // Run Firestore Transaction for atomic stock deduction
                    db.runTransaction(transaction -> {
                        // Read all medicine documents first
                        Map<String, DocumentSnapshot> docs = new HashMap<>();
                        for (String medId : deductions.keySet()) {
                            DocumentSnapshot snap = transaction.get(
                                    db.collection("medicines").document(medId));
                            docs.put(medId, snap);
                        }

                        // Write phase — deduct stock
                        for (Map.Entry<String, Long> entry : deductions.entrySet()) {
                            String medId = entry.getKey();
                            long deductQty = entry.getValue();
                            DocumentSnapshot snap = docs.get(medId);
                            if (snap == null || !snap.exists()) continue;

                            Long currentStock = snap.getLong("stock");
                            if (currentStock == null) currentStock = 0L;
                            long newStock = Math.max(0, currentStock - deductQty);

                            transaction.update(
                                    db.collection("medicines").document(medId),
                                    "stock", newStock,
                                    "updatedAt", System.currentTimeMillis());
                        }

                        // Update order status
                        transaction.update(
                                db.collection("orders").document(currentOrderId),
                                "status", "completed",
                                "completedAt", System.currentTimeMillis());

                        return null;
                    }).addOnSuccessListener(unused -> {
                        sendCustomerNotification(notifTitle, notifBody, "completed");
                        Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                        currentStatus = "completed";
                        refreshActionButtons(currentStatus);
                        updateStatusBadge(currentStatus);

                        // Trigger low-stock notifications for any medicine now below threshold
                        checkAndNotifyLowStock(deductions);
                    }).addOnFailureListener(e -> {
                        btnApprove.setEnabled(true);
                        Log.e(TAG, "Transaction failed: " + e.getMessage(), e);
                        Toast.makeText(this,
                                "Failed to complete order: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    btnApprove.setEnabled(true);
                    Toast.makeText(this, "Failed to load order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ═══════════════════════════════════════════════════
    //  Finalize order status without stock deduction
    // ═══════════════════════════════════════════════════
    private void finalizeOrderStatus(String toastMsg, String notifTitle, String notifBody) {
        db.collection("orders").document(currentOrderId)
                .update("status", "completed",
                        "completedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> {
                    sendCustomerNotification(notifTitle, notifBody, "completed");
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                    currentStatus = "completed";
                    refreshActionButtons(currentStatus);
                    updateStatusBadge(currentStatus);
                })
                .addOnFailureListener(e -> {
                    btnApprove.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ═══════════════════════════════════════════════════
    //  Post-completion low-stock alerts for deducted medicines
    // ═══════════════════════════════════════════════════
    private void checkAndNotifyLowStock(Map<String, Long> deductions) {
        for (String medId : deductions.keySet()) {
            db.collection("medicines").document(medId).get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.exists()) return;
                        Long stock = snap.getLong("stock");
                        String name = snap.getString("medicineName");
                        if (stock != null && stock <= 10 && name != null) {
                            NotificationHelper.addNotification(
                                    "⚠️ Low Stock: " + name,
                                    name + " stock is critically low (" + stock + " units remaining).",
                                    "stock");
                        }
                    });
        }
    }

    // ═══════════════════════════════════════════════════
    //  Send customer push notification
    // ═══════════════════════════════════════════════════
    private void sendCustomerNotification(String title, String body, String type) {
        if (customerId == null || customerId.isEmpty()) return;

        Map<String, Object> notif = new HashMap<>();
        notif.put("userId",      customerId);
        notif.put("title",       title);
        notif.put("message",     body);
        notif.put("type",        type);
        notif.put("referenceId", currentOrderId);
        notif.put("isRead",      false);
        notif.put("createdAt",   System.currentTimeMillis());
        db.collection("notifications").add(notif)
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to send customer notification: " + e.getMessage()));
    }

    // ═══════════════════════════════════════════════════
    //  Camera / Gallery
    // ═══════════════════════════════════════════════════
    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery / Files"};
        new AlertDialog.Builder(this)
                .setTitle("Upload Prescription")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.CAMERA},
                                    CAMERA_PERMISSION_CODE);
                        } else {
                            openCamera();
                        }
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ═══════════════════════════════════════════════════
    //  UI Helpers
    // ═══════════════════════════════════════════════════
    private void refreshActionButtons(String status) {
        if (status == null) return;
        String ownerId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        // Re-check if this pharmacy owner already confirmed, to decide button state
        if (currentOrderId != null && !currentOrderId.isEmpty()) {
            db.collection("orders").document(currentOrderId).get().addOnSuccessListener(orderDoc -> {
                if (!orderDoc.exists()) return;
                java.util.List<?> confirmedList = (java.util.List<?>) orderDoc.get("confirmedPharmacies");
                java.util.List<?> rejectedList  = (java.util.List<?>) orderDoc.get("rejectedPharmacies");
                boolean alreadyConfirmed = confirmedList != null && confirmedList.contains(ownerId);
                boolean alreadyRejected  = rejectedList  != null && rejectedList.contains(ownerId);

                if (alreadyConfirmed) {
                    if (btnApprove != null) {
                        btnApprove.setVisibility(android.view.View.VISIBLE);
                        btnApprove.setText("ORDER CONFIRMED ✓");
                        btnApprove.setEnabled(false);
                    }
                    if (btnReject != null) btnReject.setVisibility(android.view.View.GONE);
                } else if (alreadyRejected) {
                    if (btnApprove != null) btnApprove.setVisibility(android.view.View.GONE);
                    if (btnReject != null) {
                        btnReject.setVisibility(android.view.View.VISIBLE);
                        btnReject.setText("ORDER REJECTED ✗");
                        btnReject.setEnabled(false);
                    }
                } else {
                    switch (status.toLowerCase()) {
                        case "pending":
                        case "awaiting_approval":
                        case "partially_approved":
                        case "partially_rejected":
                        case "processing":
                        case "approved_pending_payment":
                            if (btnApprove != null) {
                                btnApprove.setVisibility(android.view.View.VISIBLE);
                                btnApprove.setEnabled(true);
                                btnApprove.setText("Approve");
                            }
                            if (btnReject != null) {
                                btnReject.setVisibility(android.view.View.VISIBLE);
                                btnReject.setEnabled(true);
                                btnReject.setText("Reject");
                            }
                            break;
                        case "rejected":
                        case "cancelled":
                        case "delivered":
                        case "completed":
                        default:
                            if (btnApprove != null) btnApprove.setVisibility(android.view.View.GONE);
                            if (btnReject != null) btnReject.setVisibility(android.view.View.GONE);
                            break;
                    }
                }
            });
        } else {
            // Fallback without DB check
            switch (status.toLowerCase()) {
                case "pending":
                case "awaiting_approval":
                case "partially_approved":
                    if (btnApprove != null) { btnApprove.setVisibility(android.view.View.VISIBLE); btnApprove.setEnabled(true); }
                    if (btnReject != null)  { btnReject.setVisibility(android.view.View.VISIBLE); btnReject.setEnabled(true); }
                    break;
                default:
                    if (btnApprove != null) btnApprove.setVisibility(android.view.View.GONE);
                    if (btnReject != null)  btnReject.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void updateStatusBadge(String status) {
        if (txtStatus != null && status != null) {
            txtStatus.setText(status.toUpperCase());
        }
    }
}