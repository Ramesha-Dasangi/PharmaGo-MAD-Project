package com.nibm.pharmagomadproject.pharmacyowner.inventory;

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

        // EditTexts
        edtName = findViewById(R.id.edtName);
        edtGeneric = findViewById(R.id.edtGeneric);
        edtCategory = findViewById(R.id.edtCategory);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        edtDate = findViewById(R.id.edtDate);

        // Radio Buttons
        radioType = findViewById(R.id.radioType);
        rbOTC = findViewById(R.id.rbOTC);
        rbPrescription = findViewById(R.id.rbPrescription);

        // Default Selection
        rbOTC.setChecked(true);

        // Buttons
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        // Save Button
        btnSave.setOnClickListener(v -> {

            String medicineName = edtName.getText().toString().trim();
            String genericName = edtGeneric.getText().toString().trim();
            String category = edtCategory.getText().toString().trim();
            String price = edtPrice.getText().toString().trim();
            String stock = edtStock.getText().toString().trim();
            String expiryDate = edtDate.getText().toString().trim();

            String type;

            if (rbOTC.isChecked()) {
                type = "OTC";
            } else {
                type = "Prescription";
            }

            Toast.makeText(
                    EditMedicineActivity.this,
                    "Medicine Saved\nType : " + type,
                    Toast.LENGTH_SHORT
            ).show();

        });

        // Delete Button
        btnDelete.setOnClickListener(v -> {

            Toast.makeText(
                    EditMedicineActivity.this,
                    "Medicine Deleted",
                    Toast.LENGTH_SHORT
            ).show();

        });

    }
}