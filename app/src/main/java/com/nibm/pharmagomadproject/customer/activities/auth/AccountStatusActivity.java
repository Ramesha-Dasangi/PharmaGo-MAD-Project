package com.nibm.pharmagomadproject.customer.activities.auth;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;

public class AccountStatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_status);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}