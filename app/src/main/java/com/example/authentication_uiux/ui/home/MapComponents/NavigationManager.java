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
        void onRouteFound(ArrayList<GeoPoint> route, String duration, String distance);
        void onRouteFound(List<GeoPoint> route, String duration, String distance);
        void onError(String message);
    }

    public static void getRoute(GeoPoint start, GeoPoint end, NavigationCallback callback) {
        if (start == null || end == null) {
            callback.onError("Điểm đầu hoặc điểm cuối không hợp lệ");
            return;
        }

        new AsyncTask<GeoPoint, Void, String>() {
            @Override
            protected String doInBackground(GeoPoint... points) {
                HttpURLConnection conn = null;
                BufferedReader reader = null;

                try {
                    String url = OSRM_API +
                            points[0].getLongitude() + "," + points[0].getLatitude() + ";" +
                            points[1].getLongitude() + "," + points[1].getLatitude() +
                            "?overview=full&geometries=polyline";

                    conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(10000); // 10 seconds timeout
                    conn.setReadTimeout(10000);

                    // Kiểm tra response code
                    int responseCode = conn.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        return null;
                    }

                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    return response.toString();

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    try {
                        if (reader != null) reader.close();
                        if (conn != null) conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null) {
                    callback.onError("Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng");
                    return;
                }

                try {
                    JSONObject json = new JSONObject(result);

                    // Kiểm tra có routes array không
                    if (!json.has("routes") || json.getJSONArray("routes").length() == 0) {
                        callback.onError("Không tìm thấy đường đi phù hợp");
                        return;
                    }

                    JSONObject route = json.getJSONArray("routes").getJSONObject(0);

                    // Kiểm tra các trường bắt buộc
                    if (!route.has("geometry") || !route.has("duration") || !route.has("distance")) {
                        callback.onError("Dữ liệu đường đi không hợp lệ");
                        return;
                    }

                    // Decode polyline to get route points
                    String geometry = route.getString("geometry");
                    List<GeoPoint> points = decodePolyline(geometry);

                    if (points.isEmpty()) {
                        callback.onError("Không thể tạo đường đi");
                        return;
                    }

                    // Get route information
                    double duration = route.getDouble("duration") / 60; // Convert to minutes
                    double distance = route.getDouble("distance") / 1000; // Convert to km

                    callback.onRouteFound(
                            new ArrayList<>(points),
                            String.format("%.1f phút", duration),
                            String.format("%.1f km", distance)
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                }
            }
        }.execute(start, end);
    }

    private static List<GeoPoint> decodePolyline(String encoded) {
        List<GeoPoint> points = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) {
            return points;
        }

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return points;
    }
}