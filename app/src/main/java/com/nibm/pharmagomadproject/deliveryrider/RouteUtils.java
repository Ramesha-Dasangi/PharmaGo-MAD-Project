package com.nibm.pharmagomadproject.deliveryrider;

import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RouteUtils {

    private static final String TAG = "RouteUtils";

    public interface RouteCallback {
        void onRouteReady(List<GeoPoint> points);
        void onFallback(List<GeoPoint> straightLine);
    }

    public static void fetchRoute(List<GeoPoint> waypoints, ExecutorService executor,
                                  Handler mainHandler, RouteCallback callback) {
        if (waypoints == null || waypoints.size() < 2) {
            callback.onFallback(waypoints);
            return;
        }

        executor.execute(() -> {
            try {
                // Build coordinates string: lon,lat;lon,lat
                StringBuilder coords = new StringBuilder();
                for (int i = 0; i < waypoints.size(); i++) {
                    if (i > 0) coords.append(";");
                    GeoPoint p = waypoints.get(i);
                    coords.append(p.getLongitude()).append(",").append(p.getLatitude());
                }

                // Use router.project-osrm.org — free, no key needed
                String urlStr = "https://router.project-osrm.org/route/v1/driving/"
                        + coords + "?overview=full&geometries=geojson";

                Log.d(TAG, "Requesting: " + urlStr);

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "DeliveryRiderApp/1.0");

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode != 200) {
                    Log.e(TAG, "Bad response: " + responseCode);
                    mainHandler.post(() -> callback.onFallback(waypoints));
                    return;
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                // Parse GeoJSON
                JSONObject json = new JSONObject(sb.toString());
                String code = json.optString("code", "");
                if (!"Ok".equals(code)) {
                    Log.e(TAG, "OSRM code: " + code);
                    mainHandler.post(() -> callback.onFallback(waypoints));
                    return;
                }

                JSONArray routes = json.getJSONArray("routes");
                JSONArray coordinates = routes.getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates");

                List<GeoPoint> routePoints = new ArrayList<>();
                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray c = coordinates.getJSONArray(i);
                    routePoints.add(new GeoPoint(c.getDouble(1), c.getDouble(0)));
                }

                Log.d(TAG, "Route ready: " + routePoints.size() + " points");
                mainHandler.post(() -> callback.onRouteReady(routePoints));

            } catch (Exception e) {
                Log.e(TAG, "Route fetch exception: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onFallback(waypoints));
            }
        });
    }
}
