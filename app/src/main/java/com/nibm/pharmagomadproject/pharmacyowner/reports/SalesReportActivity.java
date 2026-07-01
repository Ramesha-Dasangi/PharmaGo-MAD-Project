package com.nibm.pharmagomadproject.pharmacyowner.reports;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nibm.pharmagomadproject.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.nibm.pharmagomadproject.pharmacyowner.DashboardActivity;
import com.nibm.pharmagomadproject.pharmacyowner.InventoryActivity;
import com.nibm.pharmagomadproject.pharmacyowner.OrdersActivity;
import com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity;

public class SalesReportActivity extends AppCompatActivity {

    private RecyclerView recyclerChart;
    private ReportBarAdapter adapter;
    private ArrayList<SalesReportModel> reportList;

    private Button btnToday, btnWeek, btnMonth, btnExport;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_report);

        recyclerChart = findViewById(R.id.recyclerChart);

        btnToday = findViewById(R.id.btnToday);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        btnExport = findViewById(R.id.btnExport);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        recyclerChart.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false));

        reportList = new ArrayList<>();
        adapter = new ReportBarAdapter(reportList);
        recyclerChart.setAdapter(adapter);

        // Default Today Report
        loadTodayData();
        highlightButton(btnToday);

        btnToday.setOnClickListener(v -> {
            highlightButton(btnToday);
            loadTodayData();
        });

        btnWeek.setOnClickListener(v -> {
            highlightButton(btnWeek);
            loadWeekData();
        });

        btnMonth.setOnClickListener(v -> {
            highlightButton(btnMonth);
            loadMonthData();
        });

        btnExport.setOnClickListener(v -> exportPDF());

        bottomNavigation.setSelectedItemId(R.id.nav_reports);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                startActivity(new Intent(
                        SalesReportActivity.this,
                        DashboardActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_orders) {

                startActivity(new Intent(
                        SalesReportActivity.this,
                        OrdersActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_inventory) {

                startActivity(new Intent(
                        SalesReportActivity.this,
                        InventoryActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_reports) {

                return true;

            } else if (id == R.id.nav_profile) {

                startActivity(new Intent(
                        SalesReportActivity.this,
                        ProfileActivity.class));

                finish();
                return true;
            }

            return false;
        });
    }

    // ---------------- Today ----------------

    private void loadTodayData() {

        reportList.clear();

        reportList.add(new SalesReportModel("9AM",25));
        reportList.add(new SalesReportModel("10AM",35));
        reportList.add(new SalesReportModel("11AM",50));
        reportList.add(new SalesReportModel("12PM",60));
        reportList.add(new SalesReportModel("1PM",40));
        reportList.add(new SalesReportModel("2PM",70));

        adapter.notifyDataSetChanged();
    }

    // ---------------- Week ----------------

    private void loadWeekData() {

        reportList.clear();

        reportList.add(new SalesReportModel("Mon",35));
        reportList.add(new SalesReportModel("Tue",45));
        reportList.add(new SalesReportModel("Wed",25));
        reportList.add(new SalesReportModel("Thu",70));
        reportList.add(new SalesReportModel("Fri",40));
        reportList.add(new SalesReportModel("Sat",30));
        reportList.add(new SalesReportModel("Sun",65));

        adapter.notifyDataSetChanged();
    }

    // ---------------- Month ----------------

    private void loadMonthData() {

        reportList.clear();

        reportList.add(new SalesReportModel("W1",80));
        reportList.add(new SalesReportModel("W2",55));
        reportList.add(new SalesReportModel("W3",90));
        reportList.add(new SalesReportModel("W4",75));

        adapter.notifyDataSetChanged();
    }

    // ---------------- Highlight ----------------

    private void highlightButton(Button selectedButton) {

        Button[] buttons = {btnToday, btnWeek, btnMonth};

        for (Button button : buttons) {

            button.setBackgroundTintList(
                    getColorStateList(R.color.light));

            button.setTextColor(
                    getColor(R.color.green));
        }

        selectedButton.setBackgroundTintList(
                getColorStateList(R.color.green));

        selectedButton.setTextColor(Color.WHITE);
    }

    // ---------------- Export PDF ----------------

    private void exportPDF() {

        PdfDocument pdfDocument = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        595,
                        842,
                        1).create();

        PdfDocument.Page page =
                pdfDocument.startPage(pageInfo);

        Paint paint = new Paint();

        paint.setTextSize(24);
        paint.setFakeBoldText(true);

        page.getCanvas().drawText(
                "MediCare Pharmacy Sales Report",
                60,
                70,
                paint);

        paint.setTextSize(18);
        paint.setFakeBoldText(false);

        page.getCanvas().drawText("Date : 26/06/2026",60,120,paint);
        page.getCanvas().drawText("Total Revenue : Rs.24,500",60,170,paint);
        page.getCanvas().drawText("Orders Completed : 38",60,210,paint);
        page.getCanvas().drawText("Best Selling : Panadol 500mg",60,250,paint);
        page.getCanvas().drawText("Profit : Rs.9,850",60,290,paint);

        pdfDocument.finishPage(page);

        try {

            File file = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "SalesReport.pdf");

            FileOutputStream out =
                    new FileOutputStream(file);

            pdfDocument.writeTo(out);

            out.close();
            pdfDocument.close();

            Toast.makeText(
                    this,
                    "PDF Saved Successfully\n\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Failed to Export PDF",
                    Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }
    }
}