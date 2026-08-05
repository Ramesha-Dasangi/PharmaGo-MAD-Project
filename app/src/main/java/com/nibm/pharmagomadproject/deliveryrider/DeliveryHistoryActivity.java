package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private RecyclerView rvHistory;
    private TextView tvEmpty, tvEarnedAmount, tvDeliveriesCompleted;
    private TextView tvOrdersThisWeek, tvOrdersThisMonth;
    private RiderHistoryAdapter adapter;
    private final List<Map<String, String>> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_history);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvHistory = findViewById(R.id.rvHistory);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvEarnedAmount = findViewById(R.id.tvEarnedAmount);
        tvDeliveriesCompleted = findViewById(R.id.tvDeliveriesCompleted);
        tvOrdersThisWeek = findViewById(R.id.tvOrdersThisWeek);
        tvOrdersThisMonth = findViewById(R.id.tvOrdersThisMonth);

        adapter = new RiderHistoryAdapter(this, orderList);
        if (rvHistory != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            rvHistory.setAdapter(adapter);
        }

        setupBottomNav();
        loadOrders();
    }

    private void loadOrders() {
        if (mAuth.getCurrentUser() == null) return;
        String riderId = mAuth.getCurrentUser().getUid();

        // Query orders where this rider was assigned (riderId field matches)
        db.collection("orders")
                .whereEqualTo("riderId", riderId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    orderList.clear();
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();

                    if (docs.isEmpty()) {
                        // Fallback: show ALL orders with assigned/delivered/cancelled status
                        loadAllRelevantOrders();
                        return;
                    }

                    long todayEarned = 0;
                    int todayCompleted = 0;
                    int weekCompleted = 0;
                    int monthCompleted = 0;
                    
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    cal.set(java.util.Calendar.MINUTE, 0);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    long startOfDay = cal.getTimeInMillis();
                    
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -7);
                    long startOfWeek = cal.getTimeInMillis();
                    
                    cal = java.util.Calendar.getInstance();
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    cal.set(java.util.Calendar.MINUTE, 0);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    cal.add(java.util.Calendar.MONTH, -1);
                    long startOfMonth = cal.getTimeInMillis();

                    for (DocumentSnapshot doc : docs) {
                        Map<String, String> item = new HashMap<>();
                        item.put("orderId", doc.getId());
                        String status = doc.getString("status");
                        item.put("status", status != null ? status : "unknown");

                        String customerId = doc.getString("customerId");
                        item.put("customerId", customerId != null ? customerId : "");
                        item.put("customerName", "Loading...");
                        
                        Long deliveryFee = doc.getLong("deliveryFee");
                        long fee = deliveryFee != null ? deliveryFee : 100L;
                        item.put("earnings", "+ LKR " + fee);
                        
                        // Check if delivered today
                        if ("delivered".equalsIgnoreCase(status)) {
                            Long completedAt = doc.getLong("completedAt");
                            if (completedAt == null) {
                                completedAt = doc.getLong("createdAt");
                            }
                            if (completedAt != null) {
                                if (completedAt >= startOfDay) {
                                    todayEarned += fee;
                                    todayCompleted++;
                                }
                                if (completedAt >= startOfWeek) {
                                    weekCompleted++;
                                }
                                if (completedAt >= startOfMonth) {
                                    monthCompleted++;
                                }
                            }
                        }

                        orderList.add(item);

                        // Fetch real rating from reviews collection
                        db.collection("reviews").whereEqualTo("orderId", doc.getId()).get()
                                .addOnSuccessListener(rSnap -> {
                                    if (!rSnap.isEmpty()) {
                                        Double r = rSnap.getDocuments().get(0).getDouble("rating");
                                        if (r != null && r > 0) {
                                            item.put("rating", String.format(java.util.Locale.getDefault(), "⭐ %.1f", r));
                                            if (adapter != null) adapter.notifyDataSetChanged();
                                        }
                                    }
                                });

                        // Fetch customer name
                        if (customerId != null) {
                            final int idx = orderList.size() - 1;
                            db.collection("users").document(customerId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists() && userDoc.getString("name") != null) {
                                            item.put("customerName", userDoc.getString("name"));
                                            adapter.notifyItemChanged(idx);
                                        }
                                    });
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    
                    if (tvEarnedAmount != null) tvEarnedAmount.setText("Rs. " + todayEarned);
                    if (tvDeliveriesCompleted != null) tvDeliveriesCompleted.setText(todayCompleted + " deliveries completed");
                    if (tvOrdersThisWeek != null) tvOrdersThisWeek.setText(String.valueOf(weekCompleted));
                    if (tvOrdersThisMonth != null) tvOrdersThisMonth.setText(String.valueOf(monthCompleted));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAllRelevantOrders() {
        // Fallback: load orders that have assigned/delivered/cancelled status
        db.collection("orders").get().addOnSuccessListener(querySnapshot -> {
            orderList.clear();
            
            long todayEarned = 0;
            int todayCompleted = 0;
            int weekCompleted = 0;
            int monthCompleted = 0;
            
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();
            
            cal.add(java.util.Calendar.DAY_OF_YEAR, -7);
            long startOfWeek = cal.getTimeInMillis();
            
            cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            cal.add(java.util.Calendar.MONTH, -1);
            long startOfMonth = cal.getTimeInMillis();

            if (mAuth.getCurrentUser() == null) return;
            String currentUid = mAuth.getCurrentUser().getUid();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String riderId = doc.getString("riderId");
                if (riderId == null || !riderId.equals(currentUid)) continue;

                String status = doc.getString("status");
                if (status == null) continue;

                // Only show assigned, delivered, cancelled
                if (status.equalsIgnoreCase("assigned") ||
                        status.equalsIgnoreCase("delivered") ||
                        status.equalsIgnoreCase("cancelled") ||
                        status.equalsIgnoreCase("out_for_delivery") ||
                        status.equalsIgnoreCase("picked_up")) {

                    Map<String, String> item = new HashMap<>();
                    item.put("orderId", doc.getId());
                    item.put("status", status);

                    String customerId = doc.getString("customerId");
                    item.put("customerName", "Customer");
                    
                    Long deliveryFee = doc.getLong("deliveryFee");
                    long fee = deliveryFee != null ? deliveryFee : 100L;
                    item.put("earnings", "+ LKR " + fee);
                    
                    // Check if delivered today
                    if ("delivered".equalsIgnoreCase(status)) {
                        Long completedAt = doc.getLong("completedAt");
                        if (completedAt == null) {
                            completedAt = doc.getLong("createdAt");
                        }
                        if (completedAt != null) {
                            if (completedAt >= startOfDay) {
                                todayEarned += fee;
                                todayCompleted++;
                            }
                            if (completedAt >= startOfWeek) {
                                weekCompleted++;
                            }
                            if (completedAt >= startOfMonth) {
                                monthCompleted++;
                            }
                        }
                    }

                    orderList.add(item);

                    // Fetch customer name
                    if (customerId != null) {
                        final int idx = orderList.size() - 1;
                        db.collection("users").document(customerId).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists() && userDoc.getString("name") != null) {
                                        item.put("customerName", userDoc.getString("name"));
                                        adapter.notifyItemChanged(idx);
                                    }
                                });
                    }
                }
            }

            adapter.notifyDataSetChanged();
            updateEmptyState();
            
            if (tvEarnedAmount != null) tvEarnedAmount.setText("Rs. " + todayEarned);
            if (tvDeliveriesCompleted != null) tvDeliveriesCompleted.setText(todayCompleted + " deliveries completed");
            if (tvOrdersThisWeek != null) tvOrdersThisWeek.setText(String.valueOf(weekCompleted));
            if (tvOrdersThisMonth != null) tvOrdersThisMonth.setText(String.valueOf(monthCompleted));
        });
    }

    private void updateEmptyState() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (rvHistory != null) {
            rvHistory.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void setupBottomNav() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent i = new Intent(this, RiderDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
        View navMap = findViewById(R.id.navMap);
        if (navMap != null) {
            navMap.setOnClickListener(v ->
                    startActivity(new Intent(this, LiveMapActivity.class)));
        }
        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) {
            navHistory.setOnClickListener(v -> { /* already here */ });
        }
        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, RiderProfileActivity.class)));
        }
    }
}
