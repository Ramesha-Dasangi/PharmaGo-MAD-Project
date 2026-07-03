package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Search bar
        findViewById(R.id.searchBar).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));

        // Categories — for loop remove karala individually set karanna
        findViewById(R.id.catRx).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));
        findViewById(R.id.catFirstAid).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));
        findViewById(R.id.catVitamins).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));
        findViewById(R.id.catChronic).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));
        findViewById(R.id.catBaby).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));
        findViewById(R.id.catEyeCare).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));
        findViewById(R.id.catDental).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));
        findViewById(R.id.catOtc).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));

        // Nearby pharmacies
        findViewById(R.id.pharmacyMediCare).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));
        findViewById(R.id.pharmacyCityPharma).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));

        // Notification bell
        findViewById(R.id.btnNotification).setOnClickListener(v ->
                android.widget.Toast.makeText(this, "No new notifications",
                        android.widget.Toast.LENGTH_SHORT).show());

        setupBottomNav();
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> { });
        findViewById(R.id.navSearch).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineDetailsActivity.class)));
        findViewById(R.id.navCart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));
        findViewById(R.id.navOrders).setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}