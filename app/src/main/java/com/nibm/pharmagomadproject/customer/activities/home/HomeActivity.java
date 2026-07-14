package com.nibm.pharmagomadproject.customer.activities.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.order.CartActivity;
import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineListActivity;
import com.nibm.pharmagomadproject.customer.activities.notification.NotificationsActivity;
import com.nibm.pharmagomadproject.customer.activities.order.OrderHistoryActivity;
import com.nibm.pharmagomadproject.customer.activities.pharmacy.PharmacyDetailsActivity;
import com.nibm.pharmagomadproject.customer.activities.profile.ProfileActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // ── Search bar → MedicineListActivity ──
        safeClick(R.id.searchBar, v -> openMedicineList("search", "Search medicines", ""));

        // ── Categories ──
        safeClick(R.id.catRx,        v -> openMedicineList("category", "Prescription medicines",  "rx"));
        safeClick(R.id.catFirstAid,  v -> openMedicineList("category", "First Aid",               "firstaid"));
        safeClick(R.id.catVitamins,  v -> openMedicineList("category", "Vitamins & Supplements",  "vitamins"));
        safeClick(R.id.catChronic,   v -> openMedicineList("category", "Chronic medicines",       "chronic"));
        safeClick(R.id.catBaby,      v -> openMedicineList("category", "Baby Care",               "baby"));
        safeClick(R.id.catEyeCare,   v -> openMedicineList("category", "Eye Care",                "eyecare"));
        safeClick(R.id.catDental,    v -> openMedicineList("category", "Dental",                  "dental"));
        safeClick(R.id.catOtc,       v -> openMedicineList("category", "OTC medicines",           "otc"));

        // ── Nearby pharmacies → PharmacyDetailsActivity ──
        safeClick(R.id.pharmacyMediCare, v -> openPharmacyDetails(
                "MediCare Pharmacy", "0.3 km away", "⭐ 4.8", "8:00 AM – 10:00 PM"));
        safeClick(R.id.pharmacyCityPharma, v -> openPharmacyDetails(
                "City Pharma", "0.7 km away", "⭐ 4.5", "9:00 AM – 9:00 PM"));

        // ── Notification bell ──
        safeClick(R.id.btnNotification, v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        setupBottomNav();
    }

    private void openMedicineList(String mode, String title, String query) {
        Intent intent = new Intent(this, MedicineListActivity.class);
        intent.putExtra(MedicineListActivity.EXTRA_MODE,  mode);
        intent.putExtra(MedicineListActivity.EXTRA_TITLE, title);
        intent.putExtra(MedicineListActivity.EXTRA_QUERY, query);
        intent.putExtra(MedicineListActivity.EXTRA_QUERY, query.trim().toLowerCase());
        startActivity(intent);
    }

    private void openPharmacyDetails(String name, String distance, String rating, String hours) {
        Intent intent = new Intent(this, PharmacyDetailsActivity.class);
        intent.putExtra(PharmacyDetailsActivity.EXTRA_PHARMACY_NAME,     name);
        intent.putExtra(PharmacyDetailsActivity.EXTRA_PHARMACY_DISTANCE, distance);
        intent.putExtra(PharmacyDetailsActivity.EXTRA_PHARMACY_RATING,   rating);
        intent.putExtra(PharmacyDetailsActivity.EXTRA_PHARMACY_HOURS,    hours);
        startActivity(intent);
    }

    private void safeClick(int id, android.view.View.OnClickListener l) {
        try {
            android.view.View v = findViewById(id);
            if (v != null) v.setOnClickListener(l);
        } catch (Exception ignored) {}
    }

    private void setupBottomNav() {
        safeClick(R.id.navHome,    v -> { /* already here */ });
        safeClick(R.id.navSearch,  v -> openMedicineList("search", "Search medicines", ""));
        safeClick(R.id.navCart,    v -> startActivity(new Intent(this, CartActivity.class)));
        safeClick(R.id.navOrders,  v -> startActivity(new Intent(this, OrderHistoryActivity.class)));
        safeClick(R.id.navProfile, v -> startActivity(new Intent(this, ProfileActivity.class)));
    }
}