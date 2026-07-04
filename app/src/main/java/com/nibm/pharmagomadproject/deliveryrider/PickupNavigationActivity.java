package com.nibm.pharmagomadproject.deliveryrider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.nibm.pharmagomadproject.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PickupNavigationActivity extends AppCompatActivity {

    private static final String TAG = "PickupNav";
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private MapView map = null;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker riderMarker;
    private Polyline routePolyline;

    private GeoPoint currentRiderPos = new GeoPoint(6.9271, 79.8612);
    private final GeoPoint pickup1 = new GeoPoint(6.9290, 79.8600);
    private final GeoPoint pickup2 = new GeoPoint(6.9250, 79.8630);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_pickup_navigation);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupMap();
        setupButtons();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void setupMap() {
        map = findViewById(R.id.mapView);
        if (map == null) return;

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(currentRiderPos);

        // Route polyline — add FIRST so markers render on top
        routePolyline = new Polyline(map);
        routePolyline.getOutlinePaint().setColor(Color.parseColor("#00BA7A"));
        routePolyline.getOutlinePaint().setStrokeWidth(16f);
        routePolyline.getOutlinePaint().setAlpha(230);
        routePolyline.getOutlinePaint().setAntiAlias(true);
        map.getOverlays().add(routePolyline);

        // Pickup 1 Marker
        Marker p1 = new Marker(map);
        p1.setPosition(pickup1);
        p1.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        p1.setTitle("MediCare Pharmacy");
        p1.setSnippet("Paracetamol 500mg ×2");
        p1.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_pin));
        map.getOverlays().add(p1);

        // Pickup 2 Marker
        Marker p2 = new Marker(map);
        p2.setPosition(pickup2);
        p2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        p2.setTitle("City Pharma");
        p2.setSnippet("Vitamin C 1000mg ×1");
        p2.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_pin));
        map.getOverlays().add(p2);

        // Rider Marker
        riderMarker = new Marker(map);
        riderMarker.setPosition(currentRiderPos);
        riderMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        riderMarker.setTitle("You");
        riderMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_rider_marker));
        map.getOverlays().add(riderMarker);

        map.invalidate();

        // Fetch road route right away
        fetchAndDrawRoute(currentRiderPos);
    }

    private void fetchAndDrawRoute(GeoPoint riderPos) {
        List<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(riderPos);
        waypoints.add(pickup1);
        waypoints.add(pickup2);

        RouteUtils.fetchRoute(waypoints, executor, mainHandler, new RouteUtils.RouteCallback() {
            @Override
            public void onRouteReady(List<GeoPoint> points) {
                routePolyline.setPoints(points);
                map.invalidate();
                Log.d(TAG, "Route drawn: " + points.size() + " points");
            }

            @Override
            public void onFallback(List<GeoPoint> straightLine) {
                routePolyline.setPoints(straightLine);
                map.invalidate();
                Log.w(TAG, "Fallback straight line drawn");
            }
        });
    }

    private void zoomToFitAll() {
        double minLat = Math.min(currentRiderPos.getLatitude(),
                Math.min(pickup1.getLatitude(), pickup2.getLatitude()));
        double maxLat = Math.max(currentRiderPos.getLatitude(),
                Math.max(pickup1.getLatitude(), pickup2.getLatitude()));
        double minLon = Math.min(currentRiderPos.getLongitude(),
                Math.min(pickup1.getLongitude(), pickup2.getLongitude()));
        double maxLon = Math.max(currentRiderPos.getLongitude(),
                Math.max(pickup1.getLongitude(), pickup2.getLongitude()));

        // Add padding around the bounding box
        double pad = 0.006;
        BoundingBox box = new BoundingBox(maxLat + pad, maxLon + pad, minLat - pad, minLon - pad);
        map.post(() -> map.zoomToBoundingBox(box, true, 150));
    }

    private void startLocationUpdates() {
        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) {
                    currentRiderPos = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                    riderMarker.setPosition(currentRiderPos);
                    map.invalidate();
                    fetchAndDrawRoute(currentRiderPos);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(req, locationCallback, getMainLooper());
            fusedLocationClient.getLastLocation().addOnSuccessListener(loc -> {
                if (loc != null) {
                    currentRiderPos = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                    riderMarker.setPosition(currentRiderPos);
                    fetchAndDrawRoute(currentRiderPos);
                }
            });
        }
    }

    private void setupButtons() {
        Button btnNavigate = findViewById(R.id.btnNavigateToPickup);
        if (btnNavigate != null) {
            btnNavigate.setOnClickListener(v ->
                    startActivity(new Intent(this, LiveMapActivity.class)));
        }

        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent i = new Intent(this, RiderDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) {
            navHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, DeliveryHistoryActivity.class)));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, RiderProfileActivity.class)));
        }

        // Map tab = zoom to fit all stops
        View navMap = findViewById(R.id.navMap);
        if (navMap != null) {
            navMap.setOnClickListener(v -> zoomToFitAll());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    public void onResume() { super.onResume(); if (map != null) map.onResume(); }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        if (fusedLocationClient != null && locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
