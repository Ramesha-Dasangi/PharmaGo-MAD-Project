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

        // Live listener for users (customers + pharmacy owners)
        listenerUsers = db.collection("users").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            int total = snapshots.size();
            int pharmaciesCount = 0;
            int pendingPharm = 0;

            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                String role = doc.getString("role");
                String status = doc.getString("status");
                if ("pharmacy_owner".equals(role)) {
                    pharmaciesCount++;
                    if ("pending".equals(status)) pendingPharm++;
                }
            }

            if (tvTotalUsers != null) tvTotalUsers.setText(String.valueOf(total));
            if (tvPharmacies != null) tvPharmacies.setText(String.valueOf(pharmaciesCount));

            pendingPharmaciesCount = pendingPharm;
            updatePendingUI(tvPendingApprovalsCount, tvPendingApprovalsSub);
        });

        // Live listener for riders collection (source of truth for riders)
        listenerRiders = db.collection("riders").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            int approvedRiders = 0;
            int pendingRiders = 0;

            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                String status = doc.getString("status");
                if ("approved".equals(status)) approvedRiders++;
                else if ("pending".equals(status)) pendingRiders++;
            }

            if (tvActiveRiders != null) tvActiveRiders.setText(String.valueOf(approvedRiders));

            pendingRidersCount = pendingRiders;
            updatePendingUI(tvPendingApprovalsCount, tvPendingApprovalsSub);
        });

        // Live listener for Orders
        listenerOrders = db.collection("orders").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            if (tvOrdersToday != null) tvOrdersToday.setText(String.valueOf(snapshots.size()));
            int unassigned = 0;
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                String status = doc.getString("status");
                if ("pending".equals(status) || "ready".equals(status) || "unassigned".equals(status)) {
                    unassigned++;
                }
            }
            if (tvUnassignedOrdersCount != null) tvUnassignedOrdersCount.setText(unassigned + " unassigned orders");
        });

        // Live listener for Complaints
        listenerComplaints = db.collection("complaints").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            if (tvComplaintsCount != null) tvComplaintsCount.setText(snapshots.size() + " complaints");
            int highPriority = 0;
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                String priority = doc.getString("priority");
                if ("high".equalsIgnoreCase(priority)) highPriority++;
            }
            if (tvComplaintsSub != null) tvComplaintsSub.setText(highPriority + " high priority");
        });
    }

    private void updatePendingUI(android.widget.TextView tvCount, android.widget.TextView tvSub) {
        int total = pendingPharmaciesCount + pendingRidersCount;
        if (tvCount != null) tvCount.setText(total + " pending approvals");
        if (tvSub != null) tvSub.setText(pendingPharmaciesCount + " pharmacies · " + pendingRidersCount + " riders");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerUsers != null) listenerUsers.remove();
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
