package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.nibm.pharmagomadproject.R;

public class OrderHistoryActivity extends AppCompatActivity {

    private TextView tabAll, tabActive, tabDelivered, tabCancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);
        getSupportActionBar().hide();

        tabAll       = findViewById(R.id.tabAll);
        tabActive    = findViewById(R.id.tabActive);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabCancelled = findViewById(R.id.tabCancelled);

        // Tab clicks
        tabAll.setOnClickListener(v       -> selectTab(tabAll));
        tabActive.setOnClickListener(v    -> selectTab(tabActive));
        tabDelivered.setOnClickListener(v -> selectTab(tabDelivered));
        tabCancelled.setOnClickListener(v -> selectTab(tabCancelled));

        // Bottom nav
        setupBottomNav();
    }

    private void selectTab(TextView selected) {
        TextView[] tabs = {tabAll, tabActive, tabDelivered, tabCancelled};
        int primary = getResources().getColor(R.color.pg_primary, null);
        int sub     = getResources().getColor(R.color.pg_sub, null);

        for (TextView tab : tabs) {
            boolean isSelected = tab == selected;
            tab.setBackgroundResource(isSelected
                    ? R.drawable.bg_tab_selected
                    : R.drawable.bg_tab_unselected);
            tab.setTextColor(isSelected ? primary : sub);
            tab.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.navCart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
            finish();
        });
        findViewById(R.id.navOrders).setOnClickListener(v -> {
            // already here
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
}
