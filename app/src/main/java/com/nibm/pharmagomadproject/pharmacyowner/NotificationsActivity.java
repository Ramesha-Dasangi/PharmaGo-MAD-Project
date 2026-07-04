package com.nibm.pharmagomadproject.pharmacyowner;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView recyclerNotifications;
    BottomNavigationView bottomNavigation;

    ArrayList<NotificationModel> notificationList;
    NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        recyclerNotifications.setLayoutManager(
                new LinearLayoutManager(this));

        notificationList = new ArrayList<>();

        // Sample Notifications

        notificationList.add(new NotificationModel(
                "New Order Received",
                "Order #ORD001 from Kasun Perera",
                "2 mins ago",
                R.color.white));

        notificationList.add(new NotificationModel(
                "Low Stock Alert",
                "Paracetamol 500mg is running low",
                "10 mins ago",
                R.color.white));

        notificationList.add(new NotificationModel(
                "Prescription Uploaded",
                "Customer uploaded a prescription",
                "30 mins ago",
                R.color.white));

        notificationList.add(new NotificationModel(
                "Order Completed",
                "Order #ORD001 completed successfully",
                "1 hour ago",
                R.color.white));

        adapter = new NotificationAdapter(this, notificationList);

        recyclerNotifications.setAdapter(adapter);

        // Bottom Navigation

        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                finish();
                return true;

            } else if (id == R.id.nav_orders) {
                // startActivity(new Intent(this, OrdersActivity.class));
                return true;

            } else if (id == R.id.nav_inventory) {
                // startActivity(new Intent(this, InventoryActivity.class));
                return true;

            } else if (id == R.id.nav_reports) {
                // startActivity(new Intent(this, ReportsActivity.class));
                return true;

            } else if (id == R.id.nav_profile) {
                // startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });

    }
}