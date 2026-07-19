package com.nibm.pharmagomadproject.deliveryrider;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.auth.LoginActivity;

public class RiderProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvAvatarInitials, tvProfileName, tvProfileVehicle;
    private TextView tvExpandName, tvExpandPhone, tvExpandVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileVehicle = findViewById(R.id.tvProfileVehicle);
        tvExpandName = findViewById(R.id.tvExpandName);
        tvExpandPhone = findViewById(R.id.tvExpandPhone);
        tvExpandVehicle = findViewById(R.id.tvExpandVehicle);

        setupExpandableItems();
        setupBottomNav();

        fetchUserData();
    }

    private void fetchUserData() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            // Fetch basic info from users collection
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    String phone = doc.getString("phone");

                    if (name != null) {
                        if (tvProfileName != null) tvProfileName.setText(name);
                        if (tvExpandName != null) tvExpandName.setText(name);

                        // Set Initials
                        String[] parts = name.trim().split("\\s+");
                        String initials = "";
                        if (parts.length > 0) initials += parts[0].charAt(0);
                        if (parts.length > 1) initials += parts[1].charAt(0);
                        if (tvAvatarInitials != null) tvAvatarInitials.setText(initials.toUpperCase());
                    }

                    if (phone != null && tvExpandPhone != null) {
                        tvExpandPhone.setText(phone);
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            });

            // Fetch vehicle info from riders collection
            db.collection("riders").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String vNumber = doc.getString("vehicleReg");
                    String vType = doc.getString("vehicleType");
                    String vehicleStr = (vNumber != null ? vNumber : "Unknown") + " · " + (vType != null ? vType : "Unknown");
                    
                    if (tvProfileVehicle != null) tvProfileVehicle.setText(vehicleStr);
                    if (tvExpandVehicle != null) tvExpandVehicle.setText(vehicleStr);
                }
            });
        }
    }

    private void setupExpandableItems() {
        // Personal Info
        setupExpandable(
                R.id.rowPersonalInfo,
                R.id.expandPersonalInfo,
                R.id.chevronPersonal
        );

        // Earnings
        setupExpandable(
                R.id.rowEarnings,
                R.id.expandEarnings,
                R.id.chevronEarnings
        );

        // Notifications
        setupExpandable(
                R.id.rowNotifications,
                R.id.expandNotifications,
                R.id.chevronNotif
        );

        // Log out
        View itemLogout = findViewById(R.id.itemLogout);
        if (itemLogout != null) {
            itemLogout.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    /** Toggles the expandable section and rotates the chevron */
    private void setupExpandable(int rowId, int expandId, int chevronId) {
        View row = findViewById(rowId);
        LinearLayout expand = findViewById(expandId);
        ImageView chevron = findViewById(chevronId);

        if (row == null || expand == null || chevron == null) return;

        final boolean[] isExpanded = {false};

        row.setOnClickListener(v -> {
            isExpanded[0] = !isExpanded[0];

            if (isExpanded[0]) {
                expand.setVisibility(View.VISIBLE);
                // Rotate chevron down (90°)
                ObjectAnimator.ofFloat(chevron, "rotation", 0f, 90f)
                        .setDuration(200).start();
            } else {
                expand.setVisibility(View.GONE);
                // Rotate chevron back to right
                ObjectAnimator.ofFloat(chevron, "rotation", 90f, 0f)
                        .setDuration(200).start();
            }
        });
    }

    private void setupBottomNav() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent i = new Intent(this, RiderDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
        View navMap = findViewById(R.id.navMap);
        if (navMap != null) navMap.setOnClickListener(v ->
                startActivity(new Intent(this, LiveMapActivity.class)));

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, DeliveryHistoryActivity.class)));

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) navProfile.setOnClickListener(v -> { /* already here */ });
    }
}
