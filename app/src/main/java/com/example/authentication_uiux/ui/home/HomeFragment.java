package com.example.authentication_uiux.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.authentication_uiux.R;
import com.example.authentication_uiux.models.PotholeData;
import com.example.authentication_uiux.API.PotholeApi;
import com.example.authentication_uiux.ui.home.MapComponents.LocationSearchManager;
import com.example.authentication_uiux.ui.home.MapComponents.NavigationManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.shapes.GHPoint3D;

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
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import android.location.Location;

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
    private GraphHopper hopper;

    private LocationSearchManager searchManager;
    private NavigationManager navigationManager;
    private Location currentLocation;
    private LocationManager locationManager;
    private Polyline routeOverlay;
    private GeoPoint selectedLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize OSMDroid configuration
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE));

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(root);

        initializeSensors();

        initializeMap();

        setupRetrofit();

        initializeGraphHopper("vietnam_latest.osm.pbf");

        searchManager = new LocationSearchManager();
        navigationManager = new NavigationManager();

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
        locationOverlay.runOnFirstFix(() -> {
            GeoPoint userLocation = locationOverlay.getMyLocation();

            if (userLocation != null) {
                requireActivity().runOnUiThread(() -> {
                    // Add current location marker
                    currentLocationMarker = new Marker(mapView);
                    currentLocationMarker.setPosition(userLocation);
                    currentLocationMarker.setTitle("Your Location");
                    mapView.getOverlays().add(currentLocationMarker);

                    mapController.setCenter(userLocation);
                    mapController.setZoom(18.0);

                    isLocationTracking = true;
                    trackLocationButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                });
            } else {
                requireActivity().runOnUiThread(() -> showToast("Unable to get current location"));
            }
        });
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
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            searchManager.searchLocation(query, new LocationSearchManager.SearchCallBack() {
                @Override
                public void onLocationFound(double lat, double lon, String name) {
                    selectedLocation = new GeoPoint(lat, lon);

                    // Animate map to found location
                    mapController.animateTo(selectedLocation);
                    mapController.setZoom(16.0);

                    // Add marker
                    Marker marker = new Marker(mapView);
                    marker.setPosition(selectedLocation);
                    marker.setTitle(name);
                    mapView.getOverlays().add(marker);

                    // Show navigation option
                    showNavigationOption(selectedLocation);
                }

                @Override
                public void onError(String message) {
                    showToast(message);
                }
            });
        } else {
            showToast("Please enter a search query");
        }
    }

    private void showNavigationOption(GeoPoint destination) {
        GeoPoint currentLoc = locationOverlay.getMyLocation();
        if (currentLoc != null) {
            // Remove old route if exists
            if (routeOverlay != null) {
                mapView.getOverlays().remove(routeOverlay);
            }

            // Calculate new route
            navigationManager.getRoute(currentLoc, destination,
                    new NavigationManager.NavigationCallback() {
                        @Override
                        public void onRouteFound(ArrayList<GeoPoint> route, String duration, String distance) {

                        }

                        @Override
                        public void onRouteFound(List<GeoPoint> route, String duration, String distance) {
                            // Draw route
                            routeOverlay = new Polyline();
                            routeOverlay.setPoints(route);
                            routeOverlay.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
                            routeOverlay.setWidth(5f);
                            mapView.getOverlays().add(routeOverlay);

                            // Show info
                            showToast("Distance: " + distance + "\nDuration: " + duration);
                            mapView.invalidate();
                        }

                        @Override
                        public void onError(String message) {
                            showToast(message);
                        }
                    });
        } else {
            showToast("Please enable location services to use navigation");
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        currentLocation = location;
                        locationManager.removeUpdates(this);
                        showPotholePopup(new GeoPoint(location.getLatitude(), location.getLongitude()));
                    }
                });
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
        if(location == null){
            showToast("Invalid Location Data");
            return;
        }

        // Định dạng thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));  // Đảm bảo lưu thời gian UTC
        String currentTime = sdf.format(new Date());

        // Tạo đối tượng Ổ GÀ
        PotholeData potholeData = new PotholeData(
                location.getLatitude(),
                location.getLongitude(),
                currentTime,
                "ThinhTesting",
                "reported"
        );

        // Gửi yêu cầu Lưu dữ liệu lên mongoDB thông qua API
        potholeApi.addPothole(potholeData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    addPotholeMarker(location); // Thêm marker ổ gà vào bản đồ
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

        //Thiết lập tiêu đề và mô tả
        potholeMarker.setTitle("Pothole");
        potholeMarker.setSnippet("Location: " + location.getLatitude() + ", " + location.getLongitude() +
                "\nDetected at: " + getCurrentTime());

        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.pothole_icon, null);
        potholeMarker.setIcon(icon);

        //Hiển thị bong bóng thông tin
        potholeMarker.setOnMarkerClickListener((marker, mapView) -> {
            marker.showInfoWindow();
            return true; //Ngăn việc map di chuyển khi click vào marker
        });

        mapView.getOverlays().add(potholeMarker);
        mapView.invalidate(); //Redraw the map
    }

    // Hàm lấy thời gian hiện tại
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return sdf.format(new Date());
    }

    //Khởi tạo GraphHopper
    private void initializeGraphHopper(String filePath) {
        new Thread(() -> {
            try {
                hopper = new GraphHopper()
                        .setOSMFile(filePath)
                        .setGraphHopperLocation(requireContext().getFilesDir().getAbsolutePath())
                        .setProfiles(new Profile("motorcycle").setVehicle("motorcycle").setWeighting("fastest"))
                        .importOrLoad();
            } catch (Exception e) {
                Log.e("GraphHopper", "Error initializing GraphHopper", e);
            }
        }).start();
    }

    //Tính toán đường đi
    private void calculateRoute(GeoPoint start, GeoPoint end) {
        if (hopper == null) {
            showToast("GraphHopper hasn't initialized yet!");
            return;
        }

        new Thread(() -> {
            GHRequest req = new GHRequest(
                    start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude()
            ).setProfile("motorcycle").setLocale("en");

            GHResponse resp = hopper.route(req);

            if (resp.hasErrors()) {
                Log.e(TAG, "Route errors: " + resp.getErrors());
                showToast("Can't calculate route");
                return;
            }

            List<GeoPoint> routePoints = new ArrayList<>();
            for (GHPoint3D point : resp.getBest().getPoints()) {
                routePoints.add(new GeoPoint(point.getLat(), point.getLon()));
            }

            requireActivity().runOnUiThread(() -> drawRouteOnMap(routePoints));
        }).start();
    }

    //Vẽ đường đi trên bản đồ
    private void drawRouteOnMap(List<GeoPoint> routePoints) {
        Polyline routeOverlay = new Polyline();
        routeOverlay.setPoints(routePoints);
        routeOverlay.setColor(ContextCompat.getColor(requireContext(), R.color.red));
        routeOverlay.setWidth(10.0f);

        mapView.getOverlays().add(routeOverlay);
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

        if (currentLocationMarker == null) {
            currentLocationMarker = new Marker(mapView);
            currentLocationMarker.setPosition(p);
            currentLocationMarker.setTitle("Start Point");
            mapView.getOverlays().add(currentLocationMarker);
        } else {
            calculateRoute(currentLocationMarker.getPosition(), p);
        }

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