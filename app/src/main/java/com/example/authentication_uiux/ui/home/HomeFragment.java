package com.example.authentication_uiux.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.authentication_uiux.R;
import com.example.authentication_uiux.models.PotholeData;
import com.example.authentication_uiux.API.PotholeApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements OnMapReadyCallback, SensorEventListener {
    private static final String TAG = "HomeFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float SHAKE_THRESHOLD = 30.0f;
    private static final long ALERT_DELAY_MS = 5000;
    private static final String BASE_URL = "http://192.168.124.155:3000/";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker currentLocationMarker;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isShaking = false;
    private long lastAlertTime = 0;
    private View popupView;
    private Handler popupHandler;
    private EditText searchEditText;
    private ImageButton searchButton;
    private FloatingActionButton trackLocationButton;
    private ImageButton zoomInButton, zoomOutButton;
    private Geocoder geocoder;
    private PotholeApi potholeApi;
    private boolean isLocationTracking = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(root);
        initializeSensors();
        initializeMap();
        setupRetrofit();
        return root;
    }

    private void initializeViews(View root) {
        searchEditText = root.findViewById(R.id.search_edit_text);
        searchButton = root.findViewById(R.id.search_button);
        zoomInButton = root.findViewById(R.id.button_zoom_in);
        zoomOutButton = root.findViewById(R.id.button_zoom_out);
        trackLocationButton = root.findViewById(R.id.button_track_location);

        geocoder = new Geocoder(requireContext(), Locale.getDefault());
        popupHandler = new Handler(Looper.getMainLooper());

        setupSearch();
        setupButtons();
    }


    private void setupSearch() {
        searchButton.setOnClickListener(v -> performSearch());

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }
    private void performSearch() {
        String searchQuery = searchEditText.getText().toString().trim();
        if (searchQuery.isEmpty()) {
            showToast("Vui lòng nhập địa chỉ cần tìm");
            return;
        }

        // Hide keyboard after search
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        try {
            List<Address> addressList = geocoder.getFromLocationName(searchQuery, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng searchLocation = new LatLng(address.getLatitude(), address.getLongitude());

                // Add marker for searched location
                mMap.clear(); // Clear previous markers
                mMap.addMarker(new MarkerOptions()
                        .position(searchLocation)
                        .title(address.getAddressLine(0))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                // Move camera to searched location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLocation, 15f));
            } else {
                showToast("Không tìm thấy địa điểm");
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage());
            showToast("Lỗi tìm kiếm địa điểm");
        }
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error: Map Fragment is null");
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        setupLocationCallback();
    }

    private void setupButtons() {
        zoomInButton.setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomIn());
        });

        zoomOutButton.setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomOut());
        });

        trackLocationButton.setOnClickListener(v -> {
            if (!isLocationTracking) {
                checkAndRequestLocationPermission();
            } else {
                stopLocationUpdates();
            }
        });
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null && mMap != null) {
                    updateCurrentLocation(location);
                }
            }
        };
    }

    private void updateCurrentLocation(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (currentLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(currentLocation)
                    .title("Vị trí của bạn")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            currentLocationMarker = mMap.addMarker(markerOptions);
        } else {
            currentLocationMarker.setPosition(currentLocation);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f));
    }

    private void checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(3000)
                .build();

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            isLocationTracking = true;
            trackLocationButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isLocationTracking = false;
        trackLocationButton.setImageResource(android.R.drawable.ic_menu_mylocation);
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
            currentLocationMarker = null;
        }
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        potholeApi = retrofit.create(PotholeApi.class);
    }

    private void savePotholeDataToMongoDB(LatLng potholeLocation) {
        if (potholeLocation == null) {
            Log.e(TAG, "Cannot save null location");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        String currentTime = sdf.format(new Date());

        PotholeData potholeData = new PotholeData(
                potholeLocation.latitude,
                potholeLocation.longitude,
                currentTime,
                "User", // Replace with actual user info
                "reported"
        );

        potholeApi.addPothole(potholeData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Đã lưu thông tin ổ gà thành công");
                    addPotholeMarker(potholeLocation);
                } else {
                    Log.e(TAG, "Error saving pothole: " + response.message());
                    showToast("Lỗi khi lưu thông tin ổ gà");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                showToast("Lỗi kết nối mạng");
            }
        });
    }

    private void addPotholeMarker(LatLng location) {
        if (mMap != null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title("Ổ gà")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(markerOptions);
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng defaultLocation = new LatLng(10.870894, 106.803054);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float delta = calculateShakeForce(event.values);
            long currentTime = System.currentTimeMillis();

            if (delta > SHAKE_THRESHOLD && currentTime - lastAlertTime > ALERT_DELAY_MS) {
                lastAlertTime = currentTime;
                showPotholePopup();
            }
        }
    }

    private void showPotholePopup() {
        if (popupView != null) return; // Prevent multiple pop-ups

        if (currentLocationMarker == null) {
            showToast("Không thể lấy vị trí hiện tại");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        popupView = inflater.inflate(R.layout.popup_pothole, null);

        Button btnConfirm = popupView.findViewById(R.id.btn_confirm);

        btnConfirm.setOnClickListener(v -> {
            LatLng potholeLocation = currentLocationMarker.getPosition();
            if (potholeLocation != null) {
                savePotholeDataToMongoDB(potholeLocation);
            }
            removePopup();
        });

        // Add the popup to the screen
        ((ViewGroup) requireView()).addView(popupView);

        // Auto-dismiss after 10 seconds
        popupHandler.postDelayed(this::removePopup, 10000);
    }

    private void removePopup() {
        if (popupView != null && popupView.getParent() != null) {
            ((ViewGroup) popupView.getParent()).removeView(popupView);
            popupView = null;
        }
        popupHandler.removeCallbacksAndMessages(null);
    }

    private float calculateShakeForce(float[] values) {
        float x = values[0];
        float y = values[1];
        float z = values[2];
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
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
        stopLocationUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();
        popupHandler.removeCallbacksAndMessages(null);
    }
}