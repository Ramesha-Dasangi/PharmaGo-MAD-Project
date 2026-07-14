package com.nibm.pharmagomadproject.customer.activities.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nibm.pharmagomadproject.R;

public class DeliveryAddressActivity extends AppCompatActivity {

    private TextInputEditText etAddress, etCity;
    private String selectedLabel = "Home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_address);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etAddress = findViewById(R.id.etAddress);
        etCity    = findViewById(R.id.etCity);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Label chips: Home / Work / Other
        android.widget.TextView labelHome  = findViewById(R.id.labelHome);
        android.widget.TextView labelWork  = findViewById(R.id.labelWork);
        android.widget.TextView labelOther = findViewById(R.id.labelOther);

        labelHome.setOnClickListener(v -> selectLabel("Home",  labelHome, labelWork, labelOther));
        labelWork.setOnClickListener(v -> selectLabel("Work",  labelHome, labelWork, labelOther));
        labelOther.setOnClickListener(v -> selectLabel("Other", labelHome, labelWork, labelOther));

        // Save address button
        MaterialButton btnSave = findViewById(R.id.btnSaveAddress);
        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void selectLabel(String label,
                             android.widget.TextView home,
                             android.widget.TextView work,
                             android.widget.TextView other) {
        selectedLabel = label;
        home.setBackgroundResource(label.equals("Home")
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        work.setBackgroundResource(label.equals("Work")
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);
        other.setBackgroundResource(label.equals("Other")
                ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected);

        int primary = getResources().getColor(R.color.pg_primary, null);
        int sub     = getResources().getColor(R.color.pg_sub, null);
        home.setTextColor(label.equals("Home")  ? primary : sub);
        work.setTextColor(label.equals("Work")  ? primary : sub);
        other.setTextColor(label.equals("Other")? primary : sub);
    }

    private void saveAddress() {
        String address = etAddress.getText() != null
                ? etAddress.getText().toString().trim() : "";
        String city    = etCity.getText() != null
                ? etCity.getText().toString().trim() : "";

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Please enter your address");
            etAddress.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(city)) {
            etCity.setError("Please enter your city");
            etCity.requestFocus();
            return;
        }

        // TODO: save to Firebase / SharedPreferences
        Toast.makeText(this, "Address saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
