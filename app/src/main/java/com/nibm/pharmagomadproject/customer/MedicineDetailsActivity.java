package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

public class MedicineDetailsActivity extends AppCompatActivity {

    private CardView selectedPharmacy = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_details);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Read intent extras from MedicineListActivity
        String name     = getIntent().getStringExtra("medicine_name");
        int    price    = getIntent().getIntExtra("medicine_price", 42);
        String type     = getIntent().getStringExtra("medicine_type");
        String category = getIntent().getStringExtra("medicine_category");
        String pharmacy = getIntent().getStringExtra("medicine_pharmacy");

        // Update UI with medicine data if available
        if (name != null) {
            TextView tvName = findViewById(R.id.tvMedicineName);
            if (tvName != null) tvName.setText(name);
        }
        if (category != null) {
            TextView tvManuf = findViewById(R.id.tvManufacturer);
            if (tvManuf != null) tvManuf.setText(category);
        }

        // Price comparison — update best price pharmacy name + price
        if (pharmacy != null) {
            TextView tvBestPharmacy = findViewById(R.id.tvPharmacy1Name);
            if (tvBestPharmacy != null) tvBestPharmacy.setText(pharmacy);
            TextView tvBestPrice = findViewById(R.id.tvPharmacy1Price);
            if (tvBestPrice != null) tvBestPrice.setText("Rs. " + price);
        }

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Cart icon
        findViewById(R.id.btnCart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));

        // Pharmacy price comparison cards
        CardView pharmMedicare   = findViewById(R.id.pharmacyMedicare);
        CardView pharmCityPharma = findViewById(R.id.pharmacyCityPharma);
        CardView pharmHealthPlus = findViewById(R.id.pharmacyHealthPlus);

        if (pharmMedicare != null && pharmCityPharma != null && pharmHealthPlus != null) {
            pharmMedicare.setOnClickListener(v ->
                    highlightPharmacy(pharmMedicare, pharmCityPharma, pharmHealthPlus));
            pharmCityPharma.setOnClickListener(v ->
                    highlightPharmacy(pharmCityPharma, pharmMedicare, pharmHealthPlus));
            pharmHealthPlus.setOnClickListener(v ->
                    highlightPharmacy(pharmHealthPlus, pharmMedicare, pharmCityPharma));

            // Default — pre-select first pharmacy
            highlightPharmacy(pharmMedicare, pharmCityPharma, pharmHealthPlus);
        }

        // Add to cart
        MaterialButton btnAddToCart = findViewById(R.id.btnAddToCart);
        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> {
                String medName = name != null ? name : "Medicine";
                Toast.makeText(this, medName + " added to cart!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, CartActivity.class));
            });
        }

        // Order now → prescription upload (for Rx) or cart (for OTC)
        MaterialButton btnOrderNow = findViewById(R.id.btnOrderNow);
        if (btnOrderNow != null) {
            btnOrderNow.setOnClickListener(v -> {
                if ("Prescription".equals(type) || "Rx".equals(type)) {
                    startActivity(new Intent(this, PrescriptionUploadActivity.class));
                } else {
                    startActivity(new Intent(this, CartActivity.class));
                }
            });
        }
    }

    private void highlightPharmacy(CardView selected, CardView a, CardView b) {
        int green  = getResources().getColor(R.color.pg_primary_light, null);
        int normal = getResources().getColor(R.color.pg_card, null);
        selected.setCardBackgroundColor(green);
        a.setCardBackgroundColor(normal);
        b.setCardBackgroundColor(normal);
        selectedPharmacy = selected;
    }
}
