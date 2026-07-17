package com.nibm.pharmagomadproject.pharmacyowner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nibm.pharmagomadproject.R;

public class OrderDetailsActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private TextView txtOrderId;
    private TextView txtDate;
    private TextView txtCustomer;
    private TextView txtPhone;
    private TextView txtAddress;
    private TextView txtTotal;

    private ImageView imgPrescription;

    private TextView txtFileName;
    private ImageView btnBack;

    private Button btnUploadPrescription;
    private Button btnReject;
    private Button btnApprove;

    // ==========================
    // Gallery Launcher
    // ==========================

    //==========================
    // Gallery / Files Picker
    //==========================

    private com.google.firebase.firestore.FirebaseFirestore db;
    private String currentOrderId;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK &&
                                result.getData() != null &&
                                result.getData().getData() != null) {

                            Uri uri = result.getData().getData();

                            imgPrescription.setImageURI(uri);

                            txtFileName.setText("Prescription Selected");

                        }

                    });


    // ==========================
    // Camera
    //==========================

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK &&
                                result.getData() != null) {

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

        // ==========================
        // Initialize Views
        // ==========================

        btnBack = findViewById(R.id.btnBack);

        txtOrderId = findViewById(R.id.txtOrderId);
        txtDate = findViewById(R.id.txtDate);
        txtCustomer = findViewById(R.id.txtCustomer);
        txtPhone = findViewById(R.id.txtPhone);
        txtAddress = findViewById(R.id.txtAddress);
        txtTotal = findViewById(R.id.txtTotal);

        imgPrescription = findViewById(R.id.imgPrescription);
        txtFileName = findViewById(R.id.txtFileName);

        btnUploadPrescription = findViewById(R.id.btnUploadPrescription);

        btnReject = findViewById(R.id.btnReject);
        btnApprove = findViewById(R.id.btnApprove);

        // ==========================
        // Back Button
        // ==========================

        btnBack.setOnClickListener(v -> finish());

        // Receive orderId
        currentOrderId = getIntent().getStringExtra("orderId");
        String customer = getIntent().getStringExtra("customerName");
        String time = getIntent().getStringExtra("time");
        String amount = getIntent().getStringExtra("amount");
        String customerId = getIntent().getStringExtra("customerId");

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        if (currentOrderId != null) txtOrderId.setText(currentOrderId);
        if (time != null) txtDate.setText(time);
        if (customer != null) txtCustomer.setText(customer);
        if (amount != null) txtTotal.setText("Total : " + amount);

        // Load real address & phone from users collection
        if (customerId != null && !customerId.isEmpty()) {
            db.collection("users").document(customerId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String phone = doc.getString("phone");
                            String address = doc.getString("address");
                            if (phone != null) txtPhone.setText(phone);
                            if (address != null) txtAddress.setText(address);
                        }
                    });
        } else {
            txtPhone.setText("—");
            txtAddress.setText("—");
        }

        // Also load order from Firestore to refresh any live data
        if (currentOrderId != null) {
            db.collection("orders").document(currentOrderId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            java.util.List<java.util.Map<String, Object>> items =
                                    (java.util.List<java.util.Map<String, Object>>) doc.get("items");
                            if (items != null) {
                                StringBuilder desc = new StringBuilder();
                                for (java.util.Map<String, Object> item : items) {
                                    if (desc.length() > 0) desc.append("\n");
                                    desc.append(item.get("medicineName"))
                                        .append(" x").append(item.get("quantity"))
                                        .append("  —  Rs. ")
                                        .append(String.format("%.0f", ((Number) item.getOrDefault("price", 0)).doubleValue()));
                                }
                                // Show items in txtDate as a secondary display if available
                                // (txtDate already has time, so only update if customer/amount not yet set)
                            }
                            // Show prescription if present
                            String presUrl = doc.getString("prescriptionUrl");
                            if (presUrl != null && !presUrl.isEmpty() && txtFileName != null) {
                                txtFileName.setText("Prescription attached — tap to view");
                            }
                            Double total = doc.getDouble("total");
                            if (total != null) txtTotal.setText("Total : Rs. " + total.intValue());
                        }
                    });
        }

        btnUploadPrescription.setOnClickListener(v -> showImagePickerDialog());

        // Approve — set status to 'processing'
        btnApprove.setOnClickListener(v -> {
            if (currentOrderId == null) return;
            btnApprove.setEnabled(false);
            db.collection("orders").document(currentOrderId)
                    .update("status", "processing")
                    .addOnSuccessListener(unused -> {
                        // Notify customer
                        if (customerId != null && !customerId.isEmpty()) {
                            java.util.Map<String, Object> notif = new java.util.HashMap<>();
                            notif.put("userId",      customerId);
                            notif.put("title",       "Pharmacy preparing your order 💊");
                            notif.put("message",     "Order " + currentOrderId + " has been approved and is being prepared.");
                            notif.put("type",        "order_processing");
                            notif.put("referenceId", currentOrderId);
                            notif.put("isRead",      false);
                            notif.put("createdAt",   System.currentTimeMillis());
                            db.collection("notifications").add(notif);
                        }
                        Toast.makeText(OrderDetailsActivity.this,
                                "Order approved — now processing!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnApprove.setEnabled(true);
                        Toast.makeText(OrderDetailsActivity.this,
                                "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Reject — set status to 'rejected'
        btnReject.setOnClickListener(v -> {
            if (currentOrderId == null) return;
            btnReject.setEnabled(false);
            db.collection("orders").document(currentOrderId)
                    .update("status", "rejected")
                    .addOnSuccessListener(unused -> {
                        // Notify customer
                        if (customerId != null && !customerId.isEmpty()) {
                            java.util.Map<String, Object> notif = new java.util.HashMap<>();
                            notif.put("userId",      customerId);
                            notif.put("title",       "Order update ⚠️");
                            notif.put("message",     "Order " + currentOrderId + " was rejected by the pharmacy. Please contact support.");
                            notif.put("type",        "order_rejected");
                            notif.put("referenceId", currentOrderId);
                            notif.put("isRead",      false);
                            notif.put("createdAt",   System.currentTimeMillis());
                            db.collection("notifications").add(notif);
                        }
                        Toast.makeText(OrderDetailsActivity.this,
                                "Order rejected.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnReject.setEnabled(true);
                        Toast.makeText(OrderDetailsActivity.this,
                                "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

    }

    // ==========================
    // Camera / Gallery Dialog
    // ==========================

    private void showImagePickerDialog() {

        String[] options = {
                "Take Photo",
                "Choose from Gallery / Files"
        };

        new AlertDialog.Builder(this)
                .setTitle("Upload Prescription")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {

                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(
                                    this,
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

    // ==========================
    // Open Camera
    // ==========================

    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {

            cameraLauncher.launch(intent);

        } else {

            Toast.makeText(
                    this,
                    "Camera not available",
                    Toast.LENGTH_SHORT
            ).show();

        }

    }


    //==================================================
    // Gallery / Files
    //==================================================

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("image/*");

        galleryLauncher.launch(intent);

    }

    // ==========================
    // Permission Result
    // ==========================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                openCamera();

            } else {

                Toast.makeText(
                        this,
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT
                ).show();

            }

        }

    }

}