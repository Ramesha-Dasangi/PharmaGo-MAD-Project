package com.nibm.pharmagomadproject.pharmacyowner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nibm.pharmagomadproject.R;

public class OrderDetailsActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private TextView txtOrderId;
    private TextView txtDate;
    private TextView txtCustomer;
    private TextView txtPhone;
    private TextView txtAddress;
    private TextView txtTotal;

    private ImageView imgPrescription;

    private TextView txtFileName;
    private ImageView btnBack;

    private Button btnUploadPrescription;
    private Button btnReject;
    private Button btnApprove;

    // ==========================
    // Gallery Launcher
    // ==========================

    //==========================
    // Gallery / Files Picker
    //==========================

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK &&
                                result.getData() != null &&
                                result.getData().getData() != null) {

                            Uri uri = result.getData().getData();

                            imgPrescription.setImageURI(uri);

                            txtFileName.setText("Prescription Selected");

                        }

                    });


    // ==========================
    // Camera
    //==========================

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK &&
                                result.getData() != null) {

                            Bundle extras = result.getData().getExtras();

                            if (extras != null) {

                                Bitmap bitmap = (Bitmap) extras.get("data");

                                imgPrescription.setImageBitmap(bitmap);

                                txtFileName.setText("Prescription Captured");
                            }
                        }

                    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // ==========================
        // Initialize Views
        // ==========================

        btnBack = findViewById(R.id.btnBack);

        txtOrderId = findViewById(R.id.txtOrderId);
        txtDate = findViewById(R.id.txtDate);
        txtCustomer = findViewById(R.id.txtCustomer);
        txtPhone = findViewById(R.id.txtPhone);
        txtAddress = findViewById(R.id.txtAddress);
        txtTotal = findViewById(R.id.txtTotal);

        imgPrescription = findViewById(R.id.imgPrescription);
        txtFileName = findViewById(R.id.txtFileName);

        btnUploadPrescription = findViewById(R.id.btnUploadPrescription);

        btnReject = findViewById(R.id.btnReject);
        btnApprove = findViewById(R.id.btnApprove);

        // ==========================
        // Back Button
        // ==========================

        btnBack.setOnClickListener(v -> finish());

        // ==========================
        // Receive Data
        // ==========================

        String orderId = getIntent().getStringExtra("orderId");
        String customer = getIntent().getStringExtra("customerName");
        String time = getIntent().getStringExtra("time");
        String amount = getIntent().getStringExtra("amount");

        if (orderId != null)
            txtOrderId.setText(orderId);

        if (time != null)
            txtDate.setText(time);

        if (customer != null)
            txtCustomer.setText(customer);

        txtPhone.setText("077 123 4567");
        txtAddress.setText("Colombo, Sri Lanka");

        if (amount != null)
            txtTotal.setText("Total : " + amount);

        // ==========================
        // Upload Button
        // ==========================

        btnUploadPrescription.setOnClickListener(v -> showImagePickerDialog());

        // ==========================
        // Approve
        // ==========================

        btnApprove.setOnClickListener(v ->

                Toast.makeText(
                        OrderDetailsActivity.this,
                        "Order Approved Successfully",
                        Toast.LENGTH_SHORT
                ).show()

        );

        // ==========================
        // Reject
        // ==========================

        btnReject.setOnClickListener(v ->

                Toast.makeText(
                        OrderDetailsActivity.this,
                        "Order Rejected",
                        Toast.LENGTH_SHORT
                ).show()

        );

    }

    // ==========================
    // Camera / Gallery Dialog
    // ==========================

    private void showImagePickerDialog() {

        String[] options = {
                "Take Photo",
                "Choose from Gallery / Files"
        };

        new AlertDialog.Builder(this)
                .setTitle("Upload Prescription")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {

                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(
                                    this,
                                    new String[]{Manifest.permission.CAMERA},
                                    CAMERA_PERMISSION_CODE);

                        } else {

                            openCamera();

                        }

                    } else {

                        openGallery();

                    }

                })
                .show();

    }

    // ==========================
    // Open Camera
    // ==========================

    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {

            cameraLauncher.launch(intent);

        } else {

            Toast.makeText(
                    this,
                    "Camera not available",
                    Toast.LENGTH_SHORT
            ).show();

        }

    }


    //==================================================
    // Gallery / Files
    //==================================================

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("image/*");

        galleryLauncher.launch(intent);

    }

    // ==========================
    // Permission Result
    // ==========================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                openCamera();

            } else {

                Toast.makeText(
                        this,
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT
                ).show();

            }

        }

    }

}