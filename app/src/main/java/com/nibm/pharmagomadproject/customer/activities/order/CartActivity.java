package com.nibm.pharmagomadproject.customer.activities.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.profile.DeliveryAddressActivity;
import com.nibm.pharmagomadproject.customer.adapter.CartAdapter;
import com.nibm.pharmagomadproject.customer.models.Cart;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartListener {

    private RecyclerView   rvCart;
    private CartAdapter    adapter;
    private List<Cart>     cartItems;
    private TextView       tvSubtotal, tvTotal, tvEmptyCart, tvDeliveryAddress;
    private CardView       cardAddressCart;
    private MaterialButton btnProceedToPayment;

    private static final int DELIVERY_FEE = 100;

    // ✅ Static cart store — singleton list (activity restart survive karanawa)
    public static final List<Cart> CART_STORE = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        rvCart              = findViewById(R.id.rvCart);
        tvSubtotal          = findViewById(R.id.tvSubtotal);
        tvTotal             = findViewById(R.id.tvTotal);
        tvEmptyCart         = findViewById(R.id.tvEmptyCart);
        tvDeliveryAddress   = findViewById(R.id.tvDeliveryAddress);
        cardAddressCart     = findViewById(R.id.cardAddressCart);
        btnProceedToPayment = findViewById(R.id.btnProceedToPayment);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Launch Delivery Address screen when clicking address card
        if (cardAddressCart != null) {
            cardAddressCart.setOnClickListener(v -> {
                startActivity(new Intent(this, DeliveryAddressActivity.class));
            });
        }

        // ✅ Use global cart store
        cartItems = CART_STORE;

        adapter = new CartAdapter(cartItems, this);
        if (rvCart != null) {
            rvCart.setLayoutManager(new LinearLayoutManager(this));
            rvCart.setAdapter(adapter);
        }

        updateUI();

        btnProceedToPayment.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("subtotal",    (int) getSubtotal());
            intent.putExtra("total",       (int) (getSubtotal() + DELIVERY_FEE));
            intent.putExtra("deliveryFee", DELIVERY_FEE);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserAddress();
        loadUserCart();
    }

    private void loadUserAddress() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId != null && tvDeliveryAddress != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String address = document.getString("address");
                            if (address != null && !address.trim().isEmpty()) {
                                tvDeliveryAddress.setText(address);
                            } else {
                                tvDeliveryAddress.setText("Tap to add delivery address");
                            }
                        }
                    });
        }
    }

    private void loadUserCart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .collection("cart")
                    .get()
                    .addOnSuccessListener(query -> {
                        CART_STORE.clear();
                        for (DocumentSnapshot doc : query) {
                            Cart c = doc.toObject(Cart.class);
                            if (c != null) {
                                CART_STORE.add(c);
                            }
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        updateUI();
                    });
        }
    }

    // CartAdapter.CartListener
    @Override
    public void onQuantityChanged(int position, int newQty) {
        Cart cart = cartItems.get(position);
        cart.setQuantity(newQty);
        adapter.notifyItemChanged(position);
        updateUI();

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .collection("cart")
                    .document(cart.getMedicineId())
                    .update("quantity", newQty);
        }
    }

    @Override
    public void onRemoveItem(int position) {
        Cart cart = cartItems.remove(position);
        adapter.notifyItemRemoved(position);
        updateUI();

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .collection("cart")
                    .document(cart.getMedicineId())
                    .delete();
        }
    }

    private void updateUI() {
        boolean empty = cartItems.isEmpty();
        if (rvCart != null) {
            rvCart.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
        if (tvEmptyCart != null) {
            tvEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        if (btnProceedToPayment != null) {
            btnProceedToPayment.setEnabled(!empty);
        }

        double subtotal = getSubtotal();
        double total    = subtotal + DELIVERY_FEE;
        if (tvSubtotal != null) {
            tvSubtotal.setText("Rs. " + String.format("%.0f", subtotal));
        }
        if (tvTotal != null) {
            tvTotal.setText("Rs. " + String.format("%.0f", total));
        }
    }

    private double getSubtotal() {
        double sum = 0;
        for (Cart c : cartItems) sum += c.getSubtotal();
        return sum;
    }

    // Static helper — add item to cart from anywhere
    public static void addToCart(Cart item) {
        // Check if same medicine from same pharmacy already in cart
        boolean found = false;
        for (Cart c : CART_STORE) {
            if (c.getMedicineId().equals(item.getMedicineId())
                    && c.getPharmacyName().equals(item.getPharmacyName())) {
                c.setQuantity(c.getQuantity() + item.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            CART_STORE.add(item);
        }

        // Sync with Firestore if user logged in
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid != null) {
            Cart toWrite = null;
            for (Cart c : CART_STORE) {
                if (c.getMedicineId().equals(item.getMedicineId())) {
                    toWrite = c;
                    break;
                }
            }
            if (toWrite != null) {
                FirebaseFirestore.getInstance().collection("users")
                        .document(uid)
                        .collection("cart")
                        .document(item.getMedicineId())
                        .set(toWrite);
            }
        }
    }

    public static void clearCart() {
        CART_STORE.clear();
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .collection("cart")
                    .get()
                    .addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query) {
                            doc.getReference().delete();
                        }
                    });
        }
    }
}
