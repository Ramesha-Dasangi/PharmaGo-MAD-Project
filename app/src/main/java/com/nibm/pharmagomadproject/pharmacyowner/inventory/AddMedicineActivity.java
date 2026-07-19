package com.nibm.pharmagomadproject.pharmacyowner.inventory;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.nibm.pharmagomadproject.R;

import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    // Inputs
    private EditText edtName, edtBrand, edtPrice, edtStock, edtDate, edtDescription;
    private AutoCompleteTextView edtCategory;

    private RadioGroup radioType;
    private RadioButton rbOTC, rbRx;

    private Button btnSave;

    private ImageView imgMedicine;
    private LinearLayout layoutImage;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        //========================
        // Initialize Views
        //========================

        edtName = findViewById(R.id.edtName);
        edtCategory = findViewById(R.id.edtCategory);
        edtBrand = findViewById(R.id.edtBrand);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        edtDate = findViewById(R.id.edtDate);
        edtDescription = findViewById(R.id.edtDescription);

        radioType = findViewById(R.id.radioType);
        rbOTC = findViewById(R.id.rbOTC);
        rbRx = findViewById(R.id.rbRx);

        imgMedicine = findViewById(R.id.imgMedicine);
        layoutImage = findViewById(R.id.layoutImage);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnSave = findViewById(R.id.btnSave);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        //========================
        // Category Dropdown
        //========================

        String[] categories = {
                "Rx",
                "First Aid",
                "Vitamins",
                "Chronic",
                "Baby",
                "Eye Care",
                "Dental",
                "OTC"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        categories
                );

        edtCategory.setAdapter(adapter);

        edtCategory.setOnClickListener(v -> edtCategory.showDropDown());

        edtCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                edtCategory.showDropDown();
            }
        });

        //========================
        // Date Picker
        //========================

        edtDate.setOnClickListener(v -> showDatePicker());

        //========================
        // Camera Launcher
        //========================

        cameraLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() == RESULT_OK &&
                                    result.getData() != null &&
                                    result.getData().getExtras() != null) {

                                Bitmap bitmap =
                                        (Bitmap) result.getData()
                                                .getExtras()
                                                .get("data");

                                imgMedicine.setImageBitmap(bitmap);
                            }
                        });

        //========================
        // Gallery Launcher
        //========================

        galleryLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() == RESULT_OK &&
                                    result.getData() != null) {

                                Uri uri = result.getData().getData();

                                imgMedicine.setImageURI(uri);
                            }
                        });

        //========================
        // Upload Image
        //========================

        layoutImage.setOnClickListener(v -> showImagePickerDialog());

        //========================
        // Save Button
        //========================

        btnSave.setOnClickListener(v -> {

            if (edtName.getText().toString().trim().isEmpty()) {
                edtName.setError("Medicine name required");
                edtName.requestFocus();
                return;
            }

            if (edtCategory.getText().toString().trim().isEmpty()) {
                edtCategory.setError("Select category");
                edtCategory.requestFocus();
                return;
            }

            if (edtBrand.getText().toString().trim().isEmpty()) {
                edtBrand.setError("Brand name required");
                edtBrand.requestFocus();
                return;
            }

            if (edtPrice.getText().toString().trim().isEmpty()) {
                edtPrice.setError("Price required");
                edtPrice.requestFocus();
                return;
            }

            if (edtStock.getText().toString().trim().isEmpty()) {
                edtStock.setError("Stock required");
                edtStock.requestFocus();
                return;
            }

            if (edtDate.getText().toString().trim().isEmpty()) {
                edtDate.setError("Expiry date required");
                return;
            }

            if (edtDescription.getText().toString().trim().isEmpty()) {
                edtDescription.setError("Description required");
                edtDescription.requestFocus();
                return;
            }

            if (radioType.getCheckedRadioButtonId() == -1) {

                Toast.makeText(
                        this,
                        "Select medicine type",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String medicineName = edtName.getText().toString().trim();
            String category = edtCategory.getText().toString().trim();
            String brand = edtBrand.getText().toString().trim();

            String type = rbOTC.isChecked() ? "OTC" : "Prescription";

            double price = Double.parseDouble(edtPrice.getText().toString().trim());
            int stock = Integer.parseInt(edtStock.getText().toString().trim());

            String expiryDate = edtDate.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();

            String pharmacyId="";


            if(mAuth.getCurrentUser()!=null){

                pharmacyId =
                        mAuth.getCurrentUser().getUid();

            }

            Medicine medicine = new Medicine(
                    medicineName,
                    category,
                    brand,
                    type,
                    price,
                    stock,
                    expiryDate,
                    description,
                    "",   // imageUrl (දැනට හිස්)
                    pharmacyId,
                    System.currentTimeMillis()
            );

            db.collection("medicines")
                    .add(medicine)
                    .addOnSuccessListener(documentReference -> {

                        Toast.makeText(
                                AddMedicineActivity.this,
                                "Medicine Added Successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();

                    })
                    .addOnFailureListener(e ->

                            Toast.makeText(
                                    AddMedicineActivity.this,
                                    e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()

                    );

        });

    }

    //========================
    // Date Picker
    //========================

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) ->

                                edtDate.setText(
                                        dayOfMonth + "/" +
                                                (month + 1) + "/" +
                                                year),

                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    //========================
    // Image Picker Dialog
    //========================

    private void showImagePickerDialog() {

        String[] options = {
                "Camera",
                "Gallery"
        };

        new AlertDialog.Builder(this)
                .setTitle("Upload Image")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {

                        checkCameraPermission();

                    } else {

                        openGallery();

                    }

                })
                .show();
    }

    //========================
    // Camera Permission
    //========================

    private void checkCameraPermission() {

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

    }

    //========================
    // Open Camera
    //========================

    private void openCamera() {

        Intent intent =
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        cameraLauncher.launch(intent);

    }

    //========================
    // Open Gallery
    //========================

    private void openGallery() {

        Intent intent =
                new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        galleryLauncher.launch(intent);

    }

    //========================
    // Permission Result
    //========================

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