package com.nibm.pharmagomadproject.pharmacyowner.inventory;


import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.pharmacyowner.InventoryModel;
import com.nibm.pharmagomadproject.pharmacyowner.LowStockAdapter;


import java.util.ArrayList;


public class LowStockActivity extends AppCompatActivity {


    RecyclerView recyclerView;

    LowStockAdapter adapter;

    ArrayList<InventoryModel> lowStockList;

    FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_low_stock);


        recyclerView = findViewById(R.id.recyclerLowStock);


        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());



        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );



        lowStockList = new ArrayList<>();


        adapter = new LowStockAdapter(
                this,
                lowStockList
        );


        recyclerView.setAdapter(adapter);



        db = FirebaseFirestore.getInstance();



        loadLowStockMedicines();

    }




    private void loadLowStockMedicines(){


        db.collection("medicines")


                .whereLessThanOrEqualTo(
                        "stock",
                        10
                )


                .get()


                .addOnSuccessListener(queryDocumentSnapshots -> {



                    lowStockList.clear();



                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){



                        InventoryModel model =
                                new InventoryModel();



                        // Document ID save
                        model.setMedicineId(
                                document.getId()
                        );



                        model.setMedicineName(
                                document.getString("medicineName")
                        );



                        model.setCategory(
                                document.getString("category")
                        );



                        Long stock =
                                document.getLong("stock");



                        if(stock != null){

                            model.setStock(
                                    stock.intValue()
                            );

                        }



                        Long price =
                                document.getLong("price");



                        if(price != null){

                            model.setPrice(
                                    price.doubleValue()
                            );

                        }



                        // Reorder level
                        model.setMaxStock(10);



                        lowStockList.add(model);



                    }



                    adapter.notifyDataSetChanged();



                    if(lowStockList.isEmpty()){


                        Toast.makeText(
                                this,
                                "No Low Stock Medicines",
                                Toast.LENGTH_SHORT
                        ).show();

                    }



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