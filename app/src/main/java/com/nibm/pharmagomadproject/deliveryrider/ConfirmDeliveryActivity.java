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
                    Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                if (ivPhotoResult != null) {
                    ivPhotoResult.setImageBitmap(imageBitmap);
                    ivPhotoResult.setVisibility(View.VISIBLE);
                    findViewById(R.id.iconCamera).setVisibility(View.GONE);
                    findViewById(R.id.tvTakePhoto).setVisibility(View.GONE);
                }
            }
        }
    }
}
