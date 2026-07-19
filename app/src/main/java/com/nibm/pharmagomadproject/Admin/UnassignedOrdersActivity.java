package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.nibm.pharmagomadproject.R;

public class UnassignedOrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unassigned_orders);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        // Order cards click to assign rider
        MaterialCardView cardOrder1 = findViewById(R.id.cardOrder1);
        MaterialCardView cardOrder2 = findViewById(R.id.cardOrder2);

        cardOrder1.setOnClickListener(v -> {
            Intent intent = new Intent(UnassignedOrdersActivity.this, AssignRiderActivity.class);
            intent.putExtra("ORDER_ID", "Order #PG-00234");
            intent.putExtra("ORDER_DETAILS", "2 pharmacies · Buddhini Perera · Colombo 3");
            startActivity(intent);
        });

        cardOrder2.setOnClickListener(v -> {
            Intent intent = new Intent(UnassignedOrdersActivity.this, AssignRiderActivity.class);
            intent.putExtra("ORDER_ID", "Order #PG-00235");
            intent.putExtra("ORDER_DETAILS", "1 pharmacy · Nimal Kumara · Kandy");
            startActivity(intent);
        });

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_delivery);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(UnassignedOrdersActivity.this, AdminDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_approvals) {
                startActivity(new Intent(UnassignedOrdersActivity.this, PendingApprovalsActivity.class));
                return true;
            } else if (itemId == R.id.nav_delivery) {
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(UnassignedOrdersActivity.this, ComplaintsActivity.class));
                return true;
            }
            return false;
        });
    }
}
