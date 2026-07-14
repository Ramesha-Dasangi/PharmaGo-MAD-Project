package com.nibm.pharmagomadproject.customer.db;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";

    // Collection names
    public static final String COL_MEDICINES   = "medicines";
    public static final String COL_USERS       = "users";
    public static final String COL_PHARMACIES  = "pharmacies";
    public static final String COL_ORDERS      = "orders";
    public static final String COL_REVIEWS     = "reviews";

    private final FirebaseFirestore db;

    // Singleton
    private static DatabaseHelper instance;
    public static DatabaseHelper getInstance() {
        if (instance == null) instance = new DatabaseHelper();
        return instance;
    }

    private DatabaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // MEDICINES

    public interface MedicineCallback {
        void onSuccess(List<MedicineItem> medicines);
        void onFailure(String error);
    }

    public void getAllMedicines(MedicineCallback callback) {
        db.collection(COL_MEDICINES)
                .get()
                .addOnSuccessListener(query -> {
                    List<MedicineItem> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        list.add(docToMedicine(doc));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getAllMedicines failed", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void getMedicinesByCategory(String categoryKey, MedicineCallback callback) {
        db.collection(COL_MEDICINES)
                .whereEqualTo("categoryKey", categoryKey)
                .get()
                .addOnSuccessListener(query -> {
                    List<MedicineItem> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) list.add(docToMedicine(doc));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getMedicinesByPharmacy(String pharmacyName, MedicineCallback callback) {
        db.collection(COL_MEDICINES)
                .whereEqualTo("pharmacyName", pharmacyName)
                .get()
                .addOnSuccessListener(query -> {
                    List<MedicineItem> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) list.add(docToMedicine(doc));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void searchMedicines(String searchText, MedicineCallback callback) {
        String end = searchText.substring(0, searchText.length() - 1)
                + (char)(searchText.charAt(searchText.length() - 1) + 1);

        db.collection(COL_MEDICINES)
                .orderBy("name")
                .startAt(searchText)
                .endAt(end)
                .get()
                .addOnSuccessListener(query -> {
                    List<MedicineItem> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) list.add(docToMedicine(doc));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getPriceComparison(String medicineName, MedicineCallback callback) {
        db.collection(COL_MEDICINES)
                .whereEqualTo("name", medicineName)
                .orderBy("price", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<MedicineItem> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) list.add(docToMedicine(doc));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private MedicineItem docToMedicine(QueryDocumentSnapshot doc) {
        MedicineItem m = new MedicineItem();
        m.id          = doc.getId();
        m.name        = str(doc, "name");
        m.brandName   = str(doc, "brandName");
        m.category    = str(doc, "category");
        m.type        = str(doc, "type");
        m.categoryKey = str(doc, "categoryKey");
        m.pharmacyName= str(doc, "pharmacyName");
        m.price       = doc.getLong("price") != null ? doc.getLong("price").intValue() : 0;
        m.inStock     = doc.getBoolean("inStock") != null ? doc.getBoolean("inStock") : true;
        return m;
    }

    // ORDERS

    public interface SimpleCallback {
        void onSuccess(String documentId);
        void onFailure(String error);
    }

    public void placeOrder(String customerId, String pharmacyId, String pharmacyName,
                           List<Map<String, Object>> items, int total,
                           String paymentMethod, String address,
                           SimpleCallback callback) {

        Map<String, Object> order = new HashMap<>();
        order.put("customerId",     customerId);
        order.put("pharmacyId",     pharmacyId);
        order.put("pharmacyName",   pharmacyName);
        order.put("items",          items);
        order.put("total",          total);
        order.put("paymentMethod",  paymentMethod);
        order.put("address",        address);
        order.put("status",         "pending");
        order.put("createdAt",      com.google.firebase.Timestamp.now());

        db.collection(COL_ORDERS)
                .add(order)
                .addOnSuccessListener(ref -> callback.onSuccess(ref.getId()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface OrderCallback {
        void onSuccess(List<Map<String, Object>> orders);
        void onFailure(String error);
    }

    public void getCustomerOrders(String customerId, OrderCallback callback) {
        db.collection(COL_ORDERS)
                .whereEqualTo("customerId", customerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Map<String, Object> data = doc.getData();
                        data.put("orderId", doc.getId());
                        list.add(data);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // REVIEWS

    public void submitReview(String customerId, String orderId,
                             String pharmacyId, int pharmacyRating, String pharmacyComment,
                             String riderId, int riderRating, String riderComment,
                             SimpleCallback callback) {

        Map<String, Object> review = new HashMap<>();
        review.put("customerId",       customerId);
        review.put("orderId",          orderId);
        review.put("pharmacyId",       pharmacyId);
        review.put("pharmacyRating",   pharmacyRating);
        review.put("pharmacyComment",  pharmacyComment);
        review.put("riderId",          riderId);
        review.put("riderRating",      riderRating);
        review.put("riderComment",     riderComment);
        review.put("createdAt",        com.google.firebase.Timestamp.now());

        db.collection(COL_REVIEWS)
                .add(review)
                .addOnSuccessListener(ref -> callback.onSuccess(ref.getId()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // HELPERS

    private String str(QueryDocumentSnapshot doc, String key) {
        return doc.getString(key) != null ? doc.getString(key) : "";
    }

    // DATA MODEL

    public static class MedicineItem {
        public String id;
        public String name;
        public String brandName;
        public String category;
        public String type;
        public String categoryKey;
        public String pharmacyName;
        public int    price;
        public boolean inStock;
    }
}
