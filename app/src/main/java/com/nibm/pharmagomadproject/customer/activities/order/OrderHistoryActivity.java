package com.nibm.pharmagomadproject.customer.activities.order;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.home.HomeActivity;
import com.nibm.pharmagomadproject.customer.activities.profile.ProfileActivity;
import com.nibm.pharmagomadproject.customer.activities.report.ReportIssueActivity;
import com.nibm.pharmagomadproject.customer.adapter.OrderAdapter;
import com.nibm.pharmagomadproject.customer.models.Order;

import java.util.ArrayList;
import java.util.List;

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

    private void loadOrders() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        db.collection("orders")
                .whereEqualTo("customerId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    allOrders.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        Order o = new Order();
                        o.setOrderId(doc.getId());
                        o.setPharmacyName(doc.getString("pharmacyName") != null
                                ? doc.getString("pharmacyName") : "");
                        o.setStatus(doc.getString("status") != null
                                ? doc.getString("status") : "pending");
                        o.setTotal(doc.getDouble("total") != null
                                ? doc.getDouble("total") : 0);
                        allOrders.add(o);
                    }

                    applyTabFilter();
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
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
            String s = o.getStatus();
            boolean include;
            switch (currentTab) {
                case "active":
                    include = "pending".equals(s) || "processing".equals(s)
                            || "picked_up".equals(s) || "out_for_delivery".equals(s);
                    break;
                case "delivered":
                    include = "delivered".equals(s);
                    break;
                case "cancelled":
                    include = "cancelled".equals(s);
                    break;
                default:
                    include = true;
            }
            if (include) filteredOrders.add(o);
        }

        adapter.notifyDataSetChanged();

        boolean empty = filteredOrders.isEmpty();
        if (tvEmpty != null) {
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            tvEmpty.setText("No " + currentTab + " orders");
        }
        if (rvOrders != null) rvOrders.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ─── OrderAdapter.OrderListener ──────────────────

    @Override
    public void onTrackOrder(Order order) {
        Intent i = new Intent(this, OrderTrackingActivity.class);
        i.putExtra("orderId", order.getOrderId());
        startActivity(i);
    }

    @Override
    public void onReorder(Order order) {
        Toast.makeText(this, "Items added to cart!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, CartActivity.class));
    }

    @Override
    public void onReportIssue(Order order) {
        Intent i = new Intent(this, ReportIssueActivity.class);
        i.putExtra("orderId", order.getOrderId());
        startActivity(i);
    }

    @Override
    public void onCancelOrder(Order order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel order?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Yes, cancel", (dialog, which) -> {
                    db.collection("orders").document(order.getOrderId())
                            .update("status", "cancelled")
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show();
                                loadOrders();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
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
