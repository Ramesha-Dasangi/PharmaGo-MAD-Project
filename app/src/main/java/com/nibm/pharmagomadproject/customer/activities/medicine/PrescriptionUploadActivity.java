package com.nibm.pharmagomadproject.customer.activities.medicine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.order.OrderTrackingActivity;

public class PrescriptionUploadActivity extends AppCompatActivity {

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Toast.makeText(this, "Prescription selected!", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    Toast.makeText(this, "Photo taken!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prescription_upload);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Upload area tap → open gallery
        findViewById(R.id.uploadArea).setOnClickListener(v ->
                galleryLauncher.launch("image/*"));

        // Gallery button
        MaterialButton btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // Camera button
        MaterialButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(v -> {
            // Simple camera intent fallback
            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (camera.resolveActivity(getPackageManager()) != null) {
                startActivity(camera);
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Submit prescription
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitPrescription);
        btnSubmit.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please upload a prescription first",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: upload to Firebase Storage, then save reference to Firestore
            Toast.makeText(this, "Prescription submitted! Awaiting pharmacy verification.",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, OrderTrackingActivity.class));
            finish();
        });
    }
}