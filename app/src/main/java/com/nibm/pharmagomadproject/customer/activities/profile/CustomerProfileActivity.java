package com.nibm.pharmagomadproject.customer.activities.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.order.CartActivity;
import com.nibm.pharmagomadproject.customer.activities.order.OrderHistoryActivity;
import com.nibm.pharmagomadproject.customer.activities.auth.ChangePasswordActivity;
import com.nibm.pharmagomadproject.customer.activities.auth.LoginActivity;
import com.nibm.pharmagomadproject.customer.activities.home.HomeActivity;
import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineListActivity;

public class CustomerProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvProfileName, tvProfileEmail, tvProfilePhone, tvProfileAddress;
    private com.google.android.material.switchmaterial.SwitchMaterial switchNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileAddress = findViewById(R.id.tvProfileAddress);
        switchNotifications = findViewById(R.id.switchNotifications);

        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (mAuth.getCurrentUser() != null) {
                    String uid = mAuth.getCurrentUser().getUid();
                    db.collection("users").document(uid)
                            .update("notificationsEnabled", isChecked);
                }
            });
        }

        // Row: Delivery address
        LinearLayout rowAddress = findViewById(R.id.rowAddress);
        rowAddress.setOnClickListener(v -> {
            startActivity(new Intent(this, DeliveryAddressActivity.class));
        });

        // Row: Order history
        LinearLayout rowOrderHistory = findViewById(R.id.rowOrderHistory);
        rowOrderHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
        });

        // Row: Change password
        LinearLayout rowChangePassword = findViewById(R.id.rowChangePassword);
        rowChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        // Row: Log out
        LinearLayout rowLogout = findViewById(R.id.rowLogout);
        rowLogout.setOnClickListener(v -> showLogoutDialog());

        // Bottom nav
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String phone = document.getString("phone");
                        String address = document.getString("address");
                        Boolean notifEnabled = document.getBoolean("notificationsEnabled");

                        if (tvProfileName != null && name != null) tvProfileName.setText(name);
                        if (tvProfileEmail != null && email != null) tvProfileEmail.setText(email);
                        if (tvProfilePhone != null && phone != null) tvProfilePhone.setText(phone);
                        if (tvProfileAddress != null && address != null) {
                            tvProfileAddress.setText(address);
                        } else if (tvProfileAddress != null) {
                            tvProfileAddress.setText("No address saved");
                        }
                        if (switchNotifications != null) {
                            switchNotifications.setChecked(notifEnabled == null || notifEnabled);
                        }
                    }
                });
    }

    private void showLogoutDialog() {
        android.view.View dialogView = getLayoutInflater()
                .inflate(R.layout.activity_dialog_logout, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancelLogout)
                .setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnConfirmLogout)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    // Sign out and clear session
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

        dialog.show();
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.navSearch).setOnClickListener(v -> {
            Intent intent = new Intent(this, MedicineListActivity.class);
            intent.putExtra(MedicineListActivity.EXTRA_MODE, "search");
            startActivity(intent);
            finish();
        });
        findViewById(R.id.navCart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
            finish();
        });
        findViewById(R.id.navOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
            finish();
        });
    }
}
