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

        // Search bar → MedicineListActivity (search mode)
        findViewById(R.id.searchBar).setOnClickListener(v ->
                openMedicineList("search", "Search medicines", ""));

        // Categories
        findViewById(R.id.catRx).setOnClickListener(v ->
                openMedicineList("category", "Prescription medicines", "rx"));
        findViewById(R.id.catFirstAid).setOnClickListener(v ->
                openMedicineList("category", "First Aid", "firstaid"));
        findViewById(R.id.catVitamins).setOnClickListener(v ->
                openMedicineList("category", "Vitamins & Supplements", "vitamins"));
        findViewById(R.id.catChronic).setOnClickListener(v ->
                openMedicineList("category", "Chronic medicines", "chronic"));
        findViewById(R.id.catBaby).setOnClickListener(v ->
                openMedicineList("category", "Baby Care", "baby"));
        findViewById(R.id.catEyeCare).setOnClickListener(v ->
                openMedicineList("category", "Eye Care", "eyecare"));
        findViewById(R.id.catDental).setOnClickListener(v ->
                openMedicineList("category", "Dental", "dental"));
        findViewById(R.id.catOtc).setOnClickListener(v ->
                openMedicineList("category", "OTC medicines", "otc"));

        // Nearby pharmacies → MedicineListActivity (pharmacy mode)
        findViewById(R.id.pharmacyMediCare).setOnClickListener(v ->
                openMedicineList("pharmacy", "MediCare Pharmacy", "MediCare Pharmacy"));
        findViewById(R.id.pharmacyCityPharma).setOnClickListener(v ->
                openMedicineList("pharmacy", "City Pharma", "City Pharma"));

        // Notification bell → NotificationsActivity
        findViewById(R.id.btnNotification).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        setupBottomNav();
    }

    private void openMedicineList(String mode, String title, String query) {
        Intent intent = new Intent(this, MedicineListActivity.class);
        intent.putExtra(MedicineListActivity.EXTRA_MODE,  mode);
        intent.putExtra(MedicineListActivity.EXTRA_TITLE, title);
        intent.putExtra(MedicineListActivity.EXTRA_QUERY, query);
        startActivity(intent);
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> { });
        findViewById(R.id.navSearch).setOnClickListener(v ->
                openMedicineList("search", "Search medicines", ""));
        findViewById(R.id.navCart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));
        findViewById(R.id.navOrders).setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}
