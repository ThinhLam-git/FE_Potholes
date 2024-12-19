package com.example.authentication_uiux.ui.home.MapComponents;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LocationSearchManager {
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search?";
    private static final String REVERSE_API = "https://nominatim.openstreetmap.org/reverse?";

    public interface SearchCallBack{
        void onLocationFound(double lat, double lon, String display_name);
        void onError(String message);
    }
    public static void searchLocation(String query, SearchCallBack callBack){
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String encodedQuery = URLEncoder.encode(params[0], "UTF-8");
                    URL url = new URL(NOMINATIM_API + "q=" + encodedQuery + "&format=json&limit=1");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "PotholeDetector");

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
                    JSONArray jsonArray = new JSONArray(result);
                    if (jsonArray.length() > 0) {
                        JSONObject location = jsonArray.getJSONObject(0);
                        double lat = location.getDouble("lat");
                        double lon = location.getDouble("lon");
                        String name = location.getString("display_name");
                        callBack.onLocationFound(lat, lon, name);
                    } else {
                        callBack.onError("Không tìm thấy địa điểm");
                    }
                } catch (Exception e) {
                    callBack.onError("Lỗi tìm kiếm: " + e.getMessage());
                }
            }
        }.execute(query);
    }

    public static void reverseGeocode(double lat, double lon, SearchCallBack callback) {
        new AsyncTask<Double, Void, String>() {
            @Override
            protected String doInBackground(Double... params) {
                try {
                    URL url = new URL(REVERSE_API + "lat=" + params[0] + "&lon=" + params[1] + "&format=json");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "PotholeDetector");

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
                    JSONObject location = new JSONObject(result);
                    String name = location.getString("display_name");
                    callback.onLocationFound(lat, lon, name);
                } catch (Exception e) {
                    callback.onError("Lỗi truy ngược: " + e.getMessage());
                }
            }
        }.execute(lat, lon);
    }
}

