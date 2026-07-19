package com.nibm.pharmagomadproject.pharmacyowner.inventory;


import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.pharmacyowner.NotificationHelper;
import com.nibm.pharmagomadproject.pharmacyowner.NetworkUtils;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;



public class DeleteMedicineActivity extends AppCompatActivity {

    private TextView txtTitle,
            txtDescription;


    private Button btnDeleteMedicine,
            btnCancel;



    private String medicineId;

    private String medicineName;



    private FirebaseFirestore db;
    private FirebaseAuth mAuth;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(
                R.layout.activity_delete_medicine
        );



        db =
                FirebaseFirestore.getInstance();
        mAuth =
                FirebaseAuth.getInstance();




        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());;



        txtTitle =
                findViewById(R.id.txtTitle);



        txtDescription =
                findViewById(R.id.txtDescription);



        btnDeleteMedicine =
                findViewById(R.id.btnDeleteMedicine);



        btnCancel =
                findViewById(R.id.btnCancel);





        medicineId =
                getIntent()
                        .getStringExtra("medicineId");



        medicineName =
                getIntent()
                        .getStringExtra("medicineName");





        if(medicineName == null){

            medicineName = "Medicine";

        }





        txtTitle.setText(
                "Delete " + medicineName + "?"
        );




        txtDescription.setText(

                "Are you sure you want to permanently delete this medicine?\n\n"
                        +
                        "Medicine : "
                        +
                        medicineName
                        +
                        "\n\nThis action cannot be undone."

        );


        btnCancel.setOnClickListener(v -> finish());

        btnDeleteMedicine.setOnClickListener(v -> {


            showDeleteDialog();


        });



    }









    private void showDeleteDialog(){



        new AlertDialog.Builder(this)

                .setTitle("Delete Medicine")

                .setMessage(
                        "Do you really want to delete "
                                +
                                medicineName
                                +
                                "?"
                )


                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteMedicine()
                )


                .setNegativeButton(
                        "Cancel",
                        null
                )


                .show();



    }









    private void deleteMedicine(){

        if (!NetworkUtils.isNetworkAvailable(DeleteMedicineActivity.this)) {
            Toast.makeText(DeleteMedicineActivity.this, "No Internet Connection. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String pharmacyId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        if (pharmacyId.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        btnDeleteMedicine.setEnabled(false);
        final String finalPharmacyId = pharmacyId;

        // Fetch current stock before soft deleting
        db.collection("medicines").document(medicineId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        btnDeleteMedicine.setEnabled(true);
                        Toast.makeText(DeleteMedicineActivity.this, "Medicine does not exist.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long currentStockLong = documentSnapshot.getLong("stock");
                    final int currentStock = currentStockLong != null ? currentStockLong.intValue() : 0;

                    // Update document to soft delete
                    db.collection("medicines")
                            .document(medicineId)
                            .update(
                                    "deleted", true,
                                    "updatedAt", System.currentTimeMillis()
                            )
                            .addOnSuccessListener(unused -> {
                                // Stock history update: from currentStock to 0 (since deleted)
                                if (currentStock > 0) {
                                    String histId = db.collection("stock_history").document().getId();
                                    StockHistory history = new StockHistory(
                                            histId,
                                            medicineId,
                                            medicineName,
                                            currentStock,
                                            0,
                                            -currentStock,
                                            "Medicine Deleted (Soft Delete)",
                                            finalPharmacyId,
                                            System.currentTimeMillis()
                                    );
                                    db.collection("stock_history").document(histId).set(history);
                                }

                                // Audit Log entry
                                String auditId = db.collection("inventory_audit_log").document().getId();
                                InventoryAuditLog audit = new InventoryAuditLog(
                                        auditId,
                                        "DELETE",
                                        medicineId,
                                        medicineName,
                                        "Soft deleted medicine from inventory. Remaining stock was: " + currentStock,
                                        finalPharmacyId,
                                        System.currentTimeMillis()
                                );
                                db.collection("inventory_audit_log").document(auditId).set(audit);

                                NotificationHelper.addNotification(
                                        "Medicine Deleted",
                                        medicineName + " removed from inventory.",
                                        "inventory"
                                );

                                Toast.makeText(
                                        DeleteMedicineActivity.this,
                                        "Medicine Deleted Successfully",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnDeleteMedicine.setEnabled(true);
                                Toast.makeText(
                                        DeleteMedicineActivity.this,
                                        "Failed to delete: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnDeleteMedicine.setEnabled(true);
                    Toast.makeText(DeleteMedicineActivity.this, "Error fetching medicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



}