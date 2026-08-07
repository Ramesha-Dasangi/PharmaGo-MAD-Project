package com.nibm.pharmagomadproject.customer.activities.home;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import com.nibm.pharmagomadproject.R;

import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineListActivity;
import com.nibm.pharmagomadproject.customer.activities.notification.NotificationsActivity;
import com.nibm.pharmagomadproject.customer.activities.order.CartActivity;
import com.nibm.pharmagomadproject.customer.activities.order.OrderHistoryActivity;
import com.nibm.pharmagomadproject.customer.activities.pharmacy.PharmacyDetailsActivity;
import com.nibm.pharmagomadproject.customer.activities.profile.ProfileActivity;

import com.nibm.pharmagomadproject.customer.adapter.PharmacyAdapter;
import com.nibm.pharmagomadproject.customer.models.Pharmacy;


import java.util.ArrayList;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.common.api.ResolvableApiException;
import android.app.Activity;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

public class HomeActivity extends AppCompatActivity {


    private FirebaseFirestore db;
    private RecyclerView rvNearbyPharmacies;
    private ArrayList<Pharmacy> pharmacyList;
    private PharmacyAdapter pharmacyAdapter;

    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;

    // Launcher for GPS enable dialog result
    private final androidx.activity.result.ActivityResultLauncher<IntentSenderRequest> gpsSettingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    getCurrentLocation();
                } else {
                    // GPS still off — load pharmacies without distance
                    loadPharmacies();
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted   = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if ((fineGranted != null && fineGranted) || (coarseGranted != null && coarseGranted)) {
                    // Permission granted — now check GPS is ON
                    checkGpsEnabled();
                } else {
                    // Permanently denied? Guide user to Settings
                    boolean permanentlyDenied =
                            !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (permanentlyDenied) {
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Location Permission Required")
                            .setMessage("PharmaGo needs your location to show nearby pharmacies. Please enable it in App Settings.")
                            .setPositiveButton("Open Settings", (d, w) -> {
                                android.net.Uri uri = android.net.Uri.fromParts("package", getPackageName(), null);
                                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
                            })
                            .setNegativeButton("Not now", (d, w) -> loadPharmacies())
                            .show();
                    } else {
                        loadPharmacies();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_home);

        if(getSupportActionBar()!=null){

            getSupportActionBar().hide();

        }

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadGreetingAndName();

        // Search

        safeClick(
                R.id.searchBar,
                v -> openMedicineList(
                        "search",
                        "Search medicines",
                        ""
                )
        );

        // Categories

        safeClick(
                R.id.catRx,
                v -> openMedicineList(
                        "category",
                        "Prescription medicines",
                        "rx"
                )
        );


        safeClick(
                R.id.catFirstAid,
                v -> openMedicineList(
                        "category",
                        "First Aid",
                        "firstaid"
                )
        );


        safeClick(
                R.id.catVitamins,
                v -> openMedicineList(
                        "category",
                        "Vitamins",
                        "vitamins"
                )
        );


        safeClick(
                R.id.catChronic,
                v -> openMedicineList(
                        "category",
                        "Chronic medicines",
                        "chronic"
                )
        );


        safeClick(
                R.id.catBaby,
                v -> openMedicineList(
                        "category",
                        "Baby Care",
                        "baby"
                )
        );


        safeClick(
                R.id.catEyeCare,
                v -> openMedicineList(
                        "category",
                        "Eye Care",
                        "eyecare"
                )
        );


        safeClick(
                R.id.catDental,
                v -> openMedicineList(
                        "category",
                        "Dental",
                        "dental"
                )
        );


        safeClick(
                R.id.catOtc,
                v -> openMedicineList(
                        "category",
                        "OTC medicines",
                        "otc"
                )
        );

        // Load Pharmacies

        rvNearbyPharmacies =
                findViewById(
                        R.id.rvNearbyPharmacies
                );


        pharmacyList =
                new ArrayList<>();



        pharmacyAdapter =
                new PharmacyAdapter(
                        pharmacyList,
                        pharmacy -> {


                             Intent intent =
                                     new Intent(
                                             this,
                                             PharmacyDetailsActivity.class
                                     );

                             intent.putExtra(
                                     "pharmacyId",
                                     pharmacy.getId()
                             );

                             intent.putExtra(
                                     "ownerId",
                                     pharmacy.getOwnerId()
                             );

                             intent.putExtra(
                                     "name",
                                     pharmacy.getName()
                             );

                             intent.putExtra(
                                     "address",
                                     pharmacy.getAddress()
                             );

                             if (pharmacy.getDistanceKm() >= 0) {
                                 intent.putExtra("pharmacy_distance", String.format("%.1f km away", pharmacy.getDistanceKm()));
                             } else {
                                 intent.putExtra("pharmacy_distance", "0.5 km away");
                             }

                            if (pharmacy.getRatingCount() > 0 && pharmacy.getRating() > 0) {
                                intent.putExtra("pharmacy_rating", "⭐ " + String.format("%.1f", pharmacy.getRating()));
                            } else {
                                intent.putExtra("pharmacy_rating", "⭐ New");
                            }
                             startActivity(intent);

                        }
                );



        rvNearbyPharmacies.setLayoutManager(
                new LinearLayoutManager(this)
        );


        rvNearbyPharmacies.setAdapter(
                pharmacyAdapter
        );



        // Request/Check location permission on startup
        // Small delay so Activity is fully ready before showing dialogs
        rvNearbyPharmacies.post(this::checkAndRequestLocationPermission);

        // Notification

        safeClick(
                R.id.btnNotification,
                v -> startActivity(
                        new Intent(
                                this,
                                NotificationsActivity.class
                        )
                )
        );
        setupBottomNav();
    }

    private void checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Already granted — still check if GPS is turned ON
            checkGpsEnabled();
            return;
        }

        // Check if permanently denied (user denied twice or tapped "Don't ask again")
        boolean permanentlyDenied =
                !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permanentlyDenied) {
            // Show a dialog directing them to Settings — system dialog won't appear
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Location Access Needed")
                .setMessage("To show nearby pharmacies, PharmaGo needs location access.\n\nPlease go to Settings -> Permissions → Location → Allow.")
                .setPositiveButton("Open Settings", (d, w) -> {
                    android.net.Uri uri = android.net.Uri.fromParts("package", getPackageName(), null);
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
                })
                .setNegativeButton("Skip", (d, w) -> loadPharmacies())
                .setCancelable(false)
                .show();
        } else {
            // First time OR previously denied once — show our explanation first, then request
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Nearby Pharmacies")
                .setMessage("Allow PharmaGo to use your location so we can show pharmacies near you.")
                .setPositiveButton("Allow Location", (d, w) -> locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }))
                .setNegativeButton("Not now", (d, w) -> loadPharmacies())
                .setCancelable(false)
                .show();
        }
    }

    //Check if device GPS is ON. if not, show system dialog to enable it
    private void checkGpsEnabled() {
        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMaxUpdates(1).build();
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(req)
                .setAlwaysShow(true)   // forces the dialog to always appear
                .build();

        SettingsClient client = LocationServices.getSettingsClient(this);
        client.checkLocationSettings(settingsRequest)
                .addOnSuccessListener(response -> getCurrentLocation())  // GPS already ON
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        // GPS is off — show the system "Turn on GPS?" dialog
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            IntentSenderRequest req2 = new IntentSenderRequest.Builder(
                                    resolvable.getResolution().getIntentSender()).build();
                            gpsSettingsLauncher.launch(req2);
                        } catch (Exception ex) {
                            loadPharmacies();
                        }
                    } else {
                        loadPharmacies();
                    }
                });
    }

    private void getCurrentLocation() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        // 1. Try loading saved location from Firestore profile first
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Double lat = doc.getDouble("latitude");
                            Double lng = doc.getDouble("longitude");
                            if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                                userLatitude = lat;
                                userLongitude = lng;
                                loadPharmacies();
                            }
                        }
                    });
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadPharmacies();
            return;
        }

        // 2. Fetch live GPS location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadPharmacies();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        userLatitude  = location.getLatitude();
                        userLongitude = location.getLongitude();
                        if (uid != null) {
                            Map<String, Object> locData = new HashMap<>();
                            locData.put("latitude",  userLatitude);
                            locData.put("longitude", userLongitude);
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(uid).update(locData);
                        }
                        loadPharmacies();
                    } else {
                        // Fallback: request a single fresh location update
                        LocationRequest req = new LocationRequest.Builder(
                                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                                .setMaxUpdates(1)
                                .build();
                        try {
                            fusedLocationClient.requestLocationUpdates(req, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    Location loc = locationResult.getLastLocation();
                                    if (loc != null) {
                                        userLatitude  = loc.getLatitude();
                                        userLongitude = loc.getLongitude();
                                        if (uid != null) {
                                            Map<String, Object> locData = new HashMap<>();
                                            locData.put("latitude",  userLatitude);
                                            locData.put("longitude", userLongitude);
                                            FirebaseFirestore.getInstance().collection("users")
                                                    .document(uid).update(locData);
                                        }
                                        loadPharmacies();
                                    }
                                }
                            }, android.os.Looper.getMainLooper());
                        } catch (SecurityException se) {
                            loadPharmacies();
                        }
                    }
                })
                .addOnFailureListener(e -> loadPharmacies());
    }

    private void loadPharmacies() {
        db.collection("pharmacies")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(snapshots -> {
                    pharmacyList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Pharmacy pharmacy = doc.toObject(Pharmacy.class);
                        if (pharmacy != null) {
                            pharmacy.setId(doc.getId());
                            Long rCount = doc.getLong("ratingCount");
                            if (rCount != null) pharmacy.setRatingCount(rCount);

                            // Compute distance if we have user coordinates
                            if (userLatitude != 0.0 && userLongitude != 0.0) {
                                double pLat = pharmacy.getLatitude();
                                double pLng = pharmacy.getLongitude();
                                if (pLat == 0.0 && pLng == 0.0) {
                                    pLat = 6.9271;
                                    pLng = 79.8612;
                                }
                                float[] results = new float[1];
                                Location.distanceBetween(
                                        userLatitude, userLongitude,
                                        pLat, pLng,
                                        results
                                );
                                double distanceKm = results[0] / 1000.0;
                                pharmacy.setDistanceKm(distanceKm);
                            } else {
                                pharmacy.setDistanceKm(-1.0);
                            }

                            pharmacyList.add(pharmacy);
                        }
                    }

                    // Sort: closest first, unknown distance last
                    pharmacyList.sort((p1, p2) -> {
                        double d1 = p1.getDistanceKm();
                        double d2 = p2.getDistanceKm();
                        if (d1 < 0 && d2 < 0) return 0;
                        if (d1 < 0) return 1;
                        if (d2 < 0) return -1;
                        return Double.compare(d1, d2);
                    });

                    pharmacyAdapter.notifyDataSetChanged();
                });
    }

    private void openMedicineList(
            String mode,
            String title,
            String query
    ){

        Intent intent =
                new Intent(
                        this,
                        MedicineListActivity.class
                );


        intent.putExtra(
                MedicineListActivity.EXTRA_MODE,
                mode
        );


        intent.putExtra(
                MedicineListActivity.EXTRA_TITLE,
                title
        );


        intent.putExtra(
                MedicineListActivity.EXTRA_QUERY,
                query.toLowerCase()
        );
        startActivity(intent);
    }

    private void safeClick(
            int id,
            android.view.View.OnClickListener listener
    ){
        try{
            android.view.View view =
                    findViewById(id);

            if(view!=null){

                view.setOnClickListener(
                        listener
                );

            }
        }catch(Exception ignored){}
    }

    private void setupBottomNav(){
        safeClick(
                R.id.navHome,
                v -> {}
        );

        safeClick(
                R.id.navSearch,
                v -> openMedicineList(
                        "search",
                        "Search medicines",
                        ""
                )
        );

        safeClick(
                R.id.navCart,
                v -> startActivity(
                        new Intent(
                                this,
                                CartActivity.class
                        )
                )
        );

        safeClick(
                R.id.navOrders,
                v -> startActivity(
                        new Intent(
                                this,
                                OrderHistoryActivity.class
                        )
                )
        );

        safeClick(
                R.id.navProfile,
                v -> startActivity(
                        new Intent(
                                this,
                                ProfileActivity.class
                        )
                )
        );
    }

    private com.google.firebase.firestore.ListenerRegistration notifListenerRegistration;

    @Override
    protected void onResume() {
        super.onResume();
        loadGreetingAndName();
        checkUnreadNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (notifListenerRegistration != null) {
            notifListenerRegistration.remove();
            notifListenerRegistration = null;
        }
    }

    private void checkUnreadNotifications() {
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        TextView dot = findViewById(R.id.notifDot);
        if (dot == null) return;

        if (notifListenerRegistration != null) {
            notifListenerRegistration.remove();
        }

        notifListenerRegistration = db.collection("notifications")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((query, error) -> {
                    if (error != null || query == null) return;
                    int unreadCount = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : query) {
                        Boolean read = doc.getBoolean("isRead");
                        if (read == null || !read) {
                            unreadCount++;
                        }
                    }
                    if (unreadCount > 0) {
                        dot.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
                        dot.setVisibility(View.VISIBLE);
                    } else {
                        dot.setVisibility(View.GONE);
                    }
                });
    }


    private void loadGreetingAndName() {
        // Time-aware greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12)  greeting = "Good morning \u2600\ufe0f";
        else if (hour < 17)          greeting = "Good afternoon \uD83C\uDF1E";
        else if (hour < 21)          greeting = "Good evening \uD83C\uDF07";
        else                         greeting = "Good night \uD83C\uDF19";

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        if (tvGreeting != null) tvGreeting.setText(greeting);

        // Load username from Firestore
        com.google.firebase.auth.FirebaseAuth auth =
                com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            TextView tvName = findViewById(R.id.tvUserName);
                            if (tvName != null && name != null) {
                                String first = name.contains(" ") ? name.split(" ")[0] : name;
                                tvName.setText(first);
                            }
                        }
                    });
        }
    }

}