package com.nibm.pharmagomadproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.customer.HomeActivity;
import com.nibm.pharmagomadproject.customer.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences prefs = getSharedPreferences("pharmago_prefs", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(MainActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();

        }, 1800); // 1.8second splash delay
    }
}