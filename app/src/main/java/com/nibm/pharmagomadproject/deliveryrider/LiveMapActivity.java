package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.nibm.pharmagomadproject.R;

public class LiveMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupButtons();
    }

    private void setupButtons() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent i = new Intent(this, RiderDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) {
            navHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, DeliveryHistoryActivity.class)));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, RiderProfileActivity.class)));
        }

        Button btnRecenter = findViewById(R.id.btnRecenter);
        if (btnRecenter != null) {
            btnRecenter.setText("Open in Google Maps");
            btnRecenter.setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=6.9350,79.8550");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            });
        }
    }
}
