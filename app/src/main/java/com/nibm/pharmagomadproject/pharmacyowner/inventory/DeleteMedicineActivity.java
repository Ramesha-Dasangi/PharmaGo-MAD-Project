package com.nibm.pharmagomadproject.pharmacyowner.inventory;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;

public class DeleteMedicineActivity extends AppCompatActivity {

    private ImageView imgBack;
    private TextView txtTitle, txtDescription;
    private Button btnDeleteMedicine, btnCancel;

    private String medicineName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_medicine);

        // Initialize Views
        imgBack = findViewById(R.id.imgBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        btnDeleteMedicine = findViewById(R.id.btnDeleteMedicine);
        btnCancel = findViewById(R.id.btnCancel);

        // Get Medicine Name from Intent
        medicineName = getIntent().getStringExtra("medicineName");

        if (medicineName == null || medicineName.trim().isEmpty()) {
            medicineName = "Medicine";
        }

        txtTitle.setText("Delete " + medicineName + "?");

        txtDescription.setText(
                "Are you sure you want to permanently delete this medicine?\n\n" +
                        "Medicine : " + medicineName +
                        "\n\nThis action cannot be undone."
        );

        // Back Button
        imgBack.setOnClickListener(v -> finish());

        // Cancel Button
        btnCancel.setOnClickListener(v -> finish());

        // Delete Button
        btnDeleteMedicine.setOnClickListener(v -> showDeleteDialog());
    }

    private void showDeleteDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Medicine")
                .setMessage("Do you really want to delete \"" + medicineName + "\"?")
                .setCancelable(false)

                .setPositiveButton("Delete", (dialog, which) -> {

                    // TODO:
                    // Delete medicine from SQLite / Firebase / ArrayList

                    Toast.makeText(
                            DeleteMedicineActivity.this,
                            medicineName + " deleted successfully.",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })

                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())

                .show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}