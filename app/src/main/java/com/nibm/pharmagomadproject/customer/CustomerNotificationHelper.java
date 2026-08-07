package com.nibm.pharmagomadproject.customer;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class CustomerNotificationHelper {
    private static final String TAG = "CustomerNotifHelper";

    public static void sendNotification(String customerId, Map<String, Object> notificationData) {
        if (customerId == null || customerId.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Check if the user has enabled notifications
        db.collection("users").document(customerId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean notificationsEnabled = doc.getBoolean("notificationsEnabled");
                        // If field doesn't exist or is true, we send the notification
                        if (notificationsEnabled == null || notificationsEnabled) {
                            db.collection("notifications").add(notificationData)
                                    .addOnSuccessListener(docRef -> Log.d(TAG, "Notification sent for customer " + customerId))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to send notification", e));
                        } else {
                            Log.d(TAG, "Notifications disabled for user " + customerId + ", skipping notification.");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to check user notification settings", e));
    }
}
