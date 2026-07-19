package com.nibm.pharmagomadproject.pharmacyowner;

import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    public static void addNotification(String title,
                                       String description,
                                       String type) {

        // Check if notifications are disabled by the pharmacy owner
        try {
            Context ctx = FirebaseApp.getInstance().getApplicationContext();
            if (ctx != null) {
                SharedPreferences prefs = ctx.getSharedPreferences("PharmaPrefs", Context.MODE_PRIVATE);
                boolean enabled = prefs.getBoolean("notifications_enabled", true);
                if (!enabled) {
                    Log.d(TAG, "Notifications are disabled in settings. Skipping addNotification.");
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to read notifications preferences", e);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String ownerId = "";

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (ownerId.isEmpty()) {
            Log.e(TAG, "No logged in user, cannot save notification");
            return;
        }

        final String finalOwnerId = ownerId;

        // Query to check for duplicate unread notifications
        db.collection("notifications")
                .whereEqualTo("ownerId", finalOwnerId)
                .whereEqualTo("title", title)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Duplicate alert exists, skip saving to reduce writes and clutter
                        Log.d(TAG, "Duplicate unread notification found for: " + title + ". Skipping save.");
                        return;
                    }

                    // Save new notification
                    String time = new SimpleDateFormat(
                            "dd MMM yyyy hh:mm a",
                            Locale.getDefault()
                    ).format(new Date());

                    NotificationModel notification = new NotificationModel(
                            title,
                            description,
                            time,
                            type,
                            false,
                            finalOwnerId,
                            System.currentTimeMillis()
                    );

                    db.collection("notifications")
                            .add(notification)
                            .addOnSuccessListener(documentReference ->
                                    Log.d(TAG, "Notification saved: " + documentReference.getId()))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Notification failed", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking duplicate notifications", e));
    }
}