package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.List;
import java.util.Map;

public class DeliveryProgressActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String orderId;
    private String currentStatus = "picked_up";
    private Button btnMarkComplete;

    private TextView tvOrderTitle, tvCustomerInfo, tvStep2Title, tvStep3Title;
    private ImageView iconStep1, iconStep2, iconStep3, iconStep4, iconStep5;
    private TextView tvStep1Title, tvStep1Time, tvStep2Time, tvStep3Time, tvStep4Title, tvStep4Time, tvStep5Title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_progress);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        db = FirebaseFirestore.getInstance();

        tvOrderTitle = findViewById(R.id.tvOrderTitle);
        tvCustomerInfo = findViewById(R.id.tvCustomerInfo);
        tvStep2Title = findViewById(R.id.tvStep2Title);
        tvStep3Title = findViewById(R.id.tvStep3Title);
        
        iconStep1 = findViewById(R.id.iconStep1);
        iconStep2 = findViewById(R.id.iconStep2);
        iconStep3 = findViewById(R.id.iconStep3);
        iconStep4 = findViewById(R.id.iconStep4);
        iconStep5 = findViewById(R.id.iconStep5);
        
        tvStep1Title = findViewById(R.id.tvStep1Title);
        tvStep1Time = findViewById(R.id.tvStep1Time);
        tvStep2Time = findViewById(R.id.tvStep2Time);
        tvStep3Time = findViewById(R.id.tvStep3Time);
        tvStep4Title = findViewById(R.id.tvStep4Title);
        tvStep4Time = findViewById(R.id.tvStep4Time);
        tvStep5Title = findViewById(R.id.tvStep5Title);

        orderId = getIntent().getStringExtra("orderId");
        currentStatus = "picked_up"; // default

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        btnMarkComplete = findViewById(R.id.btnMarkComplete);
        setupButton();

        if (orderId != null && !orderId.isEmpty()) {
            fetchOrderDetails(orderId);
        } else {
            Toast.makeText(this, "No order ID passed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButton() {
        if (btnMarkComplete == null) return;
        
        if ("picked_up".equalsIgnoreCase(currentStatus)) {
            btnMarkComplete.setText("Confirm Pickup from Pharmacy");
            btnMarkComplete.setOnClickListener(v -> {
                if (orderId == null) return;
                btnMarkComplete.setEnabled(false);
                btnMarkComplete.setText("Updating...");
                db.collection("orders").document(orderId)
                        .update("status", "out_for_delivery")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Order marked as picked up", Toast.LENGTH_SHORT).show();
                            currentStatus = "out_for_delivery";
                            setupButton();
                            updateTimelineUI(currentStatus);
                            btnMarkComplete.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            btnMarkComplete.setEnabled(true);
                            btnMarkComplete.setText("Confirm Pickup from Pharmacy");
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        } else if ("out_for_delivery".equalsIgnoreCase(currentStatus)) {
            btnMarkComplete.setText("Mark as Delivered");
            btnMarkComplete.setOnClickListener(v -> {
                Intent intent = new Intent(DeliveryProgressActivity.this, ConfirmDeliveryActivity.class);
                if (orderId != null) {
                    intent.putExtra("orderId", orderId);
                }
                startActivity(intent);
            });
        } else {
            btnMarkComplete.setText("Mark stop as complete");
            btnMarkComplete.setOnClickListener(v -> {
                Intent intent = new Intent(DeliveryProgressActivity.this, ConfirmDeliveryActivity.class);
                if (orderId != null) {
                    intent.putExtra("orderId", orderId);
                }
                startActivity(intent);
            });
        }
    }

    private void updateTimelineUI(String status) {
        if (status == null) return;
        
        int colorGreen = getColor(R.color.green_accept);
        int colorOrange = getColor(R.color.primary_orange);
        int colorGray = getColor(R.color.divider_color);
        
        // Reset everything to pending
        iconStep2.setColorFilter(colorGray);
        tvStep2Title.setTextColor(colorGray);
        iconStep3.setColorFilter(colorGray);
        tvStep3Title.setTextColor(colorGray);
        iconStep4.setColorFilter(colorGray);
        tvStep4Title.setTextColor(colorGray);
        iconStep5.setColorFilter(colorGray);
        tvStep5Title.setTextColor(colorGray);
        
        iconStep2.setImageResource(android.R.drawable.ic_menu_view);
        iconStep3.setImageResource(android.R.drawable.ic_menu_myplaces);
        iconStep4.setImageResource(android.R.drawable.ic_menu_directions);
        iconStep5.setImageResource(android.R.drawable.ic_menu_view);

        // Set Default Subtitles
        tvStep1Time.setText("Done");
        tvStep2Time.setText("Pending");
        tvStep3Time.setText("Pending");
        tvStep4Time.setText("Pending");

        // Update based on status
        if (status.equalsIgnoreCase("assigned")) {
            iconStep2.setImageResource(android.R.drawable.ic_menu_view);
            iconStep2.setColorFilter(colorOrange);
            tvStep2Title.setTextColor(colorOrange);
            tvStep2Time.setText("In progress...");
        } 
        else if (status.equalsIgnoreCase("picked_up")) {
            // Rider accepted and is heading to pharmacy
            iconStep2.setImageResource(android.R.drawable.checkbox_on_background);
            iconStep2.setColorFilter(colorGreen);
            tvStep2Title.setTextColor(getColor(R.color.text_primary));
            
            iconStep3.setImageResource(android.R.drawable.ic_menu_myplaces);
            iconStep3.setColorFilter(colorOrange);
            tvStep3Title.setTextColor(colorOrange);
            
            tvStep2Time.setText("Done");
            tvStep3Time.setText("In progress...");
        } 
        else if (status.equalsIgnoreCase("out_for_delivery")) {
            // Picked up from pharmacy, heading to customer
            iconStep2.setImageResource(android.R.drawable.checkbox_on_background);
            iconStep2.setColorFilter(colorGreen);
            tvStep2Title.setTextColor(getColor(R.color.text_primary));
            
            iconStep3.setImageResource(android.R.drawable.checkbox_on_background);
            iconStep3.setColorFilter(colorGreen);
            tvStep3Title.setTextColor(getColor(R.color.text_primary));
            
            iconStep4.setImageResource(android.R.drawable.ic_menu_directions);
            iconStep4.setColorFilter(colorOrange);
            tvStep4Title.setTextColor(colorOrange);
            
            tvStep2Time.setText("Done");
            tvStep3Time.setText("Done");
            tvStep4Time.setText("In progress...");
        }
    }

    private void fetchOrderDetails(String oId) {
        if (tvOrderTitle != null) {
            // Show full order ID
            tvOrderTitle.setText("Order #" + oId.toUpperCase());
        }
        
        db.collection("orders").document(oId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String customerId = doc.getString("customerId");
                String address = doc.getString("deliveryAddress"); // Use deliveryAddress if available
                if (address == null) address = doc.getString("address");
                String status = doc.getString("status");

                if (customerId != null) {
                    final String finalAddress = address;
                    db.collection("users").document(customerId).get().addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String name = userDoc.getString("name");
                            if (name != null && tvCustomerInfo != null) {
                                tvCustomerInfo.setText(name + (finalAddress != null ? " · " + finalAddress : ""));
                            }
                        }
                    });
                }

                // Initial static titles
                if (tvStep1Title != null) tvStep1Title.setText("Assigned to you");
                if (tvStep2Title != null) tvStep2Title.setText("Assignment accepted");
                if (tvStep4Title != null) tvStep4Title.setText("Heading to Customer");
                if (tvStep5Title != null) tvStep5Title.setText("Delivered");

                // Update button based on current status
                if (status != null) {
                    currentStatus = status;
                    setupButton();
                }

                // Extract unique pharmacy names from items — list each participating pharmacy separately
                List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                java.util.List<String> pharmNamesList = new java.util.ArrayList<>();
                java.util.Set<String> seenPharmIds = new java.util.LinkedHashSet<>();
                if (items != null) {
                    java.util.List<?> confirmedList = (java.util.List<?>) doc.get("confirmedPharmacies");
                    for (Map<String, Object> item : items) {
                        if (item == null) continue;
                        String pId = (String) item.get("pharmacyId");
                        if (pId == null || seenPharmIds.contains(pId)) continue;
                        if (confirmedList != null && !confirmedList.isEmpty() && !confirmedList.contains(pId)) continue;
                        String pName = (String) item.get("pharmacyName");
                        if (pName != null && !pName.trim().isEmpty()) {
                            pharmNamesList.add(pName.trim());
                        } else {
                            // Fallback default label if name not in item
                            pharmNamesList.add("Pharmacy (" + pId.substring(0, Math.min(5, pId.length())) + ")");
                        }
                        seenPharmIds.add(pId);
                    }
                }
                if (pharmNamesList.isEmpty()) {
                    String pName = doc.getString("pharmacyName");
                    if (pName != null && !pName.trim().isEmpty()) pharmNamesList.add(pName.trim());
                }
                if (tvStep3Title != null) {
                    if (pharmNamesList.size() == 1) {
                        tvStep3Title.setText("Pickup from " + pharmNamesList.get(0));
                    } else if (pharmNamesList.size() > 1) {
                        StringBuilder sb = new StringBuilder("Pickup from:\n");
                        for (int i2 = 0; i2 < pharmNamesList.size(); i2++) {
                            if (i2 > 0) sb.append("\n");
                            sb.append("• ").append(pharmNamesList.get(i2));
                        }
                        tvStep3Title.setText(sb.toString());
                    } else {
                        tvStep3Title.setText("Heading to Pharmacy");
                    }
                }
                updateTimelineUI(status);
            } else {
                Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
