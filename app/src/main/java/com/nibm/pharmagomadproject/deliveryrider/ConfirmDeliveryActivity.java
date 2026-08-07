package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;


public class ConfirmDeliveryActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ImageView ivPhotoResult;
    private TextView tvCustomerName, tvCustomerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_delivery);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvCustomerName    = findViewById(R.id.tvCustomerName);
        tvCustomerAddress = findViewById(R.id.tvCustomerAddress);

        String confirmOrderId = getIntent().getStringExtra("orderId");
        loadOrderDetails(confirmOrderId);

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        View boxPhoto = findViewById(R.id.boxPhoto);
        ivPhotoResult = findViewById(R.id.ivPhotoResult); // Need to add this to XML
        if (boxPhoto != null) {
            boxPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(ConfirmDeliveryActivity.this, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        androidx.core.app.ActivityCompat.requestPermissions(ConfirmDeliveryActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                }
            });
        }

        SignatureView signatureView = findViewById(R.id.boxSignature);
        android.widget.TextView tvClearSignature = findViewById(R.id.tvClearSignature);
        if (tvClearSignature != null && signatureView != null) {
            tvClearSignature.setOnClickListener(v -> signatureView.clear());
        }

        Button btnConfirmDelivered = findViewById(R.id.btnConfirmDelivered);
        if (btnConfirmDelivered != null) {
            btnConfirmDelivered.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnConfirmDelivered.setEnabled(false);
                    btnConfirmDelivered.setText("Confirming...");

                    String deliveryOrderId = getIntent().getStringExtra("orderId");
                    com.google.firebase.auth.FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
                    String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

                    if (uid != null) {
                        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

                        // Clear rider's activeOrderId (separate from the order-completion transaction)
                        java.util.Map<String, Object> riderUpdates = new java.util.HashMap<>();
                        riderUpdates.put("activeOrderId", null);
                        com.google.firebase.firestore.WriteBatch riderBatch = db.batch();
                        riderBatch.update(db.collection("riders").document(uid), riderUpdates);
                        riderBatch.update(db.collection("users").document(uid), riderUpdates);

                        if (deliveryOrderId == null || deliveryOrderId.isEmpty()) {
                            riderBatch.commit().addOnCompleteListener(task -> goToRiderDashboard());
                            return;
                        }

                        // ── Mark order COMPLETED + deduct medicine stock ──
                        // (This is the only place in the app that finalizes an order,
                        // so Sales Reports / revenue / top-sellers depend on this running.)
                        db.collection("orders").document(deliveryOrderId).get()
                                .addOnSuccessListener(orderDoc -> {
                                    java.util.List<java.util.Map<String, Object>> items =
                                            (java.util.List<java.util.Map<String, Object>>) orderDoc.get("items");

                                    java.util.Map<String, Long> deductions = new java.util.HashMap<>();
                                    if (items != null) {
                                        for (java.util.Map<String, Object> item : items) {
                                            Object medIdObj = item.get("medicineId");
                                            Object qtyObj   = item.get("quantity");
                                            if (medIdObj == null) continue;
                                            long qty = qtyObj instanceof Number ? ((Number) qtyObj).longValue() : 1L;
                                            deductions.merge(medIdObj.toString(), qty, Long::sum);
                                        }
                                    }

                                    db.runTransaction(transaction -> {
                                        java.util.Map<String, com.google.firebase.firestore.DocumentSnapshot> medDocs = new java.util.HashMap<>();
                                        for (String medId : deductions.keySet()) {
                                            medDocs.put(medId, transaction.get(db.collection("medicines").document(medId)));
                                        }
                                        for (java.util.Map.Entry<String, Long> entry : deductions.entrySet()) {
                                            com.google.firebase.firestore.DocumentSnapshot snap = medDocs.get(entry.getKey());
                                            if (snap == null || !snap.exists()) continue;
                                            Long currentStock = snap.getLong("stock");
                                            if (currentStock == null) currentStock = 0L;
                                            long newStock = Math.max(0, currentStock - entry.getValue());
                                            transaction.update(db.collection("medicines").document(entry.getKey()),
                                                    "stock", newStock,
                                                    "updatedAt", System.currentTimeMillis());
                                        }

                                        long now = System.currentTimeMillis();
                                        java.util.Map<String, Object> orderUpdates = new java.util.HashMap<>();
                                        orderUpdates.put("status", "delivered");
                                        orderUpdates.put("deliveredAt", now);
                                        orderUpdates.put("completedAt", now);
                                        transaction.update(db.collection("orders").document(deliveryOrderId), orderUpdates);
                                        return null;
                                    }).addOnCompleteListener(task ->
                                            riderBatch.commit().addOnCompleteListener(t -> {
                                                android.widget.Toast.makeText(ConfirmDeliveryActivity.this, "✅ Delivery confirmed!", android.widget.Toast.LENGTH_SHORT).show();
                                                goToRiderDashboard();
                                            }));
                                })
                                .addOnFailureListener(e -> {
                                    android.widget.Toast.makeText(ConfirmDeliveryActivity.this, "Failed: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                                    btnConfirmDelivered.setEnabled(true);
                                    btnConfirmDelivered.setText("Confirm Delivered");
                                });
                    } else {
                        goToRiderDashboard();
                    }
                }
            });
        }
    }

    private void goToRiderDashboard() {
        Intent intent = new Intent(ConfirmDeliveryActivity.this, RiderDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadOrderDetails(String oId) {
        if (oId == null || oId.isEmpty()) return;
        com.google.firebase.firestore.FirebaseFirestore db =
                com.google.firebase.firestore.FirebaseFirestore.getInstance();

        db.collection("orders").document(oId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            String customerId = doc.getString("customerId");
            String rawAddress = doc.getString("deliveryAddress");
            if (rawAddress == null) rawAddress = doc.getString("address");
            final String deliveryAddress = rawAddress;

            if (deliveryAddress != null && !deliveryAddress.isEmpty() && tvCustomerAddress != null) {
                tvCustomerAddress.setText(deliveryAddress);
            }

            if (customerId != null) {
                db.collection("users").document(customerId).get().addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) return;
                    String name = userDoc.getString("name");
                    String userAddr = userDoc.getString("address");
                    if (name != null && tvCustomerName != null) {
                        tvCustomerName.setText(name);
                    }
                    // Fall back to the customer's saved profile address if the order has none
                    if ((deliveryAddress == null || deliveryAddress.isEmpty())
                            && userAddr != null && tvCustomerAddress != null) {
                        tvCustomerAddress.setText(userAddr);
                    }
                });
            }
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            android.widget.Toast.makeText(ConfirmDeliveryActivity.this, "Cannot open camera: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                android.widget.Toast.makeText(this, "Camera permission is required to take photo", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                    if (ivPhotoResult != null && imageBitmap != null) {
                        ivPhotoResult.setImageBitmap(imageBitmap);
                        ivPhotoResult.setVisibility(View.VISIBLE);
                        View iconCamera = findViewById(R.id.iconCamera);
                        View tvTakePhoto = findViewById(R.id.tvTakePhoto);
                        if (iconCamera != null) iconCamera.setVisibility(View.GONE);
                        if (tvTakePhoto != null) tvTakePhoto.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}