package com.nibm.pharmagomadproject.Admin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private TextView tabAll, tabCustomers, tabPharmacies, tabRiders;
    private RecyclerView rvUsers;
    private ProgressBar progressUsers;
    private TextView tvEmpty;

    private UserAdapter adapter;
    private FirebaseFirestore db;
    private ListenerRegistration listenerUsers, listenerRiders;

    // Cache lists loaded from Firestore
    private final List<UserModel> allUsers = new ArrayList<>();
    private final List<UserModel> riderUsers = new ArrayList<>();

    private int currentTab = 0; // 0=all, 1=customers, 2=pharmacies, 3=riders

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        tabAll = findViewById(R.id.tabAll);
        tabCustomers = findViewById(R.id.tabCustomers);
        tabPharmacies = findViewById(R.id.tabPharmacies);
        tabRiders = findViewById(R.id.tabRiders);
        rvUsers = findViewById(R.id.rvUsers);
        progressUsers = findViewById(R.id.progressUsers);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new UserAdapter();
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        tabAll.setOnClickListener(v -> selectTab(0));
        tabCustomers.setOnClickListener(v -> selectTab(1));
        tabPharmacies.setOnClickListener(v -> selectTab(2));
        tabRiders.setOnClickListener(v -> selectTab(3));

        loadData();
    }

    private void loadData() {
        progressUsers.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        // Listen to users collection (customers + pharmacy owners)
        listenerUsers = db.collection("users").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            allUsers.clear();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                String role = doc.getString("role");
                if ("admin".equals(role)) continue; // skip admins
                UserModel u = new UserModel();
                u.setId(doc.getId());
                u.setName(doc.getString("name"));
                u.setEmail(doc.getString("email"));
                u.setPhone(doc.getString("phone"));
                u.setRole(role);
                String status = doc.getString("status");
                u.setStatus(status != null ? status : "active");
                allUsers.add(u);
            }
            refreshList();
        });

        // Listen to riders collection
        listenerRiders = db.collection("riders").addSnapshotListener((snapshots, error) -> {
            if (snapshots == null) return;
            riderUsers.clear();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                UserModel u = new UserModel();
                u.setId(doc.getId());
                u.setName(doc.getString("name"));
                u.setEmail(doc.getString("email"));
                u.setPhone(doc.getString("phone"));
                u.setRole("rider");
                String status = doc.getString("status");
                u.setStatus(status != null ? status : "pending");
                riderUsers.add(u);
            }
            refreshList();
        });
    }

    private void refreshList() {
        progressUsers.setVisibility(View.GONE);

        List<UserModel> filtered = new ArrayList<>();
        switch (currentTab) {
            case 0: // All
                filtered.addAll(allUsers);
                filtered.addAll(riderUsers);
                break;
            case 1: // Customers
                for (UserModel u : allUsers) {
                    if ("customer".equals(u.getRole())) filtered.add(u);
                }
                break;
            case 2: // Pharmacies
                for (UserModel u : allUsers) {
                    if ("pharmacy_owner".equals(u.getRole())) filtered.add(u);
                }
                break;
            case 3: // Riders
                filtered.addAll(riderUsers);
                break;
        }

        adapter.setUsers(filtered);

        boolean empty = filtered.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void selectTab(int index) {
        currentTab = index;
        resetTab(tabAll);
        resetTab(tabCustomers);
        resetTab(tabPharmacies);
        resetTab(tabRiders);

        switch (index) {
            case 0: activateTab(tabAll); break;
            case 1: activateTab(tabCustomers); break;
            case 2: activateTab(tabPharmacies); break;
            case 3: activateTab(tabRiders); break;
        }

        refreshList();
    }

    private void activateTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.tab_active_bg);
        tab.setTextColor(Color.WHITE);
        tab.setTypeface(null, Typeface.BOLD);
    }

    private void resetTab(TextView tab) {
        tab.setBackground(null);
        tab.setTextColor(ContextCompat.getColor(this, R.color.pg_sub));
        tab.setTypeface(null, Typeface.NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerUsers != null) listenerUsers.remove();
        if (listenerRiders != null) listenerRiders.remove();
    }
}
