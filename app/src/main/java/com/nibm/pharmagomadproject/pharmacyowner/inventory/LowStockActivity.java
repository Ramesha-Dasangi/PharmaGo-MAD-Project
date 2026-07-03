package com.nibm.pharmagomadproject.pharmacyowner.inventory;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.pharmacyowner.InventoryModel;
import com.nibm.pharmagomadproject.pharmacyowner.LowStockAdapter;

import java.util.ArrayList;

public class LowStockActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    LowStockAdapter adapter;
    ArrayList<InventoryModel> lowStockList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_stock);

        recyclerView = findViewById(R.id.recyclerLowStock);

        lowStockList = new ArrayList<>();

        // SAMPLE DATA
        lowStockList.add(new InventoryModel(
                "Panadol 500mg",
                "Painkiller",
                "Rs.40",
                8,
                100
        ));
        lowStockList.add(new InventoryModel(
                "Vitamin C",
                "Supplement",
                "Rs.350",
                5,
                100
                ));

        lowStockList.add(new InventoryModel(
                "Amoxicillin",
                "Antibiotic",
                "Rs.450",
                2,
                40
                ));
        lowStockList.add(new InventoryModel(
                "Paracetamol",
                "Tablet",
                "Rs.200",
                7,
                60));
        lowStockList.add(new InventoryModel(
                "Paracetamol",
                "Tablet",
                "Rs.200",
                7,
                60));

        adapter = new LowStockAdapter(this, lowStockList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}