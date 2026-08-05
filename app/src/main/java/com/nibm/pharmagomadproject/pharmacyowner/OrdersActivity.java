package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;

import com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity;
import com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigation;

    private Button btnNew, btnProcessing, btnCompleted, btnRejected;

    private ArrayList<OrderModel> allOrders;
    private ArrayList<OrderModel> orderList;

    private OrderAdapter adapter;

    private com.google.firebase.firestore.FirebaseFirestore db;
    private com.google.firebase.auth.FirebaseAuth mAuth;
    private String activeFilterStatus = "New";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // INIT VIEWS
        recyclerView = findViewById(R.id.recyclerOrders);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnNew = findViewById(R.id.btnNew);
        btnProcessing = findViewById(R.id.btnProcessing);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnRejected = findViewById(R.id.btnRejected);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // LISTS
        allOrders = new ArrayList<>();
        orderList = new ArrayList<>();

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

        // ADAPTER
        adapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(adapter);

        // Removed loadOrdersFromFirestore from onCreate, now called in onResume

        highlightButton(btnNew);

        // FILTER BUTTONS
        btnNew.setOnClickListener(v -> {
            activeFilterStatus = "New";
            showOrders("New");
            highlightButton(btnNew);
        });

        btnProcessing.setOnClickListener(v -> {
            activeFilterStatus = "Processing";
            showOrders("Processing");
            highlightButton(btnProcessing);
        });

        btnCompleted.setOnClickListener(v -> {
            activeFilterStatus = "Completed";
            showOrders("Completed");
            highlightButton(btnCompleted);
        });

        if (btnRejected != null) {
            btnRejected.setOnClickListener(v -> {
                activeFilterStatus = "Rejected";
                showOrders("Rejected");
                highlightButton(btnRejected);
            });
        }

        // BOTTOM NAV
        bottomNavigation.setSelectedItemId(R.id.nav_orders);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_orders) {
                return true;

            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(this, SalesReportActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrdersFromFirestore();
    }

    // FILTER ORDERS
    private void showOrders(String status) {

        orderList.clear();

        for (OrderModel order : allOrders) {
            if (order.getStatus().equalsIgnoreCase(status)) {
                orderList.add(order);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // COUNT BUTTONS
    private void updateCounts() {

        int newCount = 0;
        int processingCount = 0;
        int completedCount = 0;
        int rejectedCount = 0;

        for (OrderModel order : allOrders) {

            switch (order.getStatus()) {
                case "New":
                    newCount++;
                    break;

                case "Processing":
                    processingCount++;
                    break;

                case "Completed":
                    completedCount++;
                    break;

                case "Rejected":
                    rejectedCount++;
                    break;
            }
        }

        btnNew.setText("New (" + newCount + ")");
        btnProcessing.setText("Processing (" + processingCount + ")");
        btnCompleted.setText("Completed (" + completedCount + ")");
        if (btnRejected != null) btnRejected.setText("Rejected (" + rejectedCount + ")");
    }

    // BUTTON HIGHLIGHT
    private void highlightButton(Button selectedButton) {

        Button[] buttons = {btnNew, btnProcessing, btnCompleted, btnRejected};

        for (Button b : buttons) {
            if (b == null) continue;
            b.setBackgroundTintList(getColorStateList(R.color.light));
            b.setTextColor(getColor(R.color.green));
        }

        selectedButton.setBackgroundTintList(getColorStateList(R.color.green));
        selectedButton.setTextColor(Color.WHITE);
    }

    private void loadOrdersFromFirestore() {
        String ownerId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        if (ownerId.isEmpty()) return;

        // First, load customer names
        db.collection("users").get().addOnSuccessListener(userSnaps -> {
            java.util.Map<String, String> userNameById = new java.util.HashMap<>();
            for (com.google.firebase.firestore.DocumentSnapshot doc : userSnaps) {
                userNameById.put(doc.getId(), doc.getString("name"));
            }

            // Then, load orders
            db.collection("orders").get().addOnSuccessListener(orderSnaps -> {
                allOrders.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : orderSnaps) {
                    java.util.List<java.util.Map<String, Object>> items = (java.util.List<java.util.Map<String, Object>>) doc.get("items");
                    boolean belongsToMe = false;
                    boolean hasRxForMe = false;
                    if (items != null) {
                        for (java.util.Map<String, Object> item : items) {
                            String itemPharmacyId = (String) item.get("pharmacyId");
                            if (ownerId.equals(itemPharmacyId)) {
                                belongsToMe = true;
                                String typeStr = (String) item.get("type");
                                if ("Prescription".equalsIgnoreCase(typeStr) || "Rx".equalsIgnoreCase(typeStr)) {
                                    hasRxForMe = true;
                                }
                            }
                        }
                    }
                    if (belongsToMe) {
                        String orderId = doc.getId();
                        String customerId = doc.getString("customerId");
                        String customerName = userNameById.get(customerId);
                        if (customerName == null) customerName = "Customer";

                        // Build items description for this pharmacy
                        StringBuilder itemsDesc = new StringBuilder();
                        if (items != null) {
                            for (java.util.Map<String, Object> item : items) {
                                String itemPharmId = (String) item.get("pharmacyId");
                                if (ownerId.equals(itemPharmId)) {
                                    if (itemsDesc.length() > 0) itemsDesc.append(", ");
                                    itemsDesc.append(item.get("medicineName"))
                                            .append(" x")
                                            .append(item.get("quantity"));
                                }
                            }
                        }

                        // Status mapping per-pharmacy
                        String fsStatus = doc.getString("status");
                        if (fsStatus == null) fsStatus = "pending";

                        java.util.List<?> confirmedPharmacies = (java.util.List<?>) doc.get("confirmedPharmacies");
                        java.util.List<?> rejectedPharmacies  = (java.util.List<?>) doc.get("rejectedPharmacies");
                        boolean myPharmacyConfirmed = confirmedPharmacies != null && confirmedPharmacies.contains(ownerId);
                        boolean myPharmacyRejected  = rejectedPharmacies  != null && rejectedPharmacies.contains(ownerId);

                        String type = hasRxForMe ? "RX Required" : "OTC";

                        String displayStatus = "New";
                        String stLower = fsStatus.toLowerCase();

                        if (myPharmacyRejected) {
                            // This pharmacy rejected the order
                            displayStatus = "Rejected";
                        } else if ("completed".equals(stLower) || "delivered".equals(stLower)
                                || "cancelled".equals(stLower)) {
                            displayStatus = "Completed";
                        } else if ("rejected".equals(stLower) || "partially_rejected".equals(stLower)) {
                            // Global rejection (not specifically by this pharmacy, or all rejected)
                            displayStatus = "Rejected";
                        } else if (myPharmacyConfirmed || !hasRxForMe
                                || "processing".equals(stLower) || "partially_approved".equals(stLower)
                                || "picked_up".equals(stLower) || "out_for_delivery".equals(stLower)) {
                            // OTC item or responded/processing -> Processing
                            displayStatus = "Processing";
                        } else {
                            // Prescription required & awaiting response -> New
                            displayStatus = "New";
                        }

                        double total = 0;
                        Object totalObj = doc.get("total");
                        if (totalObj instanceof Number) {
                            total = ((Number) totalObj).doubleValue();
                        }
                        long createdAt = doc.getLong("createdAt") != null ? doc.getLong("createdAt") : 0;

                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
                        String timeStr = sdf.format(new java.util.Date(createdAt));

                        allOrders.add(new OrderModel(
                                orderId,
                                customerName,
                                customerId,
                                itemsDesc.toString(),
                                timeStr,
                                "Rs. " + (int)total,
                                type,
                                displayStatus
                        ));
                    }
                }

                // Sort orders by orderId descending (proxy for newest first as ID contains timestamp)
                java.util.Collections.sort(allOrders, (a, b) -> b.getOrderId().compareTo(a.getOrderId()));

                showOrders(activeFilterStatus);
                updateCounts();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}