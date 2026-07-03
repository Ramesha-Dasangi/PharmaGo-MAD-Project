package com.nibm.pharmagomadproject.pharmacyowner.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;

public class EditMedicineActivity extends AppCompatActivity {

    private EditText edtName, edtGeneric, edtCategory, edtPrice, edtStock, edtDate;

    private Button btnSave, btnDelete;

    private RadioGroup radioType;
    private RadioButton rbOTC, rbPrescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medicine);

        // Initialize Views
        edtName = findViewById(R.id.edtName);
        edtGeneric = findViewById(R.id.edtGeneric);
        edtCategory = findViewById(R.id.edtCategory);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        edtDate = findViewById(R.id.edtDate);

        radioType = findViewById(R.id.radioType);
        rbOTC = findViewById(R.id.rbOTC);
        rbPrescription = findViewById(R.id.rbPrescription);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        // Default Selection
        rbOTC.setChecked(true);

        // Save Button
        btnSave.setOnClickListener(v -> {

            String type = rbOTC.isChecked() ? "OTC" : "Prescription";

            Toast.makeText(
                    EditMedicineActivity.this,
                    "Medicine Saved Successfully!\nType : " + type,
                    Toast.LENGTH_SHORT
            ).show();

            // TODO: Update medicine in database

        });

        // Delete Button
        btnDelete.setOnClickListener(v -> {

            Intent intent = new Intent(
                    EditMedicineActivity.this,
                    DeleteMedicineActivity.class
            );

            intent.putExtra(
                    "medicineName",
                    edtName.getText().toString().trim()
            );

            startActivity(intent);

        });

    }
}