package com.nibm.pharmagomadproject.customer.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterPharmacyActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etPharmacyName, etOwnerName, etLicenseNo, etEmail, etPhone, etAddress, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_pharmacy);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etPharmacyName = findViewById(R.id.etPharmacyName);
        etOwnerName    = findViewById(R.id.etOwnerName);
        etLicenseNo    = findViewById(R.id.etLicenseNo);
        etEmail        = findViewById(R.id.etEmail);
        etPhone        = findViewById(R.id.etPhone);
        etAddress      = findViewById(R.id.etAddress);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Role toggle — back to customer
        TextView roleCustomer = findViewById(R.id.roleCustomer);
        TextView roleRider    = findViewById(R.id.roleRider);
        if (roleCustomer != null) roleCustomer.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
        if (roleRider != null) roleRider.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterRiderActivity.class));
            finish();
        });

        LinearLayout uploadArea = findViewById(R.id.uploadLicenseArea);
        if (uploadArea != null) uploadArea.setOnClickListener(v ->
                Toast.makeText(this, "Upload feature coming soon", Toast.LENGTH_SHORT).show());

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitForApproval);
        if (btnSubmit != null) btnSubmit.setOnClickListener(v -> attemptRegister());

        TextView tvLogin = findViewById(R.id.tvLogin);
        if (tvLogin != null) tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String pharmacyName = etPharmacyName.getText() != null ? etPharmacyName.getText().toString().trim() : "";
        String ownerName    = etOwnerName.getText()    != null ? etOwnerName.getText().toString().trim()    : "";
        String licenseNo    = etLicenseNo.getText()    != null ? etLicenseNo.getText().toString().trim()    : "";
        String email        = etEmail.getText()        != null ? etEmail.getText().toString().trim()        : "";
        String phone        = etPhone.getText()        != null ? etPhone.getText().toString().trim()        : "";
        String address      = etAddress.getText()      != null ? etAddress.getText().toString().trim()      : "";

        if (TextUtils.isEmpty(pharmacyName)) {
            etPharmacyName.setError("Required");
            etPharmacyName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(ownerName)){
            etOwnerName.setError("Required");
            etOwnerName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(licenseNo)){
            etLicenseNo.setError("Required");
            etLicenseNo.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)){
            etEmail.setError("Required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)){
            etPhone.setError("Required");
            etPhone.requestFocus();
            return;
        }

        // Create auth account with temp password, save to Firestore — pending approval
        String tempPassword = licenseNo + phone.substring(Math.max(0, phone.length() - 4));

        mAuth.createUserWithEmailAndPassword(email, tempPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Save pharmacy owner role
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name",         ownerName);
                        userData.put("email",        email);
                        userData.put("phone",        phone);
                        userData.put("role",         "pharmacy_owner");
                        userData.put("isApproved",   false); // admin approval
                        userData.put("createdAt",    com.google.firebase.Timestamp.now());

                        // pharmacy details save in pharmacies collection
                        Map<String, Object> pharmacyData = new HashMap<>();
                        pharmacyData.put("name",      pharmacyName);
                        pharmacyData.put("ownerId",   userId);
                        pharmacyData.put("ownerName", ownerName);
                        pharmacyData.put("licenseNo", licenseNo);
                        pharmacyData.put("address",   address);
                        pharmacyData.put("phone",     phone);
                        pharmacyData.put("email",     email);
                        pharmacyData.put("isApproved",false);
                        pharmacyData.put("rating",    0.0);
                        pharmacyData.put("createdAt", com.google.firebase.Timestamp.now());

                        db.collection("users").document(userId).set(userData);
                        db.collection("pharmacies").add(pharmacyData)
                                .addOnSuccessListener(ref -> {
                                    // Sign out — they need admin approval first
                                    mAuth.signOut();
                                    startActivity(new Intent(this, AccountStatusActivity.class));
                                    finish();
                                });
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
