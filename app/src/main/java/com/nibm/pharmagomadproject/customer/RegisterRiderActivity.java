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

public class RegisterRiderActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText etName, etNic, etEmail, etPhone, etVehicleType, etVehicleReg, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_rider);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        etName       = findViewById(R.id.etName);
        etNic        = findViewById(R.id.etNic);
        etEmail      = findViewById(R.id.etEmail);
        etPhone      = findViewById(R.id.etPhone);
        etVehicleType= findViewById(R.id.etVehicleType);
        etVehicleReg = findViewById(R.id.etVehicleReg);
        // ⚠️ Add a password field in XML (not shown in your layout yet)
        etPassword   = new TextInputEditText(this);

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
                        Toast.makeText(this, "Rider account registered!", Toast.LENGTH_SHORT).show();
                        finish(); // go back to login
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
