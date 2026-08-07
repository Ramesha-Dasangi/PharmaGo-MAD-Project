package com.nibm.pharmagomadproject.pharmacyowner.reports;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.pharmacyowner.DashboardActivity;
import com.nibm.pharmagomadproject.pharmacyowner.InventoryActivity;
import com.nibm.pharmagomadproject.pharmacyowner.NetworkUtils;
import com.nibm.pharmagomadproject.pharmacyowner.OrdersActivity;
import com.nibm.pharmagomadproject.pharmacyowner.profile.ProfileActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalesReportActivity extends AppCompatActivity {

    // ──────────── Views ────────────
    private RecyclerView recyclerChart;
    private ReportBarAdapter adapter;
    private ArrayList<SalesReportModel> reportList;

    private Button btnToday, btnWeek, btnMonth, btnExport;
    private TextView txtTotalRevenue, txtOrderCount, txtBestSeller, txtChartTitle;
    private BottomNavigationView bottomNavigation;

    // ──────────── Firebase ────────────
    private FirebaseFirestore db;
    private String ownerId = "";

    // ──────────── Cached report data for PDF export ────────────
    private double cachedRevenue    = 0;
    private int    cachedOrderCount = 0;
    private String cachedBestSeller = "—";
    private String cachedPeriodLabel = "Today";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_report);

        // ── Firebase auth ──
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) ownerId = user.getUid();

        // ── View binding ──
        recyclerChart = findViewById(R.id.recyclerChart);
        btnToday      = findViewById(R.id.btnToday);
        btnWeek       = findViewById(R.id.btnWeek);
        btnMonth      = findViewById(R.id.btnMonth);
        btnExport     = findViewById(R.id.btnExport);
        txtTotalRevenue = findViewById(R.id.txtTotalRevenue);
        txtOrderCount   = findViewById(R.id.txtOrderCount);
        txtBestSeller   = findViewById(R.id.txtBestSeller);
        txtChartTitle   = findViewById(R.id.txtChartTitle);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        recyclerChart.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        reportList = new ArrayList<>();
        adapter    = new ReportBarAdapter(reportList);
        recyclerChart.setAdapter(adapter);

        // ── Default: today ──
        highlightButton(btnToday);
        loadReportData("today");

        btnToday.setOnClickListener(v -> { highlightButton(btnToday); loadReportData("today"); });
        btnWeek .setOnClickListener(v -> { highlightButton(btnWeek);  loadReportData("week");  });
        btnMonth.setOnClickListener(v -> { highlightButton(btnMonth); loadReportData("month"); });
        btnExport.setOnClickListener(v -> exportPDF());

        bottomNavigation.setSelectedItemId(R.id.nav_reports);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish(); return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrdersActivity.class));
                finish(); return true;
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryActivity.class));
                finish(); return true;
            } else if (id == R.id.nav_reports) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish(); return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load report data for current active period (default to today)
        loadReportData("today");
    }

    // ═══════════════════════════════════════════════════
    //  Load report data from Firestore based on period
    // ═══════════════════════════════════════════════════
    private void loadReportData(String period) {
        if (ownerId.isEmpty()) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        cachedPeriodLabel = period.substring(0, 1).toUpperCase() + period.substring(1);

        // Update chart title to reflect selected period
        String chartTitle;
        switch (period) {
            case "week":  chartTitle = "Revenue This Week"; break;
            case "month": chartTitle = "Revenue This Month"; break;
            default:      chartTitle = "Revenue Today"; break;
        }
        if (txtChartTitle != null) txtChartTitle.setText(chartTitle);

        long startMs = getStartMillis(period);

        db.collection("orders")
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(snapshots -> {
                    processReportData(snapshots, period, startMs);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ═══════════════════════════════════════════════════
    //  Process query results into bar chart + summary
    // ═══════════════════════════════════════════════════
    private void processReportData(
            com.google.firebase.firestore.QuerySnapshot snapshots,
            String period,
            long startMs) {

        double totalRevenue = 0;
        int orderCount = 0;
        Map<String, Integer> medicineCountMap = new HashMap<>();

        // ── Bucket map for bar chart (label → total) ──
        Map<String, Double> buckets = new java.util.LinkedHashMap<>();
        initBuckets(buckets, period);

        for (QueryDocumentSnapshot doc : snapshots) {
            // Check if this order belongs to this pharmacy owner
            boolean belongsToMe = false;
            String topPharmacyId = doc.getString("pharmacyId");
            if (ownerId.equals(topPharmacyId)) {
                belongsToMe = true;
            } else {
                List<Map<String, Object>> items =
                        (List<Map<String, Object>>) doc.get("items");
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        if (ownerId.equals(item.get("pharmacyId"))) {
                            belongsToMe = true;
                            break;
                        }
                    }
                }
            }

            if (!belongsToMe) {
                continue;
            }

            Long completedAt = doc.getLong("completedAt");
            // In-memory filter for period range to avoid Firestore composite index requirement
            if (completedAt == null || completedAt < startMs) {
                continue;
            }

            orderCount++;
            double total = 0;
            Object totalObj = doc.get("total");
            if (totalObj instanceof Number) {
                total = ((Number) totalObj).doubleValue();
            }
            totalRevenue += total;

            String label = getBucketLabel(completedAt, period);
            buckets.merge(label, total, Double::sum);

            // Top-selling medicine
            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) doc.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    String medName = item.get("medicineName") != null
                            ? item.get("medicineName").toString() : "Unknown";
                    Object qtyObj = item.get("quantity");
                    int qty = qtyObj != null ? ((Number) qtyObj).intValue() : 1;
                    medicineCountMap.merge(medName, qty, Integer::sum);
                }
            }
        }

        // ── Best seller ranked list ──
        String bestSellerText;
        if (medicineCountMap.isEmpty()) {
            bestSellerText = "— No sales data for this period";
        } else {
            // Sort medicines by total units sold descending
            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(medicineCountMap.entrySet());
            sortedEntries.sort((a, b) -> b.getValue() - a.getValue());

            StringBuilder sb = new StringBuilder();
            int rank = 1;
            for (Map.Entry<String, Integer> entry : sortedEntries) {
                if (rank > 5) break; // show top 5
                String medal;
                switch (rank) {
                    case 1: medal = "\uD83E\uDD47 "; break; // 🥇
                    case 2: medal = "\uD83E\uDD48 "; break; // 🥈
                    case 3: medal = "\uD83E\uDD49 "; break; // 🥉
                    default: medal = rank + ". "; break;
                }
                sb.append(medal)
                        .append(entry.getKey())
                        .append("  —  ")
                        .append(entry.getValue())
                        .append(" units");
                if (rank < sortedEntries.size() && rank < 5) sb.append("\n");
                rank++;
            }
            bestSellerText = sb.toString();
        }

        // ── Cache for PDF ──
        cachedRevenue    = totalRevenue;
        cachedOrderCount = orderCount;
        cachedBestSeller = medicineCountMap.isEmpty() ? "—" :
                Collections.max(medicineCountMap.entrySet(), Map.Entry.comparingByValue()).getKey();

        final String finalBestSellerText = bestSellerText;

        // ── Summary TextViews ──
        double finalRevenue = totalRevenue;
        final int finalOrderCount = orderCount;
        runOnUiThread(() -> {
            if (txtTotalRevenue != null)
                txtTotalRevenue.setText("Rs. " + String.format("%.2f", finalRevenue));
            if (txtOrderCount != null)
                txtOrderCount.setText(String.valueOf(finalOrderCount));
            if (txtBestSeller != null)
                txtBestSeller.setText(finalBestSellerText);
        });

        // ── Build bar chart ──
        reportList.clear();
        double maxVal = Collections.max(buckets.values().isEmpty()
                ? Collections.singletonList(1.0) : buckets.values());
        for (Map.Entry<String, Double> entry : buckets.entrySet()) {
            int barHeight = maxVal > 0
                    ? (int) ((entry.getValue() / maxVal) * 100)
                    : 0;
            reportList.add(new SalesReportModel(entry.getKey(), barHeight));
        }
        adapter.notifyDataSetChanged();
    }

    // ═══════════════════════════════════════════════════
    //  Build ordered bucket map for chart axis labels
    // ═══════════════════════════════════════════════════
    private void initBuckets(Map<String, Double> buckets, String period) {
        switch (period) {
            case "today":
                for (int h = 7; h <= 22; h += 3) {
                    buckets.put(formatHour(h), 0.0);
                }
                break;
            case "week":
                String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                for (String d : days) buckets.put(d, 0.0);
                break;
            case "month":
                buckets.put("W1", 0.0);
                buckets.put("W2", 0.0);
                buckets.put("W3", 0.0);
                buckets.put("W4", 0.0);
                break;
        }
    }

    private String formatHour(int hour) {
        if (hour < 12) return hour + "AM";
        if (hour == 12) return "12PM";
        return (hour - 12) + "PM";
    }

    // ═══════════════════════════════════════════════════
    //  Map a timestamp to the right bucket label
    // ═══════════════════════════════════════════════════
    private String getBucketLabel(long ms, String period) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ms);
        switch (period) {
            case "today":
                int h = cal.get(Calendar.HOUR_OF_DAY);
                // Round down to nearest 3-hour bucket starting at 7
                int bucket = ((h - 7) / 3) * 3 + 7;
                if (bucket < 7) bucket = 7;
                if (bucket > 22) bucket = 22;
                return formatHour(bucket);
            case "week":
                String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                return days[cal.get(Calendar.DAY_OF_WEEK) - 1];
            case "month":
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                if (dayOfMonth <= 7)  return "W1";
                if (dayOfMonth <= 14) return "W2";
                if (dayOfMonth <= 21) return "W3";
                return "W4";
        }
        return "—";
    }

    // ═══════════════════════════════════════════════════
    //  Get start-of-period timestamp in milliseconds
    // ═══════════════════════════════════════════════════
    private long getStartMillis(String period) {
        Calendar cal = Calendar.getInstance();
        switch (period) {
            case "today":
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case "week":
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case "month":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
        }
        return cal.getTimeInMillis();
    }

    // ═══════════════════════════════════════════════════
    //  Button highlight toggle
    // ═══════════════════════════════════════════════════
    private void highlightButton(Button selected) {
        Button[] all = {btnToday, btnWeek, btnMonth};
        for (Button b : all) {
            b.setBackgroundTintList(getColorStateList(R.color.light));
            b.setTextColor(getColor(R.color.green));
        }
        selected.setBackgroundTintList(getColorStateList(R.color.green));
        selected.setTextColor(Color.WHITE);
    }

    // ═══════════════════════════════════════════════════
    //  PDF Export with real computed data
    // ═══════════════════════════════════════════════════
    private void exportPDF() {
        PdfDocument pdfDoc = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDoc.startPage(pageInfo);

        Paint paint = new Paint();
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // Title
        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        paint.setColor(Color.parseColor("#00A86B"));
        page.getCanvas().drawText("PharmaGo — Sales Report", 50, 65, paint);

        // Divider line
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(1.5f);
        page.getCanvas().drawLine(50, 80, 545, 80, paint);

        // Data
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(false);
        paint.setTextSize(16);

        page.getCanvas().drawText("Period         : " + cachedPeriodLabel, 50, 115, paint);
        page.getCanvas().drawText("Date           : " + today,            50, 145, paint);
        page.getCanvas().drawText(
                "Total Revenue  : Rs. " + String.format("%.2f", cachedRevenue), 50, 175, paint);
        page.getCanvas().drawText(
                "Orders Completed: " + cachedOrderCount, 50, 205, paint);
        page.getCanvas().drawText(
                "Best Selling   : " + cachedBestSeller, 50, 235, paint);

        // Bar chart as text table
        paint.setFakeBoldText(true);
        paint.setTextSize(14);
        paint.setColor(Color.parseColor("#444444"));
        page.getCanvas().drawText("Sales Breakdown:", 50, 280, paint);

        paint.setFakeBoldText(false);
        int yPos = 305;
        for (SalesReportModel model : reportList) {
            String line = String.format("  %-8s  %d%%", model.getDay(), model.getValue());
            page.getCanvas().drawText(line, 50, yPos, paint);
            yPos += 24;
            if (yPos > 800) break;
        }

        // Footer
        paint.setTextSize(11);
        paint.setColor(Color.GRAY);
        page.getCanvas().drawText(
                "Generated by PharmaGo | " + today, 50, 820, paint);

        pdfDoc.finishPage(page);

        try {
            File file = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "SalesReport_" + cachedPeriodLabel + "_" + today.replace("/", "-") + ".pdf");
            FileOutputStream out = new FileOutputStream(file);
            pdfDoc.writeTo(out);
            out.close();
            pdfDoc.close();
            Toast.makeText(this,
                    "PDF Saved:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            pdfDoc.close();
            Toast.makeText(this, "Failed to export PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}