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
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.authentication_uiux.R;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
    public void onMapReady(GoogleMap googleMap) {
        Log.d("HomeFragment", "Map is ready!");
        mMap = googleMap;

        // Đặt vị trí mặc định
        LatLng location = new LatLng(10.870894, 106.803054); // Thay đổi tọa độ theo ý muốn
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
                showPotholeConfirmationDialog();
            }
        }
    }

    private void showPotholeConfirmationDialog() {
        if (mMap != null && currentLocationMarker != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận ổ gà");
            builder.setMessage("Có muốn xác nhận vị trí hiện tại là một ổ gà không?");
            builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LatLng potholeLocation = currentLocationMarker.getPosition();
                    mMap.addMarker(new MarkerOptions().position(potholeLocation).title("Ổ gà"));
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            builder.show();
        }
    }

    private void showPotholePopup() {
        if (popupView != null) return; // Prevent multiple pop-ups

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        popupView = inflater.inflate(R.layout.popup_pothole, null);
        Button btnConfirm = popupView.findViewById(R.id.btn_confirm);

        btnConfirm.setOnClickListener(v -> {
            LatLng potholeLocation = currentLocationMarker.getPosition();
            mMap.addMarker(new MarkerOptions().position(potholeLocation).title("Ổ gà"));
            //savePotholeDataToMongoDB(potholeLocation);
            removePopup();
        });

        // Add the popup to the screen
        ((ViewGroup) requireView()).addView(popupView);

        // Set timer to auto-dismiss
        popupHandler.postDelayed(this::removePopup, 10000);
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
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || mMap == null) return;
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
