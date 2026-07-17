package com.nibm.pharmagomadproject.pharmacyowner.inventory;


import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.FirebaseFirestore;

import com.nibm.pharmagomadproject.R;



public class DeleteMedicineActivity extends AppCompatActivity {

    private TextView txtTitle,
            txtDescription;


    private Button btnDeleteMedicine,
            btnCancel;



    private String medicineId;

    private String medicineName;



    private FirebaseFirestore db;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(
                R.layout.activity_delete_medicine
        );



        db =
                FirebaseFirestore.getInstance();




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



        db.collection("medicines")

                .document(medicineId)

                .delete()



                .addOnSuccessListener(unused -> {



                    Toast.makeText(

                            this,

                            "Medicine Deleted Successfully",

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