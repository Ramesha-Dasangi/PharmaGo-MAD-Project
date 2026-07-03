package com.nibm.pharmagomadproject.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.nibm.pharmagomadproject.R;

public class RegisterPharmacyActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText etPharmacyName, etOwnerName, etLicenseNo, etEmail, etPhone, etAddress, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_pharmacy);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        etPharmacyName = findViewById(R.id.etPharmacyName);
        etOwnerName    = findViewById(R.id.etOwnerName);
        etLicenseNo    = findViewById(R.id.etLicenseNo);
        etEmail        = findViewById(R.id.etEmail);
        etPhone        = findViewById(R.id.etPhone);
        etAddress      = findViewById(R.id.etAddress);
        etPassword     = new TextInputEditText(this); // add password field in XML if needed

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitForApproval);
        btnSubmit.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pass  = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) { etEmail.setError("Enter email"); etEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) { etPassword.setError("Min 6 characters"); etPassword.requestFocus(); return; }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Pharmacy account registered!", Toast.LENGTH_SHORT).show();
                        finish(); // go back to login
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
