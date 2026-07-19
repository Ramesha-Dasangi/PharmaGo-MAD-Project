package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nibm.pharmagomadproject.R;

import java.util.Calendar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity;
import com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtGreeting;
    private TextView txtPharmacy;
    private ImageView imgNotification;

    private TextView txtStock;
    private TextView txtLowStockMessage;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Views
        txtGreeting = findViewById(R.id.txtGreeting);
        txtPharmacy = findViewById(R.id.txtPharmacy);
        imgNotification = findViewById(R.id.imgNotification);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        txtStock = findViewById(R.id.txtStock);
        txtLowStockMessage = findViewById(R.id.txtLowStockMessage);

        db = FirebaseFirestore.getInstance();

        loadLowStockCount();

        // Load pharmacy name from Firestore
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            db.collection("pharmacies")
                    .whereEqualTo("ownerId", uid)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snaps -> {
                        if (!snaps.isEmpty()) {
                            String name = snaps.getDocuments().get(0).getString("name");
                            if (name != null) txtPharmacy.setText(name);
                        }
                    });
        }

        // Greeting
        setGreeting();

        // Welcome Message
        Toast.makeText(this,
                "Welcome to Pharmacy Dashboard",
                Toast.LENGTH_SHORT).show();

        // Notification Bell Click
        imgNotification.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DashboardActivity.this,
                    NotificationsActivity.class);

            startActivity(intent);
        });


        // Home Selected
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.nav_home) {

                    return true;

                } else if (id == R.id.nav_orders) {

                    startActivity(new Intent(
                            DashboardActivity.this,
                            OrdersActivity.class));

                    return true;

                } else if (id == R.id.nav_inventory) {

                    startActivity(new Intent(DashboardActivity.this,
                            InventoryActivity.class));
                    finish();
                    return true;

                } else if (id == R.id.nav_reports) {

                    startActivity(new Intent(DashboardActivity.this, SalesReportActivity.class));
                    finish();
                    return true;

                } else if (id == R.id.nav_profile) {

                    startActivity(new Intent(
                            DashboardActivity.this,
                            ProfileActivity.class));

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

        loadLowStockCount();
    }

    // Greeting according to time
    private void setGreeting() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            txtGreeting.setText("Good Morning");
        } else if (hour >= 12 && hour < 17) {
            txtGreeting.setText("Good Afternoon");
        } else if (hour >= 17 && hour < 21) {
            txtGreeting.setText("Good Evening");
        } else {
            txtGreeting.setText("Good Night");
        }
    }

    private void loadLowStockCount() {

        db.collection("medicines")
                .whereLessThanOrEqualTo("stock", 10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int count = queryDocumentSnapshots.size();

                    txtStock.setText(String.valueOf(count));

                    if (count == 0) {

                        txtLowStockMessage.setText(
                                "All medicines are sufficiently stocked"
                        );

                    } else {

                        txtLowStockMessage.setText(
                                count + " medicines need restocking"
                        );

                    }

                })
                .addOnFailureListener(e -> {

                    txtStock.setText("0");

                    txtLowStockMessage.setText("");

                });

    }

}