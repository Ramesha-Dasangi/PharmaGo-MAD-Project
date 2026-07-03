package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

public class CartActivity extends AppCompatActivity {

    private int qtyMedicare = 2;
    private int qtyCity     = 1;

    private TextView tvQtyMedicare, tvQtyCity, tvSubtotal, tvTotal;

    private static final int PRICE_MEDICARE = 42;  // per unit
    private static final int PRICE_CITY     = 120; // per unit
    private static final int DELIVERY_FEE   = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        getSupportActionBar().hide();

        tvQtyMedicare = findViewById(R.id.tvQtyMedicare);
        tvQtyCity     = findViewById(R.id.tvQtyCity);
        tvSubtotal    = findViewById(R.id.tvSubtotal);
        tvTotal       = findViewById(R.id.tvTotal);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // MediCare qty controls
        findViewById(R.id.btnPlusMedicare).setOnClickListener(v -> {
            qtyMedicare++;
            updateUI();
        });
        findViewById(R.id.btnMinusMedicare).setOnClickListener(v -> {
            if (qtyMedicare > 1) { qtyMedicare--; updateUI(); }
        });

        // City Pharma qty controls
        findViewById(R.id.btnPlusCity).setOnClickListener(v -> {
            qtyCity++;
            updateUI();
        });
        findViewById(R.id.btnMinusCity).setOnClickListener(v -> {
            if (qtyCity > 1) { qtyCity--; updateUI(); }
        });

        updateUI();

        // Proceed to payment
        MaterialButton btnProceed = findViewById(R.id.btnProceedToPayment);
        btnProceed.setOnClickListener(v ->
                startActivity(new Intent(this, PrescriptionUploadActivity.class)));
    }

    private void updateUI() {
        tvQtyMedicare.setText(String.valueOf(qtyMedicare));
        tvQtyCity.setText(String.valueOf(qtyCity));
        int subtotal = (qtyMedicare * PRICE_MEDICARE) + (qtyCity * PRICE_CITY);
        int total    = subtotal + DELIVERY_FEE;
        tvSubtotal.setText("Rs. " + subtotal);
        tvTotal.setText("Rs. " + total);
    }
}