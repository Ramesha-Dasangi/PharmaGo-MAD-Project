package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

public class OrderTrackingActivity extends AppCompatActivity {

    // Rider phone number — in real app this comes from Firebase
    private static final String RIDER_PHONE = "0771234567";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_tracking);
        getSupportActionBar().hide();

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Call rider button
        ImageView btnCall = findViewById(R.id.btnCallRider);
        btnCall.setOnClickListener(v -> {
            Intent call = new Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:" + RIDER_PHONE));
            startActivity(call);
        });

        // Report issue button → activity_report_issue
        MaterialButton btnReport = findViewById(R.id.btnReportIssue);
        btnReport.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportIssueActivity.class));
        });
    }
}
