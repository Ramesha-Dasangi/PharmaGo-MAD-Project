package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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
    private OrderAdapter adapter;

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

        adapter = new OrderAdapter();
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);

        setupBottomNav();
        loadOrders();
    }

    private void loadOrders() {
        progressOrders.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        // Listen to all orders without a rider assigned (status = pending / ready / unassigned)
        ordersListener = db.collection("orders")
                .addSnapshotListener((snapshots, error) -> {
                    progressOrders.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e(TAG, "Failed to load orders", error);
                        Toast.makeText(this, "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<OrderModel> orders = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String status = doc.getString("status");
                            // Only show orders that need a rider
                            if (!"pending".equals(status) && !"ready".equals(status) && !"unassigned".equals(status)) {
                                continue;
                            }
                            // Skip if rider already assigned
                            String riderId = doc.getString("riderId");
                            if (riderId != null && !riderId.isEmpty()) continue;

                            OrderModel order = new OrderModel();
                            order.setId(doc.getId());
                            order.setOrderId(doc.getString("orderId"));
                            order.setCustomerId(doc.getString("customerId"));
                            order.setStatus(status);
                            order.setDeliveryAddress(doc.getString("deliveryAddress"));

                            // Total
                            Number total = (Number) doc.get("total");
                            order.setTotal(total != null ? total.doubleValue() : 0);

                            // Created at
                            Number createdAt = (Number) doc.get("createdAt");
                            order.setCreatedAt(createdAt != null ? createdAt.longValue() : 0);

                            // Item count from items list
                            java.util.List<?> items = (java.util.List<?>) doc.get("items");
                            order.setItemCount(items != null ? items.size() : 0);

                            orders.add(order);
                        }
                    }

                    adapter.setOrders(orders);
                    int count = orders.size();
                    tvOrderCount.setText(count + " order" + (count == 1 ? "" : "s"));
                    tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                    rvOrders.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                });
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
