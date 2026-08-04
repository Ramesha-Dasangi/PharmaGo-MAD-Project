package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;


public class ConfirmDeliveryActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ImageView ivPhotoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_delivery);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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
                        
                        com.google.firebase.firestore.WriteBatch batch = db.batch();
                        
                        // Update order status to delivered
                        if (deliveryOrderId != null && !deliveryOrderId.isEmpty()) {
                            java.util.Map<String, Object> orderUpdates = new java.util.HashMap<>();
                            orderUpdates.put("status", "delivered");
                            orderUpdates.put("deliveredAt", System.currentTimeMillis());
                            batch.update(db.collection("orders").document(deliveryOrderId), orderUpdates);
                        }
                        
                        // Clear rider's activeOrderId
                        java.util.Map<String, Object> riderUpdates = new java.util.HashMap<>();
                        riderUpdates.put("activeOrderId", null);
                        batch.update(db.collection("riders").document(uid), riderUpdates);
                        batch.update(db.collection("users").document(uid), riderUpdates);
                        
                        batch.commit().addOnCompleteListener(task -> {
                            android.widget.Toast.makeText(ConfirmDeliveryActivity.this, "✅ Delivery confirmed!", android.widget.Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ConfirmDeliveryActivity.this, RiderDashboardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        Intent intent = new Intent(ConfirmDeliveryActivity.this, RiderDashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
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
