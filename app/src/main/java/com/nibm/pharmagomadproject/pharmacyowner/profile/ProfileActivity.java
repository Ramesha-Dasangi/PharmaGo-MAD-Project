package com.nibm.pharmagomadproject.pharmacyowner.profile;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.auth.LoginActivity;
import com.nibm.pharmagomadproject.pharmacyowner.DashboardActivity;
import com.nibm.pharmagomadproject.pharmacyowner.InventoryActivity;
import com.nibm.pharmagomadproject.pharmacyowner.OrdersActivity;
import com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private Switch switchNotification;
    private LinearLayout txtChangePassword;
    private LinearLayout txtLogout;

    // Profile display views
    private TextView txtName;
    private TextView txtLicense;
    private TextView txtAddress;
    private TextView txtHours;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase init
        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        bottomNavigation   = findViewById(R.id.bottomNavigation);
        switchNotification = findViewById(R.id.switchNotification);
        txtChangePassword  = findViewById(R.id.txtChangePassword);
        txtLogout          = findViewById(R.id.txtLogout);

        // Profile info views
        txtName    = findViewById(R.id.txtName);
        txtLicense = findViewById(R.id.txtLicense);
        txtAddress = findViewById(R.id.txtAddress);
        txtHours   = findViewById(R.id.txtHours);

        // Load real pharmacy data from Firestore
        loadPharmacyProfile();

        // Notification Switch SharedPreferences persistence
        SharedPreferences prefs = getSharedPreferences("PharmaPrefs", MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        switchNotification.setChecked(notificationsEnabled);

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            if (isChecked) {
                Toast.makeText(ProfileActivity.this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Change Password
        txtChangePassword.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class)));

        // Logout
        txtLogout.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Bottom Navigation
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(ProfileActivity.this, OrdersActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(ProfileActivity.this, InventoryActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(ProfileActivity.this, SalesReportActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    // ═══════════════════════════════════════════════════
    //  Load real pharmacy profile from Firestore
    // ═══════════════════════════════════════════════════
    private void loadPharmacyProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("pharmacies")
                .whereEqualTo("ownerId", uid)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots == null || snapshots.isEmpty()) return;

                    com.google.firebase.firestore.DocumentSnapshot doc =
                            snapshots.getDocuments().get(0);

                    // Pharmacy name
                    String name = doc.getString("name");
                    if (txtName != null && name != null && !name.isEmpty()) {
                        txtName.setText(name);
                    }

                    // License number
                    String licenseNo = doc.getString("licenseNo");
                    if (txtLicense != null) {
                        if (licenseNo != null && !licenseNo.isEmpty()) {
                            txtLicense.setText("License : " + licenseNo);
                        } else {
                            txtLicense.setText("License : —");
                        }
                    }

                    // Address
                    String address = doc.getString("address");
                    if (txtAddress != null) {
                        if (address != null && !address.isEmpty()) {
                            txtAddress.setText(address);
                        } else {
                            txtAddress.setText("—");
                        }
                    }

                    // Operating hours (no field in schema — keep default or show owner email)
                    String hours = doc.getString("hours");
                    if (txtHours != null && hours != null && !hours.isEmpty()) {
                        txtHours.setText(hours);
                    }
                    // else: leaves the default XML text "8:00 AM - 10:00 PM"
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}