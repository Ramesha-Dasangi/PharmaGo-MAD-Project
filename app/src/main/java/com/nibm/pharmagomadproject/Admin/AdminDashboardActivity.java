package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.auth.LoginActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView ivSettings = findViewById(R.id.ivSettings);
        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, SettingsActivity.class));
            }
        });

        MaterialCardView cardPendingApprovals = findViewById(R.id.cardPendingApprovals);
        cardPendingApprovals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, PendingApprovalsActivity.class);
                startActivity(intent);
            }
        });

        MaterialCardView cardComplaints = findViewById(R.id.cardComplaints);
        cardComplaints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ComplaintsActivity.class);
                startActivity(intent);
            }
        });

        MaterialCardView cardUnassignedOrders = findViewById(R.id.cardUnassignedOrders);
        cardUnassignedOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, UnassignedOrdersActivity.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_approvals) {
                startActivity(new Intent(AdminDashboardActivity.this, PendingApprovalsActivity.class));
                return true;
            } else if (itemId == R.id.nav_delivery) {
                startActivity(new Intent(AdminDashboardActivity.this, UnassignedOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(AdminDashboardActivity.this, ComplaintsActivity.class));
                return true;
            }
            return false;
        });

        ImageView ivLogout = findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new androidx.appcompat.app.AlertDialog.Builder(AdminDashboardActivity.this)
                        .setTitle("Log out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Log out", (dialog, which) -> {
                            Intent intent = new Intent(AdminDashboardActivity.this,
                                    LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        fetchDashboardData();
    }

    private com.google.firebase.firestore.ListenerRegistration listenerUsers;
    private com.google.firebase.firestore.ListenerRegistration listenerPharmacies;
    private com.google.firebase.firestore.ListenerRegistration listenerRiders;
    private com.google.firebase.firestore.ListenerRegistration listenerOrders;
    private com.google.firebase.firestore.ListenerRegistration listenerComplaints;

    private int pendingPharmaciesCount = 0;
    private int pendingRidersCount = 0;

    private void fetchDashboardData() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        android.widget.TextView tvTotalUsers = findViewById(R.id.tvTotalUsers);
        android.widget.TextView tvPharmacies = findViewById(R.id.tvPharmacies);
        android.widget.TextView tvActiveRiders = findViewById(R.id.tvActiveRiders);
        android.widget.TextView tvOrdersToday = findViewById(R.id.tvOrdersToday);
        android.widget.TextView tvPendingApprovalsCount = findViewById(R.id.tvPendingApprovalsCount);
        android.widget.TextView tvPendingApprovalsSub = findViewById(R.id.tvPendingApprovalsSub);
        android.widget.TextView tvComplaintsCount = findViewById(R.id.tvComplaintsCount);
        android.widget.TextView tvComplaintsSub = findViewById(R.id.tvComplaintsSub);
        android.widget.TextView tvUnassignedOrdersCount = findViewById(R.id.tvUnassignedOrdersCount);

        // Live listener for users (customers/users count)
        listenerUsers = db.collection("users").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            long realUsers = snapshots.getDocuments().stream()
                    .filter(d -> !"admin".equalsIgnoreCase(d.getString("role")))
                    .count();
            if (tvTotalUsers != null) tvTotalUsers.setText(String.valueOf(realUsers));
        });

        // Live listener for pharmacies collection (source of truth for pharmacies)
        listenerPharmacies = db.collection("pharmacies").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            int approvedPharmacies = 0;
            int pendingPharmacies = 0;

            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                String status = doc.getString("status");
                Boolean approved = doc.getBoolean("isApproved");
                boolean isApprovedFlag = Boolean.TRUE.equals(approved) || "approved".equalsIgnoreCase(status);
                boolean isRejectedFlag = "rejected".equalsIgnoreCase(status);

                if (isApprovedFlag) approvedPharmacies++;
                else if (!isRejectedFlag) pendingPharmacies++;
            }

            if (tvPharmacies != null) tvPharmacies.setText(String.valueOf(approvedPharmacies));
            pendingPharmaciesCount = pendingPharmacies;
            updatePendingUI(tvPendingApprovalsCount, tvPendingApprovalsSub);
        });

        // Live listener for riders collection (source of truth for riders)
        listenerRiders = db.collection("riders").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            int approvedRiders = 0;
            int pendingRiders = 0;

            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                String status = doc.getString("status");
                Boolean approved = doc.getBoolean("isApproved");
                boolean isApprovedFlag = Boolean.TRUE.equals(approved) || "approved".equalsIgnoreCase(status);
                boolean isRejectedFlag = "rejected".equalsIgnoreCase(status);

                if (isApprovedFlag) approvedRiders++;
                else if (!isRejectedFlag) pendingRiders++;
            }

            if (tvActiveRiders != null) tvActiveRiders.setText(String.valueOf(approvedRiders));
            pendingRidersCount = pendingRiders;
            updatePendingUI(tvPendingApprovalsCount, tvPendingApprovalsSub);
        });

        // Live listener for Orders — today's orders + unassigned
        listenerOrders = db.collection("orders").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;

            // Compute midnight of today (local time)
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            long todayMidnight = cal.getTimeInMillis();

            int ordersToday = 0;
            int unassigned = 0;

            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                // Count today's orders (createdAt >= midnight)
                Object createdAtObj = doc.get("createdAt");
                long createdMs = 0;
                if (createdAtObj instanceof com.google.firebase.Timestamp) {
                    createdMs = ((com.google.firebase.Timestamp) createdAtObj).toDate().getTime();
                } else if (createdAtObj instanceof Number) {
                    createdMs = ((Number) createdAtObj).longValue();
                }
                if (createdMs >= todayMidnight) ordersToday++;

                // Count orders pending rider assignment
                String riderId = doc.getString("riderId");
                if ((riderId == null || riderId.trim().isEmpty()) && isReadyForRiderAssignment(doc)) {
                    unassigned++;
                }
            }

            if (tvOrdersToday != null) tvOrdersToday.setText(String.valueOf(ordersToday));
            if (tvUnassignedOrdersCount != null)
                tvUnassignedOrdersCount.setText(unassigned + " unassigned orders");
        });

        // Live listener for Complaints
        listenerComplaints = db.collection("complaints").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            if (tvComplaintsCount != null) tvComplaintsCount.setText(snapshots.size() + " complaints");
            int pendingComplaints = 0;
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                String status = doc.getString("status");
                if (!"resolved".equalsIgnoreCase(status)) pendingComplaints++;
            }
            if (tvComplaintsSub != null) tvComplaintsSub.setText(pendingComplaints + " need attention");
        });
    }

    private void updatePendingUI(android.widget.TextView tvCount, android.widget.TextView tvSub) {
        int total = pendingPharmaciesCount + pendingRidersCount;
        if (tvCount != null) tvCount.setText(total + " pending approvals");
        if (tvSub != null) tvSub.setText(pendingPharmaciesCount + " pharmacies · " + pendingRidersCount + " riders");
    }

    private boolean isReadyForRiderAssignment(com.google.firebase.firestore.DocumentSnapshot doc) {
        String status = doc.getString("status");
        if (status == null) return false;

        String s = status.toLowerCase();
        if ("picked_up".equals(s) || "out_for_delivery".equals(s) || "delivered".equals(s)
                || "completed".equals(s) || "cancelled".equals(s) || "rejected".equals(s)) {
            return false;
        }

        java.util.List<?> pharmIds = (java.util.List<?>) doc.get("pharmacyIds");
        java.util.List<?> confirmedList = (java.util.List<?>) doc.get("confirmedPharmacies");
        java.util.List<?> rejectedList  = (java.util.List<?>) doc.get("rejectedPharmacies");

        int totalPharmCount = (pharmIds != null && !pharmIds.isEmpty()) ? pharmIds.size() : 1;

        if (pharmIds == null || pharmIds.isEmpty()) {
            java.util.List<?> items = (java.util.List<?>) doc.get("items");
            if (items != null && !items.isEmpty()) {
                java.util.Set<String> set = new java.util.HashSet<>();
                for (Object itemObj : items) {
                    if (itemObj instanceof java.util.Map) {
                        Object pId = ((java.util.Map<?, ?>) itemObj).get("pharmacyId");
                        if (pId != null) set.add(pId.toString());
                    }
                }
                if (!set.isEmpty()) totalPharmCount = set.size();
            }
        }

        int confirmedCount = confirmedList != null ? confirmedList.size() : 0;
        int rejectedCount  = rejectedList  != null ? rejectedList.size()  : 0;

        if (confirmedCount < 1) return false;
        return (confirmedCount + rejectedCount) >= totalPharmCount;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerUsers != null) listenerUsers.remove();
        if (listenerPharmacies != null) listenerPharmacies.remove();
        if (listenerRiders != null) listenerRiders.remove();
        if (listenerOrders != null) listenerOrders.remove();
        if (listenerComplaints != null) listenerComplaints.remove();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
