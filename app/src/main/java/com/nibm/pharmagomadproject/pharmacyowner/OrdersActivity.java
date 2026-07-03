package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;

import com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity;
import com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigation;

    private Button btnNew, btnProcessing, btnCompleted;

    private ArrayList<OrderModel> allOrders;
    private ArrayList<OrderModel> orderList;

    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // INIT VIEWS
        recyclerView = findViewById(R.id.recyclerOrders);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnNew = findViewById(R.id.btnNew);
        btnProcessing = findViewById(R.id.btnProcessing);
        btnCompleted = findViewById(R.id.btnCompleted);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // LISTS
        allOrders = new ArrayList<>();
        orderList = new ArrayList<>();

        // SAMPLE DATA
        allOrders.add(new OrderModel("#ORD001","Buddhini Perera","Paracetamol x2","09:30 AM","Rs.450","Rx Required","New"));
        allOrders.add(new OrderModel("#ORD002","Nimal Silva","Vitamin C","10:10 AM","Rs.850","OTC","Processing"));
        allOrders.add(new OrderModel("#ORD003","Kasun Perera","Panadol","10:45 AM","Rs.350","OTC","New"));
        allOrders.add(new OrderModel("#ORD004","Sanduni Fernando","Amoxicillin","11:20 AM","Rs.950","Rx Required","Completed"));
        allOrders.add(new OrderModel("#ORD005","Saman Kumara","Insulin","12:15 PM","Rs.2500","Rx Required","New"));
        allOrders.add(new OrderModel("#ORD006","Thilini","Vitamin D","01:30 PM","Rs.780","OTC","Completed"));

        // ADAPTER
        adapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(adapter);

        // DEFAULT LOAD
        showOrders("New");
        updateCounts();
        highlightButton(btnNew);

        Toast.makeText(this,
                "Orders: " + orderList.size(),
                Toast.LENGTH_SHORT).show();

        // FILTER BUTTONS
        btnNew.setOnClickListener(v -> {
            showOrders("New");
            highlightButton(btnNew);
        });

        btnProcessing.setOnClickListener(v -> {
            showOrders("Processing");
            highlightButton(btnProcessing);
        });

        btnCompleted.setOnClickListener(v -> {
            showOrders("Completed");
            highlightButton(btnCompleted);
        });

        // BOTTOM NAV
        bottomNavigation.setSelectedItemId(R.id.nav_orders);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_orders) {
                return true;

            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryActivity.class));
                finish();
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

    // FILTER ORDERS
    private void showOrders(String status) {

        orderList.clear();

        for (OrderModel order : allOrders) {
            if (order.getStatus().equalsIgnoreCase(status)) {
                orderList.add(order);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // COUNT BUTTONS
    private void updateCounts() {

        int newCount = 0;
        int processingCount = 0;
        int completedCount = 0;

        for (OrderModel order : allOrders) {

            switch (order.getStatus()) {
                case "New":
                    newCount++;
                    break;

                case "Processing":
                    processingCount++;
                    break;

                case "Completed":
                    completedCount++;
                    break;
            }
        }

        btnNew.setText("New (" + newCount + ")");
        btnProcessing.setText("Processing (" + processingCount + ")");
        btnCompleted.setText("Completed (" + completedCount + ")");
    }

    // BUTTON HIGHLIGHT
    private void highlightButton(Button selectedButton) {

        Button[] buttons = {btnNew, btnProcessing, btnCompleted};

        for (Button b : buttons) {
            b.setBackgroundTintList(getColorStateList(R.color.light));
            b.setTextColor(getColor(R.color.green));
        }

        selectedButton.setBackgroundTintList(getColorStateList(R.color.green));
        selectedButton.setTextColor(Color.WHITE);
    }
}