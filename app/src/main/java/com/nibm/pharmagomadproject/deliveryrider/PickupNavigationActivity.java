package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

public class PickupNavigationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_navigation);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("orderId");

        setupButtons();
    }

    private void setupButtons() {
        Button btnNavigate = findViewById(R.id.btnNavigateToPickup);
        if (btnNavigate != null) {
            btnNavigate.setText("Picked up — Head to customer");
            btnNavigate.setOnClickListener(v -> {
                if (orderId != null) {
                    btnNavigate.setEnabled(false);
                    btnNavigate.setText("Updating...");
                    // Update order status to out_for_delivery
                    db.collection("orders").document(orderId)
                            .update("status", "out_for_delivery")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Heading to customer!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PickupNavigationActivity.this, DeliveryProgressActivity.class);
                                intent.putExtra("orderId", orderId);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnNavigate.setEnabled(true);
                                btnNavigate.setText("Picked up — Head to customer");
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "No order ID", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent i = new Intent(this, RiderDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) {
            navHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, DeliveryHistoryActivity.class)));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, RiderProfileActivity.class)));
        }
        
        View navMap = findViewById(R.id.navMap);
        if (navMap != null) {
            navMap.setOnClickListener(v -> startActivity(new Intent(this, LiveMapActivity.class)));
        }
    }
}
