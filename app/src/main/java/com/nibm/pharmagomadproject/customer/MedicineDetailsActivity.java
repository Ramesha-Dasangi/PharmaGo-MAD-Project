package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

public class MedicineDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_details);
        getSupportActionBar().hide();

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Cart icon in top bar
        findViewById(R.id.btnCart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));

        // ✅ CardView widihata fix karala
        CardView pharmMedicare   = findViewById(R.id.pharmacyMedicare);
        CardView pharmCityPharma = findViewById(R.id.pharmacyCityPharma);
        CardView pharmHealthPlus = findViewById(R.id.pharmacyHealthPlus);

        pharmMedicare.setOnClickListener(v ->
                highlightPharmacy(pharmMedicare, pharmCityPharma, pharmHealthPlus));
        pharmCityPharma.setOnClickListener(v ->
                highlightPharmacy(pharmCityPharma, pharmMedicare, pharmHealthPlus));
        pharmHealthPlus.setOnClickListener(v ->
                highlightPharmacy(pharmHealthPlus, pharmMedicare, pharmCityPharma));

        // Add to cart
        MaterialButton btnAddToCart = findViewById(R.id.btnAddToCart);
        btnAddToCart.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Added to cart!",
                    android.widget.Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CartActivity.class));
        });

        // Order now
        MaterialButton btnOrderNow = findViewById(R.id.btnOrderNow);
        btnOrderNow.setOnClickListener(v ->
                startActivity(new Intent(this, PrescriptionUploadActivity.class)));
    }

    private void highlightPharmacy(CardView selected, CardView a, CardView b) {
        int green  = getResources().getColor(R.color.pg_primary_light, null);
        int normal = getResources().getColor(R.color.pg_card, null);
        selected.setCardBackgroundColor(green);
        a.setCardBackgroundColor(normal);
        b.setCardBackgroundColor(normal);
    }
}