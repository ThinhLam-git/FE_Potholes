package com.example.authentication_uiux.ui.home.MapComponents;

import android.os.AsyncTask;

import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NavigationManager {
    private static final String OSRM_API = "http://router.project-osrm.org/route/v1/driving/";

    public interface NavigationCallback {
        void onRouteFound(ArrayList<GeoPoint> route,
                          String duration,
                          String distance);

        void onRouteFound(List<GeoPoint> route, String duration, String distance);
        void onError(String message);
    }

    public void getRoute(GeoPoint start, GeoPoint end, NavigationCallback callback) {
        new AsyncTask<GeoPoint, Void, String>() {
            @Override
            protected String doInBackground(GeoPoint... points) {
                try {
                    String url = OSRM_API +
                            points[0].getLongitude() + "," + points[0].getLatitude() + ";" +
                            points[1].getLongitude() + "," + points[1].getLatitude() +
                            "?overview=full&geometries=polyline";

                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return response.toString();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject json = new JSONObject(result);
                    JSONObject route = json.getJSONArray("routes").getJSONObject(0);

                    // Decode polyline to get route points
                    String geometry = route.getString("geometry");
                    List<GeoPoint> points = decodePolyline(geometry);

                    // Get route information
                    double duration = route.getDouble("duration") / 60; // Convert to minutes
                    double distance = route.getDouble("distance") / 1000; // Convert to km

                    callback.onRouteFound(points,
                            String.format("%.1f phút", duration),
                            String.format("%.1f km", distance));
                } catch (Exception e) {
                    callback.onError("Lỗi tìm đường: " + e.getMessage());
                }
            }
        }.execute(start, end);
    }

    private List<GeoPoint> decodePolyline(String encoded) {
        List<GeoPoint> points = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            points.add(new GeoPoint(lat / 1E5, lng / 1E5));
        }
        return points;
    }
}
