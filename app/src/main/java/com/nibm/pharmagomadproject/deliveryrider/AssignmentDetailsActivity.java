package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.List;
import java.util.Map;

public class AssignmentDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String orderId;

    private TextView tvOrderTitle, tvCustomerName, tvCustomerAddress;
    private TextView tvStop1Name, tvStop1Items, tvStop2Name, tvStop2Items;
    private TextView tvStop1Address, tvStop1Phone, tvStop2Address, tvStop2Phone;
    private ConstraintLayout cardStop1, cardStop2;
    private LinearLayout layoutStop1Details, layoutStop2Details;
    private ImageView btnExpandStop1, btnExpandStop2;
    private TextView tvDropoffName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_details);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        tvOrderTitle = findViewById(R.id.tvOrderTitle);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerAddress = findViewById(R.id.tvCustomerAddress);
        tvStop1Name = findViewById(R.id.tvStop1Name);
        tvStop1Items = findViewById(R.id.tvStop1Items);
        tvStop2Name = findViewById(R.id.tvStop2Name);
        tvStop2Items = findViewById(R.id.tvStop2Items);
        cardStop1 = findViewById(R.id.cardStop1);
        cardStop2 = findViewById(R.id.cardStop2);
        tvDropoffName = findViewById(R.id.tvDropoffName);
        
        layoutStop1Details = findViewById(R.id.layoutStop1Details);
        layoutStop2Details = findViewById(R.id.layoutStop2Details);
        tvStop1Address = findViewById(R.id.tvStop1Address);
        tvStop1Phone = findViewById(R.id.tvStop1Phone);
        tvStop2Address = findViewById(R.id.tvStop2Address);
        tvStop2Phone = findViewById(R.id.tvStop2Phone);
        btnExpandStop1 = findViewById(R.id.btnExpandStop1);
        btnExpandStop2 = findViewById(R.id.btnExpandStop2);

        if (btnExpandStop1 != null) {
            btnExpandStop1.setOnClickListener(v -> toggleVisibility(layoutStop1Details, btnExpandStop1));
        }
        if (btnExpandStop2 != null) {
            btnExpandStop2.setOnClickListener(v -> toggleVisibility(layoutStop2Details, btnExpandStop2));
        }

        orderId = getIntent().getStringExtra("orderId");

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        Button btnStartNavigation = findViewById(R.id.btnStartNavigation);
        if (btnStartNavigation != null) {
            btnStartNavigation.setText("Confirm Assignment");
            btnStartNavigation.setOnClickListener(v -> {
                if (orderId != null) {
                    btnStartNavigation.setEnabled(false);
                    btnStartNavigation.setText("Confirming...");
                    // Update order status to picked_up (rider accepted the assignment)
                    db.collection("orders").document(orderId)
                            .update("status", "picked_up")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Assignment confirmed!", Toast.LENGTH_SHORT).show();
                                // Go back to dashboard — order will show in "In Progress"
                                Intent intent = new Intent(AssignmentDetailsActivity.this, RiderDashboardActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnStartNavigation.setEnabled(true);
                                btnStartNavigation.setText("Confirm Assignment");
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "No order ID", Toast.LENGTH_SHORT).show();
                }
            });
        }

        com.google.android.material.button.MaterialButton btnCancelAssignment = findViewById(R.id.btnCancelAssignment);
        if (btnCancelAssignment != null && orderId != null) {
            btnCancelAssignment.setOnClickListener(v ->
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Cancel Assignment?")
                        .setMessage("Order ID: " + orderId + "\n\nAre you sure you want to cancel?")
                        .setPositiveButton("Yes, cancel", (dialog, which) -> {
                            String riderId = FirebaseAuth.getInstance().getCurrentUser() != null
                                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                            // Update order status to cancelled — wait for completion before finishing
                            db.collection("orders").document(orderId)
                                    .update("status", "cancelled")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Cancelled: " + orderId, Toast.LENGTH_LONG).show();

                                        // Now clear rider activeOrderId
                                        if (riderId != null) {
                                            db.collection("riders").document(riderId)
                                                    .update("activeOrderId", FieldValue.delete());
                                        }

                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "FAILED for " + orderId + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                        .setNegativeButton("Keep assignment", null)
                        .show()
            );
        }

        View navHome = findViewById(R.id.navHome);
        if (navHome != null) navHome.setOnClickListener(v -> {
            Intent i = new Intent(this, RiderDashboardActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });
        View navMap = findViewById(R.id.navMap);
        if (navMap != null) navMap.setOnClickListener(v -> startActivity(new Intent(this, LiveMapActivity.class)));

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) navHistory.setOnClickListener(v -> startActivity(new Intent(this, DeliveryHistoryActivity.class)));

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) navProfile.setOnClickListener(v -> startActivity(new Intent(this, RiderProfileActivity.class)));

        if (orderId != null && !orderId.isEmpty()) {
            fetchOrderDetails(orderId);
        } else {
            Toast.makeText(this, "No order ID passed", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleVisibility(View layout, ImageView icon) {
        if (layout.getVisibility() == View.VISIBLE) {
            layout.setVisibility(View.GONE);
            icon.setImageResource(android.R.drawable.ic_media_play);
        } else {
            layout.setVisibility(View.VISIBLE);
            icon.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void fetchOrderDetails(String oId) {
        tvOrderTitle.setText("Order #" + oId.substring(0, Math.min(6, oId.length())).toUpperCase());
        
        db.collection("orders").document(oId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String customerId = doc.getString("customerId");
                String address = doc.getString("address");
                if (address != null) tvCustomerAddress.setText(address);

                if (customerId != null) {
                    db.collection("users").document(customerId).get().addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String name = userDoc.getString("name");
                            if (name != null) {
                                tvCustomerName.setText(name);
                                tvDropoffName.setText(name + "'s home");
                            }
                        }
                    });
                }

                List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                if (items != null && !items.isEmpty()) {
                    // Stop 1
                    Map<String, Object> item1 = items.get(0);
                    tvStop1Name.setText("Pharmacy"); 
                    tvStop1Items.setText(item1.get("medicineName") + " x" + item1.get("quantity"));
                    cardStop1.setVisibility(View.VISIBLE);
                    
                    String pId1 = (String) item1.get("pharmacyId");
                    if (pId1 != null && !pId1.trim().isEmpty()) {
                        db.collection("users").document(pId1).get().addOnSuccessListener(pDoc -> {
                            if (pDoc.exists()) {
                                if (pDoc.getString("name") != null) tvStop1Name.setText(pDoc.getString("name"));
                                if (tvStop1Address != null) tvStop1Address.setText("Address: " + (pDoc.getString("address") != null ? pDoc.getString("address") : "N/A"));
                                if (tvStop1Phone != null) tvStop1Phone.setText("Phone: " + (pDoc.getString("phone") != null ? pDoc.getString("phone") : "N/A"));
                            }
                        });
                    }

                    // Stop 2
                    if (items.size() > 1) {
                        Map<String, Object> item2 = items.get(1);
                        tvStop2Name.setText("Pharmacy");
                        tvStop2Items.setText(item2.get("medicineName") + " x" + item2.get("quantity"));
                        cardStop2.setVisibility(View.VISIBLE);
                        
                        String pId2 = (String) item2.get("pharmacyId");
                        if (pId2 != null && !pId2.trim().isEmpty()) {
                            db.collection("users").document(pId2).get().addOnSuccessListener(pDoc -> {
                                if (pDoc.exists()) {
                                    if (pDoc.getString("name") != null) tvStop2Name.setText(pDoc.getString("name"));
                                    if (tvStop2Address != null) tvStop2Address.setText("Address: " + (pDoc.getString("address") != null ? pDoc.getString("address") : "N/A"));
                                    if (tvStop2Phone != null) tvStop2Phone.setText("Phone: " + (pDoc.getString("phone") != null ? pDoc.getString("phone") : "N/A"));
                                }
                            });
                        }
                    } else {
                        cardStop2.setVisibility(View.GONE);
                    }
                }
            } else {
                Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
