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
    private String currentStatus = "pending";

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
            loadOrderDetails();
        }

        btnUploadPrescription.setOnClickListener(v -> showImagePickerDialog());

        // ── Action buttons ──
        btnApprove.setOnClickListener(v -> handleApproveAction());
        btnReject.setOnClickListener(v -> handleRejectAction());
    }

    // ═══════════════════════════════════════════════════
    //  Load Order — items, status, prescription, total
    // ═══════════════════════════════════════════════════
    private void loadOrderDetails() {
        db.collection("orders").document(currentOrderId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    currentStatus = doc.getString("status");
                    if (currentStatus == null) currentStatus = "pending";

                    // Render current status
                    updateStatusBadge(currentStatus);

                    // Update action buttons based on status
                    refreshActionButtons(currentStatus);

                    // Build items text
                    List<Map<String, Object>> items =
                            (List<Map<String, Object>>) doc.get("items");
                    if (items != null && !items.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Map<String, Object> item : items) {
                            if (sb.length() > 0) sb.append("\n");
                            Object name = item.get("medicineName");
                            Object qty  = item.get("quantity");
                            Object price = item.get("price");
                            sb.append(name != null ? name : "—")
                              .append("  ×")
                              .append(qty != null ? qty : "?")
                              .append("   Rs. ")
                              .append(price != null
                                      ? String.format("%.0f", ((Number) price).doubleValue())
                                      : "0");
                        }
                        txtItems.setText(sb.toString());
                    }

                    // Prescription
                    String presUrl = doc.getString("prescriptionUrl");
                    if (presUrl != null && !presUrl.isEmpty()) {
                        txtFileName.setText("Customer's prescription:");
                        com.bumptech.glide.Glide.with(OrderDetailsActivity.this)
                                .load(presUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.stat_notify_error)
                                .into(imgPrescription);
                        btnUploadPrescription.setVisibility(View.GONE);
                    } else {
                        txtFileName.setText("No prescription uploaded by customer");
                        btnUploadPrescription.setVisibility(View.GONE);
                    }

                    // Total
                    Double total = doc.getDouble("total");
                    if (total != null) txtTotal.setText("Total : Rs. " + String.format("%.2f", total));
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load order: " + e.getMessage()));
    }

    // ═══════════════════════════════════════════════════
    //  Status Badge display (optional view)
    // ═══════════════════════════════════════════════════
    private void updateStatusBadge(String status) {
        if (txtStatus == null) return;
        txtStatus.setVisibility(View.VISIBLE);
        switch (status.toLowerCase()) {
            case "pending":
                txtStatus.setText("⏳ Pending");
                txtStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
                break;
            case "processing":
                txtStatus.setText("⚙️ Processing");
                txtStatus.setTextColor(getColor(android.R.color.holo_blue_dark));
                break;
            case "ready":
                txtStatus.setText("✅ Ready for Pickup");
                txtStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                break;
            case "completed":
                txtStatus.setText("🎉 Completed");
                txtStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                break;
            case "rejected":
            case "cancelled":
                txtStatus.setText("❌ Cancelled / Rejected");
                txtStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                break;
            default:
                txtStatus.setText(status);
        }
    }

    // ═══════════════════════════════════════════════════
    //  Adjust button labels based on current order state
    // ═══════════════════════════════════════════════════
    private void refreshActionButtons(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                btnApprove.setText("APPROVE");
                btnApprove.setEnabled(true);
                btnReject.setText("REJECT");
                btnReject.setEnabled(true);
                break;
            case "processing":
                btnApprove.setText("DONE");
                btnApprove.setEnabled(true);
                btnReject.setText("CANCEL");
                btnReject.setEnabled(true);
                break;
            case "ready":
                btnApprove.setText("MARK COMPLETED");
                btnApprove.setEnabled(true);
                btnReject.setText("CANCEL");
                btnReject.setEnabled(true);
                break;
            case "completed":
            case "rejected":
            case "cancelled":
                btnApprove.setEnabled(true);
                btnReject.setEnabled(true);
                btnApprove.setText("DONE ✓");
                btnReject.setText("CLOSE");
                break;
        }
    }

    // ═══════════════════════════════════════════════════
    //  APPROVE  →  Pending → Processing → Completed
    // ═══════════════════════════════════════════════════
    private void handleApproveAction() {
        if (currentOrderId == null) return;

        String nextStatus;
        String toastMsg;
        String notifTitle;
        String notifBody;

        switch (currentStatus.toLowerCase()) {
            case "pending":
                nextStatus  = "processing";
                toastMsg    = "Order approved — now processing!";
                notifTitle  = "Pharmacy is preparing your order 💊";
                notifBody   = "Order " + currentOrderId + " has been approved and is being prepared.";
                break;
            case "processing":
                nextStatus  = "completed";
                toastMsg    = "Order marked as completed!";
                notifTitle  = "Order Completed ✅";
                notifBody   = "Order " + currentOrderId + " has been marked as completed. Thank you!";
                break;
            case "ready":
                nextStatus  = "completed";
                toastMsg    = "Order marked as completed!";
                notifTitle  = "Order Completed ✅";
                notifBody   = "Order " + currentOrderId + " has been marked as completed. Thank you!";
                break;
            case "completed":
            case "rejected":
            case "cancelled":
                // Tap DONE to simply close the detail screen
                finish();
                return;
            default:
                return;
        }

        final String finalNextStatus = nextStatus;
        final String finalToastMsg   = toastMsg;
        final String finalNotifTitle = notifTitle;
        final String finalNotifBody  = notifBody;

        btnApprove.setEnabled(false);

        if ("completed".equals(finalNextStatus)) {
            // Atomic stock deduction on completion
            deductStockAndComplete(finalToastMsg, finalNotifTitle, finalNotifBody);
        } else {
            db.collection("orders").document(currentOrderId)
                    .update("status", finalNextStatus)
                    .addOnSuccessListener(unused -> {
                        sendCustomerNotification(finalNotifTitle, finalNotifBody, finalNextStatus);
                        Toast.makeText(this, finalToastMsg, Toast.LENGTH_SHORT).show();
                        currentStatus = finalNextStatus;
                        refreshActionButtons(currentStatus);
                        updateStatusBadge(currentStatus);
                        btnApprove.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        btnApprove.setEnabled(true);
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // ═══════════════════════════════════════════════════
    //  REJECT / CANCEL
    // ═══════════════════════════════════════════════════
    private void handleRejectAction() {
        if (currentOrderId == null) return;

        // If order is already closed/completed, tapping CLOSE simply exits the screen
        switch (currentStatus.toLowerCase()) {
            case "completed":
            case "rejected":
            case "cancelled":
                finish();
                return;
        }

        String nextStatus = currentStatus.equalsIgnoreCase("pending") ? "rejected" : "cancelled";

        btnReject.setEnabled(false);
        db.collection("orders").document(currentOrderId)
                .update("status", nextStatus)
                .addOnSuccessListener(unused -> {
                    sendCustomerNotification(
                            "Order Update ⚠️",
                            "Order " + currentOrderId + " was " + nextStatus + " by the pharmacy.",
                            nextStatus);
                    Toast.makeText(this, "Order " + nextStatus + ".", Toast.LENGTH_SHORT).show();
                    currentStatus = nextStatus;
                    refreshActionButtons(currentStatus);
                    updateStatusBadge(currentStatus);
                })
                .addOnFailureListener(e -> {
                    btnReject.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
}