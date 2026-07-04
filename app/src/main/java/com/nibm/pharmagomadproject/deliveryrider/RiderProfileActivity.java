package com.nibm.pharmagomadproject.deliveryrider;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.LoginActivity;
import com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity;

public class RiderProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupExpandableItems();
        setupBottomNav();
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
