package com.nibm.pharmagomadproject.pharmacyowner;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.auth.LoginActivity;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private BottomNavigationView bottomNavigation;
    private ImageView btnClearNotifications;
    private TextView txtTitle;

    private ArrayList<NotificationModel> notificationList;
    private NotificationAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String ownerId = "";
    private String activeFilterType = "all"; // "all", "stock", "inventory", "order"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            ownerId = user.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Log.d("OWNER_UID", ownerId);

        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnClearNotifications = findViewById(R.id.btnClearNotifications);
        txtTitle = findViewById(R.id.txtTitle);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Make Title Clickable for Filtering
        txtTitle.setClickable(true);
        txtTitle.setFocusable(true);
        txtTitle.setOnClickListener(v -> showFilterOptionsDialog());

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        recyclerNotifications.setAdapter(adapter);

        // Click individual item to trigger Mark Read / Delete dialog
        adapter.setOnNotificationClickListener(this::showNotificationActionDialog);

        loadNotifications();

        btnClearNotifications.setOnClickListener(v -> showClearDialog());

        // Navigation Highlight
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrdersActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(this, com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ownerId.isEmpty()) {
            loadNotifications();
        }
    }

    private void loadNotifications() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("notifications")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("COUNT", "Documents = " + queryDocumentSnapshots.size());
                    notificationList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        NotificationModel model = document.toObject(NotificationModel.class);
                        if (model != null) {
                            model.setNotificationId(document.getId());

                            // Client-side filter for type
                            if (!activeFilterType.equals("all")) {
                                String modelType = model.getType() != null ? model.getType().toLowerCase() : "";
                                if (!modelType.equals(activeFilterType)) {
                                    continue;
                                }
                            }
                            notificationList.add(model);
                        }
                    }

                    // Sort by timestamp descending (newest first) — no Firestore index needed
                    Collections.sort(notificationList,
                            (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("NOTIFICATION_ERROR", e.getMessage(), e);
                    Toast.makeText(NotificationsActivity.this, "Error loading: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showFilterOptionsDialog() {
        String[] options = {"All Notifications", "Stock Alerts", "Inventory Updates", "Orders"};
        int checkedItem = 0;
        if ("stock".equals(activeFilterType)) checkedItem = 1;
        else if ("inventory".equals(activeFilterType)) checkedItem = 2;
        else if ("order".equals(activeFilterType)) checkedItem = 3;

        new AlertDialog.Builder(this)
                .setTitle("Filter Notifications")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            activeFilterType = "all";
                            txtTitle.setText("Notifications");
                            break;
                        case 1:
                            activeFilterType = "stock";
                            txtTitle.setText("Stock Alerts");
                            break;
                        case 2:
                            activeFilterType = "inventory";
                            txtTitle.setText("Inventory Log");
                            break;
                        case 3:
                            activeFilterType = "order";
                            txtTitle.setText("Order Alerts");
                            break;
                    }
                    dialog.dismiss();
                    loadNotifications();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showNotificationActionDialog(NotificationModel model) {
        ArrayList<String> actions = new ArrayList<>();
        if (!model.isRead()) {
            actions.add("Mark as Read");
        }
        actions.add("Delete Notification");

        String[] actionArr = actions.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Notification Options")
                .setItems(actionArr, (dialog, which) -> {
                    String selected = actionArr[which];
                    if ("Mark as Read".equals(selected)) {
                        markNotificationAsRead(model.getNotificationId());
                    } else if ("Delete Notification".equals(selected)) {
                        deleteNotification(model.getNotificationId());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markNotificationAsRead(String id) {
        if (!NetworkUtils.isNetworkAvailable(this)) return;

        db.collection("notifications").document(id)
                .update("read", true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteNotification(String id) {
        if (!NetworkUtils.isNetworkAvailable(this)) return;

        db.collection("notifications").document(id)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showClearDialog() {
        boolean hasUnread = false;
        for (NotificationModel model : notificationList) {
            if (!model.isRead()) {
                hasUnread = true;
                break;
            }
        }
        if (!hasUnread) {
            Toast.makeText(this, "No unread notifications to clear", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear Notifications")
                .setMessage("Mark all matching notifications as read?")
                .setPositiveButton("Yes", (dialog, which) -> clearAllNotifications())
                .setNegativeButton("No", null)
                .show();
    }

    private void clearAllNotifications() {
        if (!NetworkUtils.isNetworkAvailable(this)) return;

        db.collection("notifications")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    int count = 0;
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        batch.update(document.getReference(), "read", true);
                        count++;
                    }
                    if (count > 0) {
                        batch.commit()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(NotificationsActivity.this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
                                    loadNotifications();
                                })
                                .addOnFailureListener(e -> Toast.makeText(NotificationsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}