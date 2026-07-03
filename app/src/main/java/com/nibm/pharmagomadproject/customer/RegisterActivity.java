package com.nibm.pharmagomadproject.customer;

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
import com.nibm.pharmagomadproject.R;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private String selectedRole = "customer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        etName            = findViewById(R.id.etName);
        etEmail           = findViewById(R.id.etEmail);
        etPhone           = findViewById(R.id.etPhone);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Role selection
        TextView roleCustomer = findViewById(R.id.roleCustomer);
        TextView rolePharmacy = findViewById(R.id.rolePharmacy);
        TextView roleRider    = findViewById(R.id.roleRider);

        roleCustomer.setOnClickListener(v -> {
            selectedRole = "customer";
            updateRoleUI(roleCustomer, rolePharmacy, roleRider);
        });
        rolePharmacy.setOnClickListener(v -> {
            selectedRole = "pharmacy";
            // Go to pharmacy registration
            startActivity(new Intent(this, RegisterPharmacyActivity.class));
        });
        roleRider.setOnClickListener(v -> {
            selectedRole = "rider";
            // Go to rider registration
            startActivity(new Intent(this, RegisterRiderActivity.class));
        });

        // Already have account
        TextView tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(v -> finish());

        // Register button
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void updateRoleUI(TextView customer, TextView pharmacy, TextView rider) {
        int primary = getResources().getColor(R.color.pg_primary, null);
        int sub     = getResources().getColor(R.color.pg_sub, null);
        customer.setBackgroundResource(selectedRole.equals("customer")
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        pharmacy.setBackgroundResource(selectedRole.equals("pharmacy")
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        rider.setBackgroundResource(selectedRole.equals("rider")
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        customer.setTextColor(selectedRole.equals("customer") ? primary : sub);
        pharmacy.setTextColor(selectedRole.equals("pharmacy") ? primary : sub);
        rider.setTextColor(selectedRole.equals("rider")    ? primary : sub);
    }

    private void attemptRegister() {
        String name    = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email   = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone   = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String pass    = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";
        CheckBox cbTerms = findViewById(R.id.cbTerms);

        if (TextUtils.isEmpty(name))  { etName.setError("Enter your name"); etName.requestFocus(); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Enter your email"); etEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Enter your phone"); etPhone.requestFocus(); return; }
        if (pass.length() < 6)        { etPassword.setError("Min 6 characters"); etPassword.requestFocus(); return; }
        if (!pass.equals(confirm))    { etConfirmPassword.setError("Passwords do not match"); etConfirmPassword.requestFocus(); return; }
        if (!cbTerms.isChecked())     { Toast.makeText(this, "Please agree to the Terms & Privacy Policy", Toast.LENGTH_SHORT).show(); return; }

        // TODO: Firebase Auth createUserWithEmailAndPassword
        Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}