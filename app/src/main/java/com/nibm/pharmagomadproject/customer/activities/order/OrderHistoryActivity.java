package com.nibm.pharmagomadproject.customer.activities.order;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.home.HomeActivity;
import com.nibm.pharmagomadproject.customer.activities.profile.ProfileActivity;
import com.nibm.pharmagomadproject.customer.activities.report.ReportIssueActivity;
import com.nibm.pharmagomadproject.customer.adapter.OrderAdapter;
import com.nibm.pharmagomadproject.customer.models.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrderHistoryActivity extends AppCompatActivity implements OrderAdapter.OrderListener {

    private FirebaseFirestore db;
    private FirebaseAuth      mAuth;

    private RecyclerView  rvOrders;
    private OrderAdapter  adapter;
    private ProgressBar   progressBar;
    private TextView      tvEmpty;
    private TextView      tabAll, tabActive, tabDelivered, tabCancelled;

    private final List<Order> allOrders      = new ArrayList<>();
    private final List<Order> filteredOrders = new ArrayList<>();
    private String currentTab = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvOrders    = findViewById(R.id.rvOrders);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty     = findViewById(R.id.tvEmpty);
        tabAll       = findViewById(R.id.tabAll);
        tabActive    = findViewById(R.id.tabActive);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabCancelled = findViewById(R.id.tabCancelled);

        adapter = new OrderAdapter(this, filteredOrders, this);
        if (rvOrders != null) {
            rvOrders.setLayoutManager(new LinearLayoutManager(this));
            rvOrders.setAdapter(adapter);
        }

        // Tab clicks
        tabAll.setOnClickListener(v       -> selectTab("all"));
        tabActive.setOnClickListener(v    -> selectTab("active"));
        tabDelivered.setOnClickListener(v -> selectTab("delivered"));
        tabCancelled.setOnClickListener(v -> selectTab("cancelled"));

        // Bottom nav
        setupBottomNav();

        // Load orders from Firestore
        loadOrders();
        selectTab("all");
    }

    private com.google.firebase.firestore.ListenerRegistration ordersListener;

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        if (ordersListener != null) ordersListener.remove();

        ordersListener = db.collection("orders")
                .whereEqualTo("customerId", userId)
                .addSnapshotListener((query, error) -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (error != null || query == null) return;

                    allOrders.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Order o = new Order();
                        String storedId = doc.getString("orderId");
                        o.setOrderId(storedId != null && !storedId.isEmpty() ? storedId : doc.getId());

                        String phName = doc.getString("pharmacyName");
                        if (phName == null || phName.trim().isEmpty()) {
                            Object raw = doc.get("items");
                            if (raw instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> items = (List<Map<String, Object>>) raw;
                                if (!items.isEmpty()) {
                                    Object pn = items.get(0).get("pharmacyName");
                                    if (pn instanceof String) phName = (String) pn;
                                }
                            }
                        }
                        o.setPharmacyName(phName != null ? phName : "Pharmacy");

                        o.setStatus(doc.getString("status") != null
                                ? doc.getString("status") : "pending");
                        Double totalVal = doc.getDouble("total");
                        o.setTotal(totalVal != null ? totalVal : 0.0);
                        String riderIdVal = doc.getString("riderId");
                        o.setRiderId(riderIdVal != null ? riderIdVal : "");

                        Object createdAtObj = doc.get("createdAt");
                        if (createdAtObj instanceof com.google.firebase.Timestamp) {
                            o.setCreatedAt((com.google.firebase.Timestamp) createdAtObj);
                        } else if (createdAtObj instanceof Long) {
                            o.setCreatedAt(new com.google.firebase.Timestamp((Long) createdAtObj / 1000, 0));
                        } else if (createdAtObj instanceof String) {
                            try {
                                Date parsed = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) createdAtObj);
                                o.setCreatedAt(new com.google.firebase.Timestamp(parsed.getTime() / 1000, 0));
                            } catch (Exception e) {
                                o.setCreatedAt(null);
                            }
                        } else {
                            o.setCreatedAt(null);
                        }


                        Object rawItems = doc.get("items");
                        if (rawItems instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) rawItems;
                            o.setItems(itemsList);
                        }

                        allOrders.add(o);
                    }

                    // Sort newest orders first
                    allOrders.sort((o1, o2) -> {
                        if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                        }
                        return o2.getOrderId().compareTo(o1.getOrderId());
                    });

                    applyTabFilter();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersListener != null) ordersListener.remove();
    }

    private void selectTab(String tab) {
        currentTab = tab;

        // Update chip UI
        TextView[] tabs = {tabAll, tabActive, tabDelivered, tabCancelled};
        String[]   keys = {"all", "active", "delivered", "cancelled"};
        int primary = getResources().getColor(R.color.pg_primary, null);
        int sub     = getResources().getColor(R.color.pg_sub, null);

        for (int i = 0; i < tabs.length; i++) {
            boolean sel = keys[i].equals(tab);
            tabs[i].setBackgroundResource(sel
                    ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
            tabs[i].setTextColor(sel ? primary : sub);
            tabs[i].setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
        }

        applyTabFilter();
    }

    private void applyTabFilter() {
        filteredOrders.clear();

        for (Order o : allOrders) {
            String s = o.getStatus() != null ? o.getStatus().toLowerCase().trim() : "pending";
            boolean include;
            switch (currentTab) {
                case "delivered":
                    include = "delivered".equals(s) || "completed".equals(s);
                    break;
                case "cancelled":
                    include = "cancelled".equals(s);
                    break;
                case "active":
                    // Everything that is NOT delivered or cancel ed goes here
                    include = !"delivered".equals(s) && !"completed".equals(s) && !"cancelled".equals(s);
                    break;
                default: // "all"
                    include = true;
            }
            if (include) filteredOrders.add(o);
        }

        adapter.notifyDataSetChanged();

        boolean empty = filteredOrders.isEmpty();
        if (tvEmpty != null) {
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        if (rvOrders != null) rvOrders.setVisibility(empty ? View.GONE : View.VISIBLE);
    }


    // OrderAdapter.OrderListener

    @Override
    public void onTrackOrder(Order order) {
        if ("approved_pending_payment".equalsIgnoreCase(order.getStatus())) {
            Intent i = new Intent(this, PaymentActivity.class);
            i.putExtra("orderId", order.getOrderId());
            startActivity(i);
        } else {
            Intent i = new Intent(this, OrderTrackingActivity.class);
            i.putExtra("orderId", order.getOrderId());
            startActivity(i);
        }
    }

    @Override
    public void onReorder(Order order) {
        List<Map<String, Object>> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            addItemsToCartAndNavigate(items);
        } else {
            // Fetch doc from Firestore if items were not cached
            db.collection("orders").document(order.getOrderId()).get()
                    .addOnSuccessListener(doc -> {
                        List<Map<String, Object>> fetchedItems = (List<Map<String, Object>>) doc.get("items");
                        if (fetchedItems != null && !fetchedItems.isEmpty()) {
                            addItemsToCartAndNavigate(fetchedItems);
                        } else {
                            Toast.makeText(this, "No items found in this order", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load order items", Toast.LENGTH_SHORT).show());
        }
    }

    private void addItemsToCartAndNavigate(List<Map<String, Object>> items) {
        int addedCount = 0;
        for (Map<String, Object> map : items) {
            String medId = (String) map.get("medicineId");
            String name  = (String) map.get("medicineName");
            String brand = (String) map.get("brand");
            String phId  = (String) map.get("pharmacyId");
            String phName = (String) map.get("pharmacyName");
            double price = map.get("price") instanceof Number ? ((Number) map.get("price")).doubleValue() : 0.0;
            int qty      = map.get("quantity") instanceof Number ? ((Number) map.get("quantity")).intValue() : 1;

            com.nibm.pharmagomadproject.customer.models.Cart cartItem =
                    new com.nibm.pharmagomadproject.customer.models.Cart(
                            medId != null ? medId : "temp_" + System.currentTimeMillis(),
                            name != null ? name : "Medicine",
                            brand != null ? brand : "",
                            phId != null ? phId : "",
                            phName != null ? phName : "",
                            price,
                            qty
                    );
            CartActivity.addToCart(cartItem);
            addedCount++;
        }
        Toast.makeText(this, addedCount + " item(s) re-added to cart!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, CartActivity.class));
    }

    @Override
    public void onRateOrder(Order order) {
        Intent intent = new Intent(this, com.nibm.pharmagomadproject.customer.activities.review.ReviewActivity.class);
        intent.putExtra("orderId", order.getOrderId());
        intent.putExtra("pharmacyId", order.getPharmacyId() != null ? order.getPharmacyId() : "");
        intent.putExtra("riderId", order.getRiderId() != null ? order.getRiderId() : "");
        startActivity(intent);
    }

    @Override
    public void onReportIssue(Order order) {
        Intent i = new Intent(this, ReportIssueActivity.class);
        i.putExtra("orderId", order.getOrderId());
        startActivity(i);
    }

    @Override
    public void onCancelOrder(Order order) {
        String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "";
        // Block cancel if order is already processing or further
        if ("processing".equals(status) || "partially_approved".equals(status)
                || "picked_up".equals(status) || "out_for_delivery".equals(status)
                || "delivered".equals(status) || "completed".equals(status)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cannot Cancel")
                    .setMessage("This order is already being processed and cannot be cancelled.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel order?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Yes, cancel", (dialog, which) -> {
                    db.collection("orders").document(order.getOrderId())
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) return;
                                String paymentMethod = doc.getString("paymentMethod");
                                String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

                                db.collection("orders").document(order.getOrderId())
                                        .update("status", "cancelled")
                                        .addOnSuccessListener(v -> {
                                            Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show();

                                            // Send refund notification if paid by card
                                            if ("card".equalsIgnoreCase(paymentMethod) && !uid.isEmpty()) {
                                                java.util.Map<String, Object> notif = new java.util.HashMap<>();
                                                notif.put("userId", uid);
                                                notif.put("title", "Refund Initiated 💳");
                                                notif.put("message", "Your order " + order.getOrderId() + " was cancelled. A refund will be processed to your card within 2 working days.");
                                                notif.put("type", "refund");
                                                notif.put("referenceId", order.getOrderId());
                                                notif.put("isRead", false);
                                                notif.put("createdAt", System.currentTimeMillis());
                                                com.nibm.pharmagomadproject.customer.CustomerNotificationHelper.sendNotification(uid, notif);
                                            }
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Failed: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to load order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Keep order", null)
                .show();
    }

    private void setupBottomNav() {
        View navHome = findViewById(R.id.navHome);
        View navCart = findViewById(R.id.navCart);
        View navProfile = findViewById(R.id.navProfile);

        if (navHome != null) navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        if (navCart != null) navCart.setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));
        if (navProfile != null) navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}
