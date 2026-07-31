package com.nibm.pharmagomadproject.customer.activities.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

public class DeliveryAddressActivity extends AppCompatActivity {

    private TextInputEditText etAddress, etCity;
    private android.widget.TextView tvCurrentAddress;
    private android.widget.TextView tvCurrentLabel;
    private android.widget.TextView labelHomeView, labelWorkView, labelOtherView;
    private String selectedLabel = "Home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_address);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etAddress        = findViewById(R.id.etAddress);
        etCity           = findViewById(R.id.etCity);
        tvCurrentAddress = findViewById(R.id.tvCurrentAddress);
        tvCurrentLabel   = findViewById(R.id.tvCurrentLabel);
        labelHomeView    = findViewById(R.id.labelHome);
        labelWorkView    = findViewById(R.id.labelWork);
        labelOtherView   = findViewById(R.id.labelOther);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Label chips: Home / Work / Other
        labelHomeView.setOnClickListener(v  -> selectLabel("Home"));
        labelWorkView.setOnClickListener(v  -> selectLabel("Work"));
        labelOtherView.setOnClickListener(v -> selectLabel("Other"));

        // Save address button
        MaterialButton btnSave = findViewById(R.id.btnSaveAddress);
        btnSave.setOnClickListener(v -> saveAddress());

        // Load existing address
        loadExistingAddress();
    }

    private void loadExistingAddress() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Restore saved label (Home/Work/Other)
                        String savedLabel = documentSnapshot.getString("addressLabel");
                        if (savedLabel != null && !savedLabel.isEmpty()) {
                            selectLabel(savedLabel);
                        } else {
                            selectLabel("Home");
                        }

                        // Update top card label
                        if (tvCurrentLabel != null) {
                            tvCurrentLabel.setText(selectedLabel);
                        }

                        // Restore address fields
                        String fullAddress = documentSnapshot.getString("address");
                        if (fullAddress != null && !fullAddress.isEmpty()) {
                            if (tvCurrentAddress != null) {
                                tvCurrentAddress.setText(fullAddress);
                            }
                            int commaIndex = fullAddress.lastIndexOf(",");
                            if (commaIndex != -1) {
                                etAddress.setText(fullAddress.substring(0, commaIndex).trim());
                                etCity.setText(fullAddress.substring(commaIndex + 1).trim());
                            } else {
                                etAddress.setText(fullAddress);
                                etCity.setText("");
                            }
                        } else {
                            if (tvCurrentAddress != null) {
                                tvCurrentAddress.setText("No address saved");
                            }
                        }
                    }
                });
    }

    private void selectLabel(String label) {
        selectedLabel = label;
        if (labelHomeView == null) return;

        int primary = getResources().getColor(R.color.pg_primary, null);
        int sub     = getResources().getColor(R.color.pg_sub, null);

        labelHomeView.setBackgroundResource("Home".equals(label)
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        labelWorkView.setBackgroundResource("Work".equals(label)
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        labelOtherView.setBackgroundResource("Other".equals(label)
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);

        labelHomeView.setTextColor("Home".equals(label)  ? primary : sub);
        labelWorkView.setTextColor("Work".equals(label)  ? primary : sub);
        labelOtherView.setTextColor("Other".equals(label)? primary : sub);

        // Update top card label
        if (tvCurrentLabel != null) tvCurrentLabel.setText(label);
    }

    private void saveAddress() {
        String address = etAddress.getText() != null
                ? etAddress.getText().toString().trim() : "";
        String city    = etCity.getText() != null
                ? etCity.getText().toString().trim() : "";

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Please enter your address");
            etAddress.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(city)) {
            etCity.setError("Please enter your city");
            etCity.requestFocus();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId != null) {
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("address",      address + ", " + city);
            updates.put("addressLabel", selectedLabel);
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Address saved!", Toast.LENGTH_SHORT).show();
                        if (tvCurrentAddress != null) {
                            tvCurrentAddress.setText(selectedLabel + " · " + address + ", " + city);
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
