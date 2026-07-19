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

                    com.google.firebase.auth.FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
                    String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

                    if (uid != null) {
                        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
                        updates.put("activeOrderId", null);
                        // Also, theoretically, the order status should be updated to 'delivered' here, 
                        // but if that's handled elsewhere we just clear activeOrderId for the rider.
                        
                        com.google.firebase.firestore.WriteBatch batch = db.batch();
                        batch.update(db.collection("riders").document(uid), updates);
                        batch.update(db.collection("users").document(uid), updates);
                        
                        batch.commit().addOnCompleteListener(task -> {
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
