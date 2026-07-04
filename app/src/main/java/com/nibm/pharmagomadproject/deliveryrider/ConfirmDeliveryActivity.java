package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;


public class ConfirmDeliveryActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView ivPhotoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_delivery);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        
        View boxPhoto = findViewById(R.id.boxPhoto);
        ivPhotoResult = findViewById(R.id.ivPhotoResult); // Need to add this to XML
        if (boxPhoto != null) {
            boxPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            });
        }

        Button btnConfirmDelivered = findViewById(R.id.btnConfirmDelivered);
        if (btnConfirmDelivered != null) {
            btnConfirmDelivered.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ConfirmDeliveryActivity.this, RiderDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                if (ivPhotoResult != null) {
                    ivPhotoResult.setImageBitmap(imageBitmap);
                    ivPhotoResult.setVisibility(View.VISIBLE);
                    findViewById(R.id.iconCamera).setVisibility(View.GONE);
                    findViewById(R.id.tvTakePhoto).setVisibility(View.GONE);
                }
            }
        }
    }
}
