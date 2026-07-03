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

        recyclerView = findViewById(R.id.recyclerOrders);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnNew = findViewById(R.id.btnNew);
        btnProcessing = findViewById(R.id.btnProcessing);
        btnCompleted = findViewById(R.id.btnCompleted);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allOrders = new ArrayList<>();
        orderList = new ArrayList<>();

        // Sample Data

        allOrders.add(new OrderModel(
                "#ORD001",
                "Buddhini Perera",
                "Paracetamol x2",
                "09:30 AM",
                "Rs.450",
                "Rx Required",
                "New"));

        allOrders.add(new OrderModel(
                "#ORD002",
                "Nimal Silva",
                "Vitamin C",
                "10:10 AM",
                "Rs.850",
                "OTC",
                "Processing"));

        allOrders.add(new OrderModel(
                "#ORD003",
                "Kasun Perera",
                "Panadol",
                "10:45 AM",
                "Rs.350",
                "OTC",
                "New"));

        allOrders.add(new OrderModel(
                "#ORD004",
                "Sanduni Fernando",
                "Amoxicillin",
                "11:20 AM",
                "Rs.950",
                "Rx Required",
                "Completed"));

        allOrders.add(new OrderModel(
                "#ORD005",
                "Saman Kumara",
                "Insulin",
                "12:15 PM",
                "Rs.2500",
                "Rx Required",
                "New"));

        allOrders.add(new OrderModel(
                "#ORD006",
                "Thilini",
                "Vitamin D",
                "01:30 PM",
                "Rs.780",
                "OTC",
                "Completed"));

        adapter = new OrderAdapter(OrdersActivity.this, orderList);
        recyclerView.setAdapter(adapter);

        updateCounts();
        showOrders("New");
        Toast.makeText(this,
                "Orders : " + orderList.size(),
                Toast.LENGTH_LONG).show();
        highlightButton(btnNew);

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

        // Bottom Navigation

        bottomNavigation.setSelectedItemId(R.id.nav_orders);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                startActivity(new Intent(OrdersActivity.this,
                        DashboardActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_orders) {

                return true;

            } else if (id == R.id.nav_inventory) {

                startActivity(new Intent(OrdersActivity.this,
                        InventoryActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_reports) {

                startActivity(new Intent(OrdersActivity.this,
                        SalesReportActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_profile) {

                startActivity(new Intent(OrdersActivity.this,
                        ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });

    }

    private void showOrders(String status) {

        orderList.clear();

        for (OrderModel order : allOrders) {

            if (order.getStatus().equalsIgnoreCase(status)) {

                orderList.add(order);

            }

        }

        adapter.notifyDataSetChanged();

    }

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

    private void highlightButton(Button selectedButton) {

        Button[] buttons = {btnNew, btnProcessing, btnCompleted};

        for (Button button : buttons) {

            button.setBackgroundTintList(
                    getColorStateList(R.color.light));

            button.setTextColor(
                    getColor(R.color.green));
        }

        selectedButton.setBackgroundTintList(
                getColorStateList(R.color.green));

        selectedButton.setTextColor(Color.WHITE);

    }
}