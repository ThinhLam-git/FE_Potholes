package com.example.authentication_uiux.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.authentication_uiux.R;
import com.example.authentication_uiux.models.PotholeData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.authentication_uiux.API.PotholeApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker currentLocationMarker;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 30.0f;
    private boolean isShaking = false;
    private static final long ALERT_DELAY_MS = 5000;
    private long lastAlertTime = 0;
    private View popupView;
    private Handler popupHandler = new Handler();

    private PotholeApi potholeApi;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo cảm biến gia tốc
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Khởi tạo bản đồ
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("HomeFragment", "Error: Map Fragment is null");
        }

        // Khởi tạo FusedLocationProviderClient và các nút điều khiển
        setupLocationAndButtons(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("HomeFragment", "Map is ready!");
        mMap = googleMap;

        // Đặt vị trí mặc định
        LatLng location = new LatLng(10.870894, 106.803054);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float delta = (float) Math.sqrt(x * x + y * y + z * z);

            long currentTime = System.currentTimeMillis();
            if (delta > SHAKE_THRESHOLD && currentTime - lastAlertTime > ALERT_DELAY_MS) {
                lastAlertTime = currentTime;
                showPotholePopup();
            }
        }
    }

    private void showPotholePopup() {
        if (popupView != null) return; // Prevent multiple pop-ups

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        popupView = inflater.inflate(R.layout.popup_pothole, null);
        Button btnConfirm = popupView.findViewById(R.id.btn_confirm);

        btnConfirm.setOnClickListener(v -> {
            if (currentLocationMarker != null) {
                // Get the current location of the pothole from the marker
                LatLng potholeLocation = currentLocationMarker.getPosition();

                if (potholeLocation != null) {
                    // Create a custom marker for the pothole with a different icon
                    mMap.addMarker(new MarkerOptions().position(potholeLocation).title("Ổ gà"));

                    // Call the method to save pothole data to MongoDB
                    savePotholeDataToMongoDB(potholeLocation);

                    // Remove the popup after confirmation
                    removePopup();
                } else {
                    Log.e("HomeFragment", "Current location marker position is null");
                }
            } else {
                Log.e("HomeFragment", "Current location marker is not initialized");
            }
        });

        // Add the popup to the screen
        ((ViewGroup) requireView()).addView(popupView);

        // Set timer to auto-dismiss the popup after 10 seconds
        popupHandler.postDelayed(this::removePopup, 10000);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:3000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        potholeApi = retrofit.create(PotholeApi.class);
    }

    private void savePotholeDataToMongoDB(LatLng potholeLocation) {
        if (potholeApi == null) {
            setupRetrofit();
        }

        // Ensure the location is valid before sending it to the server
        if (potholeLocation != null) {
            // Sample data
            String detectionTime = "2024-11-11T08:00:00Z"; // Use the actual detection time
            String user = "ThinhLam"; // Replace with actual user information
            String status = "reported";

            // Log location details
            Log.d("HomeFragment", "Pothole Location: Latitude = " + potholeLocation.latitude + ", Longitude = " + potholeLocation.longitude);

            // Create PotholeData object
            PotholeData potholeData = new PotholeData(
                    potholeLocation.latitude,
                    potholeLocation.longitude,
                    detectionTime,
                    user,
                    status
            );

            // Log the PotholeData being sent
            Log.d("HomeFragment", "Sending Pothole Data: " + potholeData.toString());

            // Perform the API call to save pothole data to MongoDB
            Call<Void> call = potholeApi.addPothole(potholeData);

            // Log request URL and headers
            Log.d("HomeFragment", "Request URL: " + call.request().url());
            Log.d("HomeFragment", "Request Headers: " + call.request().headers());

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("HomeFragment", "Pothole data saved successfully");
                    } else {
                        // Log the response code and the response body
                        Log.e("HomeFragment", "Error saving pothole data1: " + response.message());
                        try {
                            String responseBody = response.errorBody() != null ? response.errorBody().string() : "No response body";
                            Log.e("HomeFragment", "Error Response Body: " + responseBody);
                        } catch (IOException e) {
                            Log.e("HomeFragment", "Error reading response body: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    // Log detailed error information
                    Log.e("HomeFragment", "Error saving pothole data2: " + t.getMessage());
                    t.printStackTrace(); // Print stack trace for more details
                }
            });
        } else {
            Log.e("HomeFragment", "Pothole location is null, cannot save data");
        }
    }


    private void removePopup() {
        if (popupView != null) {
            ((ViewGroup) requireView()).removeView(popupView);
            popupView = null;
            popupHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý
    }

    private void setupLocationAndButtons(View root) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        Button zoomInButton = root.findViewById(R.id.button_zoom_in);
        zoomInButton.setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomIn());
        });

        Button zoomOutButton = root.findViewById(R.id.button_zoom_out);
        zoomOutButton.setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomOut());
        });

        Button trackLocationButton = root.findViewById(R.id.button_track_location);
        trackLocationButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            startLocationUpdates();
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (mMap == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (currentLocationMarker != null) {
                            currentLocationMarker.setPosition(currentLocation);
                        } else {
                            currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Bạn đang ở đây"));
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18.0f));
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
