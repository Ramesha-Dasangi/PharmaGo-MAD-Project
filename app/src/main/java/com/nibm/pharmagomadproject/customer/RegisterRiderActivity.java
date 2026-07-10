package com.nibm.pharmagomadproject.customer;

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

public class RegisterRiderActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etName, etNic, etEmail, etPhone, etVehicleType, etVehicleReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_rider);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etName        = findViewById(R.id.etName);
        etNic         = findViewById(R.id.etNic);
        etEmail       = findViewById(R.id.etEmail);
        etPhone       = findViewById(R.id.etPhone);
        etVehicleType = findViewById(R.id.etVehicleType);
        etVehicleReg  = findViewById(R.id.etVehicleReg);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Role toggles
        TextView roleCustomer = findViewById(R.id.roleCustomer);
        TextView rolePharmacy = findViewById(R.id.rolePharmacy);
        if (roleCustomer != null) roleCustomer.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
        if (rolePharmacy != null) rolePharmacy.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterPharmacyActivity.class));
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
        String name        = etName.getText()        != null ? etName.getText().toString().trim()        : "";
        String nic         = etNic.getText()         != null ? etNic.getText().toString().trim()         : "";
        String email       = etEmail.getText()       != null ? etEmail.getText().toString().trim()       : "";
        String phone       = etPhone.getText()       != null ? etPhone.getText().toString().trim()       : "";
        String vehicleType = etVehicleType.getText() != null ? etVehicleType.getText().toString().trim() : "";
        String vehicleReg  = etVehicleReg.getText()  != null ? etVehicleReg.getText().toString().trim()  : "";

        if (TextUtils.isEmpty(name)){
            etName.setError("Required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(nic)){
            etNic.setError("Required");
            etNic.requestFocus();
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
        if (TextUtils.isEmpty(vehicleType)) {
            etVehicleType.setError("Required");
            etVehicleType.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(vehicleReg))  {
            etVehicleReg.setError("Required");
            etVehicleReg.requestFocus();
            return;
        }

        // Temp password from NIC last 4 digits
        String tempPassword = nic + phone.substring(Math.max(0, phone.length() - 4));

        mAuth.createUserWithEmailAndPassword(email, tempPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Save rider role in firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name",       name);
                        userData.put("email",      email);
                        userData.put("phone",      phone);
                        userData.put("role",       "rider");
                        userData.put("isApproved", false);
                        userData.put("createdAt",  com.google.firebase.Timestamp.now());

                        // Save Rider details
                        Map<String, Object> riderData = new HashMap<>();
                        riderData.put("name",        name);
                        riderData.put("userId",      userId);
                        riderData.put("nic",         nic);
                        riderData.put("email",       email);
                        riderData.put("phone",       phone);
                        riderData.put("vehicleType", vehicleType);
                        riderData.put("vehicleReg",  vehicleReg);
                        riderData.put("isApproved",  false);
                        riderData.put("rating",      0.0);
                        riderData.put("createdAt",   com.google.firebase.Timestamp.now());

                        db.collection("users").document(userId).set(userData);
                        db.collection("riders").add(riderData)
                                .addOnSuccessListener(ref -> {
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
