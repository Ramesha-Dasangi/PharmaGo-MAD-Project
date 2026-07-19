package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity;
import com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    // ──────────────────── Views ────────────────────
    private TextView txtGreeting, txtPharmacy;
    private ImageView imgNotification;
    private TextView txtNotificationBadge;          // unread notification count badge

    private TextView txtOrders;                     // Pending orders count (New)
    private TextView txtCompleted;                  // Orders completed today
    private TextView txtMedicine;                   // Today's prescription count (updated label)
    private TextView txtStock;                      // Low stock count
    private TextView txtLowStockMessage;            // Low stock sub-label

    // Recent orders
    private RecyclerView recyclerRecentOrders;
    private TextView txtNoRecentOrders;
    private RecentOrderAdapter recentOrderAdapter;
    private ArrayList<OrderModel> recentOrderList;

    private BottomNavigationView bottomNavigation;

    // ──────────────────── Firebase ────────────────────
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String ownerId = "";

    // ──────────────────── Realtime Listeners ────────────────────
    private ListenerRegistration ordersListener;
    private ListenerRegistration medicinesListener;
    private ListenerRegistration notificationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // ── Firebase init ──
        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, com.nibm.pharmagomadproject.customer.activities.auth.LoginActivity.class));
            finish();
            return;
        }
        ownerId = user.getUid();

        // ── View binding ──
        txtGreeting  = findViewById(R.id.txtGreeting);
        txtPharmacy  = findViewById(R.id.txtPharmacy);
        imgNotification = findViewById(R.id.imgNotification);
        txtNotificationBadge = findViewById(R.id.txtNotificationBadge);

        txtOrders    = findViewById(R.id.txtOrders);
        txtCompleted = findViewById(R.id.txtCompleted);
        txtMedicine  = findViewById(R.id.txtMedicine);
        txtStock     = findViewById(R.id.txtStock);
        txtLowStockMessage = findViewById(R.id.txtLowStockMessage);

        recyclerRecentOrders = findViewById(R.id.recyclerRecentOrders);
        txtNoRecentOrders    = findViewById(R.id.txtNoRecentOrders);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // ── Recent orders RecyclerView setup ──
        recentOrderList = new ArrayList<>();
        recentOrderAdapter = new RecentOrderAdapter(this, recentOrderList);
        recyclerRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecentOrders.setAdapter(recentOrderAdapter);

        // ── Greeting ──
        setGreeting();

        // ── Pharmacy name ──
        loadPharmacyName();

        // ── Notification bell ──
        imgNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        // ── "See All" orders link ──
        TextView txtSeeAll = findViewById(R.id.txtSeeAllOrders);
        if (txtSeeAll != null) {
            txtSeeAll.setOnClickListener(v ->
                    startActivity(new Intent(this, OrdersActivity.class)));
        }

        // ── Bottom navigation ──
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home)      return true;
                if (id == R.id.nav_orders) {
                    startActivity(new Intent(DashboardActivity.this, OrdersActivity.class));
                    return true;
                }
                if (id == R.id.nav_inventory) {
                    startActivity(new Intent(DashboardActivity.this, InventoryActivity.class));
                    finish();
                    return true;
                }
                if (id == R.id.nav_reports) {
                    startActivity(new Intent(DashboardActivity.this, SalesReportActivity.class));
                    finish();
                    return true;
                }
                if (id == R.id.nav_profile) {
                    startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ownerId.isEmpty()) {
            startRealtimeListeners();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRealtimeListeners();
    }

    // ═══════════════════════════════════════════════════
    //  Start real-time Firestore listeners
    // ═══════════════════════════════════════════════════
    private void startRealtimeListeners() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Fetch user names first to map them on order updates
        db.collection("users").get().addOnSuccessListener(userSnaps -> {
            Map<String, String> userNameById = new HashMap<>();
            for (DocumentSnapshot doc : userSnaps) {
                String name = doc.getString("name");
                userNameById.put(doc.getId(), name != null ? name : "Customer");
            }

            // Listen to ALL orders and filter in-memory by items[].pharmacyId
            // (top-level pharmacyId is not stored by the customer order flow)
            ordersListener = db.collection("orders")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Toast.makeText(DashboardActivity.this, "Failed to sync orders", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots != null) {
                            // Calculate start of today in millis
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            long startOfDay = cal.getTimeInMillis();

                            int pendingCount = 0;
                            int completedTodayCount = 0;
                            int prescriptionTodayCount = 0;

                            List<OrderModel> tempList = new ArrayList<>();
                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                String fsStatus  = doc.getString("status");
                                long createdAt   = doc.getLong("createdAt")   != null ? doc.getLong("createdAt")   : 0;
                                long completedAt = doc.getLong("completedAt") != null ? doc.getLong("completedAt") : 0;

                                List<Map<String, Object>> items =
                                        (List<Map<String, Object>>) doc.get("items");

                                // Only count orders that belong to this pharmacy
                                boolean belongsToMe = false;
                                boolean hasRx = doc.getString("prescriptionUrl") != null
                                        && !doc.getString("prescriptionUrl").isEmpty();

                                if (items != null) {
                                    for (Map<String, Object> item : items) {
                                        Object pid = item.get("pharmacyId");
                                        if (ownerId.equals(pid)) belongsToMe = true;
                                        String typeStr = (String) item.get("type");
                                        if ("Prescription".equalsIgnoreCase(typeStr)) hasRx = true;
                                    }
                                }

                                if (!belongsToMe) continue; // skip other pharmacies' orders

                                // A. New / Pending orders
                                if ("pending".equalsIgnoreCase(fsStatus)) pendingCount++;

                                // B. Completed TODAY — use completedAt timestamp
                                if ("completed".equalsIgnoreCase(fsStatus) && completedAt >= startOfDay) {
                                    completedTodayCount++;
                                }

                                // C. Prescription orders created today
                                if (hasRx && createdAt >= startOfDay) prescriptionTodayCount++;

                                // D. Build recent orders list entry
                                String orderId      = doc.getId();
                                String customerId   = doc.getString("customerId");
                                String customerName = userNameById.getOrDefault(customerId, "Customer");

                                StringBuilder itemsDesc = new StringBuilder();
                                if (items != null) {
                                    for (Map<String, Object> item : items) {
                                        if (itemsDesc.length() > 0) itemsDesc.append(", ");
                                        Object medName = item.get("medicineName");
                                        Object qty = item.get("quantity");
                                        if (medName != null) {
                                            itemsDesc.append(medName).append(" x").append(qty != null ? qty : 1);
                                        }
                                    }
                                }

                                String displayStatus = "New";
                                if ("processing".equalsIgnoreCase(fsStatus)) {
                                    displayStatus = "Processing";
                                } else if ("completed".equalsIgnoreCase(fsStatus)
                                        || "delivered".equalsIgnoreCase(fsStatus)
                                        || "ready".equalsIgnoreCase(fsStatus)) {
                                    displayStatus = "Completed";
                                } else if ("rejected".equalsIgnoreCase(fsStatus)
                                        || "cancelled".equalsIgnoreCase(fsStatus)) {
                                    displayStatus = "Cancelled";
                                }

                                double total = 0;
                                Object totalObj = doc.get("total");
                                if (totalObj instanceof Number) {
                                    total = ((Number) totalObj).doubleValue();
                                }
                                String timeStr = createdAt > 0 ? sdf.format(new Date(createdAt)) : "--:--";
                                String type = hasRx ? "RX Required" : "OTC";

                                tempList.add(new OrderModel(
                                        orderId, customerName, customerId,
                                        itemsDesc.toString(), timeStr,
                                        "Rs. " + String.format("%.0f", total), type, displayStatus));
                            }

                            // Update stats views
                            if (txtOrders != null) txtOrders.setText(String.valueOf(pendingCount));
                            if (txtCompleted != null) txtCompleted.setText(String.valueOf(completedTodayCount));
                            if (txtMedicine != null) txtMedicine.setText(String.valueOf(prescriptionTodayCount));

                            // Sort recent orders newest first (using order ID descending as PG-<timestamp>)
                            Collections.sort(tempList, (a, b) -> b.getOrderId().compareTo(a.getOrderId()));

                            recentOrderList.clear();
                            int limit = Math.min(tempList.size(), 5);
                            for (int i = 0; i < limit; i++) {
                                recentOrderList.add(tempList.get(i));
                            }
                            recentOrderAdapter.notifyDataSetChanged();

                            if (txtNoRecentOrders != null) {
                                txtNoRecentOrders.setVisibility(recentOrderList.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                        }
                    });
        });

        // 2. Real-time listener for medicines (Total medicines count & low stock)
        medicinesListener = db.collection("medicines")
                .whereEqualTo("pharmacyId", ownerId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (snapshots != null) {
                        int lowStockCount = 0;

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Boolean deleted = doc.getBoolean("deleted");
                            if (Boolean.TRUE.equals(deleted)) continue;

                            Long stock = doc.getLong("stock");
                            if (stock != null && stock <= 20) {
                                lowStockCount++;
                            }
                        }

                        if (txtStock != null) txtStock.setText(String.valueOf(lowStockCount));
                        if (txtLowStockMessage != null) {
                            if (lowStockCount == 0) {
                                txtLowStockMessage.setText("All stocked");
                                txtLowStockMessage.setTextColor(Color.parseColor("#4CAF50"));
                            } else {
                                txtLowStockMessage.setText(lowStockCount + " need restock");
                                txtLowStockMessage.setTextColor(Color.parseColor("#E53935"));
                            }
                        }
                    }
                });

        // 3. Real-time listener for unread notifications badge
        notificationsListener = db.collection("notifications")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    if (snapshots != null) {
                        int unread = snapshots.size();
                        if (txtNotificationBadge != null) {
                            if (unread > 0) {
                                txtNotificationBadge.setVisibility(View.VISIBLE);
                                txtNotificationBadge.setText(unread > 99 ? "99+" : String.valueOf(unread));
                            } else {
                                txtNotificationBadge.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    // ═══════════════════════════════════════════════════
    //  Stop real-time Firestore listeners
    // ═══════════════════════════════════════════════════
    private void stopRealtimeListeners() {
        if (ordersListener != null) {
            ordersListener.remove();
            ordersListener = null;
        }
        if (medicinesListener != null) {
            medicinesListener.remove();
            medicinesListener = null;
        }
        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }
    }

    // ═══════════════════════════════════════════════════
    //  Load pharmacy name from Firestore
    // ═══════════════════════════════════════════════════
    private void loadPharmacyName() {
        db.collection("pharmacies")
                .whereEqualTo("ownerId", ownerId)
                .limit(1)
                .get()
                .addOnSuccessListener(snaps -> {
                    if (!snaps.isEmpty()) {
                        String name = snaps.getDocuments().get(0).getString("name");
                        if (name != null) txtPharmacy.setText(name);
                    }
                });
    }

    // ═══════════════════════════════════════════════════
    //  Greeting based on time of day
    // ═══════════════════════════════════════════════════
    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if      (hour >= 5  && hour < 12) txtGreeting.setText("Good Morning ☀️");
        else if (hour >= 12 && hour < 17) txtGreeting.setText("Good Afternoon 🌤️");
        else if (hour >= 17 && hour < 21) txtGreeting.setText("Good Evening 🌆");
        else                              txtGreeting.setText("Good Night 🌙");
    }
}