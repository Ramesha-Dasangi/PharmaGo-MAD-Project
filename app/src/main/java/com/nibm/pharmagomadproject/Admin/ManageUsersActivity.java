package com.nibm.pharmagomadproject.Admin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

public class ManageUsersActivity extends AppCompatActivity {

    private TextView tabAll, tabCustomers, tabPharmacies, tabRiders;
    private LinearLayout llAll, llCustomers, llPharmacies, llRiders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        tabAll = findViewById(R.id.tabAll);
        tabCustomers = findViewById(R.id.tabCustomers);
        tabPharmacies = findViewById(R.id.tabPharmacies);
        tabRiders = findViewById(R.id.tabRiders);

        llAll = findViewById(R.id.llAll);
        llCustomers = findViewById(R.id.llCustomers);
        llPharmacies = findViewById(R.id.llPharmacies);
        llRiders = findViewById(R.id.llRiders);

        tabAll.setOnClickListener(v -> selectTab(0));
        tabCustomers.setOnClickListener(v -> selectTab(1));
        tabPharmacies.setOnClickListener(v -> selectTab(2));
        tabRiders.setOnClickListener(v -> selectTab(3));

        MaterialButton btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(v ->
                Toast.makeText(this, "Flagged account deleted", Toast.LENGTH_SHORT).show());

        MaterialButton btnDeleteCustomer = findViewById(R.id.btnDeleteCustomer);
        btnDeleteCustomer.setOnClickListener(v ->
                Toast.makeText(this, "Flagged account deleted", Toast.LENGTH_SHORT).show());
    }

    private void selectTab(int index) {
        // Reset all tabs
        resetTab(tabAll);
        resetTab(tabCustomers);
        resetTab(tabPharmacies);
        resetTab(tabRiders);

        // Hide all sections
        llAll.setVisibility(View.GONE);
        llCustomers.setVisibility(View.GONE);
        llPharmacies.setVisibility(View.GONE);
        llRiders.setVisibility(View.GONE);

        // Activate selected
        switch (index) {
            case 0:
                activateTab(tabAll);
                llAll.setVisibility(View.VISIBLE);
                break;
            case 1:
                activateTab(tabCustomers);
                llCustomers.setVisibility(View.VISIBLE);
                break;
            case 2:
                activateTab(tabPharmacies);
                llPharmacies.setVisibility(View.VISIBLE);
                break;
            case 3:
                activateTab(tabRiders);
                llRiders.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void activateTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.tab_active_bg);
        tab.setTextColor(Color.WHITE);
        tab.setTypeface(null, Typeface.BOLD);
    }

    private void resetTab(TextView tab) {
        tab.setBackground(null);
        tab.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
        tab.setTypeface(null, Typeface.NORMAL);
    }
}
