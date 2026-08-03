package com.nibm.pharmagomadproject.Admin;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.pharmagomadproject.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_reports);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        db = FirebaseFirestore.getInstance();

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        cardReport1 = findViewById(R.id.cardReport1);
        cardReport2 = findViewById(R.id.cardReport2);
        cardReport3 = findViewById(R.id.cardReport3);
        ivCheck1 = findViewById(R.id.ivCheck1);
        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);

        // Set default dates
        Calendar cal = Calendar.getInstance();
        tvToDate.setText(dateFormat.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_MONTH, -7);
        tvFromDate.setText(dateFormat.format(cal.getTime()));

        cardReport1.setOnClickListener(v -> selectReport(1));
        cardReport2.setOnClickListener(v -> selectReport(2));
        cardReport3.setOnClickListener(v -> selectReport(3));

        // Date pickers
        MaterialCardView cardFromDate = findViewById(R.id.cardFromDate);
        MaterialCardView cardToDate = findViewById(R.id.cardToDate);

        cardFromDate.setOnClickListener(v -> showDatePicker(true));
        cardToDate.setOnClickListener(v -> showDatePicker(false));

        MaterialButton btnExportReport = findViewById(R.id.btnExportReport);
        btnExportReport.setOnClickListener(v -> exportReport());
    }

    private void selectReport(int index) {
        selectedReport = index;

        int accent = ContextCompat.getColor(this, R.color.pg_primary);

        // Reset all cards
        resetReportCard(cardReport1);
        resetReportCard(cardReport2);
        resetReportCard(cardReport3);

        ivCheck1.setVisibility(View.GONE);

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
        int stroke = ContextCompat.getColor(this, R.color.pg_border);
        card.setStrokeColor(ColorStateList.valueOf(stroke));
        card.setStrokeWidth(2);
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = Calendar.getInstance();
        try {
            Date currentDate = dateFormat.parse(isFrom ? tvFromDate.getText().toString() : tvToDate.getText().toString());
            if (currentDate != null) cal.setTime(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            String date = dateFormat.format(cal.getTime());
            if (isFrom) tvFromDate.setText(date);
            else tvToDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void exportReport() {
        try {
            Date fromDate = dateFormat.parse(tvFromDate.getText().toString());
            Date toDate = dateFormat.parse(tvToDate.getText().toString());

            if (fromDate == null || toDate == null || fromDate.after(toDate)) {
                Toast.makeText(this, "Invalid date range", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Adjust toDate to end of day
            Calendar cal = Calendar.getInstance();
            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            long endMs = cal.getTimeInMillis();
            long startMs = fromDate.getTime();

            Toast.makeText(this, "Generating Report...", Toast.LENGTH_SHORT).show();
            
            if (selectedReport == 1 || selectedReport == 2) {
                db.collection("orders").get().addOnSuccessListener(snapshots -> {
                    processOrders(snapshots, startMs, endMs);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else if (selectedReport == 3) {
                db.collection("orders").get().addOnSuccessListener(snapshots -> {
                    processPharmacyActivity(snapshots, startMs, endMs);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    private void processOrders(com.google.firebase.firestore.QuerySnapshot snapshots, long startMs, long endMs) {
        int orderCount = 0;
        double totalRevenue = 0;
        int deliveredCount = 0;
        int cancelledCount = 0;
        
        List<String[]> tableData = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        
        for (QueryDocumentSnapshot doc : snapshots) {
            Long timestamp = extractOrderTimestamp(doc);
            if (timestamp == null || timestamp < startMs || timestamp > endMs) {
                continue;
            }
            
            orderCount++;
            String status = doc.getString("status");
            if (status == null) status = "unknown";
            
            double orderTotal = 0;
            if ("completed".equals(status) || "delivered".equals(status)) {
                deliveredCount++;
                Object totalObj = doc.get("total");
                if (totalObj instanceof Number) {
                    orderTotal = ((Number) totalObj).doubleValue();
                } else {
                    Object amountObj = doc.get("amount");
                    if (amountObj instanceof String) {
                        try { orderTotal = Double.parseDouble(((String) amountObj).replace("LKR ", "")); } catch (Exception ignored) {}
                    } else if (amountObj instanceof Number) {
                        orderTotal = ((Number) amountObj).doubleValue();
                    }
                }
                totalRevenue += orderTotal;
            } else if ("cancelled".equals(status)) {
                cancelledCount++;
            }
            
            String displayDate = sdf.format(new Date(timestamp));
            String oId = doc.getString("orderId") != null ? doc.getString("orderId") : doc.getId();
            
            if (selectedReport == 1) {
                tableData.add(new String[]{ oId, displayDate, status.toUpperCase(), String.format(Locale.US, "%.2f", orderTotal) });
            } else {
                if ("completed".equals(status) || "delivered".equals(status)) {
                    String rider = doc.getString("riderId") != null ? doc.getString("riderId") : "N/A";
                    tableData.add(new String[]{ oId, rider, displayDate, "Delivered" });
                }
            }
        }
        
        if (selectedReport == 1) {
            String summary = "Total Orders: " + orderCount + 
                    " | Total Revenue: LKR " + String.format(Locale.US, "%.2f", totalRevenue) +
                    " | Delivered: " + deliveredCount +
                    " | Cancelled: " + cancelledCount;
            String[] headers = {"Order ID", "Date", "Status", "Amount (LKR)"};
            generatePdfTable("Sales & Order Summary", summary, headers, tableData);
        } else {
            String summary = "Total Orders Placed: " + orderCount +
                    " | Successfully Delivered: " + deliveredCount +
                    " | Success Rate: " + (orderCount > 0 ? (deliveredCount * 100 / orderCount) + "%" : "N/A");
            String[] headers = {"Order ID", "Rider ID", "Completed Date", "Status"};
            generatePdfTable("Delivery Performance", summary, headers, tableData);
        }
    }
    
    private void processPharmacyActivity(com.google.firebase.firestore.QuerySnapshot snapshots, long startMs, long endMs) {
        Map<String, Integer> pharmacyOrderCount = new HashMap<>();
        Map<String, Double> pharmacyRevenue = new HashMap<>();
        
        for (QueryDocumentSnapshot doc : snapshots) {
            Long timestamp = extractOrderTimestamp(doc);
            if (timestamp == null || timestamp < startMs || timestamp > endMs) {
                continue;
            }
            
            String pharmacyId = doc.getString("pharmacyId");
            if (pharmacyId == null) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                if (items != null && !items.isEmpty()) {
                    pharmacyId = (String) items.get(0).get("pharmacyId");
                }
            }
            if (pharmacyId != null) {
                pharmacyOrderCount.put(pharmacyId, pharmacyOrderCount.getOrDefault(pharmacyId, 0) + 1);
                
                double orderTotal = 0;
                Object totalObj = doc.get("total");
                if (totalObj instanceof Number) {
                    orderTotal = ((Number) totalObj).doubleValue();
                }
                pharmacyRevenue.put(pharmacyId, pharmacyRevenue.getOrDefault(pharmacyId, 0.0) + orderTotal);
            }
        }
        
        List<String[]> tableData = new ArrayList<>();
        int totalOrdersAll = 0;
        for (Map.Entry<String, Integer> entry : pharmacyOrderCount.entrySet()) {
            String pId = entry.getKey();
            int count = entry.getValue();
            double rev = pharmacyRevenue.getOrDefault(pId, 0.0);
            totalOrdersAll += count;
            
            tableData.add(new String[]{ pId, String.valueOf(count), String.format(Locale.US, "%.2f", rev) });
        }
        
        String summary = "Total Active Pharmacies: " + pharmacyOrderCount.size() + " | Total Orders Handled: " + totalOrdersAll;
        String[] headers = {"Pharmacy ID", "Orders Processed", "Estimated Revenue (LKR)"};
        
        generatePdfTable("Pharmacy Activity Report", summary, headers, tableData);
    }

    private Long extractOrderTimestamp(QueryDocumentSnapshot doc) {
        Long completedAt = doc.getLong("completedAt");
        if (completedAt != null) return completedAt;
        
        String orderId = doc.getId();
        if (doc.getString("orderId") != null) {
            orderId = doc.getString("orderId");
        }
        if (orderId != null && orderId.startsWith("PG-")) {
            try {
                return Long.parseLong(orderId.substring(3));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private void generatePdfTable(String title, String summary, String[] headers, List<String[]> tableData) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Title
        paint.setColor(Color.BLACK);
        paint.setTextSize(22f);
        paint.setFakeBoldText(true);
        canvas.drawText("PharmaGo Admin Report", 50, 50, paint);
        
        paint.setTextSize(18f);
        paint.setFakeBoldText(false);
        canvas.drawText(title, 50, 75, paint);
        
        // Date Range & Summary
        paint.setTextSize(12f);
        paint.setColor(Color.DKGRAY);
        canvas.drawText("Date Range: " + tvFromDate.getText().toString() + " - " + tvToDate.getText().toString(), 50, 100, paint);
        canvas.drawText(summary, 50, 120, paint);
        
        int startY = 150;
        int marginX = 50;
        int rowHeight = 30;
        int numCols = headers.length;
        int colWidth = (595 - (2 * marginX)) / numCols;
        
        // Draw Header Background
        paint.setColor(Color.parseColor("#E0E0E0"));
        canvas.drawRect(marginX, startY, 595 - marginX, startY + rowHeight, paint);
        
        // Draw Header Text
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        paint.setTextSize(12f);
        for (int i = 0; i < numCols; i++) {
            canvas.drawText(headers[i], marginX + (i * colWidth) + 10, startY + 20, paint);
        }
        
        paint.setFakeBoldText(false);
        int currentY = startY + rowHeight;
        
        // Draw Rows
        for (int i = 0; i < tableData.size(); i++) {
            // New page if we reach the bottom
            if (currentY > 800) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                currentY = 50;
            }
            
            // Zebra striping
            if (i % 2 == 1) {
                paint.setColor(Color.parseColor("#F9F9F9"));
                canvas.drawRect(marginX, currentY, 595 - marginX, currentY + rowHeight, paint);
            }
            
            paint.setColor(Color.DKGRAY);
            String[] row = tableData.get(i);
            for (int j = 0; j < numCols; j++) {
                String text = j < row.length && row[j] != null ? row[j] : "";
                
                // Truncate long text
                if (text.length() > 25) {
                    text = text.substring(0, 22) + "...";
                }
                
                canvas.drawText(text, marginX + (j * colWidth) + 10, currentY + 20, paint);
            }
            
            // Draw row separator
            paint.setColor(Color.LTGRAY);
            canvas.drawLine(marginX, currentY + rowHeight, 595 - marginX, currentY + rowHeight, paint);
            
            currentY += rowHeight;
        }

        // Draw bounding box
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(marginX, startY, 595 - marginX, currentY, paint);

        pdfDocument.finishPage(page);

        String fileName = "PharmaGo_Report_" + System.currentTimeMillis() + ".pdf";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Downloaded", Toast.LENGTH_SHORT).show();
            showNotification(file);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
    }
    
    private void showNotification(File file) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "report_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Reports", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Report Downloaded")
                .setContentText("Tap to open " + file.getName())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
