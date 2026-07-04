package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

public class CartActivity extends AppCompatActivity {

    private int qtyMedicare = 2;
    private int qtyCity     = 1;
    private boolean mediCareInCart = true;
    private boolean cityInCart     = true;

    private TextView tvQtyMedicare, tvQtyCity, tvSubtotal, tvTotal;
    private LinearLayout medicareItem, cityItem;

    private static final int PRICE_MEDICARE = 42;
    private static final int PRICE_CITY     = 120;
    private static final int DELIVERY_FEE   = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvQtyMedicare = findViewById(R.id.tvQtyMedicare);
        tvQtyCity     = findViewById(R.id.tvQtyCity);
        tvSubtotal    = findViewById(R.id.tvSubtotal);
        tvTotal       = findViewById(R.id.tvTotal);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // MediCare qty
        findViewById(R.id.btnPlusMedicare).setOnClickListener(v -> {
            if (!mediCareInCart) return;
            qtyMedicare++;
            updateUI();
        });
        findViewById(R.id.btnMinusMedicare).setOnClickListener(v -> {
            if (!mediCareInCart) return;
            if (qtyMedicare > 1) {
                qtyMedicare--;
                updateUI();
            } else {
                // qty reaches 0 — remove item with confirmation
                showRemoveToast("Paracetamol 500mg");
                mediCareInCart = false;
                qtyMedicare = 0;
                updateUI();
            }
        });

        // City Pharma qty
        findViewById(R.id.btnPlusCity).setOnClickListener(v -> {
            if (!cityInCart) return;
            qtyCity++;
            updateUI();
        });
        findViewById(R.id.btnMinusCity).setOnClickListener(v -> {
            if (!cityInCart) return;
            if (qtyCity > 1) {
                qtyCity--;
                updateUI();
            } else {
                showRemoveToast("Vitamin C 1000mg");
                cityInCart = false;
                qtyCity = 0;
                updateUI();
            }
        });

        updateUI();

        // Proceed to payment
        MaterialButton btnProceed = findViewById(R.id.btnProceedToPayment);
        btnProceed.setOnClickListener(v -> {
            if (!mediCareInCart && !cityInCart) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, PrescriptionUploadActivity.class));
        });
    }

    private void showRemoveToast(String name) {
        Toast.makeText(this, name + " removed from cart", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        // Update quantities display
        tvQtyMedicare.setText(String.valueOf(qtyMedicare));
        tvQtyCity.setText(String.valueOf(qtyCity));

        // Calculate totals
        int subtotal = (mediCareInCart ? qtyMedicare * PRICE_MEDICARE : 0)
                     + (cityInCart     ? qtyCity     * PRICE_CITY     : 0);
        int total    = subtotal > 0 ? subtotal + DELIVERY_FEE : 0;
        tvSubtotal.setText("Rs. " + subtotal);
        tvTotal.setText("Rs. " + total);

        // Dim removed items
        View mediCareRow = findViewById(R.id.btnPlusMedicare).getRootView()
                .findViewWithTag("medicare_row");
        View cityRow = findViewById(R.id.btnPlusCity).getRootView()
                .findViewWithTag("city_row");
    }
}
