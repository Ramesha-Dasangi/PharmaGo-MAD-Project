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
import com.nibm.pharmagomadproject.customer.activities.medicine.PrescriptionUploadActivity;
import com.nibm.pharmagomadproject.customer.activities.profile.DeliveryAddressActivity;
import com.nibm.pharmagomadproject.customer.models.Cart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private TextView tvPaymentAddress;
    private CardView cardRxWarning;
    private MaterialButton btnPlaceOrder;

    // Firebase
    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;

    // Order data from CartActivity
    private int subtotal    = 0;
    private int deliveryFee = 100;
    private int total       = 100;
    private String existingOrderId = null;

    // Rx state — true if cart has prescription items
    private boolean cartHasRx = false;

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
        tvPaymentAddress   = findViewById(R.id.tvPaymentAddress);
        cardRxWarning      = findViewById(R.id.cardRxWarning);
        btnPlaceOrder      = findViewById(R.id.btnPlaceOrder);

        // Address card — tap to change
        CardView cardPaymentAddress = findViewById(R.id.cardPaymentAddress);
        if (cardPaymentAddress != null) {
            cardPaymentAddress.setOnClickListener(v ->
                    startActivity(new Intent(this, DeliveryAddressActivity.class)));
        }

        // Rx: Upload prescription button
        MaterialButton btnUploadRx = findViewById(R.id.btnUploadRxFromPayment);
        if (btnUploadRx != null) {
            btnUploadRx.setOnClickListener(v -> {
                // Find the first Rx cart item to pass pharmacy info
                Cart rxItem = null;
                for (Cart c : CartActivity.CART_STORE) {
                    if (c.isRx()) { rxItem = c; break; }
                }
                Intent intent = new Intent(this, PrescriptionUploadActivity.class);
                if (rxItem != null) {
                    intent.putExtra("pharmacy_id",   rxItem.getPharmacyId());
                    intent.putExtra("pharmacy_name", rxItem.getPharmacyName());
                    intent.putExtra("medicine_name", rxItem.getMedicineName());
                    intent.putExtra("medicine_price", (int) rxItem.getPrice());
                    // Pass all cart items as Rx order
                    intent.putExtra("is_cart_rx_order", true);
                }
                startActivity(intent);
            });
        }

        existingOrderId = getIntent().getStringExtra("orderId");
        if (existingOrderId != null && !existingOrderId.isEmpty()) {
            // Prescription/existing order approved flow — load order from Firestore
            db.collection("orders").document(existingOrderId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            // Read rejected pharmacies
                            java.util.List<?> rejectedList = (java.util.List<?>) doc.get("rejectedPharmacies");

                            // Calculate payable total: exclude items from rejected pharmacies
                            java.util.List<java.util.Map<String, Object>> items =
                                    (java.util.List<java.util.Map<String, Object>>) doc.get("items");

                            double payableSubtotal = 0;
                            int rejectedItemCount = 0;
                            java.util.List<String> rejectedPharmNames = new java.util.ArrayList<>();

                            if (items != null) {
                                for (java.util.Map<String, Object> item : items) {
                                    String pId = item.get("pharmacyId") != null ? item.get("pharmacyId").toString() : "";
                                    boolean isRejected = rejectedList != null && rejectedList.contains(pId);

                                    if (isRejected) {
                                        rejectedItemCount++;
                                        Object pName = item.get("pharmacyName");
                                        if (pName != null && !rejectedPharmNames.contains(pName.toString())) {
                                            rejectedPharmNames.add(pName.toString());
                                        }
                                    } else {
                                        Object price = item.get("price");
                                        Object qty   = item.get("quantity");
                                        if (price instanceof Number && qty instanceof Number) {
                                            payableSubtotal += ((Number) price).doubleValue() * ((Number) qty).doubleValue();
                                        }
                                    }
                                }
                            }

                            // If all items rejected — show nothing to pay
                            if (payableSubtotal == 0 && items != null && !items.isEmpty()) {
                                if (btnPlaceOrder != null) {
                                    btnPlaceOrder.setEnabled(false);
                                    btnPlaceOrder.setAlpha(0.5f);
                                    btnPlaceOrder.setText("No payable items");
                                }
                                android.widget.Toast.makeText(this,
                                        "All prescription items were rejected. Nothing to pay.",
                                        android.widget.Toast.LENGTH_LONG).show();
                                return;
                            }

                            Double orderDeliveryFee = doc.getDouble("deliveryFee");
                            deliveryFee = orderDeliveryFee != null ? orderDeliveryFee.intValue() : 100;
                            subtotal    = (int) payableSubtotal;
                            total       = subtotal + deliveryFee;

                            // Update Firestore total to reflect only payable items
                            java.util.Map<String, Object> recalc = new java.util.HashMap<>();
                            recalc.put("subtotal", (double) subtotal);
                            recalc.put("total",    (double) total);
                            db.collection("orders").document(existingOrderId).update(recalc);

                            tvPaySubtotal.setText("Rs. " + subtotal);
                            tvPayTotal.setText("Rs. " + total);

                            // Show note if some items were rejected
                            if (rejectedItemCount > 0 && !rejectedPharmNames.isEmpty()) {
                                TextView tvRxWarningMsg = findViewById(R.id.tvRxWarningMsg);
                                if (cardRxWarning != null) cardRxWarning.setVisibility(android.view.View.VISIBLE);
                                if (tvRxWarningMsg != null) {
                                    String rejectedNote = rejectedItemCount + " item(s) from "
                                            + android.text.TextUtils.join(", ", rejectedPharmNames)
                                            + " were rejected and won't be charged.";
                                    tvRxWarningMsg.setText(rejectedNote);
                                }
                                // Hide the upload Rx button since this is the payment stage
                                android.view.View btnUpload = findViewById(R.id.btnUploadRxFromPayment);
                                if (btnUpload != null) btnUpload.setVisibility(android.view.View.GONE);
                            }

                            // Show delivery address from order
                            String addr = doc.getString("deliveryAddress");
                            if (tvPaymentAddress != null) {
                                tvPaymentAddress.setText(addr != null && !addr.isEmpty()
                                        ? addr : "Tap to set address");
                            }
                        }
                    });
            // Existing Rx-approved orders don't need Rx check again
            cartHasRx = false;
        } else {
            // Normal cart flow
            subtotal    = getIntent().getIntExtra("subtotal",    0);
            deliveryFee = getIntent().getIntExtra("deliveryFee", 100);
            total       = getIntent().getIntExtra("total",       100);
            tvPaySubtotal.setText("Rs. " + subtotal);
            tvPayTotal.setText("Rs. " + total);

            // Detect Rx items in cart
            detectRxInCart();
        }

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Payment method selection — Default - COD
        selectCOD();
        optionCOD.setOnClickListener(v  -> selectCOD());
        optionCard.setOnClickListener(v -> selectCard());

        // Place order button
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh address and Rx status every time we return
        loadUserAddress();
        if (existingOrderId == null) detectRxInCart();
    }

    // Helpers

    private void loadUserAddress() {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        TextView tvBottomAddress = findViewById(R.id.tvPaymentBottomAddress);

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String address = doc.getString("address");
                        String label   = doc.getString("addressLabel");
                        if (address != null && !address.trim().isEmpty()) {
                            String display = (label != null && !label.isEmpty())
                                    ? label + "  ·  " + address : address;
                            if (tvPaymentAddress != null) tvPaymentAddress.setText(display);
                            if (tvBottomAddress != null) tvBottomAddress.setText(display);
                        } else {
                            if (tvPaymentAddress != null) tvPaymentAddress.setText("Tap to set address");
                            if (tvBottomAddress != null) tvBottomAddress.setText("Tap to set address");
                        }
                    }
                });
    }

    private void detectRxInCart() {
        cartHasRx = false;
        for (Cart c : CartActivity.CART_STORE) {
            if (c.isRx()) { cartHasRx = true; break; }
        }

        if (cardRxWarning != null) {
            cardRxWarning.setVisibility(cartHasRx ? View.VISIBLE : View.GONE);
        }
        if (btnPlaceOrder != null) {
            // Block payment button if cart has Rx items (need pharmacy approval first)
            btnPlaceOrder.setEnabled(!cartHasRx);
            btnPlaceOrder.setAlpha(cartHasRx ? 0.5f : 1.0f);
            if (cartHasRx) {
                btnPlaceOrder.setText("Awaiting prescription approval");
            } else {
                btnPlaceOrder.setText("Place order");
            }
        }
    }

    private void selectCOD() {
        selectedMethod = "cod";
        optionCOD.setBackgroundResource(R.drawable.bg_selected_option);
        optionCard.setBackgroundResource(R.drawable.bg_unselected_option);
        radioCOD.setImageResource(R.drawable.ic_check_circle);
        radioCOD.setImageTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.pg_primary, null)));
        radioCard.setImageResource(R.drawable.ic_circle_dashed);
        radioCard.setImageTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.pg_sub, null)));
        cardDetailsSection.setVisibility(View.GONE);
    }

    private void selectCard() {
        selectedMethod = "card";
        optionCard.setBackgroundResource(R.drawable.bg_selected_option);
        optionCOD.setBackgroundResource(R.drawable.bg_unselected_option);
        radioCard.setImageResource(R.drawable.ic_check_circle);
        radioCard.setImageTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.pg_primary, null)));
        radioCOD.setImageResource(R.drawable.ic_circle_dashed);
        radioCOD.setImageTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.pg_sub, null)));
        cardDetailsSection.setVisibility(View.VISIBLE);
    }

    // Place order

    private void placeOrder() {
        if (cartHasRx) {
            Toast.makeText(this, "Please upload your prescription first. Payment will be enabled after pharmacy approval.", Toast.LENGTH_LONG).show();
            return;
        }

        // Validate card details if card selected
        if ("card".equals(selectedMethod)) {
            String cardNo = etCardNumber.getText() != null ? etCardNumber.getText().toString().trim() : "";
            String expiry = etExpiry.getText()     != null ? etExpiry.getText().toString().trim()     : "";
            String cvv    = etCvv.getText()         != null ? etCvv.getText().toString().trim()        : "";
            if (cardNo.length() < 12) { etCardNumber.setError("Enter a valid card number"); etCardNumber.requestFocus(); return; }
            if (expiry.length() < 4)  { etExpiry.setError("Enter expiry date (MM/YY)");    etExpiry.requestFocus();     return; }
            if (cvv.length()    < 3)  { etCvv.setError("Enter CVV");                       etCvv.requestFocus();        return; }
        }

        // Existing order (Rx approved flow)
        if (existingOrderId != null && !existingOrderId.isEmpty()) {
            btnPlaceOrder.setEnabled(false);
            btnPlaceOrder.setText("Processing payment...");

            Map<String, Object> updates = new HashMap<>();
            updates.put("status",        "processing");
            updates.put("paymentMethod", selectedMethod);
            updates.put("paidSubtotal",  (double) subtotal);
            updates.put("paidTotal",     (double) total);

            db.collection("orders").document(existingOrderId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✅ Payment successful! Order is now being prepared.", Toast.LENGTH_SHORT).show();

                        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
                        if (!uid.isEmpty()) {
                            Map<String, Object> notif = new HashMap<>();
                            notif.put("userId",      uid);
                            notif.put("title",       "Payment Successful 💳");
                            notif.put("message",     "Payment for order " + existingOrderId + " was successful! The pharmacy is preparing your items.");
                            notif.put("type",        "order_processing");
                            notif.put("referenceId", existingOrderId);
                            notif.put("isRead",      false);
                            notif.put("createdAt",   System.currentTimeMillis());
                            db.collection("notifications").add(notif);
                        }

                        Intent intent = new Intent(this, OrderTrackingActivity.class);
                        intent.putExtra("orderId", existingOrderId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnPlaceOrder.setEnabled(true);
                        btnPlaceOrder.setText("Place order");
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            return;
        }

        // New order from cart
        String uid     = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        String orderId = "PG-" + System.currentTimeMillis();

        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (Cart c : CartActivity.CART_STORE) {
            Map<String, Object> item = new HashMap<>();
            item.put("medicineId",   c.getMedicineId());
            item.put("medicineName", c.getMedicineName());
            item.put("pharmacyId",   c.getPharmacyId());
            item.put("pharmacyName", c.getPharmacyName());
            item.put("price",        c.getPrice());
            item.put("quantity",     c.getQuantity());
            item.put("type",         c.getMedicineType() != null ? c.getMedicineType() : "OTC");
            itemsList.add(item);
        }

        java.util.List<String> pharmNames = new java.util.ArrayList<>();
        java.util.List<String> pharmIds = new java.util.ArrayList<>();
        for (Cart c : CartActivity.CART_STORE) {
            if (c.getPharmacyName() != null && !c.getPharmacyName().isEmpty() && !pharmNames.contains(c.getPharmacyName())) {
                pharmNames.add(c.getPharmacyName());
            }
            if (c.getPharmacyId() != null && !c.getPharmacyId().isEmpty() && !pharmIds.contains(c.getPharmacyId())) {
                pharmIds.add(c.getPharmacyId());
            }
        }

        String mainPharmacyName = !pharmNames.isEmpty() ? String.join(", ", pharmNames) : "Pharmacy";
        String mainPharmacyId   = !pharmIds.isEmpty() ? pharmIds.get(0) : "";

        Map<String, Object> order = new HashMap<>();
        order.put("orderId",       orderId);
        order.put("customerId",    uid);
        order.put("pharmacyId",    mainPharmacyId);
        order.put("pharmacyName",  mainPharmacyName);
        order.put("pharmacyIds",   pharmIds);
        order.put("items",         itemsList);
        order.put("subtotal",      subtotal);
        order.put("deliveryFee",   deliveryFee);
        order.put("total",         total);
        order.put("paymentMethod", selectedMethod);
        order.put("status",        "pending");
        order.put("createdAt",     System.currentTimeMillis());

        // Fetch customer address before saving
        if (!uid.isEmpty()) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String addr  = doc.getString("address");
                            String label = doc.getString("addressLabel");
                            if (addr != null && !addr.isEmpty()) {
                                order.put("deliveryAddress", (label != null && !label.isEmpty())
                                        ? label + " · " + addr : addr);
                            }
                        }
                        saveOrderToFirestore(orderId, order);
                    })
                    .addOnFailureListener(e -> saveOrderToFirestore(orderId, order));
        } else {
            saveOrderToFirestore(orderId, order);
        }
    }

    private void saveOrderToFirestore(String orderId, Map<String, Object> order) {
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Placing order...");

        db.collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    CartActivity.clearCart();

                    String notifUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
                    if (!notifUid.isEmpty()) {
                        Map<String, Object> notif = new HashMap<>();
                        notif.put("userId",      notifUid);
                        notif.put("title",       "Order confirmed 🎉");
                        notif.put("message",     "Your order " + orderId + " has been placed! We'll notify you when it's ready.");
                        notif.put("type",        "order_placed");
                        notif.put("referenceId", orderId);
                        notif.put("isRead",      false);
                        notif.put("createdAt",   System.currentTimeMillis());
                        db.collection("notifications").add(notif);
                    }

                    // ── Notify every pharmacy involved in this order ──
                    // (uses the schema pharmacyowner.NotificationsActivity queries:
                    // ownerId / title / description / time / type / read / timestamp)
                    Object pharmIdsObj = order.get("pharmacyIds");
                    if (pharmIdsObj instanceof List) {
                        java.text.SimpleDateFormat timeFmt = new java.text.SimpleDateFormat(
                                "dd MMM yyyy hh:mm a", java.util.Locale.getDefault());
                        String timeStr = timeFmt.format(new java.util.Date());
                        long now = System.currentTimeMillis();

                        for (Object pid : (List<?>) pharmIdsObj) {
                            if (pid == null || pid.toString().isEmpty()) continue;
                            Map<String, Object> ownerNotif = new HashMap<>();
                            ownerNotif.put("title",       "New Order Received 🛒");
                            ownerNotif.put("description", "You have a new order (" + orderId + "). Tap to review it.");
                            ownerNotif.put("time",        timeStr);
                            ownerNotif.put("type",        "new_order");
                            ownerNotif.put("read",        false);
                            ownerNotif.put("ownerId",     pid.toString());
                            ownerNotif.put("timestamp",   now);
                            db.collection("notifications").add(ownerNotif);
                        }
                    }

                    Toast.makeText(this, "✅ Order placed successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, OrderTrackingActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Place order");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}