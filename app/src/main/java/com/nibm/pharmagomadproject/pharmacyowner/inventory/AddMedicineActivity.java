package com.nibm.pharmagomadproject.pharmacyowner.inventory;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;

import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {

    EditText edtName, edtCategory, edtPrice, edtStock, edtDate;
    RadioGroup radioType;
    ImageView imgBack;
    Button btnSave;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        edtName = findViewById(R.id.edtName);
        edtCategory = findViewById(R.id.edtCategory);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        edtDate = findViewById(R.id.edtDate);
        radioType = findViewById(R.id.radioType);

        imgBack = findViewById(R.id.imgBack);
        btnSave = findViewById(R.id.btnSave);

        // BACK
        imgBack.setOnClickListener(v -> finish());

        // DATE PICKER
        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, day) ->
                            edtDate.setText(year + "-" + (month + 1) + "-" + day),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // SAVE
        btnSave.setOnClickListener(v ->
                Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()
        );
    }
}