package com.nibm.pharmagomadproject.Admin;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nibm.pharmagomadproject.R;

import java.util.Calendar;

public class GenerateReportsActivity extends AppCompatActivity {

    private MaterialCardView cardReport1, cardReport2, cardReport3;
    private ImageView ivCheck1;
    private TextView tvFromDate, tvToDate;
    private int selectedReport = 1;

    private final String[] reportLabels = {
            "Sales & order summary",
            "Delivery performance",
            "Pharmacy activity"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_reports);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        cardReport1 = findViewById(R.id.cardReport1);
        cardReport2 = findViewById(R.id.cardReport2);
        cardReport3 = findViewById(R.id.cardReport3);
        ivCheck1 = findViewById(R.id.ivCheck1);
        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);

        cardReport1.setOnClickListener(v -> selectReport(1));
        cardReport2.setOnClickListener(v -> selectReport(2));
        cardReport3.setOnClickListener(v -> selectReport(3));

        // Date pickers
        MaterialCardView cardFromDate = findViewById(R.id.cardFromDate);
        MaterialCardView cardToDate = findViewById(R.id.cardToDate);

        cardFromDate.setOnClickListener(v -> showDatePicker(true));
        cardToDate.setOnClickListener(v -> showDatePicker(false));

        MaterialButton btnExportReport = findViewById(R.id.btnExportReport);
        btnExportReport.setOnClickListener(v ->
                Toast.makeText(this, "Exporting: " + reportLabels[selectedReport - 1], Toast.LENGTH_SHORT).show());
    }

    private void selectReport(int index) {
        selectedReport = index;

        int accent = ContextCompat.getColor(this, R.color.colorAccent);
        int stroke = ContextCompat.getColor(this, R.color.colorStroke);

        // Reset all cards
        resetReportCard(cardReport1);
        resetReportCard(cardReport2);
        resetReportCard(cardReport3);

        // Hide check on all (only card1 has it in layout)
        ivCheck1.setVisibility(View.GONE);

        // Highlight selected
        MaterialCardView selected;
        switch (index) {
            case 1: selected = cardReport1; ivCheck1.setVisibility(View.VISIBLE); break;
            case 2: selected = cardReport2; break;
            case 3: selected = cardReport3; break;
            default: selected = cardReport1;
        }
        selected.setStrokeColor(ColorStateList.valueOf(accent));
        selected.setStrokeWidth(3);
    }

    private void resetReportCard(MaterialCardView card) {
        int stroke = ContextCompat.getColor(this, R.color.colorStroke);
        card.setStrokeColor(ColorStateList.valueOf(stroke));
        card.setStrokeWidth(2);
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format("%02d %s %d", day, getMonthName(month), year);
            if (isFrom) tvFromDate.setText(date);
            else tvToDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String getMonthName(int month) {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        return months[month];
    }
}
