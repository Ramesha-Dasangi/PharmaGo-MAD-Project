package com.example.testinterfacejava;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ComplaintsActivity extends AppCompatActivity {

    private TextView tabOpen, tabResolved;
    private LinearLayout llOpen, llResolved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        tabOpen = findViewById(R.id.tabOpen);
        tabResolved = findViewById(R.id.tabResolved);
        llOpen = findViewById(R.id.llOpen);
        llResolved = findViewById(R.id.llResolved);

        tabOpen.setOnClickListener(v -> selectTab(true));
        tabResolved.setOnClickListener(v -> selectTab(false));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_complaints);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ComplaintsActivity.this, AdminDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_approvals) {
                startActivity(new Intent(ComplaintsActivity.this, PendingApprovalsActivity.class));
                return true;
            } else if (itemId == R.id.nav_delivery) {
                startActivity(new Intent(ComplaintsActivity.this, UnassignedOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                return true;
            }
            return false;
        });
    }

    private void selectTab(boolean isOpen) {
        if (isOpen) {
            tabOpen.setBackgroundResource(R.drawable.tab_active_bg);
            tabOpen.setTextColor(Color.WHITE);
            tabOpen.setTypeface(null, Typeface.BOLD);
            
            tabResolved.setBackground(null);
            tabResolved.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabResolved.setTypeface(null, Typeface.NORMAL);
            
            llOpen.setVisibility(View.VISIBLE);
            llResolved.setVisibility(View.GONE);
        } else {
            tabResolved.setBackgroundResource(R.drawable.tab_active_bg);
            tabResolved.setTextColor(Color.WHITE);
            tabResolved.setTypeface(null, Typeface.BOLD);
            
            tabOpen.setBackground(null);
            tabOpen.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabOpen.setTypeface(null, Typeface.NORMAL);
            
            llResolved.setVisibility(View.VISIBLE);
            llOpen.setVisibility(View.GONE);
        }
    }
}
