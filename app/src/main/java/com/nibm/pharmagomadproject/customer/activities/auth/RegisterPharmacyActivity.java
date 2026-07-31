package com.nibm.pharmagomadproject.customer.activities.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.db.SupabaseStorageHelper;

import java.util.HashMap;
import java.util.Map;

public class RegisterPharmacyActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SupabaseStorageHelper storageHelper;
    private Uri licenseUri;
    private ActivityResultLauncher<String> imagePicker;

    private TextInputEditText etPharmacyName, etOwnerName, etLicenseNo,
            etEmail, etPhone, etAddress, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_pharmacy);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageHelper = new SupabaseStorageHelper(this);

        // Image picker
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        licenseUri = uri;
                        Toast.makeText(this, "License selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        etPharmacyName = findViewById(R.id.etPharmacyName);
        etOwnerName = findViewById(R.id.etOwnerName);
        etLicenseNo = findViewById(R.id.etLicenseNo);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        LinearLayout uploadArea = findViewById(R.id.uploadLicenseArea);
        if (uploadArea != null) {
            uploadArea.setOnClickListener(v -> imagePicker.launch("image/*"));
        }

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitForApproval);
        btnSubmit.setOnClickListener(v -> {
            registerPharmacy();
        });

        TextView tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerPharmacy() {
        String pharmacyName = etPharmacyName.getText().toString().trim();
        String ownerName = etOwnerName.getText().toString().trim();
        String licenseNo = etLicenseNo.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(pharmacyName)) {
            etPharmacyName.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(ownerName)) {
            etOwnerName.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(licenseNo)) {
            etLicenseNo.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Required");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Required");
            return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Password mismatch");
            return;
        }
        if (licenseUri == null) {
            Toast.makeText(this, "Please upload license", Toast.LENGTH_SHORT).show();
            return;
        }

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitForApproval);
        if (btnSubmit != null) btnSubmit.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = mAuth.getCurrentUser().getUid();
                    uploadLicenseAndSave(uid, pharmacyName, ownerName, licenseNo, email, phone, address);
                })
                .addOnFailureListener(e -> {
                    if (btnSubmit != null) btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadLicenseAndSave(String uid, String pharmacyName, String ownerName,
                                      String licenseNo, String email, String phone, String address) {
        storageHelper.uploadFile(
                SupabaseStorageHelper.BUCKET_LICENSES,
                "pharmacy/" + uid + "/license_" + System.currentTimeMillis() + ".jpg",
                licenseUri,
                new SupabaseStorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String url) {
                        saveFirestoreData(uid, pharmacyName, ownerName, licenseNo, email, phone, address, url);
                    }
                    @Override
                    public void onFailure(String error) {
                        MaterialButton btnSubmit = findViewById(R.id.btnSubmitForApproval);
                        if (btnSubmit != null) btnSubmit.setEnabled(true);
                        Toast.makeText(RegisterPharmacyActivity.this,
                                "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void saveFirestoreData(String uid, String pharmacyName, String ownerName,
                                   String licenseNo, String email, String phone,
                                   String address, String licenseUrl) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", ownerName);
        user.put("email", email);
        user.put("phone", phone);
        user.put("role", "pharmacy_owner");
        user.put("isApproved", false);
        user.put("status", "pending");
        user.put("createdAt", Timestamp.now());

        Map<String, Object> pharmacy = new HashMap<>();
        pharmacy.put("ownerId", uid);
        pharmacy.put("name", pharmacyName);
        pharmacy.put("ownerName", ownerName);
        pharmacy.put("licenseNo", licenseNo);
        pharmacy.put("email", email);
        pharmacy.put("phone", phone);
        pharmacy.put("address", address);
        pharmacy.put("licenseImageUrl", licenseUrl);
        pharmacy.put("isApproved", false);
        pharmacy.put("status", "pending");
        pharmacy.put("rating", 0.0);

        // Convert written address text into GPS latitude & longitude using Android Geocoder
        double lat = 6.9271; // Default fallback
        double lng = 79.8612;
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
            @SuppressWarnings("deprecation")
            java.util.List<android.location.Address> addresses = geocoder.getFromLocationName(address + ", Sri Lanka", 1);
            if (addresses != null && !addresses.isEmpty()) {
                lat = addresses.get(0).getLatitude();
                lng = addresses.get(0).getLongitude();
            }
        } catch (Exception ignored) {}

        pharmacy.put("latitude", lat);
        pharmacy.put("longitude", lng);
        pharmacy.put("createdAt", Timestamp.now());

        // FIX 1: Properly chain Firestore operations - save user first, then pharmacy
        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    // User saved successfully, now save pharmacy
                    db.collection("pharmacies").add(pharmacy)
                            .addOnSuccessListener(ref -> {
                                mAuth.signOut();
                                Toast.makeText(RegisterPharmacyActivity.this,
                                        "Registration submitted. Wait for approval", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RegisterPharmacyActivity.this, AccountStatusActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                MaterialButton btnSubmit = findViewById(R.id.btnSubmitForApproval);
                                if (btnSubmit != null) btnSubmit.setEnabled(true);
                                Toast.makeText(RegisterPharmacyActivity.this,
                                        "Failed to save pharmacy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // FIX #2: Delete user from Firebase Auth if Firestore save fails
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().delete()
                                .addOnCompleteListener(task -> {
                                    MaterialButton btnSubmit = findViewById(R.id.btnSubmitForApproval);
                                    if (btnSubmit != null) btnSubmit.setEnabled(true);
                                    Toast.makeText(RegisterPharmacyActivity.this,
                                            "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        MaterialButton btnSubmit = findViewById(R.id.btnSubmitForApproval);
                        if (btnSubmit != null) btnSubmit.setEnabled(true);
                        Toast.makeText(RegisterPharmacyActivity.this,
                                "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}