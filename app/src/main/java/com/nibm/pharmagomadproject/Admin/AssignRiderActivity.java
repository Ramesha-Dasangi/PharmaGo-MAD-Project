package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nibm.pharmagomadproject.R;

public class AssignRiderActivity extends AppCompatActivity {

    private MaterialCardView cardRider1, cardRider2, cardRider3;
    private ImageView ivIconRider1, ivIconRider2, ivIconRider3;
    private ImageView ivCheckRider1, ivCheckRider2, ivCheckRider3;
    private MaterialButton btnAssignRider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_rider);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cardRider1 = findViewById(R.id.cardRider1);
        cardRider2 = findViewById(R.id.cardRider2);
        cardRider3 = findViewById(R.id.cardRider3);

        android.widget.TextView tvOrderNumber = findViewById(R.id.tvOrderNumber);
        android.widget.TextView tvOrderDetails = findViewById(R.id.tvOrderDetails);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ORDER_ID")) {
            tvOrderNumber.setText(intent.getStringExtra("ORDER_ID"));
            tvOrderDetails.setText(intent.getStringExtra("ORDER_DETAILS"));
        }

        ivIconRider1 = findViewById(R.id.ivIconRider1);
        ivIconRider2 = findViewById(R.id.ivIconRider2);
        ivIconRider3 = findViewById(R.id.ivIconRider3);

        ivCheckRider1 = findViewById(R.id.ivCheckRider1);
        ivCheckRider2 = findViewById(R.id.ivCheckRider2);
        ivCheckRider3 = findViewById(R.id.ivCheckRider3);

        btnAssignRider = findViewById(R.id.btnAssignRider);

        cardRider1.setOnClickListener(v -> selectRider(1, "Kamal Silva"));
        cardRider2.setOnClickListener(v -> selectRider(2, "Nuwan Fernando"));
        cardRider3.setOnClickListener(v -> selectRider(3, "Sahan Jayasuriya"));

        btnAssignRider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AssignRiderActivity.this, "Rider assigned successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_delivery);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(AssignRiderActivity.this, AdminDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_approvals) {
                startActivity(new Intent(AssignRiderActivity.this, PendingApprovalsActivity.class));
                return true;
            } else if (itemId == R.id.nav_delivery) {
                startActivity(new Intent(AssignRiderActivity.this, UnassignedOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(AssignRiderActivity.this, ComplaintsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void selectRider(int riderIndex, String riderName) {
        // Reset all
        resetRiderCard(cardRider1, ivIconRider1, ivCheckRider1);
        resetRiderCard(cardRider2, ivIconRider2, ivCheckRider2);
        resetRiderCard(cardRider3, ivIconRider3, ivCheckRider3);

        // Highlight selected
        if (riderIndex == 1) highlightRiderCard(cardRider1, ivIconRider1, ivCheckRider1);
        else if (riderIndex == 2) highlightRiderCard(cardRider2, ivIconRider2, ivCheckRider2);
        else if (riderIndex == 3) highlightRiderCard(cardRider3, ivIconRider3, ivCheckRider3);

        btnAssignRider.setText("Assign " + riderName);
    }

    private void resetRiderCard(MaterialCardView card, ImageView icon, ImageView check) {
        card.setStrokeColor(ContextCompat.getColor(this, R.color.colorStroke));
        icon.setBackgroundResource(R.drawable.icon_bg_blue);
        icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorTextSecondary)));
        check.setImageResource(android.R.drawable.checkbox_off_background);
        check.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorStroke)));
    }

    private void highlightRiderCard(MaterialCardView card, ImageView icon, ImageView check) {
        card.setStrokeColor(ContextCompat.getColor(this, R.color.colorAccent));
        icon.setBackgroundResource(R.drawable.icon_bg_green);
        icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));
        check.setImageResource(android.R.drawable.checkbox_on_background);
        check.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));
    }
}
