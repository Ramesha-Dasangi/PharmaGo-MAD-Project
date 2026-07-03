package com.nibm.pharmagomadproject.pharmacyowner.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nibm.pharmagomadproject.R;

import com.nibm.pharmagomadproject.pharmacyowner.DashboardActivity;
import com.nibm.pharmagomadproject.pharmacyowner.InventoryActivity;
import com.nibm.pharmagomadproject.pharmacyowner.OrdersActivity;
import com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    private Switch switchNotification;

    private LinearLayout txtChangePassword;
    private LinearLayout txtLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ===========================
        // Initialize Views
        // ===========================

        bottomNavigation = findViewById(R.id.bottomNavigation);

        switchNotification = findViewById(R.id.switchNotification);

        txtChangePassword = findViewById(R.id.txtChangePassword);
        txtLogout = findViewById(R.id.txtLogout);

        // ===========================
        // Notification Switch
        // ===========================

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                Toast.makeText(
                        ProfileActivity.this,
                        "Notifications Enabled",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        ProfileActivity.this,
                        "Notifications Disabled",
                        Toast.LENGTH_SHORT
                ).show();

            }

        });

        // ===========================
        // Change Password
        // ===========================

        txtChangePassword.setOnClickListener(v ->

                Toast.makeText(
                        ProfileActivity.this,
                        "Change Password Clicked",
                        Toast.LENGTH_SHORT
                ).show()

        );

        // ===========================
        // Logout
        // ===========================

        txtLogout.setOnClickListener(v -> {

            Toast.makeText(
                    ProfileActivity.this,
                    "Logged Out Successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finishAffinity();

        });

        // ===========================
        // Bottom Navigation
        // ===========================

        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                startActivity(new Intent(
                        ProfileActivity.this,
                        DashboardActivity.class));

                finish();
                return true;

            }

            else if (id == R.id.nav_orders) {

                startActivity(new Intent(
                        ProfileActivity.this,
                        OrdersActivity.class));

                finish();
                return true;

            }

            else if (id == R.id.nav_inventory) {

                startActivity(new Intent(
                        ProfileActivity.this,
                        InventoryActivity.class));

                finish();
                return true;

            }

            else if (id == R.id.nav_reports) {

                startActivity(new Intent(
                        ProfileActivity.this,
                        SalesReportActivity.class));

                finish();
                return true;

            }

            else if (id == R.id.nav_profile) {

                return true;

            }

            return false;

        });

    }
}