package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;

import com.nibm.pharmagomadproject.pharmacyowner.inventory.AddMedicineActivity;
import com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity;
import com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerInventory;
    private InventoryAdapter adapter;

    private ArrayList<InventoryModel> inventoryList;
    private ArrayList<InventoryModel> filteredList;

    private EditText edtSearch;
    private Button btnAll, btnLowStock, btnOutStock;
    private ImageView imgAdd;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        recyclerInventory = findViewById(R.id.recyclerInventory);
        edtSearch = findViewById(R.id.edtSearch);

        btnAll = findViewById(R.id.btnAll);
        btnLowStock = findViewById(R.id.btnLowStock);
        btnOutStock = findViewById(R.id.btnOutStock);

        imgAdd = findViewById(R.id.imgAdd);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        recyclerInventory.setLayoutManager(new LinearLayoutManager(this));

        inventoryList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // sample data
        inventoryList.add(new InventoryModel("Panadol 500mg", "Painkiller · OTC", "Rs.40", 8, 100));
        inventoryList.add(new InventoryModel("Amoxicillin 500mg", "Antibiotic · Rx", "Rs.85", 52, 100));
        inventoryList.add(new InventoryModel("Vitamin C", "Supplement · OTC", "Rs.37", 120, 150));

        filteredList.addAll(inventoryList);

        adapter = new InventoryAdapter(this, filteredList);
        recyclerInventory.setAdapter(adapter);

        // ================= SEARCH =================
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchMedicine(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // ================= FILTER =================
        btnAll.setOnClickListener(v -> {
            filteredList.clear();
            filteredList.addAll(inventoryList);
            adapter.updateList(filteredList);
        });

        btnLowStock.setOnClickListener(v -> {
            ArrayList<InventoryModel> temp = new ArrayList<>();
            for (InventoryModel item : inventoryList) {
                if (item.getStock() > 0 && item.getStock() < 20) {
                    temp.add(item);
                }
            }
            adapter.updateList(temp);
        });

        btnOutStock.setOnClickListener(v -> {
            ArrayList<InventoryModel> temp = new ArrayList<>();
            for (InventoryModel item : inventoryList) {
                if (item.getStock() == 0) {
                    temp.add(item);
                }
            }
            adapter.updateList(temp);
        });

        // ================= ADD BUTTON FIXED =================
        imgAdd.setOnClickListener(v -> {
            Intent intent = new Intent(
                    InventoryActivity.this,
                    AddMedicineActivity.class   // 👉 THIS SCREEN
            );
            startActivity(intent);
        });

        // ================= BOTTOM NAV =================
        bottomNavigation.setSelectedItemId(R.id.nav_inventory);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrdersActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_inventory) {
                return true;

            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(this, SalesReportActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void searchMedicine(String keyword) {

        ArrayList<InventoryModel> temp = new ArrayList<>();

        for (InventoryModel item : inventoryList) {
            if (item.getMedicineName().toLowerCase().contains(keyword.toLowerCase())) {
                temp.add(item);
            }
        }

        adapter.updateList(temp);
    }
}