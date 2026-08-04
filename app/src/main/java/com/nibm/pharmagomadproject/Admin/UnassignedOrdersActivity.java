package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.List;

public class UnassignedOrdersActivity extends AppCompatActivity {

    private static final String TAG = "UnassignedOrders";

    private FirebaseFirestore db;
    private ListenerRegistration ordersListener;

    private RecyclerView rvOrders;
    private ProgressBar progressOrders;
    private TextView tvEmpty, tvOrderCount;
    private TextView tabUnassigned, tabAssigned;
    private OrderAdapter adapter;
    
    private List<OrderModel> unassignedList = new ArrayList<>();
    private List<OrderModel> assignedList = new ArrayList<>();
    private boolean isUnassignedTabActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unassigned_orders);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        rvOrders = findViewById(R.id.rvOrders);
        progressOrders = findViewById(R.id.progressOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        
        tabUnassigned = findViewById(R.id.tabUnassigned);
        tabAssigned = findViewById(R.id.tabAssigned);

        adapter = new OrderAdapter();
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
        
        tabUnassigned.setOnClickListener(v -> selectTab(true));
        tabAssigned.setOnClickListener(v -> selectTab(false));

        setupBottomNav();
        loadOrders();
    }

    private void loadOrders() {
        progressOrders.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        // Listen to orders
        ordersListener = db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    progressOrders.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e(TAG, "Failed to load orders", error);
                        Toast.makeText(this, "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    unassignedList.clear();
                    assignedList.clear();
                    
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String status = doc.getString("status");
                            String riderId = doc.getString("riderId");
                            String riderName = doc.getString("riderName");
                            
                            // We only care about orders that haven't been picked up or delivered yet
                            if ("picked_up".equals(status) || "delivered".equals(status) || "cancelled".equals(status)) {
                                continue;
                            }

                            OrderModel order = new OrderModel();
                            order.setId(doc.getId());
                            order.setOrderId(doc.getString("orderId"));
                            order.setCustomerId(doc.getString("customerId"));
                            order.setStatus(status);
                            order.setDeliveryAddress(doc.getString("deliveryAddress"));
                            order.setRiderId(riderId);
                            order.setRiderName(riderName);

                            // Total
                            Number total = (Number) doc.get("total");
                            order.setTotal(total != null ? total.doubleValue() : 0);

                            // Created at
                            Number createdAt = (Number) doc.get("createdAt");
                            order.setCreatedAt(createdAt != null ? createdAt.longValue() : 0);

                            // Item count from items list
                            java.util.List<?> items = (java.util.List<?>) doc.get("items");
                            order.setItemCount(items != null ? items.size() : 0);

                            if (riderId != null && !riderId.isEmpty()) {
                                assignedList.add(order);
                            } else {
                                unassignedList.add(order);
                            }
                        }
                    }

                    updateUI();
                });
    }

    private void selectTab(boolean isUnassigned) {
        if (isUnassignedTabActive == isUnassigned) return;
        isUnassignedTabActive = isUnassigned;
        
        if (isUnassigned) {
            tabUnassigned.setBackgroundResource(R.drawable.tab_active_bg);
            tabUnassigned.setTextColor(Color.WHITE);
            tabUnassigned.setTypeface(null, Typeface.BOLD);
            
            tabAssigned.setBackground(null);
            tabAssigned.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabAssigned.setTypeface(null, Typeface.NORMAL);
        } else {
            tabAssigned.setBackgroundResource(R.drawable.tab_active_bg);
            tabAssigned.setTextColor(Color.WHITE);
            tabAssigned.setTypeface(null, Typeface.BOLD);
            
            tabUnassigned.setBackground(null);
            tabUnassigned.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabUnassigned.setTypeface(null, Typeface.NORMAL);
        }
        
        updateUI();
    }
    
    private void updateUI() {
        List<OrderModel> currentList = isUnassignedTabActive ? unassignedList : assignedList;
        adapter.setOrders(currentList);
        
        int count = currentList.size();
        tvOrderCount.setText(count + " order" + (count == 1 ? "" : "s"));
        
        if (count == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(isUnassignedTabActive ? "No unassigned orders" : "No pending assigned orders");
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_delivery);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            } else if (id == R.id.nav_approvals) {
                startActivity(new Intent(this, PendingApprovalsActivity.class));
                return true;
            } else if (id == R.id.nav_delivery) {
                return true;
            } else if (id == R.id.nav_complaints) {
                startActivity(new Intent(this, ComplaintsActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersListener != null) ordersListener.remove();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_delivery);
        }
    }
}
