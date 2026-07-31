package com.nibm.pharmagomadproject.pharmacyowner.inventory;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.pharmacyowner.NotificationHelper;
import com.nibm.pharmagomadproject.pharmacyowner.NetworkUtils;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;



public class EditMedicineActivity extends AppCompatActivity {



    private EditText edtName,
            edtGeneric,
            edtCategory,
            edtPrice,
            edtStock,
            edtDate;



    private Button btnSave,
            btnDelete;



    private RadioGroup radioType;



    private RadioButton rbOTC,
            rbPrescription;



    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String medicineId;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_edit_medicine);



        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();




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




        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());





        medicineId =
                getIntent()
                        .getStringExtra("medicineId");





        loadMedicine();





        btnSave.setOnClickListener(v -> updateMedicine());





        // DELETE BUTTON

        btnDelete.setOnClickListener(v -> {



            Intent intent =
                    new Intent(
                            EditMedicineActivity.this,
                            DeleteMedicineActivity.class
                    );



            intent.putExtra(
                    "medicineId",
                    medicineId
            );



            intent.putExtra(
                    "medicineName",
                    edtName.getText()
                            .toString()
            );



            startActivity(intent);



        });



    }







    private void loadMedicine(){



        db.collection("medicines")

                .document(medicineId)

                .get()

                .addOnSuccessListener(documentSnapshot -> {



                    if(documentSnapshot.exists()){



                        edtName.setText(
                                documentSnapshot.getString("medicineName")
                        );



                        edtGeneric.setText(
                                documentSnapshot.getString("brand")
                        );



                        edtCategory.setText(
                                documentSnapshot.getString("category")
                        );



                        edtPrice.setText(
                                String.valueOf(
                                        documentSnapshot.getDouble("price")
                                )
                        );



                        edtStock.setText(
                                String.valueOf(
                                        documentSnapshot.getLong("stock")
                                )
                        );



                        edtDate.setText(
                                documentSnapshot.getString("expiryDate")
                        );




                        String type =
                                documentSnapshot.getString("type");



                        if(type != null &&
                                type.equals("Prescription")){


                            rbPrescription.setChecked(true);


                        }
                        else{


                            rbOTC.setChecked(true);


                        }



                    }



                });



    }









    private void updateMedicine(){

        if (!NetworkUtils.isNetworkAvailable(EditMedicineActivity.this)) {
            Toast.makeText(EditMedicineActivity.this, "No Internet Connection. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtName.getText().toString().trim().isEmpty()) {
            edtName.setError("Name is required");
            edtName.requestFocus();
            return;
        }
        if (edtGeneric.getText().toString().trim().isEmpty()) {
            edtGeneric.setError("Brand is required");
            edtGeneric.requestFocus();
            return;
        }
        if (edtCategory.getText().toString().trim().isEmpty()) {
            edtCategory.setError("Category is required");
            edtCategory.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(edtPrice.getText().toString().trim());
            if (price <= 0) {
                edtPrice.setError("Price must be greater than zero");
                edtPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            edtPrice.setError("Invalid price format");
            edtPrice.requestFocus();
            return;
        }

        int stock;
        try {
            stock = Integer.parseInt(edtStock.getText().toString().trim());
            if (stock < 0) {
                edtStock.setError("Stock cannot be negative");
                edtStock.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            edtStock.setError("Invalid stock format");
            edtStock.requestFocus();
            return;
        }

        String date = edtDate.getText().toString().trim();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault());
            sdf.setLenient(false);
            java.util.Date expDate = sdf.parse(date);
            java.util.Calendar todayCal = java.util.Calendar.getInstance();
            todayCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            todayCal.set(java.util.Calendar.MINUTE, 0);
            todayCal.set(java.util.Calendar.SECOND, 0);
            todayCal.set(java.util.Calendar.MILLISECOND, 0);
            if (expDate != null && expDate.before(todayCal.getTime())) {
                edtDate.setError("Expiry date cannot be in the past");
                return;
            }
        } catch (java.text.ParseException e) {
            edtDate.setError("Invalid date format (dd/MM/yyyy)");
            return;
        }

        String name = edtName.getText().toString().trim();
        String brand = edtGeneric.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();
        String type = rbOTC.isChecked() ? "OTC" : "Prescription";

        String pharmacyId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        if (pharmacyId.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        final String finalPharmacyId = pharmacyId;

        // Load existing medicine first to check old stock and fetch values
        db.collection("medicines").document(medicineId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        btnSave.setEnabled(true);
                        Toast.makeText(EditMedicineActivity.this, "Medicine does not exist.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long oldStockLong = documentSnapshot.getLong("stock");
                    int oldStock = oldStockLong != null ? oldStockLong.intValue() : 0;

                    // Duplicate Check
                    db.collection("medicines")
                            .whereEqualTo("pharmacyId", finalPharmacyId)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    btnSave.setEnabled(true);
                                    Toast.makeText(EditMedicineActivity.this, "Database error: " + 
                                            (task.getException() != null ? task.getException().getMessage() : "Unknown"), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                boolean duplicate = false;
                                if (task.getResult() != null) {
                                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : task.getResult()) {
                                        if (doc.getId().equals(medicineId)) continue;
                                        String nameInDb = doc.getString("medicineName");
                                        String brandInDb = doc.getString("brand");
                                        Boolean isDeletedInDb = doc.getBoolean("deleted");
                                        boolean active = (isDeletedInDb == null || !isDeletedInDb);
                                        if (active && nameInDb != null && brandInDb != null &&
                                                nameInDb.equalsIgnoreCase(name) &&
                                                brandInDb.equalsIgnoreCase(brand)) {
                                            duplicate = true;
                                            break;
                                        }
                                    }
                                }

                                if (duplicate) {
                                    btnSave.setEnabled(true);
                                    Toast.makeText(EditMedicineActivity.this, "Another medicine with this name and brand already exists.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // Update in Firestore
                                db.collection("medicines")
                                        .document(medicineId)
                                        .update(
                                                "medicineName", name,
                                                "brand", brand,
                                                "category", category,
                                                "type", type,
                                                "price", price,
                                                "stock", stock,
                                                "expiryDate", date,
                                                "updatedAt", System.currentTimeMillis()
                                        )
                                        .addOnSuccessListener(unused -> {
                                            // Handle Stock History if stock changed
                                            if (oldStock != stock) {
                                                String histId = db.collection("stock_history").document().getId();
                                                StockHistory history = new StockHistory(
                                                        histId,
                                                        medicineId,
                                                        name,
                                                        oldStock,
                                                        stock,
                                                        stock - oldStock,
                                                        "Manual Stock Adjustment",
                                                        finalPharmacyId,
                                                        System.currentTimeMillis()
                                                );
                                                db.collection("stock_history").document(histId).set(history);
                                            }

                                            // Log to inventory audit log
                                            String auditId = db.collection("inventory_audit_log").document().getId();
                                            InventoryAuditLog audit = new InventoryAuditLog(
                                                    auditId,
                                                    "UPDATE",
                                                    medicineId,
                                                    name,
                                                    "Updated medicine. Stock changed: " + oldStock + " -> " + stock + ", price: Rs." + price,
                                                    finalPharmacyId,
                                                    System.currentTimeMillis()
                                            );
                                            db.collection("inventory_audit_log").document(auditId).set(audit);

                                            NotificationHelper.addNotification(
                                                    "Medicine Updated",
                                                    name + " details updated successfully.",
                                                    "inventory"
                                            );

                                            Toast.makeText(
                                                    EditMedicineActivity.this,
                                                    "Medicine Updated Successfully",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            btnSave.setEnabled(true);
                                            Toast.makeText(
                                                    EditMedicineActivity.this,
                                                    "Failed to update: " + e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(EditMedicineActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



}