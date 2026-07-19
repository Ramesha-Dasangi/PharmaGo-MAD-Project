package com.nibm.pharmagomadproject.pharmacyowner.inventory;


import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.pharmacyowner.InventoryModel;
import com.nibm.pharmagomadproject.pharmacyowner.LowStockAdapter;
import com.nibm.pharmagomadproject.pharmacyowner.NetworkUtils;


import java.util.ArrayList;


public class LowStockActivity extends AppCompatActivity {


    RecyclerView recyclerView;

    LowStockAdapter adapter;

    ArrayList<InventoryModel> lowStockList;

    FirebaseFirestore db;
    FirebaseAuth mAuth;



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
        mAuth = FirebaseAuth.getInstance();

        loadLowStockMedicines();

    }




    private void loadLowStockMedicines(){

        if (!NetworkUtils.isNetworkAvailable(LowStockActivity.this)) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String ownerId = user.getUid();

        db.collection("medicines")
                .whereEqualTo("pharmacyId", ownerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    lowStockList.clear();

                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){

                        Boolean deleted = document.getBoolean("deleted");
                        if (Boolean.TRUE.equals(deleted)) {
                            continue; // Skip soft deleted items
                        }

                        Long stock = document.getLong("stock");
                        if (stock != null && stock <= 20) {   // threshold = 20
                            InventoryModel model = new InventoryModel();
                            model.setMedicineId(document.getId());
                            model.setMedicineName(document.getString("medicineName"));
                            model.setCategory(document.getString("category"));
                            model.setStock(stock.intValue());

                            Double price = document.getDouble("price");
                            if(price != null){
                                model.setPrice(price);
                            }

                            model.setMaxStock(20);   // threshold = 20
                            lowStockList.add(model);
                        }
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
                            "Error loading low stock: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


}