package com.nibm.pharmagomadproject.customer.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
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

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private String selectedRole = "customer";
    private TextView roleCustomer, rolePharmacy, roleRider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etName            = findViewById(R.id.etName);
        etEmail           = findViewById(R.id.etEmail);
        etPhone           = findViewById(R.id.etPhone);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        roleCustomer = findViewById(R.id.roleCustomer);
        rolePharmacy = findViewById(R.id.rolePharmacy);
        roleRider    = findViewById(R.id.roleRider);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Default: Customer selected
        updateRoleUI();

        // Role selection
        roleCustomer.setOnClickListener(v -> {
            selectedRole = "customer";
            updateRoleUI();
        });
        rolePharmacy.setOnClickListener(v -> {
            selectedRole = "pharmacy";
            updateRoleUI();
            startActivity(new Intent(this, RegisterPharmacyActivity.class));
        });
        roleRider.setOnClickListener(v -> {
            selectedRole = "rider";
            updateRoleUI();
            startActivity(new Intent(this, RegisterRiderActivity.class));
        });

        // Already have account
        TextView tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(v -> finish());

        // Register button
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void updateRoleUI() {
        int primary = getResources().getColor(R.color.pg_primary, null);
        int sub     = getResources().getColor(R.color.pg_sub, null);

        roleCustomer.setBackgroundResource(selectedRole.equals("customer")
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        roleCustomer.setTextColor(selectedRole.equals("customer") ? primary : sub);

        rolePharmacy.setBackgroundResource(selectedRole.equals("pharmacy")
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        rolePharmacy.setTextColor(selectedRole.equals("pharmacy") ? primary : sub);

        roleRider.setBackgroundResource(selectedRole.equals("rider")
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        roleRider.setTextColor(selectedRole.equals("rider") ? primary : sub);
    }

    private void attemptRegister() {
        if (!selectedRole.equals("customer")) {
            Toast.makeText(this, "Please use the appropriate form", Toast.LENGTH_SHORT).show();
            return;
        }

        String name    = etName.getText()            != null ? etName.getText().toString().trim()            : "";
        String email   = etEmail.getText()           != null ? etEmail.getText().toString().trim()           : "";
        String phone   = etPhone.getText()           != null ? etPhone.getText().toString().trim()           : "";
        String pass    = etPassword.getText()        != null ? etPassword.getText().toString().trim()        : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        CheckBox cbTerms = findViewById(R.id.cbTerms);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter your name");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Enter your phone");
            etPhone.requestFocus();
            return;
        }
        if (pass.length() < 6){
            etPassword.setError("Min 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!pass.equals(confirm)){
            etConfirmPassword.setError("Passwords don't match");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!cbTerms.isChecked()){
            Toast.makeText(this, "Please agree to Terms & Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        registerCustomer(email, pass, name, phone);
    }

    private void registerCustomer(String email, String password, String name, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Save data and role in firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name",      name);
                        userData.put("email",     email);
                        userData.put("phone",     phone);
                        userData.put("role",      "customer"); // role save
                        userData.put("isApproved", true);
                        userData.put("createdAt", com.google.firebase.Timestamp.now());

                        db.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(this,
                                            "Registered successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Auth success but Firestore fail
                                    Toast.makeText(this,
                                            "Registered!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
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
