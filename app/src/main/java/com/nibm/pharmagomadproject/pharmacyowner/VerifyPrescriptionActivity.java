package com.nibm.pharmagomadproject.pharmacyowner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
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

public class VerifyPrescriptionActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    private ImageView imgPrescription;
    private TextView txtFileName;

    private CheckBox chkSignature;
    private CheckBox chkMedicine;
    private CheckBox chkExpiry;

    private Button btnUploadPrescription;
    private Button btnReject;
    private Button btnApprove;

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

    // Camera

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
        setContentView(R.layout.activity_verify_prescription);

        imgPrescription = findViewById(R.id.imgPrescription);
        txtFileName = findViewById(R.id.txtFileName);

        chkSignature = findViewById(R.id.chkSignature);
        chkMedicine = findViewById(R.id.chkMedicine);
        chkExpiry = findViewById(R.id.chkExpiry);

        btnUploadPrescription = findViewById(R.id.btnUploadPrescription);

        btnReject = findViewById(R.id.btnReject);
        btnApprove = findViewById(R.id.btnApprove);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnUploadPrescription.setOnClickListener(v -> showImagePickerDialog());

        btnReject.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Prescription Rejected",
                        Toast.LENGTH_SHORT
                ).show()
        );

        btnApprove.setOnClickListener(v -> {

            if (!chkSignature.isChecked()) {
                Toast.makeText(this,
                        "Doctor signature missing",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!chkMedicine.isChecked()) {
                Toast.makeText(this,
                        "Medicine name not matched",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!chkExpiry.isChecked()) {
                Toast.makeText(this,
                        "Prescription expired",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this,
                    "Prescription Approved Successfully",
                    Toast.LENGTH_LONG).show();

            finish();

        });

    }

    // Select Camera or Gallery

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

    // Gallery / Files

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("image/*");

        galleryLauncher.launch(intent);

    }

    // Camera

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

    // Permission Result

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