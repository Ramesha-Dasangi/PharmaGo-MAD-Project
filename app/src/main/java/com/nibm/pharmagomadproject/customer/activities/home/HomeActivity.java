package com.nibm.pharmagomadproject.customer.activities.home;


import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


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
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.activity.result.contract.ActivityResultContracts;

public class HomeActivity extends AppCompatActivity {


    private FirebaseFirestore db;
    private RecyclerView rvNearbyPharmacies;
    private ArrayList<Pharmacy> pharmacyList;
    private PharmacyAdapter pharmacyAdapter;

    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;

    private final androidx.activity.result.ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if ((fineGranted != null && fineGranted) || (coarseGranted != null && coarseGranted)) {
                    getCurrentLocation();
                } else {
                    loadPharmacies();
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



        // ----------------------------
        // Search
        // ----------------------------

        safeClick(
                R.id.searchBar,
                v -> openMedicineList(
                        "search",
                        "Search medicines",
                        ""
                )
        );





        // ----------------------------
        // Categories
        // ----------------------------


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





        // ----------------------------
        // Load Pharmacies
        // ----------------------------


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

                             intent.putExtra("pharmacy_rating", "⭐ " + String.format("%.1f", pharmacy.getRating()));

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }







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







    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadPharmacies();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLatitude = location.getLatitude();
                            userLongitude = location.getLongitude();
                        }
                        loadPharmacies();
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

                            // Compute distance if we have coordinates
                            if (userLatitude != 0.0 && userLongitude != 0.0
                                    && pharmacy.getLatitude() != 0.0 && pharmacy.getLongitude() != 0.0) {
                                float[] results = new float[1];
                                Location.distanceBetween(
                                        userLatitude, userLongitude,
                                        pharmacy.getLatitude(), pharmacy.getLongitude(),
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



}