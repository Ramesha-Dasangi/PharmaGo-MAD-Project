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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AssignmentDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String orderId;

    private TextView tvOrderTitle, tvCustomerName, tvCustomerAddress, tvDropoffName, tvDropoffAddress;
    private LinearLayout stopsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_details);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        tvOrderTitle       = findViewById(R.id.tvOrderTitle);
        tvCustomerName     = findViewById(R.id.tvCustomerName);
        tvCustomerAddress  = findViewById(R.id.tvCustomerAddress);
        tvDropoffName      = findViewById(R.id.tvDropoffName);
        tvDropoffAddress   = findViewById(R.id.tvDropoffAddress);
        stopsContainer     = findViewById(R.id.stopsContainer);

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
        tvOrderTitle.setText("Order #" + oId.toUpperCase());

        db.collection("orders").document(oId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String customerId = doc.getString("customerId");
                String rawAddress = doc.getString("deliveryAddress");
                if (rawAddress == null) rawAddress = doc.getString("address");
                final String deliveryAddress = rawAddress;

                if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
                    tvCustomerAddress.setText(deliveryAddress);
                    if (tvDropoffAddress != null) tvDropoffAddress.setText("📍 " + deliveryAddress);
                }

                if (customerId != null) {
                    db.collection("users").document(customerId).get().addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String name = userDoc.getString("name");
                            String userAddr = userDoc.getString("address");
                            if (name != null) {
                                tvCustomerName.setText(name);
                                tvDropoffName.setText(name + "'s location");
                            }
                            // Use user's stored address as fallback if order has none
                            if ((deliveryAddress == null || deliveryAddress.isEmpty()) && userAddr != null) {
                                tvCustomerAddress.setText(userAddr);
                                if (tvDropoffAddress != null) tvDropoffAddress.setText("📍 " + userAddr);
                            }
                        }
                    });
                }

                List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                final List<?> confirmedPharmacies = (List<?>) doc.get("confirmedPharmacies");

                if (items != null && !items.isEmpty()) {
                    // Group items by pharmacy — only include CONFIRMED pharmacies
                    Map<String, StringBuilder> pharmacyItemsMap = new LinkedHashMap<>();
                    for (Map<String, Object> item : items) {
                        String pId = (String) item.get("pharmacyId");
                        if (pId == null) continue; // skip items without pharmacy

                        // If confirmedPharmacies exists, only show confirmed ones
                        if (confirmedPharmacies != null && !confirmedPharmacies.isEmpty()
                                && !confirmedPharmacies.contains(pId)) {
                            continue; // skip rejected/unanswered pharmacies
                        }

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

                        // Expand by default so address is immediately visible
                        if (layoutStopDetails != null) {
                            layoutStopDetails.setVisibility(View.VISIBLE);
                        }
                        if (btnExpandStop != null && layoutStopDetails != null) {
                            btnExpandStop.setOnClickListener(v -> {
                                boolean isVisible = layoutStopDetails.getVisibility() == View.VISIBLE;
                                layoutStopDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                            });
                        }

                        String inlinePharmName = null;
                        String inlinePharmAddr = null;
                        if (items != null) {
                            for (Map<String, Object> item : items) {
                                String itemPid = (String) item.get("pharmacyId");
                                if (pId.equals(itemPid)) {
                                    if (item.get("pharmacyName") != null) inlinePharmName = item.get("pharmacyName").toString();
                                    if (item.get("pharmacyAddress") != null) inlinePharmAddr = item.get("pharmacyAddress").toString();
                                }
                            }
                        }

                        if (inlinePharmName != null && tvStopName != null) tvStopName.setText(inlinePharmName);
                        if (inlinePharmAddr != null && tvStopAddress != null) tvStopAddress.setText("📍 " + inlinePharmAddr);

                        if (!"unknown".equals(pId)) {
                            final String fallbackName = inlinePharmName;
                            final String fallbackAddr = inlinePharmAddr;

                            // 1. Try pharmacies collection by doc ID first
                            db.collection("pharmacies").document(pId).get().addOnSuccessListener(pharDoc -> {
                                if (pharDoc.exists()) {
                                    String phName = pharDoc.getString("pharmacyName") != null ? pharDoc.getString("pharmacyName") : pharDoc.getString("name");
                                    String phAddr = pharDoc.getString("address") != null ? pharDoc.getString("address") : (pharDoc.getString("pharmacyAddress") != null ? pharDoc.getString("pharmacyAddress") : pharDoc.getString("locationAddress"));
                                    String phPhone = pharDoc.getString("phone") != null ? pharDoc.getString("phone") : pharDoc.getString("phoneNumber");

                                    if (phName != null && !phName.isEmpty() && tvStopName != null) tvStopName.setText(phName);
                                    if (phAddr != null && !phAddr.isEmpty() && tvStopAddress != null) tvStopAddress.setText("📍 " + phAddr);
                                    if (phPhone != null && !phPhone.isEmpty() && tvStopPhone != null) tvStopPhone.setText("📞 " + phPhone);
                                } else {
                                    // 2. Try querying pharmacies collection where ownerId == pId
                                    db.collection("pharmacies").whereEqualTo("ownerId", pId).limit(1).get().addOnSuccessListener(querySnap -> {
                                        if (querySnap != null && !querySnap.isEmpty()) {
                                            com.google.firebase.firestore.DocumentSnapshot doc2 = querySnap.getDocuments().get(0);
                                            String phName = doc2.getString("pharmacyName") != null ? doc2.getString("pharmacyName") : doc2.getString("name");
                                            String phAddr = doc2.getString("address") != null ? doc2.getString("address") : (doc2.getString("pharmacyAddress") != null ? doc2.getString("pharmacyAddress") : doc2.getString("locationAddress"));
                                            String phPhone = doc2.getString("phone") != null ? doc2.getString("phone") : doc2.getString("phoneNumber");

                                            if (phName != null && !phName.isEmpty() && tvStopName != null) tvStopName.setText(phName);
                                            if (phAddr != null && !phAddr.isEmpty() && tvStopAddress != null) tvStopAddress.setText("📍 " + phAddr);
                                            if (phPhone != null && !phPhone.isEmpty() && tvStopPhone != null) tvStopPhone.setText("📞 " + phPhone);
                                        } else {
                                            // 3. Fallback to users collection — check pharmacyName specifically
                                            db.collection("users").document(pId).get().addOnSuccessListener(pDoc -> {
                                                if (pDoc.exists()) {
                                                    String phName = pDoc.getString("pharmacyName");
                                                    String phAddr = pDoc.getString("pharmacyAddress") != null ? pDoc.getString("pharmacyAddress") : pDoc.getString("address");
                                                    String phPhone = pDoc.getString("phone") != null ? pDoc.getString("phone") : pDoc.getString("phoneNumber");

                                                    if (phName != null && !phName.isEmpty() && tvStopName != null) tvStopName.setText(phName);
                                                    if (phAddr != null && !phAddr.isEmpty() && tvStopAddress != null) tvStopAddress.setText("📍 " + phAddr);
                                                    if (phPhone != null && !phPhone.isEmpty() && tvStopPhone != null) tvStopPhone.setText("📞 " + phPhone);
                                                } else if (fallbackAddr == null && tvStopAddress != null) {
                                                    tvStopAddress.setText("📍 Address not available");
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        } else {
                            if (tvStopName    != null) tvStopName.setText("Pharmacy");
                            if (tvStopAddress != null) tvStopAddress.setText("📍 Address not available");
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