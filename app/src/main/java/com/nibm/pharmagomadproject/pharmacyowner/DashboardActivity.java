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

import com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity;
import com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtGreeting;
    private TextView txtPharmacy;
    private ImageView imgNotification;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Views
        txtGreeting = findViewById(R.id.txtGreeting);
        txtPharmacy = findViewById(R.id.txtPharmacy);
        imgNotification = findViewById(R.id.imgNotification);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Pharmacy Name
        txtPharmacy.setText("City Pharma Express");

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
}