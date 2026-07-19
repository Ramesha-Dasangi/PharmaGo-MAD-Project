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

    private TextView tvOrderTitle, tvCustomerInfo, tvStep2Title, tvStep3Title;
    private ImageView iconStep1, iconStep2, iconStep3, iconStep4, iconStep5;
    private TextView tvStep1Title, tvStep1Time, tvStep2Time, tvStep3Time, tvStep4Title, tvStep5Title;

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
        tvStep5Title = findViewById(R.id.tvStep5Title);

        orderId = getIntent().getStringExtra("orderId");

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        Button btnMarkComplete = findViewById(R.id.btnMarkComplete);
        if (btnMarkComplete != null) {
            btnMarkComplete.setOnClickListener(v -> {
                Intent intent = new Intent(DeliveryProgressActivity.this, ConfirmDeliveryActivity.class);
                if (orderId != null) {
                    intent.putExtra("orderId", orderId);
                }
                startActivity(intent);
            });
        }

        if (orderId != null && !orderId.isEmpty()) {
            fetchOrderDetails(orderId);
        } else {
            Toast.makeText(this, "No order ID passed", Toast.LENGTH_SHORT).show();
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

        // Update based on status
        if (status.equalsIgnoreCase("assigned")) {
            // Step 1 done, Step 2 active
            iconStep2.setImageResource(android.R.drawable.ic_menu_view);
            iconStep2.setColorFilter(colorOrange);
            tvStep2Title.setTextColor(colorOrange);
            tvStep2Time.setText("In progress...");
            tvStep3Time.setText("Pending");
        } 
        else if (status.equalsIgnoreCase("picked_up")) {
            // Step 2 done, Step 3 active
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
            // Step 3 done, Step 4 active
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
        }
    }

    private void fetchOrderDetails(String oId) {
        if (tvOrderTitle != null) {
            tvOrderTitle.setText("Order #" + oId.substring(0, Math.min(6, oId.length())).toUpperCase());
        }
        
        db.collection("orders").document(oId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String customerId = doc.getString("customerId");
                String address = doc.getString("address");
                String status = doc.getString("status");

                if (customerId != null) {
                    db.collection("users").document(customerId).get().addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String name = userDoc.getString("name");
                            if (name != null && tvCustomerInfo != null) {
                                tvCustomerInfo.setText(name + (address != null ? " · " + address : ""));
                            }
                        }
                    });
                }

                List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                if (items != null && !items.isEmpty()) {
                    Map<String, Object> item1 = items.get(0);
                    String pId1 = (String) item1.get("pharmacyId");
                    if (pId1 != null) {
                        db.collection("users").document(pId1).get().addOnSuccessListener(pDoc -> {
                            if (pDoc.exists() && pDoc.getString("name") != null) {
                                String pName = pDoc.getString("name");
                                if (tvStep2Title != null) tvStep2Title.setText("Picked up — " + pName);
                                if (tvStep3Title != null) tvStep3Title.setText("Heading to " + pName);
                                
                                // Update timeline colors after setting text
                                updateTimelineUI(status);
                            }
                        });
                    } else {
                        updateTimelineUI(status);
                    }
                } else {
                    updateTimelineUI(status);
                }
            } else {
                Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
