package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;
import com.google.android.material.card.MaterialCardView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MaterialCardView cardManageUsers = findViewById(R.id.cardManageUsers);
        cardManageUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ManageUsersActivity.class));
            }
        });

        MaterialCardView cardGenerateReports = findViewById(R.id.cardGenerateReports);
        cardGenerateReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, GenerateReportsActivity.class));
            }
        });
    }
}
