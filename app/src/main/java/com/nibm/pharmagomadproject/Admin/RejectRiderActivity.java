package com.nibm.pharmagomadproject.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;
import com.google.android.material.button.MaterialButton;

public class RejectRiderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reject_rider);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MaterialButton btnConfirmReject = findViewById(R.id.btnConfirmReject);
        btnConfirmReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RejectRiderActivity.this, "Rider application rejected", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
