package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String orderId;

    private TextView tvOrderTitle, tvCustomerName, tvCustomerAddress, tvDropoffName;
    private LinearLayout stopsContainer;

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
        tvDropoffName = findViewById(R.id.tvDropoffName);
        stopsContainer = findViewById(R.id.stopsContainer);

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
                    db.collection("orders").document(orderId)
                            .update("status", "picked_up")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Assignment confirmed!", Toast.LENGTH_SHORT).show();
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

                                db.collection("orders").document(orderId)
                                        .update("status", "cancelled")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Cancelled: " + orderId, Toast.LENGTH_LONG).show();

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

        if (orderId != null && !orderId.isEmpty()) {
            fetchOrderDetails(orderId);
        } else {
            Toast.makeText(this, "No order ID passed", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOrderDetails(String oId) {
        tvOrderTitle.setText("Order #" + oId.substring(0, Math.min(6, oId.length())).toUpperCase());

        db.collection("orders").document(oId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String customerId = doc.getString("customerId");
                String address = doc.getString("deliveryAddress");
                if (address == null) address = doc.getString("address");

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
                    // Group items by pharmacy
                    Map<String, StringBuilder> pharmacyItemsMap = new HashMap<>();
                    for (Map<String, Object> item : items) {
                        String pId = (String) item.get("pharmacyId");
                        if (pId == null) pId = "unknown";

                        String medicineName = item.get("medicineName") != null ? item.get("medicineName").toString() : "Medicine";
                        String qty = item.get("quantity") != null ? item.get("quantity").toString() : "1";
                        String itemStr = medicineName + " x" + qty;

                        if (!pharmacyItemsMap.containsKey(pId)) {
                            pharmacyItemsMap.put(pId, new StringBuilder(itemStr));
                        } else {
                            pharmacyItemsMap.get(pId).append("\n").append(itemStr);
                        }
                    }

                    stopsContainer.removeAllViews();
                    int stopNum = 1;

                    for (Map.Entry<String, StringBuilder> entry : pharmacyItemsMap.entrySet()) {
                        String pId = entry.getKey();
                        String itemsListStr = entry.getValue().toString();

                        View stopView = LayoutInflater.from(this).inflate(R.layout.item_assignment_stop, stopsContainer, false);
                        TextView tvStopNum = stopView.findViewById(R.id.tvStopNum);
                        TextView tvStopName = stopView.findViewById(R.id.tvStopName);
                        TextView tvStopItems = stopView.findViewById(R.id.tvStopItems);
                        TextView tvStopAddress = stopView.findViewById(R.id.tvStopAddress);
                        TextView tvStopPhone = stopView.findViewById(R.id.tvStopPhone);
                        ImageView btnExpandStop = stopView.findViewById(R.id.btnExpandStop);
                        View layoutStopDetails = stopView.findViewById(R.id.layoutStopDetails);

                        if (tvStopNum != null) tvStopNum.setText(String.valueOf(stopNum));
                        if (tvStopItems != null) tvStopItems.setText(itemsListStr);

                        if (btnExpandStop != null && layoutStopDetails != null) {
                            btnExpandStop.setOnClickListener(v -> {
                                boolean isVisible = layoutStopDetails.getVisibility() == View.VISIBLE;
                                layoutStopDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                            });
                        }

                        if (!"unknown".equals(pId)) {
                            db.collection("users").document(pId).get().addOnSuccessListener(pDoc -> {
                                if (pDoc.exists()) {
                                    String name = pDoc.getString("name");
                                    String pAddress = pDoc.getString("address");
                                    String phone = pDoc.getString("phone");

                                    if (tvStopName != null && name != null) tvStopName.setText(name);
                                    if (tvStopAddress != null) tvStopAddress.setText("Address: " + (pAddress != null ? pAddress : "N/A"));
                                    if (tvStopPhone != null) tvStopPhone.setText("Phone: " + (phone != null ? phone : "N/A"));
                                }
                            });
                        } else {
                            if (tvStopName != null) tvStopName.setText("Pharmacy");
                        }

                        stopsContainer.addView(stopView);
                        stopNum++;
                    }

                    TextView tvStopsCount = findViewById(R.id.tvStopsCount);
                    if (tvStopsCount != null) {
                        tvStopsCount.setText(pharmacyItemsMap.size() + " stops");
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