package com.nibm.pharmagomadproject.customer.activities.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.nibm.pharmagomadproject.R;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    // State
    private String selectedMethod = "cod"; // "cod" or "card"

    // Views
    private LinearLayout optionCOD, optionCard;
    private CardView cardDetailsSection;
    private ImageView    radioCOD, radioCard;
    private TextInputEditText etCardNumber, etExpiry, etCvv;
    private TextView tvPaySubtotal, tvPayTotal;

    // Firebase
    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;

    // Order data from CartActivity
    private int subtotal    = 204;
    private int deliveryFee = 100;
    private int total       = 304;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // Bind views
        optionCOD          = findViewById(R.id.optionCOD);
        optionCard         = findViewById(R.id.optionCard);
        cardDetailsSection = findViewById(R.id.cardDetailsSection);
        radioCOD           = findViewById(R.id.radioCOD);
        radioCard          = findViewById(R.id.radioCard);
        tvPaySubtotal      = findViewById(R.id.tvPaySubtotal);
        tvPayTotal         = findViewById(R.id.tvPayTotal);
        etCardNumber       = findViewById(R.id.etCardNumber);
        etExpiry           = findViewById(R.id.etExpiry);
        etCvv              = findViewById(R.id.etCvv);

        // Get totals from intent
        subtotal    = getIntent().getIntExtra("subtotal",    204);
        deliveryFee = getIntent().getIntExtra("deliveryFee", 100);
        total       = getIntent().getIntExtra("total",       304);

        tvPaySubtotal.setText("Rs. " + subtotal);
        tvPayTotal.setText("Rs. " + total);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Payment method selection
        // Default: COD selected
        selectCOD();

        optionCOD.setOnClickListener(v -> selectCOD());
        optionCard.setOnClickListener(v -> selectCard());

        // Place order button
        MaterialButton btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    // Select COD
    private void selectCOD() {
        selectedMethod = "cod";

        // Highlight COD
        optionCOD.setBackgroundResource(R.drawable.bg_selected_option);
        radioCOD.setImageResource(R.drawable.ic_check_circle);
        radioCOD.setColorFilter(getResources().getColor(R.color.pg_primary, null));

        // Unhighlight Card
        optionCard.setBackgroundResource(R.drawable.bg_unselected_option);
        radioCard.setImageResource(R.drawable.ic_circle_dashed);
        radioCard.setColorFilter(getResources().getColor(R.color.pg_sub, null));

        // Hide card details
        cardDetailsSection.setVisibility(View.GONE);
    }

    // Select Card
    private void selectCard() {
        selectedMethod = "card";

        // Highlight Card
        optionCard.setBackgroundResource(R.drawable.bg_selected_option);
        radioCard.setImageResource(R.drawable.ic_check_circle);
        radioCard.setColorFilter(getResources().getColor(R.color.pg_primary, null));

        // Unhighlight COD
        optionCOD.setBackgroundResource(R.drawable.bg_unselected_option);
        radioCOD.setImageResource(R.drawable.ic_circle_dashed);
        radioCOD.setColorFilter(getResources().getColor(R.color.pg_sub, null));

        // Show card details
        cardDetailsSection.setVisibility(View.VISIBLE);
    }

    // Place order
    private void placeOrder() {
        // Validate card details if card selected
        if ("card".equals(selectedMethod)) {
            String cardNo  = etCardNumber.getText() != null ? etCardNumber.getText().toString().trim() : "";
            String expiry  = etExpiry.getText()     != null ? etExpiry.getText().toString().trim()     : "";
            String cvv     = etCvv.getText()         != null ? etCvv.getText().toString().trim()        : "";

            if (cardNo.length() < 12) {
                etCardNumber.setError("Enter a valid card number");
                etCardNumber.requestFocus();
                return;
            }
            if (expiry.length() < 4) {
                etExpiry.setError("Enter expiry date (MM/YY)");
                etExpiry.requestFocus();
                return;
            }
            if (cvv.length() < 3) {
                etCvv.setError("Enter CVV");
                etCvv.requestFocus();
                return;
            }
        }

        // Save order to Firestore
        String uid     = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        String orderId = "PG-" + System.currentTimeMillis();

        Map<String, Object> order = new HashMap<>();
        order.put("orderId",       orderId);
        order.put("customerId",    uid);
        order.put("subtotal",      subtotal);
        order.put("deliveryFee",   deliveryFee);
        order.put("total",         total);
        order.put("paymentMethod", selectedMethod);
        order.put("status",        "pending");
        order.put("createdAt",     System.currentTimeMillis());

        // Disable button to prevent double click
        MaterialButton btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Placing order...");

        db.collection("orders")
                .document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "✅ Order placed successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to order tracking
                    Intent intent = new Intent(this, OrderTrackingActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Place order");
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
