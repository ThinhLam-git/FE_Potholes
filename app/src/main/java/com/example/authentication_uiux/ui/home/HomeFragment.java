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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.authentication_uiux.R;
import com.example.authentication_uiux.models.PotholeData;
import com.example.authentication_uiux.API.PotholeApi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements SensorEventListener, MapEventsReceiver {
    private static final String TAG = "HomeFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float SHAKE_THRESHOLD = 30.0f;
    private static final long ALERT_DELAY_MS = 5000;
    private static final String BASE_URL = "http://192.168.124.155:3000/";

    private MapView mapView;
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastAlertTime = 0;
    private View popupView;
    private Handler popupHandler;
    private EditText searchEditText;
    private ImageButton searchButton;
    private ImageButton zoomInButton, zoomOutButton;
    private FloatingActionButton trackLocationButton;
    private Geocoder geocoder;
    private PotholeApi potholeApi;
    private Marker currentLocationMarker;
    private boolean isLocationTracking = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize OSMDroid configuration
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE));

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(root);
        initializeSensors();
        initializeMap();
        setupRetrofit();
        return root;
    }   

    private void initializeViews(View root) {
        mapView = root.findViewById(R.id.map);
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

    private String copyMbTilesToInternalStorage() {
        File destinationFile = new File(requireContext().getFilesDir(), "map.mbtiles");
        if (!destinationFile.exists()) {
            try (InputStream inputStream = requireContext().getAssets().open("map.mbtiles");
                 FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error copying MBTiles file", e);
            }
        }
        return destinationFile.getAbsolutePath();
    }

    private void initializeMap() {
        // Copy MBTiles from assets to internal storage
        String mbTilesPath = copyMbTilesToInternalStorage();

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(10.870894, 106.803054);
        mapController.setCenter(startPoint);

        // Try to use MBTiles if available
        File mbTilesFile = new File(mbTilesPath);
        if (mbTilesFile.exists()) {
            try {
                ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
                SimpleRegisterReceiver registerReceiver = new SimpleRegisterReceiver(requireContext());

                MapTileFileArchiveProvider fileArchiveProvider = new MapTileFileArchiveProvider(
                        registerReceiver,
                        tileSource,
                        new IArchiveFile[]{(IArchiveFile)mbTilesFile}
                );

                MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, registerReceiver,
                        new MapTileFileArchiveProvider[]{fileArchiveProvider});

                mapView.setTileProvider(tileProviderArray);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up MBTiles", e);
            }
        }

        // Location tracking overlay
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        // Map events overlay for tap interactions
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(mapEventsOverlay);
    }

    private void setupButtons() {
        zoomInButton.setOnClickListener(v -> mapController.zoomIn());
        zoomOutButton.setOnClickListener(v -> mapController.zoomOut());

        trackLocationButton.setOnClickListener(v -> {
            if (!isLocationTracking) {
                checkAndRequestLocationPermission();
            } else {
                stopLocationTracking();
            }
        });
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationTracking();
        }
    }

    private void startLocationTracking() {
        locationOverlay.enableFollowLocation();
        GeoPoint userLocation = locationOverlay.getMyLocation();

        if (userLocation != null) {
            // Add current location marker
            currentLocationMarker = new Marker(mapView);
            currentLocationMarker.setPosition(userLocation);
            currentLocationMarker.setTitle("Your Location");
            mapView.getOverlays().add(currentLocationMarker);

            mapController.setCenter(userLocation);
            mapController.setZoom(18.0);

            isLocationTracking = true;
            trackLocationButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            showToast("Unable to get current location");
        }
    }

    private void stopLocationTracking() {
        locationOverlay.disableFollowLocation();

        if (currentLocationMarker != null) {
            mapView.getOverlays().remove(currentLocationMarker);
            currentLocationMarker = null;
        }

        isLocationTracking = false;
        trackLocationButton.setImageResource(android.R.drawable.ic_menu_mylocation);
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
            showToast("Please enter an address");
            return;
        }

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        try {
            List<Address> addressList = geocoder.getFromLocationName(searchQuery, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                GeoPoint searchLocation = new GeoPoint(address.getLatitude(), address.getLongitude());

                // Clear previous markers
                mapView.getOverlays().clear();

                // Add new search marker
                Marker searchMarker = new Marker(mapView);
                searchMarker.setPosition(searchLocation);
                searchMarker.setTitle(address.getAddressLine(0));
                mapView.getOverlays().add(searchMarker);

                // Recenter map
                mapController.setCenter(searchLocation);
                mapController.setZoom(15.0);
                mapView.invalidate();
            } else {
                showToast("Location not found");
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error", e);
            showToast("Search error");
        }
    }

    private void showPotholePopup(GeoPoint location) {
        // Ensure we're on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> showPotholePopup(location));
            return;
        }

        // Remove any existing popup
        removePopup();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        popupView = inflater.inflate(R.layout.popup_pothole, null);

        // Find confirm button
        MaterialButton btnConfirm = popupView.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            // Center the map at the current location
            mapController.setCenter(location);
            mapController.setZoom(18.0); // Optional: Set zoom level

            locationOverlay.disableMyLocation();

            // Add a marker at the current location
            addPotholeMarker(location);

            // Save pothole data to MongoDB
            savePotholeDataToMongoDB(location);
            removePopup();
        });

        // Ensure the root view is not null
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        if (rootView != null) {
            // Create layout params to center the popup
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;

            rootView.addView(popupView, params);
        }

        // Auto-dismiss after 10 seconds
        popupHandler.postDelayed(this::removePopup, 10000);
    }

    private void removePopup() {
        if (popupView != null) {
            ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.removeView(popupView);
            }
            popupView = null;
        }
        popupHandler.removeCallbacksAndMessages(null);
    }

    private void savePotholeDataToMongoDB(GeoPoint location) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        String currentTime = sdf.format(new Date());

        PotholeData potholeData = new PotholeData(
                location.getLatitude(),
                location.getLongitude(),
                currentTime,
                "User",
                "reported"
        );

        potholeApi.addPothole(potholeData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    addPotholeMarker(location);
                    showToast("Pothole saved successfully");
                } else {
                    showToast("Error saving pothole");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showToast("Network error");
            }
        });
    }

    private void addPotholeMarker(GeoPoint location) {
        Marker potholeMarker = new Marker(mapView);
        potholeMarker.setPosition(location);
        potholeMarker.setTitle("Pothole");
        mapView.getOverlays().add(potholeMarker);
        mapView.invalidate();
    }

    // Sensor and lifecycle methods
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float delta = calculateShakeForce(event.values);
            long currentTime = System.currentTimeMillis();

            if (delta > SHAKE_THRESHOLD && currentTime - lastAlertTime > ALERT_DELAY_MS) {
                lastAlertTime = currentTime;

                // Use the main thread to show popup
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Ensure we have a valid location before showing popup
                    GeoPoint currentLocation = locationOverlay.getMyLocation();
                    if (currentLocation != null) {
                        showPotholePopup(currentLocation);
                    } else {
                        showToast("Cannot determine current location");
                    }
                });
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    // MapEventsReceiver methods
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        showPotholePopup(p);
        return true;
    }

    private float calculateShakeForce(float[] values) {
        float x = values[0];
        float y = values[1];
        float z = values[2];
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        potholeApi = retrofit.create(PotholeApi.class);
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE));
        mapView.onResume();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        stopLocationTracking();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDetach();
        popupHandler.removeCallbacksAndMessages(null);
    }
}