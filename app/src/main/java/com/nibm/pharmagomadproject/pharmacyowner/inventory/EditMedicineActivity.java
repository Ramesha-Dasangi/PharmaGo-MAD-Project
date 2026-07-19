package com.nibm.pharmagomadproject.pharmacyowner.inventory;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.FirebaseFirestore;

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



    private String medicineId;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_edit_medicine);



        db = FirebaseFirestore.getInstance();




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



        String name =
                edtName.getText()
                        .toString()
                        .trim();



        String brand =
                edtGeneric.getText()
                        .toString()
                        .trim();



        String category =
                edtCategory.getText()
                        .toString()
                        .trim();



        double price =
                Double.parseDouble(
                        edtPrice.getText()
                                .toString()
                );



        int stock =
                Integer.parseInt(
                        edtStock.getText()
                                .toString()
                );



        String date =
                edtDate.getText()
                        .toString();



        String type =
                rbOTC.isChecked()
                        ?
                        "OTC"
                        :
                        "Prescription";





        db.collection("medicines")

                .document(medicineId)

                .update(


                        "medicineName",name,

                        "brand",brand,

                        "category",category,

                        "type",type,

                        "price",price,

                        "stock",stock,

                        "expiryDate",date


                )


                .addOnSuccessListener(unused -> {



                    Toast.makeText(
                            this,
                            "Medicine Updated Successfully",
                            Toast.LENGTH_SHORT
                    ).show();



                    finish();



                })



                .addOnFailureListener(e -> {



                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();



                });



    }



}