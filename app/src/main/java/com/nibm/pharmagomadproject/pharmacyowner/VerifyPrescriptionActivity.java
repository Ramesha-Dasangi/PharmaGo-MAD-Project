package com.nibm.pharmagomadproject.pharmacyowner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;

public class VerifyPrescriptionActivity extends AppCompatActivity {

    ImageButton btnBack;

    CheckBox chkSignature;
    CheckBox chkMedicine;
    CheckBox chkExpiry;

    Button btnReject;
    Button btnApprove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_prescription);

        btnBack = findViewById(R.id.btnBack);

        chkSignature = findViewById(R.id.chkSignature);
        chkMedicine = findViewById(R.id.chkMedicine);
        chkExpiry = findViewById(R.id.chkExpiry);

        btnReject = findViewById(R.id.btnReject);
        btnApprove = findViewById(R.id.btnApprove);

        // Back

        btnBack.setOnClickListener(v -> finish());

        // Reject

        btnReject.setOnClickListener(v ->

                Toast.makeText(
                        this,
                        "Order Rejected",
                        Toast.LENGTH_SHORT
                ).show());

        // Approve

        btnApprove.setOnClickListener(v -> {

            if (!chkSignature.isChecked()) {

                Toast.makeText(this,
                        "Doctor signature missing",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            if (!chkMedicine.isChecked()) {

                Toast.makeText(this,
                        "Medicine does not match",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            if (!chkExpiry.isChecked()) {

                Toast.makeText(this,
                        "Prescription expired",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            Toast.makeText(this,
                    "Prescription Approved",
                    Toast.LENGTH_LONG).show();

            finish();

        });

    }

}