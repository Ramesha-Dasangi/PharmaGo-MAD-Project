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

public class LiveMapActivity extends AppCompatActivity {

    private static final String TAG = "LiveMap";
    private static final int LOCATION_PERMISSION_REQUEST = 101;

    private MapView map = null;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker riderMarker;
    private Polyline routePolyline;

    private GeoPoint currentRiderPos = new GeoPoint(6.9271, 79.8612);
    private final GeoPoint dropOff = new GeoPoint(6.9350, 79.8550);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_live_map);
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
        routePolyline.getOutlinePaint().setColor(Color.parseColor("#108A68"));
        routePolyline.getOutlinePaint().setStrokeWidth(16f);
        routePolyline.getOutlinePaint().setAlpha(230);
        routePolyline.getOutlinePaint().setAntiAlias(true);
        map.getOverlays().add(routePolyline);

        // Drop-off Marker
        Marker dropMarker = new Marker(map);
        dropMarker.setPosition(dropOff);
        dropMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        dropMarker.setTitle("Drop-off");
        dropMarker.setSnippet("Galle Rd, Col 3");
        dropMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_pin));
        map.getOverlays().add(dropMarker);

        // Rider Marker
        riderMarker = new Marker(map);
        riderMarker.setPosition(currentRiderPos);
        riderMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        riderMarker.setTitle("You");
        riderMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_rider_marker));
        map.getOverlays().add(riderMarker);

        map.invalidate();

        // Fetch road route immediately
        fetchAndDrawRoute(currentRiderPos);
    }

    private void fetchAndDrawRoute(GeoPoint riderPos) {
        List<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(riderPos);
        waypoints.add(dropOff);

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

    /** Zoom to fit rider + drop-off in view */
    private void zoomToFitAll() {
        double minLat = Math.min(currentRiderPos.getLatitude(), dropOff.getLatitude());
        double maxLat = Math.max(currentRiderPos.getLatitude(), dropOff.getLatitude());
        double minLon = Math.min(currentRiderPos.getLongitude(), dropOff.getLongitude());
        double maxLon = Math.max(currentRiderPos.getLongitude(), dropOff.getLongitude());

        double pad = 0.008;
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

        // Re-center: zoom to show rider + drop-off
        View btnRecenter = findViewById(R.id.btnRecenter);
        if (btnRecenter != null) {
            btnRecenter.setOnClickListener(v -> zoomToFitAll());
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
