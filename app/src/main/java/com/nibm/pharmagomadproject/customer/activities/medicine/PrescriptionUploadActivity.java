package com.nibm.pharmagomadproject.customer.activities.medicine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.order.OrderTrackingActivity;
import com.nibm.pharmagomadproject.customer.db.SupabaseStorageHelper;

public class PrescriptionUploadActivity extends AppCompatActivity {

    private LinearLayout uploadArea;
    private ImageView    ivPreview;
    private TextView     tvFileName, tvProgress;
    private ProgressBar  progressBar;
    private MaterialButton btnCamera, btnGallery, btnSubmit;

    private Uri    selectedImageUri = null;
    private String uploadedUrl      = null;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri>    cameraLauncher;
    private Uri    cameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prescription_upload);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        uploadArea  = findViewById(R.id.uploadArea);
        ivPreview   = findViewById(R.id.ivPreview);
        tvFileName  = findViewById(R.id.tvFileName);
        tvProgress  = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        btnCamera   = findViewById(R.id.btnCamera);
        btnGallery  = findViewById(R.id.btnGallery);
        btnSubmit   = findViewById(R.id.btnSubmitPrescription);

        if (tvProgress  != null) tvProgress.setVisibility(View.GONE);
        if (progressBar != null) progressBar.setVisibility(View.GONE);

        TextView tvWarningText = findViewById(R.id.tvWarningText);
        if (tvWarningText != null) {
            String medName = getIntent().getStringExtra("medicine_name");
            if (medName != null && !medName.trim().isEmpty()) {
                tvWarningText.setText(" Prescription required for: " + medName);
            } else if (getIntent().getBooleanExtra("is_cart_rx_order", false)) {
                tvWarningText.setText(" Prescription required for item(s) in your cart.");
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        showPreview(selectedImageUri, "gallery_image.jpg");
                    }
                });

        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraUri != null) {
                        selectedImageUri = cameraUri;
                        showPreview(cameraUri, "camera_image.jpg");
                    }
                });

        uploadArea.setOnClickListener(v -> openGallery());
        btnGallery.setOnClickListener(v -> openGallery());
        btnCamera.setOnClickListener(v  -> openCamera());

        btnSubmit.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select a prescription image",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            uploadToSupabase();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        cameraUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new android.content.ContentValues());
        cameraLauncher.launch(cameraUri);
    }

    private void showPreview(Uri uri, String fileName) {
        if (ivPreview != null) {
            ivPreview.setVisibility(View.VISIBLE);
            ivPreview.setImageURI(uri);
        }
        if (tvFileName != null) tvFileName.setText(fileName);
    }

    // Upload to Supabase storage
    private void uploadToSupabase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "unknown";

        btnSubmit.setEnabled(false);
        if (progressBar != null) {
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
        }
        if (tvProgress  != null) {
            tvProgress.setText("Uploading...");
            tvProgress.setVisibility(View.VISIBLE);
        }

        new SupabaseStorageHelper(this).uploadPrescription(
                userId,
                selectedImageUri,
                new SupabaseStorageHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String publicUrl) {
                        runOnUiThread(() -> {
                            uploadedUrl = publicUrl;
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            if (tvProgress  != null) tvProgress.setText("Upload complete ✓");
                            Toast.makeText(PrescriptionUploadActivity.this,
                                    "Prescription uploaded!", Toast.LENGTH_SHORT).show();

                            String orderId = "PG-" + System.currentTimeMillis();

                            boolean isCartRxOrder = getIntent().getBooleanExtra("is_cart_rx_order", false);

                            java.util.List<java.util.Map<String, Object>> itemsList = new java.util.ArrayList<>();
                            int subtotal = 0;
                            String phId = getIntent().getStringExtra("pharmacy_id");
                            String pharmacyName = getIntent().getStringExtra("pharmacy_name");

                            if (isCartRxOrder && !com.nibm.pharmagomadproject.customer.activities.order.CartActivity.CART_STORE.isEmpty()) {
                                for (com.nibm.pharmagomadproject.customer.models.Cart c : com.nibm.pharmagomadproject.customer.activities.order.CartActivity.CART_STORE) {
                                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                                    item.put("medicineId",   c.getMedicineId());
                                    item.put("medicineName", c.getMedicineName());
                                    item.put("brandName",    c.getBrandName());
                                    item.put("pharmacyId",   c.getPharmacyId());
                                    item.put("pharmacyName", c.getPharmacyName());
                                    item.put("price",        c.getPrice());
                                    item.put("quantity",     c.getQuantity());
                                    item.put("type",         c.getMedicineType() != null ? c.getMedicineType() : "Prescription");
                                    itemsList.add(item);
                                    subtotal += (int) c.getSubtotal();
                                    if (phId == null || phId.isEmpty()) phId = c.getPharmacyId();
                                    if (pharmacyName == null || pharmacyName.isEmpty()) pharmacyName = c.getPharmacyName();
                                }
                            } else {
                                String medicineId   = getIntent().getStringExtra("medicine_id");
                                String medicineName = getIntent().getStringExtra("medicine_name");
                                String brandName    = getIntent().getStringExtra("brand_name");
                                int    price        = getIntent().getIntExtra("medicine_price", 0);

                                java.util.Map<String, Object> item = new java.util.HashMap<>();
                                item.put("medicineId",   medicineId);
                                item.put("medicineName", medicineName);
                                item.put("brandName",    brandName);
                                item.put("pharmacyId",   phId);
                                item.put("pharmacyName", pharmacyName);
                                item.put("price",        (double) price);
                                item.put("quantity",     1);
                                item.put("type",         "Prescription");
                                itemsList.add(item);
                                subtotal = price;
                            }

                            int deliveryFee = 100;
                            int total = subtotal + deliveryFee;

                            java.util.Map<String, Object> order = new java.util.HashMap<>();
                            order.put("orderId",       orderId);
                            order.put("customerId",    userId);
                            order.put("pharmacyId",    phId   != null ? phId         : "");
                            order.put("pharmacyName",  pharmacyName != null ? pharmacyName : "Pharmacy");
                            order.put("items",         itemsList);
                            order.put("subtotal",      subtotal);
                            order.put("deliveryFee",   deliveryFee);
                            order.put("total",         total);
                            order.put("paymentMethod", "pending");          // payment method chosen after approval
                            order.put("status",        "awaiting_approval"); // pharmacy must verify prescription
                            order.put("orderType",     "Prescription");
                            order.put("createdAt",     System.currentTimeMillis());
                            order.put("prescriptionUrl", publicUrl);

                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("users").document(userId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String addr  = userDoc.getString("address");
                                            String label = userDoc.getString("addressLabel");
                                            if (addr != null && !addr.isEmpty()) {
                                                order.put("deliveryAddress",
                                                        (label != null && !label.isEmpty())
                                                                ? label + " · " + addr : addr);
                                            }
                                        }
                                        savePrescriptionOrder(orderId, order);
                                    })
                                    .addOnFailureListener(e -> savePrescriptionOrder(orderId, order));
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            btnSubmit.setEnabled(true);
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            if (tvProgress  != null) tvProgress.setVisibility(View.GONE);
                            Toast.makeText(PrescriptionUploadActivity.this,
                                    "Upload failed: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private void savePrescriptionOrder(String orderId, java.util.Map<String, Object> order) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("orders")
                .document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    com.nibm.pharmagomadproject.customer.activities.order.CartActivity.clearCart();
                    Intent intent = new Intent(PrescriptionUploadActivity.this, OrderTrackingActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("is_prescription_order", true);  // hint to show awaiting banner
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed to create order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}