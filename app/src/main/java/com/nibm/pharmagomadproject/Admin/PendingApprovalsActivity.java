package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

public class PendingApprovalsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approvals);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MaterialButton btnReview1 = findViewById(R.id.btnReview1);
        btnReview1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PendingApprovalsActivity.this, ReviewApplicationActivity.class);
                startActivity(intent);
            }
        });

        MaterialButton btnReviewRider1 = findViewById(R.id.btnReviewRider1);
        btnReviewRider1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PendingApprovalsActivity.this, ReviewRiderActivity.class);
                startActivity(intent);
            }
        });

        TextView tabPharmacies = findViewById(R.id.tabPharmacies);
        TextView tabRiders = findViewById(R.id.tabRiders);
        LinearLayout llPharmacies = findViewById(R.id.llPharmacies);
        LinearLayout llRiders = findViewById(R.id.llRiders);

        tabPharmacies.setOnClickListener(v -> {
            tabPharmacies.setBackgroundResource(R.drawable.tab_active_bg);
            tabPharmacies.setTextColor(Color.WHITE);
            tabPharmacies.setTypeface(null, android.graphics.Typeface.BOLD);

            tabRiders.setBackground(null);
            tabRiders.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabRiders.setTypeface(null, android.graphics.Typeface.NORMAL);

            llPharmacies.setVisibility(View.VISIBLE);
            llRiders.setVisibility(View.GONE);
        });

        tabRiders.setOnClickListener(v -> {
            tabRiders.setBackgroundResource(R.drawable.tab_active_bg);
            tabRiders.setTextColor(Color.WHITE);
            tabRiders.setTypeface(null, android.graphics.Typeface.BOLD);

            tabPharmacies.setBackground(null);
            tabPharmacies.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabPharmacies.setTypeface(null, android.graphics.Typeface.NORMAL);

            llPharmacies.setVisibility(View.GONE);
            llRiders.setVisibility(View.VISIBLE);
        });

        MaterialButton btnReview2 = findViewById(R.id.btnReview2);
        btnReview2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PendingApprovalsActivity.this, ReviewApplicationActivity.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_approvals);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(PendingApprovalsActivity.this, AdminDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_approvals) {
                return true;
            } else if (itemId == R.id.nav_delivery) {
                startActivity(new Intent(PendingApprovalsActivity.this, UnassignedOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(PendingApprovalsActivity.this, ComplaintsActivity.class));
                return true;
            }
            return false;
        });
    }
}
