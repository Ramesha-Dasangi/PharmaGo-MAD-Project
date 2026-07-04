package com.nibm.pharmagomadproject.pharmacyowner.inventory;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nibm.pharmagomadproject.R;

import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    // INPUTS
    private EditText edtName, edtCategory, edtPrice, edtStock, edtDate;
    private Button btnSave;

    // IMAGE SECTION (MATCH XML IDs)
    private ImageView imgMedicine;
    private LinearLayout layoutImage;

    // LAUNCHERS
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        // ================= INIT VIEWS =================
        edtName = findViewById(R.id.edtName);
        edtCategory = findViewById(R.id.edtCategory);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        edtDate = findViewById(R.id.edtDate);

        imgMedicine = findViewById(R.id.imgMedicine);
        layoutImage = findViewById(R.id.layoutImage);
        btnSave = findViewById(R.id.btnSave);

        // ================= DATE PICKER =================
        edtDate.setOnClickListener(v -> showDatePicker());

        // ================= CAMERA RESULT =================
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK &&
                            result.getData() != null &&
                            result.getData().getExtras() != null) {

                        Bitmap bitmap = (Bitmap) result.getData()
                                .getExtras()
                                .get("data");

                        imgMedicine.setImageBitmap(bitmap);
                    }
                });

        // ================= GALLERY RESULT =================
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK &&
                            result.getData() != null) {

                        Uri uri = result.getData().getData();
                        imgMedicine.setImageURI(uri);
                    }
                });

        // ================= CLICK IMAGE AREA =================
        layoutImage.setOnClickListener(v -> showImagePickerDialog());

        // ================= SAVE BUTTON =================
        btnSave.setOnClickListener(v -> {

            if (edtName.getText().toString().trim().isEmpty()) {
                edtName.setError("Required");
                return;
            }

            Toast.makeText(this,
                    "Medicine Saved Successfully",
                    Toast.LENGTH_SHORT).show();
        });
    }

    // ================= DATE PICKER =================
    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        edtDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ================= IMAGE PICKER =================
    private void showImagePickerDialog() {

        String[] options = {"Camera", "Gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Upload Image")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        checkCameraPermissionAndOpen();
                    } else {
                        openGallery();
                    }

                }).show();
    }

    // ================= CAMERA PERMISSION =================
    private void checkCameraPermissionAndOpen() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );

        } else {
            openCamera();
        }
    }

    // ================= OPEN CAMERA =================
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    // ================= OPEN GALLERY =================
    private void openGallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        galleryLauncher.launch(intent);
    }

    // ================= PERMISSION RESULT =================
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this,
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}